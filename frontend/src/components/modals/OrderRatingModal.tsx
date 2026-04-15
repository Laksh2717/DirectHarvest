import { Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import ModalLayout from "@/components/modals/ModalLayout";
import type { OrderRatingModalProps } from "@/types/modal";

export default function OrderRatingModal({
    open,
    orderDisplayId,
    selectedRating,
    submitting,
    onClose,
    onSelectRating,
    onSubmit,
}: OrderRatingModalProps) {
    return (
        <ModalLayout
            open={open}
            title={`Rate Order ${orderDisplayId}`}
            onClose={onClose}
            closeAriaLabel="Close rate order modal"
            backdropAriaLabel="Close rate order modal backdrop"
            maxWidth="sm"
        >
            <p className="text-sm text-muted-foreground">
                Share your experience for this completed order by selecting a star rating from 1 to 5.
            </p>

            <div className="mt-4 flex items-center gap-2">
                {Array.from({ length: 5 }, (_, index) => {
                    const ratingValue = index + 1;
                    const active = ratingValue <= selectedRating;

                    return (
                        <button
                            key={ratingValue}
                            type="button"
                            onClick={() => onSelectRating(ratingValue)}
                            className="rounded-md p-1 transition-transform hover:scale-105"
                            aria-label={`Rate ${ratingValue} star${ratingValue > 1 ? "s" : ""}`}
                        >
                            <Star
                                className={`h-7 w-7 ${active ? "fill-amber-400 text-amber-400" : "text-muted-foreground/50"}`}
                            />
                        </button>
                    );
                })}
            </div>

            <p className="mt-2 text-xs text-muted-foreground">
                Selected rating: <span className="font-semibold text-foreground">{selectedRating || 0}</span>/5
            </p>

            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                <Button type="button" variant="outline" onClick={onClose} disabled={submitting}>
                    Cancel
                </Button>
                <Button type="button" onClick={onSubmit} disabled={submitting}>
                    {submitting ? "Submitting..." : "Rate"}
                </Button>
            </div>
        </ModalLayout>
    );
}
