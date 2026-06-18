import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress, InputAdornment, IconButton,} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useAppDispatch } from '../../store/hooks';

import { loginSuccess } from '../../store/slices/authSlice';
import { authService } from '../../api/services/authService';

const Login = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // ✅ Helper function to decode JWT token
    const decodeToken = (token: string) => {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return {
                userId: payload.userId,
                email: payload.sub,
                merchantId: payload.merchantId,
                role: payload.role?.replace('ROLE_', '') || 'MERCHANT',
            };
        } catch (error) {
            console.error('Failed to decode token:', error);
            return null;
        }
    };

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

            console.log('✅ Login response:', response);

            // ✅ Decode JWT token to get user data
            const tokenData = decodeToken(response.accessToken);

            if (!tokenData) {
                throw new Error('Failed to decode authentication token');
            }

            // ✅ Build user object from token + response
            const user = {
                id: tokenData.userId || response.userId,
                email: tokenData.email || response.email || email,
                merchantId: tokenData.merchantId || response.merchantId,
                role: tokenData.role || response.role,
                // role: tokenData.role ,
                firstName: response.firstName || '',
                lastName: response.lastName || '',
            };

            console.log('👤 User object:', user);

            // Validate required fields
            if (!user.id || !user.email) {
                throw new Error('Invalid user data received from server');
            }

            // If merchantId is missing, use id as fallback
            if (!user.merchantId) {
                console.warn('⚠️ merchantId missing, using id as fallback');
                user.merchantId = user.id;
            }

            // Save token + user in Redux and localStorage
            dispatch(
                loginSuccess({
                    user,
                    accessToken: response.accessToken,
                })
            );

            console.log('✅ Login successful, redirecting to dashboard');

            // Redirect to dashboard
            navigate('/dashboard');

        } catch (err: any) {
            console.error('❌ Login error:', err);
            const msg =
                err.response?.data?.message ||
                err.message ||
                'Login failed. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box
            sx={{
                position: 'fixed',
                inset: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#f5f5f5',
            }}
        >
            <Card
                sx={{
                    width: '100%',
                    maxWidth: 420,
                    borderRadius: 3,
                    boxShadow: 3,
                    mx: 2,
                }}
            >
                <CardContent sx={{ p: 4 }}>
                    <Typography
                        variant="h5"
                        fontWeight="bold"
                        align="center"
                        gutterBottom
                    >
                        PaymentSolution
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        align="center"
                        mb={3}
                    >
                        Sign in to your account
                    </Typography>

                    {error && (
                        <Alert
                            severity="error"
                            sx={{ mb: 2 }}
                            onClose={() => setError('')}
                        >
                            {error}
                        </Alert>
                    )}

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
                                            onClick={() =>
                                                setShowPassword(!showPassword)
                                            }
                                            edge="end"
                                        >
                                            {showPassword ? (
                                                <VisibilityOff />
                                            ) : (
                                                <Visibility />
                                            )}
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
                            sx={{
                                mt: 3,
                                mb: 2,
                                py: 1.5,
                                borderRadius: 2,
                            }}
                        >
                            {loading ? (
                                <CircularProgress size={24} color="inherit" />
                            ) : (
                                'Sign In'
                            )}
                        </Button>
                    </form>

                    <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="body2" color="text.secondary">
                            Don't have an account?{' '}
                            <Link
                                to="/register"
                                style={{
                                    color: '#1976d2',
                                    textDecoration: 'none',
                                    fontWeight: 500,
                                }}
                            >
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