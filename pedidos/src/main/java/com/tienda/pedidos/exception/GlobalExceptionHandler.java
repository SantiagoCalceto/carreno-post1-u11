package com.tienda.pedidos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

// Manejo centralizado de errores: reemplaza el try/catch + println + "return null" del codigo de partida.
// El controller no captura nada; cada excepcion se traduce aqui a un codigo HTTP y un ApiError.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return new ApiError(404, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        log.warn("Datos de pedido invalidos en {}: {}", req.getRequestURI(), errores);
        return new ApiError(400, "Bad Request", errores, req.getRequestURI());
    }

    // JSON mal formado o id no numerico: son errores del cliente (400), no del servidor (500)
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSolicitudMalFormada(Exception ex, HttpServletRequest req) {
        log.warn("Solicitud mal formada en {}: {}", req.getRequestURI(), ex.getClass().getSimpleName());
        return new ApiError(400, "Bad Request", "La solicitud tiene un formato invalido.", req.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMetodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return new ApiError(405, "Method Not Allowed", ex.getMessage(), req.getRequestURI());
    }

    // Ruta inexistente (Spring 6.1+ lanza NoResourceFoundException): 404, no 500
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRutaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return new ApiError(404, "Not Found", "La ruta solicitada no existe.", req.getRequestURI());
    }

    // Cualquier otro error: 500 con un mensaje generico al cliente y el detalle completo
    // (con stack trace) SOLO en el log, nunca en la respuesta.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return new ApiError(500, "Internal Server Error",
                "Error inesperado. Contactar soporte.", req.getRequestURI());
    }
}
