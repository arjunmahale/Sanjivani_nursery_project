package com.example.nursery.controller;

import com.example.nursery.model.Product;
import com.example.nursery.service.ProductService;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventory")
public class ProductController {
    private final ProductService svc;
    public ProductController(ProductService svc) { this.svc = svc; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", svc.listAll());
        return "inventory";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Product product, BindingResult br) {
        if (br.hasErrors()) return "product-form";
        svc.save(product);
        return "redirect:/inventory";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        svc.findById(id).ifPresent(p -> model.addAttribute("product", p));
        return "product-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        svc.delete(id);
        return "redirect:/inventory";
    }
}