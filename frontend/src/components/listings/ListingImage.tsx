"use client";

import { useState } from "react";
import Image from "next/image";
import { ImageOff } from "lucide-react";
import { buildCloudinaryImageUrl, ListingImageVariant } from "@/lib/cloudinary";
import type { ListingImageProps } from "@/types/listing";

const DIMENSIONS: Record<ListingImageVariant, { width: number; height: number }> = {
    card: { width: 640, height: 480 },
    detail: { width: 1600, height: 1200 },
    thumb: { width: 240, height: 240 },
};

export default function ListingImage({ image, alt, variant = "card", className, priority = false }: ListingImageProps) {
    const dimensions = DIMENSIONS[variant];
    const [hasError, setHasError] = useState(false);
    const containerClassName = `flex items-center justify-center bg-muted text-center ${className ?? ""}`;

    const renderFallback = (label: string) => (
        <div className={containerClassName} aria-label={label}>
            <div className="flex flex-col items-center justify-center gap-2">
                <div className="flex h-11 w-11 items-center justify-center rounded-full bg-background text-muted-foreground shadow-sm">
                    <ImageOff className="h-5 w-5" />
                </div>
                <span className="rounded-full bg-background/90 px-3 py-1 text-xs font-semibold text-muted-foreground">
                    No image for this listing
                </span>
            </div>
        </div>
    );

    if (hasError) {
        return renderFallback("Image unavailable");
    }

    if (!image) {
        return renderFallback("Listing image placeholder");
    }

    const src = buildCloudinaryImageUrl(image, variant);

    if (!src) {
        return renderFallback("Listing image unavailable");
    }

    return (
        <Image
            src={src}
            alt={alt}
            width={dimensions.width}
            height={dimensions.height}
            priority={priority}
            className={className}
            onError={() => setHasError(true)}
        />
    );
}
