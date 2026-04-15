import ActiveOrdersTable from "@/components/orders/ActiveOrdersTable";

export default function ActiveOrders() {
    return <ActiveOrdersTable counterpart="buyer" detailsRouteBase="/farmer/active-orders" />;
}