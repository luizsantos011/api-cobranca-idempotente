package com.example.api_cobranca_idempotente.controller;

import com.example.api_cobranca_idempotente.dto.CobrancaRequestDTO;
import com.example.api_cobranca_idempotente.dto.CobrancaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cobranca")
public class CobrancaController {

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> criarCobranca(@RequestBody CobrancaRequestDTO request) {
        return ResponseEntity.ok(new CobrancaResponseDTO());
    }
}
