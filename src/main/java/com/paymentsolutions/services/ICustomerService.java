package com.paymentsolutions.services;

import com.paymentsolutions.dto.request.CustomerRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.dto.response.CustomerResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for customer management operations.
 * Handles CRUD operations for customer entities.
 */
public interface ICustomerService {

    /**
     * Create a new customer for a merchant
     *
     * @param request Customer details
     * @param merchantId UUID of the merchant
     * @return CustomerResponse with created customer details
     */
    CustomerResponse createCustomer(CustomerRequest request, UUID merchantId);

    /**
     * Retrieve a customer by ID
     *
     * @param customerId UUID of the customer
     * @param merchantId UUID of the merchant (for authorization)
     * @return CustomerResponse with customer details
     * @throws ResourceNotFoundException if customer not found
     */
    CustomerResponse getCustomer(UUID customerId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get paginated list of customers for a merchant
     *
     * @param merchantId UUID of the merchant
     * @param pageable Pagination parameters
     * @return Page of CustomerResponse objects
     */
    Page<CustomerResponse> getCustomers(UUID merchantId, Pageable pageable);

    /**
     * Search customers by email or name
     *
     * @param merchantId UUID of the merchant
     * @param searchTerm Search query
     * @param pageable Pagination parameters
     * @return Page of matching customers
     */
    Page<CustomerResponse> searchCustomers(UUID merchantId, String searchTerm, Pageable pageable);

    /**
     * Update customer information
     *
     * @param customerId UUID of the customer
     * @param request Updated customer details
     * @param merchantId UUID of the merchant (for authorization)
     * @return Updated CustomerResponse
     */
    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Delete a customer
     *
     * @param customerId UUID of the customer to delete
     * @param merchantId UUID of the merchant (for authorization)
     */
    void deleteCustomer(UUID customerId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get customer payment history
     *
     * @param customerId UUID of the customer
     * @param merchantId UUID of the merchant
     * @param pageable Pagination parameters
     * @return Page of payments for this customer
     */
    Page<PaymentResponse> getCustomerPayments(
            UUID customerId, UUID merchantId, Pageable pageable);
}