import { NextResponse } from 'next/server'
import { cookies } from 'next/headers'
import { apiBaseUrl } from '@/lib/config'
import { ACCESS_COOKIE } from '@/lib/auth/cookies'
import { forwardedHeaders } from '@/lib/auth/backend'

/** Forwards a multipart image upload to post-service (Cloudinary). */
export async function POST(req: Request) {
  const token = (await cookies()).get(ACCESS_COOKIE)?.value
  if (!token) return NextResponse.json({ message: 'Please sign in' }, { status: 401 })
  const form = await req.formData()
  const file = form.get('file')
  if (!(file instanceof File)) return NextResponse.json({ message: 'No file' }, { status: 400 })
  if (file.size > 5 * 1024 * 1024) return NextResponse.json({ message: 'Image size must not exceed 5 MB.' }, { status: 400 })
  const out = new FormData()
  out.append('file', file, file.name)
  const res = await fetch(`${apiBaseUrl()}/api/uploads/images`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}`, ...forwardedHeaders(req) },
    body: out,
    cache: 'no-store',
  })
  return NextResponse.json(await res.json().catch(() => ({})), { status: res.status })
}
