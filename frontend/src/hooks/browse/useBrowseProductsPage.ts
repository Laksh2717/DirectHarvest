import { useCallback, useEffect, useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useForm, useWatch } from "react-hook-form";
import { useRouter } from "next/navigation";
import { listingService } from "@/services/listingService";
import type { BrowseListingsResponse, ListingResponse } from "@/types/listing";;
import { negotiationService } from "@/services/negotiationService";
import { sessionService } from "@/services/sessionService";
import { toast } from "sonner";
import type { SortOption, BrowseFilterFormValues } from "@/types/browse";
import { getSortConfig } from "@/lib/badges";

export function useBrowseProductsPage() {
    const router = useRouter();
    const [listings, setListings] = useState<ListingResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [paginationData, setPaginationData] = useState<Omit<BrowseListingsResponse, "content"> | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [selectedListing, setSelectedListing] = useState<ListingResponse | null>(null);
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [isPlaceOfferModalOpen, setIsPlaceOfferModalOpen] = useState(false);
    const [offerListing, setOfferListing] = useState<ListingResponse | null>(null);
    const [submittingOffer, setSubmittingOffer] = useState(false);

    const { register, control } = useForm<BrowseFilterFormValues>({
        defaultValues: {
            search: "",
            sortOption: "LATEST",
        },
    });

    const searchValue = useWatch({ control, name: "search" }) ?? "";
    const sortOption = useWatch({ control, name: "sortOption" }) ?? "LATEST";
    const [debouncedSearch, setDebouncedSearch] = useState("");

    const fetchListings = useCallback(
        async (page: number = 0, sort?: SortOption, search?: string) => {
            setLoading(true);
            try {
                const sortConfig = getSortConfig(sort ?? sortOption);
                const response = await listingService.browseListings({
                    page,
                    search: search || undefined,
                    sortBy: sortConfig.sortBy,
                    sortDir: sortConfig.sortDir,
                });
                setListings(response.content);
                setPaginationData({
                    page: response.page,
                    size: response.size,
                    totalElements: response.totalElements,
                    totalPages: response.totalPages,
                    first: response.first,
                    last: response.last,
                });
                setCurrentPage(page);
            } catch (error) {
                console.error("Failed to fetch listings:", error);
                toast.error("Failed to load products. Please try again.");
            } finally {
                setLoading(false);
            }
        },
        [sortOption],
    );

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedSearch(searchValue.trim());
        }, 350);

        return () => {
            window.clearTimeout(timeout);
        };
    }, [searchValue]);

    useEffect(() => {
        void fetchListings(0, sortOption, debouncedSearch);
    }, [fetchListings, sortOption, debouncedSearch]);

    const handleGoToPage = useCallback(
        (page: number) => {
            if (page >= 0 && paginationData && page < paginationData.totalPages) {
                void fetchListings(page, sortOption, debouncedSearch);
            }
        },
        [debouncedSearch, fetchListings, paginationData, sortOption],
    );

    const handleNextPage = useCallback(() => {
        if (paginationData && !paginationData.last) {
            handleGoToPage(currentPage + 1);
        }
    }, [currentPage, handleGoToPage, paginationData]);

    const handlePrevPage = useCallback(() => {
        if (paginationData && !paginationData.first) {
            handleGoToPage(currentPage - 1);
        }
    }, [currentPage, handleGoToPage, paginationData]);

    const handleOpenDetails = useCallback(async (listingId: number) => {
        setIsDetailsModalOpen(true);
        setLoadingDetails(true);
        setSelectedListing(null);

        try {
            const response = await listingService.getListingById(listingId);
            setSelectedListing(response);
        } catch (error) {
            console.error("Failed to fetch listing details:", error);
            toast.error("Failed to load listing details.");
        } finally {
            setLoadingDetails(false);
        }
    }, []);

    const handleCloseDetails = useCallback(() => {
        setIsDetailsModalOpen(false);
        setSelectedListing(null);
        setLoadingDetails(false);
    }, []);

    const handleOpenPlaceOffer = useCallback(
        (listing: ListingResponse) => {
            const activeRole = sessionService.getActiveRole();
            handleCloseDetails();

            if (activeRole !== "BUYER") {
                router.push("/login/buyer");
                return;
            }

            setOfferListing(listing);
            setIsPlaceOfferModalOpen(true);
        },
        [handleCloseDetails, router],
    );

    const handleClosePlaceOffer = useCallback(() => {
        if (submittingOffer) {
            return;
        }

        setIsPlaceOfferModalOpen(false);
        setOfferListing(null);
    }, [submittingOffer]);

    const handleSubmitPlaceOffer = useCallback(
        async (payload: { requestedQuantity: number; offeredPrice: number }) => {
            if (!offerListing) {
                return;
            }

            const activeRole = sessionService.getActiveRole();
            if (activeRole !== "BUYER") {
                setIsPlaceOfferModalOpen(false);
                setOfferListing(null);
                router.push("/login/buyer");
                return;
            }

            setSubmittingOffer(true);

            try {
                await negotiationService.createNegotiation({
                    listingId: offerListing.id,
                    offeredPrice: payload.offeredPrice,
                    requestedQuantity: payload.requestedQuantity,
                });

                toast.success("Offer placed successfully.");
                setIsPlaceOfferModalOpen(false);
                setOfferListing(null);
                router.push("/buyer/offers");
            } catch (error) {
                toast.error(resolveApiErrorMessage(error, "Failed to place offer. Please try again."));
            } finally {
                setSubmittingOffer(false);
            }
        },
        [offerListing, router],
    );

    return {
        register,
        listings,
        loading,
        paginationData,
        currentPage,
        isDetailsModalOpen,
        selectedListing,
        loadingDetails,
        isPlaceOfferModalOpen,
        offerListing,
        submittingOffer,
        handleGoToPage,
        handleNextPage,
        handlePrevPage,
        handleOpenDetails,
        handleCloseDetails,
        handleOpenPlaceOffer,
        handleClosePlaceOffer,
        handleSubmitPlaceOffer,
    };
}