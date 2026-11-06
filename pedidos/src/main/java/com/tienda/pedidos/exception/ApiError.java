package com.tienda.pedidos.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Cuerpo uniforme de todas las respuestas de error de la API
@Schema(description = "Respuesta de error de la API")
@JsonPropertyOrder({"status", "error", "mensaje", "timestamp", "path"})
public class ApiError {

    @Schema(example = "404")
    private final int status;
    @Schema(example = "Not Found")
    private final String error;
    @Schema(example = "Pedido con id 999 no encontrado.")
    private final String mensaje;
    @Schema(example = "2026-11-06T10:15:30")
    private final String timestamp;
    @Schema(example = "/api/pedidos/999")
    private final String path;

    public ApiError(int status, String error, String mensaje, String path) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
        this.timestamp = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMensaje() { return mensaje; }
    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
