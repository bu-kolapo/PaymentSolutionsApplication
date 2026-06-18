import { useState, useEffect } from 'react';
import {
    Box, Card, CardContent, Button, CircularProgress, Typography,
    Alert, Grid, FormControl, InputLabel, Select, MenuItem,
    Paper, Chip,
} from '@mui/material';
import { Refresh, TrendingUp, BarChart } from '@mui/icons-material';
import { useAIAgent } from './useAIAgent';
import { AIResponse } from './types';

const AIInsights = () => {
    const { getInsights, loading, error, clearError } = useAIAgent();
    const [insights, setInsights] = useState<AIResponse | null>(null);
    const [period, setPeriod] = useState(30);
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        loadInsights();
    }, [period]);

    const loadInsights = async () => {
        clearError();
        const result = await getInsights(period);
        if (result) {
            setInsights(result);
            setLoaded(true);
        }
    };

    return (
        <Card sx={{ borderRadius: 3, height: '100%' }}>
            <CardContent>
                {/* Header */}
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                    <Box>
                        <Typography variant="h6" fontWeight="bold">
                            📊 AI Analytics Insights
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Performance analysis powered by AI
                        </Typography>
                    </Box>
                    <Button
                        variant="outlined"
                        size="small"
                        startIcon={<Refresh />}
                        onClick={loadInsights}
                        disabled={loading}
                    >
                        Refresh
                    </Button>
                </Box>

                {/* Period Selector */}
                <FormControl sx={{ minWidth: 200, mb: 2 }} size="small">
                    <InputLabel>Analysis Period</InputLabel>
                    <Select
                        value={period}
                        label="Analysis Period"
                        onChange={(e) => setPeriod(e.target.value as number)}
                        disabled={loading}
                    >
                        <MenuItem value={7}>Last 7 Days</MenuItem>
                        <MenuItem value={14}>Last 14 Days</MenuItem>
                        <MenuItem value={30}>Last 30 Days</MenuItem>
                        <MenuItem value={60}>Last 60 Days</MenuItem>
                        <MenuItem value={90}>Last 90 Days</MenuItem>
                    </Select>
                </FormControl>

                {/* Error Alert */}
                {error && (
                    <Alert severity="error" onClose={clearError} sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {/* Loading State */}
                {loading && (
                    <Box display="flex" justifyContent="center" py={4}>
                        <CircularProgress />
                    </Box>
                )}

                {/* Metrics Grid */}
                {insights && !loading && insights.data && (
                    <Grid container spacing={2} sx={{ mb: 3 }}>
                        <Grid item xs={6} sm={3}>
                            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                <Typography variant="caption" color="text.secondary">
                                    Transactions
                                </Typography>
                                <Typography variant="h5" fontWeight="bold">
                                    {insights.data.totalTransactions || 0}
                                </Typography>
                            </Paper>
                        </Grid>
                        <Grid item xs={6} sm={3}>
                            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                <Typography variant="caption" color="text.secondary">
                                    Revenue
                                </Typography>
                                <Typography variant="h5" fontWeight="bold" color="success.main">
                                    {insights.data.totalRevenue?.toLocaleString('en-NG', {
                                        style: 'currency',
                                        currency: 'NGN',
                                    }) || '₦0'}
                                </Typography>
                            </Paper>
                        </Grid>
                        <Grid item xs={6} sm={3}>
                            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                <Typography variant="caption" color="text.secondary">
                                    Failed
                                </Typography>
                                <Typography variant="h5" fontWeight="bold" color="error.main">
                                    {insights.data.failedTransactions || 0}
                                </Typography>
                            </Paper>
                        </Grid>
                        <Grid item xs={6} sm={3}>
                            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                <Typography variant="caption" color="text.secondary">
                                    Success Rate
                                </Typography>
                                <Typography variant="h5" fontWeight="bold" color="success.main">
                                    {insights.data.successRate || '0%'}
                                </Typography>
                            </Paper>
                        </Grid>
                    </Grid>
                )}

                {/* AI Message */}
                {insights && !loading && (
                    <Paper
                        sx={{
                            p: 2,
                            bgcolor: '#f9f9f9',
                            borderLeft: '4px solid #1976d2',
                            whiteSpace: 'pre-wrap',
                        }}
                    >
                        <Typography variant="body2" sx={{ lineHeight: 1.6 }}>
                            {insights.message}
                        </Typography>
                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{ display: 'block', mt: 2 }}
                        >
                            Generated: {new Date(insights.timestamp).toLocaleString()}
                        </Typography>
                    </Paper>
                )}

                {!loaded && !loading && (
                    <Alert severity="info">
                        Click "Refresh" to generate AI insights for your payment data
                    </Alert>
                )}
            </CardContent>
        </Card>
    );
};

export default AIInsights;