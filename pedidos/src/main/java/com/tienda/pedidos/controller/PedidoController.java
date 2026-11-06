package com.tienda.pedidos.controller;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.exception.ApiError;
import com.tienda.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// El controller solo traduce HTTP <-> DTO y delega en la abstraccion PedidoService (SRP + DIP)
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Creacion y consulta de pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(summary = "Crear un nuevo pedido",
               description = "Calcula el descuento segun tipoCliente (REGULAR 0%, VIP 15%, MAYORISTA 25%), "
                           + "el IVA del 19% sobre la base con descuento y el total.")
    @ApiResponse(responseCode = "201", description = "Pedido creado, con descuento aplicado segun tipoCliente",
                 content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                 content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return service.crear(dto);
    }

    @Operation(summary = "Obtener pedido por ID")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                 content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                 content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "400", description = "El id no es numerico",
                 content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public PedidoResponseDTO obtener(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Long id) {
        return service.buscarPorId(id); // sin try/catch: GlobalExceptionHandler responde 404
    }
}
