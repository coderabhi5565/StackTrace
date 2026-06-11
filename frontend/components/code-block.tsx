'use client'

import { Check, Copy, Play, Loader2 } from 'lucide-react'
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import type { CodeBlock as CodeBlockType } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

export function CodeBlock({ block }: { block: CodeBlockType }) {
  const [copied, setCopied] = useState(false)
  const [running, setRunning] = useState(false)
  const [output, setOutput] = useState<string | null>(null)

  function copy() {
    navigator.clipboard.writeText(block.code)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  function run() {
    setRunning(true)
    setOutput(null)
    setTimeout(() => {
      setRunning(false)
      setOutput(block.output ?? 'Program exited with code 0')
    }, 900)
  }

  return (
    <div className="my-5 overflow-hidden rounded-xl border border-border bg-[oklch(0.14_0.004_250)]">
      <div className="flex items-center justify-between border-b border-border bg-secondary/50 px-3 py-1.5">
        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-medium text-muted-foreground">
          {block.language}
        </span>
        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="sm"
            className="h-7 gap-1.5 px-2 text-xs"
            onClick={copy}
          >
            {copied ? (
              <Check className="size-3.5 text-primary" />
            ) : (
              <Copy className="size-3.5" />
            )}
            {copied ? 'Copied' : 'Copy'}
          </Button>
          {block.output !== undefined && (
            <Button
              size="sm"
              className="h-7 gap-1.5 bg-primary px-2.5 text-xs text-primary-foreground hover:bg-primary/90"
              onClick={run}
              disabled={running}
            >
              {running ? (
                <Loader2 className="size-3.5 animate-spin" />
              ) : (
                <Play className="size-3.5 fill-current" />
              )}
              Run
            </Button>
          )}
        </div>
      </div>

      <pre className="overflow-x-auto p-4 font-mono text-sm leading-relaxed text-foreground/90">
        <code>{block.code}</code>
      </pre>

      {(running || output !== null) && (
        <div className="border-t border-border bg-black/40 px-4 py-3">
          <p className="mb-1.5 font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Output
          </p>
          <pre
            className={cn(
              'whitespace-pre-wrap font-mono text-sm',
              running ? 'text-muted-foreground' : 'text-primary',
            )}
          >
            {running ? 'Running...' : output}
          </pre>
        </div>
      )}
    </div>
  )
}
