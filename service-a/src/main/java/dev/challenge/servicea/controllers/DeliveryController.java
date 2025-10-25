package dev.challenge.servicea.controllers;

import dev.challenge.common.dto.DeliveryDTO;
import dev.challenge.servicea.services.DeliveryService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/entregadores")
@Tag(name = "Entregadores (Service A)", description = "CRUD de entregadores do Service A")
public class DeliveryController {

    private final DeliveryService service;

    @PostMapping
    @Operation(summary = "Criar entregador", description = "Cria um novo entregador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "409", description = "Conflito"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<DeliveryDTO> createDelivery(@RequestBody DeliveryDTO dto) {
        return ResponseEntity.status(201).body(service.createDelivery(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar entregador", description = "Atualiza um entregador existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable("id") Long id, @RequestBody DeliveryDTO dto) {
        return ResponseEntity.ok(service.updateDelivery(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover entregador", description = "Remove um entregador pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> deleteDelivery(@PathVariable("id") Long id) {
        service.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar entregadores", description = "Lista todos os entregadores")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<List<DeliveryDTO>> listDeliverys() {
        return ResponseEntity.ok(service.listDeliveries());
    }
}
