// 2. CustomerCheckout.tsx (NEW)
// Customer pays via payment link
// Route: /checkout/:reference (PUBLIC - NO AUTH)
// =====================================================

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
    Box, Button, Card, CardContent, CircularProgress,
    TextField, Typography, Alert, Grid, Divider,
    RadioGroup, FormControlLabel, Radio,
} from '@mui/material';
import { CheckCircle, CreditCard, AccountBalance, Phone } from '@mui/icons-material';
import axios from 'axios';
import toast from 'react-hot-toast';

const CustomerCheckout = () => {
    const { reference } = useParams<{ reference: string }>();
    const navigate = useNavigate();

    const [paymentMethod, setPaymentMethod] = useState('CARD');
    const [cardDetails, setCardDetails] = useState({
        cardNumber: '',
        cardExpiry: '',
        cardCvv: '',
        cardholderName: '',
    });
    const [bankDetails, setBankDetails] = useState({
        bankCode: '',
        accountNumber: '',
    });
    const [mobileDetails, setMobileDetails] = useState({
        phoneNumber: '',
        network: 'MTN',
    });

    const [paymentSuccess, setPaymentSuccess] = useState(false);

    // Fetch payment request details (NO AUTH REQUIRED)
    const { data: paymentRequest, isLoading, isError } = useQuery({
        queryKey: ['paymentRequest', reference],
        queryFn: async () => {
            const response = await axios.get(
                `http://localhost:8081/api/v1/payments/requests/${reference}`
            );
            return response.data;
        },
        enabled: !!reference,
    });

    // Process payment mutation (NO AUTH REQUIRED)
    const paymentMutation = useMutation({
        mutationFn: async () => {
            const payload: any = {
                paymentMethod,
            };

            if (paymentMethod === 'CARD') {
                payload.paymentMethodToken = 'pm_card_visa'; // Stripe test token
                payload.cardNumber = cardDetails.cardNumber;
                payload.cardExpiry = cardDetails.cardExpiry;
                payload.cardCvv = cardDetails.cardCvv;
                payload.cardholderName = cardDetails.cardholderName;
            } else if (paymentMethod === 'BANK_TRANSFER') {
                payload.bankCode = bankDetails.bankCode;
                payload.accountNumber = bankDetails.accountNumber;
            } else if (paymentMethod === 'MOBILE_MONEY') {
                payload.phoneNumber = mobileDetails.phoneNumber;
                payload.network = mobileDetails.network;
            }

            const response = await axios.post(
                `http://localhost:8081/api/v1/payments/${reference}/pay`,
                payload
            );
            return response.data;
        },
        onSuccess: () => {
            setPaymentSuccess(true);
            toast.success('Payment successful!');
        },
        onError: (err: any) => {
            toast.error(err.response?.data?.message || 'Payment failed');
        },
    });

    if (isLoading) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
            >
                <CircularProgress />
            </Box>
        );
    }

    if (isError || !paymentRequest) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
                bgcolor="#f5f5f5"
            >
                <Card sx={{ maxWidth: 500, p: 4 }}>
                    <Alert severity="error">
                        Payment request not found or expired.
                    </Alert>
                </Card>
            </Box>
        );
    }

    if (paymentSuccess) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
                bgcolor="#f5f5f5"
            >
                <Card sx={{ maxWidth: 500, p: 4, textAlign: 'center' }}>
                    <CheckCircle sx={{ fontSize: 80, color: 'success.main', mb: 2 }} />
                    <Typography variant="h5" fontWeight="bold" mb={1}>
                        Payment Successful!
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={3}>
                        Your payment of {paymentRequest.currency}{' '}
                        {Number(paymentRequest.amount).toFixed(2)} has been processed.
                    </Typography>
                    <Alert severity="success">
                        A confirmation email has been sent to {paymentRequest.customerEmail}
                    </Alert>
                </Card>
            </Box>
        );
    }

    return (
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="100vh"
            bgcolor="#f5f5f5"
            p={2}
        >
            <Card sx={{ maxWidth: 600, width: '100%' }}>
                <CardContent sx={{ p: 4 }}>
                    <Typography variant="h5" fontWeight="bold" mb={1}>
                        Complete Payment
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={3}>
                        Payment to {paymentRequest.customerName || 'Merchant'}
                    </Typography>

                    <Divider sx={{ mb: 3 }} />

                    {/* Amount Display */}
                    <Card
                        sx={{
                            bgcolor: '#1976d2',
                            color: 'white',
                            p: 3,
                            mb: 3,
                            textAlign: 'center',
                        }}
                    >
                        <Typography variant="h3" fontWeight="bold">
                            {paymentRequest.currency}{' '}
                            {Number(paymentRequest.amount).toFixed(2)}
                        </Typography>
                        {paymentRequest.description && (
                            <Typography variant="body2" sx={{ opacity: 0.8, mt: 1 }}>
                                {paymentRequest.description}
                            </Typography>
                        )}
                    </Card>

                    {/* Payment Method Selection */}
                    <Typography variant="subtitle1" fontWeight="bold" mb={2}>
                        Select Payment Method
                    </Typography>

                    <RadioGroup
                        value={paymentMethod}
                        onChange={(e) => setPaymentMethod(e.target.value)}
                    >
                        <FormControlLabel
                            value="CARD"
                            control={<Radio />}
                            label={
                                <Box display="flex" alignItems="center" gap={1}>
                                    <CreditCard /> Card Payment
                                </Box>
                            }
                        />
                        <FormControlLabel
                            value="BANK_TRANSFER"
                            control={<Radio />}
                            label={
                                <Box display="flex" alignItems="center" gap={1}>
                                    <AccountBalance /> Bank Transfer
                                </Box>
                            }
                        />
                        <FormControlLabel
                            value="MOBILE_MONEY"
                            control={<Radio />}
                            label={
                                <Box display="flex" alignItems="center" gap={1}>
                                    <Phone /> Mobile Money
                                </Box>
                            }
                        />
                    </RadioGroup>

                    <Divider sx={{ my: 3 }} />

                    {/* Card Payment Form */}
                    {paymentMethod === 'CARD' && (
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Cardholder Name"
                                    value={cardDetails.cardholderName}
                                    onChange={(e) =>
                                        setCardDetails({
                                            ...cardDetails,
                                            cardholderName: e.target.value,
                                        })
                                    }
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Card Number"
                                    value={cardDetails.cardNumber}
                                    onChange={(e) =>
                                        setCardDetails({
                                            ...cardDetails,
                                            cardNumber: e.target.value,
                                        })
                                    }
                                    placeholder="1234 5678 9012 3456"
                                    required
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Expiry Date"
                                    value={cardDetails.cardExpiry}
                                    onChange={(e) =>
                                        setCardDetails({
                                            ...cardDetails,
                                            cardExpiry: e.target.value,
                                        })
                                    }
                                    placeholder="MM/YY"
                                    required
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="CVV"
                                    value={cardDetails.cardCvv}
                                    onChange={(e) =>
                                        setCardDetails({
                                            ...cardDetails,
                                            cardCvv: e.target.value,
                                        })
                                    }
                                    placeholder="123"
                                    required
                                />
                            </Grid>
                        </Grid>
                    )}

                    {/* Bank Transfer Form */}
                    {paymentMethod === 'BANK_TRANSFER' && (
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Bank Code"
                                    value={bankDetails.bankCode}
                                    onChange={(e) =>
                                        setBankDetails({
                                            ...bankDetails,
                                            bankCode: e.target.value,
                                        })
                                    }
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Account Number"
                                    value={bankDetails.accountNumber}
                                    onChange={(e) =>
                                        setBankDetails({
                                            ...bankDetails,
                                            accountNumber: e.target.value,
                                        })
                                    }
                                    required
                                />
                            </Grid>
                        </Grid>
                    )}

                    {/* Mobile Money Form */}
                    {paymentMethod === 'MOBILE_MONEY' && (
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Phone Number"
                                    value={mobileDetails.phoneNumber}
                                    onChange={(e) =>
                                        setMobileDetails({
                                            ...mobileDetails,
                                            phoneNumber: e.target.value,
                                        })
                                    }
                                    placeholder="+234 XXX XXX XXXX"
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    select
                                    label="Network"
                                    value={mobileDetails.network}
                                    onChange={(e) =>
                                        setMobileDetails({
                                            ...mobileDetails,
                                            network: e.target.value,
                                        })
                                    }
                                >
                                    <MenuItem value="MTN">MTN</MenuItem>
                                    <MenuItem value="AIRTEL">Airtel</MenuItem>
                                    <MenuItem value="GLO">Glo</MenuItem>
                                    <MenuItem value="9MOBILE">9Mobile</MenuItem>
                                </TextField>
                            </Grid>
                        </Grid>
                    )}

                    {/* Submit Button */}
                    <Button
                        fullWidth
                        variant="contained"
                        size="large"
                        onClick={() => paymentMutation.mutate()}
                        disabled={paymentMutation.isPending}
                        sx={{ mt: 3, py: 1.5 }}
                    >
                        {paymentMutation.isPending ? (
                            <CircularProgress size={24} color="inherit" />
                        ) : (
                            `Pay ${paymentRequest.currency} ${Number(
                                paymentRequest.amount
                            ).toFixed(2)}`
                        )}
                    </Button>

                    <Typography
                        variant="caption"
                        color="text.secondary"
                        display="block"
                        textAlign="center"
                        mt={2}
                    >
                        🔒 Secure payment powered by PaymentSolution
                    </Typography>
                </CardContent>
            </Card>
        </Box>
    );
};

export default CustomerCheckout;