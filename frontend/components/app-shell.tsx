import { Suspense } from 'react'
import { Navbar } from '@/components/navbar'
import { SidebarNav } from '@/components/sidebar-nav'
import { TagPill } from '@/components/tag-pill'
import { apiGetSafe } from '@/lib/api/server'
import type { Tag } from '@/lib/api/types'

async function PopularTags() {
  const tags = await apiGetSafe<Tag[]>('/api/tags?limit=10', [], { revalidate: 300, tags: ['tags'] })
  if (!tags.length) return null
  return (
    <>
      <div className="my-4 h-px bg-border" />
      <p className="px-3 pb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">Popular Tags</p>
      <div className="flex flex-wrap gap-1.5 px-3">
        {tags.map((t) => (
          <TagPill key={t.name} tag={t.name} />
        ))}
      </div>
    </>
  )
}

export function AppShell({
  children,
  rightSidebar,
}: {
  children: React.ReactNode
  rightSidebar?: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="mx-auto flex max-w-[1600px] gap-6 px-4 py-6 md:px-6">
        <aside className="sticky top-[88px] hidden h-[calc(100vh-104px)] w-56 shrink-0 flex-col overflow-y-auto lg:flex">
          <Suspense fallback={null}>
            <SidebarNav />
          </Suspense>
          <Suspense fallback={null}>
            <PopularTags />
          </Suspense>
        </aside>

        <main className="min-w-0 flex-1">{children}</main>

        {rightSidebar && (
          <aside className="sticky top-[88px] hidden h-[calc(100vh-104px)] w-80 shrink-0 overflow-y-auto xl:block">
            {rightSidebar}
          </aside>
        )}
      </div>
    </div>
  )
}
