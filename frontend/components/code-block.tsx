'use client'

import { Check, Copy } from 'lucide-react'
import { useState } from 'react'
import { Button } from '@/components/ui/button'

export function CodeBlock({ language, code }: { language: string; code: string }) {
  const [copied, setCopied] = useState(false)

  function copy() {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    })
  }

  return (
    <div className="my-5 overflow-hidden rounded-xl border border-border bg-[oklch(0.14_0.004_250)]">
      <div className="flex items-center justify-between border-b border-border bg-secondary/50 px-3 py-1.5">
        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-medium text-muted-foreground">
          {language}
        </span>
        <Button variant="ghost" size="sm" className="h-7 gap-1.5 px-2 text-xs" onClick={copy}>
          {copied ? <Check className="size-3.5 text-primary" /> : <Copy className="size-3.5" />}
          {copied ? 'Copied' : 'Copy'}
        </Button>
      </div>
      <pre className="overflow-x-auto p-4 font-mono text-sm leading-relaxed text-foreground/90">
        <code>{code}</code>
      </pre>
    </div>
  )
}
