import { type ListingResponse } from "@/services/listingService";

const MAX_LISTING_IMAGES = 5;

export function validateRequired(value: string, fieldName: string): string | null {
    if (!value.trim()) {
        return `${fieldName} is required`;
    }
    return null;
}

export function validateEmail(value: string): string | null {
    if (!value.trim()) {
        return "Email is required";
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
        return "Please enter a valid email address";
    }
    return null;
}

export function validatePincode(value: string): string | null {
    if (!value.trim()) {
        return "Pincode is required";
    }
    const pincodeRegex = /^\d{6}$/;
    if (!pincodeRegex.test(value)) {
        return "Pincode must be exactly 6 digits";
    }
    return null;
}

export function validatePassword(value: string): string | null {
    if (!value.trim()) {
        return "Password is required";
    }
    if (value.length < 8) {
        return "Password must be at least 8 characters";
    }
    return null;
}

export const getListingImagesValidationError = ({
    listing,
    filesToAddCount,
    selectedToRemoveCount,
}: {
    listing: ListingResponse;
    filesToAddCount: number;
    selectedToRemoveCount: number;
}) => {
    const remainingImageCount = listing.images.length - selectedToRemoveCount;
    const totalAfterUpdate = remainingImageCount + filesToAddCount;

    if (totalAfterUpdate > MAX_LISTING_IMAGES) {
        return `Maximum ${MAX_LISTING_IMAGES} images allowed. Current selection results in ${totalAfterUpdate} images.`;
    }

    return null;
};
