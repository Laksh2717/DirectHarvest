import CancelledOrdersTable from "@/components/orders/CancelledOrdersTable";

export default function BuyerCancelledOrders() {
    return <CancelledOrdersTable counterpart="farmer" detailsRouteBase="/buyer/cancelled-orders" />;
}
