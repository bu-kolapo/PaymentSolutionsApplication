package com.paymentsolutions.repository;
import com.paymentsolutions.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByOwnerIdAndOwnerType(UUID ownerId, String ownerType);

    Optional<Account> findByOwnerIdAndOwnerTypeAndCurrency(
            UUID ownerId, String ownerType, String currency);

    List<Account> findByAccountType(String accountType);
    List<Account> findByCustomerId(UUID customerId);

    /**
     * Lock account for update (prevents race conditions)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(UUID id);


    boolean existsByAccountNumber(String accountNumber);



    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.currency = :currency AND a.accountType = :accountType")
    Optional<Account> findByCustomerIdAndCurrencyAndAccountType(
            @Param("customerId") UUID customerId,
            @Param("currency") String currency,
            @Param("accountType") String accountType);
    Optional<Account> findByOwnerId(UUID ownerId);

}


