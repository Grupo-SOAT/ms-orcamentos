package br.com.fiap.postech.msorcamentos.adapter.input.budget.controller;

import br.com.fiap.postech.msorcamentos.config.BudgetExceptionHandler;
import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidOrExpiredBudgetTokenException;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.RegisterBudgetDecisionUseCase;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BudgetDecisionControllerTest {

    @Mock
    private RegisterBudgetDecisionUseCase registerBudgetDecisionUseCase;

    @Mock
    private ServiceOrderReadPort serviceOrderReadPort;

    private MockMvc mockMvc;

    private static final String TOKEN = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        var controller = new BudgetDecisionController(registerBudgetDecisionUseCase, serviceOrderReadPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BudgetExceptionHandler())
                .build();
    }

    @Test
    void should_return_202_for_programmatic_client() throws Exception {
        mockMvc.perform(post("/service-orders/1/budget/APPROVE")
                        .param("token", TOKEN)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        verify(registerBudgetDecisionUseCase).process(1L, TOKEN, BudgetDecision.APPROVE);
    }

    @Test
    void should_redirect_when_browser_form_submits() throws Exception {
        mockMvc.perform(post("/service-orders/1/budget/reject")
                        .param("token", TOKEN)
                        .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isSeeOther())
                .andExpect(header().string(HttpHeaders.LOCATION, "/service-orders/1/budget/confirmation"));

        verify(registerBudgetDecisionUseCase).process(1L, TOKEN, BudgetDecision.REJECT);
    }

    @Test
    void should_return_400_for_unknown_decision() throws Exception {
        mockMvc.perform(post("/service-orders/1/budget/UNKNOWN")
                        .param("token", TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_token_missing() throws Exception {
        mockMvc.perform(post("/service-orders/1/budget/APPROVE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_401_when_token_invalid() throws Exception {
        doThrow(new InvalidOrExpiredBudgetTokenException(1L))
                .when(registerBudgetDecisionUseCase).process(1L, TOKEN, BudgetDecision.APPROVE);

        mockMvc.perform(post("/service-orders/1/budget/APPROVE").param("token", TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_render_decision_page_when_token_valid() throws Exception {
        var summary = new ServiceOrderSummary(1L, "Troca de oleo", BigDecimal.valueOf(250), "Joao", "joao@email.com");
        when(serviceOrderReadPort.findSummaryById(1L)).thenReturn(Optional.of(summary));

        mockMvc.perform(get("/service-orders/1/budget/decision-page").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("budget-decision-page"))
                .andExpect(model().attribute("clientName", "Joao"));
    }

    @Test
    void should_render_invalid_token_page_when_token_invalid() throws Exception {
        doThrow(new InvalidOrExpiredBudgetTokenException(1L))
                .when(registerBudgetDecisionUseCase).validateToken(1L, TOKEN);

        mockMvc.perform(get("/service-orders/1/budget/decision-page").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("budget-invalid-token"));
    }

    @Test
    void should_render_confirmation_page() throws Exception {
        mockMvc.perform(get("/service-orders/1/budget/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("budget-confirmation"));
    }
}
