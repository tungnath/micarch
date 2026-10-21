package com.micarch.acl;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acl")
public class AclController {
    @PostMapping("/translate")
    public CleanOrder translate(@RequestBody LegacyOrder legacy) {
        return new CleanOrder(legacy.orderNumber(), legacy.clientCode(), legacy.itemRef(), legacy.unitCount());
    }

    public record LegacyOrder(String orderNumber, String clientCode, String itemRef, int unitCount) { }
    public record CleanOrder(String orderId, String customerId, String productId, int quantity) { }
}
