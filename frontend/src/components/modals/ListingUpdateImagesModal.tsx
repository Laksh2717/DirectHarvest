import Image from "next/image";
import { Check } from "lucide-react";
import { Input } from "@/components/ui/input";
import FormModal from "@/components/ui/form-modal";
import { buildCloudinaryImageUrl } from "@/lib/cloudinary";
import { toTitleCase } from "@/lib/formatters";
import type { ListingUpdateImagesModalProps } from "@/types/modal";
import { getListingImagesValidationError } from "@/lib/validators";

export default function ListingUpdateImagesModal({ listing, imageState, actionLoading, onCancel }: ListingUpdateImagesModalProps) {
    return (
        <FormModal
            open={imageState.open}
            title={`Update Images - ${toTitleCase(listing.cropName)}`}
            description="Add new images and select existing images to remove."
            submitText="Submit"
            loading={actionLoading === "updateImages"}
            onCancel={onCancel}
            onSubmit={imageState.submit}
        >
            <div className="space-y-4">
                <div>
                    <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-muted-foreground">Add New Images</label>
                    <Input
                        type="file"
                        accept="image/*"
                        multiple
                        onChange={(event) => {
                            const files = Array.from(event.target.files ?? []);
                            imageState.setNewImageFiles(files);

                            if (files.some((file) => !file.type.startsWith("image/"))) {
                                imageState.setUpdateImagesError("Only image files are allowed");
                                return;
                            }

                            imageState.setUpdateImagesError(
                                getListingImagesValidationError({
                                    listing,
                                    filesToAddCount: files.length,
                                    selectedToRemoveCount: imageState.selectedImageIdsToRemove.length,
                                }),
                            );
                        }}
                    />
                    {imageState.newImageFiles.length > 0 ? (
                        <p className="mt-1 text-xs text-muted-foreground">{imageState.newImageFiles.length} new image(s) selected</p>
                    ) : null}
                </div>

                <div>
                    <div className="mb-2 flex items-center justify-between">
                        <label className="block text-xs font-medium uppercase tracking-wide text-muted-foreground">Current Images</label>
                        <span className="text-xs text-muted-foreground">Selected to remove: {imageState.selectedImageIdsToRemove.length}</span>
                    </div>

                    {listing.images.length === 0 ? (
                        <p className="text-sm text-muted-foreground">No existing images available.</p>
                    ) : (
                        <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
                            {listing.images.map((image) => {
                                const selected = imageState.selectedImageIdsToRemove.includes(image.id);
                                const src = buildCloudinaryImageUrl(image, "thumb");

                                return (
                                    <button
                                        key={image.id}
                                        type="button"
                                        onClick={() => {
                                            imageState.setSelectedImageIdsToRemove((current) => {
                                                const next = current.includes(image.id)
                                                    ? current.filter((id) => id !== image.id)
                                                    : [...current, image.id];

                                                imageState.setUpdateImagesError(
                                                    getListingImagesValidationError({
                                                        listing,
                                                        filesToAddCount: imageState.newImageFiles.length,
                                                        selectedToRemoveCount: next.length,
                                                    }),
                                                );

                                                return next;
                                            });
                                        }}
                                        className={`relative overflow-hidden rounded-lg border ${selected ? "border-destructive" : "border-border"}`}
                                        aria-label={selected ? "Unselect image" : "Select image to remove"}
                                    >
                                        {src ? (
                                            <Image src={src} alt="Listing preview" width={96} height={96} className="h-20 w-full object-cover" />
                                        ) : (
                                            <div className="flex h-20 items-center justify-center bg-muted text-xs text-muted-foreground">No image</div>
                                        )}
                                        {selected ? (
                                            <div className="absolute inset-0 flex items-start justify-end bg-black/45 p-1">
                                                <span className="rounded-full bg-destructive p-1 text-white">
                                                    <Check className="h-3 w-3" />
                                                </span>
                                            </div>
                                        ) : null}
                                    </button>
                                );
                            })}
                        </div>
                    )}
                </div>

                {imageState.error ? <p className="text-xs text-destructive">{imageState.error}</p> : null}
            </div>
        </FormModal>
    );
}
