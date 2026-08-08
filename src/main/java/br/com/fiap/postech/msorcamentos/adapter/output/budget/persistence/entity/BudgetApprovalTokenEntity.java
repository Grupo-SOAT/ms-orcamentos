package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Espelho somente-leitura da tabela {@code budget_approval_tokens}, cuja escrita
 * pertence ao monolito. Este servico nunca grava nesta entidade.
 */
@Entity
@Table(name = "budget_approval_tokens")
@Getter
@Setter
@NoArgsConstructor
public class BudgetApprovalTokenEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "service_order_id", nullable = false)
    private Long serviceOrderId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;
}
