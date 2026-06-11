'use client'

import { TrendingUp } from 'lucide-react'
import Link from 'next/link'
import { useState } from 'react'
import { toast } from 'sonner'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { leaderboard, topDevelopers, trending } from '@/lib/mock-data'

function FollowButton({ username }: { username: string }) {
  const [following, setFollowing] = useState(false)
  return (
    <Button
      size="sm"
      variant={following ? 'secondary' : 'outline'}
      className="h-7 rounded-full px-3 text-xs"
      onClick={() => {
        setFollowing((f) => !f)
        toast.success(following ? `Unfollowed @${username}` : `Following @${username}`)
      }}
    >
      {following ? 'Following' : 'Follow'}
    </Button>
  )
}

export function HomeRightSidebar() {
  const medals = ['text-yellow-400', 'text-zinc-300', 'text-amber-600']

  return (
    <div className="flex flex-col gap-4">
      {/* Trending */}
      <section className="rounded-xl border border-border bg-card p-4">
        <h3 className="flex items-center gap-2 text-sm font-semibold">
          <TrendingUp className="size-4 text-primary" />
          Trending this week
        </h3>
        <ol className="mt-3 flex flex-col gap-3">
          {trending.map((a, i) => (
            <li key={a.id}>
              <Link href={`/post/${a.slug}`} className="group flex gap-3">
                <span className="font-mono text-sm font-bold text-muted-foreground">
                  0{i + 1}
                </span>
                <div className="min-w-0">
                  <p className="line-clamp-2 text-sm font-medium leading-snug transition-colors group-hover:text-primary">
                    {a.title}
                  </p>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    @{a.author.username} · {a.views.toLocaleString()} views
                  </p>
                </div>
              </Link>
            </li>
          ))}
        </ol>
      </section>

      {/* Top developers */}
      <section className="rounded-xl border border-border bg-card p-4">
        <h3 className="text-sm font-semibold">Top developers</h3>
        <ul className="mt-3 flex flex-col gap-3">
          {topDevelopers.map((d) => (
            <li key={d.id} className="flex items-center gap-3">
              <Link href={`/profile/${d.username}`}>
                <Avatar className="size-9 border border-border">
                  <AvatarImage src={d.avatar} alt={d.name} />
                  <AvatarFallback>{d.name[0]}</AvatarFallback>
                </Avatar>
              </Link>
              <div className="min-w-0 flex-1">
                <Link
                  href={`/profile/${d.username}`}
                  className="block truncate text-sm font-medium hover:text-primary"
                >
                  {d.name}
                </Link>
                <p className="truncate text-xs text-muted-foreground">
                  @{d.username}
                </p>
              </div>
              <FollowButton username={d.username} />
            </li>
          ))}
        </ul>
      </section>

      {/* Mini leaderboard */}
      <section className="rounded-xl border border-border bg-card p-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold">Leaderboard</h3>
          <Link href="/leaderboard" className="text-xs text-primary hover:underline">
            View all
          </Link>
        </div>
        <ul className="mt-3 flex flex-col gap-2.5">
          {leaderboard.slice(0, 3).map((e, i) => (
            <li key={e.author.id} className="flex items-center gap-3">
              <span className={`font-mono text-sm font-bold ${medals[i]}`}>
                #{e.rank}
              </span>
              <Avatar className="size-7 border border-border">
                <AvatarImage src={e.author.avatar} alt={e.author.name} />
                <AvatarFallback>{e.author.name[0]}</AvatarFallback>
              </Avatar>
              <span className="flex-1 truncate text-sm">{e.author.name}</span>
              <span className="text-xs font-medium text-primary">
                {e.points.toLocaleString()} pts
              </span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
