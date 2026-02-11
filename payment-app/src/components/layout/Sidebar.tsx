// src/components/layout/Sidebar.tsx
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    ListItemButton,
    Divider,
} from '@mui/material';
import {
    Dashboard as DashboardIcon,
    Payment as PaymentIcon,
    People as PeopleIcon,
    Analytics as AnalyticsIcon,
    SmartToy as AIIcon,
} from '@mui/icons-material';
import { useAppSelector } from '@/store/hooks';

interface MenuItem {
    text: string;
    icon: React.ReactElement;
    path: string;
}

const menuItems: MenuItem[] = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { text: 'Payments', icon: <PaymentIcon />, path: '/payments' },
    { text: 'Customers', icon: <PeopleIcon />, path: '/customers' },
    { text: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
    { text: 'AI Assistant', icon: <AIIcon />, path: '/ai-assistant' },
];

const Sidebar: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const sidebarOpen = useAppSelector((state) => state.ui.sidebarOpen);

    return (
        <Drawer
            variant="permanent"
            sx={{
                width: sidebarOpen ? 240 : 70,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: sidebarOpen ? 240 : 70,
                    boxSizing: 'border-box',
                    mt: 8,
                    transition: 'width 0.3s',
                    overflowX: 'hidden',
                },
            }}
        >
            <Divider />
            <List>
                {menuItems.map((item) => (
                    <ListItem key={item.text} disablePadding>
                        <ListItemButton
                            selected={location.pathname.startsWith(item.path)}
                            onClick={() => navigate(item.path)}
                        >
                            <ListItemIcon>{item.icon}</ListItemIcon>
                            {sidebarOpen && <ListItemText primary={item.text} />}
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
        </Drawer>
    );
};

export default Sidebar;