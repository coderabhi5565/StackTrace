import { ArticleCardSkeleton } from '@/components/article-card-skeleton'

export default function Loading() {
  return (
    <div className="mx-auto mt-24 flex max-w-2xl flex-col gap-4 px-4">
      {Array.from({ length: 4 }).map((_, i) => (
        <ArticleCardSkeleton key={i} />
      ))}
    </div>
  )
}
