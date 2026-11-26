import { Clock, Heart, MessageSquare, PenLine } from 'lucide-react'
import Link from 'next/link'
import { BookmarkButton } from '@/components/bookmark-button'
import { TagPill } from '@/components/tag-pill'
import { UserAvatar } from '@/components/user-avatar'
import type { PostSummary } from '@/lib/api/types'
import { timeAgo } from '@/lib/format'

export function ArticleCard({ article, showStatus = false }: { article: PostSummary; showStatus?: boolean }) {
  const published = article.status === 'PUBLISHED'
  const href = published ? `/post/${article.slug}` : `/edit/${article.id}`

  return (
    <article className="group rounded-xl border border-border bg-card p-5 transition-colors hover:border-primary/40">
      <div className="flex items-center justify-between gap-2">
        <Link href={`/profile/${article.author.username}`} className="flex items-center gap-2.5" prefetch={false}>
          <UserAvatar user={article.author} />
          <div className="leading-tight">
            <p className="text-sm font-medium">{article.author.name}</p>
            <p className="text-xs text-muted-foreground">
              @{article.author.username} · {timeAgo(article.publishedAt ?? article.createdAt)}
            </p>
          </div>
        </Link>
        {showStatus && !published && (
          <span className="rounded-full border border-warning/40 bg-warning/10 px-2.5 py-0.5 text-xs font-medium text-warning">
            {article.status === 'SCHEDULED' ? 'Scheduled' : 'Draft'}
          </span>
        )}
      </div>

      <Link href={href} className="mt-4 block">
        <h2 className="text-pretty text-xl font-bold leading-snug tracking-tight transition-colors group-hover:text-primary">
          {article.title}
        </h2>
        {article.excerpt && (
          <p className="mt-2 line-clamp-2 text-sm leading-relaxed text-muted-foreground">{article.excerpt}</p>
        )}
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
          <span className="flex items-center gap-1" aria-label={`${article.likeCount} likes`}>
            <Heart className="size-3.5" />
            {article.likeCount}
          </span>
          <span className="flex items-center gap-1" aria-label={`${article.commentCount} comments`}>
            <MessageSquare className="size-3.5" />
            {article.commentCount}
          </span>
        </div>
        {published ? (
          <BookmarkButton postId={article.id} />
        ) : (
          <Link href={`/edit/${article.id}`} className="flex items-center gap-1 hover:text-primary">
            <PenLine className="size-3.5" /> Edit
          </Link>
        )}
      </div>
    </article>
  )
}
