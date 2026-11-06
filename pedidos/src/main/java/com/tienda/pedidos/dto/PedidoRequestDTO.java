package com.tienda.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// DTO de entrada: solo los datos que el cliente puede enviar (sin id, totales, estado ni notasInternas)
@Schema(description = "Datos para crear un pedido")
public class PedidoRequestDTO {

    @Schema(description = "Correo del cliente", example = "ana@correo.com")
    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El email del cliente no tiene un formato valido")
    private String clienteEmail;

    @Schema(description = "Tipo de cliente, determina el descuento aplicado",
            allowableValues = {"REGULAR", "VIP", "MAYORISTA"}, example = "VIP")
    @NotBlank(message = "El tipo de cliente es obligatorio")
    private String tipoCliente;      // REGULAR, VIP o MAYORISTA

    @Schema(description = "Subtotal del pedido antes de descuento e IVA", example = "100000.00")
    @NotNull(message = "El subtotal es obligatorio")
    @Positive(message = "El subtotal debe ser mayor a cero")
    private Double subtotal;

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
