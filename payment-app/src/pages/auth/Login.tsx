// =====================================================
// 4. src/pages/auth/Login.tsx
//    Fully wired login page
// =====================================================

import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Alert,
    CircularProgress,
    InputAdornment,
    IconButton,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useAppDispatch } from '../../store/hooks';
import { setCredentials } from '../../store/slices/authSlice';
import { authService } from '../../api/services/authService';

const Login = () => {
    const dispatch   = useAppDispatch();
    const navigate   = useNavigate();

    const [email,       setEmail]       = useState('');
    const [password,    setPassword]    = useState('');
    const [showPassword,setShowPassword]= useState(false);
    const [loading,     setLoading]     = useState(false);
    const [error,       setError]       = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        // Basic validation
        if (!email || !password) {
            setError('Please enter both email and password.');
            return;
        }

        setLoading(true);
        try {
            // Call POST /api/v1/auth/login
            const response = await authService.login({ email, password });

            // Save token + user in Redux and localStorage
            dispatch(setCredentials({
                user: {
                    id:         response.userId,
                    email:      response.email,
                    role:       response.role,
                    merchantId: response.merchantId,
                },
                accessToken: response.accessToken,
            }));

            // Redirect to dashboard
            navigate('/dashboard');

        } catch (err: any) {
            const msg = err.response?.data?.message || err.message || 'Login failed. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: '#f5f5f5',
            }}
        >
            <Card sx={{ maxWidth: 420, width: '100%', mx: 2, borderRadius: 3 }}>
                <CardContent sx={{ p: 4 }}>

                    {/* Header */}
                    <Typography variant="h5" fontWeight="bold" align="center" gutterBottom>
                        PaymentSolution
                    </Typography>
                    <Typography variant="body2" color="text.secondary" align="center" mb={3}>
                        Sign in to your account
                    </Typography>

                    {/* Error alert */}
                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
                            {error}
                        </Alert>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            margin="normal"
                            required
                            autoFocus
                            disabled={loading}
                        />

                        <TextField
                            fullWidth
                            label="Password"
                            type={showPassword ? 'text' : 'password'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            margin="normal"
                            required
                            disabled={loading}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowPassword(!showPassword)}
                                            edge="end"
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />

                        <Button
                            fullWidth
                            variant="contained"
                            type="submit"
                            disabled={loading}
                            sx={{ mt: 3, mb: 2, py: 1.5, borderRadius: 2 }}
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
                        </Button>
                    </form>

                    <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="body2" color="text.secondary">
                            Don't have an account?{' '}
                            <Link to="/register" style={{ color: '#1976d2', textDecoration: 'none' }}>
                                Sign up
                            </Link>
                        </Typography>
                    </Box>

                </CardContent>
            </Card>
        </Box>
    );
};

export default Login;