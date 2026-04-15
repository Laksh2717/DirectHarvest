export const buildRepeatedQuery = (key: string, values?: Array<string>) => {
    if (!values || values.length === 0) {
        return "";
    }

    const query = new URLSearchParams();
    values.forEach((value) => query.append(key, value));
    return query.toString();
};
