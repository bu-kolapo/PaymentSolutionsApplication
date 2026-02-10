// src/routes/PublicRoute.tsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAppSelector } from '@/store/hooks';

const PublicRoute: React.FC = () => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

    return !isAuthenticated ? <Outlet /> : <Navigate to="/dashboard" replace />;
};

export default PublicRoute;