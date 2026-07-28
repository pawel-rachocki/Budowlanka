export type UserRole = 'CLIENT' | 'CREW' | 'ADMIN'

export interface AuthTokens {
  accessToken: string
  tokenType: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  role: 'CLIENT' | 'CREW'
}

export interface User {
  id: string
  email: string
  role: UserRole
}

export interface AuthContextValue {
  user: User | null
  accessToken: string | null
  login: (data: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  isLoading: boolean // reserved for future async init (e.g. /api/auth/me)
}
