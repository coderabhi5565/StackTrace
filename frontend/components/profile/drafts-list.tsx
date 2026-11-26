'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { ArticleCard } from '@/components/article-card'
import { ArticleCardSkeleton } from '@/components/article-card-skeleton'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { buttonVariants } from '@/components/ui/button'
import { api } from '@/lib/api/client'
import type { Page, PostSummary } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'

/** Drafts and scheduled posts are private: only shown to their owner, fetched with their token. */
export function DraftsList({ ownerId }: { ownerId: number }) {
  const session = useSession()
  const [posts, setPosts] = useState<PostSummary[] | null>(null)
  const [error, setError] = useState(false)
  const isOwner = session?.id === ownerId

  useEffect(() => {
    if (!isOwner) return
    Promise.all([
      api<Page<PostSummary>>('/posts/me?status=DRAFT&size=50'),
      api<Page<PostSummary>>('/posts/me?status=SCHEDULED&size=50'),
    ])
      .then(([d, s]) => setPosts([...s.content, ...d.content]))
      .catch(() => setError(true))
  }, [isOwner])

  if (session === undefined) return <ArticleCardSkeleton />
  if (!isOwner) return <EmptyState title="Drafts are private." />
  if (error) return <ErrorState />
  if (!posts) return <ArticleCardSkeleton />
  if (posts.length === 0) {
    return (
      <EmptyState title="No drafts yet.">
        <Link href="/create" className={buttonVariants()}>
          Start writing
        </Link>
      </EmptyState>
    )
  }
  return (
    <div className="flex flex-col gap-4">
      {posts.map((p) => (
        <ArticleCard key={p.id} article={p} showStatus />
      ))}
    </div>
  )
}
