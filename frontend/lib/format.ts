const MINUTE = 60
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR

export function timeAgo(iso: string | null | undefined): string {
  if (!iso) return ''
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000))
  if (seconds < MINUTE) return 'just now'
  if (seconds < HOUR) return `${Math.floor(seconds / MINUTE)}m ago`
  if (seconds < DAY) return `${Math.floor(seconds / HOUR)}h ago`
  if (seconds < 7 * DAY) return `${Math.floor(seconds / DAY)}d ago`
  return formatDate(iso)
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

export function compact(n: number): string {
  return n >= 1000 ? `${(n / 1000).toFixed(n >= 10000 ? 0 : 1)}k` : `${n}`
}

export function initials(name: string | null | undefined): string {
  return (name ?? '?').trim().charAt(0).toUpperCase() || '?'
}

export function plural(n: number, word: string): string {
  return `${n.toLocaleString()} ${word}${n === 1 ? '' : 's'}`
}
