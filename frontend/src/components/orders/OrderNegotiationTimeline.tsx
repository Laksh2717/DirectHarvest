import { CheckCircle2 } from "lucide-react";
import { formatCurrency, formatDateTime, formatQuantity } from "@/lib/formatters";
import type { OrderNegotiationEntry } from "@/types/order";

interface OrderNegotiationTimelineProps {
  negotiations: OrderNegotiationEntry[];
  role: "buyer" | "farmer";
  acceptedBy: string;
}

export default function OrderNegotiationTimeline({ negotiations, role, acceptedBy }: OrderNegotiationTimelineProps) {
  return (
    <div className="rounded-2xl border border-border bg-card p-6 shadow-(--shadow-card)">
      <h2 className="text-2xl font-bold text-foreground">Negotiation Timeline</h2>
      <p className="mt-1 text-sm text-muted-foreground">From first offer to accepted deal.</p>
      <div className="mt-5 space-y-4">
        {negotiations.map((item, index) => (
          <div key={`${item.createdAt}-${index}`} className="rounded-xl border border-border/70 bg-background p-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-sm font-semibold text-foreground">
                Negotiation by: {role === "buyer" ? (index % 2 === 0 ? "You" : "Farmer") : index % 2 === 1 ? "You" : "Buyer"}
              </p>
              <p className="text-xs text-muted-foreground">{formatDateTime(item.createdAt)}</p>
            </div>
            <div className="mt-3 grid gap-3 sm:grid-cols-2">
              <div>
                <p className="text-xs uppercase tracking-wide text-muted-foreground">Price</p>
                <p className="mt-1 text-sm font-semibold text-foreground">{formatCurrency(item.price)}</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wide text-muted-foreground">Quantity</p>
                <p className="mt-1 text-sm font-semibold text-foreground">{formatQuantity(item.qty)}</p>
              </div>
            </div>
          </div>
        ))}
        <div className="rounded-xl border border-primary/30 bg-primary/5 p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="inline-flex items-center gap-2 text-sm font-semibold text-foreground">
              <CheckCircle2 className="h-4 w-4 text-primary" />
              Accepted by: {acceptedBy}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
