package com.example.nursery.controller;

import com.example.nursery.service.ProductService;
import com.example.nursery.service.OrderService;
import com.example.nursery.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;

    public DashboardController(CustomerService customerService, ProductService productService, OrderService orderService){
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("customersCount", customerService.listAll().size());
        model.addAttribute("productsCount", productService.listAll().size());
        model.addAttribute("ordersCount", orderService.listAll().size());
        model.addAttribute("recentOrders", orderService.findRecent(6));
        model.addAttribute("lowStock", productService.lowStock(5));
        return "dashboard";
    }
}