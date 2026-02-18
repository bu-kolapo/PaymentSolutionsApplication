import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Button, Card, CardContent, Chip, CircularProgress,
    Alert, Typography, Grid, Divider,
} from '@mui/material';
import {
    ArrowBack, Receipt, Person, Tag, CalendarToday,
    SwapHoriz, AccountBalance,
} from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

const InfoRow = ({ icon, label, value }: any) => (
    <Box display="flex" alignItems="flex-start" gap={2} py={1.5}>
        <Box sx={{ color: '#1976d2', mt: 0.3 }}>{icon}</Box>
        <Box>
            <Typography variant="caption" color="text.secondary" display="block">
                {label}
            </Typography>
            <Typography variant="body2" fontWeight="500">
                {value || '—'}
            </Typography>
        </Box>
    </Box>
);

const TransactionDetail = () => {
    const { id }   = useParams<{ id: string }>();
    const navigate = useNavigate();

    const { data: transaction, isLoading, isError } = useQuery({
        queryKey: ['transaction', id],
        queryFn: async () => {
            const res = await axiosInstance.get(`/api/v1/transactions/${id}`);
            return res.data;
        },
        enabled: !!id,
    });

    if (isLoading) return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
            <CircularProgress />
        </Box>
    );

    if (isError || !transaction) return (
        <Box>
            <Button startIcon={<ArrowBack />} onClick={() => navigate('/transactions')} sx={{ mb: 2 }}>
                Back
            </Button>
            <Alert severity="error">Transaction not found.</Alert>
        </Box>
    );

    return (
        <Box>
            <Button startIcon={<ArrowBack />} onClick={() => navigate('/transactions')} sx={{ mb: 3 }}>
                Back to Transactions
            </Button>

            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box>
                    <Typography variant="h5" fontWeight="bold">
                        Transaction Details
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                        {transaction.transactionReference}
                    </Typography>
                </Box>
                <Chip label={transaction.type.replace(/_/g, ' ')} color="primary" />
            </Box>

            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, height: '100%' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Typography variant="subtitle2" fontWeight="bold" color="text.secondary" mb={1}>
                                TRANSACTION INFO
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <InfoRow icon={<Tag />} label="Transaction ID" value={transaction.id} />
                            <InfoRow icon={<Receipt />} label="Payment ID" value={transaction.paymentId} />
                            <InfoRow icon={<AccountBalance />} label="Amount"
                                     value={`${transaction.currency} ${Number(transaction.amount).toFixed(2)}`} />
                            <InfoRow icon={<CalendarToday />} label="Created At"
                                     value={new Date(transaction.createdAt).toLocaleString()} />
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} md={6}>
                    <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, height: '100%' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Typography variant="subtitle2" fontWeight="bold" color="text.secondary" mb={1}>
                                STATE CHANGE
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <InfoRow icon={<SwapHoriz />} label="Previous Status" value={transaction.previousStatus || 'None'} />
                            <InfoRow icon={<SwapHoriz />} label="New Status" value={transaction.newStatus} />
                            <InfoRow icon={<Person />} label="Initiated By" value={transaction.initiatedBy} />
                            <InfoRow icon={<Receipt />} label="Gateway Reference" value={transaction.gatewayReference || 'N/A'} />
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <Box mt={2}>
                <Button
                    variant="outlined"
                    onClick={() => navigate(`/payments/${transaction.paymentId}`)}
                >
                    View Payment Details
                </Button>
            </Box>
        </Box>
    );
};

export default TransactionDetail;