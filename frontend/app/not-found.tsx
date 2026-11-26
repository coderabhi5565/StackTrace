import Link from 'next/link'
import { buttonVariants } from '@/components/ui/button'

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 text-center">
      <p className="font-mono text-sm text-primary">404</p>
      <h1 className="text-2xl font-bold">Page not found</h1>
      <Link href="/home" className={buttonVariants()}>
        Go to the feed
      </Link>
    </div>
  )
}
