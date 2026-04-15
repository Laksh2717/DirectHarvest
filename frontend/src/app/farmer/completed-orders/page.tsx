import CompletedOrdersTable from "@/components/orders/CompletedOrdersTable";

export default function CompletedOrders() {
    return <CompletedOrdersTable counterpart="buyer" detailsRouteBase="/farmer/completed-orders" />;
}
