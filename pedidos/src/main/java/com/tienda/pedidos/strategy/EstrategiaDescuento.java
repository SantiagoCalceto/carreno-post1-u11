package com.tienda.pedidos.strategy;

// Patron Strategy: cada tipo de cliente encapsula su propia regla de descuento.
// Agregar un tipo nuevo (p. ej. CORPORATIVO) = crear una clase @Component("CORPORATIVO"),
// sin modificar PedidoServiceImpl (principio abierto/cerrado).
public interface EstrategiaDescuento {

    double calcularDescuento(double subtotal);
}
