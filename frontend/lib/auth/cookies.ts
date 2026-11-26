import 'server-only'
import type { NextResponse } from 'next/server'
import { cookieSecure } from '@/lib/config'
import type { LoginResponse, SessionUser } from '@/lib/api/types'

export const ACCESS_COOKIE = 'st_access'
export const REFRESH_COOKIE = 'st_refresh'
/** Non-sensitive, readable by client JS for UI only (never used for authorization). */
export const USER_COOKIE = 'st_user'

const REFRESH_MAX_AGE = 60 * 60 * 24 * 7

export function encodeUser(user: SessionUser): string {
  return Buffer.from(JSON.stringify(user)).toString('base64url')
}

export function decodeUser(value: string | undefined): SessionUser | null {
  if (!value) return null
  try {
    const u = JSON.parse(Buffer.from(value, 'base64url').toString('utf8'))
    return typeof u?.id === 'number' && typeof u?.username === 'string' ? u : null
  } catch {
    return null
  }
}

/** Seconds until the JWT expires (no signature check: the backend verifies it). */
export function secondsUntilExpiry(jwt: string | undefined): number {
  if (!jwt) return -1
  try {
    const payload = JSON.parse(Buffer.from(jwt.split('.')[1], 'base64url').toString('utf8'))
    return typeof payload.exp === 'number' ? payload.exp - Math.floor(Date.now() / 1000) : -1
  } catch {
    return -1
  }
}

export function setSessionCookies(res: NextResponse, login: LoginResponse) {
  const secure = cookieSecure()
  const base = { httpOnly: true, secure, sameSite: 'lax' as const, path: '/' }
  res.cookies.set(ACCESS_COOKIE, login.token, { ...base, maxAge: login.expiresIn })
  res.cookies.set(REFRESH_COOKIE, login.refreshToken, { ...base, maxAge: REFRESH_MAX_AGE })
  res.cookies.set(
    USER_COOKIE,
    encodeUser({ id: login.id, username: login.username, name: login.name }),
    { httpOnly: false, secure, sameSite: 'lax', path: '/', maxAge: REFRESH_MAX_AGE },
  )
}

export function clearSessionCookies(res: NextResponse) {
  for (const name of [ACCESS_COOKIE, REFRESH_COOKIE, USER_COOKIE]) {
    res.cookies.set(name, '', { path: '/', maxAge: 0 })
  }
}
