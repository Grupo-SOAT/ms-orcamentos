package br.com.fiap.postech.msorcamentos.adapter.output.email;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import br.com.fiap.postech.msorcamentos.port.email.BudgetApprovalEmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class MailpitBudgetApprovalEmailSender implements BudgetApprovalEmailSenderPort {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.budget.mail.from}")
    private String from;

    @Value("${app.budget.base-url}")
    private String baseUrl;

    @Override
    public void send(ServiceOrderSummary summary, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setTo(summary.clientEmail());
            helper.setFrom(from);
            helper.setSubject("Orcamento da Ordem de Servico #" + summary.serviceOrderId());
            helper.setText(renderBody(summary, token), true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Falha ao enviar e-mail de aprovacao de orcamento", e);
        }
    }

    private String renderBody(ServiceOrderSummary summary, String token) {
        String decisionLink = baseUrl + "/service-orders/" + summary.serviceOrderId()
                + "/budget/decision-page?token=" + token;

        Context context = new Context();
        context.setVariable("clientName", summary.clientName());
        context.setVariable("serviceOrderId", summary.serviceOrderId());
        context.setVariable("description", summary.description());
        context.setVariable("estimatedAmount", summary.estimatedAmount());
        context.setVariable("decisionLink", decisionLink);

        return templateEngine.process("email/budget-approval", context);
    }
}
