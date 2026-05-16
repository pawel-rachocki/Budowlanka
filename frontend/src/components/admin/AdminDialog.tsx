import type { ReactNode } from 'react'
import * as Dialog from '@radix-ui/react-dialog'

interface AdminDialogProps {
  open: boolean
  onClose: () => void
  title: string
  description: ReactNode
  children: ReactNode
}

export default function AdminDialog({
  open,
  onClose,
  title,
  description,
  children,
}: AdminDialogProps) {
  const handleOpenChange = (open: boolean) => {
    if (!open) onClose()
  }

  return (
    <Dialog.Root open={open} onOpenChange={handleOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
        <Dialog.Content className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-surface-card p-6 shadow-xl focus:outline-none">
          <Dialog.Close className="absolute right-4 top-4 flex h-8 w-8 items-center justify-center rounded-lg text-navy-400 transition-colors hover:bg-navy-50 hover:text-navy-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500">
            <CloseIcon />
            <span className="sr-only">Zamknij</span>
          </Dialog.Close>
          <Dialog.Title className="text-lg font-bold text-navy-900">{title}</Dialog.Title>
          <Dialog.Description className="mt-1 text-sm text-navy-600">
            {description}
          </Dialog.Description>
          {children}
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}

function CloseIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}
