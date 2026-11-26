// Shapes returned by the StackTrace backend (through the API gateway).

export type UserSummary = {
  id: number
  name: string
  username: string
  avatarUrl: string | null
}

export type PostStatus = 'DRAFT' | 'SCHEDULED' | 'PUBLISHED'

export type PostSummary = {
  id: number
  title: string
  slug: string
  excerpt: string | null
  coverImageUrl: string | null
  author: UserSummary
  status: PostStatus
  tags: string[]
  readingTime: number
  likeCount: number
  commentCount: number
  scheduledAt: string | null
  publishedAt: string | null
  createdAt: string
}

export type Post = PostSummary & {
  content: string
  authorId: number
  updatedAt: string
}

export type Page<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export type Engagement = {
  postId: number
  likeCount: number
  commentCount: number
  liked: boolean
  bookmarked: boolean
}

export type Tag = { name: string; postCount: number }

export type Comment = {
  id: number
  content: string | null
  authorId: number | null
  author: UserSummary | null
  postId: number
  parentCommentId: number | null
  deleted: boolean
  edited: boolean
  createdAt: string
  updatedAt: string
  replies: Comment[]
}

export type Skill = { id: number; skillName: string }

export type Badge = {
  id: number
  name: string
  description: string
  icon: string | null
  earnedAt: string
}

export type Profile = {
  id: number
  name: string
  username: string
  bio: string | null
  avatarUrl: string | null
  location: string | null
  points: number
  rank: number
  followersCount: number
  followingCount: number
  joinedAt: string | null
  skills: Skill[]
  badges: Badge[]
}

export type LeaderboardEntry = {
  rank: number
  id: number
  username: string
  name: string
  avatarUrl: string | null
  points: number
}

export type SearchResult = {
  posts: Page<PostSummary>
  users: Page<UserSummary>
  degraded: string[]
}

export type NotificationType = 'FOLLOW' | 'COMMENT' | 'REPLY'

export type AppNotification = {
  id: number
  senderId: number
  sender: UserSummary | null
  type: NotificationType
  message: string
  referenceId: number | null
  isRead: boolean
  createdAt: string
}

export type LoginResponse = {
  id: number
  token: string
  refreshToken: string
  expiresIn: number
  name: string
  username: string
  email: string
}

export type SessionUser = { id: number; username: string; name: string }

export type ApiErrorBody = {
  status: number
  error?: string
  message?: string
  path?: string
  validationErrors?: Record<string, string>
}
