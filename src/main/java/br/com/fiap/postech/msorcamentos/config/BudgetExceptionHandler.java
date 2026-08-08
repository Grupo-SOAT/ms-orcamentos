package br.com.fiap.postech.msorcamentos.config;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidBudgetDecisionException;
import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidOrExpiredBudgetTokenException;
import br.com.fiap.postech.msorcamentos.domain.budget.exception.ServiceOrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class BudgetExceptionHandler {

    @ExceptionHandler(InvalidOrExpiredBudgetTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidOrExpiredBudgetTokenException e) {
        return errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(InvalidBudgetDecisionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDecision(InvalidBudgetDecisionException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleServiceOrderNotFound(ServiceOrderNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message
        ));
    }
}
