package com.paymentsolutions.services;


import com.paymentsolutions.model.ISO8583Message;

/**
 * ISO 8583 message processor for card transactions
 * Used for ATM, POS, and card-not-present transactions
 */
public interface IISO8583Service {

    /**
     * Parse incoming ISO 8583 message
     */
    ISO8583Message parseMessage(byte[] messageBytes);

    /**
     * Process authorization request (MTI 0100)
     */
    ISO8583Message processAuthorizationRequest(ISO8583Message request);

    /**
     * Process financial request (MTI 0200)
     */
    ISO8583Message processFinancialRequest(ISO8583Message request);

    /**
     * Process reversal request (MTI 0400)
     */
    ISO8583Message processReversalRequest(ISO8583Message request);

    /**
     * Build ISO 8583 response message
     */
    byte[] buildResponseMessage(ISO8583Message response);
}

