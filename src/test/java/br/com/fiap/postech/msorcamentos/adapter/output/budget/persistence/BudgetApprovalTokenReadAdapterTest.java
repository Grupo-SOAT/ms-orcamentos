package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetApprovalTokenEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository.BudgetApprovalTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class BudgetApprovalTokenReadAdapterTest {

    @Mock
    private BudgetApprovalTokenRepository repository;

    @InjectMocks
    private BudgetApprovalTokenReadAdapter adapter;

    @Test
    void should_map_entity_to_domain_view() {
        var expiresAt = Instant.now().plusSeconds(3600);
        var entity = new BudgetApprovalTokenEntity();
        entity.setId(1L);
        entity.setServiceOrderId(10L);
        entity.setToken("abc-123");
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(Instant.now());
        entity.setUsedAt(null);

        when(repository.findByServiceOrderIdAndToken(10L, "abc-123")).thenReturn(Optional.of(entity));

        var result = adapter.findToken(10L, "abc-123");

        assertThat(result).isPresent();
        assertThat(result.get().serviceOrderId()).isEqualTo(10L);
        assertThat(result.get().token()).isEqualTo("abc-123");
        assertThat(result.get().expiresAt()).isEqualTo(expiresAt);
        assertThat(result.get().usedAt()).isNull();
    }

    @Test
    void should_return_empty_when_token_not_found() {
        when(repository.findByServiceOrderIdAndToken(10L, "missing")).thenReturn(Optional.empty());

        assertThat(adapter.findToken(10L, "missing")).isEmpty();
    }
}
