import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import ModalLayout from "@/components/modals/ModalLayout";
import type { UserProfileUpdateModalProps } from "@/types/modal";

export default function UserProfileUpdateModal({
    open,
    isSubmitting,
    register,
    handleSubmit,
    onSubmit,
    errors,
    onClose,
}: UserProfileUpdateModalProps) {
    return (
        <ModalLayout
            open={open}
            title="Update Profile"
            onClose={onClose}
            closeAriaLabel="Close update profile modal"
            backdropAriaLabel="Close update profile modal backdrop"
            maxWidth="lg"
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-3" noValidate>
                <div>
                    <label htmlFor="update-name" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        Name
                    </label>
                    <Input
                        id="update-name"
                        placeholder="Name"
                        {...register("name", { required: "Name is required" })}
                        className={errors.name ? "border-destructive" : ""}
                    />
                    {errors.name ? <p className="mt-1 text-xs text-destructive">{errors.name.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="update-email" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        Email
                    </label>
                    <Input
                        id="update-email"
                        type="email"
                        placeholder="Email"
                        {...register("email", {
                            required: "Email is required",
                            pattern: {
                                value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                                message: "Please enter a valid email address",
                            },
                        })}
                        className={errors.email ? "border-destructive" : ""}
                    />
                    {errors.email ? <p className="mt-1 text-xs text-destructive">{errors.email.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="update-street" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        Street
                    </label>
                    <Input
                        id="update-street"
                        placeholder="Street"
                        {...register("street", { required: "Street is required" })}
                        className={errors.street ? "border-destructive" : ""}
                    />
                    {errors.street ? <p className="mt-1 text-xs text-destructive">{errors.street.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="update-city" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        City
                    </label>
                    <Input
                        id="update-city"
                        placeholder="City"
                        {...register("city", { required: "City is required" })}
                        className={errors.city ? "border-destructive" : ""}
                    />
                    {errors.city ? <p className="mt-1 text-xs text-destructive">{errors.city.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="update-state" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        State
                    </label>
                    <Input
                        id="update-state"
                        placeholder="State"
                        {...register("state", { required: "State is required" })}
                        className={errors.state ? "border-destructive" : ""}
                    />
                    {errors.state ? <p className="mt-1 text-xs text-destructive">{errors.state.message}</p> : null}
                </div>

                <div>
                    <label htmlFor="update-pincode" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">
                        Pincode
                    </label>
                    <Input
                        id="update-pincode"
                        placeholder="Pincode"
                        maxLength={6}
                        {...register("pincode", {
                            required: "Pincode is required",
                            pattern: {
                                value: /^\d{6}$/,
                                message: "Pincode must be exactly 6 digits",
                            },
                        })}
                        className={errors.pincode ? "border-destructive" : ""}
                    />
                    {errors.pincode ? <p className="mt-1 text-xs text-destructive">{errors.pincode.message}</p> : null}
                </div>

                <div className="flex flex-col-reverse gap-3 pt-1 sm:flex-row sm:justify-end">
                    <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
                        Cancel
                    </Button>
                    <Button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Saving..." : "Submit"}
                    </Button>
                </div>
            </form>
        </ModalLayout>
    );
}
