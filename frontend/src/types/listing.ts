import { FarmerListingCardState } from "@/hooks/listings/useFarmerListingCard";
import { ListingImageVariant } from "@/lib/cloudinary";
import type { SortDirection } from "@/types/common";

export type ListingStatus = "ACTIVE" | "INACTIVE" | "OUT_OF_STOCK";

export interface ListingImagePayload {
    cloudinaryPublicId: string;
    cloudinarySecureUrl: string;
    format?: string;
    width?: number;
    height?: number;
    bytes?: number;
    primary?: boolean;
}

export interface CreateListingPayload {
    cropName: string;
    quantity: number;
    pricePerKg: number;
    description?: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
    images?: ListingImagePayload[];
}

export interface CloudinaryUploadResponse {
    publicId: string;
    secureUrl: string;
    format?: string;
    width?: number;
    height?: number;
    bytes?: number;
}

export interface ListingImageResponse {
    id: number;
    cloudinaryPublicId: string;
    cloudinarySecureUrl: string;
    format: string | null;
    width: number | null;
    height: number | null;
    bytes: number | null;
    primary: boolean;
}

export interface ListingResponse {
    id: number;
    farmerId: number;
    farmerName: string;
    farmerEmail: string;
    farmerRating: number | null;
    farmerRatingCount: number | null;
    cropName: string;
    quantity: number;
    pricePerKg: number;
    description: string | null;
    street: string;
    city: string;
    state: string;
    pincode: string;
    status: ListingStatus;
    images: ListingImageResponse[];
    createdAt: string;
    updatedAt: string;
}

export interface AddListingQuantityPayload {
    quantity: number;
}

export interface UpdateListingPricePayload {
    pricePerKg: number;
}

export interface UpdateListingDetailsPayload {
    cropName: string;
    description: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
}

export interface AddListingImagesPayload {
    images: ListingImagePayload[];
}

export type BrowseListingsSortBy = "LISTING_DATE" | "PRICE" | "FARMER_RATING";

export interface BrowseListingsResponse {
    content: ListingResponse[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export interface BrowseListingsParams {
    search?: string;
    page?: number;
    sortBy?: BrowseListingsSortBy;
    sortDir?: SortDirection;
}

export type CreateListingFormValues = {
    cropName: string;
    quantity: string;
    pricePerKg: string;
    description: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
};

export type UpdateDetailsForm = {
    cropName: string;
    description: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
};

export type AddQuantityForm = {
    quantity: string;
};

export type UpdatePriceForm = {
    pricePerKg: string;
};

export type UseFarmerListingCardParams = {
    listing: ListingResponse;
    onListingUpdated?: (updatedListing: ListingResponse) => void;
    onListingDeleted?: (deletedListingId: number) => void;
};

export type ListingFilter = "ALL" | ListingStatus;

export type ListingActionsMenuPosition = {
    top: number;
    right: number;
    maxHeight: number;
};

export interface ListingDetailsModalProps {
    open: boolean;
    loading: boolean;
    listing: ListingResponse | null;
    onClose: () => void;
    onPlaceOffer: (listing: ListingResponse) => void;
}

export type FarmerListingCardProps = {
    listing: ListingResponse;
    onListingUpdated?: (updatedListing: ListingResponse) => void;
    onListingDeleted?: (deletedListingId: number) => void;
};

export type ListingActionModalsProps = {
    listing: ListingResponse;
    state: FarmerListingCardState;
};

export type ListingActionsMenuProps = {
    status: ListingStatus;
    isOpen: boolean;
    menuPosition: ListingActionsMenuPosition | null;
    menuRef: React.RefObject<HTMLDivElement | null>;
    onAddQty: () => void;
    onUpdatePrice: () => void;
    onUpdateDetails: () => void;
    onUpdateImages: () => void;
    onMakeInactive: () => void;
    onDelete: () => void;
};

export type ListingFilterOption = {
    label: string;
    value: ListingFilter;
};

export type ListingFilterTabsProps = {
    filters: ListingFilterOption[];
    selectedFilter: ListingFilter;
    onChange: (value: ListingFilter) => void;
};

export type ListingImageProps = {
    image: ListingImageResponse | null;
    alt: string;
    variant?: ListingImageVariant;
    className?: string;
    priority?: boolean;
};