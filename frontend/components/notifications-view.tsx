'use client'

import { Bell, CheckCheck, MessageSquare, Reply, UserPlus } from 'lucide-react'
import Link from 'next/link'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { api, errorMessage } from '@/lib/api/client'
import type { AppNotification, NotificationType, Page } from '@/lib/api/types'
import { timeAgo } from '@/lib/format'
import { cn } from '@/lib/utils'

const typeConfig: Record<NotificationType, { icon: typeof Bell; color: string }> = {
  FOLLOW: { icon: UserPlus, color: 'text-primary bg-primary/15' },
  COMMENT: { icon: MessageSquare, color: 'text-accent bg-accent/15' },
  REPLY: { icon: Reply, color: 'text-chart-4 bg-chart-4/15' },
}

function linkFor(n: AppNotification): string {
  if (n.type === 'FOLLOW') return n.sender ? `/profile/${n.sender.username}` : '#'
  return n.referenceId ? `/p/${n.referenceId}#comments` : '#'
}

export function NotificationsView() {
  const [pages, setPages] = useState<Page<AppNotification>[]>([])
  const [filter, setFilter] = useState<'all' | 'unread'>('all')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  async function load(page: number) {
    try {
      const res = await api<Page<AppNotification>>(`/notifications?page=${page}&size=20`)
      setPages((prev) => (page === 0 ? [res] : [...prev, res]))
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    api<Page<AppNotification>>('/notifications?page=0&size=20')
      .then((res) => !cancelled && setPages([res]))
      .catch(() => !cancelled && setError(true))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [])

  const items = pages.flatMap((p) => p.content)
  const filtered = filter === 'unread' ? items.filter((n) => !n.isRead) : items
  const unreadCount = items.filter((n) => !n.isRead).length
  const last = pages.at(-1)

  const setRead = (ids: number[] | 'all') =>
    setPages((prev) =>
      prev.map((p) => ({ ...p, content: p.content.map((n) => (ids === 'all' || ids.includes(n.id) ? { ...n, isRead: true } : n)) })),
    )

  async function markAll() {
    try {
      await api('/notifications/read-all', { method: 'PUT' })
      setRead('all')
    } catch (e) {
      toast.error(errorMessage(e))
    }
  }

  function open(n: AppNotification) {
    if (!n.isRead) {
      setRead([n.id])
      api(`/notifications/${n.id}/read`, { method: 'PUT' }).catch(() => undefined)
    }
  }

  return (
    <div className="mx-auto w-full max-w-2xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 font-mono text-2xl font-bold tracking-tight">
            <Bell className="size-6 text-primary" />
            Notifications
          </h1>
          <p className="text-sm text-muted-foreground">{unreadCount > 0 ? `${unreadCount} unread` : "You're all caught up"}</p>
        </div>
        <Button variant="ghost" size="sm" onClick={markAll} disabled={unreadCount === 0}>
          <CheckCheck className="size-4" />
          Mark all read
        </Button>
      </div>

      <div className="mt-4 flex gap-1 rounded-lg border border-border p-1">
        {(['all', 'unread'] as const).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={cn(
              'flex-1 rounded-md px-3 py-1.5 text-sm font-medium capitalize transition-colors',
              filter === f ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {f}
          </button>
        ))}
      </div>

      {error && items.length === 0 ? (
        <div className="mt-4">
          <ErrorState />
        </div>
      ) : loading && items.length === 0 ? (
        <div className="mt-4 flex flex-col gap-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-14 w-full" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="mt-4">
          <EmptyState title="Nothing here." hint="Comments, replies and new followers show up here." />
        </div>
      ) : (
        <Card className="mt-4 divide-y divide-border p-0">
          {filtered.map((n) => {
            const cfg = typeConfig[n.type]
            const Icon = cfg.icon
            return (
              <Link
                key={n.id}
                href={linkFor(n)}
                onClick={() => open(n)}
                className={cn('flex w-full items-start gap-3 px-4 py-3.5 text-left transition-colors hover:bg-secondary/40', !n.isRead && 'bg-primary/[0.04]')}
              >
                <div className={cn('flex size-9 shrink-0 items-center justify-center rounded-xl', cfg.color)}>
                  <Icon className="size-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm leading-snug text-foreground/90">
                    <span className="font-medium">{n.sender ? n.sender.name : 'Someone'}</span> {n.message}
                  </p>
                  <p className="mt-0.5 text-xs text-muted-foreground">{timeAgo(n.createdAt)}</p>
                </div>
                {!n.isRead && <span className="mt-1.5 size-2 shrink-0 rounded-full bg-primary" />}
              </Link>
            )
          })}
        </Card>
      )}

      {last && !last.last && (
        <Button
          variant="outline"
          className="mt-4"
          onClick={() => {
            setLoading(true)
            load(last.page + 1)
          }}
          disabled={loading}
        >
          Load more
        </Button>
      )}
    </div>
  )
}
