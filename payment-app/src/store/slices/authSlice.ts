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

// Helper: Check if JWT is expired
const isTokenExpired = (token: string): boolean => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000;
        return Date.now() >= exp;
    } catch {
        return true;
    }
};

// Initialize state from localStorage with validation
const getInitialState = (): AuthState => {
    try {
        const token = localStorage.getItem('access_token');
        const userStr = localStorage.getItem('user');

        if (!token || !userStr) {
            return { isAuthenticated: false, user: null, token: null };
        }

        // Check expiry
        if (isTokenExpired(token)) {
            console.warn('⏰ Token expired');
            return { isAuthenticated: false, user: null, token: null };
        }

        const user = JSON.parse(userStr);

        // Check if user has ANY valid data
        const hasValidData = user && (user.id || user.email);

        if (!hasValidData) {
            console.warn('⚠️ Invalid user data');
            return { isAuthenticated: false, user: null, token: null };
        }

        // If merchantId is missing, decode from token
        if (!user.merchantId && token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                user.merchantId = payload.merchantId || payload.userId || user.id;
                user.id = user.id || payload.userId;
                user.email = user.email || payload.sub;
                user.role = user.role || payload.role?.replace('ROLE_', '');

                console.log('✅ Recovered user from token');
                localStorage.setItem('user', JSON.stringify(user));
            } catch (error) {
                console.error('Token decode failed:', error);
            }
        }

        console.log('✅ Valid auth:', user.email);
        return { isAuthenticated: true, user, token };

    } catch (error) {
        console.error('Auth init error:', error);
        return { isAuthenticated: false, user: null, token: null };
    }
};

const authSlice = createSlice({
    name: 'auth',
    initialState: getInitialState(),
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
            localStorage.clear();
        },
    },
});

export const { setCredentials, loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;

export const checkTokenExpiry = () => (dispatch: any) => {
    const token = localStorage.getItem('access_token');
    if (!token || isTokenExpired(token)) {
        dispatch(logout());
    }
};