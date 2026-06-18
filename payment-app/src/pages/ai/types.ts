/**
 * AI Agent Type Definitions
 */

export interface AIQueryRequest {
    message: string;
    context?: string;
    conversationId?: string;
    period?: number;
}

export interface AIResponse {
    message: string;
    conversationId?: string;
    timestamp: string;
    data?: Record<string, any>;
    success?: boolean;
    error?: string;
}

export interface ConversationMessage {
    id: string;
    role: 'user' | 'assistant';
    message: string;
    timestamp: string;
    context?: string;
}