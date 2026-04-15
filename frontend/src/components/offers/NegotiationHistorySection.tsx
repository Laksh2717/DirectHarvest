import { formatCurrency, formatDateTimeMedium, formatQuantity, toTitleCase } from "@/lib/formatters";
import EmptyState from "@/components/ui/empty-state";
import { Button } from "@/components/ui/button";
import type { NegotiationHistorySectionProps } from "@/types/offer";
import { getNegotiationStatusLabel, getTurnLabel, getEventTitle } from "@/lib/badges";

export default function NegotiationHistorySection({
    role,
    negotiation,
    history,
    isMyTurn,
    onOpenAccept,
    onOpenReject,
    onOpenCounter,
    actionBusy,
}: NegotiationHistorySectionProps) {
    const roleEnum = role === "buyer" ? "BUYER" : "FARMER";
    const counterpartLabel = role === "buyer" ? "Farmer" : "Buyer";

    return (
        <div className="space-y-4">
            <div className="space-y-3">
                <div className="grid gap-3 sm:grid-cols-2">
                    <div className="rounded-xl border border-border/70 bg-background p-3">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">Listing Title</p>
                        <p className="mt-1 text-sm font-semibold text-foreground">{toTitleCase(negotiation.listingTitle)}</p>
                    </div>
                    <div className="rounded-xl border border-border/70 bg-background p-3">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">{counterpartLabel} Name</p>
                        <p className="mt-1 text-sm font-semibold text-foreground">
                            {toTitleCase(role === "buyer" ? negotiation.farmerName : negotiation.buyerName)}
                        </p>
                    </div>
                </div>
                <div className="rounded-xl border border-border/70 bg-background p-3">
                    <p className="text-xs uppercase tracking-wide text-muted-foreground">{counterpartLabel} Email</p>
                    <p className="mt-1 text-sm font-semibold text-foreground break-all">
                        {role === "buyer" ? negotiation.farmerEmail : negotiation.buyerEmail}
                    </p>
                </div>
            </div>

            {history.length === 0 ? (
                <EmptyState layout="inline" message="No negotiation history found." cardClassName="max-w-none p-4" />
            ) : (
                history.map((event, index) => {
                    const isLatest = index === history.length - 1;
                    const actorIsMe = event.actorRole === roleEnum;
                    const actorLabel = actorIsMe ? "You" : event.actorRole ? toTitleCase(event.actorRole) : "System";
                    const turnLabel = isLatest ? getTurnLabel(negotiation.status) : null;

                    return (
                        <div
                            key={event.id}
                            className={`rounded-xl border p-4 ${isLatest ? "border-primary/35 bg-primary/5" : "border-border/70 bg-background"}`}
                        >
                            <div className="flex flex-wrap items-center justify-between gap-2">
                                <p className="text-sm font-semibold text-foreground">
                                    {getEventTitle(event.eventType)} by {actorLabel}
                                </p>
                                <p className="text-xs text-muted-foreground">{formatDateTimeMedium(event.createdAt)}</p>
                            </div>

                            <div className="mt-3 grid gap-3 sm:grid-cols-2">
                                <div>
                                    <p className="text-xs uppercase tracking-wide text-muted-foreground">Offered Price/KG</p>
                                    <p className="mt-1 text-sm font-semibold text-foreground">{formatCurrency(event.offeredPrice)}</p>
                                </div>
                                <div>
                                    <p className="text-xs uppercase tracking-wide text-muted-foreground">Requested Qty (KG)</p>
                                    <p className="mt-1 text-sm font-semibold text-foreground">{formatQuantity(event.requestedQuantity)}</p>
                                </div>
                            </div>

                            {isLatest && negotiation.status === "REJECTED" && negotiation.cancellationReason?.trim() ? (
                                <div className="mt-3 rounded-lg border border-border/70 bg-background p-3">
                                    <p className="text-xs uppercase tracking-wide text-muted-foreground">Cancellation Reason</p>
                                    <p className="mt-1 text-sm text-foreground">{negotiation.cancellationReason}</p>
                                </div>
                            ) : null}

                            {isLatest ? (
                                <div className="mt-4 border-t border-border/70 pt-3">
                                    {turnLabel ? (
                                        isMyTurn ? (
                                            <div className="flex flex-wrap gap-2">
                                                <Button type="button" size="sm" disabled={actionBusy} onClick={onOpenAccept}>
                                                    Accept
                                                </Button>
                                                <Button type="button" size="sm" variant="destructive" disabled={actionBusy} onClick={onOpenReject}>
                                                    Reject
                                                </Button>
                                                <Button type="button" size="sm" variant="outline" disabled={actionBusy} onClick={onOpenCounter}>
                                                    Counter Offer
                                                </Button>
                                            </div>
                                        ) : (
                                            <span className="inline-flex rounded-full bg-rose-100 px-2.5 py-1 text-xs font-semibold text-rose-700">
                                                {turnLabel}
                                            </span>
                                        )
                                    ) : (
                                        <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                                            {getNegotiationStatusLabel(negotiation.status)}
                                        </span>
                                    )}
                                </div>
                            ) : null}
                        </div>
                    );
                })
            )}
        </div>
    );
}
