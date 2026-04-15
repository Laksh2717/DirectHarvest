"use client";

import type { NegotiationResponse } from "@/types/offer";
import type { NegotiationsTableProps } from "@/types/offer";
import NegotiationDetailsModal from "./NegotiationDetailsModal";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { Package } from "lucide-react";
import { formatCurrency, formatQuantity, toTitleCase, truncateText } from "@/lib/formatters";
import { useNegotiationsTable } from "@/hooks/offers/useNegotiationsTable";

export default function NegotiationsTable({ role }: NegotiationsTableProps) {
    const {
        activeTab,
        tabOptions,
        tableRows,
        loading,
        error,
        emptyLabel,
        counterpartLabel,
        isMyTurn,
        getCancelledByLabel,
        getLastOfferByLabel,
        handleOpenDetails,
        handleCloseDetails,
        handleActionComplete,
        handleTabChange,
        selectedNegotiationId,
        detailsOpen,
    } = useNegotiationsTable({ role });

    if (loading) {
        return <LoadingState message="Loading offers and negotiations..." />;
    }

    if (error) {
        return (
            <div className="flex min-h-[60vh] items-center justify-center">
                <div className="w-full max-w-lg rounded-2xl border border-destructive/30 bg-card p-10 text-center shadow-(--shadow-card)">
                    <p className="text-sm font-medium text-destructive">{error}</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <section className="space-y-4">
                <div className="flex items-center gap-2 overflow-x-auto pb-1">
                    {tabOptions.map((option) => (
                        <button
                            key={option.value}
                            type="button"
                            onClick={() => handleTabChange(option.value)}
                            className={`whitespace-nowrap rounded-full border px-4 py-2 text-sm font-semibold transition-colors ${activeTab === option.value
                                ? "border-primary bg-primary text-primary-foreground"
                                : "border-border bg-background text-muted-foreground hover:bg-muted hover:text-foreground"
                                }`}
                        >
                            {option.label}
                        </button>
                    ))}
                </div>

                {tableRows.length === 0 ? (
                    <EmptyState
                        layout="inline"
                        icon={<Package className="h-7 w-7 text-primary" />}
                        message={emptyLabel}
                        cardClassName="max-w-none p-8"
                    />
                ) : (
                    <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-(--shadow-card)">
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-border">
                                <thead className="bg-muted/50">
                                    <tr>
                                        <th className="px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">Listing</th>
                                        <th className="px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">{counterpartLabel}</th>
                                        <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">Offered Price/KG</th>
                                        <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">Required Qty (KG)</th>
                                        {activeTab === "ACTIVE" ? (
                                            <th className="w-52 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">Turn</th>
                                        ) : null}
                                        {activeTab === "REJECTED" ? (
                                            <th className="w-52 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">Cancelled By</th>
                                        ) : null}
                                        {activeTab === "EXPIRED" ? (
                                            <th className="w-52 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">Last Offer By</th>
                                        ) : null}
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border bg-card">
                                    {tableRows.map((negotiation: NegotiationResponse & { counterpartName: string }) => {
                                        const listingTitle = truncateText(toTitleCase(negotiation.listingTitle), 30);
                                        const counterpart = truncateText(toTitleCase(negotiation.counterpartName), 22);

                                        return (
                                            <tr
                                                key={negotiation.id}
                                                className="cursor-pointer transition-colors hover:bg-muted/30"
                                                onClick={() => handleOpenDetails(negotiation.id)}
                                            >
                                                <td className="px-5 py-3.5 text-sm text-foreground" title={toTitleCase(negotiation.listingTitle)}>
                                                    {listingTitle}
                                                </td>
                                                <td className="px-5 py-3.5 text-sm text-foreground" title={toTitleCase(negotiation.counterpartName)}>
                                                    {counterpart}
                                                </td>
                                                <td className="px-5 py-3.5 text-right text-sm font-semibold text-foreground">{formatCurrency(negotiation.offeredPrice)}</td>
                                                <td className="px-5 py-3.5 text-right text-sm font-semibold text-foreground">{formatQuantity(negotiation.requestedQuantity)}</td>

                                                {activeTab === "ACTIVE" ? (
                                                    <td className="px-5 py-3.5">
                                                        {isMyTurn(negotiation) ? (
                                                            <span className="inline-flex rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-semibold text-emerald-700">
                                                                Your turn
                                                            </span>
                                                        ) : (
                                                            <span className="inline-flex rounded-full bg-rose-100 px-2.5 py-1 text-xs font-semibold text-rose-700">
                                                                {counterpartLabel} turn
                                                            </span>
                                                        )}
                                                    </td>
                                                ) : null}

                                                {activeTab === "REJECTED" ? (
                                                    <td className="px-5 py-3.5 text-sm font-medium text-foreground">{getCancelledByLabel(negotiation)}</td>
                                                ) : null}

                                                {activeTab === "EXPIRED" ? (
                                                    <td className="px-5 py-3.5 text-sm font-medium text-foreground">{getLastOfferByLabel(negotiation)}</td>
                                                ) : null}
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </section>
            <NegotiationDetailsModal
                open={detailsOpen}
                negotiationId={selectedNegotiationId}
                role={role}
                onClose={handleCloseDetails}
                onActionComplete={handleActionComplete}
            />
        </>
    );
}
