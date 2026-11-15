package com.example.api_cobranca_idempotente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CobrancaRequestDTO(
        @NotBlank(message = "A chave de idempotencia é obrigatória")
        String idempotencyKey,

        @NotBlank(message = "O ID do cliente é obrigatório")
        String clienteId,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor
) {
}
