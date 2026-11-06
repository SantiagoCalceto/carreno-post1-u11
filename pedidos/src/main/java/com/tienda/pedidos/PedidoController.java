package com.tienda.pedidos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// CODIGO DE PARTIDA (a refactorizar en la Parte 1). Violaciones:
//  - SRP: crearPedido valida, calcula, arma SQL, persiste, notifica y registra en un solo bloque
//  - DIP: NotificacionEmailService se crea con "new"
//  - SQL embebido y concatenado en el controller (inyeccion SQL)
//  - Devuelve la entidad completa (expone notasInternas)
//  - try/catch que silencia el error y devuelve null (200 vacio en vez de 404)
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        // 1. Validar manualmente, sin Bean Validation
        if (pedido.getClienteEmail() == null || pedido.getClienteEmail().isBlank()) {
            throw new RuntimeException("El email del cliente es obligatorio");
        }
        if (pedido.getSubtotal() == null || pedido.getSubtotal() <= 0) {
            throw new RuntimeException("El subtotal debe ser mayor a cero");
        }

        // 2. Calcular el descuento segun tipo de cliente (logica de negocio en el controller)
        double descuento;
        if ("VIP".equalsIgnoreCase(pedido.getTipoCliente())) {
            descuento = pedido.getSubtotal() * 0.15;
        } else if ("MAYORISTA".equalsIgnoreCase(pedido.getTipoCliente())) {
            descuento = pedido.getSubtotal() * 0.25;
        } else {
            descuento = 0.0;
        }
        double baseConDescuento = pedido.getSubtotal() - descuento;
        double iva = baseConDescuento * 0.19;
        double total = baseConDescuento + iva;

        pedido.setDescuento(descuento);
        pedido.setIva(iva);
        pedido.setTotal(total);
        pedido.setEstado("CREADO");
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setNotasInternas("Pedido creado automaticamente, revisar stock antes de despachar");

        // 3. Persistir con SQL embebido y concatenado directamente en el controller
        String sql = "INSERT INTO pedido (cliente_email, tipo_cliente, subtotal, descuento, iva, total, "
                    + "estado, notas_internas, fecha_creacion) VALUES ('" + pedido.getClienteEmail() + "', '"
                    + pedido.getTipoCliente() + "', " + pedido.getSubtotal() + ", " + descuento + ", " + iva
                    + ", " + total + ", 'CREADO', '" + pedido.getNotasInternas() + "', NOW())";
        jdbcTemplate.update(sql);

        // 4. Enviar correo de confirmacion — dependencia creada con "new", no inyectada
        NotificacionEmailService notificador = new NotificacionEmailService();
        notificador.enviar(pedido.getClienteEmail(), "Tu pedido fue creado. Total: " + total);

        // 5. Loggear con println
        System.out.println("Pedido creado para " + pedido.getClienteEmail());

        // 6. Devolver la ENTIDAD directamente como respuesta JSON (incluye notasInternas)
        return pedido;
    }

    @GetMapping("/{id}")
    public Pedido obtenerPedido(@PathVariable Long id) {
        try {
            String sql = "SELECT * FROM pedido WHERE id = " + id;
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Pedido.class));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null; // el cliente recibe 200 OK con cuerpo vacio en vez de 404
        }
    }
}
