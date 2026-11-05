package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

public class MissingSupplyPriceException extends RuntimeException {

    public MissingSupplyPriceException(Long supplyId) {
        super("Missing price for supply: " + supplyId);
    }
}
