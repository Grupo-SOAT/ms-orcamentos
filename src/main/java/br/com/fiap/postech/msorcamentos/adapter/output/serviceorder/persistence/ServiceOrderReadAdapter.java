package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository.OwnerRepository;
import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServiceOrderReadAdapter implements ServiceOrderReadPort {

    private final ServiceOrderRepository serviceOrderRepository;
    private final OwnerRepository ownerRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceOrderSummary> findSummaryById(Long serviceOrderId) {
        return serviceOrderRepository.findById(serviceOrderId)
                .flatMap(serviceOrder -> ownerRepository.findById(serviceOrder.getClientId())
                        .map(owner -> new ServiceOrderSummary(
                                serviceOrder.getId(),
                                serviceOrder.getDescription(),
                                serviceOrder.getEstimatedAmount(),
                                owner.getName(),
                                owner.getEmail()
                        )));
    }
}
