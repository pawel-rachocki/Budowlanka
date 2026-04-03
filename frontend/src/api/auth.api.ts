import axios from 'axios'
import { config } from '../config'
import type { AuthTokens, LoginRequest, RegisterRequest } from '../types/auth.types'

const authAxios = axios.create({
  baseURL: config.apiUrl,
})

export const authApi = {
  register: (data: RegisterRequest) => authAxios.post<{ message: string }>('/auth/register', data),

  login: (data: LoginRequest) =>
    authAxios.post<AuthTokens>('/auth/login', data, { withCredentials: true }),

  refresh: () =>
    authAxios.post<Pick<AuthTokens, 'accessToken'>>('/auth/refresh', null, {
      withCredentials: true,
    }),

  logout: (accessToken: string) =>
    authAxios.post('/auth/logout', null, {
      headers: { Authorization: `Bearer ${accessToken}` },
      withCredentials: true,
    }),

  verifyEmail: (token: string) =>
    authAxios.get<{ message: string }>('/auth/verify', { params: { token } }),
}
