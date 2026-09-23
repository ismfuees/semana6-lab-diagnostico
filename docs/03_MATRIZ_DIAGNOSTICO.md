# Fase I | Matriz de diagnóstico obligatoria

Diagnósticos basados en evidencias reales del código heredado (`ServicioReservas.java`, `Reserva.java`).

| # | Ubicación | Smell / problema | Categoría | Impacto | Candidato a refactorizar | Prueba necesaria |
|---:|---|---|---|---|---|---|
| 1 | `ServicioReservas.procesar()` | **God Class / Long Class** — el método único acumula validación, cálculo de precio, persistencia simulada y notificación. Cuatro responsabilidades no relacionadas conviven en ~30 líneas. | Diseño de clases | Cualquier cambio en precio, correo, persistencia o validación obliga a tocar el mismo archivo, aumentando la probabilidad de romper algo no relacionado. | Extraer clases `ValidadorReserva`, `CalculadorTarifa`, `RepositorioReservas` y `NotificadorReserva`. | `reservaValidaNormalRetorna40()`, `reservaValidaVipRetorna34()`, `reservaValidaSeConfirma()` |
| 2 | `ServicioReservas.procesar()` líneas 39-41 | **Feature Envy** — el servicio consulta `r.getTipo()` para aplicar una política de descuento que podría ser responsabilidad de un objeto `TipoReserva` o de un calculador de tarifa dedicado. | Diseño de clases | Si se agregan más tipos (PREMIUM, CORPORATIVO), el bloque `if` crece; la lógica de precio se dispersa por el servicio en lugar de concentrarse. | Introducir método o clase `CalculadorTarifa.calcular(tipo, base)`. | `vipConservaResultadoActual()`, `normalConservaResultadoActual()` |
| 3 | `Reserva`: campos `inicio` (`LocalDateTime`) + `fin` (`LocalDateTime`) | **Data Clumps / Primitive Obsession** — `inicio` y `fin` siempre viajan juntos y comparten la invariante `fin > inicio`. Sin embargo, se modelan como dos campos sueltos; la regla se valida fuera, en `ServicioReservas`. | Datos | Si la regla `fin > inicio` se replica en otro servicio o consulta, puede validarse de forma inconsistente. El concepto "periodo" no tiene representación explícita. | Introducir Value Object `PeriodoReserva(inicio, fin)` con la invariante encapsulada en su constructor. | `periodoInvalidoNoProcesa()`, `periodoConFinIgualNoProcesa()` |
| 4 | `Reserva`: campo `tipo` (`String`) | **Primitive Obsession** — el tipo de reserva se almacena como `String` libre. La regla `"VIP".equals(r.getTipo())` en `ServicioReservas` implica un valor mágico que podría escribirse mal en cualquier llamada. | Datos | Typos en el valor `"VIP"` no son detectados en compilación; agregar un tipo nuevo requiere buscar todos los `if` con `String`. | Convertir a `enum TipoReserva { NORMAL, VIP }`. | `tipoVipAplicaDescuento()`, `tipoNormalNoCausa excepcionDeCompilacion()` |
| 5 | `ServicioReservas.procesar()` líneas 22-23 | **Magic Literal / Regla de correo frágil** — la validación de correo se reduce a `contains("@")`. Cualquier cadena como `"@"` o `"a@"` pasa la validación. La regla no tiene nombre ni está encapsulada. | Condicionales | Si mañana se necesita una validación más estricta (dominio, longitud mínima) hay que modificar el servicio directamente y volver a probar todos los escenarios relacionados. | Extraer método `esCorreoValido(String)` o introducir Value Object `Correo`. | `correoConSoloArrobaNoDebeProcesar()`, `correoNuloNoProcesa()` |
| 6 | `ServicioReservas.procesar()` — guardas encadenadas + cálculo + efectos en un solo flujo | **Shotgun Surgery potencial** — las validaciones de correo y periodo no son reutilizables; si otro servicio necesita validar lo mismo, duplicará el código. Además, mezcla validación, cálculo y efectos secundarios (console output) sin separación de fases. | Condicionales / diseño | Un cambio en la regla de anticipación (de 2 h a 4 h) requiere modificar el interior del método sin ninguna guía estructural; no hay forma de probar el cálculo VIP sin pasar por todas las validaciones. | Separar fase de validación de fase de cálculo; aislar efectos secundarios tras una interfaz. | `horasAnticipacionExactaDosPermiteProcesar()`, `unaHoraNoPermiteProcesar()` |

## Notas de diagnóstico (Fases E, F, G)

### Fase E — Diagnóstico de clases

| Señal (Long Class) | ¿Aparece? | Evidencia |
|---|---|---|
| Responsabilidades de dominio e infraestructura mezcladas | ✅ Sí | Cálculo de precio (dominio) convive con `println` de persistencia/notificación (infraestructura) en el mismo método. |
| Varios motivos para cambiar | ✅ Sí | Política de precio, canal de notificación, reglas de validación y persistencia son razones independientes. |
| Método principal con demasiadas decisiones | ✅ Sí | `procesar()` tiene 4 guardas + 1 cálculo condicional + 2 efectos secundarios + 1 llamada de estado. |
| Dependencias futuras difíciles de aislar | ✅ Sí | No existe interfaz para persistencia ni para notificación; reemplazarlos requiere modificar el método. |

### Fase F — Feature Envy

`ServicioReservas` invoca `r.getCorreo()`, `r.getInicio()`, `r.getFin()` y `r.getTipo()` para aplicar reglas que conceptualmente pertenecen a objetos de dominio (`PeriodoReserva`, `Correo`, `TipoReserva`). No significa que todo deba moverse a `Reserva`; las *políticas* de negocio (descuento VIP, anticipación mínima) pueden permanecer en el servicio, pero las *invariantes de datos* (formato de correo, periodo válido) deberían vivir en los propios objetos.

### Fase G — Diagnóstico de condicionales

| Condición | Regla expresada | Riesgo de mantenimiento |
|---|---|---|
| `r == null` | No procesar ausencia de reserva | Bajo aislado; pero si se agrega logging el condicional crece. |
| `correo == null \|\| !correo.contains("@")` | Correo inválido | Medio: la regla es débil (solo verifica `@`) y no está encapsulada; puede duplicarse. |
| `fin <= inicio` | Periodo inválido | Medio: la invariante pertenece al concepto "periodo" y no al servicio. |
| `horasAnticipacion < 2` | Anticipación insuficiente | Medio: el literal `2` es un magic number; un cambio de política requiere editar el método. |
| `"VIP".equals(r.getTipo())` | Aplicar descuento VIP | Alto: String mágico, no compila al introducir errores tipográficos; crecerá si hay más tipos. |
