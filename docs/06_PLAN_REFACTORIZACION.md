# Fase L | Plan priorizado de refactorización

No se implementa todavía. El objetivo es proponer un orden incremental y justificado.

| Orden | Cambio | Por qué primero / después | Pruebas requeridas antes de aplicar | Dependencias |
|---:|---|---|---|---|
| 1 | Caracterizar los seis escenarios con pruebas JUnit 5 (red de seguridad) | Sin pruebas, cualquier cambio posterior es ciego. Es el prerequisito de todos los demás pasos. Riesgo de romper: ninguno — no toca código de producción. | — (este paso crea las pruebas) | Ninguna |
| 2 | Separar cálculo de tarifa: extraer método privado `calcularTotal(String tipo)` en `ServicioReservas` | Riesgo bajo (cambio local). Mejora la legibilidad del flujo principal y prepara la extracción futura de `CalculadorTarifa`. Requiere solo LB-01 y LB-02 cubiertos. | `vipConservaResultadoActual()`, `normalConservaBase40()` | Pruebas del paso 1 (LB-01, LB-02) |
| 3 | Aislar notificación y persistencia: introducir interfaces `Notificador` y `Repositorio`, inyectarlas en `ServicioReservas` | Elimina dependencia de consola; hace el servicio testeable sin `println`. Es el paso de mayor impacto en testabilidad y menor riesgo funcional si se introduce con mocks sencillos. | `reservaValidaSeConfirma()`, `reservaValidaNormalRetorna40()` | Paso 2 completado; cobertura LB-01 a LB-06 |
| 4 | Introducir `TipoReserva` (enum) y `PeriodoReserva` (Value Object) | Los Value Objects son el cambio de mayor riesgo estructural: modifican el constructor de `Reserva` y trasladan invariantes. Se aplican al final, cuando la red de pruebas ya está completa y el servicio ya tiene responsabilidades reducidas. | `periodoInvalidoNoProcesa()`, `tipoVipAplicaDescuento()`, todos los LB- | Pasos 1, 2 y 3 completados |

## Justificación del orden

1. **Pruebas primero** — ningún refactoring es seguro sin una red de seguridad que verifique el contrato observable. La línea base (seis escenarios) se convierte en seis pruebas JUnit antes de tocar cualquier clase.

2. **Del menor al mayor riesgo** — se empieza por el cambio más local (extraer un método dentro de la misma clase) y se termina por el que modifica el modelo de datos (`PeriodoReserva`), que tiene el mayor impacto en cascada.

3. **Testabilidad incremental** — aislar efectos secundarios (paso 3) desbloquea las pruebas de comportamiento que hoy son difíciles de escribir por los `println` embebidos.

4. **Los Value Objects al final** — `PeriodoReserva` y `TipoReserva` son mejoras de modelado de alto valor, pero requieren adaptar el constructor de `Reserva` y todos los sitios de construcción. Hacerlos cuando ya hay cobertura completa minimiza el riesgo de regresiones silenciosas.
