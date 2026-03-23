package com.paymentsolutions.controller;
import com.paymentsolutions.dto.request.CreateWalletRequest;
import com.paymentsolutions.dto.request.FundAccountRequest;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.services.ILedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4004", "http://localhost:4003", "http://localhost:5173"})
public class AccountController {

    private final ILedgerService ledgerService;

    // ✅ Create or Get Wallet
    @PostMapping("/wallet")
    public AccountResponse createOrGetWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        return ledgerService.getOrCreateAccount(
                request.getOwnerId(),
                request.getOwnerType(),
                request.getCurrency());
    }

    // ✅ Get Account by ID
    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return ledgerService.getAccount(accountId);
    }

    // ✅ Get Account Balance by Owner
//    @GetMapping("/owner/balance")
//    public AccountResponse getAccountBalance(
//            @RequestParam UUID ownerId,
//            @RequestParam String ownerType,
//            @RequestParam String currency
//    ) {
//        return ledgerService.getAccountBalance(ownerId, ownerType, currency);
//    }
//
//    // ✅ Fund Account
//    @PostMapping("/{accountId}/fund")
//    public void fundAccount(
//            @PathVariable UUID accountId,
//            @RequestBody FundAccountRequest fundAccountRequest
//
//    ) {
//        ledgerService.fundAccount(accountId,fundAccountRequest.getAmount(), fundAccountRequest.getDescription());
//    }
}
