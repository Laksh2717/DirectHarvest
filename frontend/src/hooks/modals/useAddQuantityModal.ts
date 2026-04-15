import { useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import type { AddQuantityFormValues, UseAddQuantityModalParams } from '@/types/modal';

export function useAddQuantityModal({ listing, isOpen, onClose, onSuccess }: UseAddQuantityModalParams) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<AddQuantityFormValues>({
    defaultValues: {
      newQuantity: '',
    },
  });

  useEffect(() => {
    if (isOpen) {
      reset({ newQuantity: '' });
    }
  }, [isOpen, listing.id, reset]);

  const onSubmit = useCallback(
    async (data: AddQuantityFormValues) => {
      try {
        const qty = Number(data.newQuantity);
        const response = await fetch(`/api/listings/${listing.id}/quantity`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ quantity: qty }),
        });

        if (!response.ok) {
          const error = await response.json();
          toast.error(error.message || 'Failed to update quantity');
          return;
        }

        toast.success('Quantity updated successfully');
        onSuccess?.();
        reset({ newQuantity: '' });
        onClose();
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'Failed to update quantity');
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