package com.example.nursery.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.sun.istack.NotNull;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String species;

    private String category;

    @NotNull
    private BigDecimal price = BigDecimal.ZERO;

    @NotNull
    private Integer stockQuantity = 0;

    // getters/setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getSpecies(){ return species; }
    public void setSpecies(String species){ this.species = species; }
    public String getCategory(){ return category; }
    public void setCategory(String category){ this.category = category; }
    public BigDecimal getPrice(){ return price; }
    public void setPrice(BigDecimal price){ this.price = price; }
    public Integer getStockQuantity(){ return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity){ this.stockQuantity = stockQuantity; }
}