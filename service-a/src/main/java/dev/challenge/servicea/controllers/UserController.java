package dev.challenge.servicea.controllers;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.servicea.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
@Tag(name = "Usuários (Service A)", description = "CRUD de usuários do Service A")
public class UserController {

    private final UserService service;

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "409", description = "Conflito"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO dto) {
        return ResponseEntity.status(201).body(service.createUser(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza um usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") String id, @RequestBody @Valid UserDTO dto) {
        return ResponseEntity.ok(service.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário", description = "Remove um usuário pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<List<UserDTO>> listUsers() {
        return ResponseEntity.ok(service.listUsers());
    }
}
