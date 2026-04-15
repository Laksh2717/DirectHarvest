import { Input } from "@/components/ui/input";
import FormModal from "@/components/ui/form-modal";
import { formatQuantity, toTitleCase } from "@/lib/formatters";
import type { ListingAddQuantityModalProps } from "@/types/modal";

export default function ListingAddQuantityModal({ listing, addQtyForm, actionLoading, onCancel }: ListingAddQuantityModalProps) {
    return (
        <FormModal
            open={addQtyForm.open}
            title={`Add Quantity - ${toTitleCase(listing.cropName)}`}
            description="Increase the current listing quantity."
            submitText="Submit"
            loading={actionLoading === "addQuantity" || addQtyForm.isSubmitting}
            onCancel={onCancel}
            onSubmit={addQtyForm.handleSubmit(addQtyForm.onSubmit)}
        >
            <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                    Current quantity: <span className="font-semibold text-foreground">{formatQuantity(listing.quantity)} KG</span>
                </p>
                <div className="flex items-center gap-3">
                    <div className="flex-1">
                        <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Quantity to add</label>
                        <Input
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="10"
                            {...addQtyForm.register("quantity", {
                                required: "Quantity is required",
                                validate: (value) => {
                                    const parsed = Number(value);
                                    if (!Number.isFinite(parsed) || parsed <= 0) {
                                        return "Quantity must be greater than 0";
                                    }
                                    return true;
                                },
                            })}
                            className={addQtyForm.errors.quantity ? "border-destructive" : undefined}
                        />
                        {addQtyForm.errors.quantity ? <p className="mt-1 text-xs text-destructive">{addQtyForm.errors.quantity.message}</p> : null}
                    </div>
                    <div className="mt-6 rounded-md border border-border bg-muted px-3 py-2 text-sm font-medium text-foreground">KG</div>
                </div>
            </div>
        </FormModal>
    );
}
