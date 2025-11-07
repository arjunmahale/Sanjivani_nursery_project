package com.example.nursery.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    private BigDecimal total = BigDecimal.ZERO;

    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    // path to generated bill PDF (absolute)
    private String billPath;

    // helpers
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.orderItems.add(item);
    }

    public void removeItem(OrderItem item) {
        item.setOrder(null);
        this.orderItems.remove(item);
    }

    // getters/setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public Customer getCustomer(){ return customer; }
    public void setCustomer(Customer customer){ this.customer = customer; }
    public List<OrderItem> getOrderItems(){ return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems){ this.orderItems = orderItems; }
    public OrderStatus getStatus(){ return status; }
    public void setStatus(OrderStatus status){ this.status = status; }
    public BigDecimal getTotal(){ return total; }
    public void setTotal(BigDecimal total){ this.total = total; }
    public LocalDate getDeliveryDate(){ return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate){ this.deliveryDate = deliveryDate; }
    public PaymentStatus getPaymentStatus(){ return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus){ this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public String getBillPath(){ return billPath; }
    public void setBillPath(String billPath){ this.billPath = billPath; }
}