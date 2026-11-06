package com.tienda.pedidos.strategy;

import org.springframework.stereotype.Component;

// Cliente VIP: 15 % de descuento. El nombre del bean ("VIP") es la llave en el Map del servicio.
@Component("VIP")
public class DescuentoVip implements EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal) {
        return subtotal * 0.15;
    }
}
