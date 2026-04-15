import { Button } from "@/components/ui/button";
import ModalLayout from "@/components/modals/ModalLayout";
import { formatCurrency, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { NegotiationRejectModalProps } from "@/types/modal";

export default function NegotiationRejectModal({
    negotiation,
    currentPrice,
    currentQty,
    onClose,
    actionLoading,
    registerReject,
    handleRejectSubmit,
    isRejectSubmitting,
    onSubmit,
}: NegotiationRejectModalProps) {
    const isLoading = Boolean(actionLoading) || isRejectSubmitting;

    return (
        <ModalLayout
            open={true}
            title="Reject Offer"
            onClose={onClose}
            onBackdropClick={() => !isLoading && onClose()}
            closeButtonDisabled={isLoading}
            closeAriaLabel="Close reject offer modal"
            backdropAriaLabel="Close reject offer modal backdrop"
            maxWidth="md"
        >
            <div className="space-y-2 rounded-xl border border-border/70 bg-background p-3 text-sm">
                <p>
                    <span className="font-semibold">Listing:</span> {toTitleCase(negotiation.listingTitle)}
                </p>
                <p>
                    <span className="font-semibold">Offered Price/KG:</span> {formatCurrency(currentPrice)}
                </p>
                <p>
                    <span className="font-semibold">Requested Qty:</span> {formatQuantity(currentQty)} KG
                </p>
            </div>

            <form className="mt-4" onSubmit={handleRejectSubmit(onSubmit)}>
                <label htmlFor="rejectReason" className="mb-1 block text-sm font-medium text-foreground">
                    Cancellation Reason (Optional)
                </label>
                <textarea
                    id="rejectReason"
                    rows={4}
                    {...registerReject("rejectReason")}
                    className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    placeholder="Enter reason (optional)"
                    disabled={isLoading}
                />
            </form>

            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                <Button type="button" variant="outline" disabled={isLoading} onClick={onClose}>
                    Cancel
                </Button>
                <Button type="button" variant="destructive" disabled={isLoading} onClick={handleRejectSubmit(onSubmit)}>
                    {actionLoading === "reject" || isRejectSubmitting ? "Processing..." : "Submit Reject"}
                </Button>
            </div>
        </ModalLayout>
    );
}