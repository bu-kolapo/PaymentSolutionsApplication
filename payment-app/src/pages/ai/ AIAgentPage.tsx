import { Box, Grid, Typography, Paper, Alert } from '@mui/material';
import { SmartToy } from '@mui/icons-material';
import AIChat from './AIChat';
import AIInsights from './AIInsights';
import AIRecommendations from './AIRecommendations';
import AIPatterns from './AIPatterns';

const AIAgentPage = () => {
    return (
        <Box>
            {/* Header */}
            <Box sx={{ mb: 4 }}>
                <Box display="flex" alignItems="center" gap={2} mb={2}>
                    <SmartToy sx={{ fontSize: 32, color: '#1976d2' }} />
                    <Box>
                        <Typography variant="h4" fontWeight="bold">
                            🤖 AI Payment Assistant
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Powered by Claude AI - Get insights, recommendations, and analyze your payment patterns
                        </Typography>
                    </Box>
                </Box>

                <Alert severity="info" sx={{ mb: 2 }}>
                    💡 <strong>Tip:</strong> Start by chatting with the AI assistant or view insights to understand your
                    payment performance.
                </Alert>
            </Box>

            {/* Main Grid */}
            <Grid container spacing={3}>
                {/* Left Column: Chat (Larger) */}
                <Grid item xs={12} lg={6}>
                    <AIChat />
                </Grid>

                {/* Right Column: Recommendations & Patterns */}
                <Grid item xs={12} lg={6}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <AIRecommendations />
                        </Grid>
                        <Grid item xs={12}>
                            <AIPatterns />
                        </Grid>
                    </Grid>
                </Grid>

                {/* Full Width: Insights */}
                <Grid item xs={12}>
                    <AIInsights />
                </Grid>
            </Grid>

            {/* Footer Info */}
            <Paper
                sx={{
                    mt: 4,
                    p: 3,
                    bgcolor: '#f9f9f9',
                    borderLeft: '4px solid #1976d2',
                }}
            >
                <Typography variant="subtitle2" fontWeight="bold" mb={1}>
                    📚 How to use AI Assistant:
                </Typography>
                <Box component="ul" sx={{ mb: 0, pl: 2 }}>
                    <Typography component="li" variant="body2" sx={{ mb: 0.5 }}>
                        <strong>Chat:</strong> Ask questions about your payment data in natural language
                    </Typography>
                    <Typography component="li" variant="body2" sx={{ mb: 0.5 }}>
                        <strong>Insights:</strong> Get AI-generated analysis of your payment performance over any period
                    </Typography>
                    <Typography component="li" variant="body2" sx={{ mb: 0.5 }}>
                        <strong>Recommendations:</strong> Get 5 actionable suggestions to improve your payments
                    </Typography>
                    <Typography component="li" variant="body2">
                        <strong>Patterns:</strong> Analyze trends and detect anomalies in your transaction data
                    </Typography>
                </Box>
            </Paper>
        </Box>
    );
};

export default AIAgentPage;