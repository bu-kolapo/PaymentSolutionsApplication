// src/pages/dashboard/Dashboard.tsx
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Grid,
    Paper,
    Typography,
    Box,
    Card,
    CardContent,
    CircularProgress,
} from '@mui/material';
import {
    TrendingUp,
    Payment as PaymentIcon,
    CheckCircle,
    Cancel,
} from '@mui/icons-material';
import { analyticsService } from '@/api/services/analyticsService';
import { paymentService } from '@/api/services/paymentService';
import { formatCurrency } from '@/utils/formatters';

const Dashboard: React.FC = () => {
    const { data: analytics, isLoading: analyticsLoading } = useQuery({
        queryKey: ['analytics', 30],
        queryFn: () => analyticsService.getDashboardAnalytics(30),
    });

    const { data: recentPayments, isLoading: paymentsLoading } = useQuery({
        queryKey: ['recent-payments'],
        queryFn: () => paymentService.getPayments({ page: 0, size: 5 }),
    });

    if (analyticsLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
            </Box>
        );
    }

    const stats = [
        {
            title: 'Total Revenue',
            value: formatCurrency(analytics?.totalRevenue || 0),
            icon: <TrendingUp />,
            color: '#4caf50',
        },
        {
            title: 'Total Transactions',
            value: analytics?.totalTransactions || 0,
            icon: <PaymentIcon />,
            color: '#2196f3',
        },
        {
            title: 'Successful',
            value: analytics?.successfulTransactions || 0,
            icon: <CheckCircle />,
            color: '#66bb6a',
        },
        {
            title: 'Failed',
            value: analytics?.failedTransactions || 0,
            icon: <Cancel />,
            color: '#f44336',
        },
    ];

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Dashboard
            </Typography>

            <Grid container spacing={3}>
                {stats.map((stat) => (
                    <Grid item xs={12} sm={6} md={3} key={stat.title}>
                        <Card>
                            <CardContent>
                                <Box display="flex" alignItems="center" justifyContent="space-between">
                                    <Box>
                                        <Typography color="text.secondary" variant="body2">
                                            {stat.title}
                                        </Typography>
                                        <Typography variant="h5" sx={{ mt: 1 }}>
                                            {stat.value}
                                        </Typography>
                                    </Box>
                                    <Box
                                        sx={{
                                            backgroundColor: stat.color,
                                            borderRadius: '50%',
                                            p: 1,
                                            display: 'flex',
                                            color: 'white',
                                        }}
                                    >
                                        {stat.icon}
                                    </Box>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}

                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Success Rate
                        </Typography>
                        <Typography variant="h3" color="primary">
                            {analytics?.successRate.toFixed(1)}%
                        </Typography>
                    </Paper>
                </Grid>

                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Average Transaction
                        </Typography>
                        <Typography variant="h3" color="primary">
                            {formatCurrency(analytics?.averageTransactionValue || 0)}
                        </Typography>
                    </Paper>
                </Grid>

                <Grid item xs={12}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Recent Payments
                        </Typography>
                        {paymentsLoading ? (
                            <CircularProgress />
                        ) : (
                            <Box sx={{ mt: 2 }}>
                                {recentPayments?.content?.map((payment: any) => (
                                    <Box
                                        key={payment.id}
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            py: 2,
                                            borderBottom: '1px solid #eee',
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="body1">{payment.customerName}</Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                {payment.transactionReference}
                                            </Typography>
                                        </Box>
                                        <Box textAlign="right">
                                            <Typography variant="body1" fontWeight="bold">
                                                {formatCurrency(payment.amount)}
                                            </Typography>
                                            <Typography
                                                variant="body2"
                                                color={payment.status === 'COMPLETED' ? 'success.main' : 'error.main'}
                                            >
                                                {payment.status}
                                            </Typography>
                                        </Box>
                                    </Box>
                                ))}
                            </Box>
                        )}
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default Dashboard;