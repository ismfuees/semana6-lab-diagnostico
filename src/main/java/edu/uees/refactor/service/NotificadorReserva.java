package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/**
 * Responsabilidad única: comunicar los efectos secundarios de una reserva confirmada.
 *
 * Encapsula la simulación de persistencia y notificación que estaban
 * como println directos en ServicioReservas. Al vivir en una clase separada,
 * puede reemplazarse por una implementación real (repositorio, email)
 * sin abrir ServicioReservas.
 */
public class NotificadorReserva {

    public void notificar(Reserva r) {
        System.out.println("Guardando reserva " + r.getId());
        System.out.println("Correo enviado a " + r.getCorreo());
    }
}
