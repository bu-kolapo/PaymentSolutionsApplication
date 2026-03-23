import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Box, Button, Card, CardContent, TextField, Typography,
    MenuItem, Stepper, Step, StepLabel, Alert,
} from '@mui/material';
import { ArrowBack, Payment } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';
import toast from 'react-hot-toast';

interface PaymentFormData {
    channel: 'CARD' | 'BANK_TRANSFER' | 'MOBILE_MONEY' | 'WALLET';
    sourceAccount: string;
    destinationAccount: string;
    amount: number | '';
    currency: 'NGN' | 'USD' | 'GBP';
    narration: string;
    customerEmail: string;
    customerName: string;
    paymentMethodToken: string;
}

const steps = ['Select Channel', 'Enter Details', 'Review'];

const InitiatePayment = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [activeStep, setActiveStep] = useState(0);

    const [formData, setFormData] = useState<PaymentFormData>({
        channel: 'BANK_TRANSFER',
        sourceAccount: '',
        destinationAccount: '',
        amount: '',
        currency: 'NGN',
        narration: '',
        customerEmail: '',
        customerName: '',
        paymentMethodToken: 'pm_card_visa',
    });

    const mutation = useMutation({
        mutationFn: (data: PaymentFormData) =>
            axiosInstance.post('/api/v1/payments/process', data),
        onSuccess: () => {
            toast.success('Payment initiated successfully!');
            queryClient.invalidateQueries({ queryKey: ['payments'] });
            navigate('/payments');
        },
        onError: (err: any) => {
            toast.error(err.response?.data?.message || 'Payment failed');
        },
    });

    const handleInputChange = (key: keyof PaymentFormData, value: string | number) => {
        setFormData(prev => ({
            ...prev,
            [key]: key === 'amount' ? Number(value) || '' : value,
        }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.sourceAccount || !formData.destinationAccount || !formData.amount) {
            toast.error('Please fill all required fields.');
            return;
        }

        mutation.mutate({
            ...formData,
            amount: Number(formData.amount),
        });
    };

    return (
        <Box>
            <Button
                startIcon={<ArrowBack />}
                onClick={() => navigate('/payments')}
                sx={{ mb: 3 }}
            >
                Back
            </Button>

            <Card
                elevation={0}
                sx={{ border: '1px solid #e0e0e0', borderRadius: 3, maxWidth: 800, mx: 'auto' }}
            >
                <CardContent sx={{ p: 4 }}>
                    <Typography variant="h5" fontWeight="bold" mb={3}>
                        <Payment sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Initiate Payment
                    </Typography>

                    <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
                        {steps.map(label => (
                            <Step key={label}>
                                <StepLabel>{label}</StepLabel>
                            </Step>
                        ))}
                    </Stepper>

                    <form onSubmit={handleSubmit}>
                        {/* Channel */}
                        <TextField
                            fullWidth
                            select
                            label="Payment Channel"
                            value={formData.channel}
                            onChange={e => handleInputChange('channel', e.target.value)}
                            sx={{ mb: 2 }}
                        >
                            <MenuItem value="CARD">💳 Card Payment (Stripe)</MenuItem>
                            <MenuItem value="BANK_TRANSFER">🏦 Bank Transfer (NIP)</MenuItem>
                            <MenuItem value="MOBILE_MONEY">📱 Mobile Money</MenuItem>
                            <MenuItem value="WALLET">👛 Wallet</MenuItem>
                        </TextField>

                        {/* Source & Destination */}
                        <TextField
                            fullWidth
                            label="Source Account Number"
                            value={formData.sourceAccount}
                            onChange={e => handleInputChange('sourceAccount', e.target.value)}
                            required
                            sx={{ mb: 2 }}
                            helperText="Account to debit from"
                        />
                        <TextField
                            fullWidth
                            label="Destination Account Number"
                            value={formData.destinationAccount}
                            onChange={e => handleInputChange('destinationAccount', e.target.value)}
                            required
                            sx={{ mb: 2 }}
                            helperText="Account to credit to"
                        />

                        {/* Amount */}
                        <TextField
                            fullWidth
                            type="number"
                            label="Amount"
                            value={formData.amount}
                            onChange={e => handleInputChange('amount', e.target.value)}
                            required
                            sx={{ mb: 2 }}
                        />

                        {/* Currency */}
                        <TextField
                            fullWidth
                            select
                            label="Currency"
                            value={formData.currency}
                            onChange={e => handleInputChange('currency', e.target.value)}
                            sx={{ mb: 2 }}
                        >
                            <MenuItem value="NGN">NGN</MenuItem>
                            <MenuItem value="USD">USD</MenuItem>
                            <MenuItem value="GBP">GBP</MenuItem>
                        </TextField>

                        {/* Narration & Customer Info */}
                        <TextField
                            fullWidth
                            label="Narration"
                            value={formData.narration}
                            onChange={e => handleInputChange('narration', e.target.value)}
                            multiline
                            rows={2}
                            sx={{ mb: 2 }}
                        />
                        <TextField
                            fullWidth
                            label="Customer Name"
                            value={formData.customerName}
                            onChange={e => handleInputChange('customerName', e.target.value)}
                            sx={{ mb: 2 }}
                        />
                        <TextField
                            fullWidth
                            type="email"
                            label="Customer Email"
                            value={formData.customerEmail}
                            onChange={e => handleInputChange('customerEmail', e.target.value)}
                            sx={{ mb: 3 }}
                        />

                        {/* Error */}
                        {mutation.isError && (
                            <Alert severity="error" sx={{ mb: 2 }}>
                                {mutation.error?.response?.data?.message || 'Payment failed'}
                            </Alert>
                        )}

                        {/* Submit */}
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            size="large"
                            disabled={mutation.isPending}
                        >
                            {mutation.isPending ? 'Processing...' : 'Initiate Payment'}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </Box>
    );
};

export default InitiatePayment;