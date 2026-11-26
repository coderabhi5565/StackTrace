import { Suspense } from 'react'
import { AuthForm } from '@/components/auth-form'
import { AuthVisual } from '@/components/auth-visual'
import { Logo } from '@/components/logo'

export const metadata = { title: 'Sign in · StackTrace' }

export default function LoginPage() {
  return (
    <div className="grid min-h-screen lg:grid-cols-[2fr_3fr]">
      <div className="flex flex-col justify-center px-6 py-12 sm:px-12 lg:px-16">
        <div className="mx-auto w-full max-w-sm">
          <Logo href="/" className="mb-10" />
          <h1 className="text-3xl font-bold tracking-tight">Sign in</h1>
          <p className="mt-2 text-sm text-muted-foreground">Continue your developer journey</p>
          <Suspense>
            <AuthForm mode="login" />
          </Suspense>
        </div>
      </div>
      <AuthVisual />
    </div>
  )
}
