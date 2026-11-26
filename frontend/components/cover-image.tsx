import Image from 'next/image'

// Must match images.remotePatterns in next.config.mjs.
const OPTIMIZED_HOSTS = new Set(['res.cloudinary.com', 'avatars.githubusercontent.com'])

/** Uses next/image (resized, modern formats, lazy) for known hosts; plain <img> otherwise. */
export function CoverImage({ src, priority = false }: { src: string; priority?: boolean }) {
  let host = ''
  try {
    host = new URL(src).hostname
  } catch {
    return null
  }
  const className = 'mt-6 aspect-[2/1] w-full rounded-xl object-cover'
  if (OPTIMIZED_HOSTS.has(host)) {
    return (
      <Image src={src} alt="" width={1200} height={600} priority={priority} sizes="(max-width: 768px) 100vw, 768px" className={className} />
    )
  }
  // eslint-disable-next-line @next/next/no-img-element -- arbitrary user-supplied host, not proxied through the optimizer
  return <img src={src} alt="" loading={priority ? 'eager' : 'lazy'} className={className} />
}
