package dev.challenge.serviceb.controllers;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.serviceb.services.OrderService;
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
@RequestMapping("/pedidos")
@Tag(name = "Pedidos (Service B)", description = "CRUD de pedidos do Service B (replicação)")
public class OrdersController {

    private final OrderService service;

    @GetMapping
    @Operation(summary = "Listar pedidos (B)", description = "Lista pedidos replicados no Service B")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<List<OrderReplicaDTO>> listOrders() {
        return ResponseEntity.ok(service.listOrders());
    }

    @PostMapping
    @Operation(summary = "Criar pedido (B)", description = "Cria pedido (replicação do Service A)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "409", description = "Conflito"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OrderReplicaDTO> createOrder(@RequestBody OrderReplicaDTO dto) {
        return ResponseEntity.status(201).body(service.createOrder(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pedido (B)", description = "Atualiza pedido replicado no Service B")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OrderReplicaDTO> updateOrder(@PathVariable("id") String id, @RequestBody OrderReplicaDTO dto) {
        return ResponseEntity.ok(service.updateOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover pedido (B)", description = "Remove pedido replicado no Service B")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") String id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
