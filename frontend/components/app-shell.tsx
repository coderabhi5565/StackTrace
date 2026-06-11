'use client'

import {
  Bookmark,
  FileText,
  LayoutList,
  Trophy,
  Users,
} from 'lucide-react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { Navbar } from '@/components/navbar'
import { TagPill } from '@/components/tag-pill'
import { TAGS } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

const nav = [
  { label: 'Feed', href: '/home', icon: LayoutList },
  { label: 'Following', href: '/home?tab=following', icon: Users },
  { label: 'Bookmarks', href: '/profile?tab=bookmarks', icon: Bookmark },
  { label: 'Drafts', href: '/profile?tab=drafts', icon: FileText },
  { label: 'Leaderboard', href: '/leaderboard', icon: Trophy },
]

export function AppShell({
  children,
  rightSidebar,
}: {
  children: React.ReactNode
  rightSidebar?: React.ReactNode
}) {
  const pathname = usePathname()

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <div className="mx-auto flex max-w-[1600px] gap-6 px-4 py-6 md:px-6">
        {/* Left sidebar */}
        <aside className="sticky top-[88px] hidden h-[calc(100vh-104px)] w-56 shrink-0 flex-col overflow-y-auto lg:flex">
          <nav className="flex flex-col gap-1">
            {nav.map((item) => {
              const active = pathname === item.href.split('?')[0]
              return (
                <Link
                  key={item.label}
                  href={item.href}
                  className={cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    active
                      ? 'bg-secondary text-foreground'
                      : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                  )}
                >
                  <item.icon className="size-4" />
                  {item.label}
                </Link>
              )
            })}
          </nav>

          <div className="my-4 h-px bg-border" />

          <p className="px-3 pb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Popular Tags
          </p>
          <div className="flex flex-wrap gap-1.5 px-3">
            {TAGS.slice(0, 8).map((t) => (
              <TagPill key={t} tag={t} />
            ))}
          </div>
        </aside>

        {/* Main */}
        <main className="min-w-0 flex-1">{children}</main>

        {/* Right sidebar */}
        {rightSidebar && (
          <aside className="sticky top-[88px] hidden h-[calc(100vh-104px)] w-80 shrink-0 overflow-y-auto xl:block">
            {rightSidebar}
          </aside>
        )}
      </div>
    </div>
  )
}
