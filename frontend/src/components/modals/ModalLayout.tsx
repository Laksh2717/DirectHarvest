import { X } from "lucide-react";
import type { ModalLayoutProps } from "@/types/modal";

const maxWidthClasses = {
    sm: "max-w-md",
    md: "max-w-lg",
    lg: "max-w-xl",
    xl: "max-w-2xl",
};

export default function ModalLayout({
    open,
    title,
    onClose,
    children,
    closeAriaLabel = "Close modal",
    backdropAriaLabel = "Close modal backdrop",
    maxWidth = "md",
    onBackdropClick,
    closeButtonDisabled = false,
}: ModalLayoutProps) {
    if (!open) {
        return null;
    }

    const handleBackdropClick = onBackdropClick || onClose;
    const handleCloseClick = closeButtonDisabled ? undefined : onClose;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
            <button
                type="button"
                aria-label={backdropAriaLabel}
                className="absolute inset-0 bg-black/50 backdrop-blur-[2px]"
                onClick={handleBackdropClick}
                disabled={closeButtonDisabled}
            />

            <div className={`relative z-10 w-full ${maxWidthClasses[maxWidth]} rounded-2xl border border-border bg-card p-5 shadow-(--shadow-elevated)`}>
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-base font-semibold text-foreground">{title}</h2>
                    <button
                        type="button"
                        onClick={handleCloseClick}
                        aria-label={closeAriaLabel}
                        className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:cursor-not-allowed"
                        disabled={closeButtonDisabled}
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                {children}
            </div>
        </div>
    );
}
