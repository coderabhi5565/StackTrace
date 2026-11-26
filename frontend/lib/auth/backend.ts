import 'server-only'
import { apiBaseUrl } from '@/lib/config'
import type { LoginResponse } from '@/lib/api/types'

/** Forward the end-user IP so the gateway rate-limits per client, not per frontend server. */
export function forwardedHeaders(req: Request): Record<string, string> {
  const ip = req.headers.get('x-forwarded-for') ?? req.headers.get('x-real-ip')
  return ip ? { 'X-Forwarded-For': ip.split(',')[0].trim() } : {}
}

export async function refreshSession(refreshToken: string, req?: Request): Promise<LoginResponse | null> {
  try {
    const res = await fetch(`${apiBaseUrl()}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(req ? forwardedHeaders(req) : {}) },
      body: JSON.stringify({ refreshToken }),
      cache: 'no-store',
    })
    return res.ok ? ((await res.json()) as LoginResponse) : null
  } catch {
    return null
  }
}
