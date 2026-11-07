package br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Base envelope for events consumed by this service.
 *
 * <p>The {@code type} field is a plain string. Unknown types are deserialized as
 * {@link UnknownEvent} and acknowledged, because the topic carries events from several producers.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = UnknownEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BudgetRequestedEvent.class, name = EventTypes.BUDGET_REQUESTED)
})
public abstract class EventEnvelope {

    private String eventId;
    private String type;
    private Instant occurredAt;
    private String correlationId;
    private Long serviceOrderId;
}
