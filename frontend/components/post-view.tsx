'use client'

import {
  AlertTriangle,
  ArrowLeft,
  Bookmark,
  Clock,
  Eye,
  Code,
  Heart,
  Link2,
  Share2,
  Sparkles,
  AtSign,
} from 'lucide-react'
import Link from 'next/link'
import { useState } from 'react'
import { toast } from 'sonner'
import { CodeBlock } from '@/components/code-block'
import { Comments } from '@/components/comments'
import { Quiz } from '@/components/quiz'
import { TagPill } from '@/components/tag-pill'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import type { Article } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

export function PostView({ article }: { article: Article }) {
  const [following, setFollowing] = useState(false)
  const [liked, setLiked] = useState(false)
  const [bookmarked, setBookmarked] = useState(article.bookmarked)
  const [flags, setFlags] = useState(article.outdatedFlags)
  const [flagged, setFlagged] = useState(false)
  const [showSummary, setShowSummary] = useState(false)

  function share() {
    toast.success('Link copied to clipboard')
  }

  function flag() {
    if (flagged) return
    setFlagged(true)
    setFlags((f) => f + 1)
    toast.success('Flagged as outdated. Thanks for the heads up!')
  }

  return (
    <article className="mx-auto max-w-3xl">
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2 gap-1.5">
        <Link href="/home">
          <ArrowLeft className="size-4" />
          Back
        </Link>
      </Button>

      {flags > 0 && (
        <div className="mb-6 flex items-center gap-2 rounded-xl border border-warning/40 bg-warning/10 px-4 py-3 text-sm text-warning-foreground dark:text-warning">
          <AlertTriangle className="size-4 shrink-0" />
          <span>
            This article may be outdated — flagged by {flags}{' '}
            {flags === 1 ? 'reader' : 'readers'}.
          </span>
        </div>
      )}

      {/* Author card */}
      <div className="flex flex-col gap-4 rounded-2xl border border-border bg-card p-5 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-3">
          <Avatar className="size-12 border border-border">
            <AvatarImage src={article.author.avatar} alt={article.author.name} />
            <AvatarFallback>{article.author.name[0]}</AvatarFallback>
          </Avatar>
          <div>
            <Link
              href={`/profile/${article.author.username}`}
              className="font-semibold hover:text-primary"
            >
              {article.author.name}
            </Link>
            <p className="text-xs text-muted-foreground">
              @{article.author.username}
            </p>
            <p className="mt-1.5 max-w-md text-sm text-muted-foreground">
              {article.author.bio}
            </p>
            <div className="mt-2 flex items-center gap-2 text-muted-foreground">
              {article.author.github && (
                <Link href="#" aria-label="GitHub" className="hover:text-foreground">
                  <Code className="size-4" />
                </Link>
              )}
              {article.author.linkedin && (
                <Link href="#" aria-label="LinkedIn" className="hover:text-foreground">
                  <Link2 className="size-4" />
                </Link>
              )}
              {article.author.x && (
                <Link href="#" aria-label="X" className="hover:text-foreground">
                  <AtSign className="size-4" />
                </Link>
              )}
            </div>
          </div>
        </div>
        <Button
          variant={following ? 'secondary' : 'default'}
          className="rounded-full"
          onClick={() => {
            setFollowing((f) => !f)
            toast.success(
              following
                ? `Unfollowed @${article.author.username}`
                : `Following @${article.author.username}`,
            )
          }}
        >
          {following ? 'Following' : 'Follow'}
        </Button>
      </div>

      {/* Header */}
      <header className="mt-8">
        <h1 className="text-balance text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
          {article.title}
        </h1>
        <div className="mt-4 flex flex-wrap gap-1.5">
          {article.tags.map((t) => (
            <TagPill key={t} tag={t} />
          ))}
        </div>
        <div className="mt-4 flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <Clock className="size-4" />
            {article.readingTime} min read
          </span>
          <span>{article.postedAt}</span>
          <span className="flex items-center gap-1.5">
            <Eye className="size-4" />
            {article.views.toLocaleString()} views
          </span>
          <div className="ml-auto flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setBookmarked((b) => !b)}
              aria-label="Bookmark"
            >
              <Bookmark
                className={cn('size-4', bookmarked && 'fill-primary text-primary')}
              />
            </Button>
            <Button variant="ghost" size="icon" onClick={share} aria-label="Share">
              <Share2 className="size-4" />
            </Button>
          </div>
        </div>
      </header>

      {/* AI Summary */}
      <div className="mt-6">
        <Button
          variant="outline"
          className="gap-2 border-primary/40 text-primary hover:bg-primary/10"
          onClick={() => setShowSummary((s) => !s)}
        >
          <Sparkles className="size-4" />
          AI Summary
        </Button>
        {showSummary && (
          <div className="mt-3 rounded-xl border-2 border-transparent bg-card p-5 [background:linear-gradient(var(--card),var(--card))_padding-box,linear-gradient(135deg,oklch(0.7_0.18_290),oklch(0.75_0.18_200))_border-box]">
            <p className="flex items-center gap-2 text-sm font-semibold">
              <Sparkles className="size-4 text-primary" />
              Key takeaways
            </p>
            <ul className="mt-3 flex flex-col gap-2 text-sm text-foreground/90">
              {[
                'Prefer constructor injection for explicit, testable dependencies.',
                'Use final fields to keep your beans immutable.',
                'Disambiguate multiple candidates with @Qualifier.',
                'Mark a single default bean with @Primary.',
                'Reserve field injection for quick prototypes only.',
              ].map((s) => (
                <li key={s} className="flex gap-2">
                  <span className="mt-2 size-1.5 shrink-0 rounded-full bg-primary" />
                  {s}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Body */}
      <div className="mt-8 leading-relaxed">
        {article.content.map((section, i) => {
          if (section.type === 'heading')
            return (
              <h2 key={i} className="mt-8 text-2xl font-bold tracking-tight">
                {section.text}
              </h2>
            )
          if (section.type === 'paragraph')
            return (
              <p key={i} className="mt-4 text-[15px] leading-7 text-foreground/90">
                {section.text}
              </p>
            )
          if (section.type === 'code')
            return <CodeBlock key={i} block={section.block} />
          if (section.type === 'list')
            return (
              <ul key={i} className="mt-4 flex flex-col gap-2">
                {section.items.map((it) => (
                  <li key={it} className="flex gap-2 text-[15px] text-foreground/90">
                    <span className="mt-2.5 size-1.5 shrink-0 rounded-full bg-primary" />
                    {it}
                  </li>
                ))}
              </ul>
            )
          return null
        })}
      </div>

      {/* Sticky action bar */}
      <div className="sticky bottom-4 z-30 mt-10 flex items-center justify-center gap-2 rounded-full border border-border bg-card/90 p-2 shadow-lg backdrop-blur-xl">
        <Button
          variant="ghost"
          size="sm"
          className="gap-1.5 rounded-full"
          onClick={() => setLiked((l) => !l)}
        >
          <Heart
            className={cn('size-4', liked && 'fill-primary text-primary')}
          />
          {(article.likes + (liked ? 1 : 0)).toLocaleString()}
        </Button>
        <Button
          variant="ghost"
          size="sm"
          className="gap-1.5 rounded-full"
          onClick={() => setBookmarked((b) => !b)}
        >
          <Bookmark
            className={cn('size-4', bookmarked && 'fill-primary text-primary')}
          />
          Save
        </Button>
        <Button
          variant="ghost"
          size="sm"
          className="gap-1.5 rounded-full"
          onClick={share}
        >
          <Share2 className="size-4" />
          Share
        </Button>
        <Button
          variant="ghost"
          size="sm"
          className={cn(
            'gap-1.5 rounded-full text-warning',
            flagged && 'opacity-60',
          )}
          onClick={flag}
          disabled={flagged}
        >
          <AlertTriangle className="size-4" />
          {flagged ? 'Flagged' : 'Flag outdated'}
        </Button>
      </div>

      {article.hasQuiz && <Quiz questions={article.quiz} />}

      <Comments />
    </article>
  )
}
