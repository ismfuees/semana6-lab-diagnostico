# Fase J | Matriz de riesgo

Evalúa la probabilidad de romper comportamiento observable y el impacto si ocurre,
usando la escala: **Bajo / Medio / Alto**.

| Cambio candidato | Probabilidad de romper | Impacto si rompe | Riesgo | Cómo reducirlo |
|---|---|---|---|---|
| Extraer clase de notificación | Baja — el `println` no afecta el retorno ni el estado de `Reserva` | Medio — si se omite la llamada, la reserva ya no "notifica" aunque se confirme | **Bajo-Medio** | Proteger con `reservaValidaSeConfirma()` antes de extraer; verificar que el estado sigue siendo `CONFIRMADA` |
| Introducir `Correo` (Value Object) | Media — requiere cambiar la firma del constructor de `Reserva` y todos los puntos de construcción | Alto — si la validación en el constructor de `Correo` es más estricta que `contains("@")`, casos que hoy pasan podrían rechazarse | **Medio** | Definir primero `correoInvalidoNoProcesa()` y `normalValidaRetorna40()`; asegurarse de que `Correo` acepta exactamente los mismos valores que hoy pasan |
| Introducir `PeriodoReserva` (Value Object) | Media — cambia la firma del constructor de `Reserva`; la triple condición L27–30 desaparece del servicio | Alto — si el constructor de `PeriodoReserva` lanza excepción en lugar de retornar `0`, el contrato observable cambia | **Medio-Alto** | Definir primero `periodoInvalidoNoProcesa()` con `fin = inicio`; decidir explícitamente si `PeriodoReserva` lanza excepción o retorna nulo; mantener el retorno `0.0` en el servicio |
| Simplificar validaciones (extraer método `esEntradaValida()`) | Baja — es una extracción interna sin cambio de lógica | Medio — si se reordena o se omite un guard, un caso inválido podría pasar al cálculo | **Bajo** | Cubrir LB-03, LB-04 y LB-06 antes de mover; verificar que los tres retornan `0.0` y estado `PENDIENTE` tras la extracción |
| Separar cálculo VIP (extraer método `calcularTotal(tipo)`) | Baja — el resultado matemático `40 × 0.85 = 34` no cambia | Bajo — el único riesgo es un error aritmético al mover la expresión | **Bajo** | `vipValidaRetorna34()` y `normalValidaRetorna40()` son suficientes como red de seguridad |

---

## Escala de referencia

| Nivel | Interpretación |
|---|---|
| **Bajo** | Cambio local, comportamiento bien entendido, prueba fácil de crear antes de modificar |
| **Medio** | Afecta la firma de constructores o requiere adaptar múltiples puntos de construcción |
| **Alto** | Puede alterar el contrato observable (retorno, estado, excepciones) o efectos externos |

---

## Orden de riesgo creciente

```
Separar cálculo VIP          → Bajo
Simplificar validaciones     → Bajo
Extraer notificación         → Bajo-Medio
Introducir Correo            → Medio
Introducir PeriodoReserva    → Medio-Alto
```

> El cambio de **mayor riesgo** es `PeriodoReserva` porque modifica la firma de `Reserva`,
> elimina la triple condición del servicio y obliga a decidir si el rechazo se comunica
> mediante retorno `0.0` (contrato actual) o mediante excepción (contrato nuevo).
> Esa decisión debe tomarse explícitamente y documentarse antes de ejecutar el cambio.

---

## Relación riesgo ↔ prueba protectora

| Cambio | Prueba mínima antes de ejecutar |
|---|---|
| Separar cálculo VIP | `vipValidaRetorna34()`, `normalValidaRetorna40()` |
| Simplificar validaciones | `correoInvalidoNoProcesa()`, `periodoInvalidoNoProcesa()`, `anticipacionInsuficienteNoProcesa()` |
| Extraer notificación | `reservaValidaSeConfirma()` |
| Introducir `Correo` | `correoInvalidoNoProcesa()`, `normalValidaRetorna40()` |
| Introducir `PeriodoReserva` | `periodoInvalidoNoProcesa()`, `normalValidaRetorna40()`, `vipValidaRetorna34()` |
