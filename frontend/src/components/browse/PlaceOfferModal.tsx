"use client";

import { useCallback, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { Loader2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { formatCurrency, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { PlaceOfferModalProps } from "@/types/offer";

export default function PlaceOfferModal({ open, listing, submitting, onClose, onSubmit }: PlaceOfferModalProps) {
    const [requestedQuantity, setRequestedQuantity] = useState<string>("");
    const [offeredPrice, setOfferedPrice] = useState<string>("");
    const [errors, setErrors] = useState<Record<string, string>>({ });
    const [isSubmitting, setIsSubmitting] = useState(false);

    const resetFormState = useCallback(() => {
        setRequestedQuantity("");
        setOfferedPrice("");
        setErrors({});
        setIsSubmitting(false);
    }, []);

    const handleClose = useCallback(() => {
        if (submitting || isSubmitting) {
            return;
        }
        resetFormState();
        onClose();
    }, [onClose, resetFormState, submitting, isSubmitting]);

    useEffect(() => {
        if (!open) {
            return;
        }

        resetFormState();

        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape" && !submitting && !isSubmitting) {
                handleClose();
            }
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [handleClose, open, resetFormState, submitting, isSubmitting]);

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};

        const qty = requestedQuantity ? Number(requestedQuantity) : null;
        if (!requestedQuantity || qty === null || !Number.isFinite(qty) || qty <= 0) {
            newErrors.requestedQuantity = "Required quantity must be greater than 0.";
        }
        if (listing && qty && qty > listing.quantity) {
            newErrors.requestedQuantity = "Required quantity cannot be greater than available quantity.";
        }

        const price = offeredPrice ? Number(offeredPrice) : null;
        if (!offeredPrice || price === null || !Number.isFinite(price) || price <= 0) {
            newErrors.offeredPrice = "Offered price must be greater than 0.";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleFormSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);
        try {
            const qty = Number(requestedQuantity);
            const price = Number(offeredPrice);

            console.log("Form Values:", { requestedQuantity, offeredPrice, qty, price });

            onSubmit({
                requestedQuantity: qty,
                offeredPrice: price,
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!open || !listing) {
        return null;
    }

    return createPortal(
        <div className="fixed inset-0 z-1010 flex items-center justify-center px-4">
            <button
                type="button"
                aria-label="Close place offer backdrop"
                className="absolute inset-0 bg-black/50"
                onClick={() => {
                    handleClose();
                }}
            />

            <div className="relative z-10 w-full max-w-xl rounded-2xl border border-border bg-card p-5 shadow-(--shadow-elevated) sm:p-6">
                <div className="mb-4 flex items-start justify-between gap-3">
                    <h2 className="text-lg font-semibold text-foreground">Place Offer</h2>
                    <button
                        type="button"
                        onClick={handleClose}
                        className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:opacity-50"
                        aria-label="Close place offer modal"
                        disabled={submitting}
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <div className="space-y-2 rounded-lg border border-border bg-muted/20 p-4 text-sm">
                    <p><span className="font-semibold">Crop Name:</span> {toTitleCase(listing.cropName)}</p>
                    <p><span className="font-semibold">Farmer Name:</span> {toTitleCase(listing.farmerName)}</p>
                    <p><span className="font-semibold">Available Qty:</span> {formatQuantity(listing.quantity)} KG</p>
                    <p><span className="font-semibold">Current Price:</span> {formatCurrency(listing.pricePerKg)} /KG</p>
                </div>

                <form className="mt-4 space-y-4" onSubmit={handleFormSubmit}>
                    <div className="space-y-1.5">
                        <label htmlFor="requestedQuantity" className="text-sm font-medium text-foreground">
                            Required Quantity (KG)
                        </label>
                        <Input
                            id="requestedQuantity"
                            type="number"
                            inputMode="decimal"
                            min="0"
                            step="0.01"
                            placeholder="Enter required quantity"
                            value={requestedQuantity}
                            onChange={(e) => setRequestedQuantity(e.target.value)}
                            onBlur={() => validateForm()}
                            disabled={submitting || isSubmitting}
                            aria-invalid={Boolean(errors.requestedQuantity)}
                            className={errors.requestedQuantity ? "border-destructive focus-visible:ring-destructive" : undefined}
                        />
                        {errors.requestedQuantity ? <p className="text-xs text-destructive">{errors.requestedQuantity}</p> : null}
                    </div>

                    <div className="space-y-1.5">
                        <label htmlFor="offeredPrice" className="text-sm font-medium text-foreground">
                            Offered Price Per KG
                        </label>
                        <Input
                            id="offeredPrice"
                            type="number"
                            inputMode="decimal"
                            min="0"
                            step="0.01"
                            placeholder="Enter your offer price"
                            value={offeredPrice}
                            onChange={(e) => setOfferedPrice(e.target.value)}
                            onBlur={() => validateForm()}
                            disabled={submitting || isSubmitting}
                            aria-invalid={Boolean(errors.offeredPrice)}
                            className={errors.offeredPrice ? "border-destructive focus-visible:ring-destructive" : undefined}
                        />
                        {errors.offeredPrice ? <p className="text-xs text-destructive">{errors.offeredPrice}</p> : null}
                    </div>

                    <div className="flex flex-col-reverse gap-3 pt-1 sm:flex-row sm:justify-end">
                        <Button type="button" variant="outline" onClick={handleClose} disabled={submitting || isSubmitting}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={submitting || isSubmitting}>
                            {submitting || isSubmitting ? (
                                <span className="inline-flex items-center gap-2">
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Submitting...
                                </span>
                            ) : (
                                "Submit Offer"
                            )}
                        </Button>
                    </div>
                </form>
            </div>
        </div>,
        document.body,
    );
}
