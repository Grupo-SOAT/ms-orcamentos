package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ServiceOrderRepository extends Repository<ServiceOrderEntity, Long> {

    Optional<ServiceOrderEntity> findById(Long id);
}
