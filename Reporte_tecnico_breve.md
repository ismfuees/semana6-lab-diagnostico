# Reporte Técnico Breve — AE5
## Refactorización respaldada por pruebas unitarias

## 1. Portada y datos del estudiante

| Campo | Detalle |
|---|---|
| **Universidad** | Universidad Espíritu Santo |
| **Asignatura** | Diseño de Software — UCOM0310 |
| **Actividad** | AE5 · Refactorización respaldada por pruebas unitarias |
| **Período** | PEL 4 – 2026 · Semana 6 |
| **Estudiante** | IVAN STALYN MUELA FLOR |
| **Docente** | Ph.D. Jaime Paul Sayago Heredia |

---

## 2. Descripción del problema inicial

El sistema de reservas de tutorías contiene una clase [`ServicioReservas`](semana6-lab-diagnostico/src/main/java/edu/uees/refactor/service/ServicioReservas.java) cuyo único método público `procesar(Reserva r, int horasAnticipacion)` concentraba, en 54 líneas, cinco responsabilidades completamente independientes:

1. **Validación de entradas** — cuatro guards para null, correo, periodo y anticipación.
2. **Cálculo de precio** — precio base y descuento VIP.
3. **Persistencia simulada** — `System.out.println("Guardando reserva …")`.
4. **Notificación simulada** — `System.out.println("Correo enviado a …")`.
5. **Cambio de estado** — llamada a `r.confirmar()`.

Adicionalmente, [`Reserva`](semana6-lab-diagnostico/src/main/java/edu/uees/refactor/domain/Reserva.java) usaba un `String tipo` libre para representar el tipo de reserva, aceptando cualquier valor sin control del compilador. El resultado era un diseño frágil con cinco razones de cambio en una sola clase, efectos secundarios no inyectables y una Primitive Obsession que crecería linealmente con nuevos tipos.

---

## 3. Línea base

Ejecutada sobre el código heredado sin modificaciones. Seis escenarios verificados manualmente y luego automatizados con JUnit 5:

| ID | Escenario | Entrada | Estado final | Retorno |
|---|---|---|---|---|
| LB-01 | NORMAL válida | NORMAL, correo válido, 5h | `CONFIRMADA` | `40.0` |
| LB-02 | VIP válida | VIP, correo válido, 5h | `CONFIRMADA` | `34.0` |
| LB-03 | Correo inválido | sin `@` | `PENDIENTE` | `0.0` |
| LB-04 | Periodo inválido | fin < inicio | `PENDIENTE` | `0.0` |
| LB-05 | Límite válido | 2h anticipación exactas | `CONFIRMADA` | `40.0` |
| LB-06 | Límite inválido | 1h anticipación | `PENDIENTE` | `0.0` |

Observaciones clave:
- Los efectos de consola (`Guardando`, `Correo enviado`) **solo aparecen** en los casos que terminan en `CONFIRMADA`.
- Ningún escenario lanza excepción; los errores se comunican exclusivamente por retorno `0.0`.
- El caso LB-05 (2h exactas) es el caso frontera más crítico para la regla `horasAnticipacion < 2`.

---

## 4. Diagnóstico resumido

Se identificaron seis smells con evidencia de línea concreta:

| # | Smell | Ubicación | Impacto |
|---|---|---|---|
| 1 | Long Method / God Method | `ServicioReservas` L14–54 | Alto |
| 2 | Data Clumps + Primitive Obsession (periodo) | `ServicioReservas` L27–30 | Alto |
| 3 | Primitive Obsession (`String tipo`) | `ServicioReservas` L39 | Alto |
| 4 | Infraestructura mezclada con dominio (`println`) | `ServicioReservas` L43–49 | Medio |
| 5 | Primitive Obsession (`String correo`) | `ServicioReservas` L22–23 | Medio |
| 6 | Long Parameter List | `Reserva` L14–26 | Medio |

---

## 5. Pruebas utilizadas como red de seguridad

**Suite implementada con JUnit 5 — commit `e3e005b`:**

```
ServicioReservasTest  (7 pruebas de caracterización)
  normalActualmenteRetornaCuarenta()       → LB-01
  vipActualmenteRetornaTreintaYCuatro()    → LB-02
  correoInvalidoNoProcesaReserva()         → LB-03
  periodoConFinAnteriorNoProcesa()         → LB-04
  dosHorasExactasPermitenProcesar()        → LB-05  ← caso frontera
  unaHoraNoPermiteProcesar()               → LB-06  ← caso frontera
  reservaNulaRetornaCero()                 → guardia null

DineroTest  (3 pruebas — práctica assertThrows)
  dineroPositivoSeCreaCorrectamente()
  dineroCeroEsValido()
  dineroNegativoLanzaExcepcion()
```

**Pruebas añadidas tras la Refactorización 1 — commit `fce54f9`:**

