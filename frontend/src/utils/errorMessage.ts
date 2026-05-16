import { isAxiosError } from 'axios'

export function extractErrorMessage(err: unknown, fallback: string): string {
  if (isAxiosError(err)) {
    const msg = (err.response?.data as { message?: string })?.message
    if (msg) return msg
  }
  return fallback
}
