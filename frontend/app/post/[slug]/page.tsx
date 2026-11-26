import type { Metadata } from 'next'
import { ArrowLeft, Clock } from 'lucide-react'
import Link from 'next/link'
import { notFound } from 'next/navigation'
import { AppShell } from '@/components/app-shell'
import { CoverImage } from '@/components/cover-image'
import { Markdown } from '@/components/markdown'
import { Comments } from '@/components/post/comments'
import { EngagementBar } from '@/components/post/engagement-bar'
import { FollowButton } from '@/components/post/follow-button'
import { PostOwnerActions } from '@/components/post/post-owner-actions'
import { TagPill } from '@/components/tag-pill'
import { buttonVariants } from '@/components/ui/button'
import { UserAvatar } from '@/components/user-avatar'
import { apiGetOrNull } from '@/lib/api/server'
import type { Post } from '@/lib/api/types'
import { formatDate } from '@/lib/format'
import { cn } from '@/lib/utils'

// ISR: rendered on first request, cached, refreshed at most every 5 minutes or on edit.
export const revalidate = 300

// No pages are built ahead of time; each article is generated on first visit, then cached.
export async function generateStaticParams() {
  return []
}

const getPost = (slug: string) =>
  apiGetOrNull<Post>(`/api/posts/${encodeURIComponent(slug)}`, { revalidate: 300, tags: ['posts', `post:${slug}`] })

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const post = await getPost((await params).slug)
  if (!post) return { title: 'Post not found · StackTrace' }
  return {
    title: `${post.title} · StackTrace`,
    description: post.excerpt ?? undefined,
    openGraph: { title: post.title, description: post.excerpt ?? undefined, type: 'article' },
  }
}

export default async function PostPage({ params }: { params: Promise<{ slug: string }> }) {
  const post = await getPost((await params).slug)
  if (!post) notFound()

  return (
    <AppShell>
      <article className="mx-auto max-w-3xl">
        <Link href="/home" className={cn(buttonVariants({ variant: 'ghost', size: 'sm' }), 'mb-4 -ml-2 gap-1.5')}>
          <ArrowLeft className="size-4" />
          Back
        </Link>

        <div className="flex flex-col gap-4 rounded-2xl border border-border bg-card p-5 sm:flex-row sm:items-center sm:justify-between">
          <Link href={`/profile/${post.author.username}`} className="flex items-center gap-3">
            <UserAvatar user={post.author} className="size-12" />
            <div>
              <p className="font-semibold hover:text-primary">{post.author.name}</p>
              <p className="text-xs text-muted-foreground">@{post.author.username}</p>
            </div>
          </Link>
          <div className="flex items-center gap-2">
            <PostOwnerActions postId={post.id} authorId={post.author.id} />
            <FollowButton userId={post.author.id} username={post.author.username} />
          </div>
        </div>

        <header className="mt-8">
          <h1 className="text-balance text-3xl font-bold leading-tight tracking-tight sm:text-4xl">{post.title}</h1>
          <div className="mt-4 flex flex-wrap gap-1.5">
            {post.tags.map((t) => (
              <TagPill key={t} tag={t} />
            ))}
          </div>
          <div className="mt-4 flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <span className="flex items-center gap-1.5">
              <Clock className="size-4" />
              {post.readingTime} min read
            </span>
            <span>{formatDate(post.publishedAt)}</span>
            {post.updatedAt && post.publishedAt && new Date(post.updatedAt) > new Date(post.publishedAt) && (
              <span>Updated {formatDate(post.updatedAt)}</span>
            )}
          </div>
        </header>

        {post.coverImageUrl && <CoverImage src={post.coverImageUrl} priority />}

        <div className="mt-8">
          <Markdown source={post.content} />
        </div>

        <EngagementBar postId={post.id} initialLikes={post.likeCount} title={post.title} />

        <Comments postId={post.id} />
      </article>
    </AppShell>
  )
}
