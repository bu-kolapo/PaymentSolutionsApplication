import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';  // ✅ Import the default export

export const store = configureStore({
    reducer: {
        auth: authReducer,  // ✅ Use the imported reducer
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

