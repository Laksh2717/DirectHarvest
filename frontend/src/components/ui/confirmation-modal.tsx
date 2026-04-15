"use client";

import { ReactNode, useEffect } from "react";
import { createPortal } from "react-dom";
import { X } from "lucide-react";
import { Button } from "@/components/ui/button";

interface ConfirmationModalProps {
    open: boolean;
    title: string;
    description: string;
    confirmText?: string;
    cancelText?: string;
    loading?: boolean;
    icon?: ReactNode;
    onConfirm: () => void | Promise<void>;
    onCancel: () => void;
}

export default function ConfirmationModal({
    open,
    title,
    description,
    confirmText = "Confirm",
    cancelText = "Cancel",
    loading = false,
    icon,
    onConfirm,
    onCancel,
}: ConfirmationModalProps) {
    useEffect(() => {
        if (!open) return;

        const handleEscape = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onCancel();
            }
        };

        window.addEventListener("keydown", handleEscape);
        return () => window.removeEventListener("keydown", handleEscape);
    }, [open, onCancel]);

    if (!open) return null;

    return createPortal(
        <div className="fixed inset-0 z-[1000] flex items-center justify-center px-4">
            <button
                type="button"
                aria-label="Close modal backdrop"
                className="absolute inset-0 bg-black/50 backdrop-blur-[2px]"
                onClick={onCancel}
            />

            <div className="relative z-10 w-full max-w-md rounded-2xl border border-border bg-card p-5 shadow-[var(--shadow-elevated)]">
                <div className="mb-4 flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3">
                        {icon ? (
                            <div className="mt-0.5 flex h-10 w-10 items-center justify-center rounded-full bg-destructive/10 text-destructive">
                                {icon}
                            </div>
                        ) : null}
                        <div>
                            <h2 className="text-base font-semibold text-foreground">{title}</h2>
                            <p className="mt-1 text-sm font-medium text-muted-foreground">{description}</p>
                        </div>
                    </div>

                    <button
                        type="button"
                        onClick={onCancel}
                        className="rounded-full p-1 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                        aria-label="Close modal"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
                        {cancelText}
                    </Button>
                    <Button type="button" variant="destructive" onClick={onConfirm} disabled={loading}>
                        {loading ? "Processing..." : confirmText}
                    </Button>
                </div>
            </div>
        </div>,
        document.body,
    );
}
