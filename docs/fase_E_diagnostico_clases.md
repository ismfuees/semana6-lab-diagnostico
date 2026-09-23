# Fase E | Diagnóstico de clases

## 8.1 Long Class / demasiadas responsabilidades

El indicador no es el número de líneas sino la cantidad de **razones de cambio**.
`ServicioReservas.procesar()` (líneas 14–54) concentra cinco responsabilidades distintas
en un único método de 40 líneas.

| Señal | ¿Aparece? | Evidencia (línea en ServicioReservas.java) |
|---|---|---|
| Responsabilidades de dominio e infraestructura mezcladas | ✅ Sí | Validación de negocio (L18–35) convive con `println` de persistencia/notificación (L43–49) en el mismo método |
| Varios motivos para cambiar | ✅ Sí | Cambiar precio base (L37), descuento VIP (L39–41), regla de anticipación (L33), canal de notificación (L47–49) o mecanismo de persistencia (L43–45) son cambios independientes que tocan la misma clase |
| Método principal con demasiadas decisiones | ✅ Sí | `procesar()` contiene 5 bloques `if` (L18, L22, L27, L33, L39) más dos efectos secundarios y un retorno calculado |
| Dependencias futuras difíciles de aislar | ✅ Sí | Para reemplazar el `println` de persistencia por un repositorio real habría que modificar `ServicioReservas` directamente, sin contrato intermedio que proteja el resto de la lógica |

**Conclusión:** `ServicioReservas` es un **God Method** contenido en una clase de servicio.
La clase en sí solo tiene un método, pero ese método asume al menos cinco responsabilidades
que deberían pertenecer a colaboradores distintos.

---

## 8.2 Feature Envy

`ServicioReservas.procesar()` consulta datos de `Reserva` en **cuatro puntos distintos**
para aplicar reglas que podrían encapsularse en la propia `Reserva` o en un concepto
de dominio dedicado:

```java
r.getCorreo()   // L22–23 → para validar formato
r.getInicio()   // L27–29 → para validar periodo
r.getFin()      // L28–29 → para validar periodo
r.getTipo()     // L39    → para decidir descuento
```

| Llamada | Regla aplicada | ¿Podría pertenecer a Reserva? | Observación |
|---|---|---|---|
| `r.getCorreo()` | Formato de correo (`contains("@")`) | Parcialmente | La regla de formato pertenece al concepto *Correo*, no a `Reserva` directamente |
| `r.getInicio()` + `r.getFin()` | `fin > inicio` | Sí | La invariante del periodo pertenece al par inicio/fin; candidato a Value Object |
| `r.getTipo()` | Descuento VIP | No | La política de precio es una regla de negocio del servicio, no del objeto de dominio |

**Distinción importante:** no todo debe moverse a `Reserva`.
La validación de formato del correo le corresponde a un concepto `Correo`.
La invariante `fin > inicio` le corresponde a un concepto `PeriodoReserva`.
La política de descuento VIP le corresponde a un componente de tarificación.
`Reserva` solo debería conocer su propio estado.

---

## 8.3 Shotgun Surgery potencial

Si mañana la regla de validación del correo cambia (p. ej., exigir dominio `@uees.edu.ec`)
o la anticipación mínima pasa de 2 a 3 horas, **hay que buscar y modificar esa regla
dentro del cuerpo de `procesar()`**, sin que exista ningún punto de extensión claro.

En un sistema mayor con múltiples servicios que usen `Reserva`, esa misma regla
podría estar copiada en varios lugares. El riesgo de **Shotgun Surgery** es:

| Escenario de cambio | Clases que habría que tocar hoy | Clases que debería tocar con diseño correcto |
|---|---|---|
| Cambiar regla de validación de correo | `ServicioReservas` (y cualquier otra clase que repita la validación) | Solo la clase `Correo` |
| Cambiar anticipación mínima | `ServicioReservas` | Solo la política de validación |
| Cambiar descuento VIP | `ServicioReservas` | Solo la clase de tarificación |

El problema no es crítico hoy (una sola clase), pero **la estructura actual no frena
la propagación**: si se añaden más servicios que operen con `Reserva`, la regla se duplicará
de forma natural porque no existe un lugar canónico donde viva.
