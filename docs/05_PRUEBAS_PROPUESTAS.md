# Fase K | Proponer pruebas antes de proponer código

Principio: primero se define qué comportamiento necesita protección; después se decide cómo reorganizar la estructura.

| Refactorización candidata | Comportamiento a proteger | Prueba propuesta | Descripción AAA |
|---|---|---|---|
| Extraer cálculo VIP | VIP con base 40 devuelve exactamente 34.0 (descuento 15 %) | `vipConservaResultadoActual()` | **Arrange** reserva tipo VIP con correo válido y anticipación ≥ 2 h. **Act** llamar a `procesar()`. **Assert** retorno == 34.0. |
| Simplificar validación de anticipación | 1 h de anticipación retorna 0 y no confirma | `unaHoraNoPermiteProcesar()` | **Arrange** reserva válida con `horasAnticipacion=1`. **Act** llamar a `procesar()`. **Assert** retorno == 0 y `getEstado() == PENDIENTE`. |
| Separar notificación / persistencia | Reserva válida sigue quedando CONFIRMADA tras mover efectos secundarios | `reservaValidaSeConfirma()` | **Arrange** reserva NORMAL válida, anticipación 5 h. **Act** llamar a `procesar()`. **Assert** `getEstado() == CONFIRMADA`. |
| Introducir `PeriodoReserva` | Periodo inválido (fin <= inicio) sigue devolviendo 0 y no confirma | `periodoInvalidoNoProcesa()` | **Arrange** reserva con `fin = inicio` (o fin anterior). **Act** llamar a `procesar()`. **Assert** retorno == 0 y `getEstado() == PENDIENTE`. |
| Introducir `TipoReserva` (enum) | NORMAL no aplica descuento — devuelve 40.0 | `normalConservaBase40()` | **Arrange** reserva tipo NORMAL, correo válido, 5 h anticipación. **Act** llamar a `procesar()`. **Assert** retorno == 40.0. |
| Extraer validación de correo | Correo sin `@` devuelve 0 y no confirma | `correoSinArrobaNoPermiteProcesar()` | **Arrange** reserva con correo `"incorrecto"`. **Act** llamar a `procesar()`. **Assert** retorno == 0 y `getEstado() == PENDIENTE`. |
| Extraer validación de correo | Límite exacto de anticipación (2 h) procesa correctamente | `horasExactasDosPermiteProcesar()` | **Arrange** reserva válida con `horasAnticipacion=2`. **Act** llamar a `procesar()`. **Assert** retorno == 40.0 y `getEstado() == CONFIRMADA`. |

## Zona de testabilidad actual (Fase H)

| Zona | Qué sería deseable probar | Qué lo dificulta hoy |
|---|---|---|
| Descuento VIP | Retorno calculado exacto | Mezclado con validaciones y efectos secundarios; no hay forma de probar el cálculo solo. |
| Notificación por correo | Que se solicite envío de correo al confirmar | Solo existe `println`; no hay interfaz que permita verificar si el envío fue invocado. |
| Persistencia | Que se intente guardar la reserva | Solo existe `println`; no hay repositorio inyectable ni interfaz que se pueda observar. |
| Validación de periodo | Que `fin > inicio` sea la regla exacta | La regla vive dentro del método `procesar()`; no se puede probar de forma aislada. |

> **Nota (Semana 6):** Los dobles de prueba (mocks/stubs) se introducirán en semanas posteriores. Aquí basta identificar qué dependencias o efectos dificultan la prueba.
