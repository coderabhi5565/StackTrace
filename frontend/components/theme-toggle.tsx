'use client'

import { Moon, Sun } from 'lucide-react'
import { useTheme } from 'next-themes'
import { useSyncExternalStore } from 'react'
import { Button } from '@/components/ui/button'

const noop = () => () => {}

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  // false on the server and during hydration, true afterwards (theme is only known client-side)
  const mounted = useSyncExternalStore(noop, () => true, () => false)

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label="Toggle theme"
      onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
    >
      {mounted && theme === 'dark' ? <Sun className="size-5" /> : <Moon className="size-5" />}
    </Button>
  )
}
