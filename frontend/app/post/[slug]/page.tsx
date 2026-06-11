import { notFound } from 'next/navigation'
import { AppShell } from '@/components/app-shell'
import { PostView } from '@/components/post-view'
import { getArticle } from '@/lib/mock-data'

export default async function PostPage({
  params,
}: {
  params: Promise<{ slug: string }>
}) {
  const { slug } = await params
  const article = getArticle(slug)
  if (!article) notFound()

  return (
    <AppShell>
      <PostView article={article} />
    </AppShell>
  )
}
