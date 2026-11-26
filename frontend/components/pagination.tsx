import { ChevronLeft, ChevronRight } from 'lucide-react'
import Link from 'next/link'
import { buttonVariants } from '@/components/ui/button'
import { cn } from '@/lib/utils'

/** Link-based pagination (works in server components; page is 0-based). */
export function Pagination({
  page,
  totalPages,
  hrefFor,
}: {
  page: number
  totalPages: number
  hrefFor: (page: number) => string
}) {
  if (totalPages <= 1) return null
  const prev = page > 0
  const next = page < totalPages - 1
  return (
    <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Pagination">
      <Link
        aria-disabled={!prev}
        href={prev ? hrefFor(page - 1) : '#'}
        className={cn(buttonVariants({ variant: 'outline', size: 'icon' }), !prev && 'pointer-events-none opacity-50')}
        aria-label="Previous page"
      >
        <ChevronLeft className="size-4" />
      </Link>
      <span className="text-sm text-muted-foreground">
        Page {page + 1} of {totalPages}
      </span>
      <Link
        aria-disabled={!next}
        href={next ? hrefFor(page + 1) : '#'}
        className={cn(buttonVariants({ variant: 'outline', size: 'icon' }), !next && 'pointer-events-none opacity-50')}
        aria-label="Next page"
      >
        <ChevronRight className="size-4" />
      </Link>
    </nav>
  )
}
