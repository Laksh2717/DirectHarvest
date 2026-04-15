import { Button } from "@/components/ui/button";
import ModalLayout from "@/components/modals/ModalLayout";
import type { OrderCancelModalProps } from "@/types/modal";

export default function OrderCancelModal({
    open,
    loading,
    isSubmitting,
    onClose,
    onSubmit,
    registerCancel,
}: OrderCancelModalProps) {
    return (
        <ModalLayout
            open={open}
            title="Cancel Order"
            onClose={onClose}
            closeAriaLabel="Close cancel order modal"
            backdropAriaLabel="Close cancel order modal backdrop"
            maxWidth="md"
        >
            <form className="mt-3" onSubmit={onSubmit}>
                <p className="text-sm text-muted-foreground">Add a cancellation reason (optional).</p>
                <textarea
                    className="mt-3 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    rows={4}
                    placeholder="Enter reason (optional)"
                    {...registerCancel("cancellationReason")}
                    disabled={loading || isSubmitting}
                />
                <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <Button type="button" variant="outline" onClick={onClose} disabled={loading || isSubmitting}>
                        Close
                    </Button>
                    <Button type="submit" disabled={loading || isSubmitting}>
                        {loading || isSubmitting ? "Cancelling..." : "Submit"}
                    </Button>
                </div>
            </form>
        </ModalLayout>
    );
}
