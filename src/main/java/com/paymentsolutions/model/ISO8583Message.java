package com.paymentsolutions.model;

import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ISO8583Message {

    /**
     * Message Type Indicator
     * 0100 = Authorization Request
     * 0110 = Authorization Response
     * 0200 = Financial Request
     * 0210 = Financial Response
     * 0400 = Reversal Request
     * 0420 = Reversal Advice
     */
    private String mti;

    /**
     * Data elements (fields 1-128)
     * Common fields:
     * DE2  = PAN (Primary Account Number)
     * DE3  = Processing Code
     * DE4  = Transaction Amount
     * DE7  = Transmission Date & Time
     * DE11 = STAN (System Trace Audit Number)
     * DE12 = Local Transaction Time
     * DE13 = Local Transaction Date
     * DE22 = POS Entry Mode
     * DE37 = Retrieval Reference Number
     * DE39 = Response Code
     * DE41 = Terminal ID
     * DE42 = Merchant ID
     * DE49 = Currency Code
     */
    private Map<Integer, String> dataElements = new HashMap<>();

    // Convenience methods
    public String getPAN() {
        return dataElements.get(2);
    }

    public String getAmount() {
        return dataElements.get(4);
    }

    public String getSTAN() {
        return dataElements.get(11);
    }

    public String getResponseCode() {
        return dataElements.get(39);
    }

    public void setResponseCode(String code) {
        dataElements.put(39, code);
    }
}
