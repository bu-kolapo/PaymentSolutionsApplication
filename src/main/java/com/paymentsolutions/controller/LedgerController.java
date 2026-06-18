package com.paymentsolutions.controller;

import com.paymentsolutions.dto.request.CreateWalletRequest;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.dto.response.LedgerEntryResponse;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.ILedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ledger", description = "Account balance and ledger history endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4004", "http://localhost:4003", "http://localhost:5173"})
public class LedgerController {

    private final ILedgerService ledgerService;

    /**
     * Get or create merchant settlement account
     * Auto-called when merchant registers
     */
    @PostMapping("/account/merchant")
    public ResponseEntity<AccountResponse> getOrCreateMerchantAccount(
            @RequestParam UUID merchantId,
            @RequestParam String currency
    ) {
        log.info("💼 Getting merchant settlement account for: {}", merchantId);

        AccountResponse account = ledgerService.getOrCreateAccount(merchantId, "MERCHANT", currency);

        return ResponseEntity.ok(account);
    }

    /**
     * Get account details
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        AccountResponse account = ledgerService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }


    @GetMapping("/account/{accountId}/entries")
    public ResponseEntity<Page<LedgerEntryResponse>> getLedgerEntries(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ledgerService.getLedgerEntries(accountId, pageable));
    }
}


