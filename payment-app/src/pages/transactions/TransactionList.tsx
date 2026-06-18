import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Button, Card, Chip, CircularProgress,
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TablePagination, Typography, Alert,
} from '@mui/material';
import { Refresh } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../api/axios.config';

const statusColor = (status: string): any => {
    switch (status?.toUpperCase()) {
        case 'COMPLETED':
        case 'SUCCESS': return 'success';
        case 'PENDING': return 'warning';
        case 'FAILED': return 'error';
        case 'REFUNDED': return 'info';
        default: return 'default';
    }
};

const TransactionList = () => {
    const navigate = useNavigate();
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    const { data, isLoading, isError, error, refetch } = useQuery({
        queryKey: ['transactions', page, rowsPerPage],
        queryFn: async () => {
            console.log('📊 Fetching transactions, page:', page, 'size:', rowsPerPage);

            try {
                const response = await axiosInstance.get('/api/v1/transactions', {
                    params: { page, size: rowsPerPage },
                });

                console.log('✅ Transactions loaded:', response.data);
                return response.data;
            } catch (err: any) {
                console.error('❌ Error fetching transactions:');
                console.error('   Status:', err.response?.status);
                console.error('   Message:', err.response?.data?.message || err.message);
                throw err;
            }
        },
        retry: 1,
    });

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" pt={6}>
                <CircularProgress />
            </Box>
        );
    }

    if (isError) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Transactions
                </Typography>
                <Alert severity="error" sx={{ mb: 2 }}>
                    <Typography variant="body2" fontWeight="bold" mb={1}>
                        Failed to load transactions
                    </Typography>
                    <Typography variant="caption">
                        {error instanceof Error ? error.message : 'Please try again'}
                    </Typography>
                </Alert>
                <Button
                    variant="contained"
                    startIcon={<Refresh />}
                    onClick={() => refetch()}
                >
                    Retry
                </Button>
            </Box>
        );
    }

    return (
        <Box>
            <Typography variant="h5" fontWeight="bold" mb={3}>
                Transactions
            </Typography>

            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                <TableContainer>
                    <Table>
                        <TableHead sx={{ bgcolor: '#f5f5f5' }}>
                            <TableRow>
                                <TableCell><b>Reference</b></TableCell>
                                <TableCell><b>Type</b></TableCell>
                                <TableCell><b>Amount</b></TableCell>
                                <TableCell><b>Status</b></TableCell>
                                <TableCell><b>Description</b></TableCell>
                                <TableCell><b>Date</b></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {(!data?.content || data.content.length === 0) && (
                                <TableRow>
                                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                        <Typography color="text.secondary">
                                            No transactions yet
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            )}
                            {data?.content?.map((tx: any) => (
                                <TableRow key={tx.id} hover>
                                    <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                                        {tx.transactionReference || tx.referenceId}
                                    </TableCell>
                                    <TableCell>
                                        <Chip label={tx.type} size="small" variant="outlined" />
                                    </TableCell>
                                    <TableCell fontWeight="bold">
                                        {tx.currency} {Number(tx.amount).toLocaleString('en-NG', {
                                        minimumFractionDigits: 2,
                                    })}
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={tx.status}
                                            color={statusColor(tx.status)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>{tx.description || '—'}</TableCell>
                                    <TableCell>
                                        {new Date(tx.createdAt).toLocaleDateString()}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {data?.content?.length > 0 && (
                    <TablePagination
                        component="div"
                        count={data.totalElements || 0}
                        page={page}
                        rowsPerPage={rowsPerPage}
                        onPageChange={(_, newPage) => setPage(newPage)}
                        onRowsPerPageChange={(e) => {
                            setRowsPerPage(parseInt(e.target.value, 10));
                            setPage(0);
                        }}
                    />
                )}
            </Card>
        </Box>
    );
};

export default TransactionList;