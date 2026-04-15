import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import AuthErrorBanner from "@/components/auth/AuthErrorBanner";
import PasswordField from "@/components/auth/PasswordField";
import GoogleAuthSection from "@/components/auth/GoogleAuthSection";
import type { RegisterFormFieldsProps } from "@/types/auth";

export default function RegisterFormFields({
    apiError,
    onSubmit,
    isSubmitting,
    registerName,
    registerEmail,
    registerPassword,
    registerStreet,
    registerCity,
    registerState,
    registerPincode,
    nameError,
    emailError,
    passwordError,
    streetError,
    cityError,
    stateError,
    pincodeError,
    hasGoogleClientId,
    googleLoading,
    onGoogleSuccess,
    onGoogleError,
}: RegisterFormFieldsProps) {
    return (
        <>
            {apiError ? <AuthErrorBanner message={apiError} /> : null}

            <form onSubmit={onSubmit} className="space-y-3" autoComplete="off" noValidate>
                <div className="grid grid-cols-2 gap-3">
                    <div className="col-span-2">
                        <Input
                            id="name"
                            placeholder="Full Name"
                            autoComplete="off"
                            spellCheck={false}
                            data-lpignore="true"
                            data-1p-ignore="true"
                            className={`h-10 ${nameError ? "border-destructive" : ""}`}
                            {...registerName}
                        />
                        {nameError ? <p className="mt-1 text-xs text-destructive">{nameError}</p> : null}
                    </div>

                    <div className="col-span-2">
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

                    <div className="col-span-2">
                        <PasswordField id="password" placeholder="Password" registerProps={registerPassword} errorMessage={passwordError} />
                    </div>

                    <div className="col-span-2">
                        <Input
                            id="street"
                            placeholder="Street Address"
                            autoComplete="off"
                            data-lpignore="true"
                            data-1p-ignore="true"
                            className={`h-10 ${streetError ? "border-destructive" : ""}`}
                            {...registerStreet}
                        />
                        {streetError ? <p className="mt-1 text-xs text-destructive">{streetError}</p> : null}
                    </div>

                    <div>
                        <Input
                            id="city"
                            placeholder="City"
                            autoComplete="off"
                            data-lpignore="true"
                            data-1p-ignore="true"
                            className={`h-10 ${cityError ? "border-destructive" : ""}`}
                            {...registerCity}
                        />
                        {cityError ? <p className="mt-1 text-xs text-destructive">{cityError}</p> : null}
                    </div>

                    <div>
                        <Input
                            id="state"
                            placeholder="State"
                            autoComplete="off"
                            data-lpignore="true"
                            data-1p-ignore="true"
                            className={`h-10 ${stateError ? "border-destructive" : ""}`}
                            {...registerState}
                        />
                        {stateError ? <p className="mt-1 text-xs text-destructive">{stateError}</p> : null}
                    </div>

                    <div className="col-span-2">
                        <Input
                            id="pincode"
                            placeholder="Pincode"
                            autoComplete="off"
                            data-lpignore="true"
                            data-1p-ignore="true"
                            className={`h-10 ${pincodeError ? "border-destructive" : ""}`}
                            {...registerPincode}
                        />
                        {pincodeError ? <p className="mt-1 text-xs text-destructive">{pincodeError}</p> : null}
                    </div>
                </div>

                <Button type="submit" className="h-10 w-full gap-2 font-semibold" disabled={isSubmitting}>
                    {isSubmitting ? "Creating Account..." : "Create Account"}
                    {!isSubmitting ? <ArrowRight className="h-4 w-4" /> : null}
                </Button>

                <GoogleAuthSection
                    mode="register"
                    enabled={hasGoogleClientId}
                    loading={googleLoading}
                    onSuccess={onGoogleSuccess}
                    onError={onGoogleError}
                />
            </form>
        </>
    );
}
