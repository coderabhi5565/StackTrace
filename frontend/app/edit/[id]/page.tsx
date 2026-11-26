import dynamic from 'next/dynamic'
import { notFound } from 'next/navigation'
import { cookies } from 'next/headers'
import { AppShell } from '@/components/app-shell'
import { Skeleton } from '@/components/ui/skeleton'
import { apiGetOrNull, apiGetSafe } from '@/lib/api/server'
import { USER_COOKIE, decodeUser } from '@/lib/auth/cookies'
import type { Post, Tag } from '@/lib/api/types'

export const metadata = { title: 'Edit · StackTrace' }

const PostEditor = dynamic(() => import('@/components/post-editor').then((m) => m.PostEditor), {
  loading: () => <Skeleton className="h-[70vh] w-full" />,
})

export default async function EditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  if (!/^\d+$/.test(id)) notFound()
  const [post, tags, me] = await Promise.all([
    apiGetOrNull<Post>(`/api/posts/id/${id}`, { auth: true }),
    apiGetSafe<Tag[]>('/api/tags?limit=30', [], { revalidate: 300, tags: ['tags'] }),
    cookies().then((c) => decodeUser(c.get(USER_COOKIE)?.value)),
  ])
  // other people's posts are not editable (the backend enforces this on save as well)
  if (!post || !me || post.author.id !== me.id) notFound()
  return (
    <AppShell>
      <PostEditor post={post} suggestions={tags.map((t) => t.name)} />
    </AppShell>
  )
}
