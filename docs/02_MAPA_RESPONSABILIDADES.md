# Fase D | Mapa actual de responsabilidades

## Tabla de responsabilidades

| Fragmento de código | Responsabilidad observada | Clase actual |
|---|---|---|
| `if (r == null) return 0` | Guardia contra referencia nula | `ServicioReservas` |
| `if (!r.getCorreo().contains("@")) return 0` | Validación de formato de correo | `ServicioReservas` |
| `if (!r.getFin().isAfter(r.getInicio())) return 0` | Validación de periodo (fin > inicio) | `ServicioReservas` |
| `if (horasAnticipacion < 2) return 0` | Validación de anticipación mínima | `ServicioReservas` |
| `double total = 40` | Precio base de la reserva | `ServicioReservas` |
| `if ("VIP".equals(r.getTipo())) total = total * 0.85` | Cálculo de descuento por tipo VIP | `ServicioReservas` |
| `System.out.println("Guardando reserva …")` | Persistencia simulada | `ServicioReservas` |
| `System.out.println("Correo enviado a …")` | Notificación simulada | `ServicioReservas` |
| `r.confirmar()` | Ordenar cambio de estado de dominio | `ServicioReservas` → `Reserva` |
| `estado = EstadoReserva.CONFIRMADA` | Mantener estado propio | `Reserva` |

## Mapa conceptual

```text
ServicioReservas
├── valida referencia nula
├── valida formato de correo
├── valida periodo (fin > inicio)
├── valida anticipación mínima (≥ 2h)
├── conoce el precio base (40)
├── calcula descuento VIP (× 0.85)
├── simula persistencia (println)
├── simula notificación por correo (println)
└── ordena confirmar → Reserva

Reserva
└── mantiene su propio estado (PENDIENTE → CONFIRMADA)
```

## Razones de cambio de ServicioReservas

`ServicioReservas` tiene **al menos cinco razones independientes** para cambiar:

| # | Razón de cambio | Ejemplo concreto |
|---|---|---|
| 1 | Cambio en las reglas de validación | Añadir validación de dominio del correo o cambiar anticipación mínima a 3h |
| 2 | Cambio en la política de precios | Añadir tipo PREMIUM, modificar el descuento VIP o introducir precio variable |
| 3 | Cambio en el mecanismo de persistencia | Reemplazar `println` por una llamada a base de datos o repositorio |
| 4 | Cambio en el canal de notificación | Reemplazar `println` por un servicio de email real o mensajería |
| 5 | Cambio en el flujo de confirmación | Añadir lógica de auditoría, eventos de dominio o confirmación diferida |

> **Principio violado:** cada una de estas razones debería pertenecer a una clase o componente separado.
> Tener cinco razones de cambio en una sola clase es la evidencia central del smell **Long Class / God Method**.

## Observación sobre Feature Envy

`ServicioReservas` interroga a `Reserva` en cuatro puntos distintos
(`getCorreo()`, `getInicio()`, `getFin()`, `getTipo()`) para aplicar reglas
que podrían encapsularse en la propia `Reserva` o en un concepto de dominio dedicado.
Sin embargo, no toda esa lógica le pertenece a `Reserva`:
la política de precio y los canales de persistencia/notificación son responsabilidades
de infraestructura o de política, no del objeto de dominio.
