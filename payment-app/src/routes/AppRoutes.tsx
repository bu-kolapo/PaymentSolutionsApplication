
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '../store/hooks';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Dashboard from '../pages/dashboard/Dashboard';
import MainLayout from '../components/layout/MainLayout';
import PaymentList from '../pages/payments/PaymentList';
import CreatePayment from '../pages/payments/CreatePayment';
import PaymentDetails from '../pages/payments/PaymentDetails';
import TransactionList from '../pages/transactions/TransactionList';
import TransactionDetail from '../pages/transactions/TransactionDetail';

const PublicRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
    return !isAuthenticated ? <>{children}</> : <Navigate to="/dashboard" replace />;
};

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const AppRoutes = () => {
    return (
        <Routes>
            <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
            <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />

            <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
                <Route index                   element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard"        element={<Dashboard />} />
                <Route path="payments"         element={<PaymentList />} />
                <Route path="payments/new"     element={<CreatePayment />} />
                <Route path="payments/:id"     element={<PaymentDetails />} />
                <Route path="transactions"     element={<TransactionList />} />
                <Route path="transactions/:id" element={<TransactionDetail />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};

export default AppRoutes;