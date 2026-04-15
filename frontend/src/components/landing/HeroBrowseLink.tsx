"use client";

import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { useSyncExternalStore } from "react";
import { sessionService } from "@/services/sessionService";

export default function HeroBrowseLink() {
    const activeRole = useSyncExternalStore(
        sessionService.subscribe,
        () => sessionService.getActiveRole(),
        () => null,
    );

    const isLoggedIn = activeRole === "FARMER" || activeRole === "BUYER";

    if (isLoggedIn) {
        return null;
    }

    return (
        <Link
            href="/browse"
            className="inline-flex items-center gap-1 text-primary-foreground/70 hover:text-primary-foreground text-sm underline underline-offset-4 transition-colors animate-fade-up mt-8"
            style={{ animationDelay: "0.6s" }}
        >
            Or browse products without an account
            <ArrowRight className="h-3.5 w-3.5" />
        </Link>
    );
}
