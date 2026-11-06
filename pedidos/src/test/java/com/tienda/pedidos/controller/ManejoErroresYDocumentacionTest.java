package com.tienda.pedidos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Parte 2: GlobalExceptionHandler (ApiError 404/400) y documentacion springdoc-openapi. */
@SpringBootTest
@AutoConfigureMockMvc
class ManejoErroresYDocumentacionTest {

    @Autowired MockMvc mvc;

    @Test
    void get_idInexistente_retorna404ConApiError() throws Exception {
        mvc.perform(get("/api/pedidos/999"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404))
           .andExpect(jsonPath("$.error").value("Not Found"))
           .andExpect(jsonPath("$.mensaje").value("Pedido con id 999 no encontrado."))
           .andExpect(jsonPath("$.path").value("/api/pedidos/999"))
           .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void post_cuerpoVacio_retorna400ConLosErroresDeValidacion() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.mensaje", containsString("clienteEmail: El email del cliente es obligatorio")))
           .andExpect(jsonPath("$.mensaje", containsString("tipoCliente: El tipo de cliente es obligatorio")))
           .andExpect(jsonPath("$.mensaje", containsString("subtotal: El subtotal es obligatorio")));
    }

    @Test
    void post_subtotalNegativoYEmailInvalido_retorna400() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteEmail\":\"no-es-correo\",\"tipoCliente\":\"VIP\",\"subtotal\":-5}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.mensaje", containsString("subtotal: El subtotal debe ser mayor a cero")))
           .andExpect(jsonPath("$.mensaje", containsString("clienteEmail: El email del cliente no tiene un formato valido")));
    }

    @Test
    void post_jsonMalFormado_retorna400NoUn500() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("{\"subtotal\": "))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void get_idNoNumerico_retorna400() throws Exception {
        mvc.perform(get("/api/pedidos/abc"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void apiDocs_documentaLosEndpointsConSusRespuestas() throws Exception {
        mvc.perform(get("/api-docs"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.info.title").value("API de Pedidos"))
           .andExpect(jsonPath("$.tags[*].name", hasItem("Pedidos")))
           .andExpect(jsonPath("$.paths['/api/pedidos'].post.responses", hasKey("201")))
           .andExpect(jsonPath("$.paths['/api/pedidos'].post.responses", hasKey("400")))
           .andExpect(jsonPath("$.paths['/api/pedidos/{id}'].get.responses", hasKey("404")))
           .andExpect(jsonPath("$.components.schemas.PedidoRequestDTO.properties.tipoCliente.enum",
                   containsInAnyOrder("REGULAR", "VIP", "MAYORISTA")))
           .andExpect(jsonPath("$.components.schemas.PedidoResponseDTO.properties", not(hasKey("notasInternas"))));
    }

    @Test
    void swaggerUi_estaDisponible() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
    }
}
