import { useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import type { UpdatePriceFormValues, UseUpdatePriceModalParams } from '@/types/modal';

export function useUpdatePriceModal({ listing, isOpen, onClose, onSuccess }: UseUpdatePriceModalParams) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<UpdatePriceFormValues>({
    defaultValues: {
      newPrice: '',
    },
  });

  useEffect(() => {
    if (isOpen) {
      reset({ newPrice: '' });
    }
  }, [isOpen, listing.id, reset]);

  const onSubmit = useCallback(
    async (data: UpdatePriceFormValues) => {
      try {
        const price = Number(data.newPrice);
        const response = await fetch(`/api/listings/${listing.id}/price`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pricePerKg: price }),
        });

        if (!response.ok) {
          const error = await response.json();
          toast.error(error.message || 'Failed to update price');
          return;
        }

        toast.success('Price updated successfully');
        onSuccess?.();
        reset({ newPrice: '' });
        onClose();
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'Failed to update price');
      }
    },
    [listing.id, onClose, onSuccess, reset],
  );

  return {
    register,
    handleSubmit,
    errors,
    isSubmitting,
    onSubmit,
  };
}