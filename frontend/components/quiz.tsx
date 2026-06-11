'use client'

import { CheckCircle2, XCircle } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import type { QuizQuestion } from '@/lib/mock-data'
import { cn } from '@/lib/utils'

export function Quiz({ questions }: { questions: QuizQuestion[] }) {
  const [answers, setAnswers] = useState<Record<string, number>>({})
  const [submitted, setSubmitted] = useState(false)

  if (questions.length === 0) return null

  const score = questions.filter((q) => answers[q.id] === q.correct).length
  const points = score * 10

  function submit() {
    if (Object.keys(answers).length < questions.length) {
      toast.error('Answer all questions first')
      return
    }
    setSubmitted(true)
    const s = questions.filter((q) => answers[q.id] === q.correct).length
    toast.success(`You earned ${s * 10} points!`)
  }

  return (
    <section className="mt-10 rounded-2xl border border-border bg-card p-6">
      <h2 className="text-xl font-bold tracking-tight">Test your knowledge</h2>
      <p className="mt-1 text-sm text-muted-foreground">
        Answer 3 questions to earn points.
      </p>

      <div className="mt-6 flex flex-col gap-6">
        {questions.map((q, qi) => (
          <div key={q.id}>
            <p className="font-medium">
              {qi + 1}. {q.question}
            </p>
            <div className="mt-3 grid gap-2">
              {q.options.map((opt, oi) => {
                const selected = answers[q.id] === oi
                const isCorrect = q.correct === oi
                const showState = submitted && (selected || isCorrect)
                return (
                  <label
                    key={oi}
                    className={cn(
                      'flex cursor-pointer items-center gap-3 rounded-lg border px-3 py-2.5 text-sm transition-colors',
                      !submitted && selected && 'border-primary bg-primary/10',
                      !submitted && !selected && 'border-border hover:bg-secondary',
                      showState && isCorrect && 'border-primary bg-primary/10',
                      showState && selected && !isCorrect && 'border-destructive bg-destructive/10',
                      submitted && !showState && 'border-border opacity-60',
                    )}
                  >
                    <input
                      type="radio"
                      name={q.id}
                      className="sr-only"
                      disabled={submitted}
                      checked={selected}
                      onChange={() => setAnswers((a) => ({ ...a, [q.id]: oi }))}
                    />
                    <span
                      className={cn(
                        'flex size-5 shrink-0 items-center justify-center rounded-full border text-xs',
                        selected ? 'border-primary' : 'border-muted-foreground/40',
                      )}
                    >
                      {String.fromCharCode(65 + oi)}
                    </span>
                    <span className="flex-1">{opt}</span>
                    {showState && isCorrect && (
                      <CheckCircle2 className="size-4 text-primary" />
                    )}
                    {showState && selected && !isCorrect && (
                      <XCircle className="size-4 text-destructive" />
                    )}
                  </label>
                )
              })}
            </div>
          </div>
        ))}
      </div>

      {!submitted ? (
        <Button onClick={submit} className="mt-6 font-medium">
          Submit Quiz
        </Button>
      ) : (
        <div className="mt-6 rounded-xl border border-primary/30 bg-primary/10 p-4 text-center">
          <p className="text-lg font-bold">
            You scored {score} / {questions.length}
          </p>
          <p className="mt-1 text-primary">You earned {points} points!</p>
        </div>
      )}
    </section>
  )
}
