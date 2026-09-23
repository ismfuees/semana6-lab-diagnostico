# Fase D | Mapa actual de responsabilidades

Identificación de responsabilidades concretas **antes** de asignar nombres de smells.

| Fragmento | Responsabilidad observada | Clase actual |
|---|---|---|
| Validar null/correo/periodo/anticipación | Validación de entrada | `ServicioReservas` |
| Calcular total y descuento VIP | Cálculo de tarifa | `ServicioReservas` |
| Imprimir "Guardando reserva" | Persistencia simulada | `ServicioReservas` |
| Imprimir "Correo enviado" | Notificación simulada | `ServicioReservas` |
| Cambiar estado a CONFIRMADA | Cambio de estado de dominio | `Reserva` |

## Mapa conceptual

```text
ServicioReservas
├── valida null de reserva
├── valida correo (formato @)
├── valida periodo (fin > inicio)
├── valida anticipación (>= 2 h)
├── calcula precio base (40)
├── conoce y aplica descuento VIP (15 %)
├── simula persistencia (println)
├── simula notificación por correo (println)
└── ordena confirmar Reserva

Reserva
├── almacena id, correo, inicio, fin, tipo
└── mantiene y transiciona estado (PENDIENTE → CONFIRMADA)
```

## Respuesta a la pregunta clave

`ServicioReservas` tiene **al menos cuatro razones independientes de cambio**:

| Razón de cambio | Qué afecta |
|---|---|
| Cambio en la política de precios o descuentos | Cálculo del total / regla VIP |
| Cambio en las reglas de validación (correo, periodo, anticipación) | Guardas del método `procesar` |
| Cambio en el mecanismo de persistencia (p. ej., base de datos) | El `println` de "Guardando reserva" |
| Cambio en el canal de notificación (p. ej., email real, SMS) | El `println` de "Correo enviado" |

Esto es una señal directa del smell **God Class / Long Class**: una clase que cambia por motivos no relacionados entre sí.
