import ConfirmationModal from "@/components/ui/confirmation-modal";
import type { ListingConfirmationModalProps } from "@/types/modal";

export default function ListingConfirmationModal({
    open,
    title,
    description,
    confirmText,
    loading,
    icon,
    onCancel,
    onConfirm,
}: ListingConfirmationModalProps) {
    return (
        <ConfirmationModal
            open={open}
            title={title}
            description={description}
            confirmText={confirmText}
            loading={loading}
            icon={icon}
            onCancel={onCancel}
            onConfirm={onConfirm}
        />
    );
}