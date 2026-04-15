import { MapPin } from "lucide-react";
import ImageCarousel from "@/components/ui/image-carousel";
import { formatCurrency, formatDate, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { BrowseProductCardProps } from "@/types/browse";

export default function BrowseProductCard({ listing, onOpenDetails }: BrowseProductCardProps) {
    return (
        <article
            className="cursor-pointer rounded-2xl border border-border bg-card shadow-sm transition-all duration-200 hover:shadow-md"
            role="button"
            tabIndex={0}
            aria-label={`Open details for ${toTitleCase(listing.cropName)}`}
            onClick={() => onOpenDetails(listing.id)}
            onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onOpenDetails(listing.id);
                }
            }}
        >
            <ImageCarousel
                images={listing.images}
                alt={`${listing.cropName} listing image`}
                className="relative overflow-hidden rounded-t-2xl bg-muted/40"
                imageClassName="h-52 w-full object-cover"
                prevAriaLabel="Show previous product image"
                nextAriaLabel="Show next product image"
                stopPropagationOnControls
            />

            <div className="space-y-3 p-4">
                <div>
                    <h3 className="truncate text-lg font-bold text-foreground" title={toTitleCase(listing.cropName)}>
                        {toTitleCase(listing.cropName)}
                    </h3>
                    <p className="text-sm text-muted-foreground">
                        Farmer Name: <span className="font-semibold text-foreground">{toTitleCase(listing.farmerName)}</span>
                    </p>
                </div>

                <p className="text-sm text-foreground">
                    <span className="font-semibold">Available: </span>
                    {formatQuantity(listing.quantity)} KG
                </p>

                <p className="text-sm text-foreground">
                    <span className="font-semibold">Price: </span>
                    {formatCurrency(listing.pricePerKg)} per KG
                </p>

                <p className="inline-flex items-start gap-1.5 text-sm text-foreground">
                    <MapPin className="mt-0.5 h-4 w-4 text-primary" />
                    <span className="line-clamp-2">
                        {listing.city}, {listing.state} - {listing.pincode}
                    </span>
                </p>

                <p className="text-xs text-muted-foreground">Listed on: {formatDate(listing.createdAt)}</p>
            </div>
        </article>
    );
}
