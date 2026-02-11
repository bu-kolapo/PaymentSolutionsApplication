// src/pages/payments/PaymentDetails.tsx
import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
    Box,
    Button,
    Paper,
    Typography,
    Grid,
    Chip,
    Divider,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
} from '@mui/material';
import { ArrowBack as BackIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';
import { paymentService } from '@/api/services/paymentService';
import { formatCurrency, formatDate } from '@/utils/formatters';

const PaymentDetails: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [refundDialogOpen, setRefundDialogOpen] = React.useState(false);
    const [refundAmount, setRefundAmount] = React.useState('');

    const { data: payment, isLoading } = useQuery({
        queryKey: ['payment', id],
        queryFn: () => paymentService.getPayment(id!),
        enabled: !!id,
    });

    const refundMutation = useMutation({
        mutationFn: ({ paymentId, amount }: { paymentId: string; amount?: number }) =>
            paymentService.refundPayment(paymentId, { amount }),
        onSuccess: () => {
            toast.success('Refund processed successfully!');
            setRefundDialogOpen(false);
            navigate('/payments');
        },
    });

    const handleRefund = () => {
        if (!id) return;
        const amount = refundAmount ? parseFloat(refundAmount) : undefined;
        refundMutation.mutate({ paymentId: id, amount });
    };

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box>
            <Button
                startIcon={<BackIcon />}
                onClick={() => navigate('/payments')}
                sx={{ mb: 2 }}
            >
                Back to Payments
            </Button>

            <Typography variant="h4" gutterBottom>
                Payment Details
            </Typography>

            <Paper sx={{ p: 3, mt: 3 }}>
                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Transaction Reference
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            {payment?.transactionReference}
                        </Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Status
                        </Typography>
                        <Chip label={payment?.status} color="success" sx={{ mt: 0.5 }} />
                    </Grid>

                    <Grid item xs={12}>
                        <Divider />
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Customer Name
                        </Typography>
                        <Typography variant="body1">{payment?.customerName}</Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Customer Email
                        </Typography>
                        <Typography variant="body1">{payment?.customerEmail}</Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Amount
                        </Typography>
                        <Typography variant="h5" color="primary">
                            {formatCurrency(payment?.amount || 0)}
                        </Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Payment Method
                        </Typography>
                        <Typography variant="body1">{payment?.paymentMethod}</Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Gateway Reference
                        </Typography>
                        <Typography variant="body1">{payment?.gatewayReference || 'N/A'}</Typography>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                            Date
                        </Typography>
                        <Typography variant="body1">{formatDate(payment?.createdAt || '')}</Typography>
                    </Grid>

                    {payment?.description && (
                        <Grid item xs={12}>
                            <Typography variant="subtitle2" color="text.secondary">
                                Description
                            </Typography>
                            <Typography variant="body1">{payment.description}</Typography>
                        </Grid>
                    )}

                    {payment?.status === 'COMPLETED' && (
                        <Grid item xs={12}>
                            <Button
                                variant="outlined"
                                color="error"
                                onClick={() => setRefundDialogOpen(true)}
                            >
                                Refund Payment
                            </Button>
                        </Grid>
                    )}
                </Grid>
            </Paper>

            {/* Refund Dialog */}
            <Dialog open={refundDialogOpen} onClose={() => setRefundDialogOpen(false)}>
                <DialogTitle>Refund Payment</DialogTitle>
                <DialogContent>
                    <Typography variant="body2" gutterBottom>
                        Enter refund amount (leave empty for full refund):
                    </Typography>
                    <TextField
                        fullWidth
                        label="Amount"
                        type="number"
                        value={refundAmount}
                        onChange={(e) => setRefundAmount(e.target.value)}
                        sx={{ mt: 2 }}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setRefundDialogOpen(false)}>Cancel</Button>
                    <Button
                        onClick={handleRefund}
                        color="error"
                        variant="contained"
                        disabled={refundMutation.isPending}
                    >
                        {refundMutation.isPending ? <CircularProgress size={20} /> : 'Refund'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default PaymentDetails;