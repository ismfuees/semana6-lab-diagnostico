package edu.uees.refactor.app;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.service.ServicioReservas;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        ServicioReservas servicio = new ServicioReservas();
        LocalDateTime base = LocalDateTime.now().plusDays(1);

        // LB-01 | NORMAL válida | NORMAL, correo válido, 5h anticipación
        ejecutar("LB-01", servicio,
                new Reserva("R-001", "ana@uees.edu.ec",
                        base, base.plusHours(1), "NORMAL"), 5);

        // LB-02 | VIP válida | VIP, correo válido, 5h anticipación
        ejecutar("LB-02", servicio,
                new Reserva("R-002", "ana@uees.edu.ec",
                        base, base.plusHours(1), "VIP"), 5);

        // LB-03 | Correo inválido | sin @
        ejecutar("LB-03", servicio,
                new Reserva("R-003", "incorrecto",
                        base, base.plusHours(1), "NORMAL"), 5);

        // LB-04 | Periodo inválido | fin <= inicio
        ejecutar("LB-04", servicio,
                new Reserva("R-004", "ana@uees.edu.ec",
                        base, base.minusHours(1), "NORMAL"), 5);

        // LB-05 | Límite válido | exactamente 2h anticipación
        ejecutar("LB-05", servicio,
                new Reserva("R-005", "ana@uees.edu.ec",
                        base, base.plusHours(1), "NORMAL"), 2);

        // LB-06 | Límite inválido | 1h anticipación
        ejecutar("LB-06", servicio,
                new Reserva("R-006", "ana@uees.edu.ec",
                        base, base.plusHours(1), "NORMAL"), 1);
    }

    private static void ejecutar(
            String id,
            ServicioReservas servicio,
            Reserva reserva,
            int horasAnticipacion) {

        System.out.println("=== " + id + " ===");
        double total = servicio.procesar(reserva, horasAnticipacion);
        System.out.println("Estado : " + reserva.getEstado());
        System.out.println("Retorno: " + total);
        System.out.println();
    }
}
