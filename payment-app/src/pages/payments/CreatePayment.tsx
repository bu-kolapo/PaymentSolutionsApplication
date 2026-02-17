import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box, Button, Card, CardContent, CircularProgress,
    TextField, Typography, MenuItem, Alert, Grid,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../../api/axios.config';
import toast from 'react-hot-toast';

const CURRENCIES      = ['USD', 'EUR', 'GBP', 'NGN'];
const PAYMENT_METHODS = ['CARD', 'BANK_TRANSFER', 'MOBILE_MONEY'];

const formatUUID = (value: string): string => {
    const clean = value.replace(/[^a-zA-Z0-9]/g, '');
    if (clean.length <= 8)  return clean;
    if (clean.length <= 12) return `${clean.slice(0, 8)}-${clean.slice(8)}`;
    if (clean.length <= 16) return `${clean.slice(0, 8)}-${clean.slice(8, 12)}-${clean.slice(12)}`;
    if (clean.length <= 20) return `${clean.slice(0, 8)}-${clean.slice(8, 12)}-${clean.slice(12, 16)}-${clean.slice(16)}`;
    return `${clean.slice(0, 8)}-${clean.slice(8, 12)}-${clean.slice(12, 16)}-${clean.slice(16, 20)}-${clean.slice(20, 32)}`;
};

const CreatePayment = () => {
    const navigate    = useNavigate();
    const queryClient = useQueryClient();

    const [form, setForm] = useState({
        customerName:       '',
        customerEmail:      '',
        customerId:         '',
        amount:             '',
        currency:           'NGN',
        paymentMethod:      'CARD',
        paymentMethodToken: 'pm_card_visa',
        description:        '',
    });
    const [error, setError] = useState('');

    const mutation = useMutation({
        mutationFn: () => axiosInstance.post('/api/v1/payments', {
            ...form,
            amount: parseFloat(form.amount),
        }),
        onSuccess: () => {
            toast.success('Payment processed successfully!');
            queryClient.invalidateQueries({ queryKey: ['payments'] });
            navigate('/payments');
        },
        onError: (err: any) => {
            const msg = err.response?.data?.message || 'Payment failed. Please try again.';
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
        const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
        if (!uuidRegex.test(form.customerId)) {
            setError('Invalid Customer ID. Must be: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx');
            return;
        }
        if (parseFloat(form.amount) <= 0) {
            setError('Amount must be greater than 0.');
            return;
        }
        mutation.mutate();
    };

    return (
        <Box>
            <Button startIcon={<ArrowBack />} onClick={() => navigate('/payments')} sx={{ mb: 3 }}>
                Back to Payments
            </Button>

            <Typography variant="h5" fontWeight="bold" mb={3}>New Payment</Typography>

            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, maxWidth: 600 }}>
                <CardContent sx={{ p: 4 }}>
                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
                            {error}
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit}>
                        <Grid container spacing={2}>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Customer ID (UUID)"
                                    name="customerId"
                                    value={form.customerId}
                                    onChange={(e) => setForm({ ...form, customerId: formatUUID(e.target.value) })}
                                    required
                                    disabled={mutation.isPending}
                                    helperText="Format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                                    inputProps={{ maxLength: 36 }}
                                    error={form.customerId.length > 0 && form.customerId.length < 36}
                                />
                            </Grid>

                            <Grid item xs={6}>
                                <TextField
                                    fullWidth label="Customer Name"
                                    name="customerName" value={form.customerName}
                                    onChange={handleChange} required
                                    disabled={mutation.isPending}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth label="Customer Email" type="email"
                                    name="customerEmail" value={form.customerEmail}
                                    onChange={handleChange} required
                                    disabled={mutation.isPending}
                                />
                            </Grid>

                            <Grid item xs={6}>
                                <TextField
                                    fullWidth label="Amount" type="number"
                                    name="amount" value={form.amount}
                                    onChange={handleChange} required
                                    inputProps={{ min: 0.01, step: 0.01 }}
                                    disabled={mutation.isPending}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth select label="Currency"
                                    name="currency" value={form.currency}
                                    onChange={handleChange} disabled={mutation.isPending}
                                >
                                    {CURRENCIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                                </TextField>
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth select label="Payment Method"
                                    name="paymentMethod" value={form.paymentMethod}
                                    onChange={handleChange} disabled={mutation.isPending}
                                >
                                    {PAYMENT_METHODS.map((m) => <MenuItem key={m} value={m}>{m}</MenuItem>)}
                                </TextField>
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth label="Description (optional)"
                                    name="description" value={form.description}
                                    onChange={handleChange} multiline rows={2}
                                    disabled={mutation.isPending}
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <Button
                                    fullWidth variant="contained" type="submit" size="large"
                                    disabled={mutation.isPending}
                                    sx={{ py: 1.5, borderRadius: 2 }}
                                >
                                    {mutation.isPending
                                        ? <CircularProgress size={24} color="inherit" />
                                        : 'Process Payment'
                                    }
                                </Button>
                            </Grid>

                        </Grid>
                    </form>
                </CardContent>
            </Card>
        </Box>
    );
};

export default CreatePayment;