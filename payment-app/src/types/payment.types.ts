
export type PaymentStatus =
    | 'PENDING'
    | 'PROCESSING'
    | 'COMPLETED'
    | 'FAILED'
    | 'REFUNDED'
    | 'PARTIALLY_REFUNDED'
    | 'FRAUD_DETECTED'
    | 'CANCELLED';

export interface Payment {
    id: string;
    merchantId: string;
    customerId: string;
    amount: number;
    currency: string;
    status: PaymentStatus;
    paymentMethod: string;
    transactionReference: string;
    gatewayReference?: string;
    description?: string;
    customerEmail: string;
    customerName: string;
    transactionDate?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface PaymentRequest {
    customerId: string;
    amount: number;
    currency: string;
    paymentMethod: string;
    paymentMethodToken: string;
    description?: string;
    customerEmail: string;
    customerName: string;
    ipAddress?: string;
    userAgent?: string;
}

export interface RefundRequest {
    amount?: number;
    reason?: string;
}

export interface PaymentListResponse {
    content: Payment[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}