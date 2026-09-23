# Reflexión técnica final

## Laboratorio 1 y 2 — Diagnóstico, caracterización y primera refactorización protegida

---

### 1. ¿Qué problema de diseño genera mayor riesgo y por qué?

El problema de mayor riesgo es la **mezcla de responsabilidades en `ServicioReservas.procesar()`**.
Un único método concentra validación de entradas, cálculo de precio, simulación de persistencia,
simulación de notificación y cambio de estado de dominio. Esto produce cinco razones independientes
de cambio en la misma clase: si la política de precios cambia, si el canal de notificación cambia
o si las reglas de validación evolucionan, todas las modificaciones convergen en el mismo punto.
El riesgo concreto es que un cambio en una responsabilidad puede romper silenciosamente las demás
sin que el código de producción falle de inmediato, porque no existía ninguna prueba que lo detectara.

### 2. ¿Qué problema parece fácil de corregir pero podría alterar comportamiento?

La validación de correo en `ServicioReservas` líneas 22–23 parece trivial: reemplazar
`contains("@")` por un Value Object `Correo` con una regex más estricta. Sin embargo, si ese
constructor valida también longitud máxima o dominio (`@uees.edu.ec`), valores que hoy superan
la validación dejarían de ser aceptados. Eso no es refactorización, es un cambio funcional
encubierto. La prueba `correoInvalidoNoProcesaReserva()` lo detectaría solo si el correo de
prueba deja de construirse; si el contrato cambia en el sentido opuesto (más restrictivo),
las pruebas existentes podrían seguir verdes mientras nuevos casos fallan en producción.

### 3. ¿Qué pruebas son indispensables antes de tocar el código?

Las siete pruebas de caracterización implementadas en `ServicioReservasTest` son la red mínima:
los dos casos del camino feliz (`normalActualmenteRetornaCuarenta`, `vipActualmenteRetornaTreintaYCuatro`),
los tres casos de rechazo (`correoInvalidoNoProcesaReserva`, `periodoConFinAnteriorNoProcesa`,
`reservaNulaRetornaCero`) y los dos casos de frontera (`dosHorasExactasPermitenProcesar`,
`unaHoraNoPermiteProcesar`). Los casos de frontera son especialmente valiosos porque las
refactorizaciones de condicionales tienden a introducir errores de `<` vs `<=`.

### 4. ¿Qué responsabilidad moverías primero?

El cálculo del precio, que ya fue extraído como `calcularTotal(Reserva r)` en el Laboratorio 2.
Es la responsabilidad de menor riesgo: no cambia la firma pública de la clase, no afecta
ningún colaborador externo y el resultado matemático `40 × 0.85 = 34` es verificable de forma
directa con una sola prueba.

### 5. ¿Qué evidencia usarías para defender esa decisión?

La evidencia es el resultado de `mvn clean test` antes y después del cambio: **10/10 pruebas
verdes en ambos momentos**. Adicionalmente, la regresión intencional —cambiar `0.85` por `0.80`—
produjo inmediatamente el fallo `expected: <34.0> but was: <32.0>` en
`vipActualmenteRetornaTreintaYCuatro`, lo que confirma que la prueba detecta exactamente el
comportamiento que pretende proteger. Esa capacidad de detectar el error demuestra que la prueba
no es decorativa: reacciona ante el cambio que importa.

### 6. ¿Qué diferencia existe entre refactorizar y realizar un cambio funcional?

Una **refactorización** modifica la estructura interna del código sin alterar su comportamiento
observable: el conjunto de entradas, salidas y efectos secundarios visible desde fuera permanece
idéntico. La extracción de `calcularTotal()` es un ejemplo: el método `procesar()` sigue
retornando `40.0` para NORMAL y `34.0` para VIP, el estado de `Reserva` sigue siendo `CONFIRMADA`
en los mismos casos y los mensajes en consola siguen apareciendo en el mismo orden. Nada visible
cambió. Un **cambio funcional**, en contraste, modifica deliberadamente al menos un comportamiento
observable: añadir un tipo de reserva nuevo, cambiar el porcentaje de descuento o hacer que un
correo inválido lance excepción en lugar de retornar `0.0`. La distinción importa porque un cambio
funcional requiere una decisión explícita y documentada; si ocurre accidentalmente durante una
refactorización, es una regresión, y la suite de pruebas es el mecanismo que lo hace visible antes
de que llegue a producción.

---

## Checklist de entrega

- [x] Proyecto base compila y ejecuta (`mvn clean compile` → BUILD SUCCESS).
- [x] Seis escenarios de línea base (LB-01 a LB-06 con evidencia real en `01_LINEA_BASE.md`).
- [x] Mapa de responsabilidades (`02_MAPA_RESPONSABILIDADES.md` — 5 razones de cambio).
- [x] Mínimo cinco problemas diagnosticados (`03_MATRIZ_DIAGNOSTICO.md` — 6 smells con evidencia).
- [x] Matriz de riesgo (`04_MATRIZ_RIESGO.md` — 5 cambios evaluados).
- [x] Pruebas propuestas (`05_PRUEBAS_PROPUESTAS.md` — 10 especificaciones AAA).
- [x] Plan priorizado (`06_PLAN_REFACTORIZACION.md` — 6 pasos con justificación y dependencias).
- [x] Commit Git del estado inicial (`00d7837 chore: registrar proyecto heredado y linea base`).
- [x] Suite JUnit 5 verde (`e3e005b test: caracterizar comportamiento heredado de reservas` — 10/10).
- [x] Micro-refactorización protegida (`554b830 refactor: extraer calculo de total` — 10/10).
- [x] Regresión intencional detectada (`expected: <34.0> but was: <32.0>`).
- [x] Reflexión técnica (esta sección).
