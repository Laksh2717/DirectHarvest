import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { authService } from "@/services/authService";
import { validateEmail, validatePassword } from "@/lib/validators";
import type { AuthRole } from "@/types/auth";
import type { LoginFormValues } from "@/types/auth";
import type { ApiErrorResponse } from "@/types/common";
import type { UserRole } from "@/types/common";

export function useLoginForm(role: AuthRole) {
    const router = useRouter();
    const isFarmer = role === "farmer";
    const [apiError, setApiError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormValues>({
        defaultValues: {
            email: "",
            password: "",
        },
    });

    const onSubmit = handleSubmit(async (values) => {
        setApiError(null);

        // Add role to values before sending to backend
        const userRole: UserRole = isFarmer ? "FARMER" : "BUYER";
        const payload = { ...values, role: userRole };

        try {
            await authService.login(payload);
            toast.success("Login successful!");
            router.push(isFarmer ? "/farmer" : "/buyer");
        } catch (err: unknown) {
            if (err && typeof err === "object" && "response" in err) {
                const axiosErr = err as { response?: { data?: ApiErrorResponse } };
                const responseData = axiosErr.response?.data;
                const backendFieldErrors = responseData?.fieldErrors ?? [];

                backendFieldErrors.forEach((fieldError) => {
                    if (!fieldError.field || !fieldError.message) {
                        return;
                    }

                    if (fieldError.field === "email" || fieldError.field === "password") {
                        setError(fieldError.field, {
                            type: "server",
                            message: fieldError.message,
                        });
                    }
                });

                if (backendFieldErrors.length === 0) {
                    setApiError(responseData?.message ?? "Login failed. Please try again.");
                }
            } else {
                setApiError("Something went wrong. Please try again.");
            }
        }
    });

    const registerEmail = register("email", {
        validate: (value) => validateEmail(value) ?? true,
        onChange: () => {
            clearErrors("email");
        },
    });

    const registerPassword = register("password", {
        validate: (value) => validatePassword(value) ?? true,
        onChange: () => {
            clearErrors("password");
        },
    });

    const clearApiError = () => setApiError(null);
    const setApiErrorMessage = (message: string) => setApiError(message);

    return {
        isFarmer,
        apiError,
        errors,
        isSubmitting,
        registerEmail,
        registerPassword,
        onSubmit,
        clearApiError,
        setApiErrorMessage,
    };
}
