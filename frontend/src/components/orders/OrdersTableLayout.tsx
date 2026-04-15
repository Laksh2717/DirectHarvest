import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import type { OrdersTableLayoutProps } from "@/types/order";

export default function OrdersTableLayout({
    loading,
    loadingMessage,
    error,
    hasRows,
    emptyIcon,
    emptyTitle,
    emptyMessage,
    children,
}: OrdersTableLayoutProps) {
    if (loading) {
        return <LoadingState message={loadingMessage} />;
    }

    if (error) {
        return (
            <div className="flex min-h-[60vh] items-center justify-center">
                <div className="w-full max-w-lg rounded-2xl border border-destructive/30 bg-card p-10 text-center shadow-[var(--shadow-card)]">
                    <p className="text-sm font-medium text-destructive">{error}</p>
                </div>
            </div>
        );
    }

    if (!hasRows) {
        return (
            <EmptyState
                icon={emptyIcon}
                title={emptyTitle}
                message={emptyMessage}
                cardClassName="max-w-md p-12"
            />
        );
    }

    return (
        <section className="space-y-4">
            <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-[var(--shadow-card)]">
                <div className="overflow-x-auto">{children}</div>
            </div>
        </section>
    );
}