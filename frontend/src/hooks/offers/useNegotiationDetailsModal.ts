import { useCallback, useEffect, useMemo, useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { toast } from "sonner";
import { negotiationService, type NegotiationEventResponse, type NegotiationResponse } from "@/services/negotiationService";
import { listingService } from "@/services/listingService";

type DashboardRole = "buyer" | "farmer";

type UseNegotiationDetailsModalParams = {
    open: boolean;
    negotiationId: number | null;
    role: DashboardRole;
    onClose: () => void;
    onActionComplete?: (target: "ACTIVE" | "REJECTED" | "ACTIVE_ORDERS") => void;
};


export function useNegotiationDetailsModal({ open, negotiationId, role, onClose, onActionComplete }: UseNegotiationDetailsModalParams) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [negotiation, setNegotiation] = useState<NegotiationResponse | null>(null);
    const [history, setHistory] = useState<NegotiationEventResponse[]>([]);
    const [availableQuantity, setAvailableQuantity] = useState<number | null>(null);

    const [acceptOpen, setAcceptOpen] = useState(false);
    const [rejectOpen, setRejectOpen] = useState(false);
    const [counterOpen, setCounterOpen] = useState(false);
    const [actionLoading, setActionLoading] = useState<"accept" | "reject" | "counter" | null>(null);

    const handleClose = useCallback(() => {
        onClose();
    }, [onClose]);

    useEffect(() => {
        if (!open || !negotiationId) {
            return;
        }

        let isMounted = true;

        const loadDetails = async () => {
            setLoading(true);
            setError(null);
            try {
                const details = await negotiationService.getNegotiationById(negotiationId);
                const [events, listing] = await Promise.all([
                    negotiationService.getNegotiationHistory(negotiationId),
                    listingService.getListingById(details.listingId),
                ]);

                if (!isMounted) {
                    return;
                }

                setNegotiation(details);
                setHistory(events);
                setAvailableQuantity(listing.quantity);
            } catch {
                if (isMounted) {
                    setError("Unable to load negotiation details right now.");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadDetails();

        return () => {
            isMounted = false;
        };
    }, [open, negotiationId]);

    useEffect(() => {
        if (!open) {
            setAcceptOpen(false);
            setRejectOpen(false);
            setCounterOpen(false);
            setActionLoading(null);
            setAvailableQuantity(null);
            setNegotiation(null);
            setHistory([]);
            setError(null);
            setLoading(false);
        }
    }, [open]);

    const isMyTurn = useMemo(() => {
        if (!negotiation) {
            return false;
        }

        return (
            (role === "buyer" && negotiation.status === "PENDING_BUYER") ||
            (role === "farmer" && negotiation.status === "PENDING_FARMER")
        );
    }, [negotiation, role]);

    const actionModalOpen = acceptOpen || rejectOpen || counterOpen;
    const currentPrice = negotiation?.offeredPrice ?? 0;
    const currentQty = negotiation?.requestedQuantity ?? 0;

    const closeActionModals = useCallback(() => {
        if (!actionLoading) {
            setAcceptOpen(false);
            setRejectOpen(false);
            setCounterOpen(false);
        }
    }, [actionLoading]);

    const openAcceptModal = useCallback(() => {
        if (!actionLoading) {
            setAcceptOpen(true);
            setRejectOpen(false);
            setCounterOpen(false);
        }
    }, [actionLoading]);

    const openRejectModal = useCallback(() => {
        if (!actionLoading) {
            setRejectOpen(true);
            setAcceptOpen(false);
            setCounterOpen(false);
        }
    }, [actionLoading]);

    const openCounterModal = useCallback(() => {
        if (!actionLoading) {
            setCounterOpen(true);
            setAcceptOpen(false);
            setRejectOpen(false);
        }
    }, [actionLoading]);

    const handleAccept = useCallback(async () => {
        if (!negotiation) {
            return;
        }

        setActionLoading("accept");
        try {
            await negotiationService.acceptNegotiation(negotiation.id);
            toast.success("Offer accepted successfully.");
            setAcceptOpen(false);
            handleClose();
            onActionComplete?.("ACTIVE_ORDERS");
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to accept offer."));
        } finally {
            setActionLoading(null);
        }
    }, [handleClose, negotiation, onActionComplete]);

    const handleReject = useCallback(async (data: { rejectReason: string }) => {
        if (!negotiation) {
            return;
        }

        setActionLoading("reject");
        try {
            const trimmedReason = data.rejectReason.trim();
            await negotiationService.rejectNegotiation(
                negotiation.id,
                trimmedReason ? { cancellationReason: trimmedReason } : undefined,
            );
            toast.success("Offer rejected successfully.");
            setRejectOpen(false);
            handleClose();
            onActionComplete?.("REJECTED");
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to reject offer."));
        } finally {
            setActionLoading(null);
        }
    }, [handleClose, negotiation, onActionComplete]);

    const handleCounterSubmit = useCallback(async (data: { counterPrice: string; counterQty: string }) => {
        if (!negotiation) {
            return;
        }

        const parsedPrice = Number(data.counterPrice.trim());
        const parsedQty = Number(data.counterQty.trim());

        setActionLoading("counter");
        try {
            await negotiationService.counterOffer(negotiation.id, {
                offeredPrice: parsedPrice,
                requestedQuantity: parsedQty,
            });
            toast.success("Counter offer submitted successfully.");
            setCounterOpen(false);
            handleClose();
            onActionComplete?.("ACTIVE");
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to submit counter offer."));
        } finally {
            setActionLoading(null);
        }
    }, [handleClose, negotiation, onActionComplete]);

    return {
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
    };
}