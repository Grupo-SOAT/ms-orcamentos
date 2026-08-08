package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Espelho somente-leitura da tabela {@code owners} do monolito.
 * Traz apenas as colunas necessarias para montar o e-mail de orcamento.
 */
@Entity
@Table(name = "owners")
@Getter
@Setter
@NoArgsConstructor
public class OwnerEntity {

    @Id
    @Column(name = "owner_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}
