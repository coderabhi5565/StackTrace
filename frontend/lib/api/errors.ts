import type { ApiErrorBody } from './types'

export class ApiError extends Error {
  readonly status: number
  readonly body: ApiErrorBody | null

  constructor(status: number, body: ApiErrorBody | null, fallback = 'Request failed') {
    super(ApiError.describe(body) ?? fallback)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }

  /** Human readable message, preferring field-level validation errors. */
  static describe(body: ApiErrorBody | null): string | undefined {
    if (!body) return undefined
    const fields = body.validationErrors ? Object.values(body.validationErrors) : []
    if (fields.length) return fields.join('. ')
    return body.message
  }
}
