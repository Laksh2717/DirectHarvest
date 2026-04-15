"use client";

import dynamic from "next/dynamic";
import { Search, ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import BrowseProductCard from "./BrowseProductCard";
import ListingDetailsModal from "./ListingDetailsModal";
import { useBrowseProductsPage } from "@/hooks/browse/useBrowseProductsPage";

const PlaceOfferModal = dynamic(() => import("./PlaceOfferModal"), { ssr: false });

export default function BrowseProductsPage() {
    const {
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
    } = useBrowseProductsPage();

    return (
        <section className="space-y-5">
            {/* Search and Sort */}
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                {/* Search Bar */}
                <div className="relative flex-1 md:max-w-xs">
                    <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        {...register("search")}
                        type="text"
                        placeholder="Search by crop name..."
                        className="pl-10"
                    />
                </div>

                {/* Sort Select */}
                <div className="flex gap-2">
                    <select
                        {...register("sortOption")}
                        className="rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    >
                        <option value="LATEST">Latest</option>
                        <option value="PRICE_LOW">Price (Low to High)</option>
                        <option value="PRICE_HIGH">Price (High to Low)</option>
                        <option value="RATING_LOW">Farmer Rating (Low to High)</option>
                        <option value="RATING_HIGH">Farmer Rating (High to Low)</option>
                    </select>
                </div>
            </div>

            {/* Loading State */}
            {loading ? (
                <LoadingState
                    layout="inline"
                    message="Loading listings..."
                    className="flex min-h-[40vh] items-center justify-center"
                />
            ) : listings.length === 0 ? (
                <EmptyState
                    layout="inline"
                    message="No products found. Try adjusting your search or filters."
                    cardClassName="max-w-none"
                />
            ) : (
                <>
                    {/* Products Grid */}
                    <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
                        {listings.map((listing) => (
                            <div key={listing.id} className="transition-transform hover:scale-105">
                                <BrowseProductCard listing={listing} onOpenDetails={handleOpenDetails} />
                            </div>
                        ))}
                    </div>

                        {/* Pagination */}
                        {paginationData && paginationData.totalPages > 1 && (
                            <div className="flex items-center justify-center gap-2 pt-4">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handlePrevPage}
                                    disabled={paginationData.first}
                                    className="gap-2 p-2"
                                >
                                    <ChevronLeft className="h-4 w-4" />
                                </Button>

                                <div className="flex items-center gap-1">
                                    {Array.from({ length: paginationData.totalPages }).map((_, idx) => (
                                        <Button
                                            key={idx}
                                            variant={currentPage === idx ? "default" : "outline"}
                                            size="sm"
                                            onClick={() => handleGoToPage(idx)}
                                            className="h-auto px-2.5 py-1.5"
                                        >
                                            {idx + 1}
                                        </Button>
                                    ))}
                                </div>

                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleNextPage}
                                    disabled={paginationData.last}
                                    className="gap-2 p-2"
                                >
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                            </div>
                        )}
                    </>
                )}

            <ListingDetailsModal
                open={isDetailsModalOpen}
                loading={loadingDetails}
                listing={selectedListing}
                onClose={handleCloseDetails}
                onPlaceOffer={handleOpenPlaceOffer}
            />

            <PlaceOfferModal
                open={isPlaceOfferModalOpen}
                listing={offerListing}
                submitting={submittingOffer}
                onClose={handleClosePlaceOffer}
                onSubmit={handleSubmitPlaceOffer}
            />
        </section>
    );
}
