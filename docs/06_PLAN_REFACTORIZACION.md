# Fase L | Plan priorizado de refactorización

> La evaluación se centra en la **justificación**, no en repetir un orden predefinido.
> Cada paso debe poder ejecutarse de forma independiente sin romper los anteriores.

---

## Plan ordenado

| Orden | Cambio | Por qué en este momento | Pruebas requeridas antes | Dependencias |
|---:|---|---|---|---|
| 1 | Caracterizar la línea base con JUnit 5 (P1–P7) | Sin red de seguridad, todo cambio posterior es especulativo. Las 7 pruebas cubren todos los caminos del método `procesar()` y son el único medio objetivo de verificar que una refactorización no rompe comportamiento | Ninguna (son las primeras) | Ninguna |
| 2 | Extraer método privado `calcularTotal(String tipo)` en `ServicioReservas` | Riesgo bajo (sin cambio de firma pública), mejora la legibilidad del flujo principal y prepara el terreno para el enum `TipoReserva`. No afecta ningún colaborador externo | P1 `vipValidaRetorna34()`, P2 `normalValidaRetorna40()` | Paso 1 completo |
| 3 | Extraer interfaces `RepositorioReservas` y `ServicioNotificacion`; reemplazar `println` por llamadas a colaboradores inyectados | Reduce las responsabilidades de `ServicioReservas` de 5 a 3. El riesgo es bajo porque el contrato de retorno y estado no cambia; solo se reemplaza el canal de efecto secundario | P7 `reservaValidaSeConfirma()` | Paso 1 completo |
| 4 | Convertir `String tipo` en enum `TipoReserva {NORMAL, VIP}` | Elimina el literal `"VIP"` frágil, cierra el conjunto de valores permitidos y habilita polimorfismo futuro. Requiere adaptar el constructor de `Reserva` y todos los puntos de construcción | P1 `vipValidaRetorna34()`, P2 `normalValidaRetorna40()`, luego P10 `tipoVipAplicaDescuento()` | Paso 2 (el método `calcularTotal` usa el tipo) |
| 5 | Introducir Value Object `Correo` con validación de formato en constructor | Encapsula la regla `contains("@")` en el dato que la genera. Riesgo medio: cambia la firma del constructor de `Reserva` y todos sus puntos de construcción; la validación no debe ser más estricta que la actual | P5 `correoInvalidoNoProcesa()`, luego P9 `correoInvalidoNoProcesaConVO()` | Paso 1; independiente de pasos 2–4 |
| 6 | Introducir Value Object `PeriodoReserva(inicio, fin)` que garantice `fin > inicio` en constructor | Elimina la triple condición de `ServicioReservas` L27–30 y garantiza la invariante en el dato. Es el cambio de mayor riesgo: modifica la firma de `Reserva`, obliga a decidir si el rechazo se comunica con retorno `0.0` o con excepción, y afecta todos los constructores | P6 `periodoInvalidoNoProcesa()`, luego P8 `periodoInvalidoNoProcesaConVO()` | Pasos 1 y 5 (reducen el acoplamiento del constructor antes de este cambio) |

---

## Justificación del orden

### ¿Por qué primero las pruebas (paso 1)?
Cada cambio posterior será verificable en segundos. Sin las pruebas, el único medio de
comprobar que no se rompió nada es ejecutar `Main` manualmente y comparar con la línea base —
lo que es lento, propenso a olvidos y no reproducible en CI. Las pruebas convierten la
línea base en una red de seguridad automática.

### ¿Por qué el cálculo antes que los Value Objects (paso 2 antes de 5–6)?
Extraer `calcularTotal()` es un cambio puramente interno a `ServicioReservas` que no toca
ningún colaborador. Su riesgo es el más bajo de todos y su beneficio inmediato es que el
método `procesar()` queda más legible, lo que facilita entender el impacto de los pasos
siguientes.

### ¿Por qué la notificación en paso 3?
Separar los `println` desacopla la infraestructura del dominio sin tocar el contrato de
`Reserva`. Es el segundo cambio de menor riesgo. Ejecutarlo antes de los Value Objects
significa que, cuando se modifique el constructor de `Reserva` (pasos 5–6), `ServicioReservas`
ya tendrá una estructura más limpia y los puntos de cambio serán menores.

### ¿Por qué `Correo` antes de `PeriodoReserva` (paso 5 antes de 6)?
`Correo` afecta un solo campo del constructor de `Reserva`. `PeriodoReserva` afecta dos
campos y además elimina una triple condición del servicio, lo que requiere mayor coordinación.
Hacer primero `Correo` permite practicar el patrón de introducción de Value Object con el
cambio de menor impacto.

### ¿Por qué `PeriodoReserva` al final (paso 6)?
Es el cambio de mayor riesgo (Medio-Alto según la matriz de riesgo). Requiere:
1. Decidir si periodo inválido → retorno `0.0` o excepción.
2. Modificar el constructor de `Reserva` (dos campos → un VO).
3. Adaptar todos los puntos de construcción de `Reserva`.
4. Eliminar la triple condición de `ServicioReservas`.

Ejecutarlo último, cuando ya existen pruebas para todos los demás escenarios, minimiza
la probabilidad de introducir una regresión no detectada.

---

## Vista de dependencias

```
Paso 1 (Pruebas JUnit)
│
├── Paso 2 (Extraer calcularTotal)
│   └── Paso 4 (Enum TipoReserva)
│
├── Paso 3 (Extraer notificación/persistencia)
│
├── Paso 5 (VO Correo)
│   └── Paso 6 (VO PeriodoReserva)  ← mayor riesgo, al final
```

---

## Lo que este plan NO incluye (y por qué)

| Exclusión | Razón |
|---|---|
| Builder para `Reserva` | Útil cuando el constructor supere 5–6 parámetros con muchos opcionales; hoy los pasos 5–6 ya reducen la lista a 3–4 parámetros |
| Polimorfismo para tipos de reserva (Strategy) | Solo hay dos tipos hoy; el enum + método `calcularTotal` es suficiente hasta que aparezca un tercer tipo real |
| Refactorizar `Main.java` | `Main` es código de demostración, no de producción; no forma parte del contrato observable |
