import { Bookmark } from 'lucide-react'
import { AppShell } from '@/components/app-shell'
import { ArticleCard } from '@/components/article-card'
import { EmptyState, ErrorState } from '@/components/empty-state'
import { Pagination } from '@/components/pagination'
import { apiGet } from '@/lib/api/server'
import type { Page, PostSummary } from '@/lib/api/types'

export const metadata = { title: 'Bookmarks · StackTrace' }

// Per-user page (protected by proxy.ts): rendered on every request with the user's token.
export default async function BookmarksPage({ searchParams }: { searchParams: Promise<{ page?: string }> }) {
  const page = Math.max(0, Number((await searchParams).page ?? 0) || 0)
  let data: Page<PostSummary> | null = null
  try {
    data = await apiGet<Page<PostSummary>>(`/api/posts/bookmarks?page=${page}&size=10`, { auth: true })
  } catch {
    /* error state */
  }

  return (
    <AppShell>
      <div className="mx-auto max-w-2xl">
        <h1 className="mb-5 flex items-center gap-2 font-mono text-2xl font-bold tracking-tight">
          <Bookmark className="size-6 text-primary" />
          Bookmarks
        </h1>
        {!data ? (
          <ErrorState />
        ) : data.content.length === 0 ? (
          <EmptyState title="No bookmarks yet" hint="Save articles to read later with the bookmark icon." />
        ) : (
          <>
            <div className="flex flex-col gap-4">
              {data.content.map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
            <Pagination page={data.page} totalPages={data.totalPages} hrefFor={(p) => `/bookmarks?page=${p}`} />
          </>
        )}
      </div>
    </AppShell>
  )
}
