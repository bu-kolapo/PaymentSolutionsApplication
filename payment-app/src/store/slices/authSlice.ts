import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';


// =====================================================
// 3. src/store/slices/authSlice.ts
//    Make sure isAuthenticated persists from localStorage
// =====================================================

// import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface User {
    id: string;
    email: string;
    role: string;
    merchantId: string;
}

interface AuthState {
    user: User | null;
    accessToken: string | null;
    isAuthenticated: boolean;
}

// ✅ Read token from localStorage on app load
const storedToken = localStorage.getItem('access_token');

const initialState: AuthState = {
    user: null,
    accessToken: storedToken,
    isAuthenticated: !!storedToken,   // true if token exists
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ user: User; accessToken: string }>
        ) => {
            state.user = action.payload.user;
            state.accessToken = action.payload.accessToken;
            state.isAuthenticated = true;
            localStorage.setItem('access_token', action.payload.accessToken);
        },
        logout: (state) => {
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;
            localStorage.removeItem('access_token');
        },
    },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;




