// src/api/services/paymentService.ts
// REPLACE YOUR ENTIRE FILE WITH THIS

import axiosInstance from '../axios.config';

// Defined locally here to avoid any import issues
interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

interface PaymentRequest {
    customerId: string;
    amount: number;
    currency: string;
    paymentMethod: string;
    paymentMethodToken: string;
    description?: string;
    customerEmail: string;
    customerName: string;
}

interface RefundRequest {
    amount?: number;
    reason?: string;
}

interface PaymentResponse {
    id: string;
    merchantId: string;
    customerId: string;
    amount: number;
    currency: string;
    status: string;
    paymentMethod: string;
    transactionReference: string;
    gatewayReference?: string;
    description?: string;
    customerEmail: string;
    customerName: string;
    createdAt: string;
}

export const paymentService = {

    // GET /api/v1/payments
    getPayments: async (page = 0, size = 10): Promise<PageResponse<PaymentResponse>> => {
        const response = await axiosInstance.get('/api/v1/payments', {
            params: { page, size }
        });
        return response.data;
    },

    // GET /api/v1/payments/:id
    getPayment: async (id: string): Promise<PaymentResponse> => {
        const response = await axiosInstance.get(`/api/v1/payments/${id}`);
        return response.data;
    },

    // POST /api/v1/payments
    processPayment: async (request: PaymentRequest): Promise<PaymentResponse> => {
        const response = await axiosInstance.post('/api/v1/payments', request);
        return response.data;
    },

    // POST /api/v1/payments/:id/refund
    refundPayment: async (id: string, request: RefundRequest): Promise<PaymentResponse> => {
        const response = await axiosInstance.post(`/api/v1/payments/${id}/refund`, request);
        return response.data;
    },
};