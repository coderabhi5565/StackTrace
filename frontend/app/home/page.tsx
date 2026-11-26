import Link from 'next/link'
import { X } from 'lucide-react'
import { AppShell } from '@/components/app-shell'
import { ArticleCard } from '@/components/article-card'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { FollowingFeed } from '@/components/following-feed'
import { HomeRightSidebar } from '@/components/home-right-sidebar'
import { Pagination } from '@/components/pagination'
import { apiGet } from '@/lib/api/server'
import type { Page, PostSummary } from '@/lib/api/types'
import { cn } from '@/lib/utils'

export const metadata = { title: 'Feed · StackTrace' }

const TABS = [
  { id: 'latest', label: 'Latest' },
  { id: 'trending', label: 'Trending' },
  { id: 'following', label: 'Following' },
] as const

export default async function HomePage({
  searchParams,
}: {
  searchParams: Promise<{ tab?: string; tag?: string; page?: string }>
}) {
  const sp = await searchParams
  const tab = TABS.some((t) => t.id === sp.tab) ? sp.tab! : 'latest'
  const tag = sp.tag?.toLowerCase()
  const page = Math.max(0, Number(sp.page ?? 0) || 0)

  const qs = (p: Record<string, string | number | undefined>) => {
    const q = new URLSearchParams()
    Object.entries(p).forEach(([k, v]) => v !== undefined && v !== '' && q.set(k, String(v)))
    const s = q.toString()
    return s ? `/home?${s}` : '/home'
  }

  let feed: Page<PostSummary> | null = null
  let failed = false
  if (tab !== 'following') {
    const path =
      tab === 'trending'
        ? `/api/posts/trending?page=${page}&size=10`
        : `/api/posts?page=${page}&size=10${tag ? `&tag=${encodeURIComponent(tag)}` : ''}`
    try {
      feed = await apiGet<Page<PostSummary>>(path, { revalidate: 30, tags: ['posts'] })
    } catch {
      failed = true
    }
  }

  return (
    <AppShell rightSidebar={<HomeRightSidebar />}>
      <div className="mx-auto max-w-2xl">
        <div className="mb-5 flex items-center gap-1 rounded-full border border-border bg-card p-1">
          {TABS.map((t) => (
            <Link
              key={t.id}
              href={qs({ tab: t.id === 'latest' ? undefined : t.id, tag: t.id === 'latest' ? tag : undefined })}
              className={cn(
                'flex-1 rounded-full px-4 py-1.5 text-center text-sm font-medium transition-colors',
                tab === t.id ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {t.label}
            </Link>
          ))}
        </div>

        {tag && tab === 'latest' && (
          <div className="mb-4 flex items-center gap-2 text-sm">
            <span className="text-muted-foreground">Filtered by</span>
            <Link href="/home" className="inline-flex items-center gap-1 rounded-full bg-secondary px-3 py-1 font-mono text-xs">
              #{tag} <X className="size-3" />
            </Link>
          </div>
        )}

        {tab === 'following' ? (
          <FollowingFeed />
        ) : failed ? (
          <ErrorState />
        ) : feed && feed.content.length > 0 ? (
          <>
            <div className="flex flex-col gap-4">
              {feed.content.map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
            <Pagination page={feed.page} totalPages={feed.totalPages} hrefFor={(p) => qs({ tab: tab === 'latest' ? undefined : tab, tag, page: p })} />
          </>
        ) : (
          <EmptyState title="No articles yet" hint={tag ? `Nobody has written about #${tag} yet.` : 'Be the first to publish something.'} />
        )}
      </div>
    </AppShell>
  )
}
