import api from "@/lib/api";
import { buildRepeatedQuery } from "@/lib/api/query";
import type { CancelOrderPayload, OrderDetailsResponse, OrderResponse, OrderStatus } from "@/types/order";

export type { CancelOrderPayload, OrderDetailsResponse, OrderNegotiationEntry, OrderResponse, OrderStatus } from "@/types/order";

export const orderService = {
    getMyOrders: async (statuses: OrderStatus[]) => {
        const query = buildRepeatedQuery("status", statuses);
        const response = await api.get<OrderResponse[]>(`/orders/me?${query}`);
        return response.data;
    },
    getOrderById: async (orderId: number) => {
        const response = await api.get<OrderDetailsResponse>(`/orders/${orderId}`);
        return response.data;
    },
    completeOrder: async (orderId: number) => {
        const response = await api.post<OrderResponse>(`/orders/${orderId}/complete`);
        return response.data;
    },
    cancelOrder: async (orderId: number, payload?: CancelOrderPayload) => {
        const response = await api.post<OrderResponse>(`/orders/${orderId}/cancel`, payload);
        return response.data;
    },
};
