import { useCallback, useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useForm } from "react-hook-form";
import { listingService } from "@/services/listingService";
import { toast } from "sonner";
import type { AddQuantityForm, UpdateDetailsForm, UpdatePriceForm, UseFarmerListingCardParams } from "@/types/listing";    

export function useFarmerListingCard({ listing, onListingUpdated, onListingDeleted }: UseFarmerListingCardParams) {
    const [isAddQuantityOpen, setIsAddQuantityOpen] = useState(false);
    const [isUpdatePriceOpen, setIsUpdatePriceOpen] = useState(false);
    const [isUpdateDetailsOpen, setIsUpdateDetailsOpen] = useState(false);
    const [isUpdateImagesOpen, setIsUpdateImagesOpen] = useState(false);
    const [actionLoading, setActionLoading] = useState<"addQuantity" | "updatePrice" | "updateDetails" | "updateImages" | null>(null);
    const [isInactiveConfirmOpen, setIsInactiveConfirmOpen] = useState(false);
    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState<"inactive" | "delete" | null>(null);
    const [newImageFiles, setNewImageFiles] = useState<File[]>([]);
    const [selectedImageIdsToRemove, setSelectedImageIdsToRemove] = useState<number[]>([]);
    const [updateImagesError, setUpdateImagesError] = useState<string | null>(null);

    const {
        register: registerAddQuantity,
        handleSubmit: handleAddQuantityFormSubmit,
        reset: resetAddQuantityForm,
        formState: { errors: addQuantityErrors, isSubmitting: isAddQuantitySubmitting },
    } = useForm<AddQuantityForm>({ defaultValues: { quantity: "" } });

    const {
        register: registerUpdatePrice,
        handleSubmit: handleUpdatePriceFormSubmit,
        reset: resetUpdatePriceForm,
        formState: { errors: updatePriceErrors, isSubmitting: isUpdatePriceSubmitting },
    } = useForm<UpdatePriceForm>({ defaultValues: { pricePerKg: "" } });

    const {
        register: registerUpdateDetails,
        handleSubmit: handleUpdateDetailsFormSubmit,
        reset: resetUpdateDetailsForm,
        formState: { errors: updateDetailsErrors, isSubmitting: isUpdateDetailsSubmitting },
    } = useForm<UpdateDetailsForm>({
        defaultValues: {
            cropName: listing.cropName,
            description: listing.description ?? "",
            street: listing.street,
            city: listing.city,
            state: listing.state,
            pincode: listing.pincode,
        },
    });

    const closeAllActionUi = useCallback(() => {
        setIsAddQuantityOpen(false);
        setIsUpdatePriceOpen(false);
        setIsUpdateDetailsOpen(false);
        setIsUpdateImagesOpen(false);
        setIsInactiveConfirmOpen(false);
        setIsDeleteConfirmOpen(false);
        setNewImageFiles([]);
        setSelectedImageIdsToRemove([]);
        setUpdateImagesError(null);
        setActionLoading(null);
        setConfirmLoading(null);
        resetAddQuantityForm({ quantity: "" });
        resetUpdatePriceForm({ pricePerKg: "" });
        resetUpdateDetailsForm({
            cropName: listing.cropName,
            description: listing.description ?? "",
            street: listing.street,
            city: listing.city,
            state: listing.state,
            pincode: listing.pincode,
        });
    }, [listing, resetAddQuantityForm, resetUpdateDetailsForm, resetUpdatePriceForm]);

    const openAddQuantityModal = useCallback(() => {
        resetAddQuantityForm({ quantity: "" });
        setIsAddQuantityOpen(true);
    }, [resetAddQuantityForm]);

    const openUpdatePriceModal = useCallback(() => {
        resetUpdatePriceForm({ pricePerKg: String(listing.pricePerKg) });
        setIsUpdatePriceOpen(true);
    }, [listing.pricePerKg, resetUpdatePriceForm]);

    const openUpdateDetailsModal = useCallback(() => {
        resetUpdateDetailsForm({
            cropName: listing.cropName,
            description: listing.description ?? "",
            street: listing.street,
            city: listing.city,
            state: listing.state,
            pincode: listing.pincode,
        });
        setIsUpdateDetailsOpen(true);
    }, [listing, resetUpdateDetailsForm]);

    const openUpdateImagesModal = useCallback(() => {
        setNewImageFiles([]);
        setSelectedImageIdsToRemove([]);
        setUpdateImagesError(null);
        setIsUpdateImagesOpen(true);
    }, []);

    const handleAddQuantitySubmit = useCallback(async (data: AddQuantityForm) => {
        const parsed = Number(data.quantity);

        setActionLoading("addQuantity");
        try {
            const updated = await listingService.addListingQuantity(listing.id, { quantity: parsed });
            onListingUpdated?.(updated);
            toast.success("Quantity added successfully");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to add quantity."));
            setActionLoading(null);
        }
    }, [closeAllActionUi, listing.id, onListingUpdated]);

    const handleUpdatePriceSubmit = useCallback(async (data: UpdatePriceForm) => {
        const parsed = Number(data.pricePerKg);

        setActionLoading("updatePrice");
        try {
            const updated = await listingService.updateListingPrice(listing.id, { pricePerKg: parsed });
            onListingUpdated?.(updated);
            toast.success("Price updated successfully");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to update price."));
            setActionLoading(null);
        }
    }, [closeAllActionUi, listing.id, onListingUpdated]);

    const handleUpdateDetailsSubmit = useCallback(async (data: UpdateDetailsForm) => {
        setActionLoading("updateDetails");
        try {
            const updated = await listingService.updateListingDetails(listing.id, {
                cropName: data.cropName.trim(),
                description: data.description.trim(),
                street: data.street.trim(),
                city: data.city.trim(),
                state: data.state.trim(),
                pincode: data.pincode.trim(),
            });
            onListingUpdated?.(updated);
            toast.success("Listing details updated successfully");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to update listing details."));
            setActionLoading(null);
        }
    }, [closeAllActionUi, listing.id, onListingUpdated]);

    const handleUpdateImagesSubmit = useCallback(async () => {
        const hasChanges = newImageFiles.length > 0 || selectedImageIdsToRemove.length > 0;
        if (!hasChanges) {
            closeAllActionUi();
            return;
        }

        if (newImageFiles.some((file) => !file.type.startsWith("image/"))) {
            setUpdateImagesError("Only image files are allowed");
            return;
        }

        const remainingImageCount = listing.images.length - selectedImageIdsToRemove.length;
        const totalAfterUpdate = remainingImageCount + newImageFiles.length;
        if (totalAfterUpdate > 5) {
            setUpdateImagesError(`Maximum 5 images allowed. Current selection results in ${totalAfterUpdate} images.`);
            return;
        }

        setUpdateImagesError(null);
        setActionLoading("updateImages");

        try {
            let latestListing = listing;

            if (selectedImageIdsToRemove.length > 0) {
                for (const imageId of selectedImageIdsToRemove) {
                    latestListing = await listingService.removeListingImage(listing.id, imageId);
                }
            }

            if (newImageFiles.length > 0) {
                const uploadedImages = [] as Awaited<ReturnType<typeof listingService.uploadListingImage>>[];

                for (const file of newImageFiles) {
                    const uploaded = await listingService.uploadListingImage(file);
                    uploadedImages.push(uploaded);
                }

                const shouldSetFirstNewAsPrimary = latestListing.images.length === 0;

                const payloadImages = uploadedImages.map((uploaded, index) => ({
                    cloudinaryPublicId: uploaded.publicId,
                    cloudinarySecureUrl: uploaded.secureUrl,
                    format: uploaded.format,
                    width: uploaded.width,
                    height: uploaded.height,
                    bytes: uploaded.bytes,
                    primary: shouldSetFirstNewAsPrimary && index === 0,
                }));

                latestListing = await listingService.addListingImages(listing.id, { images: payloadImages });
            }

            onListingUpdated?.(latestListing);
            toast.success("Listing images updated successfully");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to update listing images."));
            setActionLoading(null);
        }
    }, [closeAllActionUi, listing, newImageFiles, onListingUpdated, selectedImageIdsToRemove]);

    const handleConfirmInactive = useCallback(async () => {
        setConfirmLoading("inactive");
        try {
            const updated = await listingService.markListingInactive(listing.id);
            onListingUpdated?.(updated);
            toast.success("Listing marked as inactive");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to mark listing inactive."));
            setConfirmLoading(null);
        }
    }, [closeAllActionUi, listing.id, onListingUpdated]);

    const handleConfirmDelete = useCallback(async () => {
        setConfirmLoading("delete");
        try {
            await listingService.deleteListing(listing.id);
            onListingDeleted?.(listing.id);
            toast.success("Listing deleted successfully");
            closeAllActionUi();
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to delete listing."));
            setConfirmLoading(null);
        }
    }, [closeAllActionUi, listing.id, onListingDeleted]);

    return {
        addQtyForm: {
            open: isAddQuantityOpen,
            isSubmitting: isAddQuantitySubmitting,
            errors: addQuantityErrors,
            register: registerAddQuantity,
            handleSubmit: handleAddQuantityFormSubmit,
            onOpen: openAddQuantityModal,
            onSubmit: handleAddQuantitySubmit,
        },
        updatePriceForm: {
            open: isUpdatePriceOpen,
            isSubmitting: isUpdatePriceSubmitting,
            errors: updatePriceErrors,
            register: registerUpdatePrice,
            handleSubmit: handleUpdatePriceFormSubmit,
            onOpen: openUpdatePriceModal,
            onSubmit: handleUpdatePriceSubmit,
        },
        updateDetailsForm: {
            open: isUpdateDetailsOpen,
            isSubmitting: isUpdateDetailsSubmitting,
            errors: updateDetailsErrors,
            register: registerUpdateDetails,
            handleSubmit: handleUpdateDetailsFormSubmit,
            onOpen: openUpdateDetailsModal,
            onSubmit: handleUpdateDetailsSubmit,
        },
        imageState: {
            open: isUpdateImagesOpen,
            newImageFiles,
            selectedImageIdsToRemove,
            error: updateImagesError,
            setNewImageFiles,
            setSelectedImageIdsToRemove,
            setUpdateImagesError,
            onOpen: openUpdateImagesModal,
            submit: handleUpdateImagesSubmit,
        },
        modals: {
            isInactiveConfirmOpen,
            isDeleteConfirmOpen,
            confirmLoading,
            setIsInactiveConfirmOpen,
            setIsDeleteConfirmOpen,
            closeAllActionUi,
        },
        actions: {
            actionLoading,
            handleConfirmInactive,
            handleConfirmDelete,
        },
    };
}

export type FarmerListingCardState = ReturnType<typeof useFarmerListingCard>;