import { NextResponse } from 'next/server'
import { apiBaseUrl } from '@/lib/config'
import { forwardedHeaders } from '@/lib/auth/backend'
import { setSessionCookies } from '@/lib/auth/cookies'
import type { LoginResponse } from '@/lib/api/types'

export async function POST(req: Request) {
  const body = await req.json().catch(() => null)
  const res = await fetch(`${apiBaseUrl()}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...forwardedHeaders(req) },
    body: JSON.stringify({ email: body?.email, password: body?.password }),
    cache: 'no-store',
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    return NextResponse.json(data, { status: res.status })
  }
  const login = data as LoginResponse
  const out = NextResponse.json({ user: { id: login.id, username: login.username, name: login.name } })
  setSessionCookies(out, login)
  return out
}
