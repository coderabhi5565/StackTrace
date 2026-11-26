'use client'

import { Search as SearchIcon } from 'lucide-react'
import Link from 'next/link'
import { usePathname, useRouter, useSearchParams } from 'next/navigation'
import { useEffect, useState } from 'react'
import { ArticleCard } from '@/components/article-card'
import { ArticleCardSkeleton } from '@/components/article-card-skeleton'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { UserAvatar } from '@/components/user-avatar'
import { api } from '@/lib/api/client'
import type { SearchResult } from '@/lib/api/types'
import { cn } from '@/lib/utils'

const TYPES = [
  { id: 'ALL', label: 'All' },
  { id: 'POST', label: 'Articles' },
  { id: 'USER', label: 'Developers' },
] as const

export function SearchView() {
  const params = useSearchParams()
  const router = useRouter()
  const pathname = usePathname()
  const q = params.get('q') ?? ''
  const type = (params.get('type') ?? 'ALL').toUpperCase()
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0)

  const [input, setInput] = useState(q)
  // keep the box in sync when the URL changes (back button, navbar search)
  const [prevQ, setPrevQ] = useState(q)
  if (q !== prevQ) {
    setPrevQ(q)
    setInput(q)
  }

  // results are keyed by the query they belong to; a key mismatch means "loading"
  const key = q ? `${q}|${type}|${page}` : ''
  const [state, setState] = useState<{ key: string; result?: SearchResult; error?: boolean }>({ key: '' })

  // debounce typing into the URL (shareable, back-button friendly)
  useEffect(() => {
    if (input.trim() === q) return
    const t = setTimeout(() => navigate({ q: input.trim(), page: undefined }), 350)
    return () => clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [input])

  useEffect(() => {
    if (!key) return
    let cancelled = false
    api<SearchResult>(`/search?keyword=${encodeURIComponent(q)}&type=${type}&page=${page}&size=10`)
      .then((result) => !cancelled && setState({ key, result }))
      .catch(() => !cancelled && setState({ key, error: true }))
    return () => {
      cancelled = true
    }
  }, [key, q, type, page])

  const current = state.key === key ? state : null
  const loading = Boolean(key) && !current
  const error = Boolean(current?.error)
  // while a new query loads, keep showing the previous results (dimmed)
  const result = key ? (current?.result ?? state.result ?? null) : null

  function navigate(next: Record<string, string | undefined>) {
    const sp = new URLSearchParams(params.toString())
    Object.entries(next).forEach(([k, v]) => (v ? sp.set(k, v) : sp.delete(k)))
    router.replace(`${pathname}?${sp.toString()}`, { scroll: false })
  }

  const posts = result?.posts.content ?? []
  const users = result?.users.content ?? []
  const totalPages = Math.max(result?.posts.totalPages ?? 0, result?.users.totalPages ?? 0)

  return (
    <div className="mx-auto w-full max-w-3xl">
      <h1 className="font-mono text-2xl font-bold tracking-tight">Search</h1>

      <div className="relative mt-4">
        <SearchIcon className="absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Search articles, topics, developers…"
          aria-label="Search query"
          className="h-12 pl-11 text-base"
          maxLength={200}
          autoFocus
        />
      </div>

      <div className="mt-4 flex gap-1 rounded-lg border border-border p-1">
        {TYPES.map((t) => (
          <button
            key={t.id}
            onClick={() => navigate({ type: t.id === 'ALL' ? undefined : t.id, page: undefined })}
            className={cn(
              'flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
              type === t.id ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {t.label}
          </button>
        ))}
      </div>

      {!q ? (
        <p className="mt-10 text-center text-sm text-muted-foreground">Type to search articles (full-text) and developers.</p>
      ) : error ? (
        <div className="mt-6">
          <ErrorState message="Search is unavailable right now." />
        </div>
      ) : loading && !result ? (
        <div className="mt-6 flex flex-col gap-4">
          <ArticleCardSkeleton />
          <ArticleCardSkeleton />
        </div>
      ) : result && posts.length === 0 && users.length === 0 ? (
        <div className="mt-6">
          <EmptyState title={`No results for “${q}”`} hint="Try different keywords." />
        </div>
      ) : (
        <div className={cn('mt-6 flex flex-col gap-6', loading && 'opacity-60')}>
          {result?.degraded.length ? (
            <p className="text-xs text-warning">Some results are temporarily unavailable ({result.degraded.join(', ')}).</p>
          ) : null}

          {users.length > 0 && (
            <section>
              <h2 className="mb-2 text-sm font-semibold text-muted-foreground">Developers ({result?.users.totalElements})</h2>
              <Card className="divide-y divide-border p-0">
                {users.map((u) => (
                  <Link key={u.id} href={`/profile/${u.username}`} className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-secondary/40">
                    <UserAvatar user={u} />
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium">{u.name}</p>
                      <p className="truncate text-xs text-muted-foreground">@{u.username}</p>
                    </div>
                  </Link>
                ))}
              </Card>
            </section>
          )}

          {posts.length > 0 && (
            <section>
              <h2 className="mb-2 text-sm font-semibold text-muted-foreground">Articles ({result?.posts.totalElements})</h2>
              <div className="flex flex-col gap-4">
                {posts.map((a) => (
                  <ArticleCard key={a.id} article={a} />
                ))}
              </div>
            </section>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3">
              <Button variant="outline" disabled={page === 0} onClick={() => navigate({ page: String(page - 1) })}>
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </span>
              <Button variant="outline" disabled={page >= totalPages - 1} onClick={() => navigate({ page: String(page + 1) })}>
                Next
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
