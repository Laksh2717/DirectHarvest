import CancelledOrdersTable from "@/components/orders/CancelledOrdersTable";

export default function FarmerCancelledOrders() {
    return <CancelledOrdersTable counterpart="buyer" detailsRouteBase="/farmer/cancelled-orders" />;
}
