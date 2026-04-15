"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { ArrowLeft, MapPin, Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import ConfirmationModal from "@/components/ui/confirmation-modal";
import LoadingState from "@/components/ui/loading-state";
import OrderCancelModal from "@/components/modals/OrderCancelModal";
import OrderRatingModal from "@/components/modals/OrderRatingModal";
import { formatCurrency, formatDateTime, formatQuantity, toTitleCase } from "@/lib/formatters";
import type { OrderUserRole, OrderViewType, OrderDetailsPageProps } from "@/types/order";
import { useOrderDetailsPage } from "@/hooks/orders/useOrderDetailsPage";
import { getCancelledByLabel, getBackLabel } from "@/lib/badges";
import OrderNegotiationTimeline from "./OrderNegotiationTimeline";

const getBackHref = (role: OrderUserRole, viewType: OrderViewType) => `/${role}/${viewType}-orders`;

export default function OrderDetailsPage({ role, viewType }: OrderDetailsPageProps) {
    const params = useParams<{ orderId: string }>();
    const orderId = Number(params.orderId);
    const {
        order,
        loading,
        error,
        completeOpen,
        cancelOpen,
        actionLoading,
        isRatingModalOpen,
        selectedRating,
        submittingRating,
        acceptedBy,
        totalAmount,
        setCompleteOpen,
        setIsRatingModalOpen,
        setSelectedRating,
        openCancelModal,
        closeCancelModal,
        closeRatingModal,
        handleMarkCompleted,
        handleSubmitRating,
        registerCancel,
        handleCancelSubmit,
        isCancelSubmitting,
        handleCancelOrder,
    } = useOrderDetailsPage({ role, viewType, orderId });

    if (loading) {
        return <LoadingState message="Loading order details..." cardClassName="max-w-xl" />;
    }

    if (error || !order) {
        return (
            <div className="space-y-4">
                <Link href={getBackHref(role, viewType)} className="inline-flex items-center gap-2 text-sm font-medium text-primary hover:underline">
                    <ArrowLeft className="h-4 w-4" />
                    {getBackLabel(viewType)}
                </Link>
                <div className="w-full max-w-xl rounded-2xl border border-destructive/30 bg-card p-10 text-center shadow-(--shadow-card)">
                    <p className="text-sm font-medium text-destructive">{error ?? "Order not found."}</p>
                </div>
            </div>
        );
    }

    const address = `${toTitleCase(order.listingStreet)}, ${toTitleCase(order.listingCity)}, ${toTitleCase(order.listingState)} - ${order.listingPincode}`;
    const statusLabel = toTitleCase(order.status);
    const counterpartyLabel = role === "buyer" ? "Farmer" : "Buyer";
    const counterpartyName = role === "buyer" ? order.farmerName : order.buyerName;
    const counterpartyEmail = role === "buyer" ? order.farmerEmail : order.buyerEmail;
    const cancelledByLabel = getCancelledByLabel(role, order.cancelledBy);

    return (
        <section className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <Link href={getBackHref(role, viewType)} className="inline-flex items-center gap-2 text-sm font-medium text-primary hover:underline">
                    <ArrowLeft className="h-4 w-4" />
                    {getBackLabel(viewType)}
                </Link>

                {viewType === "active" ? (
                    <div className="flex items-center gap-2">
                        <span className={`inline-flex rounded-full px-3.5 py-1.5 text-sm font-semibold ${order.status === "CONFIRMED" ? "bg-yellow-100 text-yellow-800" : "bg-green-100 text-green-800"}`}>
                            {statusLabel}
                        </span>
                        {order.status === "CONFIRMED" ? (
                            <>
                                <Button type="button" onClick={openCancelModal}>
                                    Cancel Order
                                </Button>
                                {role === "buyer" ? (
                                    <Button type="button" onClick={() => setCompleteOpen(true)}>
                                    Mark as Completed
                                    </Button>
                                ) : null}
                            </>
                        ) : null}
                        {order.status === "ACTIVE" && role === "buyer" ? (
                            <Button type="button" onClick={() => setCompleteOpen(true)}>
                                Mark as Completed
                            </Button>
                        ) : null}
                    </div>
                ) : null}

                {viewType === "completed" && role === "buyer" ? (
                    order.rated && order.ratingScore !== null ? (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3.5 py-1.5 text-sm font-semibold text-amber-800">
                            {order.ratingScore}
                            <Star className="h-4 w-4 fill-amber-500 text-amber-500" />
                        </span>
                    ) : (
                                <Button type="button" onClick={() => setIsRatingModalOpen(true)}>
                            Rate Now
                        </Button>
                    )
                ) : null}

                {viewType === "completed" && role === "farmer" ? (
                    order.rated && order.ratingScore !== null ? (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3.5 py-1.5 text-sm font-semibold text-amber-800">
                            {order.ratingScore}
                            <Star className="h-4 w-4 fill-amber-500 text-amber-500" />
                        </span>
                    ) : (
                        <span className="inline-flex rounded-full bg-muted px-3.5 py-1.5 text-sm font-semibold text-muted-foreground">
                            Not Rated
                        </span>
                    )
                ) : null}

                {viewType === "cancelled" ? (
                    <span className="inline-flex rounded-full bg-red-100 px-3.5 py-1.5 text-sm font-semibold text-red-800">
                        Cancelled by: {cancelledByLabel}
                    </span>
                ) : null}
            </div>

            <div className="rounded-2xl border border-border bg-card p-6 shadow-(--shadow-card)">
                <h1 className="text-2xl font-bold text-foreground">Order {order.displayOrderId}</h1>
                <p className="mt-1 text-sm text-muted-foreground">Order placed on: {formatDateTime(order.createdAt)}</p>
                {viewType === "completed" ? <p className="mt-1 text-sm text-muted-foreground">Order completed on: {formatDateTime(order.completedAt)}</p> : null}
                {viewType === "cancelled" ? <p className="mt-1 text-sm text-muted-foreground">Order cancelled on: {formatDateTime(order.cancelledAt)}</p> : null}

                <div className="mt-5 grid gap-4 sm:grid-cols-2">
                    <div className="rounded-xl border border-border/70 bg-background p-4">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">{counterpartyLabel} Name</p>
                        <p className="mt-1 font-semibold text-foreground">{toTitleCase(counterpartyName)}</p>
                    </div>
                    <div className="rounded-xl border border-border/70 bg-background p-4">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">{counterpartyLabel} Email</p>
                        <p className="mt-1 font-semibold text-foreground">{counterpartyEmail}</p>
                    </div>
                    {viewType === "cancelled" && order.cancelledReason?.trim() ? (
                        <div className="rounded-xl border border-border/70 bg-background p-4">
                            <p className="text-xs uppercase tracking-wide text-muted-foreground">Cancellation Reason</p>
                            <p className="mt-1 text-sm text-foreground">{order.cancelledReason}</p>
                        </div>
                    ) : null}
                    <div className="rounded-xl border border-border/70 bg-background p-4 sm:col-span-2">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">Listing Title</p>
                        <p className="mt-1 font-semibold text-foreground">{toTitleCase(order.listingTitle)}</p>
                    </div>
                    {order.listingDescription?.trim() ? (
                        <div className="rounded-xl border border-border/70 bg-background p-4 sm:col-span-2">
                            <p className="text-xs uppercase tracking-wide text-muted-foreground">Listing Description</p>
                            <p className="mt-1 text-sm text-foreground">{order.listingDescription}</p>
                        </div>
                    ) : null}
                    <div className="grid gap-4 sm:col-span-2 sm:grid-cols-3">
                        <div className="rounded-xl border border-border/70 bg-background p-4">
                            <p className="text-xs uppercase tracking-wide text-muted-foreground">Agreed Price</p>
                            <p className="mt-1 font-semibold text-foreground">{formatCurrency(order.agreedPrice)}</p>
                        </div>
                        <div className="rounded-xl border border-border/70 bg-background p-4">
                            <p className="text-xs uppercase tracking-wide text-muted-foreground">Agreed Quantity</p>
                            <p className="mt-1 font-semibold text-foreground">{formatQuantity(order.agreedQuantity)}</p>
                        </div>
                        <div className="rounded-xl border border-border/70 bg-background p-4">
                            <p className="text-xs uppercase tracking-wide text-muted-foreground">Total Amount</p>
                            <p className="mt-1 font-semibold text-green-700">{formatCurrency(totalAmount)}</p>
                        </div>
                    </div>
                    <div className="rounded-xl border border-border/70 bg-background p-4 sm:col-span-2">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">Listing Address</p>
                        <p className="mt-1 inline-flex items-start gap-2 text-sm text-foreground">
                            <MapPin className="mt-0.5 h-4 w-4 text-primary" />
                            <span>{address}</span>
                        </p>
                    </div>
                </div>
            </div>

            <OrderNegotiationTimeline negotiations={order.negotiations} role={role} acceptedBy={acceptedBy} />

            {viewType === "active" ? (
                <>
                    <ConfirmationModal
                        open={completeOpen}
                        title="Mark Order as Completed"
                        description="Are you sure you want to mark this order as completed?"
                        confirmText="Yes, Complete"
                        cancelText="No"
                        loading={actionLoading}
                        onConfirm={handleMarkCompleted}
                        onCancel={() => {
                            if (!actionLoading) {
                                setCompleteOpen(false);
                            }
                        }}
                    />

                    <OrderCancelModal
                        open={cancelOpen}
                        loading={actionLoading}
                        isSubmitting={isCancelSubmitting}
                        onClose={closeCancelModal}
                        onSubmit={handleCancelSubmit(handleCancelOrder)}
                        registerCancel={registerCancel}
                    />
                </>
            ) : null}

            {viewType === "completed" && role === "buyer" ? (
                <OrderRatingModal
                    open={isRatingModalOpen}
                    orderDisplayId={order.displayOrderId}
                    selectedRating={selectedRating}
                    submitting={submittingRating}
                    onClose={closeRatingModal}
                    onSelectRating={setSelectedRating}
                    onSubmit={handleSubmitRating}
                />
            ) : null}
        </section>
    );
}
