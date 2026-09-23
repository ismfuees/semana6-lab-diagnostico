# Fase G | Diagnóstico de condicionales

Los cinco condicionales de `ServicioReservas.procesar()` se analizan individualmente.
Para cada uno se responden las cinco preguntas del laboratorio.

---

## Tabla de condicionales

| # | Línea | Condición | Regla expresada | Riesgo de mantenimiento |
|---|---|---|---|---|
| C1 | L18 | `r == null` | No procesar ausencia de reserva | Bajo aislado; alto si otros métodos repiten la guardia |
| C2 | L22–23 | `r.getCorreo() == null \|\| !r.getCorreo().contains("@")` | Correo inválido (nulo o sin `@`) | Medio — la regla de formato puede crecer (dominio, longitud) y está dispersa |
| C3 | L27–30 | `r.getInicio() == null \|\| r.getFin() == null \|\| !r.getFin().isAfter(r.getInicio())` | Periodo inválido (nulos o fin ≤ inicio) | Alto — tres sub-condiciones acopladas que deberían vivir en `PeriodoReserva` |
| C4 | L33 | `horasAnticipacion < 2` | Anticipación insuficiente | Medio — el umbral `2` es un número mágico sin nombre; cambiar la política obliga a buscar el literal |
| C5 | L39 | `"VIP".equals(r.getTipo())` | Aplicar descuento VIP | Alto — compara `String` libre; cada nuevo tipo requiere un `else if` adicional aquí |

---

## Análisis detallado por condicional

### C1 — `r == null` (L18)

```java
if (r == null) {
    return 0;
}
```

| Pregunta | Respuesta |
|---|---|
| ¿Expresa una regla de negocio con nombre? | No — es una guardia técnica de robustez, no una regla del dominio |
| ¿Se repite? | No en el código actual, pero se repetiría en cualquier otro método que reciba `Reserva` |
| ¿Oculta el flujo principal? | Marginalmente — es el primer `return` temprano |
| ¿Mezcla validación con cálculo y efectos? | No, está aislado al inicio |
| ¿La variante crecerá? | Poco probable |

**Veredicto:** guardia defensiva aceptable. Riesgo bajo hoy; no priorizar.

---

### C2 — Correo inválido (L22–23)

```java
if (r.getCorreo() == null
        || !r.getCorreo().contains("@")) {
    return 0;
}
```

| Pregunta | Respuesta |
|---|---|
| ¿Expresa una regla de negocio con nombre? | Sí — "el correo debe tener formato válido" es una regla del dominio |
| ¿Se repite? | Potencialmente en cualquier otro servicio que valide correos |
| ¿Oculta el flujo principal? | Sí — es uno de cuatro `return 0` que fragmentan el camino feliz |
| ¿Mezcla validación con cálculo y efectos? | Sí — esta validación convive en el mismo método con el cálculo de precio y los `println` |
| ¿La variante crecerá? | Sí — es probable añadir validación de dominio (`@uees.edu.ec`), longitud máxima, etc. |

**Veredicto:** regla con nombre propio que debería vivir en un Value Object `Correo`.
Riesgo medio-alto de duplicación al crecer el sistema.

---

### C3 — Periodo inválido (L27–30)

```java
if (r.getInicio() == null
        || r.getFin() == null
        || !r.getFin().isAfter(r.getInicio())) {
    return 0;
}
```

| Pregunta | Respuesta |
|---|---|
| ¿Expresa una regla de negocio con nombre? | Sí — "el periodo debe ser válido: fin posterior a inicio" |
| ¿Se repite? | La misma triple condición se repetiría en cualquier consulta o reporte que use el par inicio/fin |
| ¿Oculta el flujo principal? | Sí — tres sub-condiciones acopladas en un único `if` |
| ¿Mezcla validación con cálculo y efectos? | Sí — está incrustada en el mismo flujo que calcula el precio y llama a `println` |
| ¿La variante crecerá? | Sí — podrían añadirse reglas de duración mínima, horarios permitidos, solapamiento |

**Veredicto:** es el condicional de mayor complejidad interna. Las tres sub-condiciones
son una invariante del par `inicio`/`fin` que debería garantizarse en el constructor de
`PeriodoReserva`, no comprobarse externamente. **Riesgo alto.**

---

### C4 — Anticipación insuficiente (L33)

```java
if (horasAnticipacion < 2) {
    return 0;
}
```

| Pregunta | Respuesta |
|---|---|
| ¿Expresa una regla de negocio con nombre? | Sí — "se requieren al menos 2 horas de anticipación" |
| ¿Se repite? | No hoy, pero el literal `2` podría aparecer en tests u otros servicios |
| ¿Oculta el flujo principal? | Levemente — es otro `return 0` temprano |
| ¿Mezcla validación con cálculo y efectos? | Sí — está incrustado junto al cálculo y los efectos secundarios |
| ¿La variante crecerá? | Posible — podría variar por tipo de reserva (VIP necesita 4h, NORMAL 2h) |

**Veredicto:** el número mágico `2` es el problema principal. Extraer una constante
con nombre (`ANTICIPACION_MINIMA_HORAS`) o una política es el cambio de menor riesgo
y mayor claridad. Riesgo medio.

---

### C5 — Descuento VIP (L39)

```java
if ("VIP".equals(r.getTipo())) {
    total = total * 0.85;
}
```

| Pregunta | Respuesta |
|---|---|
| ¿Expresa una regla de negocio con nombre? | Sí — "reservas VIP tienen 15 % de descuento" |
| ¿Se repite? | El literal `"VIP"` podría aparecer en tests, reportes u otros servicios |
| ¿Oculta el flujo principal? | Sí — interrumpe el cálculo con una rama condicional |
| ¿Mezcla validación con cálculo y efectos? | Sí — está embebido entre las validaciones (arriba) y los efectos (abajo) |
| ¿La variante crecerá? | Alta probabilidad — añadir `PREMIUM`, `BECADO` o descuentos escalonados requeriría `else if` aquí |

**Veredicto:** comparar `String` libre es frágil (distingue mayúsculas, acepta errores
tipográficos). La estructura `if`/`else if` crecerá de forma lineal con cada nuevo tipo.
Candidato a polimorfismo o estrategia de tarificación. **Riesgo alto.**

---

## Resumen de riesgos por condicional

| Condicional | Riesgo | Razón principal |
|---|---|---|
| C1 — `r == null` | Bajo | Guardia técnica, no crece |
| C2 — correo inválido | Medio | Regla con nombre, probable crecimiento de formato |
| C3 — periodo inválido | Alto | Triple condición acoplada, invariante fuera del dato |
| C4 — anticipación < 2 | Medio | Número mágico, puede variar por tipo |
| C5 — tipo VIP | Alto | `String` libre + estructura que crece linealmente con nuevos tipos |
