// src/components/layout/MainLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Header from './Header';
import Sidebar from './Sidebar';
import { useAppSelector } from '@/store/hooks';

const MainLayout: React.FC = () => {
    const sidebarOpen = useAppSelector((state) => state.ui.sidebarOpen);

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh' }}>
            <Header />
            <Sidebar />
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    mt: 8,
                    ml: sidebarOpen ? '240px' : '70px',
                    transition: 'margin-left 0.3s',
                    backgroundColor: '#f5f5f5',
                }}
            >
                <Outlet />
            </Box>
        </Box>
    );
};

export default MainLayout;