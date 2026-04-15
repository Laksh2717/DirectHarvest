import type { ListingFilterTabsProps } from "@/types/listing";

export default function ListingFilterTabs({ filters, selectedFilter, onChange }: ListingFilterTabsProps) {
    return (
        <div className="flex flex-wrap items-center gap-2">
            {filters.map((filter) => {
                const active = selectedFilter === filter.value;
                return (
                    <button
                        key={filter.value}
                        type="button"
                        onClick={() => onChange(filter.value)}
                        className={`rounded-full border px-4 py-1.5 text-sm font-medium transition-colors ${active
                            ? "border-primary bg-primary text-primary-foreground"
                            : "border-border bg-card text-foreground hover:bg-muted"
                            }`}
                    >
                        {filter.label}
                    </button>
                );
            })}
        </div>
    );
}
