# Fase C | Línea base manual

Escenarios ejecutados sobre el código heredado sin modificar su estructura.
Salida de referencia verificada con `mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"`:

```
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado: CONFIRMADA
Total: 34.0
```

## Tabla de escenarios

| ID | Escenario | Entrada principal | Estado final | Retorno | Mensajes / excepción |
|---|---|---|---|---|---|
| LB-01 | NORMAL válida | tipo="NORMAL", correo="ana@uees.edu.ec", horasAnticipacion=5 | CONFIRMADA | 40.0 | "Guardando reserva R-001" / "Correo enviado a ana@uees.edu.ec" |
| LB-02 | VIP válida | tipo="VIP", correo="ana@uees.edu.ec", horasAnticipacion=5 | CONFIRMADA | 34.0 | "Guardando reserva R-001" / "Correo enviado a ana@uees.edu.ec" |
| LB-03 | Correo inválido | correo="incorrecto" (sin @) | PENDIENTE | 0 | Ninguno — retorno temprano en la segunda guarda |
| LB-04 | Periodo inválido | fin = inicio (fin no es posterior a inicio) | PENDIENTE | 0 | Ninguno — retorno temprano en la tercera guarda |
| LB-05 | Límite válido | tipo="NORMAL", horasAnticipacion=2 (exactamente el límite) | CONFIRMADA | 40.0 | "Guardando reserva …" / "Correo enviado a …" |
| LB-06 | Límite inválido | horasAnticipacion=1 (justo por debajo del límite) | PENDIENTE | 0 | Ninguno — retorno temprano en la cuarta guarda |

## Registro de observaciones (Fase B)

| Observación | ¿Es comportamiento observable? | ¿Es detalle interno? | Comentario |
|---|---|---|---|
| La reserva termina CONFIRMADA | ✅ Sí | No | Visible desde fuera a través de `getEstado()`. |
| Se imprime un mensaje de persistencia | ✅ Sí | No | El mensaje es un efecto lateral observable en consola. |
| `ServicioReservas` contiene un `if VIP` | No | ✅ Sí | Estructura interna; puede cambiarse mientras el retorno 34.0 se preserve. |
| El total VIP es 34.0 | ✅ Sí | No | Valor retornado por el método, forma parte del contrato. |
| `Reserva` almacena `inicio` y `fin` por separado | No | ✅ Sí | Decisión de modelado interno; no es perceptible desde el contrato del servicio. |

## Preguntas de análisis

1. **¿Qué valores cambian entre NORMAL y VIP?**
   Solo el retorno del método: NORMAL devuelve `40.0`, VIP devuelve `34.0` (descuento del 15 %). El estado final (`CONFIRMADA`) y los mensajes de consola son idénticos.

2. **¿Qué casos dejan la reserva en PENDIENTE?**
   LB-03, LB-04 y LB-06: correo inválido, periodo inválido y anticipación insuficiente. En todos ellos el método retorna `0` antes de llamar a `r.confirmar()`.

3. **¿Qué devuelve `procesar()` cuando una entrada no es procesable?**
   Devuelve `0` (literal entero auto-promovido a `double`). No lanza excepción.

4. **¿Existe alguna excepción visible en el flujo actual?**
   No. Todos los caminos inválidos se resuelven con retornos anticipados (`return 0`). Si `r` es `null` la primera guarda lo captura; las demás guardas asumen `r != null`.

5. **¿Qué mensajes aparecen solo cuando la reserva se confirma?**
   `"Guardando reserva <id>"` y `"Correo enviado a <correo>"`. Aparecen únicamente cuando las cuatro validaciones se superan, inmediatamente antes de `r.confirmar()`.
