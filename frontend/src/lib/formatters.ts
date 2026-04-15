const INR_LOCALE = "en-IN";

const toValidDate = (value: string | Date | null | undefined) => {
    if (!value) {
        return null;
    }

    const parsed = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return null;
    }

    return parsed;
};

export const toTitleCase = (value: string) =>
    value
        .trim()
        .split(/\s+/)
        .filter(Boolean)
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(" ");

export const truncateText = (value: string, maxLength: number) => {
    if (value.length <= maxLength) {
        return value;
    }

    return `${value.slice(0, maxLength)}...`;
};

export const formatCurrency = (value: number) =>
    new Intl.NumberFormat(INR_LOCALE, {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 2,
    }).format(value);

export const formatQuantity = (value: number, maximumFractionDigits = 2) =>
    new Intl.NumberFormat(INR_LOCALE, {
        maximumFractionDigits,
    }).format(value);

export const formatDate = (value: string | Date | null | undefined, fallback = "-") => {
    const parsed = toValidDate(value);
    if (!parsed) {
        return fallback;
    }

    return parsed.toLocaleDateString(INR_LOCALE, {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
};

export const formatDateTime = (value: string | Date | null | undefined, fallback = "-") => {
    const parsed = toValidDate(value);
    if (!parsed) {
        return fallback;
    }

    return parsed.toLocaleString(INR_LOCALE, {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

export const formatDateTimeMedium = (value: string | Date | null | undefined, fallback = "-") => {
    const parsed = toValidDate(value);
    if (!parsed) {
        return fallback;
    }

    return new Intl.DateTimeFormat(INR_LOCALE, {
        dateStyle: "medium",
        timeStyle: "short",
    }).format(parsed);
};

export const formatRating = (value: number | null) => {
    if (value === null || Number.isNaN(value)) {
        return "N/A";
    }
    return Number(value).toFixed(1);
};