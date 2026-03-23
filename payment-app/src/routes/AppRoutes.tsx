import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '../store/hooks';

// Layout & Pages
import MainLayout from '../components/layout/MainLayout';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Dashboard from '../pages/dashboard/Dashboard';
import PaymentList from '../pages/payments/PaymentList';
import CreatePaymentRequest from '../pages/payments/CreatePaymentRequest'; // RENAMED
import PaymentDetails from '../pages/payments/PaymentDetails';
import CustomerCheckout from '../pages/payments/CustomerCheckout'; // NEW
import TransactionList from '../pages/transactions/TransactionList';
import TransactionDetail from '../pages/transactions/TransactionDetail';
import AccountLedger from '../pages/ledger/AccountLedger';
import AccountSetup from '../pages/account/AccountSetup';

// Route Guards
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
            {/* Public Routes */}
            <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
            <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />

            {/* PUBLIC CHECKOUT ROUTE - NO AUTH REQUIRED */}
            <Route path="/checkout/:reference" element={<CustomerCheckout />} />

            {/* Private Routes with Main Layout */}
            <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />

                {/* Payments - UPDATED */}
                <Route path="payments" element={<PaymentList />} />
                <Route path="payments/new" element={<CreatePaymentRequest />} /> {/* RENAMED */}
                <Route path="payments/:id" element={<PaymentDetails />} />


                {/* Transactions */}
                <Route path="transactions" element={<TransactionList />} />
                <Route path="transactions/:id" element={<TransactionDetail />} />

                {/* Ledger */}
                <Route path="ledger" element={<AccountLedger />} />

                {/* Account */}
                <Route path="account/setup" element={<AccountSetup />} />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
};

export default AppRoutes;