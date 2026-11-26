import type { Metadata } from 'next'
import { Award, CalendarDays, Flag, Flame, Brain, MapPin, Trophy, Users, Zap } from 'lucide-react'
import Link from 'next/link'
import { notFound } from 'next/navigation'
import { AppShell } from '@/components/app-shell'
import { ArticleCard } from '@/components/article-card'
import { EmptyState } from '@/components/empty-state'
import { Pagination } from '@/components/pagination'
import { DraftsList } from '@/components/profile/drafts-list'
import { ProfileActions } from '@/components/profile/profile-actions'
import { TagPill } from '@/components/tag-pill'
import { Card } from '@/components/ui/card'
import { UserAvatar } from '@/components/user-avatar'
import { apiGet, apiGetOrNull } from '@/lib/api/server'
import type { Page, PostSummary, Profile, UserSummary } from '@/lib/api/types'
import { compact, formatDate } from '@/lib/format'
import { cn } from '@/lib/utils'

export const revalidate = 60

const ICONS: Record<string, typeof Flame> = { flame: Flame, brain: Brain, zap: Zap, trophy: Trophy, flag: Flag }
const TABS = ['posts', 'followers', 'following', 'drafts'] as const

const getProfile = (username: string) =>
  apiGetOrNull<Profile>(`/api/users/username/${encodeURIComponent(username)}`, { revalidate: 60, tags: ['users', `user:${username}`] })

export async function generateMetadata({ params }: { params: Promise<{ username: string }> }): Promise<Metadata> {
  const p = await getProfile((await params).username)
  return { title: p ? `${p.name} (@${p.username}) · StackTrace` : 'Profile not found · StackTrace', description: p?.bio ?? undefined }
}

