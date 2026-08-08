package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.persistence.entity.OwnerEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface OwnerRepository extends Repository<OwnerEntity, Long> {

    Optional<OwnerEntity> findById(Long id);
}
