import { Dispatch, ReactNode, SetStateAction } from "react";

export type OrderStatus = "CONFIRMED" | "ACTIVE" | "COMPLETED" | "CANCELLED";

export interface OrderResponse {
    id: number;
    displayOrderId: string;
    listingId: number;
    listingTitle: string;
    negotiationId: number;
    buyerId: number;
    buyerName: string;
    farmerId: number;
    farmerName: string;
    agreedPrice: number;
    agreedQuantity: number;
    status: OrderStatus;
    cancelledBy: "BUYER" | "FARMER" | "SYSTEM" | null;
    cancelledAt: string | null;
    completedAt: string | null;
    rated: boolean;
    ratingScore: number | null;
    createdAt: string;
}

export interface OrderNegotiationEntry {
    price: number;
    qty: number;
    createdAt: string;
}

export interface OrderDetailsResponse {
    id: number;
    displayOrderId: string;
    listingId: number;
    listingTitle: string;
    listingDescription: string | null;
    listingStreet: string;
    listingCity: string;
    listingState: string;
    listingPincode: string;
    negotiationId: number;
    buyerId: number;
    buyerName: string;
    buyerEmail: string;
    farmerId: number;
    farmerName: string;
    farmerEmail: string;
    farmerAverageRating: number | null;
    farmerRatingCount: number | null;
    agreedPrice: number;
    agreedQuantity: number;
    status: OrderStatus;
    cancelledBy: "BUYER" | "FARMER" | "SYSTEM" | null;
    cancelledReason: string | null;
    cancelledAt: string | null;
    activatedAt: string | null;
    completedAt: string | null;
    rated: boolean;
    ratingScore: number | null;
    createdAt: string;
    updatedAt: string;
    negotiations: OrderNegotiationEntry[];
}

export interface CancelOrderPayload {
    cancellationReason?: string;
}

export type CancelOrderFormValues = {
    cancellationReason: string;
};

export type OrderUserRole = "buyer" | "farmer";
export type OrderViewType = "active" | "completed" | "cancelled";

export type UseOrderDetailsPageParams = {
    role: OrderUserRole;
    viewType: OrderViewType;
    orderId: number;
};

export type OrderTableRowType = "active" | "cancelled" | "completed";
export type OrderCounterpart = "buyer" | "farmer";

export type UseOrdersTableParams = {
    statuses: OrderResponse["status"][];
    errorMessage: string;
};

export type UseCompletedOrderRatingParams = {
    setOrders: Dispatch<SetStateAction<OrderResponse[]>>;
};

export interface ActiveOrdersTableProps {
    counterpart: OrderCounterpart;
    detailsRouteBase?: string;
}

export type CancelledOrdersTableProps = {
    counterpart: OrderCounterpart;
    detailsRouteBase?: string;
};

export type CompletedOrdersTableProps = {
    counterpart: OrderCounterpart;
    detailsRouteBase?: string;
};

export type OrderDetailsPageProps = {
    role: OrderUserRole;
    viewType: OrderViewType;
};

export type OrdersTableLayoutProps = {
    loading: boolean;
    loadingMessage: string;
    error: string | null;
    hasRows: boolean;
    emptyIcon: ReactNode;
    emptyTitle: string;
    emptyMessage: string;
    children: ReactNode;
};

export type OrderTableRowProps = {
    order: OrderResponse;
    counterpart: OrderCounterpart;
    type: OrderTableRowType;
    canNavigate: boolean;
    onNavigate?: (orderId: number) => void;
    onRateClick?: (order: OrderResponse) => void;
    cancelledByLabel?: string;
    displayData: {
        displayOrderId: string;
        displayOrderIdTitle: string;
        listingTitle: string;
        listingTitleRaw: string;
        counterpartName: string;
        counterpartNameRaw: string;
    };
};