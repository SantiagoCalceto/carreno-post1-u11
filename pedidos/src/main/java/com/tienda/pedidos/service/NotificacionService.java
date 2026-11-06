package com.tienda.pedidos.service;

// Abstraccion de la notificacion: el servicio de pedidos la recibe por constructor
// (en pruebas se reemplaza por un mock; en el codigo de partida se creaba con "new")
public interface NotificacionService {

    void enviarConfirmacion(String clienteEmail, double total);
}
