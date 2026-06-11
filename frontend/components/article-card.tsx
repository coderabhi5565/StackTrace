'use client'

import { Bookmark, Clock, Eye, Sparkles } from 'lucide-react'
import Link from 'next/link'
import { useState } from 'react'
import { toast } from 'sonner'
import { TagPill } from '@/components/tag-pill'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import type { Article } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

export function ArticleCard({ article }: { article: Article }) {
  const [following, setFollowing] = useState(false)
  const [bookmarked, setBookmarked] = useState(article.bookmarked)

  function toggleFollow(e: React.MouseEvent) {
    e.preventDefault()
    setFollowing((f) => !f)
    toast.success(following ? `Unfollowed @${article.author.username}` : `Following @${article.author.username}`)
  }

  function toggleBookmark(e: React.MouseEvent) {
    e.preventDefault()
    setBookmarked((b) => !b)
    toast.success(bookmarked ? 'Removed from bookmarks' : 'Saved to bookmarks')
  }

  return (
    <article className="group rounded-xl border border-border bg-card p-5 transition-colors hover:border-primary/40">
      <div className="flex items-center justify-between gap-2">
        <Link
          href={`/profile/${article.author.username}`}
          className="flex items-center gap-2.5"
        >
          <Avatar className="size-9 border border-border">
            <AvatarImage src={article.author.avatar} alt={article.author.name} />
            <AvatarFallback>{article.author.name[0]}</AvatarFallback>
          </Avatar>
          <div className="leading-tight">
            <p className="text-sm font-medium">{article.author.name}</p>
            <p className="text-xs text-muted-foreground">
              @{article.author.username} · {article.postedAt}
            </p>
          </div>
        </Link>
        <Button
          size="sm"
          variant={following ? 'secondary' : 'outline'}
          className="h-8 rounded-full text-xs"
          onClick={toggleFollow}
        >
          {following ? 'Following' : 'Follow'}
        </Button>
      </div>

      <Link href={`/post/${article.slug}`} className="mt-4 block">
        <h2 className="text-pretty text-xl font-bold leading-snug tracking-tight transition-colors group-hover:text-primary">
          {article.title}
        </h2>
        <p className="mt-2 line-clamp-2 text-sm leading-relaxed text-muted-foreground">
          {article.description}
        </p>
      </Link>

      <div className="mt-4 flex flex-wrap gap-1.5">
        {article.tags.map((t) => (
          <TagPill key={t} tag={t} />
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between border-t border-border pt-3 text-xs text-muted-foreground">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1">
            <Clock className="size-3.5" />
            {article.readingTime} min read
          </span>
          <span className="flex items-center gap-1">
            <Eye className="size-3.5" />
            {article.views.toLocaleString()}
          </span>
          {article.hasQuiz && (
            <span className="hidden items-center gap-1 rounded-full border border-primary/30 bg-primary/10 px-2 py-0.5 text-primary sm:flex">
              <Sparkles className="size-3" />
              Quiz
            </span>
          )}
        </div>
        <button
          onClick={toggleBookmark}
          aria-label="Bookmark"
          className="rounded-md p-1 transition-colors hover:text-primary"
        >
          <Bookmark
            className={cn('size-4', bookmarked && 'fill-primary text-primary')}
          />
        </button>
      </div>
    </article>
  )
}
