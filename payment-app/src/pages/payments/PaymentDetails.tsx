// src/pages/payments/PaymentDetail.tsx
// REPLACE YOUR ENTIRE FILE WITH THIS

import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box, Button, Card, CardContent, Chip, CircularProgress,
    Alert, Typography, Grid, Divider, Dialog, DialogTitle,
    DialogContent, DialogActions, TextField,
} from '@mui/material';
import {
    ArrowBack, Receipt, Person, CreditCard,
    CalendarToday, Tag, RefreshOutlined,
} from '@mui/icons-material';
import { useState } from 'react';
import axiosInstance from '../../api/axios.config';
import toast from 'react-hot-toast';

// ── Status helpers ────────────────────────────────────────────
const statusColor = (status: string): any => {
    switch (status) {
        case 'COMPLETED': return 'success';
        case 'PENDING':   return 'warning';
        case 'FAILED':    return 'error';
        case 'REFUNDED':  return 'info';
        default:          return 'default';
    }
};

// ── Info row component ────────────────────────────────────────
const InfoRow = ({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) => (
    <Box display="flex" alignItems="flex-start" gap={2} py={1.5}>
        <Box sx={{ color: '#1976d2', mt: 0.3 }}>{icon}</Box>
        <Box>
            <Typography variant="caption" color="text.secondary" display="block">
                {label}
            </Typography>
            <Typography variant="body2" fontWeight="500">
                {value || '—'}
            </Typography>
        </Box>
    </Box>
);

// ── Main component ────────────────────────────────────────────
const PaymentDetails = () => {
    const { id }       = useParams<{ id: string }>();
    const navigate     = useNavigate();
    const queryClient  = useQueryClient();

    const [refundOpen,   setRefundOpen]   = useState(false);
    const [refundReason, setRefundReason] = useState('');

    // Fetch payment details
    const { data: payment, isLoading, isError } = useQuery({
        queryKey: ['payment', id],
        queryFn: async () => {
            const res = await axiosInstance.get(`/api/v1/payments/${id}`);
            return res.data;
        },
        enabled: !!id,
    });

    // Refund mutation
    const refundMutation = useMutation({
        mutationFn: () => axiosInstance.post(`/api/v1/payments/${id}/refund`, {
            reason: refundReason,
        }),
        onSuccess: () => {
            toast.success('Refund processed successfully!');
            queryClient.invalidateQueries({ queryKey: ['payment', id] });
            queryClient.invalidateQueries({ queryKey: ['payments'] });
            setRefundOpen(false);
        },
        onError: (err: any) => {
            toast.error(err.response?.data?.message || 'Refund failed.');
        },
    });

    // ── Loading state ─────────────────────────────────────────
    if (isLoading) return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
            <CircularProgress />
        </Box>
    );

    // ── Error state ───────────────────────────────────────────
    if (isError || !payment) return (
        <Box>
            <Button startIcon={<ArrowBack />} onClick={() => navigate('/payments')} sx={{ mb: 2 }}>
                Back to Payments
            </Button>
            <Alert severity="error">Payment not found or failed to load.</Alert>
        </Box>
    );

    // ── Main render ───────────────────────────────────────────
    return (
        <Box>
            {/* Back button */}
            <Button
                startIcon={<ArrowBack />}
                onClick={() => navigate('/payments')}
                sx={{ mb: 3 }}
            >
                Back to Payments
            </Button>

            {/* Header */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box>
                    <Typography variant="h5" fontWeight="bold">
                        Payment Details
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                        {payment.transactionReference}
                    </Typography>
                </Box>
                <Box display="flex" gap={2} alignItems="center">
                    <Chip
                        label={payment.status}
                        color={statusColor(payment.status)}
                        sx={{ fontWeight: 'bold', px: 1 }}
                    />
                    {/* Show refund button only for COMPLETED payments */}
                    {payment.status === 'COMPLETED' && (
                        <Button
                            variant="outlined"
                            color="error"
                            startIcon={<RefreshOutlined />}
                            onClick={() => setRefundOpen(true)}
                        >
                            Refund
                        </Button>
                    )}
                </Box>
            </Box>

            <Grid container spacing={3}>

                {/* ── Amount Card ── */}
                <Grid item xs={12} md={4}>
                    <Card elevation={0} sx={{
                        border: '1px solid #e0e0e0',
                        borderRadius: 3,
                        bgcolor: payment.status === 'COMPLETED' ? '#f0fdf4' : '#fff',
                        textAlign: 'center',
                        py: 3,
                    }}>
                        <Typography variant="h3" fontWeight="bold" color={
                            payment.status === 'COMPLETED' ? '#16a34a' :
                                payment.status === 'FAILED'    ? '#dc2626' :
                                    payment.status === 'REFUNDED'  ? '#2563eb' : '#d97706'
                        }>
                            {payment.currency} {Number(payment.amount).toLocaleString('en-US', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                        })}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" mt={1}>
                            {payment.paymentMethod?.replace('_', ' ')}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            {new Date(payment.createdAt).toLocaleString()}
                        </Typography>
                    </Card>
                </Grid>

                {/* ── Transaction Info ── */}
                <Grid item xs={12} md={4}>
                    <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, height: '100%' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Typography variant="subtitle2" fontWeight="bold" color="text.secondary" mb={1}>
                                TRANSACTION INFO
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <InfoRow
                                icon={<Tag fontSize="small" />}
                                label="Transaction Reference"
                                value={payment.transactionReference}
                            />
                            <InfoRow
                                icon={<Receipt fontSize="small" />}
                                label="Gateway Reference"
                                value={payment.gatewayReference || 'N/A'}
                            />
                            <InfoRow
                                icon={<CreditCard fontSize="small" />}
                                label="Payment Method"
                                value={payment.paymentMethod?.replace('_', ' ')}
                            />
                            <InfoRow
                                icon={<CalendarToday fontSize="small" />}
                                label="Date"
                                value={new Date(payment.createdAt).toLocaleString()}
                            />
                        </CardContent>
                    </Card>
                </Grid>

                {/* ── Customer Info ── */}
                <Grid item xs={12} md={4}>
                    <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, height: '100%' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Typography variant="subtitle2" fontWeight="bold" color="text.secondary" mb={1}>
                                CUSTOMER INFO
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <InfoRow
                                icon={<Person fontSize="small" />}
                                label="Customer Name"
                                value={payment.customerName}
                            />
                            <InfoRow
                                icon={<Person fontSize="small" />}
                                label="Email"
                                value={payment.customerEmail}
                            />
                            <InfoRow
                                icon={<Tag fontSize="small" />}
                                label="Customer ID"
                                value={payment.customerId}
                            />
                        </CardContent>
                    </Card>
                </Grid>

                {/* ── Description ── */}
                {payment.description && (
                    <Grid item xs={12}>
                        <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="subtitle2" fontWeight="bold" color="text.secondary" mb={1}>
                                    DESCRIPTION
                                </Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Typography variant="body2">{payment.description}</Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                )}

            </Grid>

            {/* ── Refund Dialog ── */}
            <Dialog
                open={refundOpen}
                onClose={() => setRefundOpen(false)}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle>Process Refund</DialogTitle>
                <DialogContent>
                    <Typography variant="body2" color="text.secondary" mb={2}>
                        You are about to refund{' '}
                        <strong>{payment.currency} {Number(payment.amount).toLocaleString()}</strong>{' '}
                        to <strong>{payment.customerName}</strong>.
                    </Typography>
                    <TextField
                        fullWidth
                        label="Reason for refund (optional)"
                        value={refundReason}
                        onChange={(e) => setRefundReason(e.target.value)}
                        multiline
                        rows={3}
                        placeholder="e.g. Customer request, duplicate charge..."
                    />
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button
                        onClick={() => setRefundOpen(false)}
                        disabled={refundMutation.isPending}
                    >
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={() => refundMutation.mutate()}
                        disabled={refundMutation.isPending}
                    >
                        {refundMutation.isPending
                            ? <CircularProgress size={20} color="inherit" />
                            : 'Confirm Refund'
                        }
                    </Button>
                </DialogActions>
            </Dialog>

        </Box>
    );
};

export default PaymentDetails;


// =====================================================
// UPDATE src/routes/AppRoutes.tsx
// Add PaymentDetail route
// =====================================================

// Add this import at the top:
// import PaymentDetail from '../pages/payments/PaymentDetail';

// Add this route inside the protected Route:
// <Route path="payments/:id" element={<PaymentDetail />} />

// Full updated protected routes:
/*
<Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
    <Route index                  element={<Navigate to="/dashboard" replace />} />
    <Route path="dashboard"       element={<Dashboard />} />
    <Route path="payments"        element={<PaymentList />} />
    <Route path="payments/new"    element={<CreatePayment />} />
    <Route path="payments/:id"    element={<PaymentDetail />} />   ← ADD THIS
</Route>
*/