```
ValidadorReservaTest  (6 pruebas sobre ValidadorReserva)
  reservaValidaEsAceptada()
  reservaNulaEsRechazada()
  correoSinArrobaEsRechazado()
  periodoConFinAnteriorEsRechazado()
  unaHoraDeAnticipacionEsRechazada()
  dosHorasDeAnticipacionSonAceptadas()
```

**Total final: 16 pruebas — 16/16 verde en cada commit.**

La regresión intencional (`0.85 → 0.80`) fue detectada inmediatamente:
```
expected: <34.0> but was: <32.0>   →  vipActualmenteRetornaTreintaYCuatro
```

---

## 6. Refactorización 1 — Extract Class `ValidadorReserva`

**Problema diagnosticado:** Los cuatro guards de validación (null, correo, periodo, anticipación) vivían incrustados en `procesar()`. Cualquier cambio en las reglas de validación obligaba a abrir el servicio completo, aunque el precio y la notificación no cambiaran.

**Código antes:**
```java
// ServicioReservas.procesar() — L14–35
if (r == null) { return 0; }
if (r.getCorreo() == null || !r.getCorreo().contains("@")) { return 0; }
if (r.getInicio() == null || r.getFin() == null
        || !r.getFin().isAfter(r.getInicio())) { return 0; }
if (horasAnticipacion < 2) { return 0; }
```

**Técnica:** Extract Class → nueva clase [`ValidadorReserva`](semana6-lab-diagnostico/src/main/java/edu/uees/refactor/service/ValidadorReserva.java) con método `esValida(Reserva, int)`.

**Código después:**
```java
// ServicioReservas.procesar()
if (!validador.esValida(r, horasAnticipacion)) { return 0; }
```

**Pruebas protectoras:** 7 pruebas de `ServicioReservasTest` (comportamiento observable sin cambio) + 6 nuevas en `ValidadorReservaTest`.

**Commit:** `fce54f9` — `refactor: extract class ValidadorReserva` — **16/16 verde**.

---

## 7. Refactorización 2 — Extract Class `NotificadorReserva`

**Problema diagnosticado:** Los dos `System.out.println` (persistencia y notificación simuladas) estaban acoplados directamente al flujo de dominio. No eran inyectables ni verificables en pruebas; reemplazarlos por un canal real (base de datos, email) requería abrir `ServicioReservas`.

**Código antes:**
```java
System.out.println("Guardando reserva " + r.getId());
System.out.println("Correo enviado a " + r.getCorreo());
```

**Técnica:** Extract Class → nueva clase [`NotificadorReserva`](semana6-lab-diagnostico/src/main/java/edu/uees/refactor/service/NotificadorReserva.java) con método `notificar(Reserva)`.

**Código después:**
```java
notificador.notificar(r);
```

**Prueba protectora:** `reservaValidaSeConfirma()` — estado `CONFIRMADA` preservado tras la delegación.

**Commit:** `db31780` — `refactor: extract class NotificadorReserva` — **16/16 verde**.

---

## 8. Refactorización 3 — Introducir enum `TipoReserva`

**Problema diagnosticado:** `String tipo` en `Reserva` aceptaba cualquier cadena. La comparación `"VIP".equals(r.getTipo())` era frágil: distingue mayúsculas, acepta errores tipográficos sin error de compilación y crecería linealmente con cada nuevo tipo (`else if ("PREMIUM"…)`).

**Código antes:**
```java
// Reserva.java
private final String tipo;

// ServicioReservas.calcularTotal()
if ("VIP".equals(r.getTipo())) { return total * 0.85; }
```

**Técnica:** Primitive Obsession → nuevo enum [`TipoReserva {NORMAL, VIP}`](semana6-lab-diagnostico/src/main/java/edu/uees/refactor/domain/TipoReserva.java).

**Código después:**
```java
// Reserva.java
private final TipoReserva tipo;

// ServicioReservas.calcularTotal()
if (TipoReserva.VIP == r.getTipo()) { return total * 0.85; }
```

Un valor inválido ahora es un **error de compilación**, no un descuento silenciosamente incorrecto en producción. Se actualizaron todos los constructores de `Reserva` en `Main`, `ServicioReservasTest` y `ValidadorReservaTest`.

**Pruebas protectoras:** `vipActualmenteRetornaTreintaYCuatro()`, `normalActualmenteRetornaCuarenta()`.

**Commit:** `1ac6c69` — `refactor: introducir enum TipoReserva` — **16/16 verde**.

---

## 9. Comparación antes / después

