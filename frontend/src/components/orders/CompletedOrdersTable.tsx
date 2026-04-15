"use client";

import { useRouter } from "next/navigation";
import { CheckCircle } from "lucide-react";
import type { CompletedOrdersTableProps, OrderResponse } from "@/types/order";
import { useOrdersTable } from "@/hooks/orders/useOrdersTable";
import { useCompletedOrderRating } from "@/hooks/orders/useCompletedOrderRating";
import OrderRatingModal from "@/components/modals/OrderRatingModal";
import OrdersTableLayout from "./OrdersTableLayout";
import OrderTableRow from "./OrderTableRow";
import { getCounterpartLabel, getOrderRowDisplayData } from "@/lib/badges";

const COMPLETED_ORDER_STATUSES: OrderResponse["status"][] = ["COMPLETED"];

export default function CompletedOrdersTable({ counterpart, detailsRouteBase }: CompletedOrdersTableProps) {
    const router = useRouter();
    const { orders, setOrders, loading, error } = useOrdersTable({
        statuses: COMPLETED_ORDER_STATUSES,
        errorMessage: "Unable to load completed orders right now.",
    });
    const {
        isRatingModalOpen,
        selectedOrder,
        selectedRating,
        submittingRating,
        setSelectedRating,
        openRatingModal,
        closeRatingModal,
        handleSubmitRating,
    } = useCompletedOrderRating({ setOrders });

    const counterpartLabel = getCounterpartLabel(counterpart);

    return (
        <>
            <OrdersTableLayout
                loading={loading}
                loadingMessage="Loading completed orders..."
                error={error}
                hasRows={orders.length > 0}
                emptyIcon={<CheckCircle className="h-8 w-8 text-primary" />}
                emptyTitle="Completed Orders"
                emptyMessage="No completed orders found yet."
            >
                    <table className="min-w-full divide-y divide-border">
                        <thead className="bg-muted/50">
                            <tr>
                                <th className="w-28 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Order ID
                                </th>
                                <th className="px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Listing
                                </th>
                                <th className="px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    {counterpartLabel}
                                </th>
                                <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Agreed Price
                                </th>
                                <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Agreed Qty
                                </th>
                                <th className="w-44 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Completed At
                                </th>
                                <th className="w-32 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Rating
                                </th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-border bg-card">
                            {orders.map((order) => {
                                const row = getOrderRowDisplayData(order, counterpart);
                                const canNavigate = Boolean(detailsRouteBase);

                                return (
                                    <OrderTableRow
                                        key={order.id}
                                        order={order}
                                        counterpart={counterpart}
                                        type="completed"
                                        canNavigate={canNavigate}
                                        onNavigate={detailsRouteBase ? (id) => router.push(`${detailsRouteBase}/${id}`) : undefined}
                                        onRateClick={counterpart === "farmer" ? openRatingModal : undefined}
                                        displayData={row}
                                    />
                                );
                            })}
                        </tbody>
                    </table>
            </OrdersTableLayout>

            <OrderRatingModal
                open={isRatingModalOpen && Boolean(selectedOrder)}
                orderDisplayId={selectedOrder?.displayOrderId ?? ""}
                selectedRating={selectedRating}
                submitting={submittingRating}
                onClose={closeRatingModal}
                onSelectRating={setSelectedRating}
                onSubmit={handleSubmitRating}
            />
        </>
    );
}
