"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import LoadingState from "@/components/ui/loading-state";
import { userService } from "@/services/userService";
import { sessionService } from "@/services/sessionService";
import type { DashboardRouteGuardProps } from "@/types/dashboard";

export default function DashboardRouteGuard({ expectedRole, loginPath, children }: DashboardRouteGuardProps) {
    const router = useRouter();
    const [checking, setChecking] = useState(true);

    useEffect(() => {
        let isMounted = true;

        const verifyAccess = async () => {
            const activeRole = sessionService.getActiveRole();
            if (activeRole !== expectedRole) {
                router.replace(loginPath);
                return;
            }

            try {
                await userService.getMyProfile();
                if (isMounted) {
                    setChecking(false);
                }
            } catch {
                router.replace(loginPath);
            }
        };

        void verifyAccess();

        return () => {
            isMounted = false;
        };
    }, [expectedRole, loginPath, router]);

    if (checking) {
        return (
            <div className="min-h-[calc(100vh-4rem)] bg-background px-6 py-10">
                <LoadingState
                    layout="inline"
                    message="Checking dashboard access..."
                    className="mx-auto flex max-w-3xl flex-col items-center justify-center"
                    cardClassName="px-6 py-16"
                />
            </div>
        );
    }

    return <>{children}</>;
}
