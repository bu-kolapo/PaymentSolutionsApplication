
// =====================================================
// 1. CreatePaymentRequest.tsx (REPLACES CreatePaymentRequest.tsx)
// Merchant creates payment request
// Route: /payments/new
// =====================================================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box, Button, Card, CardContent, CircularProgress,
    TextField, Typography, MenuItem, Alert, Grid,
    Dialog, DialogTitle, DialogContent, DialogActions,
    IconButton,
} from '@mui/material';
import { ArrowBack, ContentCopy, Share } from '@mui/icons-material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../api/axios.config';
import toast from 'react-hot-toast';

const CURRENCIES = ['NGN', 'USD', 'EUR', 'GBP'];
const CHANNELS = [
    { value: 'CARD', label: '💳 Card Payment' },
    { value: 'BANK_TRANSFER', label: '🏦 Bank Transfer' },
    { value: 'MOBILE_MONEY', label: '📱 Mobile Money' },
];

const CreatePaymentRequest = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [form, setForm] = useState({
        customerName: '',
        customerEmail: '',
        amount: '',
        currency: 'NGN',
        channel: 'CARD',
        description: '',
    });

    const [error, setError] = useState('');
    const [paymentLink, setPaymentLink] = useState('');
    const [showLinkDialog, setShowLinkDialog] = useState(false);

    const mutation = useMutation({
        mutationFn: async () => {
            const response = await axiosInstance.post('/api/v1/payments/requests', {
                ...form,
                amount: parseFloat(form.amount),
            });
            return response.data;
        },
        onSuccess: (data) => {
            toast.success('Payment request created!');
            setPaymentLink(data.paymentLink);
            setShowLinkDialog(true);
            queryClient.invalidateQueries({ queryKey: ['payments'] });
        },
        onError: (err: any) => {
            const msg = err.response?.data?.message || 'Failed to create payment request';
            setError(msg);
            toast.error(msg);
        },
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (parseFloat(form.amount) <= 0) {
            setError('Amount must be greater than 0');
            return;
        }

        mutation.mutate();
    };

    const copyLink = () => {
        navigator.clipboard.writeText(paymentLink);
        toast.success('Payment link copied!');
    };

    return (
        <Box>
            <Button
                startIcon={<ArrowBack />}
                onClick={() => navigate('/payments')}
                sx={{ mb: 3 }}
            >
                Back to Payment Requests
            </Button>

            <Typography variant="h5" fontWeight="bold" mb={3}>
                Create Payment Request
            </Typography>

            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, maxWidth: 600 }}>
                <CardContent sx={{ p: 4 }}>
                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
                            {error}
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit}>
                        <Grid container spacing={2}>
                            {/* Customer Info */}
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Customer Name"
                                    name="customerName"
                                    value={form.customerName}
                                    onChange={handleChange}
                                    required
                                    disabled={mutation.isPending}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Customer Email"
                                    type="email"
                                    name="customerEmail"
                                    value={form.customerEmail}
                                    onChange={handleChange}
                                    required
                                    disabled={mutation.isPending}
                                />
                            </Grid>

                            {/* Amount & Currency */}
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Amount"
                                    type="number"
                                    name="amount"
                                    value={form.amount}
                                    onChange={handleChange}
                                    required
                                    inputProps={{ min: 0.01, step: 0.01 }}
                                    disabled={mutation.isPending}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    select
                                    label="Currency"
                                    name="currency"
                                    value={form.currency}
                                    onChange={handleChange}
                                    disabled={mutation.isPending}
                                >
                                    {CURRENCIES.map((c) => (
                                        <MenuItem key={c} value={c}>{c}</MenuItem>
                                    ))}
                                </TextField>
                            </Grid>

                            {/* Payment Channel */}
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    select
                                    label="Payment Channel (Optional)"
                                    name="channel"
                                    value={form.channel}
                                    onChange={handleChange}
                                    disabled={mutation.isPending}
                                    helperText="Leave as default to allow customer to choose"
                                >
                                    {CHANNELS.map((c) => (
                                        <MenuItem key={c.value} value={c.value}>
                                            {c.label}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            </Grid>

                            {/* Description */}
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Description"
                                    name="description"
                                    value={form.description}
                                    onChange={handleChange}
                                    multiline
                                    rows={2}
                                    disabled={mutation.isPending}
                                    placeholder="What is this payment for?"
                                />
                            </Grid>

                            {/* Submit Button */}
                            <Grid item xs={12}>
                                <Button
                                    fullWidth
                                    variant="contained"
                                    type="submit"
                                    size="large"
                                    disabled={mutation.isPending}
                                    sx={{ py: 1.5, borderRadius: 2 }}
                                >
                                    {mutation.isPending ? (
                                        <CircularProgress size={24} color="inherit" />
                                    ) : (
                                        'Create Payment Request'
                                    )}
                                </Button>
                            </Grid>
                        </Grid>
                    </form>
                </CardContent>
            </Card>

            {/* Payment Link Dialog */}
            <Dialog
                open={showLinkDialog}
                onClose={() => {
                    setShowLinkDialog(false);
                    navigate('/payments');
                }}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle>
                    ✅ Payment Request Created
                </DialogTitle>
                <DialogContent>
                    <Typography variant="body2" color="text.secondary" mb={2}>
                        Share this link with your customer to complete the payment:
                    </Typography>

                    <Card sx={{ bgcolor: '#f5f5f5', p: 2 }}>
                        <Box display="flex" alignItems="center" gap={1}>
                            <Typography
                                variant="body2"
                                sx={{
                                    fontFamily: 'monospace',
                                    wordBreak: 'break-all',
                                    flex: 1,
                                }}
                            >
                                {paymentLink}
                            </Typography>
                            <IconButton onClick={copyLink} color="primary">
                                <ContentCopy />
                            </IconButton>
                        </Box>
                    </Card>

                    <Alert severity="info" sx={{ mt: 2 }}>
                        Customer will receive an email with this payment link.
                    </Alert>
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={copyLink} startIcon={<Share />}>
                        Copy Link
                    </Button>
                    <Button
                        variant="contained"
                        onClick={() => {
                            setShowLinkDialog(false);
                            navigate('/payments');
                        }}
                    >
                        Done
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default CreatePaymentRequest;


