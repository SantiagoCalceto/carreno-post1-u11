package com.tienda.pedidos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacionEmailServiceImpl implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEmailServiceImpl.class);

    @Override
    public void enviarConfirmacion(String clienteEmail, double total) {
        // simulacion de envio de correo (fuera del alcance de este laboratorio)
        log.info("Correo de confirmacion enviado: clienteEmail={}, total={}", clienteEmail, total);
    }
}
