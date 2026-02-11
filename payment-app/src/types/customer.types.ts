
export interface Customer {
    id: string;
    merchantId: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    phone?: string;
    address?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface CustomerRequest {
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    address?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
}