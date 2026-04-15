import type { ReactNode } from "react";

type FormFieldProps = {
    label: string;
    id?: string;
    error?: string;
    helpText?: string;
    children: ReactNode;
    required?: boolean;
};

export function FormField({ label, id, error, helpText, children, required = false }: FormFieldProps) {
    return (
        <div>
            <label htmlFor={id} className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                {label}
                {required && <span className="text-destructive">*</span>}
            </label>
            {children}
            {error && <p className="mt-1 text-xs text-destructive">{error}</p>}
            {helpText && !error && <p className="mt-1 text-xs text-muted-foreground">{helpText}</p>}
        </div>
    );
}
