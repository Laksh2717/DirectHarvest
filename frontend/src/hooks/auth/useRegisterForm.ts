import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { authService } from "@/services/authService";
import { validateEmail, validatePassword, validatePincode, validateRequired } from "@/lib/validators";
import type { AuthRole } from "@/types/auth";
import type { RegisterFormValues } from "@/types/auth";
import type { ApiErrorResponse } from "@/types/common";

export function useRegisterForm(role: AuthRole) {
    const router = useRouter();
    const isFarmer = role === "farmer";
    const [apiError, setApiError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<RegisterFormValues>({
        defaultValues: {
            name: "",
            email: "",
            password: "",
            street: "",
            city: "",
            state: "",
            pincode: "",
        },
    });

    const onSubmit = handleSubmit(async (values) => {
        setApiError(null);

        try {
            await authService.register({
                ...values,
                role: isFarmer ? "FARMER" : "BUYER",
            });
            toast.success("Registration successful!");
            router.push(`/login/${role}`);
        } catch (err: unknown) {
            if (err && typeof err === "object" && "response" in err) {
                const axiosErr = err as { response?: { data?: ApiErrorResponse } };
                const responseData = axiosErr.response?.data;
                const backendFieldErrors = responseData?.fieldErrors ?? [];

                backendFieldErrors.forEach((fieldError) => {
                    if (!fieldError.field || !fieldError.message) {
                        return;
                    }

                    if (
                        fieldError.field === "name" ||
                        fieldError.field === "email" ||
                        fieldError.field === "password" ||
                        fieldError.field === "street" ||
                        fieldError.field === "city" ||
                        fieldError.field === "state" ||
                        fieldError.field === "pincode"
                    ) {
                        setError(fieldError.field, {
                            type: "server",
                            message: fieldError.message,
                        });
                    }
                });

                if (backendFieldErrors.length === 0) {
                    setApiError(responseData?.message ?? "Registration failed. Please try again.");
                }
            } else {
                setApiError("Something went wrong. Please try again.");
            }
        }
    });

    const clearApiError = () => setApiError(null);
    const setApiErrorMessage = (message: string) => setApiError(message);

    return {
        isFarmer,
        apiError,
        errors,
        isSubmitting,
        onSubmit,
        clearApiError,
        setApiErrorMessage,
        registerName: register("name", {
            validate: (value) => validateRequired(value, "Name") ?? true,
            onChange: () => clearErrors("name"),
        }),
        registerEmail: register("email", {
            validate: (value) => validateEmail(value) ?? true,
            onChange: () => clearErrors("email"),
        }),
        registerPassword: register("password", {
            validate: (value) => validatePassword(value) ?? true,
            onChange: () => clearErrors("password"),
        }),
        registerStreet: register("street", {
            validate: (value) => validateRequired(value, "Street address") ?? true,
            onChange: () => clearErrors("street"),
        }),
        registerCity: register("city", {
            validate: (value) => validateRequired(value, "City") ?? true,
            onChange: () => clearErrors("city"),
        }),
        registerState: register("state", {
            validate: (value) => validateRequired(value, "State") ?? true,
            onChange: () => clearErrors("state"),
        }),
        registerPincode: register("pincode", {
            validate: (value) => validatePincode(value) ?? true,
            onChange: () => clearErrors("pincode"),
        }),
    };
}
