import api from "@/lib/api";
import type { CreateRatingPayload } from "@/types/rating";

export type { CreateRatingPayload } from "@/types/rating";

export const ratingService = {
    createOrderRating: (orderId: number, payload: CreateRatingPayload) =>
        api.post(`/orders/${orderId}/rating`, payload),
};
