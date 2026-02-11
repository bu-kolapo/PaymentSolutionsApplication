
export interface AnalyticsData {
    totalRevenue: number;
    totalTransactions: number;
    successfulTransactions: number;
    failedTransactions: number;
    successRate: number;
    averageTransactionValue: number;
    period: string;
}

export interface ChartData {
    date: string;
    revenue: number;
    transactions: number;
}