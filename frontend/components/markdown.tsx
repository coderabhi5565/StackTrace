import ReactMarkdown, { type Components } from 'react-markdown'
import rehypeSanitize, { defaultSchema } from 'rehype-sanitize'
import remarkGfm from 'remark-gfm'
import { CodeBlock } from '@/components/code-block'

// Allow `language-xxx` classes on <code> (for the language label); everything else uses
// GitHub's sanitization schema: no raw HTML, scripts, event handlers or javascript: URLs.
const schema = {
  ...defaultSchema,
  attributes: {
    ...defaultSchema.attributes,
    code: [...(defaultSchema.attributes?.code ?? []), ['className', /^language-[\w+#-]+$/]],
  },
}

const components: Components = {
  h1: ({ node: _n, ...p }) => <h2 className="mt-8 text-2xl font-bold tracking-tight" {...p} />,
  h2: ({ node: _n, ...p }) => <h2 className="mt-8 text-2xl font-bold tracking-tight" {...p} />,
  h3: ({ node: _n, ...p }) => <h3 className="mt-6 text-xl font-semibold tracking-tight" {...p} />,
  h4: ({ node: _n, ...p }) => <h4 className="mt-5 text-lg font-semibold" {...p} />,
  p: ({ node: _n, ...p }) => <p className="mt-4 text-[15px] leading-7 text-foreground/90" {...p} />,
  ul: ({ node: _n, ...p }) => <ul className="mt-4 list-disc space-y-1.5 pl-6 text-[15px] text-foreground/90" {...p} />,
  ol: ({ node: _n, ...p }) => <ol className="mt-4 list-decimal space-y-1.5 pl-6 text-[15px] text-foreground/90" {...p} />,
  blockquote: ({ node: _n, ...p }) => <blockquote className="mt-4 border-l-2 border-primary/50 pl-4 italic text-muted-foreground" {...p} />,
  a: ({ node: _n, href, ...p }) => (
    <a href={href} className="text-primary underline-offset-4 hover:underline" rel="noopener noreferrer nofollow" target="_blank" {...p} />
  ),
  hr: () => <hr className="my-8 border-border" />,
  table: (p) => (
    <div className="mt-4 overflow-x-auto">
      <table className="w-full border-collapse text-sm" {...p} />
    </div>
  ),
  th: ({ node: _n, ...p }) => <th className="border border-border bg-secondary/50 px-3 py-1.5 text-left font-semibold" {...p} />,
  td: ({ node: _n, ...p }) => <td className="border border-border px-3 py-1.5" {...p} />,
  // eslint-disable-next-line @next/next/no-img-element
  img: ({ src, alt }) => <img src={typeof src === 'string' ? src : ''} alt={alt ?? ''} loading="lazy" className="mt-4 max-w-full rounded-lg" />,
  pre: ({ children }) => <>{children}</>,
  code: ({ className, children }) => {
    const match = /language-([\w+#-]+)/.exec(className ?? '')
    const text = String(children ?? '')
    // fenced blocks have a language or span multiple lines; everything else is inline code
    if (match || text.includes('\n')) {
      return <CodeBlock language={match?.[1] ?? 'text'} code={text.replace(/\n$/, '')} />
    }
    return <code className="rounded bg-secondary px-1.5 py-0.5 font-mono text-[0.85em]">{children}</code>
  },
}

/** Renders user-authored markdown safely (sanitized; works in server and client components). */
export function Markdown({ source }: { source: string }) {
  return (
    <div className="leading-relaxed">
      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[[rehypeSanitize, schema]]} components={components}>
        {source}
      </ReactMarkdown>
    </div>
  )
}
