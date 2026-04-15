import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

type LoadingStateProps = {
    message?: string;
    layout?: "page" | "inline";
    minHeightClassName?: string;
    className?: string;
    cardClassName?: string;
};

export default function LoadingState({
    message = "Loading...",
    layout = "page",
    minHeightClassName = "min-h-[60vh]",
    className,
    cardClassName,
}: LoadingStateProps) {
    const card = (
        <div className={cn("w-full max-w-lg rounded-2xl border border-border bg-card p-10 text-center shadow-(--shadow-card)", cardClassName)}>
            <Loader2 className="mx-auto h-8 w-8 animate-spin text-primary" />
            <p className="mt-3 text-sm text-muted-foreground">{message}</p>
        </div>
    );

    if (layout === "inline") {
        return <div className={cn(className)}>{card}</div>;
    }

    return <div className={cn("flex items-center justify-center", minHeightClassName, className)}>{card}</div>;
}
