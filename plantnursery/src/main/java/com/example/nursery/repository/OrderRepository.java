package com.example.nursery.repository;

import com.example.nursery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByDeliveryDate(LocalDate date);

    @Query("select o from Order o where o.createdAt >= ?1 and o.createdAt < ?2")
    List<Order> findOrdersBetween(LocalDateTime from, LocalDateTime to);

    @Query("select oi.product.id, oi.product.name, sum(oi.quantity) as qty " +
            "from Order o join o.orderItems oi " +
            "where o.createdAt >= ?1 and o.createdAt < ?2 " +
            "group by oi.product.id, oi.product.name " +
            "order by qty desc")
    List<Object[]> findTopSellingProducts(LocalDateTime from, LocalDateTime to);

    // convenience to fetch recent orders (DB-level limit)
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}