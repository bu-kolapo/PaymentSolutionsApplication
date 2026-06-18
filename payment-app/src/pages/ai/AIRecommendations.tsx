import { useState, useEffect } from 'react';
import {
    Box, Card, CardContent, Button, CircularProgress, Typography,
    Alert, List, ListItem, ListItemIcon, ListItemText, Divider,
} from '@mui/material';
import { Refresh, LightbulbOutlined, CheckCircle } from '@mui/icons-material';
import { useAIAgent } from './useAIAgent';

const AIRecommendations = () => {
    const { getRecommendations, loading, error, clearError } = useAIAgent();
    const [recommendations, setRecommendations] = useState<string[]>([]);
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        loadRecommendations();
    }, []);

    const loadRecommendations = async () => {
        clearError();
        const result = await getRecommendations();
        if (result) {
            setRecommendations(result);
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
                            💡 AI Recommendations
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            5 personalized actions to improve your payments
                        </Typography>
                    </Box>
                    <Button
                        variant="outlined"
                        size="small"
                        startIcon={<Refresh />}
                        onClick={loadRecommendations}
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

                {/* Recommendations List */}
                {recommendations.length > 0 && !loading && (
                    <>
                        <List>
                            {recommendations.map((rec, idx) => (
                                <Box key={idx}>
                                    <ListItem
                                        sx={{
                                            bgcolor: idx % 2 === 0 ? '#f9f9f9' : 'transparent',
                                            borderRadius: 1,
                                            mb: 1,
                                        }}
                                    >
                                        <ListItemIcon>
                                            <CheckCircle sx={{ color: 'success.main' }} />
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={`${idx + 1}. ${rec}`}
                                            primaryTypographyProps={{
                                                variant: 'body2',
                                                sx: { lineHeight: 1.5 },
                                            }}
                                        />
                                    </ListItem>
                                </Box>
                            ))}
                        </List>

                        <Divider sx={{ my: 2 }} />

                        <Alert severity="success" icon={<LightbulbOutlined />}>
                            <Typography variant="caption">
                                💡 <strong>Pro Tip:</strong> Implement these recommendations to improve your
                                payment success rate and reduce failed transactions.
                            </Typography>
                        </Alert>
                    </>
                )}

                {!loaded && !loading && (
                    <Alert severity="info">
                        Click "Refresh" to generate personalized recommendations
                    </Alert>
                )}

                {loaded && recommendations.length === 0 && !loading && (
                    <Alert severity="warning">
                        No recommendations available at this time
                    </Alert>
                )}
            </CardContent>
        </Card>
    );
};

export default AIRecommendations;