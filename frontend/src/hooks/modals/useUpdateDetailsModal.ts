import { useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import type { ListingResponse as Listing } from '@/services/listingService';
import type { UpdateDetailsFormData, UseUpdateDetailsModalParams } from '@/types/modal';

const getDefaultValues = (listing: Listing): UpdateDetailsFormData => ({
  cropName: listing.cropName || '',
  description: listing.description || '',
  street: listing.street || '',
  city: listing.city || '',
  state: listing.state || '',
  pincode: listing.pincode || '',
});

export function useUpdateDetailsModal({ listing, isOpen, onClose, onSuccess }: UseUpdateDetailsModalParams) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<UpdateDetailsFormData>({
    defaultValues: getDefaultValues(listing),
  });

  useEffect(() => {
    if (isOpen) {
      reset(getDefaultValues(listing));
    }
  }, [isOpen, listing, reset]);

  const onSubmit = useCallback(
    async (formData: UpdateDetailsFormData) => {
      try {
        const response = await fetch(`/api/listings/${listing.id}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            cropName: formData.cropName.trim(),
            description: formData.description.trim() || undefined,
            street: formData.street.trim(),
            city: formData.city.trim(),
            state: formData.state.trim(),
            pincode: formData.pincode.trim(),
          }),
        });

        if (!response.ok) {
          const error = await response.json();
          toast.error(error.message || 'Failed to update details');
          return;
        }

        toast.success('Details updated successfully');
        onSuccess?.();
        reset(formData);
        onClose();
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'Failed to update details');
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