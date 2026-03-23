import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Card, CardContent, Typography, CircularProgress,
    Alert, Grid, Chip,
} from '@mui/material';
import { AccountBalance, TrendingUp } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

const AccountSetup = () => {
    const [merchantId, setMerchantId] = useState<string>('');

    // Get merchant ID from logged-in user
    useEffect(() => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            const user = JSON.parse(userStr);
            setMerchantId(user.merchantId || user.id);
        }
    }, []);

    // Fetch merchant settlement account
    const { data: account, isLoading, isError } = useQuery({
        queryKey: ['merchantSettlementAccount', merchantId],
        queryFn: async () => {
            const response = await axiosInstance.post(
                '/api/v1/ledger/account/merchant',
                null,
                {
                    params: {
                        merchantId,
                        currency: 'NGN',
                    },
                }
            );
            return response.data;
        },
        enabled: !!merchantId,
    });

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" pt={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (isError) {
        return (
            <Alert severity="error">
                Failed to load settlement account. Please try again.
            </Alert>
        );
    }

    return (
        <Box>
            <Typography variant="h5" fontWeight="bold" mb={3}>
                Settlement Account
            </Typography>

            <Grid container spacing={3}>
                {/* Settlement Account Card */}
                <Grid item xs={12} md={6}>
                    <Card sx={{ borderRadius: 3, bgcolor: '#1976d2', color: 'white' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Box display="flex" alignItems="center" gap={1} mb={2}>
                                <AccountBalance />
                                <Typography variant="subtitle2">
                                    Merchant Settlement Account
                                </Typography>
                            </Box>

                            <Typography variant="h3" fontWeight="bold" mb={1}>
                                {account.currency}{' '}
                                {Number(account.balance).toLocaleString('en-US', {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                            </Typography>

                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                                Account: {account.accountNumber}
                            </Typography>

                            <Box mt={2}>
                                <Chip
                                    label={account.status}
                                    color="success"
                                    size="small"
                                    sx={{ bgcolor: 'rgba(255,255,255,0.2)' }}
                                />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Available Balance */}
                <Grid item xs={12} md={6}>
                    <Card
                        sx={{
                            borderRadius: 3,
                            border: '1px solid #e0e0e0',
                            height: '100%',
                        }}
                    >
                        <CardContent sx={{ p: 3 }}>
                            <Box display="flex" alignItems="center" gap={1} mb={2}>
                                <TrendingUp color="success" />
                                <Typography variant="subtitle2" color="text.secondary">
                                    Available Balance
                                </Typography>
                            </Box>

                            <Typography variant="h3" fontWeight="bold" color="success.main">
                                {account.currency}{' '}
                                {Number(account.availableBalance).toLocaleString('en-US', {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                            </Typography>

                            <Typography variant="caption" color="text.secondary" mt={1}>
                                Ready for withdrawal
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Info Card */}
                <Grid item xs={12}>
                    <Alert severity="info">
                        💡 <strong>How it works:</strong> When customers pay via your payment
                        requests, money is credited to this settlement account. You can withdraw
                        funds anytime.
                    </Alert>
                </Grid>

                {/* Account Details */}
                <Grid item xs={12}>
                    <Card sx={{ borderRadius: 3, border: '1px solid #e0e0e0' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Typography variant="h6" fontWeight="bold" mb={2}>
                                Account Details
                            </Typography>

                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                    >
                                        Account Number
                                    </Typography>
                                    <Typography variant="body1" fontWeight="500">
                                        {account.accountNumber}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                    >
                                        Account Type
                                    </Typography>
                                    <Typography variant="body1" fontWeight="500">
                                        {account.accountType}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                    >
                                        Currency
                                    </Typography>
                                    <Typography variant="body1" fontWeight="500">
                                        {account.currency}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                    >
                                        Status
                                    </Typography>
                                    <Chip label={account.status} color="success" size="small" />
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};

export default AccountSetup;
