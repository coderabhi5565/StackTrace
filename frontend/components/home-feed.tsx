'use client'

import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useState } from 'react'
import { ArticleCard } from '@/components/article-card'
import { ArticleCardSkeleton } from '@/components/article-card-skeleton'
import { Button } from '@/components/ui/button'
import { articles } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

const tabs = ['Latest', 'Trending', 'Following'] as const

export function HomeFeed({ initialTab }: { initialTab?: string }) {
  const [tab, setTab] = useState<(typeof tabs)[number]>(
    initialTab === 'following' ? 'Following' : 'Latest',
  )
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(1)

  useEffect(() => {
    setLoading(true)
    const t = setTimeout(() => setLoading(false), 700)
    return () => clearTimeout(t)
  }, [tab, page])

  const sorted = [...articles].sort((a, b) => {
    if (tab === 'Trending') return b.views - a.views
    if (tab === 'Following') return b.likes - a.likes
    return 0
  })

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-5 flex items-center gap-1 rounded-full border border-border bg-card p-1">
        {tabs.map((t) => (
          <button
            key={t}
            onClick={() => {
              setTab(t)
              setPage(1)
            }}
            className={cn(
              'flex-1 rounded-full px-4 py-1.5 text-sm font-medium transition-colors',
              tab === t
                ? 'bg-primary text-primary-foreground'
                : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="flex flex-col gap-4">
        {loading
          ? Array.from({ length: 4 }).map((_, i) => <ArticleCardSkeleton key={i} />)
          : sorted.map((a) => <ArticleCard key={a.id} article={a} />)}
      </div>

      {!loading && (
        <div className="mt-8 flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="icon"
            disabled={page === 1}
            onClick={() => setPage((p) => Math.max(1, p - 1))}
            aria-label="Previous page"
          >
            <ChevronLeft className="size-4" />
          </Button>
          {[1, 2, 3].map((p) => (
            <Button
              key={p}
              variant={page === p ? 'default' : 'outline'}
              size="icon"
              onClick={() => setPage(p)}
            >
              {p}
            </Button>
          ))}
          <Button
            variant="outline"
            size="icon"
            disabled={page === 3}
            onClick={() => setPage((p) => Math.min(3, p + 1))}
            aria-label="Next page"
          >
            <ChevronRight className="size-4" />
          </Button>
        </div>
      )}
    </div>
  )
}
