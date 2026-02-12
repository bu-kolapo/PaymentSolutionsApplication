import axiosInstance from '../axios.config';
import type { AnalyticsData } from '../types/analytics.types';

export const analyticsService = {
    getAnalytics: async (): Promise<AnalyticsData> => {
        const response = await axiosInstance.get<AnalyticsData>('/api/v1/analytics');
        return response.data;
    }
};
