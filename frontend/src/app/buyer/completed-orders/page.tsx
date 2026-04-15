import CompletedOrdersTable from "@/components/orders/CompletedOrdersTable";

export default function BuyerCompletedOrders() {
    return <CompletedOrdersTable counterpart="farmer" detailsRouteBase="/buyer/completed-orders" />;
}
