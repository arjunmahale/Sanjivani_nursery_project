package com.example.nursery.controller;

import com.example.nursery.model.Customer;
import com.example.nursery.service.CustomerService;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService svc;
    public CustomerController(CustomerService svc) { this.svc = svc; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", svc.listAll());
        return "customers";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Customer customer, BindingResult br) {
        if (br.hasErrors()) return "customer-form";
        svc.save(customer);
        return "redirect:/customers";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        svc.findById(id).ifPresent(c -> model.addAttribute("customer", c));
        return "customer-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        svc.delete(id);
        return "redirect:/customers";
    }
}