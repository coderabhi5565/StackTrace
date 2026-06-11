'use client'

import { Heart, Reply } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { comments as initialComments, currentUser } from '@/lib/mock-data'

export function Comments() {
  const [list, setList] = useState(initialComments)
  const [draft, setDraft] = useState('')
  const [liked, setLiked] = useState<Record<string, boolean>>({})

  function post() {
    if (!draft.trim()) return
    setList((c) => [
      {
        id: `new-${Date.now()}`,
        author: currentUser,
        content: draft.trim(),
        time: 'just now',
        likes: 0,
      },
      ...c,
    ])
    setDraft('')
    toast.success('Comment posted')
  }

  return (
    <section className="mt-10">
      <h2 className="text-xl font-bold tracking-tight">
        Comments <span className="text-muted-foreground">({list.length})</span>
      </h2>

      <div className="mt-4 flex gap-3">
        <Avatar className="size-9 border border-border">
          <AvatarImage src={currentUser.avatar} alt={currentUser.name} />
          <AvatarFallback>{currentUser.name[0]}</AvatarFallback>
        </Avatar>
        <div className="flex-1">
          <Textarea
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            placeholder="Add to the discussion..."
            className="min-h-20 resize-none"
          />
          <div className="mt-2 flex justify-end">
            <Button onClick={post} disabled={!draft.trim()} className="font-medium">
              Post
            </Button>
          </div>
        </div>
      </div>

      <ul className="mt-6 flex flex-col gap-5">
        {list.map((c) => (
          <li key={c.id} className="flex gap-3">
            <Avatar className="size-9 border border-border">
              <AvatarImage src={c.author.avatar} alt={c.author.name} />
              <AvatarFallback>{c.author.name[0]}</AvatarFallback>
            </Avatar>
            <div className="flex-1">
              <div className="rounded-xl border border-border bg-card p-3.5">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">{c.author.name}</span>
                  <span className="text-xs text-muted-foreground">
                    @{c.author.username} · {c.time}
                  </span>
                </div>
                <p className="mt-1.5 text-sm leading-relaxed text-foreground/90">
                  {c.content}
                </p>
              </div>
              <div className="mt-1.5 flex items-center gap-4 px-1 text-xs text-muted-foreground">
                <button
                  className="flex items-center gap-1 transition-colors hover:text-primary"
                  onClick={() =>
                    setLiked((l) => ({ ...l, [c.id]: !l[c.id] }))
                  }
                >
                  <Heart
                    className={
                      liked[c.id]
                        ? 'size-3.5 fill-primary text-primary'
                        : 'size-3.5'
                    }
                  />
                  {c.likes + (liked[c.id] ? 1 : 0)}
                </button>
                <button className="flex items-center gap-1 transition-colors hover:text-primary">
                  <Reply className="size-3.5" />
                  Reply
                </button>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
