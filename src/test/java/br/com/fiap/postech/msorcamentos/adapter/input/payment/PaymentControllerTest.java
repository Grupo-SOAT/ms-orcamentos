package br.com.fiap.postech.msorcamentos.adapter.input.payment;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.CreatePaymentPreferenceUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.PaymentWebhookUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.RequestRefundUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private final CreatePaymentPreferenceUseCase preferenceUseCase = mock(CreatePaymentPreferenceUseCase.class);
    private final PaymentWebhookUseCase webhookUseCase = mock(PaymentWebhookUseCase.class);
    private final RequestRefundUseCase refundUseCase = mock(RequestRefundUseCase.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentController(preferenceUseCase, webhookUseCase, refundUseCase))
                .setControllerAdvice(new PaymentExceptionHandler())
                .build();
    }

    @Test
    void should_create_checkout_preference() throws Exception {
        when(preferenceUseCase.createOrReuse(3L)).thenReturn(
                new CreatePaymentPreferenceUseCase.PreferenceResult("pref-1", "http://checkout", null));

        mockMvc.perform(post("/payments/budgets/3/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferenceId").value("pref-1"))
                .andExpect(jsonPath("$.initPoint").value("http://checkout"));
    }

    @Test
    void should_return_not_found_when_budget_is_missing() throws Exception {
        when(preferenceUseCase.createOrReuse(9L))
                .thenThrow(new CreatePaymentPreferenceUseCase.BudgetNotFoundException(9L));

        mockMvc.perform(post("/payments/budgets/9/checkout"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_handle_webhook() throws Exception {
        when(webhookUseCase.handle("mp-1", "corr-1"))
                .thenReturn(Payment.restore(1L, 7L, 3L, "pref", "mp-1", PaymentStatus.APPROVED,
                        BigDecimal.TEN, "url", Instant.now(), Instant.now().plusSeconds(60), null, null));

        mockMvc.perform(post("/payments/webhook")
                        .param("correlationId", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"mp-1\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_not_found_when_webhook_payment_is_unknown() throws Exception {
        when(webhookUseCase.handle(eq("mp-1"), any()))
                .thenThrow(new PaymentWebhookUseCase.PaymentNotFoundException(7L));

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"mp-1\"}}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_refund_payment() throws Exception {
        when(refundUseCase.refund(7L, "corr-1")).thenReturn(
                Refund.restore(1L, 10L, 7L, BigDecimal.TEN, "APPROVED", Instant.now()));

        mockMvc.perform(post("/payments/7/refund").param("correlationId", "corr-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(10));
    }

    @Test
    void should_return_conflict_when_refund_is_invalid() throws Exception {
        when(refundUseCase.refund(eq(7L), any())).thenThrow(new IllegalStateException("not approved"));

        mockMvc.perform(post("/payments/7/refund"))
                .andExpect(status().isConflict());
    }
}
