package br.com.fiap.postech.msorcamentos.adapter.input.budget.controller;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidBudgetDecisionException;
import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidOrExpiredBudgetTokenException;
import br.com.fiap.postech.msorcamentos.domain.budget.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.RegisterBudgetDecisionUseCase;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.UUID;

/**
 * {@code @Controller} (nao {@code @RestController}) de proposito: os endpoints GET
 * devolvem nomes de view Thymeleaf, enquanto o POST devolve {@link ResponseEntity},
 * que e resolvido corretamente por ambos os tipos de controller.
 */
@Controller
@RequiredArgsConstructor
public class BudgetDecisionController {

    private final RegisterBudgetDecisionUseCase registerBudgetDecisionUseCase;
    private final ServiceOrderReadPort serviceOrderReadPort;

    @PostMapping("/service-orders/{id}/budget/{decision}")
    public ResponseEntity<Void> decide(
            @PathVariable Long id,
            @PathVariable String decision,
            @RequestParam UUID token,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept
    ) {
        BudgetDecision parsedDecision = parseDecision(decision);
        registerBudgetDecisionUseCase.process(id, token.toString(), parsedDecision);

        if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header(HttpHeaders.LOCATION, "/service-orders/" + id + "/budget/confirmation")
                    .build();
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/service-orders/{id}/budget/decision-page", produces = MediaType.TEXT_HTML_VALUE)
    public String decisionPage(@PathVariable Long id, @RequestParam UUID token, Model model) {
        try {
            registerBudgetDecisionUseCase.validateToken(id, token.toString());
        } catch (InvalidOrExpiredBudgetTokenException e) {
            return "budget-invalid-token";
        }

        var summary = serviceOrderReadPort.findSummaryById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));

        model.addAttribute("serviceOrderId", summary.serviceOrderId());
        model.addAttribute("clientName", summary.clientName());
        model.addAttribute("description", summary.description());
        model.addAttribute("estimatedAmount", summary.estimatedAmount());
        model.addAttribute("token", token);
        return "budget-decision-page";
    }

    @GetMapping(value = "/service-orders/{id}/budget/confirmation", produces = MediaType.TEXT_HTML_VALUE)
    public String confirmation() {
        return "budget-confirmation";
    }

    private BudgetDecision parseDecision(String decision) {
        try {
            return BudgetDecision.valueOf(decision.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidBudgetDecisionException(decision);
        }
    }
}
