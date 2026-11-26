'use client'

import { Button } from '@/components/ui/button'

export default function Error({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      <h1 className="text-2xl font-bold">Something went wrong</h1>
      <p className="text-muted-foreground">The server may be temporarily unavailable.</p>
      <Button onClick={reset}>Try again</Button>
    </div>
  )
}
