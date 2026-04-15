import { useEffect, useMemo, useState } from "react";
import { orderService } from "@/services/orderService";
import type { OrderResponse, UseOrdersTableParams } from "@/types/order";

export function useOrdersTable({ statuses, errorMessage }: UseOrdersTableParams) {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const statusFilter = useMemo(() => statuses, [statuses.join("|")]);

    useEffect(() => {
        let isMounted = true;

        const loadOrders = async () => {
            setLoading(true);
            try {
                const data = await orderService.getMyOrders(statusFilter);
                if (isMounted) {
                    setOrders(data);
                    setError(null);
                }
            } catch {
                if (isMounted) {
                    setError(errorMessage);
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadOrders();

        return () => {
            isMounted = false;
        };
    }, [errorMessage, statusFilter]);

    return {
        orders,
        setOrders,
        loading,
        error,
    };
}