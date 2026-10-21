package com.micarch.saga.orchestration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** A small deterministic orchestrator showing forward and compensation paths. */
@RestController
@RequestMapping("/api/sagas")
public class SagaController {
    @PostMapping
    public ResponseEntity<SagaResult> start(@RequestBody StartSaga request) {
        String sagaId = UUID.randomUUID().toString();
        if (request.quantity() <= 0) {
            return ResponseEntity.unprocessableEntity().body(new SagaResult(sagaId, "REJECTED", "validate-order"));
        }
        if (request.paymentAmount() <= 0) {
            return ResponseEntity.ok(new SagaResult(sagaId, "COMPENSATED", "release-stock"));
        }
        return ResponseEntity.accepted().body(new SagaResult(sagaId, "COMPLETED", "notify-customer"));
    }

    public record StartSaga(String orderId, int quantity, double paymentAmount) { }
    public record SagaResult(String sagaId, String state, String nextStep) { }
}
