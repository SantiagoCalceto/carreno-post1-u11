package com.tienda.pedidos.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstrategiaDescuentoTest {

    @Test
    void regular_calcularDescuento_retornaCero() {
        assertThat(new DescuentoRegular().calcularDescuento(100000)).isEqualTo(0.0);
    }

    @Test
    void vip_calcularDescuento_retorna15PorCiento() {
        assertThat(new DescuentoVip().calcularDescuento(100000)).isEqualTo(15000.0);
    }

    @Test
    void mayorista_calcularDescuento_retorna25PorCiento() {
        assertThat(new DescuentoMayorista().calcularDescuento(100000)).isEqualTo(25000.0);
    }
}
