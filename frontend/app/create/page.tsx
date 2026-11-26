import dynamic from 'next/dynamic'
import { AppShell } from '@/components/app-shell'
import { Skeleton } from '@/components/ui/skeleton'
import { apiGetSafe } from '@/lib/api/server'
import type { Tag } from '@/lib/api/types'

export const metadata = { title: 'Write · StackTrace' }

// The editor is heavy and interactive: split it into its own chunk.
const PostEditor = dynamic(() => import('@/components/post-editor').then((m) => m.PostEditor), {
  loading: () => <Skeleton className="h-[70vh] w-full" />,
})

export default async function CreatePage() {
  const tags = await apiGetSafe<Tag[]>('/api/tags?limit=30', [], { revalidate: 300, tags: ['tags'] })
  return (
    <AppShell>
      <PostEditor suggestions={tags.map((t) => t.name)} />
    </AppShell>
  )
}