export default async function ProfilePage({
  params,
  searchParams,
}: {
  params: Promise<{ username: string }>
  searchParams: Promise<{ tab?: string; page?: string }>
}) {
  const [{ username }, sp] = await Promise.all([params, searchParams])
  const profile = await getProfile(username)
  if (!profile) notFound()

  const tab = (TABS as readonly string[]).includes(sp.tab ?? '') ? sp.tab! : 'posts'
  const page = Math.max(0, Number(sp.page ?? 0) || 0)
  const href = (t: string, p?: number) => `/profile/${profile.username}?tab=${t}${p ? `&page=${p}` : ''}`

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-4xl">
        <Card className="overflow-hidden">
          <div className="h-32 bg-gradient-to-br from-primary/30 via-accent/20 to-primary/10 lg:h-40" />
          <div className="px-6 pb-8">
            <div className="flex flex-col gap-6 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex flex-col gap-4">
                <div className="-mt-14 w-fit">
                  <UserAvatar user={profile} className="size-28 rounded-2xl border-4 border-card text-3xl shadow-md" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold">{profile.name}</h1>
                  <p className="text-base text-muted-foreground">@{profile.username}</p>
                  {profile.bio && <p className="mt-2 max-w-lg text-pretty leading-relaxed text-foreground/90">{profile.bio}</p>}
                </div>
              </div>
              <ProfileActions profile={profile} />
            </div>

            <div className="mt-5 flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
              {profile.location && (
                <span className="flex items-center gap-1.5">
                  <MapPin className="size-4" />
                  {profile.location}
                </span>
              )}
              {profile.joinedAt && (
                <span className="flex items-center gap-1.5">
                  <CalendarDays className="size-4" />
                  Joined {formatDate(profile.joinedAt)}
                </span>
              )}
            </div>

            {profile.skills.length > 0 && (
              <div className="mt-5 flex flex-wrap gap-1.5">
                {profile.skills.map((s) => (
                  <TagPill key={s.id} tag={s.skillName.toLowerCase()} asLink={false} />
                ))}
              </div>
            )}

            <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
              <Stat icon={<Users className="size-4" />} label="Followers" value={compact(profile.followersCount)} />
              <Stat icon={<Users className="size-4" />} label="Following" value={compact(profile.followingCount)} />
              <Stat icon={<Trophy className="size-4" />} label="Points" value={compact(profile.points)} />
              <Stat icon={<Award className="size-4" />} label="Rank" value={`#${profile.rank}`} />
            </div>

            {profile.badges.length > 0 && (
              <div className="mt-6">
                <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold">
                  <Award className="size-4" />
                  Badges ({profile.badges.length})
                </h2>
                <div className="grid gap-2 sm:grid-cols-3">
                  {profile.badges.map((b) => {
                    const Icon = ICONS[b.icon ?? ''] ?? Award
                    return (
                      <div key={b.id} className="flex items-center gap-2.5 rounded-lg border border-accent/30 bg-accent/5 p-3" title={`Earned ${formatDate(b.earnedAt)}`}>
                        <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary/15 text-primary">
                          <Icon className="size-4" />
                        </div>
                        <div>
                          <p className="text-xs font-semibold leading-tight">{b.name}</p>
                          <p className="text-xs leading-tight text-muted-foreground">{b.description}</p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            )}
          </div>
        </Card>

        <div className="mt-6 flex gap-1 overflow-x-auto border-b border-border">
          {TABS.map((t) => (
            <Link
              key={t}
              href={href(t)}
              scroll={false}
              className={cn(
                'relative px-4 py-2.5 text-sm font-medium capitalize transition-colors',
                tab === t ? 'text-foreground' : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {t}
              {tab === t && <span className="absolute inset-x-2 -bottom-px h-0.5 rounded-full bg-primary" />}
            </Link>
          ))}
        </div>

        <div className="mt-6">
          {tab === 'posts' && <PostsTab authorId={profile.id} page={page} href={(p) => href('posts', p)} />}
          {tab === 'followers' && <PeopleTab path={`/api/users/${profile.id}/followers`} page={page} href={(p) => href('followers', p)} empty="No followers yet." />}
          {tab === 'following' && <PeopleTab path={`/api/users/${profile.id}/following`} page={page} href={(p) => href('following', p)} empty="Not following anyone yet." />}
          {tab === 'drafts' && <DraftsList ownerId={profile.id} />}
        </div>
      </div>
    </AppShell>
  )
}

async function PostsTab({ authorId, page, href }: { authorId: number; page: number; href: (p: number) => string }) {
  const posts = await apiGet<Page<PostSummary>>(`/api/posts/user/${authorId}?page=${page}&size=10`, { revalidate: 60, tags: ['posts'] })
  if (posts.content.length === 0) return <EmptyState title="No published articles yet." />
  return (
    <>
      <div className="flex flex-col gap-4">
        {posts.content.map((a) => (
          <ArticleCard key={a.id} article={a} />
        ))}
      </div>
      <Pagination page={posts.page} totalPages={posts.totalPages} hrefFor={href} />
    </>
  )
}

async function PeopleTab({ path, page, href, empty }: { path: string; page: number; href: (p: number) => string; empty: string }) {
  const people = await apiGet<Page<UserSummary>>(`${path}?page=${page}&size=20`, { revalidate: 60, tags: ['users'] })
  if (people.content.length === 0) return <EmptyState title={empty} />
  return (
    <>
      <Card className="divide-y divide-border p-0">
        {people.content.map((u) => (
          <Link key={u.id} href={`/profile/${u.username}`} className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-secondary/40">
            <UserAvatar user={u} />
            <div className="min-w-0">
              <p className="truncate text-sm font-medium">{u.name}</p>
              <p className="truncate text-xs text-muted-foreground">@{u.username}</p>
            </div>
          </Link>
        ))}
      </Card>
      <Pagination page={people.page} totalPages={people.totalPages} hrefFor={href} />
    </>
  )
}

function Stat({ icon, label, value }: { icon: React.ReactNode; label: string; value: string | number }) {
  return (
    <div className="rounded-lg border border-border bg-secondary/30 p-3">
      <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
        {icon}
        <span>{label}</span>
      </div>
      <p className="mt-1 font-mono text-lg font-bold">{value}</p>
    </div>
  )
}
