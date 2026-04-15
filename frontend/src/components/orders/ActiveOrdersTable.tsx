"use client";

import { useRouter } from "next/navigation";
import { Truck } from "lucide-react";
import type { OrderResponse } from "@/types/order";
import type { ActiveOrdersTableProps } from "@/types/order";
import { useOrdersTable } from "@/hooks/orders/useOrdersTable";
import OrdersTableLayout from "./OrdersTableLayout";
import OrderTableRow from "./OrderTableRow";
import { getCounterpartLabel, getOrderRowDisplayData } from "@/lib/badges";

const ACTIVE_ORDER_STATUSES: OrderResponse["status"][] = ["CONFIRMED", "ACTIVE"];

export default function ActiveOrdersTable({ counterpart, detailsRouteBase }: ActiveOrdersTableProps) {
    const router = useRouter();
    const { orders, loading, error } = useOrdersTable({
        statuses: ACTIVE_ORDER_STATUSES,
        errorMessage: "Unable to load active orders right now.",
    });

    const counterpartLabel = getCounterpartLabel(counterpart);

    return (
        <OrdersTableLayout
            loading={loading}
            loadingMessage="Loading active orders..."
            error={error}
            hasRows={orders.length > 0}
            emptyIcon={<Truck className="h-8 w-8 text-primary" />}
            emptyTitle="Active Orders"
            emptyMessage="No confirmed or active orders found yet."
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
                                <th className="w-32 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Status
                                </th>
                                <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Agreed Price
                                </th>
                                <th className="px-5 py-3.5 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Agreed Qty
                                </th>
                                <th className="w-44 px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                    Created At
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
                                        type="active"
                                        canNavigate={canNavigate}
                                        onNavigate={detailsRouteBase ? (id) => router.push(`${detailsRouteBase}/${id}`) : undefined}
                                        displayData={row}
                                    />
                                );
                            })}
                        </tbody>
                    </table>
        </OrdersTableLayout>
    );
}