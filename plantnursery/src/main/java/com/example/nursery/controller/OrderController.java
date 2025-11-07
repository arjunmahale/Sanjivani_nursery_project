package com.example.nursery.controller;

import com.example.nursery.model.*;
import com.example.nursery.service.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    public OrderController(OrderService orderService, CustomerService customerService, ProductService productService){
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.listAll());
        return "orders";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        model.addAttribute("customers", customerService.listAll());
        model.addAttribute("products", productService.listAll());
        model.addAttribute("order", new Order());
        return "order-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam Long customerId,
                       @RequestParam(name = "productIds", required = false) List<Long> productIds,
                       @RequestParam(name = "quantities", required = false) List<Integer> quantities,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
                       @RequestParam(name="paymentStatus", required=false) PaymentStatus paymentStatus) {
        Order order = new Order();
        Customer c = new Customer();
        c.setId(customerId);
        order.setCustomer(c);
        order.setDeliveryDate(deliveryDate);
        if (paymentStatus != null) order.setPaymentStatus(paymentStatus);
        if (productIds != null && quantities != null && productIds.size() == quantities.size()) {
            for (int i = 0; i < productIds.size(); i++) {
                Long pid = productIds.get(i);
                Integer qty = quantities.get(i);
                if (qty == null || qty <= 0) continue;
                Product p = new Product();
                p.setId(pid);
                OrderItem oi = new OrderItem();
                oi.setProduct(p);
                oi.setQuantity(qty);
                order.addItem(oi);
            }
        }
        orderService.createOrder(order);
        return "redirect:/orders";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        orderService.findById(id).ifPresent(o -> model.addAttribute("order", o));
        return "order-view";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        orderService.delete(id);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/bill")
    public ResponseEntity<Resource> viewBill(@PathVariable Long id) {
        Order o = orderService.findById(id).orElse(null);
        if (o == null || o.getBillPath() == null) {
            return ResponseEntity.notFound().build();
        }
        File f = new File(o.getBillPath());
        if (!f.exists()) return ResponseEntity.notFound().build();
        Resource r = new FileSystemResource(f);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline().filename(f.getName()).build());
        headers.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok().headers(headers).body(r);
    }

    @PostMapping("/send-bill/{id}")
    public String sendBill(@PathVariable Long id) {
        orderService.generateBillAndNotify(id);
        return "redirect:/orders/view/" + id;
    }
}