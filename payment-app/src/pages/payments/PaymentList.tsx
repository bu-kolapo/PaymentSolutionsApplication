// src/pages/payments/PaymentList.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
    Box,
    Button,
    Paper,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TablePagination,
    Chip,
    CircularProgress,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { paymentService } from '@/api/services/paymentService';
import { formatCurrency, formatDate } from '@/utils/formatters';
import type { PaymentStatus } from '@/types/payment.types';

const PaymentList: React.FC = () => {
    const navigate = useNavigate();
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(10);

    const { data, isLoading } = useQuery({
        queryKey: ['payments', page, rowsPerPage],
        queryFn: () => paymentService.getPayments({ page, size: rowsPerPage }),
    });

    const getStatusColor = (status: PaymentStatus) => {
        const colors: Record<PaymentStatus, any> = {
            COMPLETED: 'success',
            PENDING: 'warning',
            PROCESSING: 'info',
            FAILED: 'error',
            REFUNDED: 'default',
            PARTIALLY_REFUNDED: 'default',
            FRAUD_DETECTED: 'error',
            CANCELLED: 'default',
        };
        return colors[status] || 'default';
    };

    const handleChangePage = (_: unknown, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h4">Payments</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => navigate('/payments/new')}
                >
                    New Payment
                </Button>
            </Box>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Transaction ID</TableCell>
                            <TableCell>Customer</TableCell>
                            <TableCell>Amount</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Method</TableCell>
                            <TableCell>Date</TableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {data?.content?.map((payment: any) => (
                            <TableRow
                                key={payment.id}
                                hover
                                sx={{ cursor: 'pointer' }}
                                onClick={() => navigate(`/payments/${payment.id}`)}
                            >
                                <TableCell>{payment.transactionReference}</TableCell>

                                <TableCell>
                                    <Typography variant="body2">
                                        {payment.customerName}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {payment.customerEmail}
                                    </Typography>
                                </TableCell>

                                <TableCell>
                                    {formatCurrency(payment.amount)}
                                </TableCell>

                                <TableCell>
                                    <Chip
                                        label={payment.status}
                                        color={getStatusColor(payment.status)}
                                        size="small"
                                    />
                                </TableCell>

                                <TableCell>{payment.paymentMethod}</TableCell>

                                <TableCell>
                                    {formatDate(payment.createdAt)}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>

                <TablePagination
                    component="div"
                    count={data?.totalElements || 0}
                    page={page}
                    onPageChange={handleChangePage}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                />
            </TableContainer>
        </Box>
    );
};

export default PaymentList;
