import 'server-only'
import { cookies } from 'next/headers'
import { apiBaseUrl } from '@/lib/config'
import { ACCESS_COOKIE } from '@/lib/auth/cookies'
import { ApiError } from './errors'
import type { ApiErrorBody } from './types'

type GetOptions = {
  /** Send the user's access token (makes the request per-user and uncached). */
  auth?: boolean
  /** ISR: seconds to cache a public response. Ignored when auth is true. */
  revalidate?: number
  tags?: string[]
}

/**
 * Server-side GET against the gateway. Public requests are cached (ISR) and tagged so
 * mutations can revalidate them; authenticated requests are never cached.
 */
export async function apiGet<T>(path: string, opts: GetOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  let init: RequestInit & { next?: { revalidate?: number; tags?: string[] } }
  if (opts.auth) {
    const token = (await cookies()).get(ACCESS_COOKIE)?.value
    if (token) headers.Authorization = `Bearer ${token}`
    init = { headers, cache: 'no-store' }
  } else {
    init = { headers, next: { revalidate: opts.revalidate ?? 60, tags: opts.tags } }
  }
  const res = await fetch(`${apiBaseUrl()}${path}`, init)
  if (!res.ok) {
    let body: ApiErrorBody | null = null
    try {
      body = await res.json()
    } catch {
      /* non-JSON error */
    }
    throw new ApiError(res.status, body)
  }
  return (await res.json()) as T
}

/** Like apiGet but returns null on 404. */
export async function apiGetOrNull<T>(path: string, opts: GetOptions = {}): Promise<T | null> {
  try {
    return await apiGet<T>(path, opts)
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null
    throw e
  }
}

/** For non-critical page sections: render a fallback instead of failing the page. */
export async function apiGetSafe<T>(path: string, fallback: T, opts: GetOptions = {}): Promise<T> {
  try {
    return await apiGet<T>(path, opts)
  } catch (e) {
    console.error(`GET ${path} failed:`, e instanceof Error ? e.message : e)
    return fallback
  }
}
