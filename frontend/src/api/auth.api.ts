// Plain axios (nie apiClient) — celowo:
// 1. auth endpoints nie wymagają Bearer tokena w request interceptorze
// 2. apiClient importuje authApi do obsługi refresh — użycie apiClient tutaj
//    spowodowałoby circular dependency
import axios from 'axios'
import { config } from '../config'
import type { AuthTokens, LoginRequest, RefreshRequest, RegisterRequest } from '../types/auth.types'

export const authApi = {
  register: (data: RegisterRequest) =>
    axios.post<{ message: string }>(`${config.apiUrl}/auth/register`, data),

  login: (data: LoginRequest) => axios.post<AuthTokens>(`${config.apiUrl}/auth/login`, data),

  refresh: (data: RefreshRequest) =>
    axios.post<Pick<AuthTokens, 'accessToken'>>(`${config.apiUrl}/auth/refresh`, data),

  logout: (accessToken: string) =>
    axios.post(`${config.apiUrl}/auth/logout`, null, {
      headers: { Authorization: `Bearer ${accessToken}` },
    }),

  verifyEmail: (token: string) =>
    axios.get<{ message: string }>(`${config.apiUrl}/auth/verify`, { params: { token } }),
}
