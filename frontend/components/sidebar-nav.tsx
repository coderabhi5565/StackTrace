'use client'

import { Bookmark, FileText, LayoutList, Trophy, Users } from 'lucide-react'
import Link from 'next/link'
import { usePathname, useSearchParams } from 'next/navigation'
import { useSession } from '@/lib/auth/session-client'
import { cn } from '@/lib/utils'

export function SidebarNav() {
  const pathname = usePathname()
  const params = useSearchParams()
  const session = useSession()

  const nav = [
    { label: 'Feed', href: '/home', icon: LayoutList, active: pathname === '/home' && params.get('tab') !== 'following' },
    { label: 'Following', href: '/home?tab=following', icon: Users, active: pathname === '/home' && params.get('tab') === 'following' },
    { label: 'Bookmarks', href: '/bookmarks', icon: Bookmark, active: pathname === '/bookmarks' },
    {
      label: 'Drafts',
      href: session ? `/profile/${session.username}?tab=drafts` : '/login',
      icon: FileText,
      active: false,
    },
    { label: 'Leaderboard', href: '/leaderboard', icon: Trophy, active: pathname === '/leaderboard' },
  ]

  return (
    <nav className="flex flex-col gap-1">
      {nav.map((item) => (
        <Link
          key={item.label}
          href={item.href}
          className={cn(
            'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
            item.active ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
          )}
        >
          <item.icon className="size-4" />
          {item.label}
        </Link>
      ))}
    </nav>
  )
}
