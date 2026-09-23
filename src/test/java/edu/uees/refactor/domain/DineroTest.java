package edu.uees.refactor.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DineroTest {

    @Test
    void dineroPositivoSeCreaCorrectamente() {
        // Arrange + Act
        Dinero d = new Dinero(100);

        // Assert
        assertEquals(100, d.valor(), 0.001);
    }

    @Test
    void dineroCeroEsValido() {
        // Arrange + Act
        Dinero d = new Dinero(0);

        // Assert
        assertEquals(0, d.valor(), 0.001);
    }

    @Test
    void dineroNegativoLanzaExcepcion() {
        // Arrange + Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new Dinero(-1),
                "Dinero negativo debe lanzar IllegalArgumentException"
        );
    }
}
