'use client'

import { ApiError } from './errors'
import type { ApiErrorBody } from './types'

/**
 * Browser-side calls go through the same-origin BFF proxy (/api/proxy/...), which attaches
 * the httpOnly access token and refreshes it transparently. Tokens never reach JS.
 */
export async function api<T>(path: string, init: { method?: string; body?: unknown } = {}): Promise<T> {
  const res = await fetch(`/api/proxy${path}`, {
    method: init.method ?? 'GET',
    headers: init.body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
    body: init.body !== undefined ? JSON.stringify(init.body) : undefined,
    credentials: 'same-origin',
  })
  if (res.status === 204) return undefined as T
  let data: unknown = null
  try {
    data = await res.json()
  } catch {
    /* empty body */
  }
  if (!res.ok) {
    throw new ApiError(res.status, data as ApiErrorBody | null, res.status === 401 ? 'Please sign in' : undefined)
  }
  return data as T
}

export function errorMessage(e: unknown, fallback = 'Something went wrong'): string {
  if (e instanceof ApiError) {
    if (e.status === 429) return 'Too many requests, please slow down.'
    return e.message || fallback
  }
  return fallback
}
