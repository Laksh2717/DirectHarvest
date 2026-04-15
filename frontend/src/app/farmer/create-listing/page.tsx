"use client";

import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useCreateListingForm } from "@/hooks/listings/useCreateListingForm";

export default function CreateListing() {
    const {
        errors,
        images,
        imageError,
        isSubmitting,
        loadingAddress,
        onSubmit,
        handleImageChange,
        handleUseProfileAddress,
        registerCropName,
        registerQuantity,
        registerPricePerKg,
        registerDescription,
        registerStreet,
        registerCity,
        registerState,
        registerPincode,
    } = useCreateListingForm();

    return (
        <section className="mx-auto w-full max-w-4xl rounded-2xl border border-border bg-card p-6 shadow-[var(--shadow-card)]">
            <form onSubmit={onSubmit} className="space-y-4" noValidate>
                <div>
                    <label htmlFor="cropName" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Crop Name</label>
                    <Input id="cropName" placeholder="Wheat" className={errors.cropName ? "border-destructive" : ""} {...registerCropName} />
                    {errors.cropName?.message ? <p className="mt-1 text-xs text-destructive">{errors.cropName.message}</p> : null}
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                        <label htmlFor="quantity" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Quantity (In Kgs)</label>
                        <Input id="quantity" type="number" min="0" step="0.01" placeholder="120.5" className={errors.quantity ? "border-destructive" : ""} {...registerQuantity} />
                        {errors.quantity?.message ? <p className="mt-1 text-xs text-destructive">{errors.quantity.message}</p> : null}
                    </div>

                    <div>
                        <label htmlFor="pricePerKg" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Price Per Kg</label>
                        <Input id="pricePerKg" type="number" min="0" step="0.01" placeholder="27.75" className={errors.pricePerKg ? "border-destructive" : ""} {...registerPricePerKg} />
                        {errors.pricePerKg?.message ? <p className="mt-1 text-xs text-destructive">{errors.pricePerKg.message}</p> : null}
                    </div>
                </div>

                <div>
                    <label htmlFor="description" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Description (Optional)</label>
                    <textarea
                        id="description"
                        rows={4}
                        {...registerDescription}
                        className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                        placeholder="Fresh harvest, moisture controlled"
                    />
                </div>

                <div>
                    <div className="mb-1 flex items-center justify-between">
                        <label htmlFor="street" className="block text-xs font-medium uppercase tracking-wide text-muted-foreground">Street</label>
                        <Button type="button" size="sm" className="cursor-pointer" onClick={handleUseProfileAddress} disabled={loadingAddress}>
                            {loadingAddress ? (
                                <>
                                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                    Fetching...
                                </>
                            ) : (
                                "Use Profile Address"
                            )}
                        </Button>
                    </div>
                    <Input id="street" placeholder="Plot 17, Ring Road" className={errors.street ? "border-destructive" : ""} {...registerStreet} />
                    {errors.street?.message ? <p className="mt-1 text-xs text-destructive">{errors.street.message}</p> : null}
                </div>

                <div className="grid gap-4 sm:grid-cols-3">
                    <div>
                        <label htmlFor="city" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">City</label>
                        <Input id="city" placeholder="Ahmedabad" className={errors.city ? "border-destructive" : ""} {...registerCity} />
                        {errors.city?.message ? <p className="mt-1 text-xs text-destructive">{errors.city.message}</p> : null}
                    </div>

                    <div>
                        <label htmlFor="state" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">State</label>
                        <Input id="state" placeholder="Gujarat" className={errors.state ? "border-destructive" : ""} {...registerState} />
                        {errors.state?.message ? <p className="mt-1 text-xs text-destructive">{errors.state.message}</p> : null}
                    </div>

                    <div>
                        <label htmlFor="pincode" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Pincode</label>
                        <Input id="pincode" placeholder="380001" className={errors.pincode ? "border-destructive" : ""} {...registerPincode} />
                        {errors.pincode?.message ? <p className="mt-1 text-xs text-destructive">{errors.pincode.message}</p> : null}
                    </div>
                </div>

                <div>
                    <label htmlFor="images" className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Images (Optional)</label>
                    <Input id="images" type="file" accept="image/*" multiple onChange={handleImageChange} className={imageError ? "border-destructive" : ""} />
                    {imageError ? <p className="mt-1 text-xs text-destructive">{imageError}</p> : null}
                    {images.length > 0 ? (
                        <p className="mt-1 text-xs text-muted-foreground">{images.length} image(s) selected. First uploaded image will be marked as primary.</p>
                    ) : null}
                </div>

                <Button type="submit" disabled={isSubmitting} className="cursor-pointer">
                    {isSubmitting ? (
                        <>
                            <Loader2 className="h-4 w-4 animate-spin" />
                            Creating Listing...
                        </>
                    ) : (
                        "Create Listing"
                    )}
                </Button>
            </form>
        </section>
    );
}
