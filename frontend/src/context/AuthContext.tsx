/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/auth.api'
import { setAccessToken as syncModuleToken } from '../api/client'
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessTokenState] = useState<string | null>(null)
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // On startup: attempt silent refresh using the httpOnly cookie.
  // If no valid session exists the request will 401 and the user stays logged out.
  useEffect(() => {
    async function initAuth() {
      try {
        const { data } = await authApi.refresh()
        const token = data.accessToken
        syncModuleToken(token)
        setAccessTokenState(token)
        setUser(userFromToken(token))
      } catch {
        // no valid session — stay logged out
      } finally {
        setIsLoading(false)
      }
    }
    initAuth()
  }, [])

  useEffect(() => {
    function handleAuthLogout() {
      syncModuleToken(null)
      setUser(null)
      setAccessTokenState(null)
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
    syncModuleToken(tokens.accessToken)
    setAccessTokenState(tokens.accessToken)
    setUser(decoded)
  }, [])

  const logout = useCallback(async () => {
    if (accessToken) {
      try {
        await authApi.logout(accessToken)
      } catch {
        // best effort
      }
    }
    syncModuleToken(null)
    setAccessTokenState(null)
    setUser(null)
  }, [accessToken])

  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  )
}
