// src/routes/PublicRoute.tsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAppSelector } from '@/store/hooks';

const PublicRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

    console.log('🔓 PublicRoute - isAuthenticated:', isAuthenticated);
    console.log('🔓 PublicRoute - children:', children?.type?.name);

    if (isAuthenticated) {
        console.log('➡️ Redirecting to /dashboard');
        return <Navigate to="/dashboard" replace />;
    }

    console.log('✅ Rendering public route');
    return <>{children}</>;
};

export default PublicRoute;