package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/**
 * Responsabilidad única: decidir si una reserva puede procesarse.
 *
 * Agrupa las cuatro reglas de validación que estaban dispersas
 * en ServicioReservas.procesar() y las encapsula en un concepto
 * con nombre propio. Cada regla puede cambiar de forma independiente
 * sin abrir ServicioReservas.
 */
public class ValidadorReserva {

    public boolean esValida(Reserva r, int horasAnticipacion) {
        if (r == null) {
            return false;
        }
        if (r.getCorreo() == null
                || !r.getCorreo().contains("@")) {
            return false;
        }
        if (r.getInicio() == null
                || r.getFin() == null
                || !r.getFin().isAfter(r.getInicio())) {
            return false;
        }
        if (horasAnticipacion < 2) {
            return false;
        }
        return true;
    }
}
