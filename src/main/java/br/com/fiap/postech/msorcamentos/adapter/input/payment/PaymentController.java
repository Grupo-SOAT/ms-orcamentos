package br.com.fiap.postech.msorcamentos.adapter.input.payment;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.CreatePaymentPreferenceUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.PaymentWebhookUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.RequestRefundUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final CreatePaymentPreferenceUseCase preferenceUseCase;
    private final PaymentWebhookUseCase webhookUseCase;
    private final RequestRefundUseCase refundUseCase;

    public PaymentController(CreatePaymentPreferenceUseCase preferenceUseCase,
                             PaymentWebhookUseCase webhookUseCase,
                             RequestRefundUseCase refundUseCase) {
        this.preferenceUseCase = preferenceUseCase;
        this.webhookUseCase = webhookUseCase;
        this.refundUseCase = refundUseCase;
    }

    @PostMapping("/budgets/{budgetId}/checkout")
    public ResponseEntity<PreferenceResponse> checkout(@PathVariable Long budgetId) {
        var result = preferenceUseCase.createOrReuse(budgetId);
        return ResponseEntity.ok(new PreferenceResponse(result.preferenceId(), result.initPoint()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody WebhookRequest request,
                                        @RequestParam(required = false) String correlationId) {
        webhookUseCase.handle(request.data().id(), correlationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{serviceOrderId}/refund")
    public ResponseEntity<RefundResponse> refund(@PathVariable Long serviceOrderId,
                                                 @RequestParam(required = false) String correlationId) {
        Refund refund = refundUseCase.refund(serviceOrderId, correlationId);
        return ResponseEntity.ok(new RefundResponse(refund.getId(), refund.getPaymentId(),
                refund.getAmount(), refund.getStatus(), refund.getCreatedAt()));
    }

    public record WebhookRequest(String type, WebhookData data) {
    }

    public record WebhookData(String id) {
    }

    public record PreferenceResponse(String preferenceId, String initPoint) {
    }

    public record RefundResponse(Long id, Long paymentId, BigDecimal amount, String status, Instant createdAt) {
    }

    public static PaymentResponse payment(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getServiceOrderId(), payment.getStatus().name(),
                payment.getAmount(), payment.getPreferenceId(), payment.getPaymentId(), payment.getInitPoint());
    }

    public record PaymentResponse(Long id, Long serviceOrderId, String status, BigDecimal amount,
                                  String preferenceId, String paymentId, String initPoint) {
    }
}
