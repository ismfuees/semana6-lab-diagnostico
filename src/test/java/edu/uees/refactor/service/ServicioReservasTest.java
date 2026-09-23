package edu.uees.refactor.service;

import edu.uees.refactor.domain.EstadoReserva;
import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.domain.TipoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioReservasTest {

    private ServicioReservas servicio;

    // Fecha fija para reproducibilidad (no depende de LocalDateTime.now())
    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 9, 20, 10, 0);

    @BeforeEach
    void setUp() {
        servicio = new ServicioReservas();
    }

    // ---------------------------------------------------------------
    // Fixtures de preparación
    // ---------------------------------------------------------------

    private Reserva reservaNormalValida() {
        return new Reserva(
                "R-NORMAL",
                "ana@uees.edu.ec",
                INICIO,
                INICIO.plusHours(1),
                TipoReserva.NORMAL
        );
    }

    private Reserva reservaVipValida() {
        return new Reserva(
                "R-VIP",
                "vip@uees.edu.ec",
                INICIO,
                INICIO.plusHours(1),
                TipoReserva.VIP
        );
    }

    // ---------------------------------------------------------------
    // Prueba 1 | Caso NORMAL válido — LB-01
    // ---------------------------------------------------------------

    @Test
    void normalActualmenteRetornaCuarenta() {
        // Arrange
        Reserva reserva = reservaNormalValida();

        // Act
        double total = servicio.procesar(reserva, 5);

        // Assert
        assertAll(
                () -> assertEquals(40, total, 0.001,
                        "Precio base NORMAL debe ser 40"),
                () -> assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(),
                        "Reserva NORMAL válida debe quedar CONFIRMADA")
        );
    }

    // ---------------------------------------------------------------
    // Prueba 2 | Caso VIP válido — LB-02
    // ---------------------------------------------------------------

    @Test
    void vipActualmenteRetornaTreintaYCuatro() {
        // Arrange
        Reserva reserva = reservaVipValida();

        // Act
        double total = servicio.procesar(reserva, 5);

        // Assert
        assertEquals(34, total, 0.001,
                "VIP debe aplicar 15% de descuento: 40 * 0.85 = 34");
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(),
                "Reserva VIP válida debe quedar CONFIRMADA");
    }

    // ---------------------------------------------------------------
    // Prueba 3 | Correo inválido — LB-03
    // ---------------------------------------------------------------

    @Test
    void correoInvalidoNoProcesaReserva() {
        // Arrange
        Reserva reserva = new Reserva(
                "R-EMAIL",
                "correo-invalido",      // sin @
                INICIO,
                INICIO.plusHours(1),
                TipoReserva.NORMAL
        );

        // Act
        double total = servicio.procesar(reserva, 5);

        // Assert
        assertEquals(0, total, 0.001,
                "Correo sin @ debe retornar 0");
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado(),
                "Reserva con correo inválido no debe confirmarse");
    }

    // ---------------------------------------------------------------
    // Prueba 4 | Periodo inválido (fin < inicio) — LB-04
    // ---------------------------------------------------------------

    @Test
    void periodoConFinAnteriorNoProcesa() {
        // Arrange
        Reserva reserva = new Reserva(
                "R-PERIODO",
                "ana@uees.edu.ec",
                INICIO,
                INICIO.minusHours(1),   // fin < inicio
                TipoReserva.NORMAL
        );

        // Act
        double total = servicio.procesar(reserva, 5);

        // Assert
        assertEquals(0, total, 0.001,
                "Periodo con fin anterior a inicio debe retornar 0");
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado(),
                "Reserva con periodo inválido no debe confirmarse");
    }

    // ---------------------------------------------------------------
    // Prueba 5 | Límite válido: exactamente 2h — LB-05
    // ---------------------------------------------------------------

    @Test
    void dosHorasExactasPermitenProcesar() {
        // Arrange
        Reserva reserva = reservaNormalValida();

        // Act
        double total = servicio.procesar(reserva, 2);

        // Assert
        assertEquals(40, total, 0.001,
                "Exactamente 2h de anticipación debe ser aceptado (límite válido)");
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(),
                "Con 2h exactas la reserva debe quedar CONFIRMADA");
    }

    // ---------------------------------------------------------------
    // Prueba 6 | Límite inválido: 1h — LB-06
    // ---------------------------------------------------------------

    @Test
    void unaHoraNoPermiteProcesar() {
        // Arrange
        Reserva reserva = reservaNormalValida();

        // Act
        double total = servicio.procesar(reserva, 1);

        // Assert
        assertEquals(0, total, 0.001,
                "1h de anticipación debe retornar 0 (por debajo del mínimo de 2h)");
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado(),
                "Con 1h la reserva no debe confirmarse");
    }

    // ---------------------------------------------------------------
    // Prueba 7 | Guardia null
    // ---------------------------------------------------------------

    @Test
    void reservaNulaRetornaCero() {
        // Arrange — null explícito

        // Act
        double total = servicio.procesar(null, 5);

        // Assert
        assertEquals(0, total, 0.001,
                "Reserva null debe retornar 0 sin lanzar excepción");
    }
}
