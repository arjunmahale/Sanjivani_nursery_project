package com.example.nursery.service;

import com.example.nursery.model.*;
import com.example.nursery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo, CustomerRepository customerRepo,
                        InvoiceService invoiceService, NotificationService notificationService){
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
    }

    public List<Order> listAll() { return orderRepo.findAll(); }
    public Optional<Order> findById(Long id) { return orderRepo.findById(id); }

    @Transactional
    public Order createOrder(Order order) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        Customer cust = customerRepo.findById(order.getCustomer().getId()).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        order.setCustomer(cust);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            Product p = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));
            if (p.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product " + p.getName());
            }
            p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
            productRepo.save(p);

            item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            total = total.add(item.getSubtotal());
        }
        order.setTotal(total);
        Order saved = orderRepo.save(order);

        try {
            String path = invoiceService.generateInvoicePdf(saved);
            saved.setBillPath(path);
            saved = orderRepo.save(saved);
            notificationService.sendBillNotification(saved);
        } catch (Exception ex) {
            System.err.println("Failed to generate/send invoice: " + ex.getMessage());
        }

        return saved;
    }

    @Transactional
    public Order updateOrder(Order order) {
        // naive update: ensure order exists and then update items, totals, stock adjustments if needed
        Order existing = orderRepo.findById(order.getId()).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // For simplicity: remove all items and re-apply stock adjustments is omitted here.
        existing.setDeliveryDate(order.getDeliveryDate());
        existing.setPaymentStatus(order.getPaymentStatus());
        existing.setStatus(order.getStatus());
        // any other updates...
        Order saved = orderRepo.save(existing);
        // regenerate bill & notify
        try {
            String path = invoiceService.generateInvoicePdf(saved);
            saved.setBillPath(path);
            saved = orderRepo.save(saved);
            notificationService.sendBillNotification(saved);
        } catch (Exception ex) {
            System.err.println("Failed to regenerate/send invoice: " + ex.getMessage());
        }
        return saved;
    }

    public void delete(Long id) { orderRepo.deleteById(id); }

    public List<Order> findByDeliveryDate(LocalDate date) { return orderRepo.findByDeliveryDate(date); }

    public List<Object[]> topSelling(LocalDateTime from, LocalDateTime to){ return orderRepo.findTopSellingProducts(from,to); }

    public List<Order> findOrdersBetween(LocalDateTime from, LocalDateTime to){ return orderRepo.findOrdersBetween(from,to); }

    public List<Order> findRecent(int limit){
        return orderRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional
    public Order generateBillAndNotify(Long orderId){
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        try {
            String path = invoiceService.generateInvoicePdf(o);
            o.setBillPath(path);
            o = orderRepo.save(o);
            notificationService.sendBillNotification(o);
        } catch (Exception ex) {
            System.err.println("Failed to generate/send invoice: " + ex.getMessage());
        }
        return o;
    }
}