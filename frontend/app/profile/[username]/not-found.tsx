import Link from 'next/link'
import { buttonVariants } from '@/components/ui/button'

export default function NotFound() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      <h1 className="text-2xl font-bold">Developer not found</h1>
      <Link href="/leaderboard" className={buttonVariants()}>
        Browse the leaderboard
      </Link>
    </div>
  )
}
