package com.example.ricms.repository;

import com.example.ricms.domain.entity.Order;
import com.example.ricms.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByBuyerUserIdAndIdempotencyKeyHash(UUID buyerUserId, String idempotencyKeyHash);

    @Query("SELECT o FROM Order o WHERE o.status = com.example.ricms.domain.enums.OrderStatus.PLACED AND o.createdAt < :cutoff")
    List<Order> findTimedOutOrders(@Param("cutoff") OffsetDateTime cutoff);

    @Query("SELECT o FROM Order o WHERE " +
           "(:buyerId IS NULL OR o.buyerUserId = :buyerId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:orderNumber IS NULL OR o.orderNumber = :orderNumber)")
    Page<Order> findByBuyerUserIdWithFilters(
            @Param("buyerId") UUID buyerId,
            @Param("status") OrderStatus status,
            @Param("orderNumber") String orderNumber,
            Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPayable), 0) FROM Order o WHERE o.status = com.example.ricms.domain.enums.OrderStatus.COMPLETED")
    java.math.BigDecimal sumCompletedRevenue();
}
