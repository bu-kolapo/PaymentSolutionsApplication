import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Button, Card, Chip, CircularProgress,
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TablePagination, Typography,
    Alert, Tabs, Tab, TextField, MenuItem,
} from '@mui/material';
import { Timeline } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

// Transaction type colors
const typeColor = (type: string): any => {
    switch (type) {
        case 'PAYMENT_CREATED':   return 'info';
        case 'PAYMENT_COMPLETED': return 'success';
        case 'PAYMENT_FAILED':    return 'error';
        case 'REFUND_ISSUED':     return 'warning';
        default:                  return 'default';
    }
};

// Status colors
const statusColor = (status: string): any => {
    switch (status) {
        case 'SUCCESS': return 'success';
        case 'FAILED':  return 'error';
        case 'PENDING': return 'warning';
        default:        return 'default';
    }
};

const TransactionList = () => {
    const navigate = useNavigate();
    const [page, setPage]               = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [filterTab, setFilterTab]     = useState<'all' | 'type' | 'status'>('all');
    const [typeFilter, setTypeFilter]   = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    // Build API endpoint based on filters
    const getEndpoint = () => {
        if (filterTab === 'type' && typeFilter) {
            return `/api/v1/transactions/type/${typeFilter}?page=${page}&size=${rowsPerPage}`;
        }
        if (filterTab === 'status' && statusFilter) {
            return `/api/v1/transactions/status/${statusFilter}?page=${page}&size=${rowsPerPage}`;
        }
        return `/api/v1/transactions?page=${page}&size=${rowsPerPage}`;
    };

    const { data, isLoading, isError } = useQuery({
        queryKey: ['transactions', page, rowsPerPage, filterTab, typeFilter, statusFilter],
        queryFn: async () => {
            const res = await axiosInstance.get(getEndpoint());
            return res.data;
        },
    });

    if (isLoading) return (
        <Box display="flex" justifyContent="center" pt={6}>
            <CircularProgress />
        </Box>
    );

    if (isError) return (
        <Alert severity="error">Failed to load transactions.</Alert>
    );

    return (
        <Box>
            {/* Header */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box>
                    <Typography variant="h5" fontWeight="bold">Transactions</Typography>
                    <Typography variant="body2" color="text.secondary">
                        Complete audit trail of all payment state changes
                    </Typography>
                </Box>
                <Button
                    variant="outlined"
                    startIcon={<Timeline />}
                    onClick={() => navigate('/transactions/stats')}
                >
                    View Stats
                </Button>
            </Box>

            {/* Filter Tabs */}
            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3, mb: 2 }}>
                <Tabs
                    value={filterTab}
                    onChange={(_, val) => {
                        setFilterTab(val);
                        setPage(0);
                    }}
                    sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
                >
                    <Tab label="All Transactions" value="all" />
                    <Tab label="By Type" value="type" />
                    <Tab label="By Status" value="status" />
                </Tabs>

                {/* Type Filter */}
                {filterTab === 'type' && (
                    <Box p={2}>
                        <TextField
                            select
                            size="small"
                            label="Transaction Type"
                            value={typeFilter}
                            onChange={(e) => {
                                setTypeFilter(e.target.value);
                                setPage(0);
                            }}
                            sx={{ minWidth: 250 }}
                        >
                            <MenuItem value="">All Types</MenuItem>
                            <MenuItem value="PAYMENT_CREATED">Payment Created</MenuItem>
                            <MenuItem value="PAYMENT_COMPLETED">Payment Completed</MenuItem>
                            <MenuItem value="PAYMENT_FAILED">Payment Failed</MenuItem>
                            <MenuItem value="REFUND_ISSUED">Refund Issued</MenuItem>
                        </TextField>
                    </Box>
                )}

                {/* Status Filter */}
                {filterTab === 'status' && (
                    <Box p={2}>
                        <TextField
                            select
                            size="small"
                            label="Status"
                            value={statusFilter}
                            onChange={(e) => {
                                setStatusFilter(e.target.value);
                                setPage(0);
                            }}
                            sx={{ minWidth: 200 }}
                        >
                            <MenuItem value="">All Status</MenuItem>
                            <MenuItem value="SUCCESS">Success</MenuItem>
                            <MenuItem value="FAILED">Failed</MenuItem>
                            <MenuItem value="PENDING">Pending</MenuItem>
                        </TextField>
                    </Box>
                )}
            </Card>

            {/* Table */}
            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                <TableContainer>
                    <Table>
                        <TableHead sx={{ bgcolor: '#f5f5f5' }}>
                            <TableRow>
                                <TableCell><b>Reference</b></TableCell>
                                <TableCell><b>Type</b></TableCell>
                                <TableCell><b>Status</b></TableCell>
                                <TableCell><b>Amount</b></TableCell>
                                <TableCell><b>State Change</b></TableCell>
                                <TableCell><b>Initiated By</b></TableCell>
                                <TableCell><b>Date</b></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {(!data?.content || data.content.length === 0) && (
                                <TableRow>
                                    <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                                        <Typography color="text.secondary">No transactions yet</Typography>
                                    </TableCell>
                                </TableRow>
                            )}
                            {data?.content?.map((txn: any) => (
                                <TableRow
                                    key={txn.id}
                                    hover
                                    sx={{ cursor: 'pointer' }}
                                    onClick={() => navigate(`/transactions/${txn.id}`)}
                                >
                                    <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                                        {txn.transactionReference}
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={txn.type.replace(/_/g, ' ')}
                                            color={typeColor(txn.type)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={txn.status}
                                            color={statusColor(txn.status)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Typography fontWeight="bold">
                                            {txn.currency} {Number(txn.amount).toFixed(2)}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        {txn.previousStatus && (
                                            <Typography variant="caption" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                                {txn.previousStatus} → {txn.newStatus}
                                            </Typography>
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2">{txn.initiatedBy || '—'}</Typography>
                                    </TableCell>
                                    <TableCell>
                                        {new Date(txn.createdAt).toLocaleString()}
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
                    onRowsPerPageChange={(e) => {
                        setRowsPerPage(parseInt(e.target.value, 10));
                        setPage(0);
                    }}
                />
            </Card>
        </Box>
    );
};

export default TransactionList;