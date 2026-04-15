import { useEffect } from "react";
import { createPortal } from "react-dom";
import { Star, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { formatCurrency, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { ListingDetailsModalProps } from "@/types/listing";

export default function ListingDetailsModal({ open, loading, listing, onClose, onPlaceOffer }: ListingDetailsModalProps) {
    useEffect(() => {
        if (!open) {
            return;
        }

        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onClose();
            }
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    if (!open) {
        return null;
    }

    return createPortal(
        <div className="fixed inset-0 z-1000 flex items-center justify-center px-4">
            <button
                type="button"
                aria-label="Close listing details backdrop"
                className="absolute inset-0 bg-black/50"
                onClick={onClose}
            />

            <div className="relative z-10 w-full max-w-xl rounded-2xl border border-border bg-card p-5 shadow-(--shadow-elevated) sm:p-6">
                <div className="mb-4 flex items-start justify-between gap-3">
                    <h2 className="text-lg font-semibold text-foreground">Listing Details</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                        aria-label="Close listing details"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                {loading ? (
                    <LoadingState
                        layout="inline"
                        message="Loading listing details..."
                        className="flex min-h-52 items-center justify-center"
                        cardClassName="max-w-none border-0 bg-transparent p-0 shadow-none"
                    />
                ) : listing ? (
                    <div className="space-y-3">
                        {/* Crop Name */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Crop Name</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">{toTitleCase(listing.cropName)}</p>
                            </div>
                        </div>

                        {/* Available Qty */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Available Qty</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">{formatQuantity(listing.quantity)} KG</p>
                            </div>
                        </div>

                        {/* Price Per KG */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Price Per KG</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">{formatCurrency(listing.pricePerKg)}</p>
                            </div>
                        </div>

                        {/* Description - Only show if exists */}
                        {listing.description?.trim() && (
                            <div className="flex items-start gap-3">
                                <label className="text-xs font-medium text-muted-foreground w-25 mt-2">Description</label>
                                <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                    <p className="text-sm text-foreground">{listing.description}</p>
                                </div>
                            </div>
                        )}

                        {/* Farmer Name */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Farmer Name</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">{toTitleCase(listing.farmerName)}</p>
                            </div>
                        </div>

                        {/* Farmer Email */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Farmer Email</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">{listing.farmerEmail}</p>
                            </div>
                        </div>

                        {/* Farmer Rating */}
                        <div className="flex items-center gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25">Farmer Rating</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2 flex items-center gap-2">
                                <Star className="h-4 w-4 fill-amber-400 text-amber-500" />
                                <p className="text-sm text-foreground">
                                    {listing.farmerRating == null 
                                        ? "Not rated yet" 
                                        : `${Number(listing.farmerRating).toFixed(1)} (${listing.farmerRatingCount ?? 0})`}
                                </p>
                            </div>
                        </div>

                        {/* Address */}
                        <div className="flex items-start gap-3">
                            <label className="text-xs font-medium text-muted-foreground w-25 mt-2">Address</label>
                            <div className="flex-1 rounded border border-border bg-background px-3 py-2">
                                <p className="text-sm text-foreground">
                                    {listing.street}, {listing.city}, {listing.state} {listing.pincode}
                                </p>
                            </div>
                        </div>

                        <div className="pt-2">
                            <Button
                                type="button"
                                className="w-full"
                                onClick={() => onPlaceOffer(listing)}
                            >
                                Place Offer
                            </Button>
                        </div>
                    </div>
                ) : (
                    <EmptyState layout="inline" message="Unable to load listing details." cardClassName="max-w-none p-4" />
                )}
            </div>
        </div>,
        document.body,
    );
}
