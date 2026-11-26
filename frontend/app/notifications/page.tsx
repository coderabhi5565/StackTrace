import { AppShell } from '@/components/app-shell'
import { NotificationsView } from '@/components/notifications-view'

export const metadata = { title: 'Notifications · StackTrace' }

export default function NotificationsPage() {
  return (
    <AppShell>
      <NotificationsView />
    </AppShell>
  )
}
