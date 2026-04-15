"use client";

import { Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import LoadingState from "@/components/ui/loading-state";
import UserProfileUpdateModal from "@/components/modals/UserProfileUpdateModal";
import { useUserProfileSection } from "@/hooks/profile/useUserProfileSection";
import { toTitleCase, formatRating } from "@/lib/formatters";
import type { FieldItemProps, UserProfileSectionProps } from "@/types/user";

const FieldItem = ({ label, value, shouldCapitalize = true }: FieldItemProps) => (
    <div className="rounded-xl border border-border/70 bg-background px-4 py-3">
        <div className="flex items-center gap-2 text-sm">
            <p className="uppercase tracking-wide text-muted-foreground">{label}:</p>
            <p className="font-semibold text-foreground">{shouldCapitalize ? toTitleCase(value) : value}</p>
        </div>
    </div>
);

export default function UserProfileSection({ role }: UserProfileSectionProps) {
    const {
        profile,
        loading,
        error,
        isEditModalOpen,
        register,
        handleSubmit,
        errors,
        isSubmitting,
        openEditModal,
        closeEditModal,
        onSubmit,
    } = useUserProfileSection();

    if (loading) {
        return <LoadingState message="Loading profile..." />;
    }

    if (error || !profile) {
        return (
            <div className="flex min-h-[60vh] items-center justify-center">
                <div className="w-full max-w-lg rounded-2xl border border-destructive/30 bg-card p-10 text-center shadow-(--shadow-card)">
                    <p className="text-sm font-medium text-destructive">{error ?? "Profile unavailable."}</p>
                </div>
            </div>
        );
    }

    return (
        <section className="mx-auto w-full max-w-4xl space-y-4">
            <div className="space-y-3">
                <FieldItem label="Name" value={profile.name || "-"} />
                <FieldItem label="Email" value={profile.email || "-"} shouldCapitalize={false} />
                <FieldItem label="Street" value={profile.street || "-"} />
                <FieldItem label="City" value={profile.city || "-"} />
                <FieldItem label="State" value={profile.state || "-"} />
                <FieldItem label="Pincode" value={profile.pincode || "-"} shouldCapitalize={false} />

                {role === "farmer" ? (
                    <div className="rounded-xl border border-border/70 bg-background p-3">
                        <div className="flex items-center gap-2 text-sm text-foreground">
                            <p className="uppercase tracking-wide text-muted-foreground">Average Rating:</p>
                            <Star className="h-4 w-4 text-secondary" />
                            <p className="text-sm font-semibold">{formatRating(profile.averageRating)}</p>
                        </div>
                    </div>
                ) : null}

                {role === "farmer" ? (
                    <FieldItem label="Rating Count" value={String(profile.ratingCount ?? 0)} />
                ) : null}
            </div>

            <Button type="button" className="cursor-pointer" onClick={openEditModal}>
                Update Profile
            </Button>

            <UserProfileUpdateModal
                open={isEditModalOpen}
                isSubmitting={isSubmitting}
                register={register}
                handleSubmit={handleSubmit}
                onSubmit={onSubmit}
                errors={errors}
                onClose={closeEditModal}
            />
        </section>
    );
}
