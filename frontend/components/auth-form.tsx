'use client'

import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ApiError } from '@/lib/api/errors'
import { notifySessionChange } from '@/lib/auth/session-client'

/** Only allow same-site relative redirects (no open redirect via ?next=). */
function safeNext(next: string | null): string {
  return next && next.startsWith('/') && !next.startsWith('//') ? next : '/home'
}

export function AuthForm({ mode }: { mode: 'login' | 'register' }) {
  const router = useRouter()
  const params = useSearchParams()
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = Object.fromEntries(new FormData(e.currentTarget)) as Record<string, string>
    setLoading(true)
    setErrors({})
    try {
      const res = await fetch(`/api/auth/${mode}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await res.json().catch(() => null)
      if (!res.ok) {
        if (data?.validationErrors) setErrors(data.validationErrors)
        throw new ApiError(res.status, data)
      }
      if (!data?.user) {
        toast.success(data?.message ?? 'Account created, please sign in.')
        router.push('/login')
        return
      }
      toast.success(mode === 'login' ? `Welcome back, ${data.user.name}!` : 'Account created! Welcome to StackTrace.')
      notifySessionChange()
      router.push(safeNext(params.get('next')))
      router.refresh()
    } catch (err) {
      const status = err instanceof ApiError ? err.status : 0
      toast.error(
        status === 429
          ? 'Too many attempts. Please wait a minute.'
          : err instanceof ApiError && err.message
            ? err.message
            : 'Could not reach the server',
      )
      setLoading(false)
    }
  }

  const field = (id: string, label: string, props: React.ComponentProps<typeof Input>) => (
    <div className="flex flex-col gap-2">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} name={id} required className="h-11" aria-invalid={Boolean(errors[id])} {...props} />
      {errors[id] && <p className="text-xs text-destructive">{errors[id]}</p>}
    </div>
  )

  return (
    <form onSubmit={onSubmit} className="mt-8 flex flex-col gap-4" noValidate={false}>
      {mode === 'register' && field('name', 'Full name', { placeholder: 'Jordan Park', maxLength: 100, autoComplete: 'name' })}
      {mode === 'register' &&
        field('username', 'Username', {
          placeholder: 'jordandev',
          pattern: '[A-Za-z0-9_]{3,30}',
          title: '3-30 letters, digits or underscores',
          autoComplete: 'username',
        })}
      {field('email', 'Email', { type: 'email', placeholder: 'you@dev.com', autoComplete: 'email' })}
      {field('password', 'Password', {
        type: 'password',
        placeholder: '••••••••',
        minLength: mode === 'register' ? 8 : undefined,
        maxLength: 72,
        autoComplete: mode === 'register' ? 'new-password' : 'current-password',
      })}

      <Button type="submit" disabled={loading} className="mt-2 h-11 font-medium">
        {loading ? (mode === 'login' ? 'Signing in…' : 'Creating account…') : mode === 'login' ? 'Sign in' : 'Create account'}
      </Button>

      <p className="mt-4 text-center text-sm text-muted-foreground">
        {mode === 'login' ? (
          <>
            Don&apos;t have an account?{' '}
            <Link href="/register" className="font-medium text-primary hover:underline">
              Register
            </Link>
          </>
        ) : (
          <>
            Already have an account?{' '}
            <Link href="/login" className="font-medium text-primary hover:underline">
              Sign in
            </Link>
          </>
        )}
      </p>
    </form>
  )
}
