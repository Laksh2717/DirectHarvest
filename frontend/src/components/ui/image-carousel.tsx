"use client";

import { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, ImageOff } from "lucide-react";
import ListingImage from "@/components/listings/ListingImage";
import { type ListingImageResponse } from "@/services/listingService";
import { type ListingImageVariant } from "@/lib/cloudinary";

type ImageCarouselProps = {
    images: ListingImageResponse[];
    alt: string;
    variant?: ListingImageVariant;
    className?: string;
    imageClassName?: string;
    autoPlayMs?: number;
    noImageText?: string;
    prevAriaLabel?: string;
    nextAriaLabel?: string;
    stopPropagationOnControls?: boolean;
};

export default function ImageCarousel({
    images,
    alt,
    variant = "card",
    className,
    imageClassName,
    autoPlayMs = 2800,
    noImageText = "No image for this listing",
    prevAriaLabel = "Show previous image",
    nextAriaLabel = "Show next image",
    stopPropagationOnControls = false,
}: ImageCarouselProps) {
    const [activeImageIndex, setActiveImageIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);

    const hasMultipleImages = images.length > 1;
    const hasImages = images.length > 0;
    const activeImage = images[activeImageIndex] ?? images[0] ?? null;

    useEffect(() => {
        if (!hasMultipleImages || isHovered) {
            return;
        }

        const timer = window.setInterval(() => {
            setActiveImageIndex((prev) => (prev + 1) % images.length);
        }, autoPlayMs);

        return () => {
            window.clearInterval(timer);
        };
    }, [autoPlayMs, hasMultipleImages, images.length, isHovered]);

    const showPreviousImage = () => {
        if (!hasMultipleImages) {
            return;
        }
        setActiveImageIndex((prev) => (prev - 1 + images.length) % images.length);
    };

    const showNextImage = () => {
        if (!hasMultipleImages) {
            return;
        }
        setActiveImageIndex((prev) => (prev + 1) % images.length);
    };

    return (
        <div
            className={className ?? "relative overflow-hidden rounded-t-2xl bg-muted/40"}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <ListingImage image={activeImage} alt={alt} variant={variant} className={imageClassName} />

            {!hasImages ? (
                <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-muted/70">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-background/90 text-muted-foreground shadow-sm">
                        <ImageOff className="h-5 w-5" />
                    </div>
                    <p className="rounded-full bg-background/90 px-3 py-1 text-xs font-semibold text-muted-foreground">
                        {noImageText}
                    </p>
                </div>
            ) : null}

            {hasMultipleImages ? (
                <>
                    <button
                        type="button"
                        onClick={(event) => {
                            if (stopPropagationOnControls) {
                                event.stopPropagation();
                            }
                            showPreviousImage();
                        }}
                        aria-label={prevAriaLabel}
                        className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-black/55 p-1.5 text-white transition hover:bg-black/75"
                    >
                        <ChevronLeft className="h-4 w-4" />
                    </button>
                    <button
                        type="button"
                        onClick={(event) => {
                            if (stopPropagationOnControls) {
                                event.stopPropagation();
                            }
                            showNextImage();
                        }}
                        aria-label={nextAriaLabel}
                        className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-black/55 p-1.5 text-white transition hover:bg-black/75"
                    >
                        <ChevronRight className="h-4 w-4" />
                    </button>
                </>
            ) : null}
        </div>
    );
}