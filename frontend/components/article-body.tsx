import { CodeBlock } from "@/components/code-block"
import type { Block } from "@/components/create-post"

export function ArticleBody({ blocks }: { blocks: Block[] }) {
  if (!blocks.length) {
    return <p className="text-sm italic text-muted-foreground">Start typing to see your article preview.</p>
  }

  return (
    <div className="leading-relaxed">
      {blocks.map((block, i) => {
        if (block.type === "heading") {
          return (
            <h2 key={i} className="mt-6 text-xl font-bold tracking-tight first:mt-0">
              {block.text}
            </h2>
          )
        }
        if (block.type === "paragraph") {
          return (
            <p key={i} className="mt-3 text-[15px] leading-7 text-foreground/90">
              {block.text}
            </p>
          )
        }
        return (
          <CodeBlock key={i} block={{ language: block.lang, code: block.code, output: undefined }} />
        )
      })}
    </div>
  )
}
