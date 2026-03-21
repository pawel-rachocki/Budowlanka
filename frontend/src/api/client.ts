import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { config } from '../config'
import { authApi } from './auth.api'

export const TOKEN_KEYS = {
  access: 'access_token',
  refresh: 'refresh_token',
} as const

const apiClient = axios.create({
  baseURL: config.apiUrl,
})


apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEYS.access)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

interface QueueEntry {
  resolve: (token: string) => void
  reject: (reason: unknown) => void
}

let isRefreshing = false
let failedQueue: QueueEntry[] = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach((entry) => {
    if (error) {
      entry.reject(error)
    } else {
      entry.resolve(token as string)
    }
  })
  failedQueue = []
}

function handleAuthFailure() {
  localStorage.removeItem(TOKEN_KEYS.access)
  localStorage.removeItem(TOKEN_KEYS.refresh)
  window.dispatchEvent(new Event('auth:logout'))
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    const refreshToken = localStorage.getItem(TOKEN_KEYS.refresh)
    if (!refreshToken) {
        handleAuthFailure()
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then((newToken) => {
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        originalRequest._retry = true
        return apiClient(originalRequest)
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    try {
      const { data } = await authApi.refresh({ refreshToken })
      const newAccessToken = data.accessToken

      localStorage.setItem(TOKEN_KEYS.access, newAccessToken)

      processQueue(null, newAccessToken)
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return apiClient(originalRequest)
    } catch (refreshError) {
      processQueue(refreshError, null)
      handleAuthFailure()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export default apiClient
