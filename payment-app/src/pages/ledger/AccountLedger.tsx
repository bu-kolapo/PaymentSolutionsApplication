import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Card, CardContent, CircularProgress, Alert,
    Typography, Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TablePagination, Chip, Grid, Button,
} from '@mui/material';
import { AccountBalance, TrendingUp, TrendingDown, Refresh } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

interface LedgerEntry {
    id: string;
    amount: number;
    entryType: string;
    currency: string;
    description: string;
    entryReference: string;
    balanceAfter: number;
    createdAt: string;
}

interface LedgerResponse {
    content: LedgerEntry[];
    totalElements: number;
    totalPages: number;
}

interface SettlementAccount {
    id: string;
    accountNumber: string;
    balance: number;
    currency: string;
}

const AccountLedger = () => {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [merchantId, setMerchantId] = useState<string>('');
    const [error, setError] = useState<string>('');

    // Get merchant ID from logged-in user
    useEffect(() => {
        try {
            const userStr = localStorage.getItem('user');
            if (!userStr) {
                setError('User data not found. Please login again.');
                return;
            }

            const user = JSON.parse(userStr);
            const id = user?.merchantId || user?.id;

            if (!id) {
                setError('Merchant ID not found.');
                return;
            }

            console.log('✅ Merchant ID found:', id);
            setMerchantId(id);
        } catch (err) {
            console.error('❌ Error parsing user:', err);
            setError('Failed to load user data.');
        }
    }, []);

    // Fetch merchant account
    const {
        data: account,
        isLoading: accountLoading,
        error: accountError,
    } = useQuery({
        queryKey: ['merchantAccount', merchantId],
        queryFn: async () => {
            if (!merchantId) {
                throw new Error('Merchant ID is empty');
            }

            console.log('🏦 Fetching merchant account for:', merchantId);

            const res = await axiosInstance.post(
                '/api/v1/ledger/account/merchant',
                {},
                {
                    params: { merchantId, currency: 'NGN' },
                    headers: {
                        'X-Merchant-Id': merchantId,
                    },
                }
            );

            console.log('✅ Account loaded:', res.data);
            return res.data as SettlementAccount;
        },
        enabled: !!merchantId,
        retry: 2,
    });

    // Fetch ledger entries
    const {
        data: ledger,
        isLoading: entriesLoading,
        isError: entriesError,
        error: entriesQueryError,
        refetch: refetchEntries,
    } = useQuery({
        queryKey: ['ledger', account?.id, page, rowsPerPage],
        queryFn: async () => {
            if (!account?.id) {
                throw new Error('Account ID not available');
            }

            if (!merchantId) {
                throw new Error('Merchant ID not available');
            }

            // ✅ CORRECT ENDPOINT
            const endpoint = `/api/v1/ledger/account/${account.id}/entries`;

            console.log('📜 Fetching ledger entries from:', endpoint);
            console.log('   Parameters:', { page, size: rowsPerPage });

            try {
                const res = await axiosInstance.get(endpoint, {
                    params: {
                        page,
                        size: rowsPerPage,
                    },
                    headers: {
                        'X-Merchant-Id': merchantId,
                    },
                });

                console.log('✅ Ledger entries loaded:', res.data);
                return res.data as LedgerResponse;
            } catch (err: any) {
                console.error('❌ Error fetching entries:', err);
                console.error('   Endpoint:', endpoint);
                console.error('   Status:', err.response?.status);
                console.error('   Error:', err.response?.data);
                throw err;
            }
        },
        enabled: !!account?.id && !!merchantId,
        retry: 2,
    });

    // ✅ INITIAL ERROR STATE
    if (error) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Account Ledger
                </Typography>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    // ✅ LOADING ACCOUNT
    if (accountLoading) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Account Ledger
                </Typography>
                <Box display="flex" justifyContent="center" pt={4}>
                    <CircularProgress />
                </Box>
            </Box>
        );
    }

    // ✅ ACCOUNT FETCH ERROR
    if (accountError || !account) {
        return (
            <Box>
                <Typography variant="h5" fontWeight="bold" mb={3}>
                    Account Ledger
                </Typography>
                <Alert severity="error">
                    Failed to load settlement account. Please try again.
                </Alert>
            </Box>
        );
    }

    return (
        <Box>
            <Typography variant="h5" fontWeight="bold" mb={3}>
                Account Ledger
            </Typography>

            {/* Balance Card */}
            <Grid container spacing={3} mb={3}>
                <Grid item xs={12} md={4}>
                    <Card sx={{ bgcolor: '#1976d2', color: 'white', borderRadius: 3 }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" gap={1} mb={1}>
                                <AccountBalance />
                                <Typography variant="subtitle2">Settlement Account</Typography>
                            </Box>
                            <Typography variant="h4" fontWeight="bold">
                                {account.currency}{' '}
                                {Number(account.balance).toLocaleString('en-NG', {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                            </Typography>
                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                                Account: {account.accountNumber}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Ledger Table */}
            <Card elevation={0} sx={{ border: '1px solid #e0e0e0', borderRadius: 3 }}>
                <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                        <Typography variant="h6">
                            Transaction History
                        </Typography>
                        {entriesError && (
                            <Button
                                size="small"
                                startIcon={<Refresh />}
                                onClick={() => refetchEntries()}
                            >
                                Retry
                            </Button>
                        )}
                    </Box>

                    {/* LOADING STATE */}
                    {entriesLoading && (
                        <Box display="flex" justifyContent="center" py={4}>
                            <CircularProgress />
                        </Box>
                    )}

                    {/* ERROR STATE */}
                    {entriesError && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            <Typography variant="body2" fontWeight="bold" mb={1}>
                                Failed to load transaction history
                            </Typography>
                            <Typography variant="caption">
                                {entriesQueryError instanceof Error
                                    ? entriesQueryError.message
                                    : 'Please check your connection.'}
                            </Typography>
                        </Alert>
                    )}

                    {/* TABLE */}
                    {ledger && !entriesError && (
                        <>
                            <TableContainer>
                                <Table>
                                    <TableHead sx={{ bgcolor: '#f5f5f5' }}>
                                        <TableRow>
                                            <TableCell><b>Date</b></TableCell>
                                            <TableCell><b>Type</b></TableCell>
                                            <TableCell><b>Amount</b></TableCell>
                                            <TableCell><b>Balance After</b></TableCell>
                                            <TableCell><b>Description</b></TableCell>
                                            <TableCell><b>Reference</b></TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {(!ledger.content || ledger.content.length === 0) && (
                                            <TableRow>
                                                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                                    <Typography color="text.secondary">
                                                        No transactions yet
                                                    </Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                        {ledger.content?.map((entry: LedgerEntry) => (
                                            <TableRow key={entry.id} hover>
                                                <TableCell sx={{ fontSize: 14 }}>
                                                    {new Date(entry.createdAt).toLocaleString()}
                                                </TableCell>
                                                <TableCell>
                                                    <Chip
                                                        icon={
                                                            entry.entryType === 'CREDIT' ? (
                                                                <TrendingUp />
                                                            ) : (
                                                                <TrendingDown />
                                                            )
                                                        }
                                                        label={entry.entryType}
                                                        color={
                                                            entry.entryType === 'CREDIT'
                                                                ? 'success'
                                                                : 'error'
                                                        }
                                                        size="small"
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <Typography
                                                        fontWeight="bold"
                                                        color={
                                                            entry.entryType === 'CREDIT'
                                                                ? 'success.main'
                                                                : 'error.main'
                                                        }
                                                    >
                                                        {entry.entryType === 'CREDIT' ? '+' : '-'}
                                                        {entry.currency}{' '}
                                                        {Number(entry.amount).toLocaleString('en-NG', {
                                                            minimumFractionDigits: 2,
                                                            maximumFractionDigits: 2,
                                                        })}
                                                    </Typography>
                                                </TableCell>
                                                <TableCell>
                                                    {entry.currency}{' '}
                                                    {Number(entry.balanceAfter).toLocaleString('en-NG', {
                                                        minimumFractionDigits: 2,
                                                        maximumFractionDigits: 2,
                                                    })}
                                                </TableCell>
                                                <TableCell sx={{ fontSize: 13 }}>
                                                    {entry.description}
                                                </TableCell>
                                                <TableCell sx={{ fontFamily: 'monospace', fontSize: 11 }}>
                                                    {entry.entryReference}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>

                            {ledger.content && ledger.content.length > 0 && (
                                <TablePagination
                                    component="div"
                                    count={ledger.totalElements || 0}
                                    page={page}
                                    rowsPerPage={rowsPerPage}
                                    onPageChange={(_, newPage) => setPage(newPage)}
                                    onRowsPerPageChange={(e) => {
                                        setRowsPerPage(parseInt(e.target.value, 10));
                                        setPage(0);
                                    }}
                                />
                            )}
                        </>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default AccountLedger;
