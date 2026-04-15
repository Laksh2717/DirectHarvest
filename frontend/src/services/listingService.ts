import api from "@/lib/api";
import { buildRepeatedQuery } from "@/lib/api/query";
import type {
    AddListingImagesPayload,
    AddListingQuantityPayload,
    BrowseListingsParams,
    BrowseListingsResponse,
    CloudinaryUploadResponse,
    CreateListingPayload,
    ListingResponse,
    ListingStatus,
    UpdateListingDetailsPayload,
    UpdateListingPricePayload,
} from "@/types/listing";

export type {
    AddListingImagesPayload,
    AddListingQuantityPayload,
    BrowseListingsParams,
    BrowseListingsResponse,
    BrowseListingsSortBy,
    CloudinaryUploadResponse,
    CreateListingPayload,
    ListingImagePayload,
    ListingImageResponse,
    ListingResponse,
    ListingStatus,
    UpdateListingDetailsPayload,
    UpdateListingPricePayload,
} from "@/types/listing";

export const listingService = {
    uploadListingImage: async (file: File) => {
        const formData = new FormData();
        formData.append("file", file);
        const response = await api.post<CloudinaryUploadResponse>("/cloudinary/upload", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        });
        return response.data;
    },
    createListing: async (data: CreateListingPayload) => {
        const response = await api.post<ListingResponse>("/listings", data);
        return response.data;
    },
    browseListings: async (params?: BrowseListingsParams) => {
        const response = await api.get<BrowseListingsResponse>("/listings/browse", {
            params: {
                page: params?.page ?? 0,
                size: 12,
                sortBy: params?.sortBy ?? "LISTING_DATE",
                sortDir: params?.sortDir ?? "DESC",
                ...(params?.search?.trim() && { search: params.search.trim() }),
            },
        });
        return response.data;
    },
    getListingById: async (listingId: number) => {
        const response = await api.get<ListingResponse>(`/listings/${listingId}`);
        return response.data;
    },
    getMyListings: async (statuses?: ListingStatus[]) => {
        const query = buildRepeatedQuery("status", statuses);
        const endpoint = query ? `/listings/me?${query}` : "/listings/me";
        const response = await api.get<ListingResponse[]>(endpoint);
        return response.data;
    },
    addListingQuantity: async (listingId: number, data: AddListingQuantityPayload) => {
        const response = await api.post<ListingResponse>(`/listings/${listingId}/quantity/add`, data);
        return response.data;
    },
    updateListingPrice: async (listingId: number, data: UpdateListingPricePayload) => {
        const response = await api.patch<ListingResponse>(`/listings/${listingId}/price`, data);
        return response.data;
    },
    updateListingDetails: async (listingId: number, data: UpdateListingDetailsPayload) => {
        const response = await api.patch<ListingResponse>(`/listings/${listingId}`, data);
        return response.data;
    },
    addListingImages: async (listingId: number, data: AddListingImagesPayload) => {
        const response = await api.post<ListingResponse>(`/listings/${listingId}/images`, data);
        return response.data;
    },
    removeListingImage: async (listingId: number, imageId: number) => {
        const response = await api.delete<ListingResponse>(`/listings/${listingId}/images/${imageId}`);
        return response.data;
    },
    markListingInactive: async (listingId: number) => {
        const response = await api.patch<ListingResponse>(`/listings/${listingId}/inactive`);
        return response.data;
    },
    deleteListing: async (listingId: number) => {
        await api.delete(`/listings/${listingId}`);
    },
};
