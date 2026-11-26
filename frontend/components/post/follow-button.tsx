'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { api, errorMessage } from '@/lib/api/client'
import { useSession } from '@/lib/auth/session-client'

export function FollowButton({
  userId,
  username,
  onChange,
  className,
}: {
  userId: number
  username: string
  onChange?: (following: boolean) => void
  className?: string
}) {
  const session = useSession()
  const router = useRouter()
  const [following, setFollowing] = useState<boolean | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!session || session.id === userId) return
    api<{ following: boolean }>(`/users/${userId}/follow-status`)
      .then((r) => setFollowing(r.following))
      .catch(() => setFollowing(false))
  }, [session, userId])

  if (session && session.id === userId) return null

  async function toggle() {
    if (!session) {
      router.push('/login')
      return
    }
    setBusy(true)
    try {
      await api(`/users/${userId}/follow`, { method: following ? 'DELETE' : 'POST' })
      const next = !following
      setFollowing(next)
      onChange?.(next)
      toast.success(next ? `Following @${username}` : `Unfollowed @${username}`)
    } catch (e) {
      toast.error(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Button
      variant={following ? 'secondary' : 'default'}
      className={className ?? 'rounded-full'}
      onClick={toggle}
      disabled={busy || (session !== null && session !== undefined && following === null)}
    >
      {following ? 'Following' : 'Follow'}
    </Button>
  )
}
