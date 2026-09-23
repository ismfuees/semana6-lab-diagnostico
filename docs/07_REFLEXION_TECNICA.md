# Reflexión técnica final

## Respuestas

**1. ¿Qué problema de diseño genera mayor riesgo y por qué?**

El mayor riesgo proviene de la concentración de responsabilidades en `ServicioReservas.procesar()`. En una sola función conviven validación de datos, cálculo de precio, persistencia simulada y notificación. Esto significa que un cambio en cualquiera de esas cuatro áreas —por ejemplo, sustituir el `println` de persistencia por una llamada a base de datos— obliga a modificar el mismo método que también contiene la lógica de descuento VIP. Esa proximidad física no refleja ninguna proximidad conceptual: son razones de cambio totalmente independientes. El riesgo concreto es que al modificar la lógica de notificación se rompa accidentalmente la validación de periodo, o que al ajustar el descuento se altere la condición de anticipación. Sin pruebas que cubran cada camino de forma aislada, ese riesgo es invisible hasta que el defecto aparece en producción.

**2. ¿Qué problema parece fácil de corregir, pero podría alterar comportamiento?**

Simplificar las validaciones parece trivial —son cuatro guardas en cadena— pero es donde el riesgo es más sutil. Reordenar las guardas, fusionarlas en un único bloque o extraerlas a un método separado puede cambiar el orden en que se evalúan las condiciones. Si, por ejemplo, la guarda de correo se mueve debajo de la de periodo, un objeto con correo inválido y periodo inválido podría devolver el mismo `0` pero por una razón diferente. Eso no rompe el contrato de retorno, pero puede enmascarar errores de validación que el cliente necesitaría diferenciar en el futuro. Es exactamente el tipo de cambio que parece inocuo y resulta en un bug silencioso.

**3. ¿Qué pruebas son indispensables antes de tocar el código?**

Son indispensables los seis escenarios de la línea base convertidos en pruebas JUnit: reserva NORMAL válida (retorno 40.0, estado CONFIRMADA), reserva VIP válida (retorno 34.0, estado CONFIRMADA), correo inválido (retorno 0, estado PENDIENTE), periodo inválido (retorno 0, estado PENDIENTE), límite exacto de 2 h (retorno 40.0, CONFIRMADA) y límite inferior de 1 h (retorno 0, PENDIENTE). Estas seis pruebas documentan el contrato observable actual y actuarán como red de seguridad ante cualquier reorganización estructural.

**4. ¿Qué responsabilidad moverías primero?**

Extraería primero el cálculo de tarifa a un método privado con nombre intencional (`calcularTotal`). Es el cambio de menor riesgo: no altera la firma pública del método `procesar`, no modifica ningún objeto externo y su corrección es verificable con una única aserción numérica (`== 34.0` para VIP, `== 40.0` para NORMAL). Además, al dar nombre explícito a esa operación se hace visible que el cálculo de precio es una responsabilidad separable, lo que prepara el terreno para extraerla a una clase propia en pasos posteriores.

**5. ¿Qué evidencia usarías para defender esa decisión?**

La evidencia es la propia línea base: LB-01 (40.0, NORMAL) y LB-02 (34.0, VIP) definen exactamente qué debe preservarse. Después de extraer el método, ejecutar esas dos pruebas —junto con el resto de la red de seguridad— confirma que el comportamiento observable no cambió. La ausencia de diferencias en la salida del programa (`Total: 34.0`, `Estado: CONFIRMADA`) es la evidencia final que demuestra que la refactorización fue puramente estructural.

**6. ¿Qué diferencia existe entre refactorizar y realizar un cambio funcional?**

Refactorizar es reorganizar la estructura interna del código —clases, métodos, nombres, jerarquías— sin alterar ningún comportamiento observable. El contrato que el código expone a quien lo usa permanece idéntico antes y después. Un cambio funcional, en cambio, modifica intencionalmente qué hace el código: agrega una regla nueva, cambia un valor de retorno, introduce un efecto secundario diferente. La distinción importa porque la refactorización puede verificarse con las pruebas existentes (si pasan, no se rompió nada), mientras que un cambio funcional requiere actualizar o crear pruebas para el nuevo comportamiento. En este laboratorio se aplica solo diagnóstico; toda modificación de código pertenece al siguiente ciclo, donde la red de seguridad ya estará en su lugar.

---

## Checklist

- [x] Proyecto base compila y ejecuta.
- [x] Seis escenarios de línea base.
- [x] Mapa de responsabilidades.
- [x] Mínimo cinco problemas diagnosticados.
- [x] Matriz de riesgo.
- [x] Pruebas propuestas.
- [x] Plan priorizado.
- [ ] Commit Git del estado inicial.
- [x] Reflexión técnica.
