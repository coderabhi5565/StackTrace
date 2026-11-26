'use client'

import { Bookmark, Heart, MessageSquare, Share2 } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { api, errorMessage } from '@/lib/api/client'
import type { Engagement } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'
import { cn } from '@/lib/utils'

/** Live like/bookmark state for the viewer (the page itself is cached for everyone). */
export function EngagementBar({ postId, initialLikes, title }: { postId: number; initialLikes: number; title: string }) {
  const session = useSession()
  const router = useRouter()
  const [state, setState] = useState<Engagement>({
    postId,
    likeCount: initialLikes,
    commentCount: 0,
    liked: false,
    bookmarked: false,
  })
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (session === undefined) return
    api<Engagement>(`/posts/${postId}/engagement`).then(setState).catch(() => undefined)
  }, [postId, session])

  async function toggle(kind: 'like' | 'bookmark') {
    if (!session) {
      router.push('/login')
      return
    }
    const active = kind === 'like' ? state.liked : state.bookmarked
    // optimistic update, rolled back on failure
    const prev = state
    setState((s) =>
      kind === 'like'
        ? { ...s, liked: !active, likeCount: s.likeCount + (active ? -1 : 1) }
        : { ...s, bookmarked: !active },
    )
    setBusy(true)
    try {
      setState(await api<Engagement>(`/posts/${postId}/${kind}`, { method: active ? 'DELETE' : 'POST' }))
      if (kind === 'bookmark') toast.success(active ? 'Removed from bookmarks' : 'Saved to bookmarks')
    } catch (e) {
      setState(prev)
      toast.error(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function share() {
    const url = window.location.href
    try {
      if (navigator.share) await navigator.share({ title, url })
      else {
        await navigator.clipboard.writeText(url)
        toast.success('Link copied to clipboard')
      }
    } catch {
      /* user cancelled */
    }
  }

  return (
    <div className="sticky bottom-4 z-30 mt-10 flex items-center justify-center gap-2 rounded-full border border-border bg-card/90 p-2 shadow-lg backdrop-blur-xl">
      <Button variant="ghost" size="sm" className="gap-1.5 rounded-full" onClick={() => toggle('like')} disabled={busy} aria-pressed={state.liked}>
        <Heart className={cn('size-4', state.liked && 'fill-primary text-primary')} />
        {state.likeCount.toLocaleString()}
      </Button>
      <a href="#comments" className="inline-flex h-7 items-center gap-1.5 rounded-full px-2.5 text-[0.8rem] font-medium hover:bg-muted">
        <MessageSquare className="size-4" />
        {state.commentCount.toLocaleString()}
      </a>
      <Button variant="ghost" size="sm" className="gap-1.5 rounded-full" onClick={() => toggle('bookmark')} disabled={busy} aria-pressed={state.bookmarked}>
        <Bookmark className={cn('size-4', state.bookmarked && 'fill-primary text-primary')} />
        {state.bookmarked ? 'Saved' : 'Save'}
      </Button>
      <Button variant="ghost" size="sm" className="gap-1.5 rounded-full" onClick={share}>
        <Share2 className="size-4" />
        Share
      </Button>
    </div>
  )
}
