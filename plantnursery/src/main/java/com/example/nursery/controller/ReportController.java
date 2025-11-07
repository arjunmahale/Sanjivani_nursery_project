package com.example.nursery.controller;

import com.example.nursery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;

@Controller
public class ReportController {
    private final OrderService orderService;

    public ReportController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping("/reports")
    public String reports(Model model) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        model.addAttribute("dailyOrders", orderService.findOrdersBetween(dayStart, dayEnd));

        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstOfMonth.atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        model.addAttribute("monthlyOrders", orderService.findOrdersBetween(monthStart, monthEnd));

        model.addAttribute("topProducts", orderService.topSelling(monthStart, monthEnd));
        return "reports";
    }
}