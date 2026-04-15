import { FieldErrors, UseFormHandleSubmit, UseFormRegister } from "react-hook-form";
import { ListingResponse } from "./listing";

export type NegotiationRole = "buyer" | "farmer";

export interface CreateNegotiationPayload {
    listingId: number;
    offeredPrice: number;
    requestedQuantity: number;
}

export interface CounterOfferPayload {
    offeredPrice: number;
    requestedQuantity: number;
}

export interface RejectNegotiationPayload {
    cancellationReason?: string;
}

export type NegotiationStatusFilter = "PENDING_FARMER" | "PENDING_BUYER" | "ACCEPTED" | "REJECTED" | "EXPIRED";

export type NegotiationEventType = "CREATED" | "COUNTERED" | "ACCEPTED" | "REJECTED" | "EXPIRED";

export interface NegotiationResponse {
    id: number;
    listingId: number;
    listingTitle: string;
    buyerId: number;
    buyerName: string;
    buyerEmail: string;
    farmerId: number;
    farmerName: string;
    farmerEmail: string;
    requestedQuantity: number;
    offeredPrice: number;
    status: NegotiationStatusFilter;
    proposedBy: "BUYER" | "FARMER";
    cancellationReason: string | null;
    cancelledBy: "BUYER" | "FARMER" | "ADMIN" | null;
    expiresAt: string;
    createdAt: string;
    updatedAt: string;
}

export interface NegotiationEventResponse {
    id: number;
    negotiationId: number;
    eventType: NegotiationEventType;
    actorId: number | null;
    actorName: string | null;
    actorRole: "BUYER" | "FARMER" | "ADMIN" | null;
    offeredPrice: number;
    requestedQuantity: number;
    statusAfter: NegotiationStatusFilter;
    createdAt: string;
}

export type ActionFormValues = {
    rejectReason: string;
    counterPrice: string;
    counterQty: string;
};

export type UseNegotiationActionFormsParams = {
    open: boolean;
    rejectOpen: boolean;
    counterOpen: boolean;
    negotiation: NegotiationResponse | null;
    availableQuantity: number | null;
};

export type UseNegotiationDetailsModalParams = {
    open: boolean;
    negotiationId: number | null;
    role: NegotiationRole;
    onClose: () => void;
    onActionComplete?: (target: "ACTIVE" | "REJECTED" | "ACTIVE_ORDERS") => void;
};

export type NegotiationsTab = "ACTIVE" | "REJECTED" | "EXPIRED";

export type UseNegotiationsTableParams = {
    role: NegotiationRole;
};

export interface PlaceOfferModalProps {
    open: boolean;
    listing: ListingResponse | null;
    submitting: boolean;
    onClose: () => void;
    onSubmit: (payload: { requestedQuantity: number; offeredPrice: number }) => void;
}

export type PlaceOfferFormValues = {
    requestedQuantity: number;
    offeredPrice: number;
};  

export type NegotiationActionFormValues = {
    rejectReason: string;
    counterPrice: string;
    counterQty: string;
};

export type NegotiationActionModalsProps = {
    open: boolean;
    negotiation: NegotiationResponse;
    currentPrice: number;
    currentQty: number;
    availableQuantity: number | null;
    actionLoading: "accept" | "reject" | "counter" | null;
    acceptOpen: boolean;
    rejectOpen: boolean;
    counterOpen: boolean;
    onClose: () => void;
    onAccept: () => void;
    onReject: (data: { rejectReason: string }) => void;
    onCounter: (data: { counterPrice: string; counterQty: string }) => void;
    registerReject: UseFormRegister<Pick<NegotiationActionFormValues, "rejectReason">>;
    handleRejectSubmit: UseFormHandleSubmit<Pick<NegotiationActionFormValues, "rejectReason">>;
    isRejectSubmitting: boolean;
    registerCounter: UseFormRegister<Pick<NegotiationActionFormValues, "counterPrice" | "counterQty">>;
    handleCounterFormSubmit: UseFormHandleSubmit<Pick<NegotiationActionFormValues, "counterPrice" | "counterQty">>;
    counterErrors: FieldErrors<Pick<NegotiationActionFormValues, "counterPrice" | "counterQty">>;
    isCounterSubmitting: boolean;
};

export interface NegotiationDetailsModalProps {
    open: boolean;
    negotiationId: number | null;
    role: NegotiationRole;
    onClose: () => void;
    onActionComplete?: (target: "ACTIVE" | "REJECTED" | "ACTIVE_ORDERS") => void;
}

export type NegotiationHistorySectionProps = {
    role: NegotiationRole;
    negotiation: NegotiationResponse;
    history: NegotiationEventResponse[];
    isMyTurn: boolean;
    onOpenAccept: () => void;
    onOpenReject: () => void;
    onOpenCounter: () => void;
    actionBusy: boolean;
};

export interface NegotiationsTableProps {
    role: NegotiationRole;
}