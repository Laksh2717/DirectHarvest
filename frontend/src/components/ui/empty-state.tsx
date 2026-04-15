import { type ReactNode } from "react";
import { cn } from "@/lib/utils";

type EmptyStateProps = {
    message: string;
    title?: string;
    icon?: ReactNode;
    layout?: "page" | "inline";
    minHeightClassName?: string;
    className?: string;
    cardClassName?: string;
};

export default function EmptyState({
    message,
    title,
    icon,
    layout = "page",
    minHeightClassName = "min-h-[60vh]",
    className,
    cardClassName,
}: EmptyStateProps) {
    const card = (
        <div className={cn("w-full max-w-lg rounded-2xl border border-border bg-card p-10 text-center shadow-(--shadow-card)", cardClassName)}>
            {icon ? <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">{icon}</div> : null}
            {title ? <h2 className="mb-2 font-heading text-2xl font-bold text-foreground">{title}</h2> : null}
            <p className="text-sm text-muted-foreground">{message}</p>
        </div>
    );

    if (layout === "inline") {
        return <div className={cn(className)}>{card}</div>;
    }

    return <div className={cn("flex items-center justify-center", minHeightClassName, className)}>{card}</div>;
}
