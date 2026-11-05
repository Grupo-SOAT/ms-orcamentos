package br.com.fiap.postech.msorcamentos.adapter.input.payment;

import br.com.fiap.postech.msorcamentos.domain.payment.model.InvalidPaymentTransitionException;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentDecisionAlreadyUsedException;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.CreatePaymentPreferenceUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.PaymentWebhookUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler({CreatePaymentPreferenceUseCase.BudgetNotFoundException.class,
            PaymentWebhookUseCase.PaymentNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, InvalidPaymentTransitionException.class,
            PaymentDecisionAlreadyUsedException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", String.valueOf(message)
        ));
    }
}
