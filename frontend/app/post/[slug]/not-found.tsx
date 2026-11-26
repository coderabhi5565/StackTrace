import Link from 'next/link'
import { buttonVariants } from '@/components/ui/button'

export default function NotFound() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      <h1 className="text-2xl font-bold">Article not found</h1>
      <p className="text-muted-foreground">It may have been deleted, or it is still a draft.</p>
      <Link href="/home" className={buttonVariants()}>
        Back to the feed
      </Link>
    </div>
  )
}
