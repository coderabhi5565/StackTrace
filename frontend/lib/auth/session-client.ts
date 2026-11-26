'use client'

import { useMemo, useSyncExternalStore } from 'react'
import type { SessionUser } from '@/lib/api/types'

/** Fired after sign-in/sign-out so every useSession() consumer re-reads the cookie. */
export const SESSION_EVENT = 'st-session-change'

export function notifySessionChange() {
  window.dispatchEvent(new Event(SESSION_EVENT))
}

function subscribe(onChange: () => void) {
  window.addEventListener(SESSION_EVENT, onChange)
  window.addEventListener('focus', onChange)
  return () => {
    window.removeEventListener(SESSION_EVENT, onChange)
    window.removeEventListener('focus', onChange)
  }
}

const readRaw = () =>
  document.cookie
    .split('; ')
    .find((c) => c.startsWith('st_user='))
    ?.slice('st_user='.length) ?? ''

function parse(raw: string): SessionUser | null {
  if (!raw) return null
  try {
    const b64 = raw.replace(/-/g, '+').replace(/_/g, '/')
    const bytes = Uint8Array.from(atob(b64), (c) => c.charCodeAt(0))
    const u = JSON.parse(new TextDecoder().decode(bytes))
    return typeof u?.id === 'number' ? u : null
  } catch {
    return null
  }
}

/**
 * Current user for UI purposes (display name, "is this my post"). Authorization is always
 * enforced by the backend; this only reads the non-sensitive st_user cookie.
 * `undefined` during server render / hydration, `null` when signed out.
 */
export function useSession(): SessionUser | null | undefined {
  const raw = useSyncExternalStore<string | undefined>(subscribe, readRaw, () => undefined)
  return useMemo(() => (raw === undefined ? undefined : parse(raw)), [raw])
}