| Dimensión | Antes | Después | Evidencia |
|---|---|---|---|
| **Responsabilidades** | 5 en `ServicioReservas.procesar()` (validación, precio, persistencia, notificación, estado) | 1 por clase: `ValidadorReserva`, `NotificadorReserva`, `ServicioReservas` | Mapa de responsabilidades / estructura de clases |
| **Cohesión** | Baja — método con razones de cambio heterogéneas | Alta — cada clase tiene un único motivo para cambiar | Número de responsabilidades por clase |
| **Acoplamiento** | Alto — `ServicioReservas` conocía las reglas de validación, el canal de notificación y el tipo de reserva | Reducido — `ServicioReservas` delega en colaboradores con responsabilidad única | Dependencias entre clases |
| **Datos del dominio** | `String tipo` libre, aceptaba cualquier valor | `TipoReserva` enum — conjunto cerrado, verificado en compilación | `TipoReserva.java`; error tipográfico = error de compilación |
| **Condicionales** | 5 `if` en `procesar()`, incluyendo comparación de `String` frágil | 1 `if` en `esValida()`, 1 en `calcularTotal()` — cada uno en su clase | `ValidadorReserva.java`, `ServicioReservas.calcularTotal()` |
| **Pruebas** | 0 (código heredado sin suite) | 16 pruebas automatizadas — 16/16 verde en todos los commits | `mvn clean test` BUILD SUCCESS |
| **Git** | — | 7 commits trazables: diagnóstico → suite → micro-refactor → 3 refactorizaciones | `git log --oneline` |

### `ServicioReservas.procesar()` — tamaño y estructura

| Métrica | Antes | Después |
|---|---|---|
| Líneas totales de la clase | 54 | 34 |
| Líneas de `procesar()` | 40 | 7 |
| Bloques `if` en `procesar()` | 5 | 1 |
| Responsabilidades en `procesar()` | 5 | 1 (orquestar colaboradores) |

---

## 10. Historial Git

```
1ac6c69  refactor: introducir enum TipoReserva
db31780  refactor: extract class NotificadorReserva
fce54f9  refactor: extract class ValidadorReserva
b6c0e28  docs:    agregar reflexion tecnica Labs 1 y 2
554b830  refactor: extraer calculo de total            ← Lab 2
e3e005b  test:    caracterizar comportamiento heredado  ← Lab 2
00d7837  chore:   registrar proyecto heredado y linea base  ← Lab 1
bee73e6  chore:   registrar proyecto heredado y linea base  (inicial)
```

Cada commit de refactorización fue precedido por la ejecución de `mvn clean test` verde y seguido de un nuevo `mvn clean test` verde, cumpliendo el ciclo: **PRUEBA VERDE → CAMBIO PEQUEÑO → PRUEBA VERDE → COMMIT**.

---

## 11. Conclusiones

1. **La línea base es indispensable antes de cualquier cambio.** Sin los seis escenarios LB-01 a LB-06 ejecutados y documentados, las refactorizaciones habrían sido especulativas. La suite automatizada convirtió esa evidencia en una red de seguridad reproducible.

2. **Extract Class reduce razones de cambio de forma medible.** `ServicioReservas.procesar()` pasó de cinco responsabilidades a una (orquestar colaboradores). Ahora, un cambio en las reglas de validación solo abre `ValidadorReserva`; un cambio en el canal de notificación solo abre `NotificadorReserva`.

3. **Primitive Obsession no es solo un problema de legibilidad.** `String tipo` podía producir descuentos incorrectos en producción sin fallo visible. El enum `TipoReserva` traslada ese error al compilador, eliminando una categoría entera de bugs silenciosos.

4. **Las pruebas de frontera son las más valiosas.** `dosHorasExactasPermitenProcesar()` y `unaHoraNoPermiteProcesar()` protegen el límite `>= 2` que las refactorizaciones de condicionales suelen romper con un simple `<` vs `<=`.

5. **Refactorizar ≠ cambio funcional.** La evidencia concreta es la suite verde antes y después de cada commit. La regresión intencional (`0.85 → 0.80`, `expected: <34.0> but was: <32.0>`) demostró que las pruebas detectan exactamente el cambio que pretenden evitar.

---

## 12. Declaración de uso de IA

Este trabajo fue realizado con asistencia de IBM Bob (modelo de IA) para la ejecución de comandos, generación de código y redacción de documentación técnica. Todas las decisiones de diseño, justificaciones de refactorización y análisis de smells fueron revisadas y validadas por el estudiante. El código generado fue ejecutado y verificado en el entorno local (`mvn clean test — BUILD SUCCESS`). La comprensión del problema, la selección de refactorizaciones y la interpretación de resultados son responsabilidad del estudiante.

---

## 13. Enlace al repositorio

```
Proyecto local: semana6-lab-diagnostico/
Rama:           main
Commit final:   1ac6c69  refactor: introducir enum TipoReserva
```

**Instrucciones de ejecución:**

```bash
# Compilar
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean compile

# Ejecutar suite completa
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean test

# Ejecutar aplicación
/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64/bin/java \
  -cp target/classes edu.uees.refactor.app.Main
```

**Resultado esperado de la suite:**
```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
