import type { AuthErrorBannerProps } from "@/types/auth";

export default function AuthErrorBanner({ message }: AuthErrorBannerProps) {
    return <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{message}</div>;
}
