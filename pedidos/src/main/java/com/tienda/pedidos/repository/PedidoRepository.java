package com.tienda.pedidos.repository;

import com.tienda.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// DAO con Spring Data JPA: reemplaza el SQL concatenado del controller de partida.
// Los metodos CRUD vienen de JpaRepository y las consultas derivadas se generan
// a partir del nombre del metodo (con parametros, nunca concatenacion).
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteEmail(String clienteEmail);
}
