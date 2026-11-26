'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { ArticleCard } from '@/components/article-card'
import { ArticleCardSkeleton } from '@/components/article-card-skeleton'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { Button, buttonVariants } from '@/components/ui/button'
import { api } from '@/lib/api/client'
import type { Page, PostSummary } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'

/** Personalised feed: fetched client-side through the BFF so public pages stay cacheable. */
export function FollowingFeed() {
  const session = useSession()
  const [pages, setPages] = useState<Page<PostSummary>[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  async function load(page: number) {
    try {
      const res = await api<Page<PostSummary>>(`/posts/feed/following?page=${page}&size=10`)
      setPages((prev) => (page === 0 ? [res] : [...prev, res]))
      setError(false)
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!session) return
    let cancelled = false
    api<Page<PostSummary>>('/posts/feed/following?page=0&size=10')
      .then((res) => !cancelled && (setPages([res]), setError(false)))
      .catch(() => !cancelled && setError(true))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [session])

  if (session === null) {
    return (
      <EmptyState title="Sign in to see posts from developers you follow">
        <Link href="/login?next=/home?tab=following" className={buttonVariants()}>
          Sign in
        </Link>
      </EmptyState>
    )
  }
  if (error && pages.length === 0) return <ErrorState />

  const posts = pages.flatMap((p) => p.content)
  const last = pages.at(-1)

  if (!loading && posts.length === 0) {
    return <EmptyState title="Your following feed is empty" hint="Follow developers from their profile or the leaderboard." />
  }

  return (
    <div className="flex flex-col gap-4">
      {posts.map((a) => (
        <ArticleCard key={a.id} article={a} />
      ))}
      {loading && Array.from({ length: 3 }).map((_, i) => <ArticleCardSkeleton key={i} />)}
      {!loading && last && !last.last && (
        <Button
          variant="outline"
          onClick={() => {
            setLoading(true)
            load(last.page + 1)
          }}
          className="mx-auto mt-2"
        >
          Load more
        </Button>
      )}
    </div>
  )
}
