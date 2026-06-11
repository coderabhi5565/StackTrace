export type Author = {
  id: string
  name: string
  username: string
  avatar: string
  bio: string
  github?: string
  linkedin?: string
  x?: string
  portfolio?: string
  followers: number
  following: number
  points: number
  rank: number
  articles: number
  totalReads: number
  location: string
  joined: string
  skills: string[]
}

export type CodeBlock = {
  language: string
  code: string
  output?: string
}

export type QuizQuestion = {
  id: string
  question: string
  options: string[]
  correct: number
}

export type Article = {
  id: string
  slug: string
  title: string
  description: string
  author: Author
  tags: string[]
  readingTime: number
  views: number
  likes: number
  postedAt: string
  hasQuiz: boolean
  outdatedFlags: number
  bookmarked: boolean
  content: ArticleSection[]
  quiz: QuizQuestion[]
}

export type ArticleSection =
  | { type: 'heading'; text: string }
  | { type: 'paragraph'; text: string }
  | { type: 'code'; block: CodeBlock }
  | { type: 'list'; items: string[] }

export type Difficulty = "Beginner" | "Intermediate" | "Advanced"
export const difficultyLevels: Difficulty[] = ["Beginner", "Intermediate", "Advanced"]

export const TAGS = [
  'java',
  'python',
  'dsa',
  'springboot',
  'react',
  'docker',
  'systemdesign',
  'cpp',
  'typescript',
  'kubernetes',
  'rust',
  'go',
]

export const TAG_COLORS: Record<string, string> = {
  java: 'text-orange-400 border-orange-400/30 bg-orange-400/10',
  python: 'text-blue-400 border-blue-400/30 bg-blue-400/10',
  dsa: 'text-pink-400 border-pink-400/30 bg-pink-400/10',
  springboot: 'text-green-400 border-green-400/30 bg-green-400/10',
  react: 'text-cyan-400 border-cyan-400/30 bg-cyan-400/10',
  docker: 'text-sky-400 border-sky-400/30 bg-sky-400/10',
  systemdesign: 'text-amber-400 border-amber-400/30 bg-amber-400/10',
  cpp: 'text-indigo-400 border-indigo-400/30 bg-indigo-400/10',
  typescript: 'text-blue-500 border-blue-500/30 bg-blue-500/10',
  kubernetes: 'text-sky-500 border-sky-500/30 bg-sky-500/10',
  rust: 'text-red-400 border-red-400/30 bg-red-400/10',
  go: 'text-teal-400 border-teal-400/30 bg-teal-400/10',
}

export const authors: Author[] = [
  {
    id: 'a1',
    name: 'Aarav Mehta',
    username: 'aaravcodes',
    avatar: '/avatars/dev-1.png',
    bio: 'Backend engineer obsessed with distributed systems and clean abstractions. I write so I learn twice.',
    github: 'aaravcodes',
    linkedin: 'aaravmehta',
    x: 'aaravcodes',
    portfolio: 'aarav.dev',
    followers: 12400,
    following: 312,
    points: 4820,
    rank: 1,
    articles: 48,
    totalReads: 184000,
    location: 'Bengaluru, India',
    joined: 'March 2021',
    skills: ['Java', 'Spring Boot', 'Kafka', 'PostgreSQL', 'System Design'],
  },
  {
    id: 'a2',
    name: 'Lena Fischer',
    username: 'lenabuilds',
    avatar: '/avatars/dev-2.png',
    bio: 'Frontend architect. React, performance, and design systems. Ex-big-tech, now indie.',
    github: 'lenabuilds',
    linkedin: 'lenafischer',
    x: 'lenabuilds',
    followers: 9800,
    following: 188,
    points: 4310,
    rank: 2,
    articles: 39,
    totalReads: 142000,
    location: 'Berlin, Germany',
    joined: 'July 2021',
    skills: ['React', 'TypeScript', 'CSS', 'Accessibility', 'Vite'],
  },
  {
    id: 'a3',
    name: 'Diego Santos',
    username: 'dsantos',
    avatar: '/avatars/dev-3.png',
    bio: 'Platform & infra. I make Kubernetes behave so you don\u2019t have to.',
    github: 'dsantos',
    linkedin: 'diegosantos',
    followers: 7600,
    following: 95,
    points: 3990,
    rank: 3,
    articles: 31,
    totalReads: 98000,
    location: 'S\u00e3o Paulo, Brazil',
    joined: 'Jan 2022',
    skills: ['Docker', 'Kubernetes', 'Go', 'Terraform', 'AWS'],
  },
  {
    id: 'a4',
    name: 'Priya Nair',
    username: 'priyacodes',
    avatar: '/avatars/dev-4.png',
    bio: 'Algorithms nerd. DSA mentor. Turning hard problems into intuitive pictures.',
    github: 'priyacodes',
    x: 'priyacodes',
    followers: 15200,
    following: 240,
    points: 3870,
    rank: 4,
    articles: 56,
    totalReads: 211000,
    location: 'Remote',
    joined: 'Nov 2020',
    skills: ['DSA', 'C++', 'Python', 'Competitive Programming'],
  },
  {
    id: 'a5',
    name: 'Marcus Lee',
    username: 'marcusdev',
    avatar: '/avatars/dev-5.png',
    bio: 'Rustacean. Systems programming and writing about memory like it owes me money.',
    github: 'marcusdev',
    linkedin: 'marcuslee',
    followers: 5400,
    following: 130,
    points: 3540,
    rank: 5,
    articles: 22,
    totalReads: 67000,
    location: 'Singapore',
    joined: 'May 2022',
    skills: ['Rust', 'C++', 'WebAssembly', 'Performance'],
  },
  {
    id: 'a6',
    name: 'Sofia Rossi',
    username: 'sofiar',
    avatar: '/avatars/dev-6.png',
    bio: 'Full-stack dev & DevRel. Python by day, TypeScript by night.',
    github: 'sofiar',
    x: 'sofiar',
    followers: 8900,
    following: 410,
    points: 3320,
    rank: 6,
    articles: 44,
    totalReads: 121000,
    location: 'Milan, Italy',
    joined: 'Aug 2021',
    skills: ['Python', 'TypeScript', 'FastAPI', 'React'],
  },
]

