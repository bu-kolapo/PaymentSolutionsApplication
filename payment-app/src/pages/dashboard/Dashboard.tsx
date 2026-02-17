// =====================================================
// 1. src/pages/dashboard/Dashboard.tsx
//    Full working dashboard
// =====================================================

import { Box, Grid, Card, CardContent, Typography, Chip, Avatar } from '@mui/material';
import {
    TrendingUp,
    Payment,
    People,
    ErrorOutline,
} from '@mui/icons-material';
import { useAppSelector } from '../../store/hooks';

const Dashboard = () => {
    const user = useAppSelector((state) => state.auth.user);

    const stats = [
        {
            title: 'Total Revenue',
            value: '$0.00',
            icon: <TrendingUp />,
            color: '#1976d2',
            bg: '#e3f2fd',
        },
        {
            title: 'Total Transactions',
            value: '0',
            icon: <Payment />,
            color: '#388e3c',
            bg: '#e8f5e9',
        },
        {
            title: 'Customers',
            value: '0',
            icon: <People />,
            color: '#f57c00',
            bg: '#fff3e0',
        },
        {
            title: 'Failed Payments',
            value: '0',
            icon: <ErrorOutline />,
            color: '#d32f2f',
            bg: '#ffebee',
        },
    ];

    return (
        <Box>
            {/* Welcome header */}
            <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: '#1976d2', width: 48, height: 48 }}>
                    {user?.email?.charAt(0).toUpperCase()}
                </Avatar>
                <Box>
                    <Typography variant="h5" fontWeight="bold">
                        Welcome back! 👋
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        {user?.email}
                    </Typography>
                </Box>
                <Chip
                    label="Active"
                    color="success"
                    size="small"
                    sx={{ ml: 'auto' }}
                />
            </Box>

            {/* Stats cards */}
            <Grid container spacing={3}>
                {stats.map((stat) => (
                    <Grid item xs={12} sm={6} md={3} key={stat.title}>
                        <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                            <CardContent sx={{ p: 3 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                                    <Typography variant="body2" color="text.secondary">
                                        {stat.title}
                                    </Typography>
                                    <Avatar sx={{ bgcolor: stat.bg, color: stat.color, width: 36, height: 36 }}>
                                        {stat.icon}
                                    </Avatar>
                                </Box>
                                <Typography variant="h4" fontWeight="bold" color={stat.color}>
                                    {stat.value}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            {/* Quick info */}
            <Card elevation={0} sx={{ mt: 3, border: '1px solid #e0e0e0', borderRadius: 3 }}>
                <CardContent sx={{ p: 3 }}>
                    <Typography variant="h6" fontWeight="bold" gutterBottom>
                        🎉 You're logged in successfully!
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Your PaymentSolution dashboard is ready. Start by adding customers
                        and processing payments.
                    </Typography>
                    <Box sx={{ mt: 2 }}>
                        <Chip label={`Merchant ID: ${user?.merchantId}`} variant="outlined" size="small" sx={{ mr: 1 }} />
                        <Chip label={`Role: ${user?.role}`} variant="outlined" size="small" />
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};

export default Dashboard;
