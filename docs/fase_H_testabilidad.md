# Fase H | Evaluación de testabilidad

Identificar qué dificulta probar `ServicioReservas` de forma aislada,
**sin implementar mocks todavía** (eso corresponde a semanas posteriores).

---

## Tabla de zonas y obstáculos

| Zona | Qué sería deseable probar | Qué lo dificulta hoy | Tipo de obstáculo |
|---|---|---|---|
| Descuento VIP | Que `procesar()` retorna `34.0` para una reserva VIP válida | El cálculo está mezclado con 4 validaciones previas y 2 efectos secundarios; no existe un método separado que solo calcule | Responsabilidades mezcladas |
| Validación de correo | Que un correo sin `@` hace retornar `0.0` sin confirmar | La regla vive enterrada dentro de `procesar()`; no hay método `esCorreoValido()` invocable de forma aislada | Sin punto de extensión |
| Validación de periodo | Que `fin ≤ inicio` hace retornar `0.0` sin confirmar | La triple condición (L27–30) está incrustada en el flujo; no existe un concepto `PeriodoReserva` con método `esValido()` | Lógica sin encapsular |
| Anticipación mínima | Que `1h` retorna `0.0` y `2h` retorna `40.0` | El umbral `2` es un literal mágico en el cuerpo del método; cambiar la regla requiere modificar el método completo | Número mágico sin nombre |
| Persistencia | Que al confirmar una reserva se "guarda" | Hoy solo existe `System.out.println`; no hay interfaz ni colaborador inyectable que se pueda reemplazar por un doble de prueba | Efecto secundario acoplado |
| Notificación | Que al confirmar se "envía correo" | Ídem — `System.out.println` mezclado con el flujo principal; no existe separación entre decidir enviar y ejecutar el envío | Efecto secundario acoplado |
| Estado de la reserva | Que `CONFIRMADA` solo se asigna si todas las validaciones pasan | `r.confirmar()` es llamado directamente dentro de `procesar()`; no es posible verificar en qué condición se llama sin ejecutar el método completo | Acoplamiento directo |

---

## Análisis por tipo de obstáculo

### Responsabilidades mezcladas
El método `procesar()` realiza en secuencia: validar → calcular → persistir → notificar → confirmar.
Para probar solo el cálculo del descuento VIP hay que construir una `Reserva` válida en todos
sus campos, pasar todas las validaciones previas y aceptar que los `println` se ejecuten como
efecto colateral del test. No hay forma de llegar al cálculo sin pasar por todo lo anterior.

```java
// Para probar solo esto (L39-41)…
if ("VIP".equals(r.getTipo())) {
    total = total * 0.85;
}
// …hay que superar primero L18, L22-23, L27-30 y L33.
```

### Efectos secundarios no inyectables
Los dos `System.out.println` (L43–49) son efectos secundarios fijos. Hoy no existe forma de:
- Verificar que se llamaron (no hay objeto al que preguntar).
- Suprimirlos en un entorno de prueba.
- Reemplazarlos por un colaborador alternativo sin modificar `ServicioReservas`.

Esto no impide escribir pruebas sobre el **retorno** y el **estado**, pero sí impide
verificar los efectos de persistencia y notificación de forma directa.

### Sin puntos de extensión
Ninguna de las reglas de validación está expuesta como método separado ni como
interfaz. Esto significa que:
- No se puede probar la regla de correo sin invocar `procesar()` completo.
- No se puede sustituir la política de anticipación sin modificar el código fuente.
- No se puede añadir un nuevo tipo de reserva sin abrir `ServicioReservas`.

---

## Qué sí es testable hoy (sin mocks)

A pesar de los obstáculos, los siguientes comportamientos **pueden verificarse** con
JUnit básico sobre el método `procesar()` completo:

| Comportamiento verificable | Estrategia de prueba |
|---|---|
| Retorno `0.0` para correo inválido | Crear `Reserva` con correo sin `@`, llamar `procesar()`, assert retorno == 0 |
| Retorno `0.0` para periodo inválido | Crear `Reserva` con `fin = inicio`, llamar `procesar()`, assert retorno == 0 |
| Retorno `0.0` para anticipación < 2h | Reserva válida + `horasAnticipacion = 1`, assert retorno == 0 |
| Estado `PENDIENTE` en casos rechazados | En los tres casos anteriores, assert `reserva.getEstado() == PENDIENTE` |
| Retorno `40.0` para NORMAL válida | Reserva NORMAL + entradas válidas + 5h, assert retorno == 40.0 |
| Retorno `34.0` para VIP válida | Reserva VIP + entradas válidas + 5h, assert retorno == 34.0 |
| Estado `CONFIRMADA` para casos válidos | En los dos anteriores, assert `reserva.getEstado() == CONFIRMADA` |

Estos siete escenarios coinciden con los de la línea base (LB-01 a LB-06)
y son la red de seguridad mínima antes de cualquier refactorización.

---

## Resumen de obstáculos por prioridad

| Obstáculo | Impacto en testabilidad | Refactorización que lo resuelve |
|---|---|---|
| `println` de persistencia no inyectable | Medio — no se puede verificar ni suprimir | Extraer interfaz `Repositorio` / `ServicioNotificacion` |
| Cálculo VIP mezclado con validaciones | Medio — requiere setup complejo para probar una regla simple | Extraer método `calcularTotal(tipo)` |
| Regla de periodo sin encapsular | Alto — la triple condición se duplicará | Introducir `PeriodoReserva` con `esValido()` |
| Regla de correo sin encapsular | Medio — probable crecimiento de formato | Introducir Value Object `Correo` |
| Número mágico `2` | Bajo — testable pero frágil si cambia | Extraer constante `ANTICIPACION_MINIMA_HORAS` |
