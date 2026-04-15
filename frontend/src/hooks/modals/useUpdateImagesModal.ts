import { useMemo, useState } from 'react';
import { toast } from 'sonner';
import type { ChangeEvent } from 'react';
import type { ImagePreview, UseUpdateImagesModalParams } from '@/types/modal';

const CLOUDINARY_CLOUD_NAME = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;
const CLOUDINARY_UPLOAD_PRESET = process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET;

export function useUpdateImagesModal({ listing, onClose, onSuccess }: UseUpdateImagesModalParams) {
  const [newFiles, setNewFiles] = useState<File[]>([]);
  const [currentImages] = useState<ImagePreview[]>(
    (listing.images || []).map((img) => ({
      id: img.id,
      url: img.cloudinarySecureUrl,
      isPrimary: img.primary,
    })),
  );
  const [imagesToRemove, setImagesToRemove] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const remainingCurrentImages = useMemo(() => currentImages.length - imagesToRemove.length, [currentImages.length, imagesToRemove.length]);
  const totalImages = useMemo(() => remainingCurrentImages + newFiles.length, [newFiles.length, remainingCurrentImages]);

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const imageFiles = Array.from(e.target.files).filter((file) => file.type.startsWith('image/'));

      if (imageFiles.length !== e.target.files.length) {
        toast.error('Please select only image files');
      }

      setNewFiles(imageFiles);
    }
  };

  const handleRemoveImage = (imageId: number) => {
    if (imagesToRemove.includes(imageId)) {
      setImagesToRemove(imagesToRemove.filter((id) => id !== imageId));
    } else {
      setImagesToRemove([...imagesToRemove, imageId]);
    }
  };

  const uploadImageToCloudinary = async (file: File): Promise<{ public_id: string; secure_url: string }> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET || '');

    const response = await fetch(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Failed to upload image to Cloudinary');
    }

    return response.json();
  };

  const handleSubmit = async () => {
    try {
      if (totalImages > 5) {
        toast.error(`Cannot exceed 5 images per listing. Total would be ${totalImages}`);
        return;
      }

      if (totalImages === 0) {
        toast.error('At least one image is required');
        return;
      }

      setIsLoading(true);

      const uploadedImages: Array<{ publicId: string; imageUrl: string; isPrimary: boolean }> = [];
      for (const file of newFiles) {
        const cloudinaryResponse = await uploadImageToCloudinary(file);
        uploadedImages.push({
          publicId: cloudinaryResponse.public_id,
          imageUrl: cloudinaryResponse.secure_url,
          isPrimary: false,
        });
      }

      for (const imageId of imagesToRemove) {
        await fetch(`/api/listings/${listing.id}/images/${imageId}`, {
          method: 'DELETE',
        });
      }

      for (let i = 0; i < uploadedImages.length; i++) {
        const image = uploadedImages[i];
        if (remainingCurrentImages === 0 && i === 0) {
          image.isPrimary = true;
        }

        const response = await fetch(`/api/listings/${listing.id}/images`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            publicId: image.publicId,
            imageUrl: image.imageUrl,
            isPrimary: image.isPrimary,
          }),
        });

        if (!response.ok) {
          throw new Error('Failed to add image to listing');
        }
      }

      const currentRemaining = currentImages.filter((img) => !imagesToRemove.includes(img.id));
      if (currentRemaining.length > 0 && !currentRemaining[0].isPrimary) {
        await fetch(`/api/listings/${listing.id}/images/${currentRemaining[0].id}/set-primary`, {
          method: 'PATCH',
        });
      }

      toast.success('Images updated successfully');
      onSuccess?.();
      onClose();
    } catch (error) {
      console.error('Error updating images:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to update images');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    setNewFiles([]);
    setImagesToRemove([]);
    onClose();
  };

  return {
    newFiles,
    currentImages,
    imagesToRemove,
    isLoading,
    remainingCurrentImages,
    totalImages,
    handleFileChange,
    handleRemoveImage,
    handleSubmit,
    handleCancel,
  };
}