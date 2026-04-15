import { Button } from "@/components/ui/button";
import ModalLayout from "@/components/modals/ModalLayout";
import { formatCurrency, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { NegotiationAcceptModalProps } from "@/types/modal";

export default function NegotiationAcceptModal({
    negotiation,
    currentPrice,
    currentQty,
    onClose,
    actionLoading,
    onConfirm,
}: NegotiationAcceptModalProps) {
    const isLoading = Boolean(actionLoading);

    return (
        <ModalLayout
            open={true}
            title="Accept Offer"
            onClose={onClose}
            onBackdropClick={() => !isLoading && onClose()}
            closeButtonDisabled={isLoading}
            closeAriaLabel="Close accept offer modal"
            backdropAriaLabel="Close accept offer modal backdrop"
            maxWidth="sm"
        >
            <p className="text-sm text-muted-foreground">Please confirm these offer details before accepting.</p>

            <div className="mt-4 space-y-2 rounded-xl border border-border/70 bg-background p-3 text-sm">
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

            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                <Button type="button" variant="outline" disabled={isLoading} onClick={onClose}>
                    Cancel
                </Button>
                <Button type="button" disabled={isLoading} onClick={onConfirm}>
                    {actionLoading === "accept" ? "Processing..." : "Confirm Accept"}
                </Button>
            </div>
        </ModalLayout>
    );
}