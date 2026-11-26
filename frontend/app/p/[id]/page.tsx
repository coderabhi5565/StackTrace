import { notFound, redirect } from 'next/navigation'
import { apiGetOrNull } from '@/lib/api/server'
import type { Post } from '@/lib/api/types'

/** Short link by id (used by notifications): /p/123 -> /post/<slug>. */
export default async function PostById({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  if (!/^\d+$/.test(id)) notFound()
  const post = await apiGetOrNull<Post>(`/api/posts/id/${id}`, { auth: true })
  if (!post) notFound()
  redirect(post.status === 'PUBLISHED' ? `/post/${post.slug}` : `/edit/${post.id}`)
}
