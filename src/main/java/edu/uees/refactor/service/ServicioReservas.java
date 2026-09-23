package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

public class ServicioReservas {

    private final ValidadorReserva validador = new ValidadorReserva();

    public double procesar(
            Reserva r,
            int horasAnticipacion) {

        if (!validador.esValida(r, horasAnticipacion)) {
            return 0;
        }

        double total = calcularTotal(r);

        System.out.println(
                "Guardando reserva " + r.getId()
        );

        System.out.println(
                "Correo enviado a " + r.getCorreo()
        );

        r.confirmar();

        return total;
    }

    private double calcularTotal(Reserva r) {
        double total = 40;
        if ("VIP".equals(r.getTipo())) {
            return total * 0.85;
        }
        return total;
    }
}
