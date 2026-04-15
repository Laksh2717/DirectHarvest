import { useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { toast } from "sonner";
import { ratingService } from "@/services/ratingService";
import type { OrderResponse, UseCompletedOrderRatingParams } from "@/types/order";

export function useCompletedOrderRating({ setOrders }: UseCompletedOrderRatingParams) {
    const [isRatingModalOpen, setIsRatingModalOpen] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
    const [selectedRating, setSelectedRating] = useState(0);
    const [submittingRating, setSubmittingRating] = useState(false);

    const openRatingModal = (order: OrderResponse) => {
        setSelectedOrder(order);
        setSelectedRating(0);
        setIsRatingModalOpen(true);
    };

    const closeRatingModal = () => {
        if (submittingRating) {
            return;
        }

        setIsRatingModalOpen(false);
        setSelectedOrder(null);
        setSelectedRating(0);
    };

    const handleSubmitRating = async () => {
        if (!selectedOrder) {
            return;
        }

        if (selectedRating <= 0) {
            toast.error("Please select a rating before submitting");
            return;
        }

        setSubmittingRating(true);
        try {
            await ratingService.createOrderRating(selectedOrder.id, { score: selectedRating });

            setOrders((prev) =>
                prev.map((order) =>
                    order.id === selectedOrder.id
                        ? { ...order, rated: true, ratingScore: selectedRating }
                        : order,
                ),
            );

            toast.success("Rating submitted successfully");
            closeRatingModal();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err));
        } finally {
            setSubmittingRating(false);
        }
    };

    return {
        isRatingModalOpen,
        selectedOrder,
        selectedRating,
        submittingRating,
        setSelectedRating,
        openRatingModal,
        closeRatingModal,
        handleSubmitRating,
    };
}
