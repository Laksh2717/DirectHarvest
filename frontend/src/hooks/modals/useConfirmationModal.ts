import { useCallback, useState } from 'react';
import { toast } from 'sonner';
import type { UseConfirmationModalParams } from '@/types/modal';

export function useConfirmationModal({ listingId, actionType, onConfirm, onCancel }: UseConfirmationModalParams) {
  const [isLoading, setIsLoading] = useState(false);

  const handleConfirm = useCallback(async () => {
    try {
      setIsLoading(true);

      if (actionType === 'makeInactive') {
        const response = await fetch(`/api/listings/${listingId}/make-inactive`, {
          method: 'POST',
        });

        if (!response.ok) {
          const error = await response.json();
          toast.error(error.message || 'Failed to make listing inactive');
          return;
        }

        toast.success('Listing marked as inactive');
      } else if (actionType === 'delete') {
        const response = await fetch(`/api/listings/${listingId}`, {
          method: 'DELETE',
        });

        if (!response.ok) {
          const error = await response.json();
          toast.error(error.message || 'Failed to delete listing');
          return;
        }

        toast.success('Listing deleted successfully');
      }

      onConfirm();
      onCancel();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'An error occurred');
    } finally {
      setIsLoading(false);
    }
  }, [actionType, listingId, onCancel, onConfirm]);

  return { isLoading, handleConfirm };
}