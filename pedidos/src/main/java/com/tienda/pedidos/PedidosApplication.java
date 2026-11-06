package com.tienda.pedidos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
    info = @Info(
        title = "API de Pedidos",
        version = "1.0",
        description = "API REST para la gestion de pedidos de la tienda: creacion con descuento "
                    + "segun el tipo de cliente (REGULAR 0%, VIP 15%, MAYORISTA 25%) e IVA del 19%, "
                    + "y consulta por id."
    )
)
@SpringBootApplication
public class PedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
