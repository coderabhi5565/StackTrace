import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { initials } from '@/lib/format'
import { cn } from '@/lib/utils'

export function UserAvatar({
  user,
  className,
}: {
  user: { name?: string | null; username?: string | null; avatarUrl?: string | null } | null
  className?: string
}) {
  const label = user?.name || user?.username || 'User'
  return (
    <Avatar className={cn('size-9 border border-border', className)}>
      {user?.avatarUrl ? <AvatarImage src={user.avatarUrl} alt={label} loading="lazy" /> : null}
      <AvatarFallback className="bg-primary/15 font-semibold text-primary">{initials(label)}</AvatarFallback>
    </Avatar>
  )
}
