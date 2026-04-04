import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { config } from '../config'
import { authApi } from './auth.api'

// accessToken lives in module memory — never persisted to localStorage.
// AuthContext calls setAccessToken() on login/logout/refresh.
let accessToken: string | null = null

export function setAccessToken(token: string | null): void {
  accessToken = token
}

const apiClient = axios.create({
  baseURL: config.apiUrl,
  withCredentials: true,
})

apiClient.interceptors.request.use((reqConfig) => {
  if (accessToken) {
    reqConfig.headers.Authorization = `Bearer ${accessToken}`
  }
  return reqConfig
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
  setAccessToken(null)
  window.dispatchEvent(new Event('auth:logout'))
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // No point attempting refresh if we have no session in memory
    if (!accessToken) {
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
      const { data } = await authApi.refresh()
      const newAccessToken = data.accessToken

      setAccessToken(newAccessToken)
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
  }
)

export default apiClient
