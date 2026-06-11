"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Card } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { TagPill } from "@/components/tag-pill"
import { CodeBlock } from "@/components/code-block"
import { ArticleBody } from "@/components/article-body"
import { TAGS as allTags, difficultyLevels, type Difficulty } from "@/lib/mock-data"
import { toast } from "sonner"
import { Eye, FileText, Plus, Sparkles, X, HelpCircle, Trash2 } from "lucide-react"
import { cn } from "@/lib/utils"

type QuizDraft = {
  question: string
  options: string[]
  correctIndex: number
}

export function CreatePost() {
  const router = useRouter()
  const [title, setTitle] = useState("")
  const [excerpt, setExcerpt] = useState("")
  const [body, setBody] = useState(
    "## Introduction\n\nStart writing your article here. You can use markdown-style headings, paragraphs, and insert code snippets below.\n\n```js\nconsole.log('Hello, StackTrace!')\n```\n\nExplain what the code does and why it matters.",
  )
  const [selectedTags, setSelectedTags] = useState<string[]>([])
  const [difficulty, setDifficulty] = useState<Difficulty>("Beginner")
  const [codeOpen, setCodeOpen] = useState(false)
  const [quizOpen, setQuizOpen] = useState(false)
  const [publishOpen, setPublishOpen] = useState(false)

  // code modal state
  const [codeLang, setCodeLang] = useState("js")
  const [codeContent, setCodeContent] = useState("")

  // quiz state
  const [quiz, setQuiz] = useState<QuizDraft | null>(null)
  const [draftQuiz, setDraftQuiz] = useState<QuizDraft>({
    question: "",
    options: ["", "", "", ""],
    correctIndex: 0,
  })

  function toggleTag(tag: string) {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : prev.length >= 4 ? prev : [...prev, tag],
    )
  }

  function insertCode() {
    if (!codeContent.trim()) {
      toast.error("Add some code first")
      return
    }
    setBody((prev) => `${prev}\n\n\`\`\`${codeLang}\n${codeContent}\n\`\`\`\n`)
    setCodeContent("")
    setCodeOpen(false)
    toast.success("Code snippet inserted")
  }

  function saveQuiz() {
    if (!draftQuiz.question.trim() || draftQuiz.options.some((o) => !o.trim())) {
      toast.error("Complete the question and all options")
      return
    }
    setQuiz(draftQuiz)
    setQuizOpen(false)
    toast.success("Quiz attached to article")
  }

  function handlePublish() {
    if (!title.trim() || !excerpt.trim() || selectedTags.length === 0) {
      toast.error("Add a title, excerpt, and at least one tag")
      setPublishOpen(false)
      return
    }
    setPublishOpen(false)
    toast.success("Article published! Redirecting to your feed...")
    setTimeout(() => router.push("/home"), 1200)
  }

  // Parse body into preview blocks
  const previewBlocks = parseBody(body)

  return (
    <div className="mx-auto w-full max-w-7xl px-4 py-6 lg:px-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-mono text-2xl font-bold tracking-tight">Write an article</h1>
          <p className="text-sm text-muted-foreground">Share knowledge with the developer community.</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => toast("Draft saved locally")}>
            Save draft
          </Button>
          <Button onClick={() => setPublishOpen(true)}>
            <Sparkles className="mr-1.5 h-4 w-4" />
            Publish
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Editor */}
        <div className="flex flex-col gap-5">
          <div className="flex flex-col gap-2">
            <Label htmlFor="title">Title</Label>
            <Input
              id="title"
              placeholder="A descriptive, searchable title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="text-base"
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="excerpt">Excerpt</Label>
            <Textarea
              id="excerpt"
              placeholder="A one or two sentence summary that appears on cards."
              value={excerpt}
              onChange={(e) => setExcerpt(e.target.value)}
              rows={2}
            />
          </div>

          <div className="grid gap-2 sm:grid-cols-2">
            <div className="flex flex-col gap-2">
              <Label>Difficulty</Label>
              <Select value={difficulty} onValueChange={(v) => setDifficulty(v as Difficulty)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {difficultyLevels.map((d) => (
                    <SelectItem key={d} value={d}>
                      {d}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex flex-col gap-2">
              <Label>Quiz</Label>
              <Button variant="outline" className="justify-start bg-transparent" onClick={() => setQuizOpen(true)}>
                <HelpCircle className="mr-1.5 h-4 w-4" />
                {quiz ? "Edit quiz" : "Add a quiz"}
              </Button>
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <Label>Tags ({selectedTags.length}/4)</Label>
            <div className="flex flex-wrap gap-1.5 rounded-lg border border-border bg-card p-3">
              {allTags.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  onClick={() => toggleTag(tag)}
                  className={cn(
                    "rounded-md px-2.5 py-1 font-mono text-xs transition-colors",
                    selectedTags.includes(tag)
                      ? "bg-primary text-primary-foreground"
                      : "bg-secondary text-secondary-foreground hover:bg-secondary/70",
                  )}
                >
                  {tag}
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="body">Content</Label>
              <Button variant="ghost" size="sm" onClick={() => setCodeOpen(true)}>
                <Plus className="mr-1 h-3.5 w-3.5" />
                Insert code
              </Button>
            </div>
            <Textarea
              id="body"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              rows={18}
              className="font-mono text-sm leading-relaxed"
            />
          </div>
        </div>

        {/* Live preview */}
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
            <Eye className="h-4 w-4" />
            Live preview
          </div>
          <Card className="flex-1 overflow-hidden p-6">
            {title ? (
              <h2 className="text-balance font-mono text-2xl font-bold leading-tight">{title}</h2>
            ) : (
              <h2 className="font-mono text-2xl font-bold text-muted-foreground">Untitled article</h2>
            )}
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <span className="rounded-md bg-accent/15 px-2 py-0.5 font-mono text-xs text-accent">{difficulty}</span>
              {selectedTags.map((t) => (
                <TagPill key={t} tag={t} />
              ))}
            </div>
            {excerpt && <p className="mt-4 text-pretty leading-relaxed text-muted-foreground">{excerpt}</p>}
            <div className="mt-6">
              <ArticleBody blocks={previewBlocks} />
            </div>
            {quiz && (
              <div className="mt-6 rounded-lg border border-accent/30 bg-accent/5 p-4">
                <div className="flex items-center gap-2 text-sm font-semibold text-accent">
                  <HelpCircle className="h-4 w-4" />
                  Quiz attached
                </div>
                <p className="mt-1 text-sm text-foreground">{quiz.question}</p>
              </div>
            )}
          </Card>
        </div>
      </div>

      {/* Insert code modal */}
      <Dialog open={codeOpen} onOpenChange={setCodeOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Insert code snippet</DialogTitle>
            <DialogDescription>Add a runnable code block to your article.</DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-2">
              <Label>Language</Label>
              <Select value={codeLang} onValueChange={setCodeLang}>
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="js">JavaScript</SelectItem>
                  <SelectItem value="ts">TypeScript</SelectItem>
                  <SelectItem value="py">Python</SelectItem>
                  <SelectItem value="bash">Bash</SelectItem>
                  <SelectItem value="json">JSON</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Textarea
              value={codeContent}
              onChange={(e) => setCodeContent(e.target.value)}
              rows={8}
              placeholder="// paste your code here"
              className="font-mono text-sm"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCodeOpen(false)}>
              Cancel
            </Button>
            <Button onClick={insertCode}>Insert</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Quiz modal */}
      <Dialog open={quizOpen} onOpenChange={setQuizOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add a knowledge check</DialogTitle>
            <DialogDescription>Help readers test what they learned.</DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-3">
            <div className="flex flex-col gap-2">
              <Label>Question</Label>
              <Input
                value={draftQuiz.question}
                onChange={(e) => setDraftQuiz({ ...draftQuiz, question: e.target.value })}
                placeholder="What does the code above return?"
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label>Options (select the correct one)</Label>
              {draftQuiz.options.map((opt, i) => (
                <div key={i} className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setDraftQuiz({ ...draftQuiz, correctIndex: i })}
                    className={cn(
                      "flex h-6 w-6 shrink-0 items-center justify-center rounded-full border text-xs font-bold transition-colors",
                      draftQuiz.correctIndex === i
                        ? "border-primary bg-primary text-primary-foreground"
                        : "border-border text-muted-foreground",
                    )}
                    aria-label={`Mark option ${i + 1} correct`}
                  >
                    {String.fromCharCode(65 + i)}
                  </button>
                  <Input
                    value={opt}
                    onChange={(e) => {
                      const next = [...draftQuiz.options]
                      next[i] = e.target.value
                      setDraftQuiz({ ...draftQuiz, options: next })
                    }}
                    placeholder={`Option ${i + 1}`}
                  />
                </div>
              ))}
            </div>
          </div>
          <DialogFooter className="sm:justify-between">
            {quiz ? (
              <Button
                variant="ghost"
                className="text-destructive hover:text-destructive"
                onClick={() => {
                  setQuiz(null)
                  setQuizOpen(false)
                  toast("Quiz removed")
                }}
              >
                <Trash2 className="mr-1.5 h-4 w-4" />
                Remove
              </Button>
            ) : (
              <span />
            )}
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => setQuizOpen(false)}>
                Cancel
              </Button>
              <Button onClick={saveQuiz}>Save quiz</Button>
            </div>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Publish modal */}
      <Dialog open={publishOpen} onOpenChange={setPublishOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Publish article</DialogTitle>
            <DialogDescription>Review the details before going live.</DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-3 text-sm">
            <SummaryRow icon={<FileText className="h-4 w-4" />} label="Title" value={title || "—"} />
            <SummaryRow
              icon={<X className="h-4 w-4 rotate-45" />}
              label="Difficulty"
              value={difficulty}
            />
            <div className="flex items-start gap-2">
              <span className="mt-0.5 text-muted-foreground">
                <Plus className="h-4 w-4" />
              </span>
              <div>
                <div className="text-muted-foreground">Tags</div>
                <div className="mt-1 flex flex-wrap gap-1.5">
                  {selectedTags.length ? (
                    selectedTags.map((t) => <TagPill key={t} tag={t} />)
                  ) : (
                    <span className="text-foreground">—</span>
                  )}
                </div>
              </div>
            </div>
            <SummaryRow
              icon={<HelpCircle className="h-4 w-4" />}
              label="Quiz"
              value={quiz ? "Attached" : "None"}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setPublishOpen(false)}>
              Keep editing
            </Button>
            <Button onClick={handlePublish}>
              <Sparkles className="mr-1.5 h-4 w-4" />
              Publish now
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

function SummaryRow({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-start gap-2">
      <span className="mt-0.5 text-muted-foreground">{icon}</span>
      <div>
        <div className="text-muted-foreground">{label}</div>
        <div className="text-foreground">{value}</div>
      </div>
    </div>
  )
}

// Lightweight markdown-ish parser shared shape with ArticleBody
export type Block =
  | { type: "heading"; text: string }
  | { type: "paragraph"; text: string }
  | { type: "code"; lang: string; code: string }

function parseBody(raw: string): Block[] {
  const blocks: Block[] = []
  const lines = raw.split("\n")
  let i = 0
  let paragraph: string[] = []

  function flushParagraph() {
    if (paragraph.length) {
      blocks.push({ type: "paragraph", text: paragraph.join(" ").trim() })
      paragraph = []
    }
  }

  while (i < lines.length) {
    const line = lines[i]
    if (line.startsWith("```")) {
      flushParagraph()
      const lang = line.slice(3).trim() || "text"
      const code: string[] = []
      i++
      while (i < lines.length && !lines[i].startsWith("```")) {
        code.push(lines[i])
        i++
      }
      blocks.push({ type: "code", lang, code: code.join("\n") })
      i++
      continue
    }
    if (line.startsWith("#")) {
      flushParagraph()
      blocks.push({ type: "heading", text: line.replace(/^#+\s*/, "") })
      i++
      continue
    }
    if (line.trim() === "") {
      flushParagraph()
      i++
      continue
    }
    paragraph.push(line)
    i++
  }
  flushParagraph()
  return blocks
}
