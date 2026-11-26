// Liveness probe for containers / platforms (does not call the backend).
export function GET() {
  return Response.json({ status: 'UP' })
}
