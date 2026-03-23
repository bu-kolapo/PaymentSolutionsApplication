import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Box, Card, CardContent, CircularProgress, Alert,
    Typography, Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TablePagination, Chip, Grid,
} from '@mui/material';
import { AccountBalance, TrendingUp, TrendingDown } from '@mui/icons-material';
import axiosInstance from '../../api/axios.config';

const AccountLedger = () => {
    const [page, setPage]               = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    // Fetch merchant account
    const [merchantId, setMerchantId] = useState<string>('');
        useEffect(() => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            const user = JSON.parse(userStr);
            setMerchantId(user.merchantId || user.id);
        }
    }, []);

    const { data: account } = useQuery({
        queryKey: ['merchantAccount', merchantId],
        queryFn: async () => {
            const res = await axiosInstance.post('/api/v1/ledger/account/merchant', null, {
                params: { merchantId, currency: 'NGN' }
            });
            return res.data;
        },
        enabled: !!merchantId,
    });


    // Fetch ledger entries
    const { data: ledger, isLoading, isError } = useQuery({
        queryKey: ['ledger', account?.id, page, rowsPerPage],
        queryFn: async () => {
            const res = await axiosInstance.get(
                `/api/v1/ledger/account/${account.id}/entries?page=${page}&size=${rowsPerPage}`
            );
            return res.data;
        },
        enabled: !!account?.id,
    });

    if (!account) return <CircularProgress />;

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
                                {account.currency} {Number(account.balance).toLocaleString('en-US', {
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
                    <Typography variant="h6" mb={2}>Transaction History</Typography>
                    {isLoading && <CircularProgress />}
                    {isError && <Alert severity="error">Failed to load ledger</Alert>}
                    {ledger && (
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
                                        {ledger.content.length === 0 && (
                                            <TableRow>
                                                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                                    <Typography color="text.secondary">No transactions yet</Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                        {ledger.content.map((entry: any) => (
                                            <TableRow key={entry.id} hover>
                                                <TableCell>
                                                    {new Date(entry.createdAt).toLocaleString()}
                                                </TableCell>
                                                <TableCell>
                                                    <Chip
                                                        icon={entry.entryType === 'CREDIT' ? <TrendingUp /> : <TrendingDown />}
                                                        label={entry.entryType}
                                                        color={entry.entryType === 'CREDIT' ? 'success' : 'error'}
                                                        size="small"
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <Typography
                                                        fontWeight="bold"
                                                        color={entry.entryType === 'CREDIT' ? 'success.main' : 'error.main'}
                                                    >
                                                        {entry.entryType === 'CREDIT' ? '+' : '-'}
                                                        {entry.currency} {Number(entry.amount).toFixed(2)}
                                                    </Typography>
                                                </TableCell>
                                                <TableCell>
                                                    {entry.currency} {Number(entry.balanceAfter).toFixed(2)}
                                                </TableCell>
                                                <TableCell>{entry.description}</TableCell>
                                                <TableCell sx={{ fontFamily: 'monospace', fontSize: 11 }}>
                                                    {entry.entryReference}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
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
                        </>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default AccountLedger;