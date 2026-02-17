
export interface User {
    id: string;
    email: string;
    firstName?: string;
    lastName?: string;
    role: 'ADMIN' | 'MERCHANT' | 'CUSTOMER' | 'SUPPORT';
    merchantId?: string;
}

export interface AuthResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    userId: string;
    email: string;
    role: string;
    merchantId?: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    businessName: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    businessType?: string;
}
export interface AuthResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    userId: string;
    email: string;
    role: string;
    merchantId: string;
}
