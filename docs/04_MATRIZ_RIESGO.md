# Fase J | Matriz de riesgo

Evaluación de cada cambio candidato antes de decidir un orden de refactorización.

| Cambio candidato | Probabilidad de romper | Impacto si rompe | Riesgo | Cómo reducirlo |
|---|---|---|---|---|
| Extraer clase de notificación | Baja — el comportamiento observable (reserva confirmada, retorno correcto) no cambia; solo se mueve el `println`. | Alto — si la notificación deja de enviarse o se envía duplicada, el contrato de "correo enviado a …" falla. | **Medio** | Crear prueba `reservaValidaSeConfirma()` que verifique estado CONFIRMADA y retorno antes de mover el código. Introducir interfaz `Notificador` inyectable. |
| Introducir `Correo` (Value Object) | Media — cambia la firma del constructor de `Reserva` y cualquier lugar que construya una reserva debe adaptarse. | Medio — si la validación del VO es más estricta que el `contains("@")` actual, casos que hoy pasan podrían fallar. | **Medio** | Cubrir LB-03 y variantes de correo con pruebas caracterizadoras antes de cambiar. Mantener la misma regla de validación inicialmente. |
| Introducir `PeriodoReserva` (Value Object) | Media — modifica el constructor de `Reserva` y la validación en `ServicioReservas` desaparece (se traslada al VO). | Alto — si la invariante `fin > inicio` se implementa de forma diferente, LB-04 y LB-05 pueden comportarse distinto. | **Alto** | Cubrir LB-04, LB-05 con pruebas antes de extraer. Validar que el VO lanza excepción en los mismos casos que hoy devuelven `0`. |
| Simplificar validaciones | Media — reorganiza o extrae las 4 guardas; el flujo del método cambia aunque el comportamiento no debería. | Medio — una guarda mal ordenada o con condición alterada hace que casos inválidos se procesen o viceversa. | **Medio** | Cubrir los seis escenarios de la línea base como pruebas antes de tocar las guardas. |
| Separar cálculo VIP | Baja — es un cambio local dentro de `procesar()`; puede extraerse a un método privado o clase sin alterar la signatura pública. | Medio — si el factor `0.85` se escribe mal o se aplica dos veces, LB-02 deja de retornar `34.0`. | **Bajo** | Crear `vipConservaResultadoActual()` que afirme `34.0` antes de mover el bloque. |

## Escala

- **Bajo:** cambio local, comportamiento bien entendido y prueba fácil de crear.
- **Medio:** afecta varias decisiones o requiere adaptar construcción de objetos.
- **Alto:** puede alterar contrato observable, flujos de error o efectos externos.

## Conclusión de riesgo relativo

El cambio de **mayor riesgo** es introducir `PeriodoReserva` porque modifica el modelo de datos central (`Reserva`) y traslada la responsabilidad de validación del servicio al objeto, alterando la cadena de errores visible. El de **menor riesgo** es separar el cálculo VIP porque es un cambio completamente local dentro del método y su efecto es verificable con una sola aserción numérica.
