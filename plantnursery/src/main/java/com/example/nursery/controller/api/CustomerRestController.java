package com.example.nursery.controller.api;

import com.example.nursery.model.Customer;
import com.example.nursery.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    private final CustomerService svc;
    public CustomerRestController(CustomerService svc){ this.svc = svc; }

    @GetMapping
    public List<Customer> all() { return svc.listAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        return svc.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Customer create(@RequestBody Customer c) { return svc.save(c); }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer c) {
        return svc.findById(id).map(existing -> {
            c.setId(existing.getId());
            return ResponseEntity.ok(svc.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}