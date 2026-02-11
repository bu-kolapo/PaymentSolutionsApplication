// src/api/services/analyticsService.ts
import axiosInstance from '../axios.config';
import { AnalyticsData } from '@/types/analytics.types';

export const analyticsService = {
    getDashboardAnalytics: async (days: number = 30): Promise<AnalyticsData> => {
        const response = await axiosInstance.get<AnalyticsData>(
            `/api/v1/analytics/dashboard?days=${days}`
        );
        return response.data;
    },
};