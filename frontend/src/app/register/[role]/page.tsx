"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import AuthPageShell from "@/components/auth/AuthPageShell";
import RegisterFormFields from "@/components/auth/RegisterFormFields";
import { getAuthRole, getAuthContent, getOppositeRole } from "@/lib/authContent";;
import { useRegisterForm } from "@/hooks/auth/useRegisterForm";
import { useGoogleAuth } from "@/hooks/auth/useGoogleAuth";

const Register = () => {
    const params = useParams<{ role: string }>();
    const role = getAuthRole(params.role);
    const {
        apiError,
        errors,
        isSubmitting,
        onSubmit,
        setApiErrorMessage,
        clearApiError,
        registerName,
        registerEmail,
        registerPassword,
        registerStreet,
        registerCity,
        registerState,
        registerPincode,
    } = useRegisterForm(role);
    const content = getAuthContent("register", role);
    const otherRole = getOppositeRole(role);
    const hasGoogleClientId = Boolean(process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID);
    const { googleLoading, handleGoogleAuth, handleGoogleAuthError } = useGoogleAuth({
        role,
        successMessage: "Google authentication successful!",
        defaultErrorMessage: "Google authentication failed. Please try again.",
        clearApiError,
        setApiErrorMessage,
    });

    return (
        <AuthPageShell
            mode="register"
            role={role}
            footer={
                <>
                    <p>
                        {content.footerPrompt}{" "}
                        <Link href={`/login/${role}`} className="font-medium text-primary hover:underline">
                            {content.footerLinkLabel}
                        </Link>
                    </p>
                    <p className="lg:hidden">
                        {content.switchPrompt}{" "}
                        <Link href={`/register/${otherRole}`} className="font-medium text-primary hover:underline">
                            {content.switchLinkLabel}
                        </Link>
                    </p>
                </>
            }
        >
            <RegisterFormFields
                apiError={apiError}
                onSubmit={onSubmit}
                isSubmitting={isSubmitting}
                registerName={registerName}
                registerEmail={registerEmail}
                registerPassword={registerPassword}
                registerStreet={registerStreet}
                registerCity={registerCity}
                registerState={registerState}
                registerPincode={registerPincode}
                nameError={errors.name?.message}
                emailError={errors.email?.message}
                passwordError={errors.password?.message}
                streetError={errors.street?.message}
                cityError={errors.city?.message}
                stateError={errors.state?.message}
                pincodeError={errors.pincode?.message}
                hasGoogleClientId={hasGoogleClientId}
                googleLoading={googleLoading}
                onGoogleSuccess={handleGoogleAuth}
                onGoogleError={handleGoogleAuthError}
            />
        </AuthPageShell>
    );
};

export default Register;
