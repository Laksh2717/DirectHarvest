import type { BrowseListingsSortBy, ListingResponse } from "@/services/listingService";

export type SortOption = "LATEST" | "PRICE_LOW" | "PRICE_HIGH" | "RATING_LOW" | "RATING_HIGH";

export type BrowseFilterFormValues = {
    search: string;
    sortOption: SortOption;
};

export interface SortConfig {
    sortBy: BrowseListingsSortBy;
    sortDir: "ASC" | "DESC";
}

export type BrowseProductCardProps = {
    listing: ListingResponse;
    onOpenDetails: (listingId: number) => void;
};