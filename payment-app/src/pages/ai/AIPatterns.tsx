import { useState, useEffect } from 'react';
import {
    Box, Card, CardContent, Button, CircularProgress, Typography,
    Alert, Grid, Paper, Chip, Divider,
} from '@mui/material';
import { Refresh, TrendingUp, TrendingDown } from '@mui/icons-material';
import { useAIAgent } from './useAIAgent';
import { AIResponse } from './types';

const AIPatterns = () => {
    const { analyzePatterns, loading, error, clearError } = useAIAgent();
    const [patterns, setPatterns] = useState<AIResponse | null>(null);
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        loadPatterns();
    }, []);

    const loadPatterns = async () => {
        clearError();
        const result = await analyzePatterns();
        if (result) {
            setPatterns(result);
            setLoaded(true);
        }
    };

    const getTrendIcon = (trend: string) => {
        if (trend === 'INCREASING') return <TrendingUp sx={{ color: 'success.main' }} />;
        if (trend === 'DECREASING') return <TrendingDown sx={{ color: 'error.main' }} />;
        return null;
    };

    return (
        <Card sx={{ borderRadius: 3, height: '100%' }}>
            <CardContent>
                {/* Header */}
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                    <Box>
                        <Typography variant="h6" fontWeight="bold">
                            📈 Pattern Analysis
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Trend detection (7-day vs 30-day comparison)
                        </Typography>
                    </Box>
                    <Button
                        variant="outlined"
                        size="small"
                        startIcon={<Refresh />}
                        onClick={loadPatterns}
                        disabled={loading}
                    >
                        Refresh
                    </Button>
                </Box>

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
                {patterns && !loading && patterns.data && (
                    <>
                        <Grid container spacing={2} sx={{ mb: 3 }}>
                            {/* Last 7 Days Transactions */}
                            <Grid item xs={6} sm={3}>
                                <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                    <Typography variant="caption" color="text.secondary">
                                        Last 7 Days
                                    </Typography>
                                    <Typography variant="h5" fontWeight="bold">
                                        {patterns.data.transactionsLast7Days || 0}
                                    </Typography>
                                    <Typography variant="caption">Transactions</Typography>
                                </Paper>
                            </Grid>

                            {/* Last 30 Days Transactions */}
                            <Grid item xs={6} sm={3}>
                                <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                    <Typography variant="caption" color="text.secondary">
                                        Last 30 Days
                                    </Typography>
                                    <Typography variant="h5" fontWeight="bold">
                                        {patterns.data.transactionsLast30Days || 0}
                                    </Typography>
                                    <Typography variant="caption">Transactions</Typography>
                                </Paper>
                            </Grid>

                            {/* Last 7 Days Revenue */}
                            <Grid item xs={6} sm={3}>
                                <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                    <Typography variant="caption" color="text.secondary">
                                        Revenue (7d)
                                    </Typography>
                                    <Typography variant="h6" fontWeight="bold" color="success.main">
                                        ₦{Number(patterns.data.revenueLast7Days || 0).toLocaleString()}
                                    </Typography>
                                </Paper>
                            </Grid>

                            {/* Last 30 Days Revenue */}
                            <Grid item xs={6} sm={3}>
                                <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                    <Typography variant="caption" color="text.secondary">
                                        Revenue (30d)
                                    </Typography>
                                    <Typography variant="h6" fontWeight="bold" color="success.main">
                                        ₦{Number(patterns.data.revenueLast30Days || 0).toLocaleString()}
                                    </Typography>
                                </Paper>
                            </Grid>
                        </Grid>

                        {/* Trend Status */}
                        <Box sx={{ mb: 2, p: 2, bgcolor: '#f9f9f9', borderRadius: 1 }}>
                            <Typography variant="body2" fontWeight="bold" mb={1}>
                                📊 Current Trend
                            </Typography>
                            <Box display="flex" alignItems="center" gap={1}>
                                {getTrendIcon(patterns.data.trend)}
                                <Chip
                                    label={patterns.data.trend || 'STABLE'}
                                    color={
                                        patterns.data.trend === 'INCREASING'
                                            ? 'success'
                                            : patterns.data.trend === 'DECREASING'
                                                ? 'error'
                                                : 'default'
                                    }
                                    variant="outlined"
                                />
                                <Typography variant="caption" color="text.secondary">
                                    Compared to 30-day weekly average
                                </Typography>
                            </Box>
                        </Box>

                        <Divider sx={{ my: 2 }} />

                        {/* AI Analysis */}
                        <Paper
                            sx={{
                                p: 2,
                                bgcolor: '#f9f9f9',
                                borderLeft: '4px solid #1976d2',
                                whiteSpace: 'pre-wrap',
                            }}
                        >
                            <Typography variant="subtitle2" fontWeight="bold" mb={1}>
                                🔍 Pattern Analysis
                            </Typography>
                            <Typography variant="body2" sx={{ lineHeight: 1.6 }}>
                                {patterns.message}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{ display: 'block', mt: 2 }}
                            >
                                Analyzed: {new Date(patterns.timestamp).toLocaleString()}
                            </Typography>
                        </Paper>
                    </>
                )}

                {!loaded && !loading && (
                    <Alert severity="info">
                        Click "Refresh" to analyze transaction patterns and trends
                    </Alert>
                )}
            </CardContent>
        </Card>
    );
};

export default AIPatterns;