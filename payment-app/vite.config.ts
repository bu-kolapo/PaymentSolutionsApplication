// vite.config.ts

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        port: 4003,
        strictPort: false,
        proxy: {
            '/api': {                        // ✅ /api calls routed to backend
                target: 'http://localhost:8081',
                changeOrigin: true,
                secure: false,
            },
        },
    },
})