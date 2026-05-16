import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { adminApi } from '../api/admin.api'
import type { AdminCrewsParams } from '../api/admin.api'
import type {
  AdminCrewResponse,
  BlockCrewRequest,
  ModerationDecisionRequest,
} from '../types/admin.types'
import type { PhotoModerationItem, ModerationStatus } from '../types/photo.types'
import type { Page } from '../types/crew.types'
import { extractErrorMessage } from '../utils/errorMessage'
import { useAuth } from './useAuth'
import { useToast } from './useToast'

export function useModerationQueue(status?: ModerationStatus, page = 0, size?: number) {
  const { user } = useAuth()

  const { data, isLoading, isFetching, error } = useQuery<Page<PhotoModerationItem>, AxiosError>({
    queryKey: ['admin', 'moderation', status, page, size],
    queryFn: () => adminApi.listModerationQueue(status, page, size).then((res) => res.data),
    enabled: user?.role === 'ADMIN',
    staleTime: 10_000,
  })

  return {
    queue: data?.content ?? [],
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    isLoading,
    isFetching,
    error,
  }
}

export function useModeratePhoto() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const { showToast } = useToast()

  const { mutate, isPending } = useMutation({
    mutationFn: ({ id, body }: { id: string; body: ModerationDecisionRequest }) => {
      if (user?.role !== 'ADMIN') throw new Error('Brak uprawnień')
      return adminApi.decideOnPhoto(id, body).then((res) => res.data)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'moderation'] })
      showToast('Decyzja zapisana', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się zapisać decyzji'), 'error')
    },
  })

  return {
    moderatePhoto: mutate,
    isSubmitting: isPending,
  }
}

export function useAdminCrews(params?: AdminCrewsParams) {
  const { user } = useAuth()

  const { data, isLoading, isFetching, error } = useQuery<Page<AdminCrewResponse>, AxiosError>({
    queryKey: ['admin', 'crews', params],
    queryFn: () => adminApi.listAdminCrews(params).then((res) => res.data),
    enabled: user?.role === 'ADMIN',
    staleTime: 30_000,
  })

  return {
    crews: data?.content ?? [],
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    isLoading,
    isFetching,
    error,
  }
}

export function useBlockCrew() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const { showToast } = useToast()

  const { mutate, isPending } = useMutation({
    mutationFn: ({ id, body }: { id: string; body: BlockCrewRequest }) => {
      if (user?.role !== 'ADMIN') throw new Error('Brak uprawnień')
      return adminApi.blockCrew(id, body).then((res) => res.data)
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'crews'] })
      queryClient.invalidateQueries({ queryKey: ['crews'] })
      showToast(variables.body.blocked ? 'Profil zablokowany' : 'Profil odblokowany', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się zmienić statusu profilu'), 'error')
    },
  })

  return {
    blockCrew: mutate,
    isSubmitting: isPending,
  }
}
