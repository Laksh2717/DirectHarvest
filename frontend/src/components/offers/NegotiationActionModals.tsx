import { type NegotiationActionModalsProps } from "@/types/offer";
import NegotiationAcceptModal from "../modals/NegotiationAcceptModal";
import NegotiationRejectModal from "../modals/NegotiationRejectModal";
import NegotiationCounterModal from "../modals/NegotiationCounterModal";

export default function NegotiationActionModals({
    open,
    negotiation,
    currentPrice,
    currentQty,
    availableQuantity,
    actionLoading,
    acceptOpen,
    rejectOpen,
    counterOpen,
    onClose,
    onAccept,
    onReject,
    onCounter,
    registerReject,
    handleRejectSubmit,
    isRejectSubmitting,
    registerCounter,
    handleCounterFormSubmit,
    counterErrors,
    isCounterSubmitting,
}: NegotiationActionModalsProps) {
    if (!open) {
        return null;
    }

    return (
        <>
            {acceptOpen ? (
                <NegotiationAcceptModal
                    negotiation={negotiation}
                    currentPrice={currentPrice}
                    currentQty={currentQty}
                    onClose={onClose}
                    actionLoading={actionLoading}
                    onConfirm={onAccept}
                />
            ) : null}

            {rejectOpen ? (
                <NegotiationRejectModal
                    negotiation={negotiation}
                    currentPrice={currentPrice}
                    currentQty={currentQty}
                    onClose={onClose}
                    actionLoading={actionLoading}
                    registerReject={registerReject}
                    handleRejectSubmit={handleRejectSubmit}
                    isRejectSubmitting={isRejectSubmitting}
                    onSubmit={onReject}
                />
            ) : null}

            {counterOpen ? (
                <NegotiationCounterModal
                    negotiation={negotiation}
                    currentPrice={currentPrice}
                    currentQty={currentQty}
                    availableQuantity={availableQuantity}
                    onClose={onClose}
                    actionLoading={actionLoading}
                    registerCounter={registerCounter}
                    handleCounterFormSubmit={handleCounterFormSubmit}
                    counterErrors={counterErrors}
                    isCounterSubmitting={isCounterSubmitting}
                    onSubmit={onCounter}
                />
            ) : null}
        </>
    );
}
