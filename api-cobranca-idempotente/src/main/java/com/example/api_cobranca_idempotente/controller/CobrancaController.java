package com.example.api_cobranca_idempotente.controller;

import com.example.api_cobranca_idempotente.dto.CobrancaRequestDTO;
import com.example.api_cobranca_idempotente.dto.CobrancaResponseDTO;
import com.example.api_cobranca_idempotente.model.Cobranca;
import com.example.api_cobranca_idempotente.service.CobrancaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cobranca")
public class CobrancaController {
    private final CobrancaService service;

    public CobrancaController(CobrancaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> criarCobranca(@Valid  @RequestBody CobrancaRequestDTO request) {
        CobrancaResponseDTO response = service.processarCobranca(request);
        return ResponseEntity.ok(response);
    }
}
