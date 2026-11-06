package com.tienda.pedidos;

// CODIGO DE PARTIDA: clase concreta que el controller instancia con "new" (viola DIP)
public class NotificacionEmailService {

    public void enviar(String destinatario, String mensaje) {
        // simulacion de envio de correo
        System.out.println("Correo enviado a " + destinatario + ": " + mensaje);
    }
}
