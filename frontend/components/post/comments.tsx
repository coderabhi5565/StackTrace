'use client'

import { PenLine, Reply, Trash2 } from 'lucide-react'
import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'sonner'
import { ErrorState } from '@/components/empty-state'
import { Button, buttonVariants } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { UserAvatar } from '@/components/user-avatar'
import { api, errorMessage } from '@/lib/api/client'
import type { Comment, Page, SessionUser } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'
import { timeAgo } from '@/lib/format'
import { cn } from '@/lib/utils'

const PAGE_SIZE = 10
const MAX_INDENT = 4

function CommentForm({
  initial = '',
  placeholder,
  submitLabel,
  onSubmit,
  onCancel,
  autoFocus,
}: {
  initial?: string
  placeholder: string
  submitLabel: string
  onSubmit: (text: string) => Promise<void>
  onCancel?: () => void
  autoFocus?: boolean
}) {
  const [text, setText] = useState(initial)
  const [busy, setBusy] = useState(false)

  async function submit() {
    if (!text.trim()) return
    setBusy(true)
    try {
      await onSubmit(text.trim())
      setText('')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex-1">
      <Textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder={placeholder}
        maxLength={2000}
        autoFocus={autoFocus}
        className="min-h-20 resize-none"
        onKeyDown={(e) => {
          if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) submit()
        }}
      />
      <div className="mt-2 flex justify-end gap-2">
        {onCancel && (
          <Button variant="ghost" onClick={onCancel} disabled={busy}>
            Cancel
          </Button>
        )}
        <Button onClick={submit} disabled={busy || !text.trim()} className="font-medium">
          {busy ? 'Posting…' : submitLabel}
        </Button>
      </div>
    </div>
  )
}

function CommentItem({
  comment,
  depth,
  session,
  postId,
  onChanged,
}: {
  comment: Comment
  depth: number
  session: SessionUser | null | undefined
  postId: number
  onChanged: () => void
}) {
  const [replying, setReplying] = useState(false)
  const [editing, setEditing] = useState(false)
  const mine = session && comment.authorId === session.id

  async function reply(text: string) {
    try {
      await api('/comments', { method: 'POST', body: { postId, parentCommentId: comment.id, content: text } })
      setReplying(false)
      toast.success('Reply posted')
      onChanged()
    } catch (e) {
      toast.error(errorMessage(e))
      throw e
    }
  }

  async function edit(text: string) {
    try {
      await api(`/comments/${comment.id}`, { method: 'PUT', body: { content: text } })
      setEditing(false)
      onChanged()
    } catch (e) {
      toast.error(errorMessage(e))
      throw e
    }
  }

  async function remove() {
    if (!window.confirm('Delete this comment?')) return
    try {
      await api(`/comments/${comment.id}`, { method: 'DELETE' })
      toast.success('Comment deleted')
      onChanged()
    } catch (e) {
      toast.error(errorMessage(e))
    }
  }

  return (
    <li className="flex gap-3">
      <UserAvatar user={comment.author} className="size-8" />
      <div className="min-w-0 flex-1">
        {editing ? (
          <CommentForm initial={comment.content ?? ''} placeholder="Edit your comment" submitLabel="Save" onSubmit={edit} onCancel={() => setEditing(false)} autoFocus />
        ) : (
          <div className={cn('rounded-xl border border-border bg-card p-3.5', comment.deleted && 'border-dashed bg-transparent')}>
            {comment.deleted ? (
              <p className="text-sm italic text-muted-foreground">This comment was deleted.</p>
            ) : (
              <>
                <div className="flex flex-wrap items-center gap-x-2">
                  <Link href={`/profile/${comment.author?.username}`} className="text-sm font-medium hover:text-primary">
                    {comment.author?.name}
                  </Link>
                  <span className="text-xs text-muted-foreground">
                    @{comment.author?.username} · {timeAgo(comment.createdAt)}
                    {comment.edited && ' · edited'}
                  </span>
                </div>
                <p className="mt-1.5 whitespace-pre-wrap break-words text-sm leading-relaxed text-foreground/90">{comment.content}</p>
              </>
            )}
          </div>
        )}

        {!comment.deleted && !editing && (
          <div className="mt-1.5 flex items-center gap-4 px-1 text-xs text-muted-foreground">
            {session && (
              <button className="flex items-center gap-1 transition-colors hover:text-primary" onClick={() => setReplying((r) => !r)}>
                <Reply className="size-3.5" />
                Reply
              </button>
            )}
            {mine && (
              <>
                <button className="flex items-center gap-1 transition-colors hover:text-primary" onClick={() => setEditing(true)}>
                  <PenLine className="size-3.5" />
                  Edit
                </button>
                <button className="flex items-center gap-1 transition-colors hover:text-destructive" onClick={remove}>
                  <Trash2 className="size-3.5" />
                  Delete
                </button>
              </>
            )}
          </div>
        )}

        {replying && (
          <div className="mt-3">
            <CommentForm placeholder={`Reply to @${comment.author?.username}`} submitLabel="Reply" onSubmit={reply} onCancel={() => setReplying(false)} autoFocus />
          </div>
        )}

        {comment.replies.length > 0 && (
          <ul className={cn('mt-4 flex flex-col gap-4', depth < MAX_INDENT && 'border-l border-border pl-4')}>
            {comment.replies.map((r) => (
              <CommentItem key={r.id} comment={r} depth={depth + 1} session={session} postId={postId} onChanged={onChanged} />
            ))}
          </ul>
        )}
      </div>
    </li>
  )
}

