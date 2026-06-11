"use client"

import { useMemo, useState, Suspense } from "react"
import { useSearchParams } from "next/navigation"
import { AppShell } from "@/components/app-shell"
import { ArticleCard } from "@/components/article-card"
import { Input } from "@/components/ui/input"
import { TAGS } from "@/lib/mock-data"
import { articles } from "@/lib/mock-data"
import { Search as SearchIcon, SlidersHorizontal } from "lucide-react"
import { cn } from "@/lib/utils"

const sorts = ["Relevant", "Newest", "Most read", "Most liked"]

function SearchContent() {
  const params = useSearchParams()
  const [query, setQuery] = useState(params.get("q") ?? "")
  const [activeTags, setActiveTags] = useState<string[]>([])
  const [sort, setSort] = useState("Relevant")

  function toggleTag(tag: string) {
    setActiveTags((prev) => (prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]))
  }

  const results = useMemo(() => {
    let list = articles.filter((a) => {
      const matchesQuery =
        !query ||
        a.title.toLowerCase().includes(query.toLowerCase()) ||
        a.description.toLowerCase().includes(query.toLowerCase())
      const matchesTags = activeTags.length === 0 || activeTags.every((t) => a.tags.includes(t))
      return matchesQuery && matchesTags
    })
    if (sort === "Newest") list = [...list].reverse()
    if (sort === "Most read") list = [...list].sort((a, b) => b.views - a.views)
    if (sort === "Most liked") list = [...list].sort((a, b) => b.likes - a.likes)
    return list
  }, [query, activeTags, sort])

  return (
    <AppShell>
      <div className="mx-auto w-full max-w-3xl">
        <h1 className="font-mono text-2xl font-bold tracking-tight">Search</h1>

        <div className="relative mt-4">
          <SearchIcon className="absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search articles, topics, technologies..."
            className="h-12 pl-11 text-base"
            autoFocus
          />
        </div>

        {/* Tag filters */}
        <div className="mt-4 flex flex-wrap gap-1.5">
          {TAGS.map((tag) => (
            <button
              key={tag}
              onClick={() => toggleTag(tag)}
              className={cn(
                "rounded-full px-3 py-1 font-mono text-xs transition-colors",
                activeTags.includes(tag)
                  ? "bg-primary text-primary-foreground"
                  : "bg-secondary text-secondary-foreground hover:bg-secondary/70",
              )}
            >
              #{tag}
            </button>
          ))}
        </div>

        {/* Sort + count */}
        <div className="mt-5 flex items-center justify-between">
          <p className="text-sm text-muted-foreground">
            {results.length} {results.length === 1 ? "result" : "results"}
          </p>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <SlidersHorizontal className="size-4" />
            <select
              value={sort}
              onChange={(e) => setSort(e.target.value)}
              className="cursor-pointer rounded-md border border-border bg-card px-2 py-1 text-sm text-foreground focus:outline-none focus:ring-1 focus:ring-ring"
            >
              {sorts.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Results */}
        <div className="mt-4 flex flex-col gap-4">
          {results.length === 0 ? (
            <div className="py-16 text-center">
              <p className="text-sm font-medium">No results found</p>
              <p className="mt-1 text-sm text-muted-foreground">Try a different search or remove some filters.</p>
            </div>
          ) : (
            results.map((a) => <ArticleCard key={a.id} article={a} />)
          )}
        </div>
      </div>
    </AppShell>
  )
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <SearchContent />
    </Suspense>
  )
}
