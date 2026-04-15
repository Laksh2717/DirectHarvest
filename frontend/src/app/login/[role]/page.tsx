"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import AuthPageShell from "@/components/auth/AuthPageShell";
import LoginFormFields from "@/components/auth/LoginFormFields";
import { getAuthRole, getAuthContent, getOppositeRole } from "@/lib/authContent";;
import { useLoginForm } from "@/hooks/auth/useLoginForm";
import { useGoogleAuth } from "@/hooks/auth/useGoogleAuth";

const Login = () => {
    const params = useParams<{ role: string }>();
    const role = getAuthRole(params.role);
    const {
        apiError,
        errors,
        isSubmitting,
        registerEmail,
        registerPassword,
        onSubmit,
        clearApiError,
        setApiErrorMessage,
    } = useLoginForm(role);
    const content = getAuthContent("login", role);
    const otherRole = getOppositeRole(role);
    const hasGoogleClientId = Boolean(process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID);
    const { googleLoading, handleGoogleAuth, handleGoogleAuthError } = useGoogleAuth({
        role,
        successMessage: "Google login successful!",
        defaultErrorMessage: "Google login failed. Please try again.",
        clearApiError,
        setApiErrorMessage,
    });

    return (
        <AuthPageShell
            mode="login"
            role={role}
            footer={
                <>
                    <p>
                        {content.footerPrompt}{" "}
                        <Link href={`/register/${role}`} className="font-medium text-primary hover:underline">
                            {content.footerLinkLabel}
                        </Link>
                    </p>
                    <p className="lg:hidden">
                        {content.switchPrompt}{" "}
                        <Link href={`/login/${otherRole}`} className="font-medium text-primary hover:underline">
                            {content.switchLinkLabel}
                        </Link>
                    </p>
                </>
            }
        >
            <LoginFormFields
                apiError={apiError}
                onSubmit={onSubmit}
                registerEmail={registerEmail}
                registerPassword={registerPassword}
                emailError={errors.email?.message}
                passwordError={errors.password?.message}
                isSubmitting={isSubmitting}
                hasGoogleClientId={hasGoogleClientId}
                googleLoading={googleLoading}
                onGoogleSuccess={handleGoogleAuth}
                onGoogleError={handleGoogleAuthError}
            />
        </AuthPageShell>
    );
};

export default Login;
