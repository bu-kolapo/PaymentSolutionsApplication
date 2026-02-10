
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { User, AuthResponse } from '@/types/auth.types';

interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    accessToken: string | null;
}

const loadUserFromStorage = (): User | null => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
};

const initialState: AuthState = {
    user: loadUserFromStorage(),
    isAuthenticated: !!localStorage.getItem('access_token'),
    accessToken: localStorage.getItem('access_token'),
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (state, action: PayloadAction<AuthResponse>) => {
            const { accessToken, userId, email, role, merchantId } = action.payload;

            const user: User = {
                id: userId,
                email,
                role: role as any,
                merchantId,
            };

            state.user = user;
            state.accessToken = accessToken;
            state.isAuthenticated = true;

            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('user', JSON.stringify(user));
        },
        logout: (state) => {
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;

            localStorage.removeItem('access_token');
            localStorage.removeItem('user');
        },
    },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;