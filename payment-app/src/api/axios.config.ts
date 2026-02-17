// src/api/axios.config.ts

import axios from "axios";
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from "axios";

const axiosInstance: AxiosInstance = axios.create({
    baseURL: "",   // ✅ Empty — Vite proxy handles /api routing
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

// Request interceptor — attach JWT token
axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem("access_token"); // ✅ matches authSlice key
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error: AxiosError) => Promise.reject(error)
);

// Response interceptor — handle 401
axiosInstance.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            localStorage.removeItem("access_token"); // ✅ matches authSlice key
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;