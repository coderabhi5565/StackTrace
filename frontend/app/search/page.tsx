import { Suspense } from 'react'
import { AppShell } from '@/components/app-shell'
import { SearchView } from '@/components/search-view'
import { Skeleton } from '@/components/ui/skeleton'

export const metadata = { title: 'Search · StackTrace' }

export default function SearchPage() {
  return (
    <AppShell>
      <Suspense fallback={<Skeleton className="mx-auto h-12 max-w-3xl" />}>
        <SearchView />
      </Suspense>
    </AppShell>
  )
}
