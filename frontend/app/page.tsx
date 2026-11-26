import { ArrowRight, BookOpen, MessageSquare, Search, Trophy } from 'lucide-react'
import Link from 'next/link'
import { ArticleCard } from '@/components/article-card'
import { Logo } from '@/components/logo'
import { ThemeToggle } from '@/components/theme-toggle'
import { buttonVariants } from '@/components/ui/button'
import { apiGetSafe } from '@/lib/api/server'
import type { Page, PostSummary } from '@/lib/api/types'
import { cn } from '@/lib/utils'

// Public marketing page: statically generated and refreshed every minute (ISR).
export const revalidate = 60

const features = [
  { icon: BookOpen, title: 'Write in Markdown', text: 'Code blocks, tables and live preview, with drafts and one-click publishing.' },
  { icon: MessageSquare, title: 'Threaded discussions', text: 'Nested comments and replies keep technical conversations readable.' },
  { icon: Search, title: 'Find anything', text: 'Full-text search across every article and developer.' },
  { icon: Trophy, title: 'Earn reputation', text: 'Points and badges for publishing and for the likes your work receives.' },
]

export default async function LandingPage() {
  const emptyPage: Page<PostSummary> = { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0, last: true }
  const trending = await apiGetSafe<Page<PostSummary>>('/api/posts/trending?size=3', emptyPage, { revalidate: 60, tags: ['posts'] })
  const posts = trending.content.length
    ? trending.content
    : (await apiGetSafe<Page<PostSummary>>('/api/posts?size=3', emptyPage, { revalidate: 60, tags: ['posts'] })).content

  return (
    <div className="min-h-screen bg-background">
      <header className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 md:px-6">
        <Logo href="/" />
        <nav className="flex items-center gap-2">
          <ThemeToggle />
          <Link href="/home" className={buttonVariants({ variant: 'ghost' })}>
            Explore
          </Link>
          <Link href="/login" className={buttonVariants({ variant: 'ghost' })}>
            Sign in
          </Link>
          <Link href="/register" className={cn(buttonVariants(), 'rounded-full')}>
            Join
          </Link>
        </nav>
      </header>

      <main>
        <section className="mx-auto max-w-4xl px-4 pb-16 pt-20 text-center md:pt-28">
          <h1 className="text-balance text-4xl font-bold tracking-tight sm:text-6xl">
            Where developers <span className="text-primary">write, learn</span> and grow
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-pretty text-lg text-muted-foreground">
            Publish technical articles, follow the engineers you learn from, and build a reputation in a community
            that values deep, practical knowledge.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
            <Link href="/register" className={cn(buttonVariants({ size: 'lg' }), 'h-11 rounded-full px-6')}>
              Start writing <ArrowRight className="size-4" />
            </Link>
            <Link href="/home" className={cn(buttonVariants({ variant: 'outline', size: 'lg' }), 'h-11 rounded-full px-6')}>
              Read articles
            </Link>
          </div>
        </section>

        <section className="mx-auto grid max-w-6xl gap-4 px-4 pb-20 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((f) => (
            <div key={f.title} className="rounded-xl border border-border bg-card p-5">
              <f.icon className="size-5 text-primary" />
              <h2 className="mt-3 font-semibold">{f.title}</h2>
              <p className="mt-1 text-sm text-muted-foreground">{f.text}</p>
            </div>
          ))}
        </section>

        {posts.length > 0 && (
          <section className="mx-auto max-w-6xl px-4 pb-24">
            <div className="mb-5 flex items-end justify-between">
              <h2 className="text-2xl font-bold tracking-tight">Trending on StackTrace</h2>
              <Link href="/home?tab=trending" className="text-sm text-primary hover:underline">
                See all
              </Link>
            </div>
            <div className="grid gap-4 lg:grid-cols-3">
              {posts.map((p) => (
                <ArticleCard key={p.id} article={p} />
              ))}
            </div>
          </section>
        )}
      </main>

      <footer className="border-t border-border py-8 text-center text-sm text-muted-foreground">
        StackTrace · built by developers, for developers
      </footer>
    </div>
  )
}
