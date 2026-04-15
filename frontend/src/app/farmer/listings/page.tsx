"use client";

import { Package } from "lucide-react";
import FarmerListingCard from "@/components/listings/FarmerListingCard";
import ListingFilterTabs from "@/components/listings/ListingFilterTabs";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { useFarmerListingsPage } from "@/hooks/listings/useFarmerListingsPage";
import type { ListingFilter } from "@/types/listing";

const FILTERS: Array<{ label: string; value: ListingFilter }> = [
    { label: "All", value: "ALL" },
    { label: "Active", value: "ACTIVE" },
    { label: "Out of Stock", value: "OUT_OF_STOCK" },
    { label: "Inactive", value: "INACTIVE" },
];

export default function MyListings() {
    const {
        selectedFilter,
        setSelectedFilter,
        listings,
        loading,
        error,
        handleListingUpdated,
        handleListingDeleted,
    } = useFarmerListingsPage();

    return (
        <section className="space-y-5">
            <ListingFilterTabs filters={FILTERS} selectedFilter={selectedFilter} onChange={setSelectedFilter} />

            {loading ? (
                <LoadingState
                    layout="inline"
                    message="Loading your listings..."
                    className="flex min-h-[40vh] items-center justify-center"
                />
            ) : null}

            {!loading && error ? (
                <div className="rounded-2xl border border-destructive/30 bg-card p-10 text-center shadow-(--shadow-card)">
                    <p className="text-sm font-medium text-destructive">{error}</p>
                </div>
            ) : null}

            {!loading && !error && listings.length === 0 ? (
                <EmptyState
                    layout="inline"
                    icon={<Package className="h-7 w-7 text-primary" />}
                    message="No listings found for the selected status."
                    cardClassName="max-w-none"
                />
            ) : null}

            {!loading && !error && listings.length > 0 ? (
                <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
                    {listings.map((listing) => (
                        <div key={listing.id} className="transition-transform hover:scale-105">
                            <FarmerListingCard
                                listing={listing}
                                onListingUpdated={handleListingUpdated}
                                onListingDeleted={handleListingDeleted}
                            />
                        </div>
                    ))}
                </div>
            ) : null}
        </section>
    );
}

