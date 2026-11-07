package br.com.fiap.postech.msorcamentos.port.catalog;

import br.com.fiap.postech.msorcamentos.domain.budget.model.CatalogPrice;

public interface CatalogQueryPort {

    CatalogPrice findPrice(Long catalogServiceId);
}
