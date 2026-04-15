import ActiveOrdersTable from "@/components/orders/ActiveOrdersTable";

export default function BuyerActiveOrders() {
    return <ActiveOrdersTable counterpart="farmer" detailsRouteBase="/buyer/active-orders" />;
}
