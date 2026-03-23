import {
    Dashboard as DashboardIcon,
    Payment as PaymentIcon,
    Receipt as ReceiptIcon,  // ← ADD THIS
    AccountBalance as AccountBalanceIcon,
    Settings as SettingsIcon,
    // ... other imports
} from '@mui/icons-material';
import {
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
} from '@mui/material';

import { useNavigate, useLocation } from 'react-router-dom';

interface SidebarProps {
    open: boolean;
}

const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { text: 'Payment Requests', icon: <PaymentIcon />, path: '/payments' },
    { text: 'Transactions', icon: <ReceiptIcon />, path: '/transactions' },
    { text: 'Settlement Account',  icon: <SettingsIcon/> , path: '/account/setup' },
    { text: 'Account Ledger', icon: <AccountBalanceIcon />, path: '/ledger' },
];

const Sidebar = ({ open }: SidebarProps) => {
    const navigate = useNavigate();
    const location = useLocation();

    return (
        <Drawer
            variant="persistent"
            open={open}
            sx={{
                width: 240,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: 240,
                    boxSizing: 'border-box',
                },
            }}
        >
            <Toolbar />
            <List>
                {menuItems.map((item) => (
                    <ListItem key={item.text} disablePadding>
                        <ListItemButton
                            selected={location.pathname === item.path}
                            onClick={() => navigate(item.path)}
                        >
                            <ListItemIcon>{item.icon}</ListItemIcon>
                            <ListItemText primary={item.text} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
        </Drawer>
    );
};

export default Sidebar;