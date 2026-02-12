import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { Toaster } from 'react-hot-toast';
import { store } from './store';
import { theme } from './theme/theme';
import AppRoutes from './routes/AppRoutes';

// Create React Query client
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1,
            staleTime: 5 * 60 * 1000, // 5 minutes
        },
    },
});

function App() {
    return (
        <Provider store={store}>
            <QueryClientProvider client={queryClient}>
                <BrowserRouter>
                    <ThemeProvider theme={theme}>
                        <CssBaseline />
                        <AppRoutes />
                        <Toaster position="top-right" />
                    </ThemeProvider>
                </BrowserRouter>
                <ReactQueryDevtools initialIsOpen={false} />
            </QueryClientProvider>
        </Provider>
    );
}

export default App;

// import { BrowserRouter, Routes, Route } from 'react-router-dom';
//
// function Login() {
//     return <div style={{ padding: '50px' }}><h1>Login Page</h1></div>;
// }
//
// function Dashboard() {
//     return <div style={{ padding: '50px' }}><h1>Dashboard</h1></div>;
// }
//
// function App() {
//     return (
//         <BrowserRouter>
//             <Routes>
//                 <Route path="/login" element={<Login />} />
//                 <Route path="/dashboard" element={<Dashboard />} />
//                 <Route path="/" element={<Login />} />
//             </Routes>
//         </BrowserRouter>
//     );
// }
//
// export default App;