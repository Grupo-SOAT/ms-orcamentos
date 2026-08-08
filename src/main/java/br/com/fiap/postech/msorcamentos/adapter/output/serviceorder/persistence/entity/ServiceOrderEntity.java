package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Espelho somente-leitura da tabela {@code service_orders} do monolito.
 * Traz apenas as colunas necessarias para montar o e-mail de orcamento.
 */
@Entity
@Table(name = "service_orders")
@Getter
@Setter
@NoArgsConstructor
public class ServiceOrderEntity {

    @Id
    @Column(name = "service_order_id")
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "description")
    private String description;

    @Column(name = "estimated_amount")
    private BigDecimal estimatedAmount;
}
