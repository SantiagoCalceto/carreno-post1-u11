package com.tienda.pedidos.strategy;

import org.springframework.stereotype.Component;

// Cliente REGULAR: Sin descuento. El nombre del bean ("REGULAR") es la llave en el Map del servicio.
@Component("REGULAR")
public class DescuentoRegular implements EstrategiaDescuento {

    @Override
    public double calcularDescuento(double subtotal) {
        return 0.0;
    }
}
