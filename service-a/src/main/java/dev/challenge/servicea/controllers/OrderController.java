package dev.challenge.servicea.controllers;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.servicea.services.OrderService;
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
@Tag(name = "Pedidos (Service A)", description = "CRUD de pedidos do Service A")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "409", description = "Conflito"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO dto) {
        return ResponseEntity.status(201).body(service.createOrder(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pedido", description = "Atualiza um pedido existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable("id") String id, @RequestBody OrderDTO dto) {
        return ResponseEntity.ok(service.updateOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover pedido", description = "Remove um pedido pelo ID")
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

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Lista todos os pedidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<List<OrderDTO>> listOrders() {
        return ResponseEntity.ok(service.listOrders());
    }
}
