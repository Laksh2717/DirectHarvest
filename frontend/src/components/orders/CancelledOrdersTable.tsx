"use client";

import { useRouter } from "next/navigation";
import { XCircle } from "lucide-react";
import { sessionService } from "@/services/sessionService";
import type { OrderResponse } from "@/types/order";
import type { CancelledOrdersTableProps } from "@/types/order";
import { useOrdersTable } from "@/hooks/orders/useOrdersTable";
import OrdersTableLayout from "./OrdersTableLayout";
import OrderTableRow from "./OrderTableRow";
import { getCancelLabel, getCounterpartLabel, getOrderRowDisplayData } from "@/lib/badges";

const CANCELLED_ORDER_STATUSES: OrderResponse["status"][] = ["CANCELLED"];

export default function CancelledOrdersTable({ counterpart, detailsRouteBase }: CancelledOrdersTableProps) {
    const router = useRouter();
    const { orders, loading, error } = useOrdersTable({
        statuses: CANCELLED_ORDER_STATUSES,
        errorMessage: "Unable to load cancelled orders right now.",
    });

    const counterpartLabel = getCounterpartLabel(counterpart);
    const currentUserRole = sessionService.getActiveRole();

    return (
        <OrdersTableLayout
            loading={loading}
            loadingMessage="Loading cancelled orders..."
            error={error}
            hasRows={orders.length > 0}
            emptyIcon={<XCircle className="h-8 w-8 text-primary" />}
            emptyTitle="Cancelled Orders"
            emptyMessage="No cancelled orders found yet."
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
                                    Cancelled At
                                </th>
                                <th className="w-36 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Cancelled By
                                </th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-border bg-card">
                            {orders.map((order) => {
                                const row = getOrderRowDisplayData(order, counterpart);
                                const canNavigate = Boolean(detailsRouteBase);
                                const cancelledByLabel = getCancelLabel({
                                    cancelledBy: order.cancelledBy,
                                    currentUserRole,
                                });

                                return (
                                    <OrderTableRow
                                        key={order.id}
                                        order={order}
                                        counterpart={counterpart}
                                        type="cancelled"
                                        canNavigate={canNavigate}
                                        onNavigate={detailsRouteBase ? (id) => router.push(`${detailsRouteBase}/${id}`) : undefined}
                                        cancelledByLabel={cancelledByLabel}
                                        displayData={row}
                                    />
                                );
                            })}
                        </tbody>
                    </table>
        </OrdersTableLayout>
    );
}
