'use client'

import { Bookmark } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { api, errorMessage } from '@/lib/api/client'
import type { Engagement } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'
import { cn } from '@/lib/utils'

/** Bookmark toggle for cards. State starts unknown and is set by the first toggle. */
export function BookmarkButton({ postId, initial = false }: { postId: number; initial?: boolean }) {
  const session = useSession()
  const router = useRouter()
  const [bookmarked, setBookmarked] = useState(initial)
  const [busy, setBusy] = useState(false)

  async function toggle(e: React.MouseEvent) {
    e.preventDefault()
    if (!session) {
      router.push('/login')
      return
    }
    setBusy(true)
    try {
      const res = await api<Engagement>(`/posts/${postId}/bookmark`, { method: bookmarked ? 'DELETE' : 'POST' })
      setBookmarked(res.bookmarked)
      toast.success(res.bookmarked ? 'Saved to bookmarks' : 'Removed from bookmarks')
    } catch (err) {
      toast.error(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <button
      onClick={toggle}
      disabled={busy}
      aria-label={bookmarked ? 'Remove bookmark' : 'Bookmark'}
      aria-pressed={bookmarked}
      className="rounded-md p-1 transition-colors hover:text-primary disabled:opacity-50"
    >
      <Bookmark className={cn('size-4', bookmarked && 'fill-primary text-primary')} />
    </button>
  )
}
