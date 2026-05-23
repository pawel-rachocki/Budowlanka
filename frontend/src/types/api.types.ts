export interface ApiError {
  status: number
  message: string
  timestamp: string
  errors?: string[]
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
