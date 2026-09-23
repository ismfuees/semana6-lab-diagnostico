package edu.uees.refactor.domain;

/**
 * Tipos de reserva reconocidos por el sistema.
 *
 * Reemplaza el String libre "tipo" en Reserva, cerrando el conjunto
 * de valores permitidos. Un error tipográfico ("vip", "Vip") ahora
 * es un error de compilación, no un descuento silenciosamente incorrecto.
 */
public enum TipoReserva {
    NORMAL,
    VIP
}
