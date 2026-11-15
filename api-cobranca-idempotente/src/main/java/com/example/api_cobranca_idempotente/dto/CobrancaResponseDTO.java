package com.example.api_cobranca_idempotente.dto;

import com.example.api_cobranca_idempotente.enums.Status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CobrancaResponseDTO(
        UUID uuid,
        String idempotencyKey,
        String clienteId,
        BigDecimal valor,
        Status status,
        OffsetDateTime criadoEm
) {
}
