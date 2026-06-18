import { Routes, Route, Navigate } from 'react-router-dom';

// Layout & Pages
import MainLayout from '../components/layout/MainLayout';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Dashboard from '../pages/dashboard/Dashboard';
import PaymentList from '../pages/payments/PaymentList';
import CreatePaymentRequest from '../pages/payments/CreatePaymentRequest';
import PaymentDetails from '../pages/payments/PaymentDetails';
import CustomerCheckout from '../pages/payments/CustomerCheckout';
import TransactionList from '../pages/transactions/TransactionList';
import TransactionDetail from '../pages/transactions/TransactionDetail';
import AccountLedger from '../pages/ledger/AccountLedger';
import AccountSetup from '../pages/account/AccountSetup';
import AIAgentPage from '../pages/ai/AIAgentPage';

const AppRoutes = () => {
    return (
        <Routes>
            {/* ✅ NO GUARDS - Direct routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/checkout/:reference" element={<CustomerCheckout />} />

            {/* All other routes */}
            <Route path="/" element={<MainLayout />}>
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="payments" element={<PaymentList />} />
                <Route path="payments/new" element={<CreatePaymentRequest />} />
                <Route path="payments/:id" element={<PaymentDetails />} />
                <Route path="transactions" element={<TransactionList />} />
                <Route path="transactions/:id" element={<TransactionDetail />} />
                <Route path="ledger" element={<AccountLedger />} />
                <Route path="account/setup" element={<AccountSetup />} />
                <Route path="/ai-agent" element={<AIAgentPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
    );
};

export default AppRoutes;