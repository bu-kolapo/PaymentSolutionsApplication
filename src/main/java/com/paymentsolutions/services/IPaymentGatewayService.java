package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.InitiatePaymentRequest;
import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.PaymentGatewayResponse;
import com.paymentsolutions.model.Payment;

public interface IPaymentGatewayService {

    PaymentGatewayResponse processPayment(Payment payment, ProcessPaymentRequest request);
    PaymentGatewayResponse refund(Payment payment);
    PaymentGatewayResponse verifyPayment(String gatewayReference);
}
