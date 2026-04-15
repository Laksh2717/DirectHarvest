import type { LucideIcon } from "lucide-react";
import type { UserRole } from "@/types/common";
import { BaseSyntheticEvent, ReactNode } from "react";
import { CredentialResponse } from "@react-oauth/google";
import { UseFormRegisterReturn } from "react-hook-form";

export type AuthMode = "login" | "register";
export type AuthRole = "farmer" | "buyer";

export type AuthContent = {
    leftHeadline: string;
    leftDescription: string;
    formHeadline: string;
    switchPrompt: string;
    switchLinkLabel: string;
    footerPrompt: string;
    footerLinkLabel: string;
};

export type AuthRoleContent = {
    icon: LucideIcon;
    leftIconClassName: string;
    mobileIconClassName: string;
    byMode: Record<AuthMode, AuthContent>;
};

export interface AuthResponse {
    userId: number;
    name: string;
    email: string;
    role: UserRole;
    provider: string;
    accessToken: string;
    refreshToken: string;
    accessTokenExpiresAt: string;
    refreshTokenExpiresAt: string;
}

export interface RegisterPayload {
    name: string;
    email: string;
    password: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
    role: UserRole;
}

export interface LoginPayload {
    email: string;
    password: string;
    role: UserRole;
}

export interface GoogleLoginPayload {
    idToken: string;
    role?: UserRole;
}

export type UseGoogleAuthParams = {
    role: AuthRole;
    successMessage: string;
    defaultErrorMessage: string;
    clearApiError: () => void;
    setApiErrorMessage: (message: string) => void;
};

export type LoginFormValues = {
    email: string;
    password: string;
    role?: UserRole;
};

export type RegisterFormValues = {
    name: string;
    email: string;
    password: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
};

export type AuthErrorBannerProps = {
    message: string;
};

export type AuthPageShellProps = {
    mode: AuthMode;
    role: AuthRole;
    children: ReactNode;
    footer: ReactNode;
};

export type GoogleAuthSectionProps = {
    mode: AuthMode;
    enabled: boolean;
    loading: boolean;
    onSuccess: (credentialResponse: CredentialResponse) => void;
    onError: () => void;
};

export type LoginFormFieldsProps = {
    apiError: string | null;
    onSubmit: (event?: BaseSyntheticEvent) => unknown;
    registerEmail: UseFormRegisterReturn;
    registerPassword: UseFormRegisterReturn;
    emailError?: string;
    passwordError?: string;
    isSubmitting: boolean;
    hasGoogleClientId: boolean;
    googleLoading: boolean;
    onGoogleSuccess: (credentialResponse: CredentialResponse) => void;
    onGoogleError: () => void;
};

export type PasswordFieldProps = {
    id: string;
    placeholder: string;
    registerProps: UseFormRegisterReturn;
    errorMessage?: string;
};

export type RegisterFormFieldsProps = {
    apiError: string | null;
    onSubmit: (event?: BaseSyntheticEvent) => unknown;
    isSubmitting: boolean;
    registerName: UseFormRegisterReturn;
    registerEmail: UseFormRegisterReturn;
    registerPassword: UseFormRegisterReturn;
    registerStreet: UseFormRegisterReturn;
    registerCity: UseFormRegisterReturn;
    registerState: UseFormRegisterReturn;
    registerPincode: UseFormRegisterReturn;
    nameError?: string;
    emailError?: string;
    passwordError?: string;
    streetError?: string;
    cityError?: string;
    stateError?: string;
    pincodeError?: string;
    hasGoogleClientId: boolean;
    googleLoading: boolean;
    onGoogleSuccess: (credentialResponse: CredentialResponse) => void;
    onGoogleError: () => void;
};