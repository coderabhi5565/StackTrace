/** @type {import('next').NextConfig} */
const nextConfig = {
  // Self-contained server bundle for the Docker image (ignored by Vercel).
  output: 'standalone',
  poweredByHeader: false,
  images: {
    // Remote images are restricted to known hosts so the optimizer is not an open proxy.
    remotePatterns: [
      { protocol: 'https', hostname: 'res.cloudinary.com' },
      { protocol: 'https', hostname: 'avatars.githubusercontent.com' },
    ],
  },
}

export default nextConfig
