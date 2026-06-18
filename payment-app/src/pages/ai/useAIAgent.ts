import { useState } from 'react';
import axiosInstance from '../../api/axios.config';
import { AIResponse, AIQueryRequest } from './types';

export const useAIAgent = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 💬 Chat with AI
    const chat = async (request: AIQueryRequest): Promise<AIResponse | null> => {
        setLoading(true);
        setError(null);

        try {
            console.log('💬 Sending chat request:', request);
            const response = await axiosInstance.post('/api/v1/ai/chat', request);
            console.log('✅ Chat response:', response.data);
            return response.data;
        } catch (err: any) {
            const message = err.response?.data?.error || err.message || 'Failed to chat with AI';
            setError(message);
            console.error('❌ Chat error:', message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    // 📊 Get insights
    const getInsights = async (period: number = 30): Promise<AIResponse | null> => {
        setLoading(true);
        setError(null);

        try {
            console.log('📊 Fetching insights for period:', period);
            const response = await axiosInstance.get(`/api/v1/ai/insights?period=${period}`);
            console.log('✅ Insights loaded:', response.data);
            return response.data;
        } catch (err: any) {
            const message = err.response?.data?.error || err.message || 'Failed to generate insights';
            setError(message);
            console.error('❌ Insights error:', message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    // 💡 Get recommendations
    const getRecommendations = async (): Promise<string[] | null> => {
        setLoading(true);
        setError(null);

        try {
            console.log('💡 Fetching recommendations...');
            const response = await axiosInstance.get('/api/v1/ai/recommendations');
            console.log('✅ Recommendations loaded:', response.data);
            return response.data;
        } catch (err: any) {
            const message = err.response?.data?.error || err.message || 'Failed to get recommendations';
            setError(message);
            console.error('❌ Recommendations error:', message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    // 📈 Analyze patterns
    const analyzePatterns = async (): Promise<AIResponse | null> => {
        setLoading(true);
        setError(null);

        try {
            console.log('📈 Analyzing patterns...');
            const response = await axiosInstance.get('/api/v1/ai/patterns');
            console.log('✅ Pattern analysis loaded:', response.data);
            return response.data;
        } catch (err: any) {
            const message = err.response?.data?.error || err.message || 'Failed to analyze patterns';
            setError(message);
            console.error('❌ Patterns error:', message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    const clearError = () => setError(null);

    return {
        chat,
        getInsights,
        getRecommendations,
        analyzePatterns,
        loading,
        error,
        clearError,
    };
};