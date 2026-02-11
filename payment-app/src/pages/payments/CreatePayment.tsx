// src/pages/payments/CreatePayment.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery } from '@tanstack/react-query';
import * as z from 'zod';
import {
    Box,
    Button,
    Paper,
    Typography,
    TextField,
    Grid,
    MenuItem,
    CircularProgress,
} from '@mui/material';
import toast from 'react-hot-toast';
import { paymentService } from '@/api/services/paymentService';
import { customerService } from '@/api/services/customerService';

const paymentSchema = z.object({
    customerId: z.string().min(1, 'Customer is required'),
    amount: z.number().min(0.01, 'Amount must be greater than 0'),
    currency: z.string().length(3, 'Currency must be 3 characters'),
    paymentMethod: z.string().min(1, 'Payment method is required'),
    paymentMethodToken: z.string().min(1, 'Payment method token is required'),
    description: z.string().optional(),
});

type PaymentFormData = z.infer<typeof paymentSchema>;

const CreatePayment: React.FC = () => {
    const navigate = useNavigate();

    const { data: customers } = useQuery({
        queryKey: ['customers'],
        queryFn: () => customerService.getCustomers({ size: 100 }),
    });

    const mutation = useMutation({
        mutationFn: paymentService.createPayment,
        onSuccess: () => {
            toast.success('Payment created successfully!');
            navigate('/payments');
        },
    });

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<PaymentFormData>({
        resolver: zodResolver(paymentSchema),
        defaultValues: {
            currency: 'USD',
            paymentMethod: 'CARD',
        },
    });

    const onSubmit = async (data: PaymentFormData) => {
        const selectedCustomer = customers?.content?.find((c: any) => c.id === data.customerId);

        mutation.mutate({
            ...data,
            customerEmail: selectedCustomer?.email || '',
            customerName: selectedCustomer?.fullName || '',
        });
    };

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Create Payment
            </Typography>

            <Paper sx={{ p: 3, mt: 3 }}>
                <Box component="form" onSubmit={handleSubmit(onSubmit)}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={6}>
                            <TextField
                                select
                                fullWidth
                                label="Customer"
                                {...register('customerId')}
                                error={!!errors.customerId}
                                helperText={errors.customerId?.message}
                            >
                                {customers?.content?.map((customer: any) => (
                                    <MenuItem key={customer.id} value={customer.id}>
                                        {customer.fullName} - {customer.email}
                                    </MenuItem>
                                ))}
                            </TextField>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <TextField
                                fullWidth
                                label="Amount"
                                type="number"
                                inputProps={{ step: '0.01' }}
                                {...register('amount', { valueAsNumber: true })}
                                error={!!errors.amount}
                                helperText={errors.amount?.message}
                            />
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <TextField
                                select
                                fullWidth
                                label="Currency"
                                {...register('currency')}
                                error={!!errors.currency}
                                helperText={errors.currency?.message}
                            >
                                <MenuItem value="USD">USD</MenuItem>
                                <MenuItem value="EUR">EUR</MenuItem>
                                <MenuItem value="GBP">GBP</MenuItem>
                            </TextField>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <TextField
                                select
                                fullWidth
                                label="Payment Method"
                                {...register('paymentMethod')}
                                error={!!errors.paymentMethod}
                                helperText={errors.paymentMethod?.message}
                            >
                                <MenuItem value="CARD">Card</MenuItem>
                                <MenuItem value="BANK_TRANSFER">Bank Transfer</MenuItem>
                                <MenuItem value="WALLET">Digital Wallet</MenuItem>
                            </TextField>
                        </Grid>

                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Payment Method Token"
                                placeholder="tok_visa or payment method identifier"
                                {...register('paymentMethodToken')}
                                error={!!errors.paymentMethodToken}
                                helperText={errors.paymentMethodToken?.message}
                            />
                        </Grid>

                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Description (Optional)"
                                multiline
                                rows={3}
                                {...register('description')}
                                error={!!errors.description}
                                helperText={errors.description?.message}
                            />
                        </Grid>

                        <Grid item xs={12}>
                            <Box display="flex" gap={2}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    size="large"
                                    disabled={mutation.isPending}
                                >
                                    {mutation.isPending ? <CircularProgress size={24} /> : 'Create Payment'}
                                </Button>
                                <Button
                                    variant="outlined"
                                    size="large"
                                    onClick={() => navigate('/payments')}
                                >
                                    Cancel
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                </Box>
            </Paper>
        </Box>
    );
};

export default CreatePayment;