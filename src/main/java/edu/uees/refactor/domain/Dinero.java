package edu.uees.refactor.domain;

/**
 * Clase auxiliar para practicar assertThrows sin modificar el contrato
 * heredado de ServicioReservas.
 *
 * Laboratorio 2 — ejercicio adicional (sección 14 del enunciado).
 */
public record Dinero(double valor) {

    public Dinero {
        if (valor < 0) {
            throw new IllegalArgumentException(
                    "El dinero no puede ser negativo"
            );
        }
    }
}