export const currentUser: Author = {
  id: 'me',
  name: 'Jordan Park',
  username: 'jordandev',
  avatar: '/avatars/dev-7.png',
  bio: 'Software engineer learning in public. Java, Spring Boot & a growing love for distributed systems.',
  github: 'jordandev',
  linkedin: 'jordanpark',
  x: 'jordandev',
  portfolio: 'jordan.codes',
  followers: 842,
  following: 196,
  points: 1240,
  rank: 47,
  articles: 7,
  totalReads: 18400,
  location: 'Seattle, USA',
  joined: 'Feb 2023',
  skills: ['Java', 'Spring Boot', 'DSA', 'React', 'Docker'],
}

function author(id: string): Author {
  return authors.find((a) => a.id === id) ?? authors[0]
}

export const articles: Article[] = [
  {
    id: 'p1',
    slug: 'mastering-spring-boot-dependency-injection',
    title: 'Mastering Dependency Injection in Spring Boot',
    description:
      'A deep dive into how Spring\u2019s IoC container wires your beans, and the patterns that keep large services maintainable.',
    author: author('a1'),
    tags: ['java', 'springboot', 'systemdesign'],
    readingTime: 8,
    views: 24300,
    likes: 1820,
    postedAt: '2 hours ago',
    hasQuiz: true,
    outdatedFlags: 0,
    bookmarked: false,
    content: [
      {
        type: 'paragraph',
        text: 'Dependency injection is the backbone of every Spring Boot application. Instead of constructing collaborators yourself, you let the IoC container hand them to you. This inversion of control is what makes Spring apps so testable and modular.',
      },
      { type: 'heading', text: 'Constructor injection over field injection' },
      {
        type: 'paragraph',
        text: 'Prefer constructor injection. It makes dependencies explicit, supports immutability with final fields, and plays nicely with unit tests where you simply pass mocks into the constructor.',
      },
      {
        type: 'code',
        block: {
          language: 'java',
          code: 'import org.springframework.stereotype.Service;\n\n@Service\npublic class OrderService {\n    private final PaymentClient payment;\n    private final OrderRepository repo;\n\n    public OrderService(PaymentClient payment, OrderRepository repo) {\n        this.payment = payment;\n        this.repo = repo;\n    }\n\n    public String checkout(long orderId) {\n        return "Order " + orderId + " charged via " + payment.provider();\n    }\n}',
          output: 'Order 42 charged via stripe',
        },
      },
      { type: 'heading', text: 'When to reach for @Qualifier' },
      {
        type: 'paragraph',
        text: 'When more than one bean satisfies a type, Spring needs a hint. Use @Qualifier to disambiguate, or mark one bean as @Primary so it wins by default.',
      },
      {
        type: 'list',
        items: [
          'Use constructor injection for required dependencies.',
          'Reserve @Autowired field injection for quick prototypes only.',
          'Mark exactly one candidate @Primary to avoid surprises.',
        ],
      },
    ],
    quiz: [
      {
        id: 'q1',
        question: 'Which injection style is recommended for required dependencies?',
        options: ['Field injection', 'Constructor injection', 'Setter injection', 'Static injection'],
        correct: 1,
      },
      {
        id: 'q2',
        question: 'What does @Primary do?',
        options: [
          'Marks a bean as a singleton',
          'Picks a default bean when several match a type',
          'Disables a bean',
          'Runs a bean first at startup',
        ],
        correct: 1,
      },
      {
        id: 'q3',
        question: 'Constructor injection enables which property on fields?',
        options: ['volatile', 'transient', 'final / immutability', 'synchronized'],
        correct: 2,
      },
    ],
  },
  {
    id: 'p2',
    slug: 'react-server-components-mental-model',
    title: 'A Practical Mental Model for React Server Components',
    description:
      'Stop guessing where your code runs. Here is the simple model I use to reason about server vs client boundaries.',
    author: author('a2'),
    tags: ['react', 'typescript'],
    readingTime: 6,
    views: 18700,
    likes: 1340,
    postedAt: '5 hours ago',
    hasQuiz: true,
    outdatedFlags: 0,
    bookmarked: true,
    content: [
      {
        type: 'paragraph',
        text: 'Server Components run on the server and never ship to the browser. Client Components are interactive and hydrate on the client. The boundary is the "use client" directive.',
      },
      {
        type: 'code',
        block: {
          language: 'js',
          code: 'function Counter() {\n  // "use client" at top of file makes this interactive\n  const [n, setN] = React.useState(0)\n  return <button onClick={() => setN(n + 1)}>{n}</button>\n}\n\nconsole.log("rendered on client")',
          output: 'rendered on client',
        },
      },
    ],
    quiz: [
      {
        id: 'q1',
        question: 'Where do Server Components execute?',
        options: ['Only the browser', 'Only the server', 'Both', 'A web worker'],
        correct: 1,
      },
      {
        id: 'q2',
        question: 'Which directive marks a Client Component?',
        options: ['"use server"', '"use client"', '"client only"', '"hydrate"'],
        correct: 1,
      },
      {
        id: 'q3',
        question: 'Can a Server Component use useState?',
        options: ['Yes', 'No', 'Only with a flag', 'Only in dev'],
        correct: 1,
      },
    ],
  },
  {
    id: 'p3',
    slug: 'binary-search-the-template-you-never-forget',
    title: 'Binary Search: The Template You\u2019ll Never Forget',
    description:
      'Off-by-one errors haunt binary search. Here is one boundary-safe template that handles every variant.',
    author: author('a4'),
    tags: ['dsa', 'cpp', 'python'],
    readingTime: 7,
    views: 31200,
    likes: 2410,
    postedAt: '1 day ago',
    hasQuiz: true,
    outdatedFlags: 3,
    bookmarked: false,
    content: [
      {
        type: 'paragraph',
        text: 'The trick to never breaking binary search is to fix your invariant: the answer always lives in [lo, hi]. Shrink the search space while keeping that true.',
      },
      {
        type: 'code',
        block: {
          language: 'python',
          code: 'def lower_bound(arr, target):\n    lo, hi = 0, len(arr)\n    while lo < hi:\n        mid = (lo + hi) // 2\n        if arr[mid] < target:\n            lo = mid + 1\n        else:\n            hi = mid\n    return lo\n\nprint(lower_bound([1, 3, 3, 5, 7], 3))',
          output: '1',
        },
      },
    ],
    quiz: [
      {
        id: 'q1',
        question: 'What is the time complexity of binary search?',
        options: ['O(n)', 'O(log n)', 'O(n log n)', 'O(1)'],
        correct: 1,
      },
      {
        id: 'q2',
        question: 'A prerequisite for binary search is that the array is...',
        options: ['Hashed', 'Sorted', 'Reversed', 'Unique'],
        correct: 1,
      },
      {
        id: 'q3',
        question: 'lower_bound returns the index of...',
        options: [
          'The last element',
          'The first element >= target',
          'The largest element',
          'A random match',
        ],
        correct: 1,
      },
    ],
  },
  {
    id: 'p4',
    slug: 'docker-multi-stage-builds-that-shrink-images',
    title: 'Docker Multi-Stage Builds That Actually Shrink Your Images',
    description:
      'Cut your image size by 80% with multi-stage builds. A before/after walkthrough with a Go binary.',
    author: author('a3'),
    tags: ['docker', 'go', 'kubernetes'],
    readingTime: 5,
    views: 14900,
    likes: 980,
    postedAt: '2 days ago',
    hasQuiz: false,
    outdatedFlags: 0,
    bookmarked: false,
    content: [
      {
        type: 'paragraph',
        text: 'A multi-stage build lets you compile in a fat image and copy only the artifact into a tiny runtime image. The result: smaller, faster, more secure containers.',
      },
      {
        type: 'code',
        block: {
          language: 'docker',
          code: 'FROM golang:1.22 AS build\nWORKDIR /src\nCOPY . .\nRUN go build -o /app\n\nFROM gcr.io/distroless/base\nCOPY --from=build /app /app\nENTRYPOINT ["/app"]',
          output: 'Successfully built image: 12.4MB (was 980MB)',
        },
      },
    ],
    quiz: [],
  },
  {
    id: 'p5',
    slug: 'rust-ownership-explained-with-coffee',
    title: 'Rust Ownership Explained With a Cup of Coffee',
    description:
      'Borrow checker got you down? This everyday analogy makes move semantics finally click.',
    author: author('a5'),
    tags: ['rust', 'cpp'],
    readingTime: 9,
    views: 22100,
    likes: 1670,
    postedAt: '3 days ago',
    hasQuiz: true,
    outdatedFlags: 0,
    bookmarked: true,
    content: [
      {
        type: 'paragraph',
        text: 'Ownership means each value has exactly one owner. When the owner goes out of scope, the value is dropped. Think of a coffee cup: you can lend it (borrow) but only one person drinks from it at a time.',
      },
      {
        type: 'code',
        block: {
          language: 'rust',
          code: 'fn main() {\n    let cup = String::from("espresso");\n    let sip = &cup; // borrow\n    println!("sipping {}", sip);\n    println!("owner still has {}", cup);\n}',
          output: 'sipping espresso\nowner still has espresso',
        },
      },
    ],
    quiz: [
      {
        id: 'q1',
        question: 'How many owners can a Rust value have at once?',
        options: ['Zero', 'Exactly one', 'Two', 'Unlimited'],
        correct: 1,
      },
      {
        id: 'q2',
        question: 'What happens when an owner goes out of scope?',
        options: ['Nothing', 'The value is dropped', 'It is cloned', 'It leaks'],
        correct: 1,
      },
      {
        id: 'q3',
        question: 'A shared reference is created with which symbol?',
        options: ['*', '&', '#', '@'],
        correct: 1,
      },
    ],
  },
  {
    id: 'p6',
    slug: 'designing-a-rate-limiter-from-scratch',
    title: 'Designing a Rate Limiter From Scratch',
    description:
      'Token bucket vs sliding window vs leaky bucket. Which one should power your API gateway?',
    author: author('a1'),
    tags: ['systemdesign', 'java', 'springboot'],
    readingTime: 11,
    views: 27800,
    likes: 2090,
    postedAt: '4 days ago',
    hasQuiz: true,
    outdatedFlags: 0,
    bookmarked: false,
    content: [
      {
        type: 'paragraph',
        text: 'Rate limiting protects your services from abuse and cascading failures. The token bucket algorithm strikes a great balance between burst tolerance and steady throughput.',
      },
      {
        type: 'code',
        block: {
          language: 'java',
          code: 'class TokenBucket {\n    private double tokens;\n    private final double capacity, refillPerSec;\n    private long last = System.nanoTime();\n\n    TokenBucket(double cap, double rate) { capacity = cap; refillPerSec = rate; tokens = cap; }\n\n    synchronized boolean allow() {\n        long now = System.nanoTime();\n        tokens = Math.min(capacity, tokens + (now - last) / 1e9 * refillPerSec);\n        last = now;\n        if (tokens >= 1) { tokens -= 1; return true; }\n        return false;\n    }\n}',
          output: 'request allowed: true',
        },
      },
    ],
    quiz: [
      {
        id: 'q1',
        question: 'Which algorithm tolerates short bursts best?',
        options: ['Leaky bucket', 'Token bucket', 'Fixed window', 'None'],
        correct: 1,
      },
      {
        id: 'q2',
        question: 'Rate limiting primarily protects against...',
        options: ['Typos', 'Abuse & overload', 'Slow disks', 'CSS bugs'],
        correct: 1,
      },
      {
        id: 'q3',
        question: 'The token bucket refills tokens...',
        options: ['Never', 'At a fixed rate over time', 'On restart', 'Randomly'],
        correct: 1,
      },
    ],
  },
]

