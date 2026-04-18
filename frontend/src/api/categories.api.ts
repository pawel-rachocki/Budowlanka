import apiClient from './client'
import type { ServiceCategoryResponse } from '../types/category.types'

export const categoriesApi = {
  getCategories: () => apiClient.get<ServiceCategoryResponse[]>('/categories'),
}
