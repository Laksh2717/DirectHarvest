import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import AuthErrorBanner from "@/components/auth/AuthErrorBanner";
import PasswordField from "@/components/auth/PasswordField";
import GoogleAuthSection from "@/components/auth/GoogleAuthSection";
import type { LoginFormFieldsProps } from "@/types/auth";

export default function LoginFormFields({
    apiError,
    onSubmit,
    registerEmail,
    registerPassword,
    emailError,
    passwordError,
    isSubmitting,
    hasGoogleClientId,
    googleLoading,
    onGoogleSuccess,
    onGoogleError,
}: LoginFormFieldsProps) {
    return (
        <>
            {apiError ? <AuthErrorBanner message={apiError} /> : null}

            <form onSubmit={onSubmit} className="space-y-4" autoComplete="off" noValidate>
                <div>
                    <Input
                        id="email"
                        type="email"
                        placeholder="Email"
                        autoComplete="off"
                        spellCheck={false}
                        data-lpignore="true"
                        data-1p-ignore="true"
                        className={`h-10 ${emailError ? "border-destructive" : ""}`}
                        {...registerEmail}
                    />
                    {emailError ? <p className="mt-1 text-xs text-destructive">{emailError}</p> : null}
                </div>

                <PasswordField id="password" placeholder="Password" registerProps={registerPassword} errorMessage={passwordError} />

                <Button type="submit" className="h-10 w-full gap-2 font-semibold" disabled={isSubmitting}>
                    {isSubmitting ? "Logging in..." : "Login"}
                    {!isSubmitting ? <ArrowRight className="h-4 w-4" /> : null}
                </Button>

                <GoogleAuthSection
                    mode="login"
                    enabled={hasGoogleClientId}
                    loading={googleLoading}
                    onSuccess={onGoogleSuccess}
                    onError={onGoogleError}
                />
            </form>
        </>
    );
}
