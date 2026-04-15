import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export function resolveApiErrorMessage(err: unknown, fallback: string = "Something went wrong."): string {
    if (err && typeof err === "object" && "response" in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        return axiosErr.response?.data?.message ?? fallback;
    }
    return fallback;
}

export const resolveErrorMessage = (err: unknown, fallback: string) => (err instanceof Error && err.message.trim() ? err.message : fallback);  

export const parsePositiveNumberError = (value: string, label: string) => {
    if (!value.trim()) {
        return `${label} is required`;
    }

    const parsed = Number(value);
    if (Number.isNaN(parsed) || parsed <= 0) {
        return `${label} must be greater than 0`;
    }

    return null;
};
