package br.com.fiap.postech.msorcamentos.adapter.output.email;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailpitBudgetApprovalEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private MailpitBudgetApprovalEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new MailpitBudgetApprovalEmailSender(mailSender, templateEngine);
        ReflectionTestUtils.setField(sender, "from", "orcamentos@oficina.com.br");
        ReflectionTestUtils.setField(sender, "baseUrl", "http://localhost:8081");

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(templateEngine.process(eq("email/budget-approval"), any())).thenReturn("<html>corpo</html>");
    }

    @Test
    void should_send_email_with_rendered_template_and_decision_link() throws Exception {
        var summary = new ServiceOrderSummary(1L, "Troca de oleo", BigDecimal.valueOf(250), "Joao", "joao@email.com");

        sender.send(summary, "some-token");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();
        assertThat(sent.getAllRecipients()).hasSize(1);
        assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("joao@email.com");
        assertThat(sent.getFrom()).isNotNull();
        assertThat(sent.getFrom()[0].toString()).isEqualTo("orcamentos@oficina.com.br");
        assertThat(sent.getSubject()).contains("1");
        assertThat(sent.getContent()).isEqualTo("<html>corpo</html>");
        assertThat(sent.getDataHandler().getContentType()).contains("text/html");
    }

    @Test
    void should_render_template_with_every_budget_variable() {
        var summary = new ServiceOrderSummary(7L, "Revisao completa", BigDecimal.valueOf(1234), "Maria", "maria@email.com");

        sender.send(summary, "token-abc");

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("email/budget-approval"), contextCaptor.capture());

        Context context = (Context) contextCaptor.getValue();
        assertThat(context.getVariable("clientName")).isEqualTo("Maria");
        assertThat(context.getVariable("serviceOrderId")).isEqualTo(7L);
        assertThat(context.getVariable("description")).isEqualTo("Revisao completa");
        assertThat(context.getVariable("estimatedAmount")).isEqualTo(BigDecimal.valueOf(1234));
        assertThat(context.getVariable("decisionLink"))
                .isEqualTo("http://localhost:8081/service-orders/7/budget/decision-page?token=token-abc");
    }

}
