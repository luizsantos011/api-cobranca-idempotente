package com.example.api_cobranca_idempotente;

import com.example.api_cobranca_idempotente.dto.CobrancaRequestDTO;
import com.example.api_cobranca_idempotente.dto.CobrancaResponseDTO;
import com.example.api_cobranca_idempotente.repository.CobrancaRepository;
import com.example.api_cobranca_idempotente.service.CobrancaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CobrancaIntegrationTest {
    @Autowired
    private CobrancaRepository repository;
    @Autowired
    private CobrancaService service;
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void deveGarantirIdempotenciaEmRequisicoesRepetidas() {
        CobrancaRequestDTO request = new CobrancaRequestDTO("key-123", "Cliente-A", new BigDecimal("100.00"));
        CobrancaResponseDTO response1 = service.processarCobranca(request);
        CobrancaResponseDTO response2 = service.processarCobranca(request);
        assertEquals(response1.uuid(), response2.uuid());
        assertEquals(1, repository.count());
    }

    @Test
    void deveTratarConcorrenciaComMesmaChave() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch fim = new CountDownLatch(threads);
        CobrancaRequestDTO request = new CobrancaRequestDTO("key-concorrente", "cliente-B", new BigDecimal("100.00"));
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    largada.await();
                    service.processarCobranca(request);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fim.countDown();
                }
            });
        }
        largada.countDown();
        fim.await();
        assertEquals(1, repository.count());
    }
}
