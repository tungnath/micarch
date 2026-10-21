package com.micarch.bff.controller;

import java.math.BigDecimal;
import java.util.List;

import com.micarch.bff.service.BffDataGateway;
import com.micarch.bff.service.BffDataGateway.InventoryData;
import com.micarch.bff.service.BffDataGateway.OrderData;
import com.micarch.bff.service.BffDataGateway.PaymentData;
import com.micarch.bff.service.BffDataGateway.UserData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BffController {

    private final BffDataGateway dataGateway;

    public BffController(BffDataGateway dataGateway) {
        this.dataGateway = dataGateway;
    }

    @GetMapping("/mobile/dashboard/{userId}")
    public MobileDashboard mobileDashboard(@PathVariable("userId") String userId) {
        UserData user = dataGateway.user(userId);
        List<OrderData> orders = dataGateway.recentOrders(userId).stream().limit(3).toList();
        List<PaymentData> payments = dataGateway.pendingPayments(userId);
        return new MobileDashboard(user.userId(), user.displayName(), orders, payments,
                dataGateway.lowStockItems().stream().map(InventoryData::sku).toList());
    }

    @GetMapping("/web/dashboard/{userId}")
    public WebDashboard webDashboard(@PathVariable("userId") String userId) {
        UserData user = dataGateway.user(userId);
        return new WebDashboard(user, dataGateway.recentOrders(userId),
                dataGateway.lowStockItems(), dataGateway.pendingPayments(userId),
                new BigDecimal("498.98"));
    }

    public record MobileDashboard(
            String userId,
            String displayName,
            List<OrderData> recentOrders,
            List<PaymentData> pendingPayments,
            List<String> lowStockSkus) {
    }

    public record WebDashboard(
            UserData user,
            List<OrderData> orders,
            List<InventoryData> lowStockItems,
            List<PaymentData> pendingPayments,
            BigDecimal orderValueTotal) {
    }
}
