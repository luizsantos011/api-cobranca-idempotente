package com.example.api_cobranca_idempotente.repository;

import com.example.api_cobranca_idempotente.model.Cobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CobrancaRepository extends JpaRepository<Cobranca, UUID> {
    Optional<Cobranca> findByIdempotencyKey(String idempotenceKey);
}
