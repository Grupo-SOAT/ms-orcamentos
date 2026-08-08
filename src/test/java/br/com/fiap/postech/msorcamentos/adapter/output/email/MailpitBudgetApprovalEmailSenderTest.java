package br.com.fiap.postech.msorcamentos.adapter.output.email;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import jakarta.mail.MessagingException;
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
    void should_send_email_with_rendered_template_and_decision_link() throws MessagingException {
        var summary = new ServiceOrderSummary(1L, "Troca de oleo", BigDecimal.valueOf(250), "Joao", "joao@email.com");

        sender.send(summary, "some-token");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();
        assertThat(sent.getAllRecipients()).hasSize(1);
        assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("joao@email.com");
        assertThat(sent.getSubject()).contains("1");
    }
}
