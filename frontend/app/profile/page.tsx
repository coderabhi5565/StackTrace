"use client"

import { Suspense, useState } from "react"
import Image from "next/image"
import { useSearchParams } from "next/navigation"
import { AppShell } from "@/components/app-shell"
import { ArticleCard } from "@/components/article-card"
import { TagPill } from "@/components/tag-pill"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import {
  currentUser,
  articles,
  badges,
  type Article,
} from "@/lib/mock-data"
import {
  Code,
  Link2,
  AtSign,
  Globe,
  MapPin,
  CalendarDays,
  Flame,
  Target,
  Brain,
  Zap,
  Trophy,
  Flag,
  BookOpen,
  Eye,
  Users,
  Award,
} from "lucide-react"
import { cn } from "@/lib/utils"

const iconMap: Record<string, typeof Flame> = {
  flame: Flame,
  target: Target,
  brain: Brain,
  zap: Zap,
  trophy: Trophy,
  flag: Flag,
}

const tabs = [
  { id: "written", label: "Written" },
  { id: "activity", label: "Reading Activity" },
  { id: "bookmarks", label: "Bookmarks" },
  { id: "drafts", label: "Drafts" },
]

function ProfileContent() {
  const params = useSearchParams()
  const initial = params.get("tab") ?? "written"
  const [tab, setTab] = useState(initial)
  const [following, setFollowing] = useState(false)

  const userPosts = articles.slice(0, 4)
  const bookmarked = articles.filter((a) => a.bookmarked)
  const drafts: Article[] = articles.slice(4, 5)
  const unlockedBadges = badges.filter((b) => b.unlocked)

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-4xl">
        {/* Cover + Header card */}
        <Card className="overflow-hidden">
          {/* Cover Photo */}
          <div className="relative h-40 bg-gradient-to-br from-primary/30 via-accent/20 to-primary/10 lg:h-48">
            <div className="absolute inset-0 opacity-20" style={{
              backgroundImage: 'linear-gradient(45deg, var(--color-primary) 25%, transparent 25%, transparent 75%, var(--color-primary) 75%, var(--color-primary)), linear-gradient(45deg, var(--color-primary) 25%, transparent 25%, transparent 75%, var(--color-primary) 75%, var(--color-primary))',
              backgroundSize: '60px 60px',
              backgroundPosition: '0 0, 30px 30px',
            }} />
          </div>

          <div className="px-6 pb-8">
            {/* Avatar + Basic Info */}
            <div className="flex flex-col gap-6 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex flex-col gap-4">
                <div className="-mt-20 w-fit">
                  <Image
                    src={currentUser.avatar || "/placeholder.svg"}
                    alt={currentUser.name}
                    width={120}
                    height={120}
                    className="size-28 rounded-2xl border-4 border-card object-cover shadow-md"
                  />
                </div>
                <div>
                  <h1 className="text-2xl font-bold">{currentUser.name}</h1>
                  <p className="text-base text-muted-foreground">@{currentUser.username}</p>
                  <p className="mt-2 max-w-lg text-pretty leading-relaxed text-foreground/90">{currentUser.bio}</p>
                </div>
              </div>
              <div className="flex flex-col gap-2 sm:items-end">
                <Button variant="outline" size="sm">Edit profile</Button>
                <Button 
                  onClick={() => setFollowing((f) => !f)} 
                  variant={following ? "secondary" : "default"}
                  size="sm"
                >
                  {following ? "Following" : "Follow"}
                </Button>
              </div>
            </div>

            {/* Location + Joined + Social Links */}
            <div className="mt-5 space-y-3">
              <div className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
                <span className="flex items-center gap-1.5">
                  <MapPin className="size-4" />
                  {currentUser.location}
                </span>
                <span className="flex items-center gap-1.5">
                  <CalendarDays className="size-4" />
                  Joined {currentUser.joined}
                </span>
              </div>
              
              <div className="flex flex-wrap gap-2">
                {currentUser.github && (
                  <a href="#" className="inline-flex items-center gap-1.5 rounded-lg bg-secondary/40 px-3 py-1.5 text-xs hover:bg-secondary/60 transition-colors">
                    <Code className="size-3.5" />
                    GitHub
                  </a>
                )}
                {currentUser.linkedin && (
                  <a href="#" className="inline-flex items-center gap-1.5 rounded-lg bg-secondary/40 px-3 py-1.5 text-xs hover:bg-secondary/60 transition-colors">
                    <Link2 className="size-3.5" />
                    LinkedIn
                  </a>
                )}
                {currentUser.x && (
                  <a href="#" className="inline-flex items-center gap-1.5 rounded-lg bg-secondary/40 px-3 py-1.5 text-xs hover:bg-secondary/60 transition-colors">
                    <AtSign className="size-3.5" />
                    X
                  </a>
                )}
                {currentUser.portfolio && (
                  <a href="#" className="inline-flex items-center gap-1.5 rounded-lg bg-secondary/40 px-3 py-1.5 text-xs hover:bg-secondary/60 transition-colors">
                    <Globe className="size-3.5" />
                    Portfolio
                  </a>
                )}
              </div>
            </div>

            {/* Skills */}
            {currentUser.skills.length > 0 && (
              <div className="mt-5 flex flex-wrap gap-1.5">
                {currentUser.skills.map((s) => (
                  <TagPill key={s} tag={s} />
                ))}
              </div>
            )}

            {/* Stats Row */}
            <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-5">
              <Stat icon={<BookOpen className="size-4" />} label="Articles" value={currentUser.articles} />
              <Stat icon={<Eye className="size-4" />} label="Total reads" value={fmt(currentUser.totalReads)} />
              <Stat icon={<Users className="size-4" />} label="Followers" value={fmt(currentUser.followers)} />
              <Stat icon={<Trophy className="size-4" />} label="Points" value={fmt(currentUser.points)} />
              <Stat icon={<Award className="size-4" />} label="Rank" value={`#${currentUser.rank}`} />
            </div>

            {/* Badges Section */}
            {unlockedBadges.length > 0 && (
              <div className="mt-6">
                <h3 className="mb-3 text-sm font-semibold flex items-center gap-2">
                  <Award className="size-4" />
                  Badges ({unlockedBadges.length})
                </h3>
                <div className="grid gap-2 sm:grid-cols-3">
                  {unlockedBadges.map((b) => {
                    const Icon = iconMap[b.icon] ?? Flame
                    return (
                      <div
                        key={b.id}
                        className="flex items-center gap-2.5 rounded-lg border border-accent/30 bg-accent/5 p-3"
                        title={b.description}
                      >
                        <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary/15 text-primary">
                          <Icon className="size-4" />
                        </div>
                        <div>
                          <p className="text-xs font-semibold leading-tight">{b.name}</p>
                          <p className="text-xs text-muted-foreground leading-tight">{b.description}</p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            )}
          </div>
        </Card>

        {/* Tabs */}
        <div className="mt-6 flex gap-1 border-b border-border">
          {tabs.map((t) => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={cn(
                "relative px-4 py-2.5 text-sm font-medium transition-colors",
                tab === t.id ? "text-foreground" : "text-muted-foreground hover:text-foreground",
              )}
            >
              {t.label}
              {tab === t.id && <span className="absolute inset-x-2 -bottom-px h-0.5 rounded-full bg-primary" />}
            </button>
          ))}
        </div>

        {/* Tab content */}
        <div className="mt-6">
          {tab === "written" ? (
            <div className="flex flex-col gap-4">
              {userPosts.map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
          ) : tab === "activity" ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <Card key={i} className="p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Completed quiz on React Hooks</p>
                      <p className="text-sm text-muted-foreground">Scored 8/10 • 2 days ago</p>
                    </div>
                    <span className="text-sm font-semibold text-accent">+80 points</span>
                  </div>
                </Card>
              ))}
            </div>
          ) : tab === "bookmarks" ? (
            <div className="flex flex-col gap-4">
              {bookmarked.length > 0 ? (
                bookmarked.map((a) => (
                  <ArticleCard key={a.id} article={a} />
                ))
              ) : (
                <p className="py-12 text-center text-sm text-muted-foreground">No bookmarks yet.</p>
              )}
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {drafts.length > 0 ? (
                drafts.map((a) => (
                  <ArticleCard key={a.id} article={a} />
                ))
              ) : (
                <p className="py-12 text-center text-sm text-muted-foreground">No drafts yet.</p>
              )}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}

function Stat({ icon, label, value }: { icon: React.ReactNode; label: string; value: string | number }) {
  return (
    <div className="rounded-lg border border-border bg-secondary/30 p-3">
      <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
        {icon}
        <span className="hidden sm:inline">{label}</span>
      </div>
      <p className="mt-1 font-mono text-lg font-bold">{value}</p>
    </div>
  )
}

function fmt(n: number) {
  return n >= 1000 ? `${(n / 1000).toFixed(1)}k` : `${n}`
}

export default function ProfilePage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ProfileContent />
    </Suspense>
  )
}
