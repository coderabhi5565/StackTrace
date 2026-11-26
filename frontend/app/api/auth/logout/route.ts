import { NextResponse } from 'next/server'
import { cookies } from 'next/headers'
import { apiBaseUrl } from '@/lib/config'
import { REFRESH_COOKIE, clearSessionCookies } from '@/lib/auth/cookies'

export async function POST() {
  const refresh = (await cookies()).get(REFRESH_COOKIE)?.value
  if (refresh) {
    // revoke server-side; ignore failures, cookies are cleared regardless
    await fetch(`${apiBaseUrl()}/api/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refresh }),
      cache: 'no-store',
    }).catch(() => undefined)
  }
  const res = new NextResponse(null, { status: 204 })
  clearSessionCookies(res)
  return res
}
