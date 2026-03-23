package com.paymentsolutions.repository;



import com.paymentsolutions.model.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.*;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {
    /**
     * Get all events for a payment in chronological order
     */
    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    /**
     * Get events by type
     */
    List<PaymentEvent> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Get failed events
     */
    List<PaymentEvent> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Get events within time range
     */
    List<PaymentEvent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}


