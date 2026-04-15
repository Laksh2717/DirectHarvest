import { useState } from "react";
import { resolveApiErrorMessage } from "@/lib/utils";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { validatePincode, validateRequired } from "@/lib/validators";
import { listingService, type ListingImagePayload } from "@/services/listingService";
import { userService } from "@/services/userService";
import type { CreateListingFormValues } from "@/types/listing";

const parsePositiveNumberError = (value: string, label: string) => {
    if (!value.trim()) {
        return `${label} is required`;
    }

    const parsed = Number(value);
    if (Number.isNaN(parsed) || parsed <= 0) {
        return `${label} must be greater than 0`;
    }

    return null;
};


export function useCreateListingForm() {
    const router = useRouter();
    const [images, setImages] = useState<File[]>([]);
    const [imageError, setImageError] = useState<string | null>(null);
    const [loadingAddress, setLoadingAddress] = useState(false);

    const {
        register,
        handleSubmit,
        clearErrors,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm<CreateListingFormValues>({
        defaultValues: {
            cropName: "",
            quantity: "",
            pricePerKg: "",
            description: "",
            street: "",
            city: "",
            state: "",
            pincode: "",
        },
    });

    const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const selected = Array.from(event.target.files ?? []);
        const imageFiles = selected.filter((file) => file.type.startsWith("image/"));

        setImages(imageFiles);

        if (selected.length !== imageFiles.length) {
            setImageError("Only image files are allowed");
            return;
        }

        if (imageFiles.length > 5) {
            setImageError("Cannot exceed 5 images per listing");
            return;
        }

        setImageError(null);
    };

    const handleUseProfileAddress = async () => {
        setLoadingAddress(true);
        try {
            const response = await userService.getMyAddress();
            const { street, city, state, pincode } = response.data;

            setValue("street", street ?? "", { shouldDirty: true, shouldValidate: true });
            setValue("city", city ?? "", { shouldDirty: true, shouldValidate: true });
            setValue("state", state ?? "", { shouldDirty: true, shouldValidate: true });
            setValue("pincode", pincode ?? "", { shouldDirty: true, shouldValidate: true });
            clearErrors(["street", "city", "state", "pincode"]);
        } catch {
            toast.error("Unable to fetch profile address");
        } finally {
            setLoadingAddress(false);
        }
    };

    const onSubmit = handleSubmit(async (values) => {
        if (images.length > 5) {
            setImageError("Cannot exceed 5 images per listing");
            return;
        }

        setImageError(null);

        try {
            const uploadedImages: ListingImagePayload[] = [];

            for (let index = 0; index < images.length; index += 1) {
                const uploaded = await listingService.uploadListingImage(images[index]);
                uploadedImages.push({
                    cloudinaryPublicId: uploaded.publicId,
                    cloudinarySecureUrl: uploaded.secureUrl,
                    format: uploaded.format,
                    width: uploaded.width,
                    height: uploaded.height,
                    bytes: uploaded.bytes,
                    primary: index === 0,
                });
            }

            await listingService.createListing({
                cropName: values.cropName.trim(),
                quantity: Number(values.quantity),
                pricePerKg: Number(values.pricePerKg),
                description: values.description.trim() || undefined,
                street: values.street.trim(),
                city: values.city.trim(),
                state: values.state.trim(),
                pincode: values.pincode.trim(),
                images: uploadedImages.length > 0 ? uploadedImages : undefined,
            });

            toast.success("Listing created successfully");
            reset();
            setImages([]);
            setImageError(null);
            router.push("/farmer/listings");
        } catch (err: unknown) {
            toast.error(resolveApiErrorMessage(err, "Failed to create listing."));
        }
    });

    return {
        errors,
        images,
        imageError,
        isSubmitting,
        loadingAddress,
        onSubmit,
        handleImageChange,
        handleUseProfileAddress,
        registerCropName: register("cropName", {
            validate: (value) => validateRequired(value, "Crop name") ?? true,
            onChange: () => clearErrors("cropName"),
        }),
        registerQuantity: register("quantity", {
            validate: (value) => parsePositiveNumberError(value, "Quantity") ?? true,
            onChange: () => clearErrors("quantity"),
        }),
        registerPricePerKg: register("pricePerKg", {
            validate: (value) => parsePositiveNumberError(value, "Price per kg") ?? true,
            onChange: () => clearErrors("pricePerKg"),
        }),
        registerDescription: register("description"),
        registerStreet: register("street", {
            validate: (value) => validateRequired(value, "Street") ?? true,
            onChange: () => clearErrors("street"),
        }),
        registerCity: register("city", {
            validate: (value) => validateRequired(value, "City") ?? true,
            onChange: () => clearErrors("city"),
        }),
        registerState: register("state", {
            validate: (value) => validateRequired(value, "State") ?? true,
            onChange: () => clearErrors("state"),
        }),
        registerPincode: register("pincode", {
            validate: (value) => validatePincode(value) ?? true,
            onChange: () => clearErrors("pincode"),
        }),
    };
}
