import { AppShell } from '@/components/app-shell'
import { HomeFeed } from '@/components/home-feed'
import { HomeRightSidebar } from '@/components/home-right-sidebar'

export default async function HomePage({
  searchParams,
}: {
  searchParams: Promise<{ tab?: string }>
}) {
  const { tab } = await searchParams
  return (
    <AppShell rightSidebar={<HomeRightSidebar />}>
      <HomeFeed initialTab={tab} />
    </AppShell>
  )
}
