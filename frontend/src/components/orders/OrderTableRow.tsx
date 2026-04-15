import { Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { OrderTableRowProps } from "@/types/order";
import { formatCurrency, formatDateTime, formatQuantity } from "@/lib/formatters";
import { getOrderRowClassName } from "@/lib/badges";

export default function OrderTableRow({
    order,
    type,
    canNavigate,
    onNavigate,
    onRateClick,
    cancelledByLabel,
    displayData,
}: OrderTableRowProps) {
    const handleRowClick = canNavigate && onNavigate ? () => onNavigate(order.id) : undefined;

    return (
        <tr className={getOrderRowClassName(canNavigate)} onClick={handleRowClick}>
            {/* Order ID */}
            <td className="px-5 py-3.5 text-sm font-semibold text-foreground" title={displayData.displayOrderIdTitle}>
                {displayData.displayOrderId}
            </td>

            {/* Listing */}
            <td className="px-5 py-3.5 text-sm text-foreground" title={displayData.listingTitleRaw}>
                {displayData.listingTitle}
            </td>

            {/* Counterpart */}
            <td className="px-5 py-3.5 text-sm text-foreground" title={displayData.counterpartNameRaw}>
                {displayData.counterpartName}
            </td>

            {/* Status (Active only) */}
            {type === "active" ? (
                <td className="px-5 py-3.5 text-sm text-foreground">
                    <span className="inline-flex rounded-full bg-primary/10 px-2.5 py-1 text-xs font-semibold text-primary">
                        {order.status === "CONFIRMED" ? "Confirmed" : "Active"}
                    </span>
                </td>
            ) : null}

            {/* Agreed Price */}
            <td className="px-5 py-3.5 text-right text-sm font-semibold text-foreground">{formatCurrency(order.agreedPrice)}</td>

            {/* Agreed Qty */}
            <td className="px-5 py-3.5 text-right text-sm font-semibold text-foreground">{formatQuantity(order.agreedQuantity)}</td>

            {/* Timestamp: Created/Completed/Cancelled */}
            <td className="whitespace-nowrap px-5 py-3.5 text-sm text-muted-foreground">
                {type === "active" && formatDateTime(order.createdAt)}
                {type === "completed" && formatDateTime(order.completedAt)}
                {type === "cancelled" && formatDateTime(order.cancelledAt)}
            </td>

            {/* Type-specific column */}
            {type === "completed" ? (
                <td className="px-5 py-3.5 text-sm">
                    {order.rated && order.ratingScore !== null ? (
                        <span className="inline-flex items-center gap-1.5 font-semibold text-foreground">
                            {order.ratingScore}
                            <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                        </span>
                    ) : onRateClick ? (
                        <Button
                            type="button"
                            size="sm"
                            className="cursor-pointer"
                            onClick={(event) => {
                                event.stopPropagation();
                                onRateClick(order);
                            }}
                        >
                            Rate Now
                        </Button>
                    ) : (
                        <span className="font-medium text-muted-foreground">Not Rated</span>
                    )}
                </td>
            ) : null}

            {type === "cancelled" ? <td className="px-5 py-3.5 text-sm font-semibold text-foreground">{cancelledByLabel}</td> : null}
        </tr>
    );
}
