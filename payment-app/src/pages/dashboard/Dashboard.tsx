import { Box, Grid, Card, CardContent, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { analyticsService } from '../../api/services/analyticsService';

const Dashboard = () => {
    const { data: analytics, isLoading } = useQuery({
        queryKey: ['dashboard-analytics'],
        queryFn: () => analyticsService.getDashboardAnalytics(30),
    });

    if (isLoading) {
        return <Typography>Loading...</Typography>;
    }

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Dashboard
            </Typography>

            <Grid container spacing={3}>
                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom>
                                Total Revenue
                            </Typography>
                            <Typography variant="h5">
                                ${analytics?.totalRevenue?.toFixed(2) || '0.00'}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom>
                                Total Transactions
                            </Typography>
                            <Typography variant="h5">
                                {analytics?.totalTransactions || 0}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom>
                                Successful
                            </Typography>
                            <Typography variant="h5" color="success.main">
                                {analytics?.successfulTransactions || 0}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom>
                                Failed
                            </Typography>
                            <Typography variant="h5" color="error.main">
                                {analytics?.failedTransactions || 0}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};

export default Dashboard;