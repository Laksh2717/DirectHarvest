"use client";

import { EllipsisVertical, MapPin } from "lucide-react";
import ImageCarousel from "@/components/ui/image-carousel";
import { formatCurrency, formatDateTime, formatQuantity, toTitleCase } from "@/lib/formatters";
import { useFarmerListingCard } from "@/hooks/listings/useFarmerListingCard";
import { useListingActionsMenu } from "@/hooks/listings/useListingActionsMenu";
import ListingActionsMenu from "./ListingActionsMenu";
import ListingActionModals from "./ListingActionModals";
import { getStatusBadgeStyle, getStatusLabel } from "@/lib/badges";
import type { FarmerListingCardProps } from "@/types/listing";

export default function FarmerListingCard({ listing, onListingUpdated, onListingDeleted }: FarmerListingCardProps) {
    const state = useFarmerListingCard({ listing, onListingUpdated, onListingDeleted });
    const { isOpen, menuPosition, buttonRef, menuRef, closeMenu, toggleMenu } = useListingActionsMenu();

    const address = `${listing.street}, ${listing.city}, ${listing.state} - ${listing.pincode}`;

    const openAddQuantity = () => {
        closeMenu();
        state.addQtyForm.onOpen();
    };

    const openUpdatePrice = () => {
        closeMenu();
        state.updatePriceForm.onOpen();
    };

    const openUpdateDetails = () => {
        closeMenu();
        state.updateDetailsForm.onOpen();
    };

    const openUpdateImages = () => {
        closeMenu();
        state.imageState.onOpen();
    };

    const openInactiveConfirm = () => {
        closeMenu();
        state.modals.setIsInactiveConfirmOpen(true);
    };

    const openDeleteConfirm = () => {
        closeMenu();
        state.modals.setIsDeleteConfirmOpen(true);
    };

    return (
        <>
            <article className={`group relative ${isOpen ? "z-50" : "z-0"}`}>
            <div className="rounded-2xl border border-border bg-card shadow-(--shadow-card) transition-all duration-200 hover:shadow-(--shadow-elevated)">
                <ImageCarousel
                    images={listing.images}
                    alt={`${listing.cropName} listing image`}
                    className="relative overflow-hidden rounded-t-2xl bg-muted/40"
                    imageClassName="h-52 w-full object-cover"
                    prevAriaLabel="Show previous listing image"
                    nextAriaLabel="Show next listing image"
                />

                <div className="space-y-3 p-4">
                    <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                            <h3 className="truncate text-lg font-bold text-foreground" title={toTitleCase(listing.cropName)}>
                                {toTitleCase(listing.cropName)}
                            </h3>
                        </div>

                        <div className="relative z-50 flex shrink-0 items-center gap-2">
                            <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${getStatusBadgeStyle(listing.status)}`}>
                                {getStatusLabel(listing.status)}
                            </span>
                            {listing.status !== "INACTIVE" ? (
                                <button
                                ref={buttonRef}
                                type="button"
                                aria-label="Open listing actions"
                                onClick={toggleMenu}
                                className="rounded-full border border-border p-1.5 text-muted-foreground transition hover:bg-muted"
                            >
                                <EllipsisVertical className="h-4 w-4" />
                            </button>
                            ): null}
                        </div>
                    </div>

                    <p className="text-sm text-foreground">
                        <span className="font-semibold">Quantity: </span>
                        {formatQuantity(listing.quantity)} KG
                    </p>

                    <p className="text-sm text-foreground">
                        <span className="font-semibold">Price / Kg: </span>
                        {formatCurrency(listing.pricePerKg)}
                    </p>

                    <p className="inline-flex items-start gap-1.5 text-sm text-foreground">
                        <MapPin className="mt-0.5 h-4 w-4 text-primary" />
                        <span className="line-clamp-2">{address}</span>
                    </p>

                    <p className="text-xs text-muted-foreground">Created on: {formatDateTime(listing.createdAt)}</p>
                </div>
            </div>
            </article>
            <ListingActionsMenu
                status={listing.status}
                isOpen={isOpen}
                menuPosition={menuPosition}
                menuRef={menuRef}
                onAddQty={openAddQuantity}
                onUpdatePrice={openUpdatePrice}
                onUpdateDetails={openUpdateDetails}
                onUpdateImages={openUpdateImages}
                onMakeInactive={openInactiveConfirm}
                onDelete={openDeleteConfirm}
            />

            <ListingActionModals listing={listing} state={state} />
        </>
    );
}
