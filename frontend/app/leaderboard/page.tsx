"use client"

import { useState } from "react"
import Image from "next/image"
import Link from "next/link"
import { AppShell } from "@/components/app-shell"
import { Card } from "@/components/ui/card"
import { leaderboard } from "@/lib/mock-data"
import { Trophy, BookOpen, BadgeCheck } from "lucide-react"
import { cn } from "@/lib/utils"

const ranges = ["This week", "This month", "All time"]

export default function LeaderboardPage() {
  const [range, setRange] = useState("All time")
  const top3 = leaderboard.slice(0, 3)
  const rest = leaderboard.slice(3)

  // reorder podium: 2nd, 1st, 3rd
  const podium = [top3[1], top3[0], top3[2]].filter(Boolean)

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-4xl">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="flex items-center gap-2 font-mono text-2xl font-bold tracking-tight">
              <Trophy className="size-6 text-primary" />
              Leaderboard
            </h1>
            <p className="text-sm text-muted-foreground">Top contributors ranked by points earned.</p>
          </div>
          <div className="flex rounded-lg border border-border p-1">
            {ranges.map((r) => (
              <button
                key={r}
                onClick={() => setRange(r)}
                className={cn(
                  "rounded-md px-3 py-1.5 text-xs font-medium transition-colors",
                  range === r ? "bg-secondary text-foreground" : "text-muted-foreground hover:text-foreground",
                )}
              >
                {r}
              </button>
            ))}
          </div>
        </div>

        {/* Podium */}
        <div className="mt-8 grid grid-cols-3 items-end gap-3">
          {podium.map((entry) => {
            const isFirst = entry.rank === 1
            return (
              <div key={entry.author.id} className="flex flex-col items-center">
                <div className="relative">
                  <Image
                    src={entry.author.avatar || "/placeholder.svg"}
                    alt={entry.author.name}
                    width={isFirst ? 80 : 60}
                    height={isFirst ? 80 : 60}
                    className={cn(
                      "rounded-2xl border-2 object-cover",
                      isFirst ? "size-20 border-primary" : "size-15 border-border",
                    )}
                  />
                  <span
                    className={cn(
                      "absolute -bottom-2 left-1/2 flex size-6 -translate-x-1/2 items-center justify-center rounded-full border-2 border-card text-xs font-bold",
                      entry.rank === 1 && "bg-primary text-primary-foreground",
                      entry.rank === 2 && "bg-muted-foreground text-background",
                      entry.rank === 3 && "bg-accent text-accent-foreground",
                    )}
                  >
                    {entry.rank}
                  </span>
                </div>
                <p className="mt-4 text-center text-sm font-semibold">{entry.author.name}</p>
                <p className="font-mono text-xs text-primary">{entry.points.toLocaleString()} pts</p>
                <div
                  className={cn(
                    "mt-3 w-full rounded-t-lg bg-secondary/60",
                    isFirst ? "h-24" : "h-16",
                  )}
                />
              </div>
            )
          })}
        </div>

        {/* Rest of list */}
        <Card className="mt-6 divide-y divide-border p-0">
          {rest.map((entry) => (
            <Link
              key={entry.author.id}
              href="/profile"
              className="flex items-center gap-4 px-4 py-3 transition-colors hover:bg-secondary/40"
            >
              <span className="w-6 text-center font-mono text-sm font-semibold text-muted-foreground">
                {entry.rank}
              </span>
              <Image
                src={entry.author.avatar || "/placeholder.svg"}
                alt={entry.author.name}
                width={40}
                height={40}
                className="size-10 rounded-xl object-cover"
              />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">{entry.author.name}</p>
                <p className="truncate text-xs text-muted-foreground">@{entry.author.username}</p>
              </div>
              <div className="hidden items-center gap-1.5 text-xs text-muted-foreground sm:flex">
                <BookOpen className="size-3.5" />
                {entry.articles}
              </div>
              <div className="hidden items-center gap-1.5 text-xs text-muted-foreground sm:flex">
                <BadgeCheck className="size-3.5" />
                {entry.badges}
              </div>
              <span className="font-mono text-sm font-bold text-primary">{entry.points.toLocaleString()}</span>
            </Link>
          ))}
        </Card>
      </div>
    </AppShell>
  )
}
