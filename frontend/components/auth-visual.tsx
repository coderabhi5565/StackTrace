import { Clock, Eye, Sparkles } from 'lucide-react'
import { Logo } from '@/components/logo'

const floatingCards = [
  {
    lang: 'java',
    code: '@RestController\npublic class ApiController {\n  @GetMapping("/health")\n  String ok() { return "200 OK"; }\n}',
    title: 'Building resilient REST APIs',
    meta: '8 min · 24.3k views',
  },
  {
    lang: 'python',
    code: 'def quicksort(a):\n  if len(a) <= 1: return a\n  p = a[len(a)//2]\n  return quicksort([x for x in a if x < p])',
    title: 'Sorting algorithms, visualized',
    meta: '6 min · 31.2k views',
  },
  {
    lang: 'typescript',
    code: 'type Result<T> =\n  | { ok: true; value: T }\n  | { ok: false; error: string }',
    title: 'Type-safe error handling',
    meta: '5 min · 18.7k views',
  },
]

export function AuthVisual() {
  return (
    <div className="relative hidden h-full w-full overflow-hidden bg-sidebar lg:block">
      {/* gradient glow */}
      <div
        className="pointer-events-none absolute -left-32 top-1/4 size-96 rounded-full opacity-30 blur-[120px]"
        style={{ background: 'radial-gradient(circle, oklch(0.6 0.2 260), transparent 70%)' }}
      />
      <div
        className="pointer-events-none absolute -right-20 bottom-1/4 size-80 rounded-full opacity-30 blur-[120px]"
        style={{ background: 'radial-gradient(circle, oklch(0.7 0.18 200), transparent 70%)' }}
      />
      <div className="absolute inset-0 glow-grid opacity-60" />

      <div className="relative flex h-full flex-col justify-center gap-5 p-12">
        {floatingCards.map((card, i) => (
          <div
            key={card.title}
            className="rounded-2xl border border-border bg-card/80 p-5 shadow-xl backdrop-blur-sm"
            style={{
              marginLeft: i === 1 ? '3rem' : i === 2 ? '1.5rem' : 0,
              maxWidth: '26rem',
            }}
          >
            <div className="mb-3 flex items-center gap-2">
              <span className="flex size-2 rounded-full bg-destructive/70" />
              <span className="flex size-2 rounded-full bg-warning/70" />
              <span className="flex size-2 rounded-full bg-primary/70" />
              <span className="ml-2 rounded-md bg-secondary px-2 py-0.5 font-mono text-xs text-muted-foreground">
                {card.lang}
              </span>
            </div>
            <pre className="overflow-hidden font-mono text-xs leading-relaxed text-primary/90">
              {card.code}
            </pre>
            <p className="mt-4 text-sm font-semibold text-card-foreground">
              {card.title}
            </p>
            <div className="mt-1.5 flex items-center gap-3 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <Clock className="size-3" />
                {card.meta.split(' · ')[0]}
              </span>
              <span className="flex items-center gap-1">
                <Eye className="size-3" />
                {card.meta.split(' · ')[1]}
              </span>
              <span className="flex items-center gap-1 text-primary">
                <Sparkles className="size-3" />
                Quiz
              </span>
            </div>
          </div>
        ))}
      </div>

      <div className="absolute bottom-10 left-12 right-12">
        <Logo href="/home" className="mb-3" />
        <p className="text-balance text-2xl font-bold tracking-tight text-foreground">
          Where developers write, learn and grow.
        </p>
      </div>
    </div>
  )
}
