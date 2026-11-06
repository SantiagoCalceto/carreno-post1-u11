package com.tienda.pedidos;

import com.tienda.pedidos.strategy.EstrategiaDescuento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Parte 1 de punta a punta (contexto completo + H2): Strategy cableado por Spring y DTO en la respuesta. */
@SpringBootTest
@AutoConfigureMockMvc
class PedidosApplicationTest {

    @Autowired MockMvc mvc;
    @Autowired Map<String, EstrategiaDescuento> estrategias;

    @Test
    void contexto_registraLasTresEstrategiasPorNombreDeBean() {
        assertThat(estrategias).containsOnlyKeys("REGULAR", "VIP", "MAYORISTA");
    }

    @Test
    void post_clienteVip_retorna201ConDescuentoYSinNotasInternas() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteEmail\":\"ana@correo.com\",\"tipoCliente\":\"VIP\",\"subtotal\":100000}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.descuento").value(15000.0))
           .andExpect(jsonPath("$.iva").value(16150.0))
           .andExpect(jsonPath("$.total").value(101150.0))
           .andExpect(jsonPath("$.estado").value("CREADO"))
           .andExpect(jsonPath("$.notasInternas").doesNotExist())
           .andExpect(content().string(not(containsString("revisar stock"))));
    }

    @Test
    void get_pedidoCreado_retorna200ConElDTO() throws Exception {
        String cuerpo = mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteEmail\":\"luis@correo.com\",\"tipoCliente\":\"MAYORISTA\",\"subtotal\":100000}"))
                .andReturn().getResponse().getContentAsString();
        String id = cuerpo.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/pedidos/" + id))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.descuento").value(25000.0))
           .andExpect(jsonPath("$.notasInternas").doesNotExist());
    }
}
