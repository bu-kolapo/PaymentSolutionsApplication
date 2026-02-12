package com.paymentsolutions.repository;

import com.paymentsolutions.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Find all notifications for a merchant
    Page<Notification> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    // Find by merchant and type (EMAIL, SMS)
    List<Notification> findByMerchantIdAndType(UUID merchantId, String type);

    // Find unread notifications
    List<Notification> findByMerchantIdAndReadFalseOrderByCreatedAtDesc(UUID merchantId);

    // Count unread notifications
    long countByMerchantIdAndReadFalse(UUID merchantId);

    // Find notifications linked to a specific payment
    List<Notification> findByPaymentId(UUID paymentId);

    // Find notifications by status (SENT, FAILED, PENDING)
    Page<Notification> findByMerchantIdAndStatus(
            UUID merchantId, String status, Pageable pageable);

    // Count notifications sent within a time range
    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.merchantId = :merchantId
            AND n.createdAt >= :since
            """)
    long countByMerchantIdSince(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );

    // Count by type within time range
    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.merchantId = :merchantId
            AND n.type = :type
            AND n.createdAt >= :since
            """)
    long countByMerchantIdAndTypeSince(
            @Param("merchantId") UUID merchantId,
            @Param("type") String type,
            @Param("since") LocalDateTime since
    );
}