export function getArticle(slug: string): Article | undefined {
  return articles.find((a) => a.slug === slug)
}

export const trending = articles.slice(0, 5)

export const topDevelopers = authors.slice(0, 4)

export type Notification = {
  id: string
  type: 'follow' | 'comment' | 'flag' | 'quiz' | 'badge' | 'like'
  text: string
  time: string
  read: boolean
}

export const notifications: Notification[] = [
  { id: 'n1', type: 'follow', text: '@lenabuilds started following you', time: '12m ago', read: false },
  { id: 'n2', type: 'comment', text: '@dsantos commented on "Mastering Dependency Injection in Spring Boot"', time: '38m ago', read: false },
  { id: 'n3', type: 'badge', text: 'You earned the Quiz Master badge!', time: '2h ago', read: false },
  { id: 'n4', type: 'like', text: '@priyacodes liked your post "Intro to Spring Security"', time: '5h ago', read: true },
  { id: 'n5', type: 'quiz', text: '@marcusdev completed the quiz on your post', time: '1d ago', read: true },
  { id: 'n6', type: 'flag', text: 'Your post "Java 8 Streams" was flagged as outdated', time: '2d ago', read: true },
  { id: 'n7', type: 'follow', text: '@sofiar started following you', time: '3d ago', read: true },
]

