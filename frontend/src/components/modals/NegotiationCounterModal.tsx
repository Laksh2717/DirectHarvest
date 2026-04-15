import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import ModalLayout from "@/components/modals/ModalLayout";
import { formatCurrency, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { NegotiationCounterModalProps } from "@/types/modal";

export default function NegotiationCounterModal({
    negotiation,
    currentPrice,
    currentQty,
    availableQuantity,
    onClose,
    actionLoading,
    registerCounter,
    handleCounterFormSubmit,
    counterErrors,
    isCounterSubmitting,
    onSubmit,
}: NegotiationCounterModalProps) {
    const isLoading = Boolean(actionLoading) || isCounterSubmitting;

    return (
        <ModalLayout
            open={true}
            title="Counter Offer"
            onClose={onClose}
            onBackdropClick={() => !isLoading && onClose()}
            closeButtonDisabled={isLoading}
            closeAriaLabel="Close counter offer modal"
            backdropAriaLabel="Close counter offer modal backdrop"
            maxWidth="md"
        >
            <div className="space-y-2 rounded-xl border border-border/70 bg-background p-3 text-sm">
                <p>
                    <span className="font-semibold">Listing:</span> {toTitleCase(negotiation.listingTitle)}
                </p>
                <p>
                    <span className="font-semibold">Available Qty:</span> {availableQuantity === null ? "-" : `${formatQuantity(availableQuantity)} KG`}
                </p>
                <p>
                    <span className="font-semibold">Current Price/KG:</span> {formatCurrency(currentPrice)}
                </p>
                <p>
                    <span className="font-semibold">Current Qty:</span> {formatQuantity(currentQty)} KG
                </p>
            </div>

            <form className="mt-4 space-y-4" onSubmit={handleCounterFormSubmit(onSubmit)}>
                <div>
                    <label htmlFor="counterPrice" className="mb-1 block text-sm font-medium text-foreground">
                        Offered Price/KG
                    </label>
                    <Input
                        id="counterPrice"
                        type="number"
                        min="0"
                        step="0.01"
                        disabled={isLoading}
                        {...registerCounter("counterPrice", {
                            required: "Offered price is required.",
                            validate: (value) => {
                                const parsed = Number(value.trim());
                                if (!Number.isFinite(parsed) || parsed <= 0) {
                                    return "Offered price must be greater than 0.";
                                }
                                return true;
                            },
                        })}
                        className={counterErrors.counterPrice ? "border-destructive focus-visible:ring-destructive" : undefined}
                    />
                    {counterErrors.counterPrice ? <p className="mt-1 text-xs text-destructive">{counterErrors.counterPrice.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="counterQty" className="mb-1 block text-sm font-medium text-foreground">
                        Required Qty (KG)
                    </label>
                    <Input
                        id="counterQty"
                        type="number"
                        min="0"
                        step="0.01"
                        disabled={isLoading}
                        {...registerCounter("counterQty", {
                            required: "Required quantity is required.",
                            validate: (value) => {
                                const parsed = Number(value.trim());
                                if (!Number.isFinite(parsed) || parsed <= 0) {
                                    return "Required quantity must be greater than 0.";
                                }
                                if (availableQuantity !== null && parsed > availableQuantity) {
                                    return "Required quantity cannot be greater than available quantity.";
                                }
                                return true;
                            },
                        })}
                        className={counterErrors.counterQty ? "border-destructive focus-visible:ring-destructive" : undefined}
                    />
                    {counterErrors.counterQty ? <p className="mt-1 text-xs text-destructive">{counterErrors.counterQty.message}</p> : null}
                </div>
            </form>

            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                <Button type="button" variant="outline" disabled={isLoading} onClick={onClose}>
                    Cancel
                </Button>
                <Button type="button" disabled={isLoading} onClick={handleCounterFormSubmit(onSubmit)}>
                    {actionLoading === "counter" || isCounterSubmitting ? "Submitting..." : "Submit Counter Offer"}
                </Button>
            </div>
        </ModalLayout>
    );
}