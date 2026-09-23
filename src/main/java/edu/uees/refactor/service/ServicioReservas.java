package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.domain.TipoReserva;

public class ServicioReservas {

    private final ValidadorReserva validador = new ValidadorReserva();
    private final NotificadorReserva notificador = new NotificadorReserva();

    public double procesar(
            Reserva r,
            int horasAnticipacion) {

        if (!validador.esValida(r, horasAnticipacion)) {
            return 0;
        }

        double total = calcularTotal(r);

        notificador.notificar(r);
        r.confirmar();

        return total;
    }

    private double calcularTotal(Reserva r) {
        double total = 40;
        if (TipoReserva.VIP == r.getTipo()) {
            return total * 0.85;
        }
        return total;
    }
}
