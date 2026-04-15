import { Input } from "@/components/ui/input";
import FormModal from "@/components/ui/form-modal";
import { toTitleCase } from "@/lib/formatters";
import type { ListingUpdateDetailsModalProps } from "@/types/modal";

export default function ListingUpdateDetailsModal({ listing, updateDetailsForm, actionLoading, onCancel }: ListingUpdateDetailsModalProps) {
    return (
        <FormModal
            open={updateDetailsForm.open}
            title={`Update Details - ${toTitleCase(listing.cropName)}`}
            description="Update listing details and address information."
            submitText="Submit"
            loading={actionLoading === "updateDetails" || updateDetailsForm.isSubmitting}
            onCancel={onCancel}
            onSubmit={updateDetailsForm.handleSubmit(updateDetailsForm.onSubmit)}
        >
            <div className="grid gap-3">
                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Crop Name</label>
                    <Input
                        {...updateDetailsForm.register("cropName", { required: "Crop name is required" })}
                        className={updateDetailsForm.errors.cropName ? "border-destructive" : undefined}
                    />
                    {updateDetailsForm.errors.cropName ? <p className="mt-1 text-xs text-destructive">{updateDetailsForm.errors.cropName.message}</p> : null}
                </div>

                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Description</label>
                    <textarea
                        {...updateDetailsForm.register("description")}
                        rows={3}
                        className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    />
                </div>

                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Street</label>
                    <Input
                        {...updateDetailsForm.register("street", { required: "Street is required" })}
                        className={updateDetailsForm.errors.street ? "border-destructive" : undefined}
                    />
                    {updateDetailsForm.errors.street ? <p className="mt-1 text-xs text-destructive">{updateDetailsForm.errors.street.message}</p> : null}
                </div>

                <div className="grid gap-3 sm:grid-cols-2">
                    <div>
                        <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">City</label>
                        <Input
                            {...updateDetailsForm.register("city", { required: "City is required" })}
                            className={updateDetailsForm.errors.city ? "border-destructive" : undefined}
                        />
                        {updateDetailsForm.errors.city ? <p className="mt-1 text-xs text-destructive">{updateDetailsForm.errors.city.message}</p> : null}
                    </div>
                    <div>
                        <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">State</label>
                        <Input
                            {...updateDetailsForm.register("state", { required: "State is required" })}
                            className={updateDetailsForm.errors.state ? "border-destructive" : undefined}
                        />
                        {updateDetailsForm.errors.state ? <p className="mt-1 text-xs text-destructive">{updateDetailsForm.errors.state.message}</p> : null}
                    </div>
                </div>

                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Pincode</label>
                    <Input
                        maxLength={6}
                        {...updateDetailsForm.register("pincode", {
                            required: "Pincode is required",
                            pattern: {
                                value: /^\d{6}$/,
                                message: "Pincode must be exactly 6 digits",
                            },
                        })}
                        className={updateDetailsForm.errors.pincode ? "border-destructive" : undefined}
                    />
                    {updateDetailsForm.errors.pincode ? <p className="mt-1 text-xs text-destructive">{updateDetailsForm.errors.pincode.message}</p> : null}
                </div>
            </div>
        </FormModal>
    );
}
