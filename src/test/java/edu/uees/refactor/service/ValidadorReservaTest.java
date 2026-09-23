package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.domain.TipoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorReservaTest {

    private ValidadorReserva validador;

    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 9, 20, 10, 0);

    @BeforeEach
    void setUp() {
        validador = new ValidadorReserva();
    }

    private Reserva reservaValida() {
        return new Reserva("R-1", "ana@uees.edu.ec",
                INICIO, INICIO.plusHours(1), TipoReserva.NORMAL);
    }

    @Test
    void reservaValidaEsAceptada() {
        assertTrue(validador.esValida(reservaValida(), 5));
    }

    @Test
    void reservaNulaEsRechazada() {
        assertFalse(validador.esValida(null, 5));
    }

    @Test
    void correoSinArrobaEsRechazado() {
        Reserva r = new Reserva("R-2", "invalido",
                INICIO, INICIO.plusHours(1), TipoReserva.NORMAL);
        assertFalse(validador.esValida(r, 5));
    }

    @Test
    void periodoConFinAnteriorEsRechazado() {
        Reserva r = new Reserva("R-3", "ana@uees.edu.ec",
                INICIO, INICIO.minusHours(1), TipoReserva.NORMAL);
        assertFalse(validador.esValida(r, 5));
    }

    @Test
    void unaHoraDeAnticipacionEsRechazada() {
        assertFalse(validador.esValida(reservaValida(), 1));
    }

    @Test
    void dosHorasDeAnticipacionSonAceptadas() {
        assertTrue(validador.esValida(reservaValida(), 2));
    }
}
