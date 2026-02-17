// =====================================================
// 5. src/pages/auth/Register.tsx
//    Fully wired register page
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
    Grid,
} from '@mui/material';
import { useAppDispatch } from '../../store/hooks';
import { setCredentials } from '../../store/slices/authSlice';
import { authService } from '../../api/services/authService';

const Register = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        businessName: '',
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        phone: '',
    });
    const [loading, setLoading] = useState(false);
    const [error,   setError]   = useState('');

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (form.password !== form.confirmPassword) {
            setError('Passwords do not match.');
            return;
        }
        if (form.password.length < 8) {
            setError('Password must be at least 8 characters.');
            return;
        }

        setLoading(true);
        try {
            // Call POST /api/v1/auth/register
            const response = await authService.register({
                businessName: form.businessName,
                firstName:    form.firstName,
                lastName:     form.lastName,
                email:        form.email,
                password:     form.password,
                phone:        form.phone,
            });

            // Save credentials — auto-login after registration
            dispatch(setCredentials({
                user: {
                    id:         response.userId,
                    email:      response.email,
                    role:       response.role,
                    merchantId: response.merchantId,
                },
                accessToken: response.accessToken,
            }));

            navigate('/dashboard');

        } catch (err: any) {
            const msg = err.response?.data?.message || err.message || 'Registration failed.';
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
                py: 4,
            }}
        >
            <Card sx={{ maxWidth: 520, width: '100%', mx: 2, borderRadius: 3 }}>
                <CardContent sx={{ p: 4 }}>

                    <Typography variant="h5" fontWeight="bold" align="center" gutterBottom>
                        Create Account
                    </Typography>
                    <Typography variant="body2" color="text.secondary" align="center" mb={3}>
                        Start accepting payments today
                    </Typography>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
                            {error}
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Business Name"
                            name="businessName"
                            value={form.businessName}
                            onChange={handleChange}
                            margin="normal"
                            required
                            disabled={loading}
                        />

                        <Grid container spacing={2} sx={{ mt: 0 }}>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="First Name"
                                    name="firstName"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    required
                                    disabled={loading}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Last Name"
                                    name="lastName"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    required
                                    disabled={loading}
                                />
                            </Grid>
                        </Grid>

                        <TextField
                            fullWidth
                            label="Email"
                            name="email"
                            type="email"
                            value={form.email}
                            onChange={handleChange}
                            margin="normal"
                            required
                            disabled={loading}
                        />

                        <TextField
                            fullWidth
                            label="Phone (optional)"
                            name="phone"
                            value={form.phone}
                            onChange={handleChange}
                            margin="normal"
                            disabled={loading}
                        />

                        <TextField
                            fullWidth
                            label="Password"
                            name="password"
                            type="password"
                            value={form.password}
                            onChange={handleChange}
                            margin="normal"
                            required
                            disabled={loading}
                            helperText="Minimum 8 characters"
                        />

                        <TextField
                            fullWidth
                            label="Confirm Password"
                            name="confirmPassword"
                            type="password"
                            value={form.confirmPassword}
                            onChange={handleChange}
                            margin="normal"
                            required
                            disabled={loading}
                        />

                        <Button
                            fullWidth
                            variant="contained"
                            type="submit"
                            disabled={loading}
                            sx={{ mt: 3, mb: 2, py: 1.5, borderRadius: 2 }}
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Account'}
                        </Button>
                    </form>

                    <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="body2" color="text.secondary">
                            Already have an account?{' '}
                            <Link to="/login" style={{ color: '#1976d2', textDecoration: 'none' }}>
                                Sign in
                            </Link>
                        </Typography>
                    </Box>

                </CardContent>
            </Card>
        </Box>
    );
};

export default Register;

