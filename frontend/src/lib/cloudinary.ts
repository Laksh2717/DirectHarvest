import type { ListingImageResponse } from "@/types/listing";

export type ListingImageVariant = "card" | "detail" | "thumb";

const CLOUDINARY_CLOUD_NAME = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;

const sanitizeString = (value: unknown) => {
    if (typeof value !== "string") {
        return "";
    }

    const trimmed = value.trim();
    if (!trimmed) {
        return "";
    }

    const lowered = trimmed.toLowerCase();
    if (lowered === "null" || lowered === "undefined") {
        return "";
    }

    return trimmed;
};

const normalizeCloudinaryUrl = (value: string) => {
    const sanitized = sanitizeString(value);
    if (!sanitized) {
        return "";
    }

    if (sanitized.startsWith("//")) {
        return `https:${sanitized}`;
    }

    if (sanitized.startsWith("http://") || sanitized.startsWith("https://")) {
        return sanitized;
    }

    return "";
};

const encodePublicId = (publicId: string) =>
    publicId
        .split("/")
        .map((segment) => encodeURIComponent(segment))
        .join("/");

export const buildCloudinaryImageUrl = (
    image: Pick<ListingImageResponse, "cloudinaryPublicId" | "cloudinarySecureUrl">,
    variant: ListingImageVariant = "card",
) => {
    if (!image) {
        return "";
    }

    const secureUrl = normalizeCloudinaryUrl(image.cloudinarySecureUrl);
    const publicId = sanitizeString(image.cloudinaryPublicId);

    // Prefer the secure URL if it's valid
    if (secureUrl && secureUrl.startsWith("https://res.cloudinary.com")) {
        return secureUrl;
    }

    // Fallback: reconstruct from publicId
    if (CLOUDINARY_CLOUD_NAME && publicId) {
        const encodedPublicId = encodePublicId(publicId);
        return `https://res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}/image/upload/${encodedPublicId}`;
    }

    return "";
};

export const getPrimaryListingImage = (images: ListingImageResponse[]) => {
    if (images.length === 0) {
        return null;
    }
    return images.find((image) => image.primary) ?? images[0];
};
