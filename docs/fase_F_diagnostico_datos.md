# Fase F | Diagnóstico de datos

## 9.1 Primitive Obsession

Cada dato primitivo se analiza preguntando:
**¿existe una invariante, regla o comportamiento que justifique un concepto explícito?**

| Dato actual | Clase | Línea | Concepto posible | Regla que lo justifica | ¿Justifica Value Object? |
|---|---|---|---|---|---|
| `String correo` | `Reserva.java` | L8 | `Correo` | No puede ser nulo, debe contener `@`; en el futuro podría requerir dominio específico | ✅ Sí — tiene invariante de formato |
| `String tipo` | `Reserva.java` | L11 | `TipoReserva` (enum) | Solo existen valores conocidos (`NORMAL`, `VIP`); un `String` libre acepta cualquier valor sin control | ✅ Sí — conjunto cerrado de valores |
| `LocalDateTime inicio` + `LocalDateTime fin` | `Reserva.java` | L9–10 | `PeriodoReserva` | `fin` debe ser posterior a `inicio`; la invariante se valida hoy fuera de los datos, en `ServicioReservas` L27–30 | ✅ Sí — invariante compartida entre dos campos |
| `double total` | `ServicioReservas.java` | L37 | `Dinero` / `Tarifa` | No puede ser negativo; en el futuro podría necesitar moneda y redondeo | ⚠️ Dudoso — en el código actual no hay operación que lo justifique todavía |
| `int horasAnticipacion` | `ServicioReservas.java` | L16 | `Anticipacion` | Tiene un umbral mínimo de 2h; podría convertirse en política | ⚠️ Dudoso — regla simple, bajo costo de duplicar |

**Criterio aplicado:** solo se propone Value Object cuando existe al menos una invariante
comprobable hoy en el código. `double total` e `int horasAnticipacion` no cumplen ese
umbral con la información actual.

---

## 9.2 Data Clumps

`inicio` y `fin` siempre viajan juntos y comparten una regla:

```java
// Reserva.java — L9-10: declarados juntos
private final LocalDateTime inicio;
private final LocalDateTime fin;

// ServicioReservas.java — L27-30: validados juntos
if (r.getInicio() == null
        || r.getFin() == null
        || !r.getFin().isAfter(r.getInicio())) {
    return 0;
}
```

**Señales de Data Clumps confirmadas:**

| Señal | Evidencia |
|---|---|
| Los dos campos aparecen declarados juntos | `Reserva.java` L9–10 |
| Los dos campos se validan siempre en conjunto | `ServicioReservas.java` L27–30 |
| La regla `fin > inicio` no vive dentro de ninguno de los dos datos | La invariante está dispersa en el servicio, no en los datos que la generan |
| Si se añade un tercer servicio que use `Reserva`, deberá repetir la misma validación | No existe un lugar canónico donde viva la regla |

**Candidato:** Value Object `PeriodoReserva(LocalDateTime inicio, LocalDateTime fin)`
que garantice `fin > inicio` en su constructor y exponga solo operaciones con semántica
de periodo (duración, solapamiento, etc.).

---

## 9.3 Long Parameter List

Constructor de `Reserva` con cinco parámetros posicionales:

```java
// Reserva.java — L14-19
public Reserva(
        String id,
        String correo,
        LocalDateTime inicio,
        LocalDateTime fin,
        String tipo) { … }
```

**Análisis:**

| Problema | Detalle |
|---|---|
| Cinco parámetros posicionales | El compilador no detecta si se intercambian `inicio` y `fin` — ambos son `LocalDateTime` |
| `String correo` y `String tipo` consecutivos | Intercambiarlos compila sin error pero produce un objeto semánticamente inválido |
| Tres de los cinco parámetros son candidatos a Value Objects | `correo` → `Correo`, `inicio`+`fin` → `PeriodoReserva`, `tipo` → `TipoReserva` |
| La lista crecerá | Si se añaden atributos (aula, duración, profesor) el constructor se vuelve ilegible sin un Builder |

**Refactorizaciones candidatas:**
- Introducir `PeriodoReserva` reduce de 5 a 4 parámetros y elimina el par ambiguo.
- Introducir `TipoReserva` (enum) elimina el `String tipo` libre.
- A largo plazo, un Builder o Factory Method mejoraría la construcción expresiva.
