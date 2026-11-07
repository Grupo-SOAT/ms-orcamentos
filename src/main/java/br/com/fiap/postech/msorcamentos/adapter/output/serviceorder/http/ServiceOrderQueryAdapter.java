package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.http;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Reads the service order summary from the owning services over HTTP.
 *
 * <p>The service order data comes from {@code ms-pedidos}; the client name and e-mail come from
 * the registration service ({@code mnl}). This adapter never reads another service's database.</p>
 */
@Component
public class ServiceOrderQueryAdapter implements ServiceOrderReadPort {

    private final RestClient serviceOrderClient;
    private final RestClient registrationClient;

    public ServiceOrderQueryAdapter(
            RestClient.Builder builder,
            @Value("${app.service-order.base-url:http://localhost:8082}") String serviceOrderBaseUrl,
            @Value("${app.registration.base-url:http://localhost:8080}") String registrationBaseUrl
    ) {
        this.serviceOrderClient = builder.clone().baseUrl(serviceOrderBaseUrl).build();
        this.registrationClient = builder.clone().baseUrl(registrationBaseUrl).build();
    }

    @Override
    public Optional<ServiceOrderSummary> findSummaryById(Long serviceOrderId) {
        Optional<ServiceOrderResponse> serviceOrder = get(serviceOrderClient,
                "/service-orders/{id}", serviceOrderId, ServiceOrderResponse.class);
        if (serviceOrder.isEmpty()) {
            return Optional.empty();
        }

        Optional<OwnerResponse> owner = get(registrationClient,
                "/owners/{id}", serviceOrder.get().clientId(), OwnerResponse.class);
        return owner.map(ownerData -> new ServiceOrderSummary(
                serviceOrder.get().id(),
                serviceOrder.get().description(),
                serviceOrder.get().estimatedAmount(),
                ownerData.name(),
                ownerData.email()
        ));
    }

    private <T> Optional<T> get(RestClient client, String path, Long id, Class<T> responseType) {
        try {
            return Optional.ofNullable(client.get().uri(path, id).retrieve().body(responseType));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw exception;
        } catch (RestClientException exception) {
            throw new IllegalStateException("Cannot query " + path + " for id " + id, exception);
        }
    }

    public record ServiceOrderResponse(Long id, Long clientId, String description, BigDecimal estimatedAmount) {
    }

    public record OwnerResponse(Long id, String name, String email) {
    }
}
