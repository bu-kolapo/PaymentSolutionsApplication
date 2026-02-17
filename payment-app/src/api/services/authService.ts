
// 3. src/api/services/authService.ts
//    Calls your AuthController endpoints
// =====================================================
import axiosInstance from '../axios.config';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

export const authService = {
    login: async (data: LoginRequest): Promise<AuthResponse> => {
        const response = await axiosInstance.post<AuthResponse>('/api/v1/auth/login', data);
        return response.data;
    },

    register: async (data: RegisterRequest): Promise<AuthResponse> => {
        const response = await axiosInstance.post<AuthResponse>('/api/v1/auth/register', data);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('user');
    },
};
