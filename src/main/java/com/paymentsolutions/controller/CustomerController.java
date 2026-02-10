package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.CustomerRequest;
import com.paymentsolutions.dto.response.CustomerResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for customer operations.
 * Uses ICustomerService interface for loose coupling.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final ICustomerService customerService;

    @PostMapping
    @Operation(summary = "Create new customer")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal User user) {
        CustomerResponse response = customerService.createCustomer(request, user.getMerchantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer details")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable UUID customerId,
            @AuthenticationPrincipal User user)throws ResourceNotFoundException {
        CustomerResponse response = customerService.getCustomer(customerId, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            Pageable pageable,
            @AuthenticationPrincipal User user) {
        Page<CustomerResponse> customers = customerService.getCustomers(user.getMerchantId(), pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers")
    public ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @RequestParam String query,
            Pageable pageable,
            @AuthenticationPrincipal User user) {
        Page<CustomerResponse> customers = customerService.searchCustomers(
                user.getMerchantId(), query, pageable);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal User user)throws ResourceNotFoundException {
        CustomerResponse response = customerService.updateCustomer(
                customerId, request, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable UUID customerId,
            @AuthenticationPrincipal User user)throws ResourceNotFoundException {
        customerService.deleteCustomer(customerId, user.getMerchantId());
        return ResponseEntity.noContent().build();
    }
}
