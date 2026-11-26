import { Trophy } from 'lucide-react'
import Link from 'next/link'
import { AppShell } from '@/components/app-shell'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { Pagination } from '@/components/pagination'
import { Card } from '@/components/ui/card'
import { UserAvatar } from '@/components/user-avatar'
import { apiGet } from '@/lib/api/server'
import type { LeaderboardEntry, Page } from '@/lib/api/types'
import { cn } from '@/lib/utils'

export const metadata = { title: 'Leaderboard · StackTrace' }

export default async function LeaderboardPage({ searchParams }: { searchParams: Promise<{ page?: string }> }) {
  const page = Math.max(0, Number((await searchParams).page ?? 0) || 0)
  let board: Page<LeaderboardEntry> | null = null
  try {
    board = await apiGet<Page<LeaderboardEntry>>(`/api/users/leaderboard?page=${page}&size=20`, { revalidate: 60, tags: ['users'] })
  } catch {
    /* rendered as error state */
  }

  const entries = board?.content ?? []
  const podium = page === 0 ? [entries[1], entries[0], entries[2]].filter(Boolean) : []
  const rest = page === 0 ? entries.slice(3) : entries

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-4xl">
        <h1 className="flex items-center gap-2 font-mono text-2xl font-bold tracking-tight">
          <Trophy className="size-6 text-primary" />
          Leaderboard
        </h1>
        <p className="text-sm text-muted-foreground">
          Top contributors ranked by points: +10 per published article, +1 per like received.
        </p>

        {!board ? (
          <div className="mt-8">
            <ErrorState />
          </div>
        ) : entries.length === 0 ? (
          <div className="mt-8">
            <EmptyState title="No developers yet" />
          </div>
        ) : (
          <>
            {podium.length > 0 && (
              <div className="mt-8 grid grid-cols-3 items-end gap-3">
                {podium.map((e) => {
                  const first = e.rank === 1
                  return (
                    <Link key={e.id} href={`/profile/${e.username}`} className="flex flex-col items-center">
                      <div className="relative">
                        <UserAvatar user={e} className={cn('rounded-2xl border-2', first ? 'size-20 border-primary' : 'size-16 border-border')} />
                        <span
                          className={cn(
                            'absolute -bottom-2 left-1/2 flex size-6 -translate-x-1/2 items-center justify-center rounded-full border-2 border-card text-xs font-bold',
                            e.rank === 1 && 'bg-primary text-primary-foreground',
                            e.rank === 2 && 'bg-muted-foreground text-background',
                            e.rank === 3 && 'bg-accent text-accent-foreground',
                          )}
                        >
                          {e.rank}
                        </span>
                      </div>
                      <p className="mt-4 text-center text-sm font-semibold">{e.name}</p>
                      <p className="font-mono text-xs text-primary">{e.points.toLocaleString()} pts</p>
                      <div className={cn('mt-3 w-full rounded-t-lg bg-secondary/60', first ? 'h-24' : 'h-16')} />
                    </Link>
                  )
                })}
              </div>
            )}

            {rest.length > 0 && (
              <Card className="mt-6 divide-y divide-border p-0">
                {rest.map((e) => (
                  <Link key={e.id} href={`/profile/${e.username}`} className="flex items-center gap-4 px-4 py-3 transition-colors hover:bg-secondary/40">
                    <span className="w-8 text-center font-mono text-sm font-semibold text-muted-foreground">{e.rank}</span>
                    <UserAvatar user={e} className="size-10 rounded-xl" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{e.name}</p>
                      <p className="truncate text-xs text-muted-foreground">@{e.username}</p>
                    </div>
                    <span className="font-mono text-sm font-bold text-primary">{e.points.toLocaleString()}</span>
                  </Link>
                ))}
              </Card>
            )}
            <Pagination page={board.page} totalPages={board.totalPages} hrefFor={(p) => `/leaderboard?page=${p}`} />
          </>
        )}
      </div>
    </AppShell>
  )
}
