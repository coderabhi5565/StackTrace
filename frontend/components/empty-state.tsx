export function EmptyState({ title, hint, children }: { title: string; hint?: string; children?: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed border-border py-14 text-center">
      <p className="text-sm font-medium">{title}</p>
      {hint && <p className="mt-1 text-sm text-muted-foreground">{hint}</p>}
      {children && <div className="mt-4 flex justify-center">{children}</div>}
    </div>
  )
}

export function ErrorState({ message = "We couldn't load this right now." }: { message?: string }) {
  return (
    <div className="rounded-xl border border-destructive/40 bg-destructive/5 py-10 text-center">
      <p className="text-sm font-medium text-destructive">{message}</p>
      <p className="mt-1 text-sm text-muted-foreground">Please try again in a moment.</p>
    </div>
  )
}
