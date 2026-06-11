import Link from 'next/link'
import { TAG_COLORS } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

export function TagPill({
  tag,
  className,
  asLink = true,
}: {
  tag: string
  className?: string
  asLink?: boolean
}) {
  const classes = cn(
    'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors',
    TAG_COLORS[tag] ?? 'text-muted-foreground border-border bg-secondary',
    asLink && 'hover:brightness-125',
    className,
  )

  if (!asLink) {
    return <span className={classes}>#{tag}</span>
  }

  return (
    <Link href={`/search?q=${tag}&tab=tags`} className={classes}>
      #{tag}
    </Link>
  )
}
