export interface AuthTokens {
  accessToken: string
  refreshToken: string
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

export interface RefreshRequest {
  refreshToken: string
}