export type Comment = {
  id: string
  author: Author
  content: string
  time: string
  likes: number
}

export const comments: Comment[] = [
  {
    id: 'c1',
    author: author('a3'),
    content:
      'Constructor injection changed my life. Field injection silently broke half my tests until I switched. Great write-up!',
    time: '1h ago',
    likes: 24,
  },
  {
    id: 'c2',
    author: author('a6'),
    content:
      'Would love a follow-up on circular dependencies and how @Lazy plays into this.',
    time: '2h ago',
    likes: 11,
  },
  {
    id: 'c3',
    author: author('a4'),
    content: 'The @Primary vs @Qualifier section finally made it click. Bookmarked.',
    time: '3h ago',
    likes: 8,
  },
]

export type Badge = {
  id: string
  icon: string
  name: string
  description: string
  unlocked: boolean
}

export const badges: Badge[] = [
  { id: 'b1', icon: 'flame', name: 'First Post', description: 'Published first article', unlocked: true },
  { id: 'b2', icon: 'target', name: 'Century', description: 'Reached 100 reads', unlocked: true },
  { id: 'b3', icon: 'brain', name: 'Quiz Master', description: 'Completed 10 quizzes', unlocked: true },
  { id: 'b4', icon: 'zap', name: 'Code Runner', description: 'Ran code 50 times', unlocked: true },
  { id: 'b5', icon: 'trophy', name: 'Top 10', description: 'Reached leaderboard top 10', unlocked: false },
  { id: 'b6', icon: 'flag', name: 'Watchdog', description: 'Flagged 5 outdated articles', unlocked: false },
]

export type LeaderboardEntry = {
  rank: number
  author: Author
  articles: number
  points: number
  badges: number
}

export const leaderboard: LeaderboardEntry[] = authors
  .map((a, i) => ({
    rank: i + 1,
    author: a,
    articles: a.articles,
    points: a.points,
    badges: 6 - i,
  }))
  .concat(
    Array.from({ length: 14 }).map((_, i) => ({
      rank: i + 7,
      author: {
        ...authors[i % authors.length],
        id: `lb${i}`,
        name: ['Noah Kim', 'Emma Silva', 'Liam Chen', 'Ava Patel', 'Ethan Wright', 'Mia Lopez', 'Lucas Brown', 'Zoe Adams', 'Kai Tanaka', 'Nora Hassan', 'Leo Costa', 'Iris Wong', 'Omar Said', 'Tara Singh'][i],
        username: ['noahk', 'emmas', 'liamc', 'avap', 'ethanw', 'mial', 'lucasb', 'zoea', 'kait', 'norah', 'leoc', 'irisw', 'omars', 'taras'][i],
      },
      articles: 28 - i,
      points: 3200 - i * 180,
      badges: 5 - (i % 5),
    })),
  )
