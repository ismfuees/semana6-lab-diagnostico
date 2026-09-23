# UEES | Diseño de Software | UCOM0310
## Semana 6 | AE5 — Refactorización respaldada por pruebas unitarias

| Campo | Detalle |
|---|---|
| **Universidad** | Universidad Espíritu Santo |
| **Asignatura** | Diseño de Software — UCOM0310 |
| **Período** | PEL 4 – 2026 · Semana 6 |
| **Estudiante** | IVAN STALYN MUELA FLOR |
| **Docente** | Ph.D. Jaime Paul Sayago Heredia |

---

## Propósito

Este proyecto demuestra la mejora controlada de un sistema de reservas de tutorías heredado,
aplicando el ciclo:

> **DIAGNOSTICAR → PROBAR → REFACTORIZAR → VERIFICAR → EVIDENCIAR**

---

## Requisitos

- Java 21
- Maven 3.x
- Git

```bash
java -version   # debe mostrar 21.x
mvn -version
git --version
```

---

## Instrucciones de ejecución

### Compilar

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean compile
```

Resultado esperado:
```
BUILD SUCCESS
```

### Ejecutar suite de pruebas

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean test
```

Resultado esperado:
```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Ejecutar la aplicación

```bash
/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64/bin/java \
  -cp target/classes edu.uees.refactor.app.Main
```

Salida esperada (6 escenarios de línea base):
```
=== LB-01 ===
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado : CONFIRMADA
Retorno: 40.0

=== LB-02 ===
Guardando reserva R-002
Correo enviado a ana@uees.edu.ec
Estado : CONFIRMADA
Retorno: 34.0

=== LB-03 ===
Estado : PENDIENTE
Retorno: 0.0

=== LB-04 ===
Estado : PENDIENTE
Retorno: 0.0

=== LB-05 ===
Guardando reserva R-005
Correo enviado a ana@uees.edu.ec
Estado : CONFIRMADA
Retorno: 40.0

