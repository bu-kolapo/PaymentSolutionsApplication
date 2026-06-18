// src/api/axios.config.ts

import axios from "axios";
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from "axios";

const axiosInstance: AxiosInstance = axios.create({
    baseURL: "",   // Vite proxy handles /api
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

// Request interceptor — attach JWT token

axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('access_token');

        if (token && !config.url?.includes('/auth/')) {
            config.headers.Authorization = `Bearer ${token}`;

            // ✅ ADD merchantId header to ALL requests
            const userStr = localStorage.getItem('user');
            if (userStr) {
                try {
                    const user = JSON.parse(userStr);
                    const merchantId = user?.merchantId || user?.id;
                    if (merchantId) {
                        config.headers['X-Merchant-Id'] = merchantId;
                    }
                } catch (error) {
                    console.error('Failed to get merchantId:', error);
                }
            }
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor — handle 401 safely
axiosInstance.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        const isLoginRequest = error.config?.url?.includes("/auth/login");

        if (error.response?.status === 401 && !isLoginRequest) {
            localStorage.removeItem("access_token");

            // ✅ Trigger SPA-safe redirect instead of hard reload
            window.dispatchEvent(new Event("unauthorized"));
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;