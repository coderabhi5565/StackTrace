import 'server-only'

/** Base URL of the API gateway, server-side only (never exposed to the browser). */
export function apiBaseUrl(): string {
  const url = process.env.API_BASE_URL
  if (!url) {
    throw new Error('API_BASE_URL is not set (e.g. http://localhost:8080)')
  }
  return url.replace(/\/$/, '')
}

/** Cookies are Secure in production unless explicitly disabled (plain-http local docker runs). */
export function cookieSecure(): boolean {
  if (process.env.COOKIE_SECURE) return process.env.COOKIE_SECURE === 'true'
  return process.env.NODE_ENV === 'production'
}
