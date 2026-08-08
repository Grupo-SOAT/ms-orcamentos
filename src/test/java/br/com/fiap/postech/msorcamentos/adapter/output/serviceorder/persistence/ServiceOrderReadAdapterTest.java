package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity.OwnerEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository.OwnerRepository;
import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServiceOrderReadAdapterTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private ServiceOrderReadAdapter adapter;

    @Test
    void should_join_service_order_and_owner_into_summary() {
        var serviceOrder = new ServiceOrderEntity();
        serviceOrder.setId(1L);
        serviceOrder.setClientId(99L);
        serviceOrder.setDescription("Troca de oleo");
        serviceOrder.setEstimatedAmount(BigDecimal.valueOf(250));

        var owner = new OwnerEntity();
        owner.setId(99L);
        owner.setName("Joao");
        owner.setEmail("joao@email.com");

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(ownerRepository.findById(99L)).thenReturn(Optional.of(owner));

        var result = adapter.findSummaryById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().serviceOrderId()).isEqualTo(1L);
        assertThat(result.get().clientName()).isEqualTo("Joao");
        assertThat(result.get().clientEmail()).isEqualTo("joao@email.com");
        assertThat(result.get().description()).isEqualTo("Troca de oleo");
        assertThat(result.get().estimatedAmount()).isEqualByComparingTo("250");
    }

    @Test
    void should_return_empty_when_service_order_not_found() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(adapter.findSummaryById(1L)).isEmpty();
    }

    @Test
    void should_return_empty_when_owner_not_found() {
        var serviceOrder = new ServiceOrderEntity();
        serviceOrder.setId(1L);
        serviceOrder.setClientId(99L);

        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findSummaryById(1L)).isEmpty();
    }
}
