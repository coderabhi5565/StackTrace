"use client"

import { useState } from "react"
import { AppShell } from "@/components/app-shell"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { notifications as initial, type Notification } from "@/lib/mock-data"
import { UserPlus, MessageSquare, Flag, Brain, Award, Heart, Bell, CheckCheck } from "lucide-react"
import { cn } from "@/lib/utils"

const typeConfig: Record<
  Notification["type"],
  { icon: typeof Bell; color: string }
> = {
  follow: { icon: UserPlus, color: "text-primary bg-primary/15" },
  comment: { icon: MessageSquare, color: "text-accent bg-accent/15" },
  flag: { icon: Flag, color: "text-destructive bg-destructive/15" },
  quiz: { icon: Brain, color: "text-chart-4 bg-chart-4/15" },
  badge: { icon: Award, color: "text-primary bg-primary/15" },
  like: { icon: Heart, color: "text-destructive bg-destructive/15" },
}

export default function NotificationsPage() {
  const [items, setItems] = useState(initial)
  const [filter, setFilter] = useState<"all" | "unread">("all")

  const filtered = filter === "unread" ? items.filter((n) => !n.read) : items
  const unreadCount = items.filter((n) => !n.read).length

  function markAll() {
    setItems((prev) => prev.map((n) => ({ ...n, read: true })))
  }

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-2xl">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="flex items-center gap-2 font-mono text-2xl font-bold tracking-tight">
              <Bell className="size-6 text-primary" />
              Notifications
            </h1>
            <p className="text-sm text-muted-foreground">
              {unreadCount > 0 ? `${unreadCount} unread` : "You're all caught up"}
            </p>
          </div>
          <Button variant="ghost" size="sm" onClick={markAll} disabled={unreadCount === 0}>
            <CheckCheck className="mr-1.5 size-4" />
            Mark all read
          </Button>
        </div>

        <div className="mt-4 flex gap-1 rounded-lg border border-border p-1">
          {(["all", "unread"] as const).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={cn(
                "flex-1 rounded-md px-3 py-1.5 text-sm font-medium capitalize transition-colors",
                filter === f ? "bg-secondary text-foreground" : "text-muted-foreground hover:text-foreground",
              )}
            >
              {f}
            </button>
          ))}
        </div>

        <Card className="mt-4 divide-y divide-border p-0">
          {filtered.length === 0 && (
            <p className="py-12 text-center text-sm text-muted-foreground">Nothing here.</p>
          )}
          {filtered.map((n) => {
            const cfg = typeConfig[n.type]
            const Icon = cfg.icon
            return (
              <button
                key={n.id}
                onClick={() => setItems((prev) => prev.map((x) => (x.id === n.id ? { ...x, read: true } : x)))}
                className={cn(
                  "flex w-full items-start gap-3 px-4 py-3.5 text-left transition-colors hover:bg-secondary/40",
                  !n.read && "bg-primary/[0.04]",
                )}
              >
                <div className={cn("flex size-9 shrink-0 items-center justify-center rounded-xl", cfg.color)}>
                  <Icon className="size-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm leading-snug text-foreground/90">{n.text}</p>
                  <p className="mt-0.5 text-xs text-muted-foreground">{n.time}</p>
                </div>
                {!n.read && <span className="mt-1.5 size-2 shrink-0 rounded-full bg-primary" />}
              </button>
            )
          })}
        </Card>
      </div>
    </AppShell>
  )
}
