// =====================================================
// src/store/slices/authSlice.ts
// COMPLETE VERSION WITH TOKEN EXPIRY CHECK
// =====================================================

import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    merchantId: string;
    role: string;
}

interface AuthState {
    isAuthenticated: boolean;
    user: User | null;
    token: string | null;
}

// ✅ NEW: Helper function to check if JWT token is expired
const isTokenExpired = (token: string): boolean => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000; // Convert to milliseconds
        return Date.now() >= exp;
    } catch (error) {
        console.error('Failed to decode token:', error);
        return true; // If can't decode, treat as expired
    }
};

// ✅ UPDATED: Get initial state with token expiry validation
// authSlice.ts - CHANGE THIS PART:

const getInitialState = (): AuthState => {
    try {
        const token = localStorage.getItem('access_token');
        const userStr = localStorage.getItem('user');

        if (!token || !userStr) {
            return { isAuthenticated: false, user: null, token: null };
        }

        // Check if token is expired
        if (isTokenExpired(token)) {
            console.warn('⏰ Token expired, clearing localStorage');
            localStorage.clear();
            return { isAuthenticated: false, user: null, token: null };
        }

        const user = JSON.parse(userStr);

        // ✅ UPDATED: Make merchantId optional OR use id as fallback
        if (user?.id && user?.email) {
            // If no merchantId, use id as merchantId (for testing)
            if (!user.merchantId) {
                user.merchantId = user.id;
                localStorage.setItem('user', JSON.stringify(user));
            }

            console.log('✅ Valid auth found:', user.email);
            return { isAuthenticated: true, user, token };
        } else {
            console.warn('⚠️ Invalid user data, clearing');
            localStorage.clear();
            return { isAuthenticated: false, user: null, token: null };
        }
    } catch (error) {
        console.error('Error initializing auth state:', error);
        localStorage.clear();
        return { isAuthenticated: false, user: null, token: null };
    }
};

// ✅ FIXED: Use getInitialState() function instead of inline object
const authSlice = createSlice({
    name: 'auth',
    initialState: getInitialState(), // ← Call the function here
    reducers: {
        setCredentials: (state, action: PayloadAction<{ user: User; accessToken: string }>) => {
            state.isAuthenticated = true;
            state.user = action.payload.user;
            state.token = action.payload.accessToken;
            localStorage.setItem('access_token', action.payload.accessToken);
            localStorage.setItem('user', JSON.stringify(action.payload.user));
        },
        loginSuccess: (state, action: PayloadAction<{ user: User; accessToken: string }>) => {
            state.isAuthenticated = true;
            state.user = action.payload.user;
            state.token = action.payload.accessToken;
            localStorage.setItem('access_token', action.payload.accessToken);
            localStorage.setItem('user', JSON.stringify(action.payload.user));
        },
        logout: (state) => {
            state.isAuthenticated = false;
            state.user = null;
            state.token = null;
            localStorage.clear(); // ✅ Use clear() instead of removeItem
        },
    },
});

export const { setCredentials, loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;

// ✅ NEW: Export thunk to manually check token expiry
export const checkTokenExpiry = () => (dispatch: any) => {
    const token = localStorage.getItem('access_token');

    if (!token) {
        dispatch(logout());
        return;
    }

    if (isTokenExpired(token)) {
        console.log('⏰ Token expired during check, logging out');
        dispatch(logout());
    }
};
