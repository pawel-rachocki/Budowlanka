import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAddReview, useUpdateReview } from '../../hooks/useReviews'
import type { ReviewResponse } from '../../types/review.types'
import { StarRatingInput } from './StarRatingInput'

const schema = z.object({
  rating: z.number().int().min(1).max(5),
  comment: z
    .string()
    .trim()
    .transform((v) => (v === '' ? undefined : v))
    .pipe(z.string().min(10, 'Komentarz musi mieć co najmniej 10 znaków').max(1000).optional()),
})

type FormValues = z.infer<typeof schema>

interface ReviewFormProps {
  slug: string
  review?: ReviewResponse
  onSuccess: () => void
  onCancel?: () => void
}

export function ReviewForm({ slug, review, onSuccess, onCancel }: ReviewFormProps) {
  const isEdit = !!review
  const { addReview, isAdding } = useAddReview(slug)
  const { updateReview, isUpdating } = useUpdateReview(slug)
  const isPending = isAdding || isUpdating

  const {
    control,
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      rating: review?.rating ?? 0,
      comment: review?.comment ?? '',
    },
  })

  const rating = useWatch({ control, name: 'rating' })

  async function onSubmit(values: FormValues) {
    try {
      if (isEdit) {
        await updateReview({ reviewId: review.id, data: values })
      } else {
        await addReview(values)
      }
      onSuccess()
    } catch {
      // toast already shown by hook's onError
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-4 flex flex-col gap-3"
      noValidate
    >
      <div className="flex flex-col gap-1">
        <span className="text-sm font-medium text-navy-800">Twoja ocena</span>
        <StarRatingInput
          value={rating}
          onChange={(v) => setValue('rating', v, { shouldValidate: true })}
        />
        {errors.rating && (
          <p className="text-xs text-red-500">{errors.rating.message ?? 'Ocena jest wymagana'}</p>
        )}
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="review-comment" className="text-sm font-medium text-navy-800">
          Komentarz <span className="text-muted font-normal">(opcjonalny)</span>
        </label>
        <textarea
          id="review-comment"
          {...register('comment')}
          rows={3}
          placeholder="Opisz swoje doświadczenie z tą ekipą…"
          className="w-full rounded-lg border border-navy-100 bg-white px-3 py-2 text-sm text-navy-900 placeholder:text-muted resize-none focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
        />
        {errors.comment && <p className="text-xs text-red-500">{errors.comment.message}</p>}
      </div>

      <div className="flex items-center gap-2 pt-1">
        <button
          type="submit"
          disabled={isPending || rating === 0}
          className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-1"
        >
          {isPending ? 'Zapisywanie…' : isEdit ? 'Zapisz zmiany' : 'Dodaj opinię'}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            disabled={isPending}
            className="rounded-lg px-4 py-2 text-sm font-medium text-navy-600 transition-colors hover:text-navy-900 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-1"
          >
            Anuluj
          </button>
        )}
      </div>
    </form>
  )
}
