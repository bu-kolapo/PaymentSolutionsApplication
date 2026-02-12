import axiosInstance from '../axios.config';
import type { Payment, PaymentRequest, PaymentListResponse, RefundRequest } from '@/types/payment.types';

export const paymentService = {
    createPayment: async (data: PaymentRequest): Promise<Payment> => {
        const response = await axiosInstance.post<Payment>('/api/v1/payments', data);
        return response.data;
    },

    getPayment: async (paymentId: string): Promise<Payment> => {
        const response = await axiosInstance.get<Payment>(`/api/v1/payments/${paymentId}`);
        return response.data;
    },

    getPayments: async (params?: {
        page?: number;
        size?: number;
        status?: string;
    }): Promise<PaymentListResponse> => {
        const response = await axiosInstance.get<PaymentListResponse>('/api/v1/payments', { params });
        return response.data;
    },

    refundPayment: async (paymentId: string, data: RefundRequest): Promise<Payment> => {
        const response = await axiosInstance.post<Payment>(
            `/api/v1/payments/${paymentId}/refund`,
            data
        );
        return response.data;
    },
};
