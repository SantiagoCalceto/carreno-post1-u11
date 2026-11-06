package com.tienda.pedidos.strategy;

import org.springframework.stereotype.Component;

// Cliente MAYORISTA: 25 % de descuento. El nombre del bean ("MAYORISTA") es la llave en el Map del servicio.
@Component("MAYORISTA")
public class DescuentoMayorista implements EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal) {
        return subtotal * 0.25;
    }
}
