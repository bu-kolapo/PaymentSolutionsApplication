import { useState, useRef, useEffect } from 'react';
import {
    Box, Card, CardContent, TextField, Button, CircularProgress,
    Typography, Paper, Alert, Divider, Chip,
} from '@mui/material';
import { Send as SendIcon, Refresh } from '@mui/icons-material';
import { useAIAgent } from './useAIAgent';
import { ConversationMessage, AIQueryRequest } from './types';

const AIChat = () => {
    const { chat, loading, error, clearError } = useAIAgent();
    const [conversationId] = useState(() => Math.random().toString(36).substring(7));
    const [messages, setMessages] = useState<ConversationMessage[]>([
        {
            id: '1',
            role: 'assistant',
            message: '👋 Hi! I\'m your AI Payment Assistant. Ask me anything about your payment data, performance, or recommendations.',
            timestamp: new Date().toISOString(),
        },
    ]);
    const [input, setInput] = useState('');
    const [context, setContext] = useState('');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    // Auto-scroll to bottom
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const handleSendMessage = async () => {
        if (!input.trim()) return;

        // Add user message
        const userMessage: ConversationMessage = {
            id: Date.now().toString(),
            role: 'user',
            message: input,
            timestamp: new Date().toISOString(),
            context,
        };

        setMessages(prev => [...prev, userMessage]);
        setInput('');

        // Get AI response
        const request: AIQueryRequest = {
            message: input,
            context: context || undefined,
            conversationId,
        };

        const response = await chat(request);

        if (response) {
            const assistantMessage: ConversationMessage = {
                id: Date.now().toString(),
                role: 'assistant',
                message: response.message,
                timestamp: response.timestamp || new Date().toISOString(),
            };
            setMessages(prev => [...prev, assistantMessage]);
        }
    };

    const handleClearChat = () => {
        setMessages([
            {
                id: '1',
                role: 'assistant',
                message: '👋 Chat cleared! Ask me anything about your payments.',
                timestamp: new Date().toISOString(),
            },
        ]);
    };

    return (
        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 3 }}>
            <CardContent sx={{ flex: 1, display: 'flex', flexDirection: 'column', p: 0 }}>
                {/* Header */}
                <Box sx={{ p: 2, borderBottom: '1px solid #e0e0e0' }}>
                    <Typography variant="h6" fontWeight="bold">
                        💬 AI Chat Assistant
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                        Ask questions about your payment data
                    </Typography>
                </Box>

                {/* Messages */}
                <Box
                    sx={{
                        flex: 1,
                        overflowY: 'auto',
                        p: 2,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 2,
                    }}
                >
                    {error && (
                        <Alert severity="error" onClose={clearError}>
                            {error}
                        </Alert>
                    )}

                    {messages.map(msg => (
                        <Box
                            key={msg.id}
                            sx={{
                                display: 'flex',
                                justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
                            }}
                        >
                            <Paper
                                sx={{
                                    maxWidth: '80%',
                                    p: 2,
                                    bgcolor: msg.role === 'user' ? '#1976d2' : '#f5f5f5',
                                    color: msg.role === 'user' ? 'white' : 'black',
                                    borderRadius: 2,
                                }}
                            >
                                <Typography variant="body2">{msg.message}</Typography>
                                <Typography
                                    variant="caption"
                                    sx={{
                                        display: 'block',
                                        mt: 1,
                                        opacity: 0.7,
                                    }}
                                >
                                    {new Date(msg.timestamp).toLocaleTimeString()}
                                </Typography>
                            </Paper>
                        </Box>
                    ))}

                    {loading && (
                        <Box display="flex" justifyContent="center" py={2}>
                            <CircularProgress size={30} />
                        </Box>
                    )}

                    <div ref={messagesEndRef} />
                </Box>

                <Divider />

                {/* Context Input */}
                <Box sx={{ p: 2, bgcolor: '#f9f9f9', borderBottom: '1px solid #e0e0e0' }}>
                    <Typography variant="caption" fontWeight="bold" display="block" mb={1}>
                        Optional Context
                    </Typography>
                    <TextField
                        fullWidth
                        size="small"
                        placeholder="e.g., 'about my last 7 days' or 'comparing to last month'"
                        value={context}
                        onChange={(e) => setContext(e.target.value)}
                        variant="outlined"
                    />
                </Box>

                {/* Input Area */}
                <Box sx={{ p: 2, display: 'flex', gap: 1 }}>
                    <TextField
                        fullWidth
                        size="small"
                        placeholder="Ask something... (e.g., 'Why did my success rate drop?')"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                        disabled={loading}
                        variant="outlined"
                    />
                    <Button
                        variant="contained"
                        onClick={handleSendMessage}
                        disabled={loading || !input.trim()}
                        endIcon={<SendIcon />}
                    >
                        Send
                    </Button>
                    <Button
                        variant="outlined"
                        size="small"
                        onClick={handleClearChat}
                        title="Clear chat history"
                    >
                        Clear
                    </Button>
                </Box>
            </CardContent>
        </Card>
    );
};

export default AIChat;