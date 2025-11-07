package com.example.nursery.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sun.istack.NotNull;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    // getters/setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public Product getProduct(){ return product; }
    public void setProduct(Product product){ this.product = product; }
    public Integer getQuantity(){ return quantity; }
    public void setQuantity(Integer quantity){ this.quantity = quantity; }
    public BigDecimal getSubtotal(){ return subtotal; }
    public void setSubtotal(BigDecimal subtotal){ this.subtotal = subtotal; }
    public Order getOrder(){ return order; }
    public void setOrder(Order order){ this.order = order; }
}