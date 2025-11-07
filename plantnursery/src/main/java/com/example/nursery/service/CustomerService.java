package com.example.nursery.service;

import com.example.nursery.model.Customer;
import com.example.nursery.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    public List<Customer> listAll() { return repo.findAll(); }
    public Optional<Customer> findById(Long id) { return repo.findById(id); }
    public Customer save(Customer c) { return repo.save(c); }
    public void delete(Long id) { repo.deleteById(id); }
}