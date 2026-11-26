'use client'

import dynamic from 'next/dynamic'
import { Eye, FileText, ImagePlus, Plus, Sparkles, X } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import { toast } from 'sonner'
import { TagPill } from '@/components/tag-pill'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { api, errorMessage } from '@/lib/api/client'
import type { Post } from '@/lib/api/types'
import { cn } from '@/lib/utils'

// The markdown renderer is only needed for the preview: load it on demand.
const MarkdownPreview = dynamic(() => import('@/components/markdown').then((m) => m.Markdown), {
  loading: () => <Skeleton className="h-40 w-full" />,
})

const MAX_TAGS = 5
const TAG_RE = /^[a-z0-9][a-z0-9+#.-]{0,29}$/
const LANGS = ['js', 'ts', 'java', 'python', 'go', 'rust', 'sql', 'bash', 'json', 'yaml']

const STARTER = `## Introduction

Start writing your article here. Markdown is supported: **bold**, _italic_, \`inline code\`, lists, links and tables.

\`\`\`java
System.out.println("Hello, StackTrace!");
\`\`\`
`

export function PostEditor({ post, suggestions }: { post?: Post; suggestions: string[] }) {
  const router = useRouter()
  const editing = Boolean(post)
  const published = post?.status === 'PUBLISHED'

  const [title, setTitle] = useState(post?.title ?? '')
  const [excerpt, setExcerpt] = useState(post?.excerpt ?? '')
  const [cover, setCover] = useState(post?.coverImageUrl ?? '')
  const [body, setBody] = useState(post?.content ?? STARTER)
  const [tags, setTags] = useState<string[]>(post?.tags ?? [])
  const [tagInput, setTagInput] = useState('')
  const [showPreview, setShowPreview] = useState(false)
  const [codeOpen, setCodeOpen] = useState(false)
  const [codeLang, setCodeLang] = useState('js')
  const [codeContent, setCodeContent] = useState('')
  const [saving, setSaving] = useState<null | 'draft' | 'publish'>(null)
  const [uploading, setUploading] = useState(false)
  const dirty = useRef(false)
  const fileInput = useRef<HTMLInputElement>(null)

  // warn before leaving with unsaved changes
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (dirty.current) e.preventDefault()
    }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [])

  const touch =
    <T,>(set: (v: T) => void) =>
    (v: T) => {
      dirty.current = true
      set(v)
    }

  function addTag(raw: string) {
    const tag = raw.trim().replace(/^#/, '').toLowerCase()
    if (!tag) return
    if (!TAG_RE.test(tag)) {
      toast.error('Tags are 1-30 characters: letters, digits, + # . -')
      return
    }
    if (tags.includes(tag)) return
    if (tags.length >= MAX_TAGS) {
      toast.error(`At most ${MAX_TAGS} tags`)
      return
    }
    dirty.current = true
    setTags([...tags, tag])
    setTagInput('')
  }

  function insertCode() {
    if (!codeContent.trim()) {
      toast.error('Add some code first')
      return
    }
    touch(setBody)(`${body.trimEnd()}\n\n\`\`\`${codeLang}\n${codeContent}\n\`\`\`\n`)
    setCodeContent('')
    setCodeOpen(false)
  }

  async function upload(file: File) {
    setUploading(true)
    try {
      const form = new FormData()
      form.append('file', file)
      const res = await fetch('/api/upload', { method: 'POST', body: form })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) throw new Error(data?.message ?? 'Upload failed')
      touch(setCover)(data.imageUrl)
      toast.success('Cover image uploaded')
    } catch (e) {
      toast.error(e instanceof Error ? e.message : 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  function validate(): boolean {
    if (!title.trim()) return toast.error('Add a title'), false
    if (!body.trim()) return toast.error('Write some content'), false
    if (tags.length === 0) return toast.error('Add at least one tag'), false
    return true
  }

  async function save(mode: 'draft' | 'publish') {
    if (!validate()) return
    setSaving(mode)
    const payload = {
      title: title.trim(),
      content: body,
      excerpt: excerpt.trim() || null,
      coverImageUrl: cover.trim() || null,
      tags,
    }
    try {
      let saved: Post
      if (!post) {
        saved = await api<Post>('/posts', { method: 'POST', body: { ...payload, publish: mode === 'publish' } })
      } else {
        saved = await api<Post>(`/posts/${post.id}`, { method: 'PUT', body: payload })
        if (mode === 'publish' && saved.status !== 'PUBLISHED') {
          saved = await api<Post>(`/posts/${post.id}/publish`, { method: 'POST' })
        }
      }
      dirty.current = false
      if (saved.status === 'PUBLISHED') {
        toast.success(published ? 'Article updated' : 'Article published!')
        router.push(`/post/${saved.slug}`)
      } else {
        toast.success('Draft saved')
        if (!post) router.replace(`/edit/${saved.id}`)
      }
      router.refresh()
    } catch (e) {
      toast.error(errorMessage(e, 'Could not save the article'))
    } finally {
      setSaving(null)
    }
  }

  const tagSuggestions = suggestions.filter((t) => !tags.includes(t) && t.startsWith(tagInput.toLowerCase())).slice(0, 8)

  return (
    <div className="mx-auto w-full max-w-7xl">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-mono text-2xl font-bold tracking-tight">{editing ? 'Edit article' : 'Write an article'}</h1>
          <p className="text-sm text-muted-foreground">
            {published ? 'Changes go live immediately.' : 'Share knowledge with the developer community.'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" className="lg:hidden" onClick={() => setShowPreview((p) => !p)}>
            <Eye className="h-4 w-4" />
            {showPreview ? 'Edit' : 'Preview'}
          </Button>
          {!published && (
            <Button variant="outline" onClick={() => save('draft')} disabled={saving !== null}>
              <FileText className="h-4 w-4" />
              {saving === 'draft' ? 'Saving…' : 'Save draft'}
            </Button>
          )}
          <Button onClick={() => save('publish')} disabled={saving !== null}>
            <Sparkles className="h-4 w-4" />
            {saving === 'publish' ? 'Saving…' : published ? 'Update' : 'Publish'}
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className={cn('flex flex-col gap-5', showPreview && 'hidden lg:flex')}>
          <div className="flex flex-col gap-2">
            <Label htmlFor="title">Title</Label>
            <Input id="title" placeholder="A descriptive, searchable title" value={title} maxLength={250} onChange={(e) => touch(setTitle)(e.target.value)} className="text-base" />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="excerpt">
              Excerpt <span className="font-normal text-muted-foreground">(optional, generated from the content if empty)</span>
            </Label>
            <Textarea id="excerpt" placeholder="A one or two sentence summary that appears on cards." value={excerpt} maxLength={300} onChange={(e) => touch(setExcerpt)(e.target.value)} rows={2} />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="tags">
              Tags ({tags.length}/{MAX_TAGS})
            </Label>
            <div className="flex flex-wrap items-center gap-1.5 rounded-lg border border-border bg-card p-2">
              {tags.map((t) => (
                <span key={t} className="inline-flex items-center gap-1 rounded-md bg-primary px-2 py-0.5 font-mono text-xs text-primary-foreground">
                  {t}
                  <button type="button" aria-label={`Remove ${t}`} onClick={() => touch(setTags)(tags.filter((x) => x !== t))}>
                    <X className="size-3" />
                  </button>
                </span>
              ))}
              <input
                id="tags"
                value={tagInput}
                onChange={(e) => setTagInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ',') {
                    e.preventDefault()
                    addTag(tagInput)
                  } else if (e.key === 'Backspace' && !tagInput && tags.length) {
                    touch(setTags)(tags.slice(0, -1))
                  }
                }}
                placeholder={tags.length < MAX_TAGS ? 'Add a tag and press Enter' : ''}
                disabled={tags.length >= MAX_TAGS}
                className="min-w-32 flex-1 bg-transparent px-1 py-0.5 text-sm outline-none"
              />
            </div>
            {tagSuggestions.length > 0 && tags.length < MAX_TAGS && (
              <div className="flex flex-wrap gap-1.5">
                {tagSuggestions.map((t) => (
                  <button key={t} type="button" onClick={() => addTag(t)} className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs hover:bg-secondary/70">
                    + {t}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="cover">
              Cover image <span className="font-normal text-muted-foreground">(optional, https URL)</span>
            </Label>
            <div className="flex gap-2">
              <Input id="cover" placeholder="https://…" value={cover} onChange={(e) => touch(setCover)(e.target.value)} />
              <input ref={fileInput} type="file" accept="image/png,image/jpeg,image/webp,image/gif" className="hidden" onChange={(e) => e.target.files?.[0] && upload(e.target.files[0])} />
              <Button type="button" variant="outline" onClick={() => fileInput.current?.click()} disabled={uploading}>
                <ImagePlus className="h-4 w-4" />
                {uploading ? 'Uploading…' : 'Upload'}
              </Button>
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="body">Content (Markdown)</Label>
              <Button variant="ghost" size="sm" onClick={() => setCodeOpen(true)}>
                <Plus className="h-3.5 w-3.5" />
                Insert code
              </Button>
            </div>
            <Textarea id="body" value={body} onChange={(e) => touch(setBody)(e.target.value)} rows={20} className="font-mono text-sm leading-relaxed" />
          </div>
        </div>

        <div className={cn('flex flex-col gap-3', !showPreview && 'hidden lg:flex')}>
          <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
            <Eye className="h-4 w-4" />
            Live preview
          </div>
          <Card className="flex-1 overflow-hidden p-6">
            <h2 className={cn('text-balance text-2xl font-bold leading-tight', !title && 'text-muted-foreground')}>{title || 'Untitled article'}</h2>
            {tags.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-1.5">
                {tags.map((t) => (
                  <TagPill key={t} tag={t} asLink={false} />
                ))}
              </div>
            )}
            {excerpt && <p className="mt-4 text-pretty leading-relaxed text-muted-foreground">{excerpt}</p>}
            <div className="mt-2">
              <MarkdownPreview source={body} />
            </div>
          </Card>
        </div>
      </div>

      <Dialog open={codeOpen} onOpenChange={setCodeOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Insert code snippet</DialogTitle>
            <DialogDescription>Adds a fenced code block to the end of your article.</DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-3">
            <div className="flex flex-wrap gap-1.5">
              {LANGS.map((l) => (
                <button
                  key={l}
                  type="button"
                  onClick={() => setCodeLang(l)}
                  className={cn('rounded-md px-2.5 py-1 font-mono text-xs', codeLang === l ? 'bg-primary text-primary-foreground' : 'bg-secondary hover:bg-secondary/70')}
                >
                  {l}
                </button>
              ))}
            </div>
            <Textarea value={codeContent} onChange={(e) => setCodeContent(e.target.value)} rows={8} placeholder="// paste your code here" className="font-mono text-sm" />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCodeOpen(false)}>
              Cancel
            </Button>
            <Button onClick={insertCode}>Insert</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
