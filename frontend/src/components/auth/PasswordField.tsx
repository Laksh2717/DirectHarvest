"use client";

import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { Input } from "@/components/ui/input";
import type { PasswordFieldProps } from "@/types/auth";

export default function PasswordField({ id, placeholder, registerProps, errorMessage }: PasswordFieldProps) {
    const [showPassword, setShowPassword] = useState(false);

    return (
        <div>
            <div className="relative">
                <Input
                    id={id}
                    type={showPassword ? "text" : "password"}
                    placeholder={placeholder}
                    autoComplete="new-password"
                    data-lpignore="true"
                    data-1p-ignore="true"
                    className={`h-10 pr-10 ${errorMessage ? "border-destructive" : ""}`}
                    {...registerProps}
                />
                <button
                    type="button"
                    onClick={() => setShowPassword((prev) => !prev)}
                    className="absolute inset-y-0 right-0 flex items-center px-3 text-muted-foreground hover:text-foreground"
                    aria-label={showPassword ? "Hide password" : "Show password"}
                >
                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
            </div>
            {errorMessage ? <p className="mt-1 text-xs text-destructive">{errorMessage}</p> : null}
        </div>
    );
}
