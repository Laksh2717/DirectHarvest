import { ListingStatus } from "@/types/listing";
import { toTitleCase, truncateText } from "./formatters";
import { NegotiationEventResponse, NegotiationStatusFilter } from "@/services/negotiationService";
import { UserRole } from "@/types/common";
import { OrderResponse } from "@/types/order";
import { SortConfig, SortOption } from "@/types/browse";

type OrderViewType = "active" | "completed" | "cancelled";
type LowercaseUserRole = "buyer" | "farmer";

export const getStatusBadgeStyle = (status: ListingStatus) => {
    if (status === "ACTIVE") {
        return "bg-green-100 text-green-800";
    }
    if (status === "OUT_OF_STOCK") {
        return "bg-orange-100 text-orange-800";
    }
    return "bg-gray-100 text-gray-800";
};

export const getStatusLabel = (status: ListingStatus) => {
    if (status === "OUT_OF_STOCK") {
        return "Out of Stock";
    }
    return toTitleCase(status);
};

export const getNegotiationStatusLabel = (status: NegotiationStatusFilter) =>
    status
        .toLowerCase()
        .split("_")
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");

export const getEventTitle = (eventType: NegotiationEventResponse["eventType"]) => {
    switch (eventType) {
        case "CREATED":
            return "Offer Created";
        case "COUNTERED":
            return "Counter Offer";
        case "ACCEPTED":
            return "Offer Accepted";
        case "REJECTED":
            return "Offer Rejected";
        case "EXPIRED":
            return "Offer Expired";
    }
};

export const getTurnLabel = (status: NegotiationStatusFilter) => {
    if (status === "PENDING_BUYER") {
        return "Buyer turn";
    }
    if (status === "PENDING_FARMER") {
        return "Farmer turn";
    }
    return null;
};

export const getBackLabel = (viewType: OrderViewType) => {
    if (viewType === "active") {
        return "Back to Active Orders";
    }
    if (viewType === "completed") {
        return "Back to Completed Orders";
    }
    return "Back to Cancelled Orders";
};

export const getCancelledByLabel = (role: LowercaseUserRole | UserRole, cancelledBy: string | null) => {
    const normalizedRole = role.toLowerCase();

    if (cancelledBy === "SYSTEM") {
        return "System";
    }

    if (normalizedRole === "buyer") {
        return cancelledBy === "BUYER" ? "You" : "Farmer";
    }

    return cancelledBy === "FARMER" ? "You" : "Buyer";
};

export const getOrderRowClassName = (canNavigate: boolean) =>
    `transition-colors hover:bg-muted/30 ${canNavigate ? "cursor-pointer" : ""}`;

export type OrderTableCounterpart = "buyer" | "farmer";

export const getCounterpartLabel = (counterpart: OrderTableCounterpart) =>
    counterpart === "buyer" ? "Buyer" : "Farmer";

export const getOrderRowDisplayData = (order: OrderResponse, counterpart: OrderTableCounterpart) => {
    const counterpartName = counterpart === "buyer" ? order.buyerName : order.farmerName;

    return {
        displayOrderId: truncateText(order.displayOrderId, 14),
        displayOrderIdTitle: order.displayOrderId,
        listingTitle: truncateText(toTitleCase(order.listingTitle), 28),
        listingTitleRaw: toTitleCase(order.listingTitle),
        counterpartName: truncateText(toTitleCase(counterpartName), 20),
        counterpartNameRaw: toTitleCase(counterpartName),
    };
};

export const getCancelLabel = ({
    cancelledBy,
    currentUserRole,
}: {
    cancelledBy: OrderResponse["cancelledBy"];
    currentUserRole: string | null;
}) => {
    if (cancelledBy === currentUserRole) {
        return "You";
    }
    if (cancelledBy === "SYSTEM") {
        return "System";
    }
    if (cancelledBy === "BUYER") {
        return "Buyer";
    }
    if (cancelledBy === "FARMER") {
        return "Farmer";
    }
    return "-";
};

export const getSortConfig = (option: SortOption): SortConfig => {
    switch (option) {
        case "LATEST":
            return { sortBy: "LISTING_DATE", sortDir: "DESC" };
        case "PRICE_LOW":
            return { sortBy: "PRICE", sortDir: "ASC" };
        case "PRICE_HIGH":
            return { sortBy: "PRICE", sortDir: "DESC" };
        case "RATING_LOW":
            return { sortBy: "FARMER_RATING", sortDir: "ASC" };
        case "RATING_HIGH":
            return { sortBy: "FARMER_RATING", sortDir: "DESC" };
    }
};