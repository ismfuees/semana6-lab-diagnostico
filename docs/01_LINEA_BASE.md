# Fase C | Línea base manual

Escenarios ejecutados sobre el código heredado sin modificar `ServicioReservas`.
Compilado con Java 21 (class file version 65.0).

| ID | Escenario | Entrada principal | Estado final | Retorno | Mensajes en consola |
|---|---|---|---|---|---|
| LB-01 | NORMAL válida | NORMAL, correo válido, 5h | CONFIRMADA | 40.0 | `Guardando reserva R-001` / `Correo enviado a ana@uees.edu.ec` |
| LB-02 | VIP válida | VIP, correo válido, 5h | CONFIRMADA | 34.0 | `Guardando reserva R-002` / `Correo enviado a ana@uees.edu.ec` |
| LB-03 | Correo inválido | correo = "incorrecto" (sin @) | PENDIENTE | 0.0 | _(ninguno)_ |
| LB-04 | Periodo inválido | fin = inicio − 1h (fin < inicio) | PENDIENTE | 0.0 | _(ninguno)_ |
| LB-05 | Límite válido | 2h anticipación exactas | CONFIRMADA | 40.0 | `Guardando reserva R-005` / `Correo enviado a ana@uees.edu.ec` |
| LB-06 | Límite inválido | 1h anticipación | PENDIENTE | 0.0 | _(ninguno)_ |

## Respuestas a las preguntas de análisis

1. **¿Qué valores cambian entre NORMAL y VIP?**
   Solo el retorno: NORMAL devuelve `40.0`; VIP devuelve `34.0` (descuento del 15 %, `40 × 0.85`).
   El estado final es `CONFIRMADA` en ambos casos cuando el resto de entradas es válido.

2. **¿Qué casos dejan la reserva en PENDIENTE?**
   LB-03 (correo inválido), LB-04 (periodo inválido) y LB-06 (anticipación < 2 h).
   En todos ellos `procesar()` retorna `0.0` y **no llama a `r.confirmar()`**.

3. **¿Qué devuelve `procesar()` cuando una entrada no es procesable?**
   Devuelve `0.0` (double) en los cuatro guards: reserva nula, correo sin `@`, fin ≤ inicio y anticipación < 2 h.

4. **¿Existe alguna excepción visible en el flujo actual?**
   No. Ninguno de los seis escenarios lanza excepción; todos los errores se comunican mediante el valor de retorno `0.0`.

5. **¿Qué mensajes aparecen solo cuando la reserva se confirma?**
   `"Guardando reserva <id>"` y `"Correo enviado a <correo>"` solo aparecen en los casos que terminan en `CONFIRMADA` (LB-01, LB-02, LB-05).
   En los casos rechazados (LB-03, LB-04, LB-06) no se imprime ningún mensaje.
