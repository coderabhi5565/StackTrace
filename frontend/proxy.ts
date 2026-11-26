import { NextResponse, type NextRequest } from 'next/server'
import { refreshSession } from '@/lib/auth/backend'
import {
  ACCESS_COOKIE,
  REFRESH_COOKIE,
  USER_COOKIE,
  clearSessionCookies,
  encodeUser,
  secondsUntilExpiry,
  setSessionCookies,
} from '@/lib/auth/cookies'

const PROTECTED = ['/create', '/edit', '/bookmarks', '/notifications', '/profile/edit']
const GUEST_ONLY = ['/login', '/register']

const matches = (pathname: string, prefixes: string[]) =>
  prefixes.some((p) => pathname === p || pathname.startsWith(`${p}/`))

/**
 * Runs before every page render:
 *  - silently refreshes an expired access token (so server components always see a valid one)
 *  - protects private routes and keeps signed-in users away from /login and /register
 */
export async function proxy(req: NextRequest) {
  const { pathname, search } = req.nextUrl
  let access = req.cookies.get(ACCESS_COOKIE)?.value
  const refresh = req.cookies.get(REFRESH_COOKIE)?.value
  let response: NextResponse | null = null

  if (refresh && secondsUntilExpiry(access) < 15) {
    const login = await refreshSession(refresh, req)
    if (login) {
      access = login.token
      // make the new cookies visible to this render, and persist them in the browser
      const headers = new Headers(req.headers)
      const others = req.cookies
        .getAll()
        .filter((c) => ![ACCESS_COOKIE, REFRESH_COOKIE, USER_COOKIE].includes(c.name))
        .map((c) => `${c.name}=${c.value}`)
      headers.set(
        'cookie',
        [
          ...others,
          `${ACCESS_COOKIE}=${login.token}`,
          `${REFRESH_COOKIE}=${login.refreshToken}`,
          `${USER_COOKIE}=${encodeUser({ id: login.id, username: login.username, name: login.name })}`,
        ].join('; '),
      )
      response = NextResponse.next({ request: { headers } })
      setSessionCookies(response, login)
    } else {
      access = undefined
      response = NextResponse.next()
      clearSessionCookies(response)
    }
  }

  const signedIn = Boolean(access)
  if (!signedIn && matches(pathname, PROTECTED)) {
    const url = new URL('/login', req.url)
    url.searchParams.set('next', pathname + search)
    const redirect = NextResponse.redirect(url)
    if (response) response.cookies.getAll().forEach((c) => redirect.cookies.set(c))
    return redirect
  }
  if (signedIn && matches(pathname, GUEST_ONLY)) {
    return NextResponse.redirect(new URL('/home', req.url))
  }
  return response ?? NextResponse.next()
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico|icon|apple-icon|avatars|.*\\.(?:svg|png|jpg|jpeg|webp|ico)$).*)'],
}
