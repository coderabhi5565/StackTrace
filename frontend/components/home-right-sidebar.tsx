import { TrendingUp } from 'lucide-react'
import Link from 'next/link'
import { UserAvatar } from '@/components/user-avatar'
import { apiGetSafe } from '@/lib/api/server'
import type { LeaderboardEntry, Page, PostSummary } from '@/lib/api/types'

const emptyPage = { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0, last: true }

export async function HomeRightSidebar() {
  const medals = ['text-yellow-400', 'text-zinc-300', 'text-amber-600']
  // independent requests run in parallel (no waterfall)
  const [trending, leaders] = await Promise.all([
    apiGetSafe<Page<PostSummary>>('/api/posts/trending?size=5', emptyPage, { revalidate: 120, tags: ['posts'] }),
    apiGetSafe<Page<LeaderboardEntry>>('/api/users/leaderboard?size=5', emptyPage, { revalidate: 120, tags: ['users'] }),
  ])

  return (
    <div className="flex flex-col gap-4">
      <section className="rounded-xl border border-border bg-card p-4">
        <h3 className="flex items-center gap-2 text-sm font-semibold">
          <TrendingUp className="size-4 text-primary" />
          Trending
        </h3>
        {trending.content.length === 0 ? (
          <p className="mt-3 text-xs text-muted-foreground">Nothing trending yet. Be the first to publish!</p>
        ) : (
          <ol className="mt-3 flex flex-col gap-3">
            {trending.content.map((a, i) => (
              <li key={a.id}>
                <Link href={`/post/${a.slug}`} className="group flex gap-3">
                  <span className="font-mono text-sm font-bold text-muted-foreground">0{i + 1}</span>
                  <div className="min-w-0">
                    <p className="line-clamp-2 text-sm font-medium leading-snug transition-colors group-hover:text-primary">
                      {a.title}
                    </p>
                    <p className="mt-0.5 text-xs text-muted-foreground">
                      @{a.author.username} · {a.likeCount} likes · {a.commentCount} comments
                    </p>
                  </div>
                </Link>
              </li>
            ))}
          </ol>
        )}
      </section>

      <section className="rounded-xl border border-border bg-card p-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold">Top developers</h3>
          <Link href="/leaderboard" className="text-xs text-primary hover:underline">
            View all
          </Link>
        </div>
        <ul className="mt-3 flex flex-col gap-2.5">
          {leaders.content.map((e, i) => (
            <li key={e.id}>
              <Link href={`/profile/${e.username}`} className="flex items-center gap-3 hover:text-primary">
                <span className={`w-6 font-mono text-sm font-bold ${medals[i] ?? 'text-muted-foreground'}`}>#{e.rank}</span>
                <UserAvatar user={e} className="size-7" />
                <span className="flex-1 truncate text-sm">{e.name}</span>
                <span className="text-xs font-medium text-primary">{e.points.toLocaleString()} pts</span>
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
