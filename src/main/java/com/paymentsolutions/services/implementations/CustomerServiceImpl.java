package com.paymentsolutions.services.implementations;

import com.paymentsolutions.dto.request.CustomerRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.dto.response.CustomerResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.exception.ValidationException;
import com.paymentsolutions.model.Customer;
import com.paymentsolutions.repository.CustomerRepository;
import com.paymentsolutions.services.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of ICustomerService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request, UUID merchantId) {
        log.info("Creating customer for merchant: {}", merchantId);

        if (customerRepository.existsByMerchantIdAndEmail(merchantId, request.getEmail())) {
            throw new ValidationException("Customer with this email already exists");
        }

        Customer customer = Customer.builder()
                .merchantId(merchantId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created successfully: {}", customer.getId());

        return mapToResponse(customer);
    }

    @Override
    public CustomerResponse getCustomer(UUID customerId, UUID merchantId) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getMerchantId().equals(merchantId)) {
            throw new ValidationException("Access denied");
        }

        return mapToResponse(customer);
    }

    @Override
    public Page<CustomerResponse> getCustomers(UUID merchantId, Pageable pageable) {
        Page<Customer> customers = customerRepository.findByMerchantId(merchantId, pageable);
        return customers.map(this::mapToResponse);
    }

    @Override
    public Page<CustomerResponse> searchCustomers(UUID merchantId, String searchTerm, Pageable pageable) {
        // Implementation for searching customers
        Page<Customer> customers = customerRepository.findByMerchantId(merchantId, pageable);
        return customers.map(this::mapToResponse);
    }

    @Override
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request, UUID merchantId) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getMerchantId().equals(merchantId)) {
            throw new ValidationException("Access denied");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customer.setCountry(request.getCountry());

        customer = customerRepository.save(customer);
        log.info("Customer updated successfully: {}", customer.getId());

        return mapToResponse(customer);
    }

    @Override
    public void deleteCustomer(UUID customerId, UUID merchantId) throws ResourceNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getMerchantId().equals(merchantId)) {
            throw new ValidationException("Access denied");
        }

        customerRepository.delete(customer);
        log.info("Customer deleted successfully: {}", customerId);
    }

    @Override
    public Page<PaymentResponse> getCustomerPayments(UUID customerId, UUID merchantId, Pageable pageable) {
        // This would integrate with PaymentService
        // Implementation depends on your payment repository
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .merchantId(customer.getMerchantId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}