package com.example.nursery.service;

import com.example.nursery.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SchedulerService {

    private final OrderService orderService;
    private final NotificationService notificationService;

    @Value("${nursery.reminder.daysBefore:2}")
    private int daysBefore;

    public SchedulerService(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    // runs every day at 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDeliveryReminders() {
        LocalDate target = LocalDate.now().plusDays(daysBefore);
        List<Order> orders = orderService.findByDeliveryDate(target);
        for (Order o : orders) {
            try {
                notificationService.sendSms(o.getCustomer() != null ? o.getCustomer().getPhone() : null,
                        "Reminder: your order #" + o.getId() + " will be delivered on " + o.getDeliveryDate());
                // optionally email as well
            } catch (Exception ex) {
                System.err.println("Reminder failed for order " + o.getId() + ": " + ex.getMessage());
            }
        }
    }
}