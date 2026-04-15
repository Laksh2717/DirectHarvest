import { useEffect, useState } from "react";
import { listingService } from "@/services/listingService";
import type { ListingFilter, ListingResponse } from "@/types/listing";

export function useFarmerListingsPage() {
    const [selectedFilter, setSelectedFilter] = useState<ListingFilter>("ALL");
    const [listings, setListings] = useState<ListingResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const handleListingUpdated = (updatedListing: ListingResponse) => {
        setListings((current) => current.map((item) => (item.id === updatedListing.id ? updatedListing : item)));
    };

    const handleListingDeleted = (deletedListingId: number) => {
        setListings((current) => current.filter((item) => item.id !== deletedListingId));
    };

    useEffect(() => {
        let isMounted = true;

        const loadListings = async () => {
            setLoading(true);
            try {
                const data = await listingService.getMyListings(
                    selectedFilter === "ALL" ? undefined : [selectedFilter],
                );
                if (isMounted) {
                    setListings(data);
                    setError(null);
                }
            } catch {
                if (isMounted) {
                    setError("Unable to load your listings right now.");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadListings();

        return () => {
            isMounted = false;
        };
    }, [selectedFilter]);

    return {
        selectedFilter,
        setSelectedFilter,
        listings,
        loading,
        error,
        handleListingUpdated,
        handleListingDeleted,
    };
}
