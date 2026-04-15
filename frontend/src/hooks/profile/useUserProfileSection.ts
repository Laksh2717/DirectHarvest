import { useCallback, useEffect, useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import {
    userService,
    type UserProfileResponse,
    type UpdateUserProfilePayload,
} from "@/services/userService";

type UserProfileSectionFormValues = UpdateUserProfilePayload;

export function useUserProfileSection() {
    const [profile, setProfile] = useState<UserProfileResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm<UserProfileSectionFormValues>({
        defaultValues: {
            name: "",
            email: "",
            street: "",
            city: "",
            state: "",
            pincode: "",
        },
    });


    const openEditModal = useCallback(() => {
        if (!profile) {
            return;
        }

        reset({
            name: profile.name ?? "",
            email: profile.email ?? "",
            street: profile.street ?? "",
            city: profile.city ?? "",
            state: profile.state ?? "",
            pincode: profile.pincode ?? "",
        });
        setIsEditModalOpen(true);
    }, [profile, reset]);

    const closeEditModal = useCallback(() => {
        if (isSubmitting) {
            return;
        }

        setIsEditModalOpen(false);
    }, [isSubmitting]);

    const onSubmit = useCallback(
        async (formData: UserProfileSectionFormValues) => {
            try {
                const response = await userService.updateMyProfile(formData);
                setProfile(response.data);
                setIsEditModalOpen(false);
                toast.success("Profile updated successfully");
            } catch (err: unknown) {
                toast.error(resolveApiErrorMessage(err));
            }
        },
        [],
    );

    useEffect(() => {
        let isMounted = true;

        const loadProfile = async () => {
            try {
                const response = await userService.getMyProfile();
                if (isMounted) {
                    setProfile(response.data);
                    setError(null);
                }
            } catch {
                if (isMounted) {
                    setError("Unable to load profile details right now.");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void loadProfile();

        return () => {
            isMounted = false;
        };
    }, []);

    return {
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
    };
}
