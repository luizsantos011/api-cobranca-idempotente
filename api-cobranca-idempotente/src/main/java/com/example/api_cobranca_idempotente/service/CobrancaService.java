package com.example.api_cobranca_idempotente.service;

import com.example.api_cobranca_idempotente.dto.CobrancaRequestDTO;
import com.example.api_cobranca_idempotente.dto.CobrancaResponseDTO;
import com.example.api_cobranca_idempotente.enums.Status;
import com.example.api_cobranca_idempotente.model.Cobranca;
import com.example.api_cobranca_idempotente.repository.CobrancaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CobrancaService {
    private final CobrancaRepository repository;

    public CobrancaService(CobrancaRepository repository) {
        this.repository = repository;
    }

    public CobrancaResponseDTO processarCobranca(CobrancaRequestDTO request) {
        Optional<Cobranca> existente = repository.findByIdempotencyKey(request.idempotencyKey());
        if(existente.isPresent()) {
            return toResponse(existente.get());
        }

        Cobranca novaCobranca = toEntity(request);
        try{
            repository.save(novaCobranca);
        }catch (DataIntegrityViolationException e) {
            Cobranca duplicata = repository.findByIdempotencyKey(request.idempotencyKey())
                    .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada"));
            return toResponse(duplicata);
        }
        return toResponse(novaCobranca);
    }

    private CobrancaResponseDTO toResponse(Cobranca c) {
        return new CobrancaResponseDTO(c.getId(), c.getIdempotencyKey(), c.getClienteId(), c.getValor(), c.getStatus(), c.getCriadoEm());
    }

    private Cobranca toEntity(CobrancaRequestDTO r) {
        return new Cobranca(r.idempotencyKey(), r.clienteId(), r.valor(), Status.APROVADA);
    }
}
