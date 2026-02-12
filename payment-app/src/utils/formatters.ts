export const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat("en-NG", {
        style: "currency",
        currency: "NGN",
    }).format(amount);
};

export const formatDate = (date: string | Date): string => {
    return new Date(date).toLocaleDateString();
};
