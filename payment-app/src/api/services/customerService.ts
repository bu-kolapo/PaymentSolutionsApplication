// src/api/services/customerService.ts
import axiosInstance from '../axios.config';
import { Customer, CustomerRequest } from '@/types/customer.types';

export const customerService = {
    createCustomer: async (data: CustomerRequest): Promise<Customer> => {
        const response = await axiosInstance.post<Customer>('/api/v1/customers', data);
        return response.data;
    },

    getCustomer: async (customerId: string): Promise<Customer> => {
        const response = await axiosInstance.get<Customer>(`/api/v1/customers/${customerId}`);
        return response.data;
    },

    getCustomers: async (params?: { page?: number; size?: number }): Promise<any> => {
        const response = await axiosInstance.get('/api/v1/customers', { params });
        return response.data;
    },

    updateCustomer: async (customerId: string, data: CustomerRequest): Promise<Customer> => {
        const response = await axiosInstance.put<Customer>(
            `/api/v1/customers/${customerId}`,
            data
        );
        return response.data;
    },

    deleteCustomer: async (customerId: string): Promise<void> => {
        await axiosInstance.delete(`/api/v1/customers/${customerId}`);
    },
};