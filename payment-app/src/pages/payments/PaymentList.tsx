import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Button, Card, Chip, CircularProgress,
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TablePagination, Typography, Alert,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

const statusColor = (status: string): any => {
    switch (status) {
        case 'COMPLETED': return 'success';
        case 'PENDING':   return 'warning';
        case 'FAILED':    return 'error';
        case 'REFUNDED':  return 'info';
        default:          return 'default';
    }
};

const PaymentList = () => {
    const navigate = useNavigate();
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    // ✅ FIXED: Added merchantId header
    const { data, isLoading, isError, error } = useQuery({
        queryKey: ['payments', page, rowsPerPage],
        queryFn: async () => {
            // Get merchantId from localStorage
            const userStr = localStorage.getItem('user');
            const user = userStr ? JSON.parse(userStr) : null;
            const merchantId = user?.merchantId || user?.id;

            if (!merchantId) {
                throw new Error('Merchant ID not found. Please login again.');
            }

            console.log('🔍 Fetching payments for merchant:', merchantId);

            const response = await axiosInstance.get('/api/v1/payments', {
                params: { page, size: rowsPerPage },
                headers: {
                    'X-Merchant-Id': merchantId  // ✅ ADDED THIS
                }
            });

            console.log('✅ Payments loaded:', response.data);
            return response.data;
        },
    });

    if (isLoading) return (
        <Box display="flex" justifyContent="center" pt={6}>
            <CircularProgress />
        </Box>
    );

    if (isError) {
        console.error('❌ Error loading payments:', error);
        return (
            <Alert severity="error">
                Failed to load payments: {error?.message || 'Check your backend connection'}
            </Alert>
        );
    }

    return (
        <Box>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h5" fontWeight="bold">Payment Requests</Typography>
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/payments/new')}>
                    Create Payment Request
                </Button>
            </Box>

            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                <TableContainer>
                    <Table>
                        <TableHead sx={{ bgcolor: '#f5f5f5' }}>
                            <TableRow>
                                <TableCell><b>Reference</b></TableCell>
                                <TableCell><b>Customer</b></TableCell>
                                <TableCell><b>Amount</b></TableCell>
                                <TableCell><b>Method</b></TableCell>
                                <TableCell><b>Status</b></TableCell>
                                <TableCell><b>Date</b></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {(!data?.content || data.content.length === 0) && (
                                <TableRow>
                                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                        <Typography color="text.secondary">
                                            No payment requests yet. Create your first one!
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            )}
                            {data?.content?.map((payment: any) => (
                                <TableRow
                                    key={payment.id}
                                    hover
                                    sx={{ cursor: 'pointer' }}
                                    onClick={() => navigate(`/payments/${payment.id}`)}
                                >
                                    <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                                        {payment.paymentReference || payment.transactionReference}
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight="bold">
                                            {payment.customerName}
                                        </Typography>
                                        <Typography variant="caption" color="text.secondary">
                                            {payment.customerEmail}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography fontWeight="bold">
                                            {payment.currency} {Number(payment.amount).toFixed(2)}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>{payment.channel || payment.paymentMethod || 'N/A'}</TableCell>
                                    <TableCell>
                                        <Chip
                                            label={payment.status}
                                            color={statusColor(payment.status)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        {new Date(payment.createdAt).toLocaleDateString()}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
                <TablePagination
                    component="div"
                    count={data?.totalElements || 0}
                    page={page}
                    rowsPerPage={rowsPerPage}
                    onPageChange={(_, newPage) => setPage(newPage)}
                    onRowsPerPage Change={(e) => {
                    setRowsPerPage(parseInt(e.target.value, 10));
                    setPage(0);
                }}
                />
            </Card>
        </Box>
    );
};

export default PaymentList;
