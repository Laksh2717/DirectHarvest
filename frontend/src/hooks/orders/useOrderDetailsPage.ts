import { useEffect, useMemo, useState, useCallback } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { orderService } from "@/services/orderService";
import type { CancelOrderFormValues, OrderDetailsResponse, OrderUserRole, UseOrderDetailsPageParams } from "@/types/order";
import { ratingService } from "@/services/ratingService";
import { toast } from "sonner";


const getCounterpartyLabel = (role: OrderUserRole) => (role === "buyer" ? "Farmer" : "Buyer");

const getMakerLabel = (role: OrderUserRole, index: number) => {
    if (role === "buyer") {
        return index % 2 === 0 ? "You" : "Farmer";
    }
    return index % 2 === 1 ? "You" : "Buyer";
};

export function useOrderDetailsPage({ role, viewType, orderId }: UseOrderDetailsPageParams) {
    const router = useRouter();

    const [order, setOrder] = useState<OrderDetailsResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [completeOpen, setCompleteOpen] = useState(false);
    const [cancelOpen, setCancelOpen] = useState(false);
    const [actionLoading, setActionLoading] = useState(false);

    const [isRatingModalOpen, setIsRatingModalOpen] = useState(false);
    const [selectedRating, setSelectedRating] = useState(0);
    const [submittingRating, setSubmittingRating] = useState(false);

    const {
        register: registerCancel,
        handleSubmit: handleCancelSubmit,
        reset: resetCancelForm,
        formState: { isSubmitting: isCancelSubmitting },
    } = useForm<CancelOrderFormValues>({
        defaultValues: { cancellationReason: "" },
    });

    useEffect(() => {
        let isMounted = true;

        const loadOrderDetails = async () => {
            if (!Number.isFinite(orderId)) {
                if (isMounted) {
                    setError("Invalid order id.");
                    setLoading(false);
                }
                return;
            }

            try {
                const data = await orderService.getOrderById(orderId);
                if (isMounted) {
                    setOrder(data);
                    setError(null);
                }
            } catch {
                if (isMounted) {
                    setError("Unable to load this order right now.");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadOrderDetails();

        return () => {
            isMounted = false;
        };
    }, [orderId]);

    useEffect(() => {
        if (!cancelOpen) {
            resetCancelForm({ cancellationReason: "" });
        }
    }, [cancelOpen, resetCancelForm]);

    const totalAmount = useMemo(() => {
        if (!order) {
            return 0;
        }
        return order.agreedPrice * order.agreedQuantity;
    }, [order]);

    const acceptedBy = useMemo(() => {
        if (!order || order.negotiations.length === 0) {
            return getCounterpartyLabel(role);
        }

        const lastIndex = order.negotiations.length - 1;
        const lastMaker = getMakerLabel(role, lastIndex);
        return lastMaker === "You" ? getCounterpartyLabel(role) : "You";
    }, [order, role]);

    const closeRatingModal = useCallback(() => {
        if (submittingRating) {
            return;
        }

        setIsRatingModalOpen(false);
        setSelectedRating(0);
    }, [submittingRating]);

    const handleSubmitRating = useCallback(async () => {
        if (!order) {
            return;
        }

        if (selectedRating <= 0) {
            toast.error("Please select a rating before submitting");
            return;
        }

        setSubmittingRating(true);
        try {
            await ratingService.createOrderRating(order.id, { score: selectedRating });
            setOrder((prev) => (prev ? { ...prev, rated: true, ratingScore: selectedRating } : prev));
            toast.success("Rating submitted successfully");
            closeRatingModal();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Unable to submit rating right now."));
        } finally {
            setSubmittingRating(false);
        }
    }, [closeRatingModal, order, selectedRating]);

    const handleMarkCompleted = useCallback(async () => {
        if (!order) {
            return;
        }

        setActionLoading(true);
        try {
            await orderService.completeOrder(order.id);
            toast.success("Order marked as completed.");
            setCompleteOpen(false);
            router.push(`/${role}/completed-orders`);
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to mark order as completed."));
        } finally {
            setActionLoading(false);
        }
    }, [order, role, router]);

    const handleCancelOrder = useCallback(
        async (data: CancelOrderFormValues) => {
            if (!order) {
                return;
            }

            setActionLoading(true);
            try {
                const trimmedReason = data.cancellationReason.trim();
                await orderService.cancelOrder(order.id, trimmedReason ? { cancellationReason: trimmedReason } : undefined);
                toast.success("Order cancelled successfully.");
                setCancelOpen(false);
                resetCancelForm({ cancellationReason: "" });
                router.push(`/${role}/cancelled-orders`);
            } catch (err: unknown) {
                toast.error(resolveApiErrorMessage(err, "Failed to cancel order."));
            } finally {
                setActionLoading(false);
            }
        },
        [order, resetCancelForm, role, router]
    );

    const openCancelModal = useCallback(() => setCancelOpen(true), []);
    const closeCancelModal = useCallback(() => {
        if (!actionLoading) {
            setCancelOpen(false);
        }
    }, [actionLoading]);

    return {
        order,
        loading,
        error,
        completeOpen,
        cancelOpen,
        actionLoading,
        isRatingModalOpen,
        selectedRating,
        submittingRating,
        acceptedBy,
        totalAmount,
        setCompleteOpen,
        setIsRatingModalOpen,
        setSelectedRating,
        openCancelModal,
        closeCancelModal,
        closeRatingModal,
        handleMarkCompleted,
        handleSubmitRating,
        registerCancel,
        handleCancelSubmit,
        isCancelSubmitting,
        handleCancelOrder,
    };
}