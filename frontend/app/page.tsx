'use client'

import { Code } from 'lucide-react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { AuthVisual } from '@/components/auth-visual'
import { Logo } from '@/components/logo'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

export default function LoginPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)

  function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    toast.success('Welcome back!')
    setTimeout(() => router.push('/home'), 600)
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-[2fr_3fr]">
      {/* Left - form (40%) */}
      <div className="flex flex-col justify-center px-6 py-12 sm:px-12 lg:px-16">
        <div className="mx-auto w-full max-w-sm">
          <Logo href="/home" className="mb-10" />

          <h1 className="text-3xl font-bold tracking-tight">Sign in</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Continue your developer journey
          </p>

          <form onSubmit={onSubmit} className="mt-8 flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                required
                placeholder="you@dev.com"
                defaultValue="jordan@dev.com"
                className="h-11"
              />
            </div>
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
                <Link href="/" className="text-xs text-primary hover:underline">
                  Forgot?
                </Link>
              </div>
              <Input
                id="password"
                type="password"
                required
                placeholder="••••••••"
                defaultValue="password"
                className="h-11"
              />
            </div>

            <Button type="submit" disabled={loading} className="mt-2 h-11 font-medium">
              {loading ? 'Signing in...' : 'Sign in'}
            </Button>
          </form>

          <div className="my-6 flex items-center gap-3">
            <div className="h-px flex-1 bg-border" />
            <span className="text-xs text-muted-foreground">or</span>
            <div className="h-px flex-1 bg-border" />
          </div>

          <Button
            variant="outline"
            className="h-11 w-full font-medium"
            onClick={() => {
              toast.success('Welcome back!')
              setTimeout(() => router.push('/home'), 600)
            }}
          >
            <Code className="size-4" />
            Continue with GitHub
          </Button>

          <p className="mt-8 text-center text-sm text-muted-foreground">
            Don&apos;t have an account?{' '}
            <Link href="/register" className="font-medium text-primary hover:underline">
              Register
            </Link>
          </p>
        </div>
      </div>

      {/* Right - visual (60%) */}
      <AuthVisual />
    </div>
  )
}
