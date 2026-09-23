# Fase K | Proponer pruebas antes de proponer código

> **Principio:** primero define qué comportamiento necesitas proteger;
> después decide cómo reorganizar la estructura.

Las pruebas se especifican en formato AAA (Arrange / Act / Assert) sin implementar todavía.
Cada una corresponde a un comportamiento observable de la línea base que debe sobrevivir
a la refactorización candidata.

---

## Tabla de pruebas propuestas

| # | Refactorización candidata | Comportamiento a proteger | Nombre de prueba | Escenario LB |
|---|---|---|---|---|
| P1 | Separar cálculo VIP | Reserva VIP válida retorna `34.0` | `vipValidaRetorna34()` | LB-02 |
| P2 | Separar cálculo VIP | Reserva NORMAL válida retorna `40.0` (sin descuento) | `normalValidaRetorna40()` | LB-01 |
| P3 | Simplificar validaciones | Anticipación de 1h retorna `0.0` y no confirma | `unaHoraNoPermiteProcesar()` | LB-06 |
| P4 | Simplificar validaciones | Anticipación de 2h exactas retorna `40.0` y confirma | `dosHorasEsLimiteValido()` | LB-05 |
| P5 | Simplificar validaciones | Correo sin `@` retorna `0.0` y no confirma | `correoInvalidoNoProcesa()` | LB-03 |
| P6 | Simplificar validaciones | Periodo inválido (`fin = inicio`) retorna `0.0` y no confirma | `periodoInvalidoNoProcesa()` | LB-04 |
| P7 | Separar notificación / persistencia | Reserva válida sigue confirmándose tras separar `println` | `reservaValidaSeConfirma()` | LB-01 |
| P8 | Introducir `PeriodoReserva` | Periodo inválido sigue rechazándose con el mismo contrato | `periodoInvalidoNoProcesaConVO()` | LB-04 |
| P9 | Introducir `Correo` | Correo sin `@` sigue rechazándose con el mismo contrato | `correoInvalidoNoProcesaConVO()` | LB-03 |
| P10 | Convertir `String tipo` → enum `TipoReserva` | VIP conserva descuento; NORMAL conserva precio base | `tipoVipAplicaDescuento()`, `tipoNormalNoCambiaTotal()` | LB-01, LB-02 |

---

## Especificaciones AAA detalladas

### P1 — `vipValidaRetorna34()`
```
Arrange: reserva VIP, correo válido, fin > inicio, horasAnticipacion = 5
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 34.0
         reserva.getEstado() == CONFIRMADA
```
Protege: `40 × 0.85 = 34.0` — cualquier extracción del cálculo debe preservar esta aritmética.

---

### P2 — `normalValidaRetorna40()`
```
Arrange: reserva NORMAL, correo válido, fin > inicio, horasAnticipacion = 5
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 40.0
         reserva.getEstado() == CONFIRMADA
```
Protege: reserva NORMAL no recibe descuento; precio base intacto.

---

### P3 — `unaHoraNoPermiteProcesar()`
```
Arrange: reserva NORMAL, correo válido, fin > inicio, horasAnticipacion = 1
Act:     double resultado = servicio.procesar(reserva, 1)
Assert:  resultado == 0.0
         reserva.getEstado() == PENDIENTE
```
Protege: el umbral de anticipación mínima; verifica que el estado no cambia cuando se rechaza.

---

### P4 — `dosHorasEsLimiteValido()`
```
Arrange: reserva NORMAL, correo válido, fin > inicio, horasAnticipacion = 2
Act:     double resultado = servicio.procesar(reserva, 2)
Assert:  resultado == 40.0
         reserva.getEstado() == CONFIRMADA
```
Protege: el límite exacto `>= 2` (no `> 2`); caso frontera crítico.

---

### P5 — `correoInvalidoNoProcesa()`
```
Arrange: reserva NORMAL, correo = "incorrecto" (sin @), fin > inicio, horasAnticipacion = 5
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 0.0
         reserva.getEstado() == PENDIENTE
```
Protege: la regla de formato de correo; al introducir `Correo` como VO, el contrato no cambia.

---

### P6 — `periodoInvalidoNoProcesa()`
```
Arrange: reserva NORMAL, correo válido, fin = inicio (fin NO es posterior), horasAnticipacion = 5
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 0.0
         reserva.getEstado() == PENDIENTE
```
Protege: la invariante `fin > inicio`; al introducir `PeriodoReserva`, este rechazo
debe mantenerse con el mismo retorno `0.0` (no excepción, salvo decisión explícita).

---

### P7 — `reservaValidaSeConfirma()`
```
Arrange: reserva NORMAL, correo válido, fin > inicio, horasAnticipacion = 5
Act:     servicio.procesar(reserva, 5)
Assert:  reserva.getEstado() == CONFIRMADA
```
Protege: que extraer notificación/persistencia a clases separadas no rompe
el cambio de estado de la reserva — el efecto de dominio debe conservarse.

---

### P8 — `periodoInvalidoNoProcesaConVO()`
```
Arrange: periodo con fin = inicio encapsulado en PeriodoReserva (o sin pasar al constructor de Reserva)
Act:     servicio.procesar(reserva, 5)
Assert:  resultado == 0.0
         reserva.getEstado() == PENDIENTE
```
Prueba específica para ejecutar **después** de introducir `PeriodoReserva`;
verifica que el contrato observable no cambió respecto a P6.

---

### P9 — `correoInvalidoNoProcesaConVO()`
```
Arrange: reserva con Correo inválido encapsulado en VO Correo
Act:     servicio.procesar(reserva, 5)
Assert:  resultado == 0.0
         reserva.getEstado() == PENDIENTE
```
Prueba específica para ejecutar **después** de introducir `Correo`;
verifica que el contrato observable no cambió respecto a P5.

---

### P10 — `tipoVipAplicaDescuento()` y `tipoNormalNoCambiaTotal()`
```
// VIP
Arrange: reserva con TipoReserva.VIP (enum), entradas válidas, 5h
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 34.0

// NORMAL
Arrange: reserva con TipoReserva.NORMAL (enum), entradas válidas, 5h
Act:     double resultado = servicio.procesar(reserva, 5)
Assert:  resultado == 40.0
```
Protege: al reemplazar `String tipo` por enum, los valores `NORMAL` y `VIP`
producen exactamente los mismos resultados que antes.

---

## Red de seguridad mínima por orden de refactorización

```
Antes de cualquier cambio:  P1, P2, P3, P4, P5, P6, P7
Antes de extraer notif.:    P7
Antes de extraer cálculo:   P1, P2
Antes de Correo VO:         P5  →  luego P9
Antes de PeriodoReserva VO: P6  →  luego P8
Antes de enum TipoReserva:  P1, P2  →  luego P10
```
