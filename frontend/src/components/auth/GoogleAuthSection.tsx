import { GoogleLogin } from "@react-oauth/google";
import type { GoogleAuthSectionProps } from "@/types/auth";

export default function GoogleAuthSection({ mode, enabled, loading, onSuccess, onError }: GoogleAuthSectionProps) {
    return (
        <>
            <div className="relative">
                <div className="absolute inset-0 flex items-center">
                    <span className="w-full border-t border-border" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-background px-2 text-muted-foreground">Or</span>
                </div>
            </div>

            {enabled ? (
                <div className="space-y-2">
                    <GoogleLogin
                        text={mode === "login" ? "signin_with" : "signup_with"}
                        shape="pill"
                        onSuccess={onSuccess}
                        onError={onError}
                    />
                    {loading ? (
                        <p className="text-center text-xs text-muted-foreground">
                            {mode === "login" ? "Signing in with Google..." : "Connecting with Google..."}
                        </p>
                    ) : null}
                </div>
            ) : (
                <p className="text-center text-xs text-muted-foreground">Google login is not configured.</p>
            )}
        </>
    );
}
