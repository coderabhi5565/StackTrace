'use client'

import { PenLine, Trash2 } from 'lucide-react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { Button, buttonVariants } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { api, errorMessage } from '@/lib/api/client'
import { useSession } from '@/lib/auth/session-client'

/** Edit/delete controls, rendered only for the author (the backend enforces ownership too). */
export function PostOwnerActions({ postId, authorId }: { postId: number; authorId: number }) {
  const session = useSession()
  const router = useRouter()
  const [open, setOpen] = useState(false)
  const [busy, setBusy] = useState(false)

  if (!session || session.id !== authorId) return null

  async function remove() {
    setBusy(true)
    try {
      await api(`/posts/${postId}`, { method: 'DELETE' })
      toast.success('Article deleted')
      router.push(`/profile/${session!.username}`)
      router.refresh()
    } catch (e) {
      toast.error(errorMessage(e))
      setBusy(false)
    }
  }

  return (
    <>
      <Link href={`/edit/${postId}`} className={buttonVariants({ variant: 'outline', size: 'sm' })}>
        <PenLine className="size-3.5" /> Edit
      </Link>
      <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
        <Trash2 className="size-3.5" /> Delete
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete this article?</DialogTitle>
            <DialogDescription>It will be removed from feeds, search and your profile.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={remove} disabled={busy}>
              {busy ? 'Deleting…' : 'Delete'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
