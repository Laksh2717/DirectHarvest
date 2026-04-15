'use client';

import { ReactNode, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface FormModalProps {
  open: boolean;
  title: string;
  description?: string;
  submitText: string;
  loading?: boolean;
  onSubmit: () => void | Promise<void>;
  onCancel: () => void;
  children: ReactNode;
}

export default function FormModal({
  open,
  title,
  description,
  submitText,
  loading = false,
  onSubmit,
  onCancel,
  children,
}: FormModalProps) {
  useEffect(() => {
    if (!open) return;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onCancel();
      }
    };

    window.addEventListener('keydown', handleEscape);
    return () => window.removeEventListener('keydown', handleEscape);
  }, [open, onCancel]);

  if (!open) return null;

  return createPortal(
    <div className="fixed inset-0 z-[1000] flex items-center justify-center px-4">
      <button
        type="button"
        aria-label="Close modal backdrop"
        className="absolute inset-0 bg-black/50 backdrop-blur-[2px]"
        onClick={onCancel}
      />

      <div className="relative z-10 w-full max-w-md rounded-2xl border border-border bg-card p-5 shadow-[var(--shadow-elevated)]">
        <div className="mb-4 flex items-start justify-between gap-3">
          <div>
            <h2 className="text-base font-semibold text-foreground">{title}</h2>
            {description ? <p className="mt-1 text-sm font-medium text-muted-foreground">{description}</p> : null}
          </div>

          <button
            type="button"
            onClick={onCancel}
            className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
            aria-label="Close modal"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="space-y-4">{children}</div>

        <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
            Cancel
          </Button>
          <Button type="button" onClick={onSubmit} disabled={loading}>
            {loading ? 'Processing...' : submitText}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
