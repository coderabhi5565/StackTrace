import { Zap } from 'lucide-react'
import Link from 'next/link'
import { cn } from '@/lib/utils'

export function Logo({
  className,
  href = '/home',
  showText = true,
}: {
  className?: string
  href?: string
  showText?: boolean
}) {
  return (
    <Link
      href={href}
      className={cn('flex items-center gap-2 font-heading', className)}
    >
      <span className="flex size-7 items-center justify-center rounded-md bg-primary text-primary-foreground">
        <Zap className="size-4 fill-current" />
      </span>
      {showText && (
        <span className="text-lg font-bold tracking-tight">
          Stack<span className="text-primary">Trace</span>
        </span>
      )}
    </Link>
  )
}
