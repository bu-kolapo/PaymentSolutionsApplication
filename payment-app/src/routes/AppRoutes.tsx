import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '../store/hooks';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Dashboard from '../pages/dashboard/Dashboard';
import MainLayout from '../components/layout/MainLayout';
import PaymentList from '../pages/payments/PaymentList';
import CreatePayment from '../pages/payments/CreatePayment';
import PaymentDetails from '../pages/payments/PaymentDetails'; // ✅ matches your filename

// Redirect to /dashboard if already logged in
const PublicRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
    return !isAuthenticated ? <>{children}</> : <Navigate to="/dashboard" replace />;
};

// Redirect to /login if not logged in
const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const AppRoutes = () => {
    return (
        <Routes>
            {/* Public routes */}
            <Route path="/login" element={
                <PublicRoute><Login /></PublicRoute>
            } />
            <Route path="/register" element={
                <PublicRoute><Register /></PublicRoute>
            } />

            {/* Protected routes — wrapped in MainLayout (sidebar + header) */}
            <Route path="/" element={
                <PrivateRoute><MainLayout /></PrivateRoute>
            }>
                <Route index                element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard"     element={<Dashboard />} />
                <Route path="payments"      element={<PaymentList />} />
                <Route path="payments/new"  element={<CreatePayment />} />
                <Route path="payments/:id"  element={<PaymentDetails />} /> {/* ✅ inside protected */}
            </Route>

            {/* Catch all */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};

export default AppRoutes;