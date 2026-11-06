package com.tienda.pedidos;

import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// REFACTORIZACION EN CURSO (paso 2 de 4): el SQL concatenado con JdbcTemplate se reemplazo por
// PedidoRepository (JpaRepository). Todavia quedan pendientes: SRP (la logica sigue en el controller),
// DIP ("new" de la notificacion), la entidad expuesta en la respuesta y el try/catch que devuelve null.
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @PostMapping
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        if (pedido.getClienteEmail() == null || pedido.getClienteEmail().isBlank()) {
            throw new RuntimeException("El email del cliente es obligatorio");
        }
        if (pedido.getSubtotal() == null || pedido.getSubtotal() <= 0) {
            throw new RuntimeException("El subtotal debe ser mayor a cero");
        }

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

        // Sin SQL escrito a mano: JPA genera un INSERT con parametros
        Pedido guardado = pedidoRepository.save(pedido);

        NotificacionEmailService notificador = new NotificacionEmailService();
        notificador.enviar(guardado.getClienteEmail(), "Tu pedido fue creado. Total: " + total);

        System.out.println("Pedido creado para " + guardado.getClienteEmail());
        return guardado;
    }

    @GetMapping("/{id}")
    public Pedido obtenerPedido(@PathVariable Long id) {
        try {
            return pedidoRepository.findById(id).orElseThrow();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}
