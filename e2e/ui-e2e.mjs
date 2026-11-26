// Usage: (stack running) cd e2e && npm ci && BASE=http://localhost:3000 npm test
// Drives the real UI in headless Chrome: register, write + publish, comment, like, bookmark, follow, search.
import { chromium } from 'playwright-core'

const BASE = process.env.BASE ?? 'http://localhost:3000'
const R = Date.now().toString().slice(-6)
const errors = []
const ok = (m) => console.log('  ✓', m)

const browser = await chromium.launch({ channel: 'chrome', headless: true })

async function newUser(name) {
  const ctx = await browser.newContext()
  const page = await ctx.newPage()
  page.on('pageerror', (e) => errors.push(`[${name}] pageerror: ${e.message}`))
  page.on('console', (m) => m.type() === 'error' && errors.push(`[${name}] console: ${m.text()}`))
  await page.goto(`${BASE}/register`)
  await page.fill('#name', `UI ${name}`)
  await page.fill('#username', `ui_${name}_${R}`)
  await page.fill('#email', `ui_${name}_${R}@example.com`)
  await page.fill('#password', 'password123')
  await page.click('button[type=submit]')
  await page.waitForURL('**/home')
  await page.getByRole('button', { name: 'Account menu' }).waitFor()
  ok(`${name} registered through the form and is signed in`)
  return page
}

const alice = await newUser('alice')
await alice.getByRole('link', { name: 'Write' }).click()
await alice.waitForURL('**/create')
const title = `UI written article ${R}`
await alice.fill('#title', title)
await alice.fill('#tags', 'playwright')
await alice.press('#tags', 'Enter')
await alice.fill('#body', `## Hello from the editor\n\nThe secret word is axolotl${R}.\n\n\`\`\`js\nconsole.log(42)\n\`\`\``)
await alice.getByText('Hello from the editor').first().waitFor() // live preview rendered
ok('editor live preview renders markdown')
await alice.getByRole('button', { name: 'Publish' }).click()
await alice.waitForURL('**/post/**')
await alice.getByRole('heading', { name: title }).waitFor()
const postUrl = alice.url()
ok(`published, landed on ${new URL(postUrl).pathname}`)

const bob = await newUser('bob')
await bob.goto(postUrl)
await bob.getByText(`axolotl${R}`).waitFor()
await bob.getByRole('button', { name: 'Follow' }).click()
await bob.getByRole('button', { name: 'Following' }).waitFor()
ok('bob follows alice from the post page')
const like = bob.locator('button[aria-pressed]').first()
await like.click()
await bob.waitForFunction(() => document.querySelector('button[aria-pressed]')?.getAttribute('aria-pressed') === 'true')
ok('bob liked the post')
await bob.getByRole('button', { name: 'Save' }).click()
await bob.getByRole('button', { name: 'Saved' }).waitFor()
ok('bob bookmarked the post')
await bob.fill('textarea[placeholder="Add to the discussion…"]', 'Great read, thanks!')
await bob.getByRole('button', { name: 'Post', exact: true }).click()
await bob.getByText('Great read, thanks!').waitFor()
ok('bob commented')

await bob.getByRole('searchbox').or(bob.getByLabel('Search')).first().fill(`axolotl${R}`)
await bob.keyboard.press('Enter')
await bob.waitForURL('**/search?**')
await bob.getByRole('heading', { name: title }).waitFor()
ok('navbar search finds the article')

await bob.goto(`${BASE}/bookmarks`)
await bob.getByRole('heading', { name: title }).waitFor()
ok('bookmarks page lists it')

await alice.goto(`${BASE}/notifications`)
await alice.getByText('commented on your post.').waitFor()
await alice.getByText('started following you.').waitFor()
ok('alice sees comment + follow notifications')

// reply from alice (nested)
await alice.goto(postUrl)
await alice.getByRole('button', { name: 'Reply' }).first().click()
await alice.fill('textarea[placeholder^="Reply to"]', 'Glad you liked it!')
await alice.getByRole('button', { name: 'Reply', exact: true }).last().click()
await alice.getByText('Glad you liked it!').waitFor()
ok('alice replied in a nested thread')

await alice.goto(`${BASE}/profile/ui_alice_${R}`)
await alice.getByRole('heading', { name: 'UI alice' }).waitFor()
await alice.getByText('Followers').first().waitFor()
ok('profile page renders')

await alice.goto(`${BASE}/leaderboard`)
await alice.getByRole('heading', { name: 'Leaderboard' }).waitFor()
ok('leaderboard renders')

await browser.close()
if (errors.length) {
  console.log('Browser errors:\n' + errors.join('\n'))
  process.exit(1)
}
console.log('UI E2E passed with no browser errors')
