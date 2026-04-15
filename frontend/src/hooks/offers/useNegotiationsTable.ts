"use client";

import { useEffect, useMemo, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { negotiationService } from "@/services/negotiationService";
import type { NegotiationResponse, NegotiationsTab, UseNegotiationsTableParams } from "@/types/offer";

export function useNegotiationsTable({ role }: UseNegotiationsTableParams) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();

    const [negotiations, setNegotiations] = useState<NegotiationResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<NegotiationsTab>("ACTIVE");
    const [selectedNegotiationId, setSelectedNegotiationId] = useState<number | null>(null);
    const [detailsOpen, setDetailsOpen] = useState(false);
    const [reloadVersion, setReloadVersion] = useState(0);

    const tabOptions: Array<{ value: NegotiationsTab; label: string }> = [
        { value: "ACTIVE", label: "Active" },
        { value: "REJECTED", label: "Rejected" },
        { value: "EXPIRED", label: "Expired" },
    ];

    const getTabStatuses = (tab: NegotiationsTab): NegotiationResponse["status"][] => {
        switch (tab) {
            case "ACTIVE":
                return ["PENDING_FARMER", "PENDING_BUYER"];
            case "REJECTED":
                return ["REJECTED"];
            case "EXPIRED":
                return ["EXPIRED"];
        }
    };

    const isMyTurn = (negotiation: NegotiationResponse) =>
        (role === "buyer" && negotiation.status === "PENDING_BUYER") ||
        (role === "farmer" && negotiation.status === "PENDING_FARMER");

    useEffect(() => {
        const tabFromQuery = searchParams.get("tab");
        if (tabFromQuery === "ACTIVE" || tabFromQuery === "REJECTED" || tabFromQuery === "EXPIRED") {
            if (tabFromQuery !== activeTab) {
                setActiveTab(tabFromQuery);
            }
        }
    }, [activeTab, searchParams]);

    useEffect(() => {
        let isMounted = true;

        const loadNegotiations = async () => {
            setLoading(true);
            try {
                const data = await negotiationService.getMyNegotiations(getTabStatuses(activeTab));
                if (isMounted) {
                    setNegotiations(data);
                    setError(null);
                }
            } catch {
                if (isMounted) {
                    setError("Unable to load offers and negotiations right now.");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadNegotiations();

        return () => {
            isMounted = false;
        };
    }, [activeTab, reloadVersion]);

    const tableRows = useMemo(
        () =>
            negotiations.map((negotiation) => {
                const counterpartName = role === "buyer" ? negotiation.farmerName : negotiation.buyerName;
                return {
                    ...negotiation,
                    counterpartName,
                };
            }),
        [negotiations, role],
    );

    const counterpartLabel = role === "buyer" ? "Farmer" : "Buyer";
    const roleEnum = role === "buyer" ? "BUYER" : "FARMER";
    const emptyLabel =
        activeTab === "ACTIVE"
            ? "No active negotiations found."
            : activeTab === "REJECTED"
                ? "No rejected negotiations found."
                : "No expired negotiations found.";

    const getCancelledByLabel = (negotiation: NegotiationResponse) => {
        if (!negotiation.cancelledBy) {
            return "-";
        }

        return negotiation.cancelledBy === roleEnum ? "You" : counterpartLabel;
    };

    const getLastOfferByLabel = (negotiation: NegotiationResponse) =>
        negotiation.proposedBy === roleEnum ? "You" : counterpartLabel;

    const handleOpenDetails = (negotiationId: number) => {
        setSelectedNegotiationId(negotiationId);
        setDetailsOpen(true);
    };

    const handleCloseDetails = () => {
        setDetailsOpen(false);
        setSelectedNegotiationId(null);
    };

    const handleActionComplete = (target: "ACTIVE" | "REJECTED" | "ACTIVE_ORDERS") => {
        handleCloseDetails();

        if (target === "ACTIVE_ORDERS") {
            router.push(`/${role}/active-orders`);
            return;
        }

        const nextTab: NegotiationsTab = target;
        setActiveTab(nextTab);
        setReloadVersion((prev) => prev + 1);

        const params = new URLSearchParams(searchParams.toString());
        params.set("tab", nextTab);
        router.replace(`${pathname}?${params.toString()}`);
    };

    const handleTabChange = (tab: NegotiationsTab) => {
        setActiveTab(tab);
        const params = new URLSearchParams(searchParams.toString());
        params.set("tab", tab);
        router.replace(`${pathname}?${params.toString()}`);
    };

    return {
        negotiations,
        loading,
        error,
        activeTab,
        tabOptions,
        tableRows,
        emptyLabel,
        counterpartLabel,
        isMyTurn,
        getCancelledByLabel,
        getLastOfferByLabel,
        handleOpenDetails,
        handleCloseDetails,
        handleActionComplete,
        handleTabChange,
        selectedNegotiationId,
        detailsOpen,
    };
}
