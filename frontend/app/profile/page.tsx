import { cookies } from 'next/headers'
import { redirect } from 'next/navigation'
import { USER_COOKIE, decodeUser } from '@/lib/auth/cookies'

/** /profile -> the signed-in user's own profile. */
export default async function MyProfile({ searchParams }: { searchParams: Promise<{ tab?: string }> }) {
  const me = decodeUser((await cookies()).get(USER_COOKIE)?.value)
  if (!me) redirect('/login?next=/profile')
  const { tab } = await searchParams
  redirect(`/profile/${me.username}${tab ? `?tab=${encodeURIComponent(tab)}` : ''}`)
}
