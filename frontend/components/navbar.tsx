'use client'

import { Bell, PenLine, Search } from 'lucide-react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { Logo } from '@/components/logo'
import { ThemeToggle } from '@/components/theme-toggle'
import { buttonVariants } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { UserAvatar } from '@/components/user-avatar'
import { api } from '@/lib/api/client'
import { notifySessionChange, useSession } from '@/lib/auth/session-client'
import { cn } from '@/lib/utils'

export function Navbar() {
  const router = useRouter()
  const session = useSession()
  const [query, setQuery] = useState('')
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    if (!session) return
    let cancelled = false
    const load = () =>
      api<{ unread: number }>('/notifications/unread-count')
        .then((r) => !cancelled && setUnread(r.unread))
        .catch(() => undefined)
    load()
    const t = setInterval(load, 60_000)
    return () => {
      cancelled = true
      clearInterval(t)
    }
  }, [session])

  function onSearch(e: React.FormEvent) {
    e.preventDefault()
    if (query.trim()) router.push(`/search?q=${encodeURIComponent(query.trim())}`)
  }

  async function signOut() {
    await fetch('/api/auth/logout', { method: 'POST' })
    notifySessionChange()
    toast.success('Signed out')
    router.push('/')
    router.refresh()
  }

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-[1600px] items-center gap-3 px-4 md:gap-6 md:px-6">
        <Logo />

        <form onSubmit={onSearch} className="relative ml-auto max-w-md flex-1 md:ml-6" role="search">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search articles, devs, tags..."
            aria-label="Search"
            className="h-10 rounded-full border-border bg-secondary pl-9"
          />
        </form>

        <div className="flex items-center gap-1 md:gap-2">
          <ThemeToggle />

          {session ? (
            <>
              <Link href="/create" className={cn(buttonVariants(), 'hidden rounded-full font-medium sm:inline-flex')}>
                <PenLine className="size-4" />
                Write
              </Link>

              <Link
                href="/notifications"
                className={cn(buttonVariants({ variant: 'ghost', size: 'icon' }), 'relative')}
                aria-label={unread > 0 ? `Notifications (${unread} unread)` : 'Notifications'}
              >
                <Bell className="size-5" />
                {unread > 0 && (
                  <span className="absolute right-0.5 top-0.5 flex size-4 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-primary-foreground">
                    {unread > 9 ? '9+' : unread}
                  </span>
                )}
              </Link>

              <DropdownMenu>
                <DropdownMenuTrigger
                  className="rounded-full outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring"
                  aria-label="Account menu"
                >
                  <UserAvatar user={{ name: session.name, username: session.username }} />
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel>
                    <div className="flex flex-col">
                      <span className="font-medium">{session.name}</span>
                      <span className="text-xs text-muted-foreground">@{session.username}</span>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link href={`/profile/${session.username}`}>Profile</Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link href="/bookmarks">Bookmarks</Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link href={`/profile/${session.username}?tab=drafts`}>Drafts</Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link href="/create">Write an article</Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onSelect={signOut}>Sign out</DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : session === null ? (
            <>
              <Link href="/login" className={cn(buttonVariants({ variant: 'ghost' }), 'font-medium')}>
                Sign in
              </Link>
              <Link href="/register" className={cn(buttonVariants(), 'rounded-full font-medium')}>
                Join
              </Link>
            </>
          ) : (
            <span className="size-9" aria-hidden />
          )}
        </div>
      </div>
    </header>
  )
}
