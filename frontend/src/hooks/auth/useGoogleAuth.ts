import { useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useRouter } from "next/navigation";
import type { CredentialResponse } from "@react-oauth/google";
import { toast } from "sonner";
import { authService } from "@/services/authService";
import type { UseGoogleAuthParams } from "@/types/auth";

export function useGoogleAuth({
    role,
    successMessage,
    defaultErrorMessage,
    clearApiError,
    setApiErrorMessage,
}: UseGoogleAuthParams) {
    const router = useRouter();
    const [googleLoading, setGoogleLoading] = useState(false);

    const handleGoogleAuth = async (credentialResponse: CredentialResponse) => {
        const idToken = credentialResponse.credential;
        if (!idToken) {
            setApiErrorMessage(defaultErrorMessage);
            return;
        }

        clearApiError();
        setGoogleLoading(true);

        try {
            await authService.googleLogin({
                idToken,
                role: role === "farmer" ? "FARMER" : "BUYER",
            });
            toast.success(successMessage);
            router.push(role === "farmer" ? "/farmer" : "/buyer");
        } catch (err: unknown) {
            setApiErrorMessage(resolveApiErrorMessage(err, defaultErrorMessage));
        } finally {
            setGoogleLoading(false);
        }
    };

    const handleGoogleAuthError = () => {
        setApiErrorMessage(defaultErrorMessage);
    };

    return {
        googleLoading,
        handleGoogleAuth,
        handleGoogleAuthError,
    };
}