export function Comments({ postId }: { postId: number }) {
  const session = useSession()
  const [pages, setPages] = useState<Page<Comment>[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  /** Reloads every page loaded so far (keeps the thread in place after a change). */
  const reload = useCallback(
    async (pageCount = 1) => {
      try {
        const loaded = await Promise.all(
          Array.from({ length: pageCount }, (_, p) => api<Page<Comment>>(`/comments/post/${postId}?page=${p}&size=${PAGE_SIZE}`)),
        )
        setPages(loaded)
        setError(false)
      } catch {
        setError(true)
      } finally {
        setLoading(false)
      }
    },
    [postId],
  )

  useEffect(() => {
    let cancelled = false
    api<Page<Comment>>(`/comments/post/${postId}?page=0&size=${PAGE_SIZE}`)
      .then((res) => !cancelled && (setPages([res]), setError(false)))
      .catch(() => !cancelled && setError(true))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [postId])

  async function loadMore() {
    const last = pages.at(-1)
    if (!last) return
    setLoading(true)
    try {
      const next = await api<Page<Comment>>(`/comments/post/${postId}?page=${last.page + 1}&size=${PAGE_SIZE}`)
      setPages((p) => [...p, next])
    } catch (e) {
      toast.error(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function post(text: string) {
    try {
      await api('/comments', { method: 'POST', body: { postId, content: text } })
      toast.success('Comment posted')
      await reload(Math.max(pages.length, 1))
    } catch (e) {
      toast.error(errorMessage(e))
      throw e
    }
  }

  const comments = pages.flatMap((p) => p.content)
  const total = pages[0]?.totalElements ?? 0
  const last = pages.at(-1)

  return (
    <section className="mt-10" id="comments">
      <h2 className="text-xl font-bold tracking-tight">
        Discussion <span className="text-muted-foreground">({total})</span>
      </h2>

      <div className="mt-4 flex gap-3">
        {session ? (
          <>
            <UserAvatar user={{ name: session.name, username: session.username }} />
            <CommentForm placeholder="Add to the discussion…" submitLabel="Post" onSubmit={post} />
          </>
        ) : session === null ? (
          <div className="flex w-full items-center justify-between rounded-xl border border-dashed border-border p-4 text-sm text-muted-foreground">
            Sign in to join the discussion.
            <Link href="/login" className={buttonVariants({ size: 'sm' })}>
              Sign in
            </Link>
          </div>
        ) : null}
      </div>

      {error && comments.length === 0 ? (
        <div className="mt-6">
          <ErrorState message="Couldn't load comments." />
        </div>
      ) : (
        <ul className="mt-6 flex flex-col gap-5">
          {comments.map((c) => (
            <CommentItem key={c.id} comment={c} depth={0} session={session} postId={postId} onChanged={() => reload(pages.length)} />
          ))}
          {loading &&
            comments.length === 0 &&
            Array.from({ length: 2 }).map((_, i) => (
              <li key={i} className="flex gap-3">
                <Skeleton className="size-8 rounded-full" />
                <Skeleton className="h-16 flex-1 rounded-xl" />
              </li>
            ))}
        </ul>
      )}
      {!error && !loading && comments.length === 0 && (
        <p className="mt-6 text-sm text-muted-foreground">No comments yet. Start the conversation!</p>
      )}

      {last && !last.last && (
        <Button variant="outline" className="mt-6" onClick={loadMore} disabled={loading}>
          {loading ? 'Loading…' : 'Load more comments'}
        </Button>
      )}
    </section>
  )
}
