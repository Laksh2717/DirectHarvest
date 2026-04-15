import api from "@/lib/api";
import type {
    CounterOfferPayload,
    CreateNegotiationPayload,
    NegotiationEventResponse,
    NegotiationResponse,
    NegotiationStatusFilter,
    RejectNegotiationPayload,
} from "@/types/offer";

export type {
    CounterOfferPayload,
    CreateNegotiationPayload,
    NegotiationEventResponse,
    NegotiationEventType,
    NegotiationResponse,
    NegotiationStatusFilter,
    RejectNegotiationPayload,
} from "@/types/offer";

export const negotiationService = {
    createNegotiation: async (payload: CreateNegotiationPayload) => {
        const response = await api.post<NegotiationResponse>("/negotiations", payload);
        return response.data;
    },
    getMyNegotiations: async (statuses?: NegotiationStatusFilter[]) => {
        const response = await api.get<NegotiationResponse[]>("/negotiations/me", {
            params: statuses?.length ? { status: statuses } : undefined,
        });
        return response.data;
    },
    getNegotiationById: async (negotiationId: number) => {
        const response = await api.get<NegotiationResponse>(`/negotiations/${negotiationId}`);
        return response.data;
    },
    getNegotiationHistory: async (negotiationId: number) => {
        const response = await api.get<NegotiationEventResponse[]>(`/negotiations/${negotiationId}/history`);
        return response.data;
    },
    acceptNegotiation: async (negotiationId: number) => {
        const response = await api.post<NegotiationResponse>(`/negotiations/${negotiationId}/accept`);
        return response.data;
    },
    rejectNegotiation: async (negotiationId: number, payload?: RejectNegotiationPayload) => {
        const response = await api.post<NegotiationResponse>(`/negotiations/${negotiationId}/reject`, payload);
        return response.data;
    },
    counterOffer: async (negotiationId: number, payload: CounterOfferPayload) => {
        const response = await api.post<NegotiationResponse>(`/negotiations/${negotiationId}/counter`, payload);
        return response.data;
    },
};
