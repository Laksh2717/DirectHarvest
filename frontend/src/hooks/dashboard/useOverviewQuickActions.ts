import { useEffect, useState } from "react";
import { dashboardService } from "@/services/dashboardService";
import type { OverviewQuickActionResponse } from "@/types/dashboard";;
import { toast } from "sonner";
import { resolveErrorMessage } from "@/lib/utils";

export function useOverviewQuickActions() {
    const [actions, setActions] = useState<OverviewQuickActionResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        const loadQuickActions = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await dashboardService.getOverviewQuickActions();

                if (!isMounted) {
                    return;
                }

                setActions(response.actions.slice(0, 3));
            } catch (err) {
                const message = resolveErrorMessage(err, "Failed to load quick actions");
                if (isMounted) {
                    setError(message);
                    toast.error(message);
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadQuickActions();

        return () => {
            isMounted = false;
        };
    }, []);

    return { actions, loading, error };
}