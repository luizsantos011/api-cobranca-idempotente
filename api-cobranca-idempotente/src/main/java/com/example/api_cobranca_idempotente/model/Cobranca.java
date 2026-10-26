package com.example.api_cobranca_idempotente.model;

import com.example.api_cobranca_idempotente.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_cobranca")
@Getter
@Setter
public class Cobranca {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    @Column(nullable = false)
    private String clienteId;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private OffsetDateTime criadoEm;


    protected Cobranca() {}

    public Cobranca(String idempotencyKey, String clienteId, BigDecimal valor, Status status) {
        this.idempotencyKey = idempotencyKey;
        this.clienteId = clienteId;
        this.valor = valor;
        this.status = status;
    }

    @PrePersist
    protected void prepersist() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
        if (criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
