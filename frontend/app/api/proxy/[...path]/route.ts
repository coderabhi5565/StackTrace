import { NextResponse, type NextRequest } from 'next/server'
import { revalidateTag } from 'next/cache'
import { apiBaseUrl } from '@/lib/config'
import { forwardedHeaders, refreshSession } from '@/lib/auth/backend'
import {
  ACCESS_COOKIE,
  REFRESH_COOKIE,
  clearSessionCookies,
  secondsUntilExpiry,
  setSessionCookies,
} from '@/lib/auth/cookies'
import type { LoginResponse } from '@/lib/api/types'

/**
 * Backend-for-frontend proxy: the browser calls /api/proxy/<path>, we forward to
 * <gateway>/api/<path> with the httpOnly access token, refreshing it once if needed.
 */
async function handle(req: NextRequest, ctx: { params: Promise<{ path: string[] }> }) {
  const { path } = await ctx.params
  const method = req.method

  // CSRF defence in depth (cookies are SameSite=Lax already): mutations must be same-origin.
  if (method !== 'GET') {
    const origin = req.headers.get('origin')
    if (origin && new URL(origin).host !== req.headers.get('host')) {
      return NextResponse.json({ status: 403, message: 'Cross-origin request blocked' }, { status: 403 })
    }
  }
  if (path.some((p) => p === '..' || p.includes('/'))) {
    return NextResponse.json({ status: 400, message: 'Bad path' }, { status: 400 })
  }

  const target = `${apiBaseUrl()}/api/${path.map(encodeURIComponent).join('/')}${req.nextUrl.search}`
  const body = method === 'GET' || method === 'HEAD' ? undefined : await req.text()

  let access = req.cookies.get(ACCESS_COOKIE)?.value
  const refresh = req.cookies.get(REFRESH_COOKIE)?.value
  let refreshed: LoginResponse | null = null
  let refreshFailed = false

  const doRefresh = async () => {
    if (!refresh || refreshed || refreshFailed) return
    refreshed = await refreshSession(refresh, req)
    if (refreshed) access = refreshed.token
    else refreshFailed = true
  }

  if (refresh && secondsUntilExpiry(access) < 15) await doRefresh()

  const send = () =>
    fetch(target, {
      method,
      headers: {
        Accept: 'application/json',
        ...(body ? { 'Content-Type': req.headers.get('content-type') ?? 'application/json' } : {}),
        ...(access ? { Authorization: `Bearer ${access}` } : {}),
        ...forwardedHeaders(req),
      },
      body,
      cache: 'no-store',
    })

  let upstream = await send()
  if (upstream.status === 401 && refresh && !refreshed && !refreshFailed) {
    await doRefresh()
    if (refreshed) upstream = await send()
  }

  const out = new NextResponse(upstream.status === 204 ? null : await upstream.arrayBuffer(), {
    status: upstream.status,
    headers: { 'Content-Type': upstream.headers.get('content-type') ?? 'application/json' },
  })
  if (refreshed) setSessionCookies(out, refreshed)
  else if (refreshFailed) clearSessionCookies(out)

  if (method !== 'GET' && upstream.ok) invalidateCaches(path)
  return out
}

/**
 * On-demand ISR: refresh cached public pages affected by a successful content write.
 * Likes/bookmarks/comments are skipped: their counters are fetched live by the page.
 */
function invalidateCaches(path: string[]) {
  const [resource, , action] = path
  if (resource === 'posts' && (action === undefined || ['publish', 'schedule', 'cancel-schedule'].includes(action))) {
    revalidateTag('posts', { expire: 0 })
    revalidateTag('tags', 'max')
  } else if (resource === 'users') {
    revalidateTag('users', { expire: 0 })
  }
}

export { handle as GET, handle as POST, handle as PUT, handle as PATCH, handle as DELETE }
