/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/auth.api'
import { TOKEN_KEYS } from '../api/client'
import type { AuthContextValue, LoginRequest, User } from '../types/auth.types'

export const AuthContext = createContext<AuthContextValue | null>(null)

const VALID_ROLES = ['CLIENT', 'CREW', 'ADMIN'] as const

function parseJwt(token: string): Record<string, unknown> {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return {}
  }
}

function userFromToken(token: string): User | null {
  const p = parseJwt(token)
  if (
    typeof p.sub !== 'string' ||
    typeof p.email !== 'string' ||
    !VALID_ROLES.includes(p.role as User['role'])
  )
    return null
  return {
    id: p.sub as string,
    email: p.email as string,
    role: p.role as User['role'],
  }
}

function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEYS.access)
}

function isTokenExpired(token: string): boolean {
  const p = parseJwt(token)
  if (!p.exp) return true
  return (p.exp as number) * 1000 < Date.now()
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function initAuth() {
      const storedAccess = getStoredToken()

      if (storedAccess && !isTokenExpired(storedAccess)) {
        setAccessToken(storedAccess)
        setUser(userFromToken(storedAccess))
        setIsLoading(false)
        return
      }

      const storedRefresh = localStorage.getItem(TOKEN_KEYS.refresh)
      if (storedRefresh) {
        try {
          const { data } = await authApi.refresh({ refreshToken: storedRefresh })
          const newAccess = data.accessToken
          localStorage.setItem(TOKEN_KEYS.access, newAccess)
          setAccessToken(newAccess)
          setUser(userFromToken(newAccess))
        } catch {
          localStorage.removeItem(TOKEN_KEYS.access)
          localStorage.removeItem(TOKEN_KEYS.refresh)
        }
      }

      setIsLoading(false)
    }

    initAuth()
  }, [])

  useEffect(() => {
    function handleAuthLogout() {
      setUser(null)
      setAccessToken(null)
    }
    window.addEventListener('auth:logout', handleAuthLogout)
    return () => window.removeEventListener('auth:logout', handleAuthLogout)
  }, [])

  const login = useCallback(async (data: LoginRequest) => {
    const { data: tokens } = await authApi.login(data)
    const decoded = userFromToken(tokens.accessToken)
    if (!decoded) {
      throw new Error('Nieprawidłowy token: brak wymaganych claims')
    }
    localStorage.setItem(TOKEN_KEYS.access, tokens.accessToken)
    localStorage.setItem(TOKEN_KEYS.refresh, tokens.refreshToken)
    setAccessToken(tokens.accessToken)
    setUser(decoded)
  }, [])

  const logout = useCallback(async () => {
    const token = localStorage.getItem(TOKEN_KEYS.access)
    if (token) {
      try {
        await authApi.logout(token)
      } catch {
        // best effort
      }
    }
    localStorage.removeItem(TOKEN_KEYS.access)
    localStorage.removeItem(TOKEN_KEYS.refresh)
    setUser(null)
    setAccessToken(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  )
}
