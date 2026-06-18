import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Card, CardContent, Typography, CircularProgress,
    Alert, Grid, Chip, Button,
} from '@mui/material';
import { AccountBalance, TrendingUp, Refresh } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

interface SettlementAccount {
    id: string;
    accountNumber: string;
    accountName: string;
    ownerId: string;
    ownerType: string;
    currency: string;
    balance: number;
    availableBalance: number;
    accountType: string;
    createdAt: string;
}

const AccountSetup = () => {
    const [merchantId, setMerchantId] = useState<string>('');
    const [error, setError] = useState<string>('');

    // Get merchant ID from logged-in user
    useEffect(() => {
        try {
            const userStr = localStorage.getItem('user');
            if (!userStr) {
                setError('User data not found. Please login again.');
                return;
            }

            const user = JSON.parse(userStr);
            const id = user?.merchantId || user?.id;

            if (!id) {
                setError('Merchant ID not found in user data.');
                console.error('User object:', user);
                return;
            }

            console.log('✅ Merchant ID found:', id);
            setMerchantId(id);
        } catch (err) {
            console.error('❌ Error parsing user data:', err);
            setError('Failed to load user data.');
        }
    }, []);

    // Fetch merchant settlement account
    const {
        data: account,
        isLoading,
        isError,
        error: queryError,
        refetch,
    } = useQuery({
        queryKey: ['merchantSettlementAccount', merchantId],
        queryFn: async () => {
            if (!merchantId) {
                throw new Error('Merchant ID is empty');
            }

            console.log('🏦 Fetching settlement account for merchant:', merchantId);

            try {
                const response = await axiosInstance.post(
                    '/api/v1/ledger/account/merchant',
                    {},
                    {
                        params: {
                            merchantId,
                            currency: 'NGN',
                        },
                        headers: {
                            'X-Merchant-Id': merchantId,
                        },
                    }
                );

                console.log('✅ Settlement account loaded successfully:', response.data);
                return response.data as SettlementAccount;
            } catch (err: any) {
                console.error('❌ Error fetching account:', err);
                console.error('Error response:', err.response?.data);
                throw new Error(
                    err.response?.data?.message ||
                    'Failed to fetch settlement account'
                );
            }
        },
        enabled: !!merchantId,
        retry: 2,
    });

    // ✅ INITIAL ERROR STATE (no merchantId)
    if (error) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Settlement Account
                </Typography>
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
                <Button
                    variant="contained"
                    onClick={() => window.location.reload()}
                >
                    Reload Page
                </Button>
            </Box>
        );
    }

    // ✅ LOADING STATE
    if (isLoading) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Settlement Account
                </Typography>
                <Box display="flex" justifyContent="center" pt={6}>
                    <CircularProgress />
                </Box>
            </Box>
        );
    }

    // ✅ ERROR STATE (API error)
    if (isError || !account) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Settlement Account
                </Typography>
                <Alert severity="error" sx={{ mb: 2 }}>
                    <Typography variant="body2" fontWeight="bold" mb={1}>
                        Failed to load settlement account
                    </Typography>
                    <Typography variant="caption" display="block" mb={2}>
                        {queryError instanceof Error
                            ? queryError.message
                            : 'Please check your connection and try again.'}
                    </Typography>
                </Alert>
                <Button
                    variant="contained"
                    startIcon={<Refresh />}
                    onClick={() => refetch()}
                >
                    Try Again
                </Button>
            </Box>
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
                                {Number(account.balance).toLocaleString('en-NG', {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                            </Typography>

                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                                Account: {account.accountNumber}
                            </Typography>

                            <Box mt={2}>
                                <Chip
                                    label="Active"
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
                                {Number(
                                    account.availableBalance || account.balance
                                ).toLocaleString('en-NG', {
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
                                        sx={{ mb: 0.5 }}
                                    >
                                        Account Number
                                    </Typography>
                                    <Typography
                                        variant="body1"
                                        fontWeight="500"
                                        sx={{ fontFamily: 'monospace' }}
                                    >
                                        {account.accountNumber}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                        sx={{ mb: 0.5 }}
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
                                        sx={{ mb: 0.5 }}
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
                                        sx={{ mb: 0.5 }}
                                    >
                                        Account Name
                                    </Typography>
                                    <Typography variant="body1" fontWeight="500">
                                        {account.accountName}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                        sx={{ mb: 0.5 }}
                                    >
                                        Owner Type
                                    </Typography>
                                    <Chip
                                        label={account.ownerType}
                                        size="small"
                                        variant="outlined"
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        display="block"
                                        sx={{ mb: 0.5 }}
                                    >
                                        Created
                                    </Typography>
                                    <Typography variant="body2">
                                        {new Date(account.createdAt).toLocaleDateString()}
                                    </Typography>
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
