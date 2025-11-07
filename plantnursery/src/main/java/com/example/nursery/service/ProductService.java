package com.example.nursery.service;

import com.example.nursery.model.Product;
import com.example.nursery.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public List<Product> listAll() { return repo.findAll(); }
    public Optional<Product> findById(Long id) { return repo.findById(id); }
    public Product save(Product p) { return repo.save(p); }
    public void delete(Long id) { repo.deleteById(id); }
    public List<Product> lowStock(Integer threshold) { return repo.findByStockQuantityLessThan(threshold); }
}