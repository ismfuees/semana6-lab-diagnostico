# Fase I | Matriz de diagnóstico obligatoria

Mínimo cinco filas con evidencias reales del proyecto.
Cada fila incluye ubicación exacta, evidencia de código, categoría, impacto y prueba verificadora.

| # | Ubicación | Smell / problema | Categoría | Impacto | Candidato de refactorización | Prueba necesaria |
|---:|---|---|---|---|---|---|
| 1 | `ServicioReservas.java` L14–54 — método `procesar()` completo | **Long Method / God Method**: un único método mezcla validación, cálculo de precio, persistencia simulada, notificación simulada y cambio de estado | Diseño de clases | Alto — cinco razones independientes de cambio; cualquier modificación en precio, correo, persistencia o notificación toca el mismo método | Extraer métodos con intención: `esEntradaValida()`, `calcularTotal()`, delegar persistencia y notificación | `normalValidaRetorna40()`, `vipValidaRetorna34()` verifican que el contrato observable no cambia tras la extracción |
| 2 | `ServicioReservas.java` L27–30 — condición de periodo | **Data Clumps + Primitive Obsession**: `inicio` y `fin` se validan siempre juntos (`r.getInicio() == null \|\| r.getFin() == null \|\| !r.getFin().isAfter(r.getInicio())`) pero la invariante vive fuera de los datos que la generan | Datos | Alto — la triple condición se duplicará en cada servicio que opere con `Reserva`; un error en una copia no afecta las demás | Introducir Value Object `PeriodoReserva(inicio, fin)` que garantice `fin > inicio` en su constructor | `periodoInvalidoNoProcesa()` — `fin = inicio` retorna `0.0` y estado `PENDIENTE` |
| 3 | `ServicioReservas.java` L39 — `"VIP".equals(r.getTipo())` | **Primitive Obsession sobre `String tipo`**: el tipo de reserva se representa como `String` libre; la comparación literal es frágil y la estructura crecerá linealmente con cada nuevo tipo | Datos / Condicionales | Alto — añadir `PREMIUM` o `BECADO` requiere un nuevo `else if` aquí; un error tipográfico (`"vip"`) produce descuento incorrecto sin fallo visible | Convertir `String tipo` en enum `TipoReserva {NORMAL, VIP}`; extraer la política de descuento | `tipoVipAplicaDescuento()`, `tipoNormalNoCambiaTotal()` |
| 4 | `ServicioReservas.java` L43–49 — dos `System.out.println` | **Responsabilidades de infraestructura mezcladas con dominio**: los efectos de persistencia y notificación están implementados como `println` directos sin contrato intermedio; no son inyectables ni verificables en pruebas | Diseño de clases | Medio — no se puede reemplazar la persistencia por un repositorio real ni verificar en tests que el correo fue "enviado"; cualquier cambio de canal obliga a abrir `ServicioReservas` | Extraer interfaces `RepositorioReservas` y `ServicioNotificacion`; inyectarlas en el constructor | `reservaValidaSeConfirma()` — tras separar, el estado sigue siendo `CONFIRMADA` |
| 5 | `ServicioReservas.java` L22–23 — `r.getCorreo() == null \|\| !r.getCorreo().contains("@")` | **Primitive Obsession sobre `String correo`**: la regla de formato del correo vive en el servicio, no en el dato; `contains("@")` es una validación incompleta que puede crecer | Datos | Medio — si se añade validación de dominio o longitud máxima, hay que buscar y modificar esta condición en cada servicio que reciba correos; riesgo de Shotgun Surgery | Introducir Value Object `Correo` que valide formato en su constructor | `correoInvalidoNoProcesa()` — correo sin `@` retorna `0.0` y estado `PENDIENTE` |
| 6 | `Reserva.java` L14–26 — constructor con cinco parámetros posicionales | **Long Parameter List**: `new Reserva(id, correo, inicio, fin, tipo)` — cinco parámetros del mismo tipo abstracto; `inicio` y `fin` son ambos `LocalDateTime`, intercambiarlos compila sin error | Datos | Medio — un error de orden en la construcción produce un objeto semánticamente inválido sin excepción en compilación ni en ejecución; difícil detectar en revisión de código | Agrupar `inicio`+`fin` en `PeriodoReserva`; convertir `tipo` en enum; a largo plazo considerar Builder | `periodoInvalidoNoProcesa()` y `normalValidaRetorna40()` — cualquier agrupación de parámetros debe preservar los resultados de la línea base |

---

## Resumen por categoría

| Categoría | Smells encontrados |
|---|---|
| Diseño de clases | Long Method / God Method (#1), Infraestructura mezclada (#4) |
| Datos | Data Clumps + Primitive Obsession en periodo (#2), Primitive Obsession en tipo (#3), Primitive Obsession en correo (#5), Long Parameter List (#6) |
| Condicionales | Literal `String` frágil con crecimiento lineal (parte de #3) |

## Smells no aplicables en este proyecto

- **Duplicate Code:** no hay repetición hoy (un solo servicio); el riesgo es futuro.
- **Speculative Generality:** no hay abstracciones vacías ni hooks sin uso.
- **Dead Code:** todos los caminos del método son alcanzables (verificado en línea base).
