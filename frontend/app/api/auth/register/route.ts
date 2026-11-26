import { NextResponse } from 'next/server'
import { apiBaseUrl } from '@/lib/config'
import { forwardedHeaders } from '@/lib/auth/backend'
import { setSessionCookies } from '@/lib/auth/cookies'
import type { LoginResponse } from '@/lib/api/types'

/** Registers, then signs the new user in (sets session cookies). */
export async function POST(req: Request) {
  const body = await req.json().catch(() => null)
  const headers = { 'Content-Type': 'application/json', ...forwardedHeaders(req) }
  const reg = await fetch(`${apiBaseUrl()}/api/auth/register`, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      name: body?.name,
      username: body?.username,
      email: body?.email,
      password: body?.password,
    }),
    cache: 'no-store',
  })
  if (!reg.ok) {
    return NextResponse.json(await reg.json().catch(() => ({})), { status: reg.status })
  }
  const login = await fetch(`${apiBaseUrl()}/api/auth/login`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ email: body?.email, password: body?.password }),
    cache: 'no-store',
  })
  if (!login.ok) {
    return NextResponse.json({ message: 'Account created, please sign in.' }, { status: 201 })
  }
  const data = (await login.json()) as LoginResponse
  const out = NextResponse.json({ user: { id: data.id, username: data.username, name: data.name } }, { status: 201 })
  setSessionCookies(out, data)
  return out
}
