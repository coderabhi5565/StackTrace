'use client'

import { X } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'
import { FollowButton } from '@/components/post/follow-button'
import { Button } from '@/components/ui/button'
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
import { Textarea } from '@/components/ui/textarea'
import { api, errorMessage } from '@/lib/api/client'
import type { Profile, Skill } from '@/lib/api/types'
import { useSession } from '@/lib/auth/session-client'

export function ProfileActions({ profile }: { profile: Profile }) {
  const session = useSession()
  const router = useRouter()
  const [open, setOpen] = useState(false)

  if (session === undefined) return <div className="h-8 w-24" />
  if (session?.id !== profile.id) {
    return <FollowButton userId={profile.id} username={profile.username} onChange={() => router.refresh()} />
  }

  return (
    <>
      <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
        Edit profile
      </Button>
      {open && <EditProfileDialog profile={profile} onClose={() => setOpen(false)} />}
    </>
  )
}

function EditProfileDialog({ profile, onClose }: { profile: Profile; onClose: () => void }) {
  const router = useRouter()
  const [name, setName] = useState(profile.name)
  const [bio, setBio] = useState(profile.bio ?? '')
  const [location, setLocation] = useState(profile.location ?? '')
  const [avatarUrl, setAvatarUrl] = useState(profile.avatarUrl ?? '')
  const [skills, setSkills] = useState<Skill[]>(profile.skills)
  const [skillInput, setSkillInput] = useState('')
  const [busy, setBusy] = useState(false)

  async function addSkill() {
    const skillName = skillInput.trim()
    if (!skillName) return
    try {
      const s = await api<Skill>('/users/me/skills', { method: 'POST', body: { skillName } })
      setSkills((prev) => [...prev, s])
      setSkillInput('')
    } catch (e) {
      toast.error(errorMessage(e))
    }
  }

  async function removeSkill(id: number) {
    try {
      await api(`/users/me/skills/${id}`, { method: 'DELETE' })
      setSkills((prev) => prev.filter((s) => s.id !== id))
    } catch (e) {
      toast.error(errorMessage(e))
    }
  }

  async function save() {
    setBusy(true)
    try {
      await api('/users/me/profile', { method: 'PUT', body: { name, bio, location, avatarUrl } })
      toast.success('Profile updated')
      onClose()
      router.refresh()
    } catch (e) {
      toast.error(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && (onClose(), router.refresh())}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit profile</DialogTitle>
          <DialogDescription>Your public developer profile.</DialogDescription>
        </DialogHeader>
        <div className="flex flex-col gap-3">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="p-name">Name</Label>
            <Input id="p-name" value={name} maxLength={100} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="p-bio">Bio</Label>
            <Textarea id="p-bio" value={bio} maxLength={500} rows={3} onChange={(e) => setBio(e.target.value)} />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="p-loc">Location</Label>
              <Input id="p-loc" value={location} maxLength={100} onChange={(e) => setLocation(e.target.value)} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="p-avatar">Avatar URL (https)</Label>
              <Input id="p-avatar" value={avatarUrl} placeholder="https://…" onChange={(e) => setAvatarUrl(e.target.value)} />
            </div>
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="p-skill">Skills</Label>
            <div className="flex flex-wrap gap-1.5">
              {skills.map((s) => (
                <span key={s.id} className="inline-flex items-center gap-1 rounded-md bg-secondary px-2 py-0.5 text-xs">
                  {s.skillName}
                  <button type="button" aria-label={`Remove ${s.skillName}`} onClick={() => removeSkill(s.id)}>
                    <X className="size-3" />
                  </button>
                </span>
              ))}
            </div>
            <div className="flex gap-2">
              <Input
                id="p-skill"
                value={skillInput}
                maxLength={100}
                placeholder="e.g. Spring Boot"
                onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault()
                    addSkill()
                  }
                }}
              />
              <Button type="button" variant="outline" onClick={addSkill}>
                Add
              </Button>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={save} disabled={busy || !name.trim()}>
            {busy ? 'Saving…' : 'Save'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