=== LB-06 ===
Estado : PENDIENTE
Retorno: 0.0
```

---

## Estructura del proyecto

```text
semana6-lab-diagnostico/
├── pom.xml                                    ← JUnit 5.10.2 + Surefire 3.2.5
├── README.md
├── docs/
│   ├── 01_LINEA_BASE.md                       ← 6 escenarios ejecutados
│   ├── 02_MAPA_RESPONSABILIDADES.md           ← 5 razones de cambio
│   ├── 03_MATRIZ_DIAGNOSTICO.md               ← 6 smells con evidencia
│   ├── 04_MATRIZ_RIESGO.md                    ← 5 cambios evaluados
│   ├── 05_PRUEBAS_PROPUESTAS.md               ← 10 especificaciones AAA
│   ├── 06_PLAN_REFACTORIZACION.md             ← 6 pasos justificados
│   ├── 07_REFLEXION_TECNICA.md                ← reflexión final
│   ├── fase_E_diagnostico_clases.md
│   ├── fase_F_diagnostico_datos.md
│   ├── fase_G_diagnostico_condicionales.md
│   └── fase_H_testabilidad.md
├── src/
│   ├── main/java/edu/uees/refactor/
│   │   ├── app/
│   │   │   └── Main.java
│   │   ├── domain/
│   │   │   ├── Dinero.java                    ← record para assertThrows
│   │   │   ├── EstadoReserva.java
│   │   │   ├── Reserva.java
│   │   │   └── TipoReserva.java               ← enum (refactorización 3)
│   │   └── service/
│   │       ├── NotificadorReserva.java        ← extract class (refact. 2)
│   │       ├── ServicioReservas.java          ← orquestador
│   │       └── ValidadorReserva.java          ← extract class (refact. 1)
│   └── test/java/edu/uees/refactor/
│       ├── domain/
│       │   └── DineroTest.java                ← 3 pruebas (assertThrows)
│       └── service/
│           ├── ServicioReservasTest.java      ← 7 pruebas de caracterización
│           └── ValidadorReservaTest.java      ← 6 pruebas (refact. 1)
```

---

## Evidencias de entrega

### ☑ Código inicial y final

El código heredado original está registrado en el commit `00d7837`.
El código final refactorizado está en el commit `1ac6c69`.
Ambos estados son recuperables mediante `git checkout <commit>`.

### ☑ Pruebas JUnit 5 ejecutables

16 pruebas distribuidas en tres suites:

| Suite | Pruebas | Propósito |
|---|---|---|
| `ServicioReservasTest` | 7 | Caracterización del comportamiento heredado (LB-01 a LB-06 + null) |
| `ValidadorReservaTest` | 6 | Verificación aislada de las reglas de validación extraídas |
| `DineroTest` | 3 | Práctica de `assertThrows` con Value Object independiente |

### ☑ Resultado final de `mvn clean test`

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0  ← DineroTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  ← ValidadorReservaTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0  ← ServicioReservasTest
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### ☑ Al menos tres refactorizaciones justificadas

| # | Técnica | Problema resuelto | Commit |
|---|---|---|---|
| 1 | **Extract Class** → `ValidadorReserva` | 4 guards de validación mezclados con precio, persistencia y notificación | `fce54f9` |
| 2 | **Extract Class** → `NotificadorReserva` | `println` de infraestructura no inyectables ni verificables en pruebas | `db31780` |
| 3 | **Enum** `TipoReserva` | `String tipo` libre — error tipográfico producía descuento incorrecto sin fallo visible | `1ac6c69` |

Además, en el Lab 2 se realizó la micro-refactorización **Extract Method** → `calcularTotal(r)` (`554b830`).

### ☑ Commits incrementales

```
1ac6c69  refactor: introducir enum TipoReserva
db31780  refactor: extract class NotificadorReserva
fce54f9  refactor: extract class ValidadorReserva
b6c0e28  docs:    agregar reflexion tecnica Labs 1 y 2
554b830  refactor: extraer calculo de total
e3e005b  test:    caracterizar comportamiento heredado de reservas
00d7837  chore:   registrar proyecto heredado y linea base
```

Cada commit de refactorización fue precedido y seguido por `mvn clean test` verde.

### ☑ Tabla antes / después

| Dimensión | Antes | Después |
|---|---|---|
| Responsabilidades en `procesar()` | 5 | 1 (orquestar colaboradores) |
| Líneas de `procesar()` | 40 | 7 |
| Bloques `if` en `procesar()` | 5 | 1 |
| Tipo de reserva | `String` libre | `enum TipoReserva` — cerrado en compilación |
| Notificación/persistencia | `println` acoplado | `NotificadorReserva` delegado |
| Validación | Incrustada en el servicio | `ValidadorReserva` independiente |
| Pruebas automatizadas | 0 | 16 — 16/16 verde |

### ☑ Reporte técnico breve

Disponible en: [`Reporte_tecnico_breve.md`](./Reporte_tecnico_breve.md)

Cubre los 13 puntos del enunciado: portada, problema inicial, línea base, diagnóstico,
pruebas, tres refactorizaciones, comparación antes/después, historial Git, conclusiones,
declaración de IA y enlace al repositorio.

### ☑ Declaración de uso de IA

Este trabajo fue realizado con asistencia de **IBM Bob** para ejecución de comandos,
generación de código y redacción de documentación técnica. Todas las decisiones de diseño,
justificaciones de refactorización y análisis de smells fueron revisadas y validadas por
el estudiante. El código fue ejecutado y verificado en entorno local.

---

## Historial Git completo

```bash
git log --oneline
```

```
1ac6c69  refactor: introducir enum TipoReserva
db31780  refactor: extract class NotificadorReserva
fce54f9  refactor: extract class ValidadorReserva
b6c0e28  docs:    agregar reflexion tecnica Labs 1 y 2
554b830  refactor: extraer calculo de total
e3e005b  test:    caracterizar comportamiento heredado de reservas
00d7837  chore:   registrar proyecto heredado y linea base
```
