
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '@/components/layout/MainLayout';
import PrivateRoute from './PrivateRoute';
import PublicRoute from './PublicRoute';

// Auth Pages
import Login from '@/pages/auth/Login';
import Register from '@/pages/auth/Register';

// Dashboard & Main Pages
import Dashboard from '@/pages/dashboard/Dashboard';
import PaymentList from '@/pages/payments/PaymentList';
import PaymentDetails from '@/pages/payments/PaymentDetails';
import CreatePayment from '@/pages/payments/CreatePayment';
import CustomerList from '@/pages/customers/CustomerList';
import CustomerDetails from '@/pages/customers/CustomerDetails';
import CreateCustomer from '@/pages/customers/CreateCustomer';
import Analytics from '@/pages/analytics/Overview';
import AIChat from '@/pages/ai-assistant/AIChat';

const AppRoutes: React.FC = () => {
    return (
        <Routes>
            {/* Public Routes */}
            <Route element={<PublicRoute />}>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
            </Route>

            {/* Protected Routes */}
            <Route element={<PrivateRoute />}>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<Dashboard />} />

                    <Route path="/payments" element={<PaymentList />} />
                    <Route path="/payments/new" element={<CreatePayment />} />
                    <Route path="/payments/:id" element={<PaymentDetails />} />

                    <Route path="/customers" element={<CustomerList />} />
                    <Route path="/customers/new" element={<CreateCustomer />} />
                    <Route path="/customers/:id" element={<CustomerDetails />} />

                    <Route path="/analytics" element={<Analytics />} />
                    <Route path="/ai-assistant" element={<AIChat />} />
                </Route>
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
    );
};

export default AppRoutes;