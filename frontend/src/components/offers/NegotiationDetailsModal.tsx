"use client";

import { useEffect } from "react";
import { createPortal } from "react-dom";
import { X } from "lucide-react";
import LoadingState from "@/components/ui/loading-state";
import { useNegotiationActionForms } from "@/hooks/offers/useNegotiationActionForms";
import { useNegotiationDetailsModal } from "../../hooks/offers/useNegotiationDetailsModal";
import NegotiationHistorySection from "./NegotiationHistorySection";
import NegotiationActionModals from "./NegotiationActionModals";
import type { NegotiationDetailsModalProps } from "@/types/offer";

export default function NegotiationDetailsModal({ open, negotiationId, role, onClose, onActionComplete }: NegotiationDetailsModalProps) {
    const {
        loading,
        error,
        negotiation,
        history,
        availableQuantity,
        acceptOpen,
        rejectOpen,
        counterOpen,
        actionLoading,
        isMyTurn,
        actionModalOpen,
        currentPrice,
        currentQty,
        handleClose,
        closeActionModals,
        openAcceptModal,
        openRejectModal,
        openCounterModal,
        handleAccept,
        handleReject,
        handleCounterSubmit,
    } = useNegotiationDetailsModal({ open, negotiationId, role, onClose, onActionComplete });

    const {
        registerReject,
        handleRejectSubmit,
        isRejectSubmitting,
        registerCounter,
        handleCounterFormSubmit,
        counterErrors,
        isCounterSubmitting,
    } = useNegotiationActionForms({ open, rejectOpen, counterOpen, negotiation, availableQuantity });

    useEffect(() => {
        if (!open) {
            return;
        }

        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                handleClose();
            }
        };

        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [handleClose, open]);

    if (!open || !negotiationId) {
        return null;
    }

    return createPortal(
        <div className="fixed inset-0 z-1010 flex items-center justify-center px-4 py-6">
            {!actionModalOpen ? (
                <>
                    <button
                        type="button"
                        aria-label="Close negotiation details backdrop"
                        className="absolute inset-0 bg-black/50"
                        onClick={handleClose}
                    />

                    <div className="relative z-10 flex max-h-[92vh] w-full sm:max-w-136 flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-(--shadow-elevated)">
                        <div className="flex items-start justify-between border-b border-border px-5 py-4 sm:px-6">
                            <div>
                                <h2 className="text-lg font-semibold text-foreground">Negotiation Details</h2>
                            </div>

                            <button
                                type="button"
                                aria-label="Close negotiation details"
                                className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                                onClick={handleClose}
                            >
                                <X className="h-4 w-4" />
                            </button>
                        </div>

                        <div className="overflow-y-auto px-5 py-4 sm:px-6 sm:py-5">
                            {loading ? (
                                <LoadingState
                                    layout="inline"
                                    message="Loading negotiation history..."
                                    className="flex min-h-65 items-center justify-center"
                                    cardClassName="max-w-none border-0 bg-transparent p-0 shadow-none"
                                />
                            ) : null}

                            {!loading && error ? (
                                <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm font-medium text-destructive">
                                    {error}
                                </div>
                            ) : null}

                            {!loading && !error && negotiation ? (
                                <NegotiationHistorySection
                                    role={role}
                                    negotiation={negotiation}
                                    history={history}
                                    isMyTurn={isMyTurn}
                                    onOpenAccept={openAcceptModal}
                                    onOpenReject={openRejectModal}
                                    onOpenCounter={openCounterModal}
                                    actionBusy={Boolean(actionLoading)}
                                />
                            ) : null}
                        </div>
                    </div>
                </>
            ) : null}

            {negotiation ? (
                <NegotiationActionModals
                    open={Boolean(open && negotiation)}
                    negotiation={negotiation}
                    currentPrice={currentPrice}
                    currentQty={currentQty}
                    availableQuantity={availableQuantity}
                    actionLoading={actionLoading}
                    acceptOpen={acceptOpen}
                    rejectOpen={rejectOpen}
                    counterOpen={counterOpen}
                    onClose={closeActionModals}
                    onAccept={handleAccept}
                    onReject={handleReject}
                    onCounter={handleCounterSubmit}
                    registerReject={registerReject}
                    handleRejectSubmit={handleRejectSubmit}
                    isRejectSubmitting={isRejectSubmitting}
                    registerCounter={registerCounter}
                    handleCounterFormSubmit={handleCounterFormSubmit}
                    counterErrors={counterErrors}
                    isCounterSubmitting={isCounterSubmitting}
                />
            ) : null}
        </div>,
        document.body,
    );
}
