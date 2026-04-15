import { Input } from "@/components/ui/input";
import FormModal from "@/components/ui/form-modal";
import { formatCurrency, toTitleCase } from "@/lib/formatters";
import type { ListingUpdatePriceModalProps } from "@/types/modal";

export default function ListingUpdatePriceModal({ listing, updatePriceForm, actionLoading, onCancel }: ListingUpdatePriceModalProps) {
    return (
        <FormModal
            open={updatePriceForm.open}
            title={`Update Price - ${toTitleCase(listing.cropName)}`}
            description="Change the listing price per kg."
            submitText="Submit"
            loading={actionLoading === "updatePrice" || updatePriceForm.isSubmitting}
            onCancel={onCancel}
            onSubmit={updatePriceForm.handleSubmit(updatePriceForm.onSubmit)}
        >
            <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                    Current price: <span className="font-semibold text-foreground">{formatCurrency(listing.pricePerKg)}</span>
                </p>
                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Price per kg</label>
                    <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="250"
                        {...updatePriceForm.register("pricePerKg", {
                            required: "Price is required",
                            validate: (value) => {
                                const parsed = Number(value);
                                if (!Number.isFinite(parsed) || parsed <= 0) {
                                    return "Price must be greater than 0";
                                }
                                return true;
                            },
                        })}
                        className={updatePriceForm.errors.pricePerKg ? "border-destructive" : undefined}
                    />
                    {updatePriceForm.errors.pricePerKg ? <p className="mt-1 text-xs text-destructive">{updatePriceForm.errors.pricePerKg.message}</p> : null}
                </div>
            </div>
        </FormModal>
    );
}
