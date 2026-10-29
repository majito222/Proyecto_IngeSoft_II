# Diagnóstico técnico y refactorización — AviManager

Taller de refactorización y cierre de arquitectura · Ingeniería de Software II · Universidad del Quindío · 2026-2

## 1. Contexto

### 1.1 El proyecto

**AviManager** es un sistema web para gestionar la operación diaria de una granja de gallinas ponedoras. Reemplaza
las planillas de papel con las que hoy se registran el consumo de alimento, la mortalidad y la producción de cada
galpón, y automatiza las reglas de negocio que antes dependían de que alguien revisara los números a mano:

- bloquear el cierre del turno y avisar al veterinario cuando la mortalidad del día supera el 1,5 % (RN-01, RN-07);
- descontar el alimento de la bodega y avisar cuando baja del 50 % de su capacidad (RN-03);
- calcular la ración diaria según la edad y el peso de las aves (F-06, RN-02);
- controlar que los daños de infraestructura se reparen en 12 horas (RN-04);
- aprobar los reportes, cuadrar la bodega y consolidar los indicadores del día (F-04, RN-06).

Lo usan seis roles (operario, administrador, veterinario, zootecnista, técnico de mantenimiento y dueño), y cada uno
solo puede ejecutar las acciones de su permiso. El proyecto se basa en el Student Book *Software Modeling* del curso
y en el Plan de Requisitos del equipo (trazabilidad BR → StR → SyR → SWR). Al momento del taller tenía 11
funcionalidades implementadas y 349 pruebas automatizadas.

| | |
|---|---|
| **Equipo** | Santiago Ramírez Bernal · María José Valencia Díaz |
| **Repositorio** | https://github.com/majito222/Proyecto_IngeSoft_II |
| **Tecnología** | Java 17, Spring Boot 3.3, Spring Security (JWT); React 19 + Vite en el frontend |
| **Arquitectura** | Hexagonal (puertos y adaptadores): `domain/` sin dependencias de Spring, `infrastructure/` con adaptadores web y de persistencia en memoria |
| **Funcionalidad analizada** | Cierre de turno operativo con bloqueo por alerta sanitaria y atención de la alerta |
| **Requisitos relacionados** | F-01 (cierre de turno), F-02 (alerta sanitaria), F-02.5 (atención de la alerta), F-03 (descuento de alimento), F-10 (turnos) · RN-01, RN-03, RN-05, RN-07 |

### 1.2 La funcionalidad analizada

Cada día, el operario asignado a un galpón abre un turno sobre el lote de aves y, al terminar la jornada, lo cierra
registrando el reporte diario: kilos de alimento consumidos, aves muertas (bajas) y su causa probable, y bandejas de
huevos producidas. Con esos datos el sistema aplica, en orden, las reglas del cierre:

1. **Campos obligatorios (RN-05).** Si falta un dato, el turno no se cierra y se indican los campos faltantes.
2. **Valores válidos.** No se aceptan cantidades negativas ni más bajas que aves vivas.
3. **Descuento de alimento (F-03, RN-03).** Lo consumido se descuenta de la bodega; si el stock cruza el 50 %, se avisa
   al administrador y al dueño.
4. **Mortalidad (RN-07, RN-01).** Se calcula `bajas de hoy / población del día × 100`. Si supera el 1,5 %, el cierre
   se bloquea: el turno queda *bloqueado por alerta sanitaria*, el lote en observación y se avisa al veterinario y al
   administrador.
5. **Consolidación.** Si nada detuvo el cierre, el reporte se consolida, las bajas se descuentan de la población del
   lote y el turno queda *cerrado*.

Cuando el cierre quedó bloqueado, el **veterinario atiende la alerta (F-02.5)** registrando diagnóstico y tratamiento.
En ese momento el turno pasa a *cerrado con alerta*, el reporte se consolida y las bajas se descuentan del lote; si el
veterinario lo indica, el lote sale de observación.

La población del lote que resulta de estos dos caminos es la base de la mortalidad del día siguiente (RN-07), de la
ración diaria (F-06) y de los indicadores (F-04). Por eso un error al descontar las bajas se propaga a casi todo el
sistema.

**Por qué esta funcionalidad.** Es una transacción de negocio que, en dos casos de uso, modifica cinco entidades
(`Turno`, `ReporteDiario`, `Lote`, `AlertaSanitaria`, `Insumo`) y publica eventos de dominio. Además está cubierta por
pruebas unitarias y de integración, lo que permite verificar que una refactorización conserva el comportamiento.

### 1.3 Justificación técnica de la intervención

Se intervinieron **H-01** (regla duplicada) y **H-02** (invariante no protegida) en un solo cambio (detalle en la
sección 5).

**Por qué estos hallazgos y no otros**

- **Riesgo demostrado, no supuesto.** H-01 ya causó un defecto real: el commit `3e1d4b4` (PR #12) corrigió que una de
  las copias de la regla, la de los datos de demostración, no descontaba las bajas. Los otros hallazgos son riesgos
  potenciales o deuda sin efecto observable (sección 4).
- **Impacto alto.** La población del lote alimenta RN-07, la ración y los KPIs (sección 1.2).
- **Esfuerzo acotado y verificable.** El cambio toca cuatro archivos de producción, y las pruebas existentes ya
  verificaban la población del lote en ambos caminos (sección 6), así que se podía demostrar la conservación del
  comportamiento sin escribirlas de cero.
- **H-02 sale casi gratis.** El método que resuelve H-01 es el lugar natural para validar la invariante.

**Por qué la solución es mover la regla a la entidad `Lote`**

- **Experto en información (GRASP).** `Lote` es dueño de la población; quien descuenta las bajas debe ser quien conoce
  y protege ese dato.
- **Invariantes en la entidad (modelo de dominio rico).** Una regla que vive en un servicio solo se cumple si cada
  caso de uso se acuerda de aplicarla. Dentro de la entidad se cumple siempre, llegue el cambio por el caso de uso que
  llegue, y sin importar qué casos de uso se agreguen después.
- **Una sola fuente de verdad (DRY).** Si la regla cambia, se modifica en un único método.
- **Encapsulamiento verificable.** `registrarBajas` pasó a ser privado. Desde entonces el compilador impide descontar
  bajas sin consolidar un reporte, y la regla deja de ser solo una convención entre programadores.
- **Coherente con la arquitectura hexagonal.** El cambio queda dentro de `domain/model` y no introduce dependencias
  hacia la infraestructura; los servicios siguen coordinando y la entidad decide.

**Alternativas evaluadas y descartadas**

| Alternativa | Por qué se descartó |
|---|---|
| Dejar el código como estaba y documentarlo | El riesgo ya se había materializado una vez; documentarlo no impide que se repita. |
| Un servicio de dominio nuevo (por ejemplo `ConsolidadorDeReportes`) | Saca la regla de los casos de uso, pero no protege la invariante: `Lote.registrarBajas` seguiría siendo público y se podría saltar. |
| Poner el método en `ReporteDiario` (`reporte.consolidarEn(lote)`) | Obligaría a `ReporteDiario` a modificar el estado de `Lote`, que es otra entidad; quien cambia la población debe ser el propio lote. |
| Hacer privado también `ReporteDiario.consolidar()` | Habría obligado a modificar dos pruebas existentes, que son la evidencia de que el comportamiento se conserva. Se dejó como deuda pendiente (sección 7). |

**Por qué `IllegalStateException`.** Por los caminos actuales la invariante no se puede violar, porque
`ReglaValoresValidos` la revisa al cerrar el turno. Si llegara a ocurrir, sería un error interno del sistema y no un
dato inválido del usuario, así que debe responder 500 y no 400. Se usó a propósito para no repetir el problema que
describe H-04.

## 2. Arquitectura real

### Componentes

| Capa | Componente | Responsabilidad en la funcionalidad |
|---|---|---|
| Adaptador de entrada | [`TurnoController.cerrarTurno`](../../backend/src/main/java/com/avimanager/infrastructure/adapter/in/web/TurnoController.java) (L54-77) | Arma `CerrarTurnoCommand` con el usuario del token y traduce el resultado a 200 / 409 / 422 |
| Adaptador de entrada | `AlertaController` | Recibe la atención de la alerta (`POST /api/alertas/{id}/atencion`) |
| Caso de uso | [`CerrarTurnoService.cerrarTurno`](../../backend/src/main/java/com/avimanager/domain/service/CerrarTurnoService.java) (L52-66) | Precondiciones: lote existe, turno de hoy, solicitante responsable, turno ABIERTO; delega en la cadena |
| Dominio (cadena) | [`ReglaCierreTurno`](../../backend/src/main/java/com/avimanager/domain/service/cierre/ReglaCierreTurno.java) y 5 eslabones en `domain/service/cierre/` | Chain of Responsibility + Template Method con las reglas del cierre |
| Caso de uso | [`AtenderAlertaSanitariaService.atender`](../../backend/src/main/java/com/avimanager/domain/service/AtenderAlertaSanitariaService.java) (L63-91) | Atiende la alerta, cierra el turno con alerta, consolida el reporte y descuenta las bajas |
| Dominio (entidades) | `Turno` (+ `EstadoTurno`, State), `ReporteDiario`, `Lote`, `AlertaSanitaria`, `Insumo` | Estado y transiciones del negocio |
| Puertos de salida | `TurnoRepositoryPort`, `LoteRepositoryPort`, `ReporteDiarioRepositoryPort`, `AlertaSanitariaRepositoryPort`, `InventarioRepositoryPort`, `PublicadorEventosPort` | Persistencia y eventos |
| Adaptadores de salida | `InMemory*Repository`, `PublicadorEventosEnMemoria` + notificadores | Implementación en memoria y avisos (Observer) |
| Configuración | `UseCaseConfig.reglasDeCierreTurno` (L111-117) | Arma el orden de la cadena |

### Flujo

```
POST /api/lotes/{loteId}/turnos/cierre
 └ TurnoController.cerrarTurno
    └ CerrarTurnoService.cerrarTurno        (lote, turno de hoy, responsable, ABIERTO)
       └ cadena (UseCaseConfig L111-117)
          1 ReglaCamposObligatorios   → faltan campos: resultado CAMPOS_INCOMPLETOS (422)
          2 ReglaValoresValidos       → valores imposibles: IllegalArgumentException (400)
          3 ReglaDescontarInventario  → descuenta alimento, guarda Insumo; StockCriticoDetectado si cruza 50 %
          4 ReglaMortalidad           → % > 1,5: bloquea reporte y turno, lote en observación, crea alerta,
                                        publica AlertaSanitariaGenerada → ALERTA_SANITARIA (409)
          5 ReglaConsolidacion        → bajas al lote, consolida reporte, cierra turno → TURNO_CERRADO (200)

POST /api/alertas/{id}/atencion
 └ AtenderAlertaSanitariaService.atender
    alerta.atender → turno.cerrarConAlertaAtendida → reporte.consolidar → lote.registrarBajas
    → guarda las cuatro entidades → publica AlertaSanitariaAtendida
```

### Zonas inspeccionadas sin hallazgos

- **Controlador:** no contiene decisiones de dominio, cálculos ni acceso a persistencia; solo traduce HTTP.
- **`CerrarTurnoService`:** solo verifica precondiciones y delega; no mezcla responsabilidades.
- **Dependencias entre capas:** el paquete `domain/` no importa Spring ni `infrastructure/`.

## 3. Hallazgos

Cada hallazgo se verificó manualmente en el código de la rama `main` (commit `a9e951e`). Se distingue entre hechos
comprobados e hipótesis.

### H-01 · La regla «consolidar el reporte descuenta sus bajas del lote» está duplicada en tres lugares

| | |
|---|---|
| **Tipo** | Deuda técnica con riesgo de inconsistencia |
| **Ubicación** | `domain/service/cierre/ReglaConsolidacion.java`, `evaluar`, L34-40 · `domain/service/AtenderAlertaSanitariaService.java`, `atender`, L77-78 · `infrastructure/config/DatosDemoInitializer.java`, `turnoCerrado`, L211-217 |
| **Estado** | Implementado y verificado (sección 6) |

**Evidencia**
```java
// ReglaConsolidacion.evaluar (L34-40)
Lote lote = contexto.getLote();
lote.registrarBajas(contexto.getComando().getCantidadBajas());
loteRepository.guardar(lote);
ReporteDiario reporte = contexto.reporte();
reporte.consolidar();

// AtenderAlertaSanitariaService.atender (L77-78)
reporte.consolidar();
lote.registrarBajas(reporte.getCantidadBajas());

// DatosDemoInitializer.turnoCerrado (L211-217)
reporte.consolidar();
// Igual que ReglaConsolidacion: las bajas del cierre se descuentan del lote.
lote.registrarBajas(bajas);
```

**Observación.** Es una sola regla del negocio —un reporte consolidado ya descontó sus bajas del lote— pero ningún
objeto del dominio la garantiza: cada caso de uso debe recordar hacer las dos operaciones juntas. El comentario de
la línea 215 reconoce la copia.

**Criterio.** DRY y cohesión: una regla de negocio repartida en la capa de servicios en vez de vivir en el dominio.

**Impacto y riesgo (hecho comprobado).** El riesgo ya se materializó: el commit `3e1d4b4` (PR #12, *«bajas de demo
descontadas del lote»*) corrigió que la copia de la demo no descontaba las bajas. Si la población del lote queda mal,
se calculan mal la mortalidad del día (RN-07), la ración (F-06) y los indicadores (F-04).

**Acción propuesta.** Un único método del dominio en `Lote` que consolide el reporte y descuente sus bajas; las tres
copias pasan a usarlo.

**Verificación.** Suite completa antes y después; en especial `CerrarTurnoServiceTest`, `AlertasServiceTest`,
`KpisALaFechaTest` y los flujos de `SeguridadIntegracionTest`. Pruebas unitarias nuevas del método.

### H-02 · `Lote` no protege su invariante de población

| | |
|---|---|
| **Tipo** | Riesgo arquitectónico (invariante fuera de la entidad) |
| **Ubicación** | `domain/model/Lote.java`, `registrarBajas`, L55-57 · invariante en `domain/service/cierre/ReglaValoresValidos.java`, `evaluar`, L29-33 |
| **Estado** | Implementado y verificado (misma intervención que H-01) |

**Evidencia**
```java
// Lote.registrarBajas (L55-57): resta sin validar
public void registrarBajas(int cantidadBajas) {
    this.poblacionActual = this.poblacionActual - cantidadBajas;
}

// ReglaValoresValidos.evaluar (L29-33): la única validación, en un eslabón de la cadena
int avesVivas = contexto.getLote().getPoblacionActual();
if (comando.getCantidadBajas() < 0 || comando.getCantidadBajas() > avesVivas) {
    throw new IllegalArgumentException(...);
}
```

**Observación.** La invariante «0 ≤ bajas ≤ aves vivas» solo se aplica cuando se entra por la cadena de cierre.
`AtenderAlertaSanitariaService` y `DatosDemoInitializer` llaman a `registrarBajas` sin pasar por ella.

**Criterio.** Encapsulamiento: las invariantes de una entidad deben protegerse en la entidad (modelo rico).

**Impacto y riesgo.** Hoy no falla porque los datos de la atención ya se validaron al cerrar el turno (hecho). Es un
riesgo para cualquier caso de uso futuro: la población podría quedar negativa sin ningún error (hipótesis razonable,
no reproducible por los caminos actuales).

**Acción propuesta.** Validar en el dominio dentro del nuevo método de `Lote`.

**Verificación.** Pruebas unitarias: bajas negativas y bajas mayores que la población son rechazadas; los flujos
válidos no cambian.

### H-03 · `ReglaValoresValidos` depende implícitamente del orden de la cadena

| | |
|---|---|
| **Tipo** | Riesgo (acoplamiento temporal) |
| **Ubicación** | `domain/service/cierre/ReglaValoresValidos.java`, `evaluar`, L23, L26, L30 · orden en `infrastructure/config/UseCaseConfig.java` L111-117 |
| **Estado** | Pendiente (candidato para la siguiente intervención) |

**Evidencia**
```java
if (comando.getConsumoAlimentoKg() < 0) { ... }        // Double: null → NullPointerException
if (comando.getProduccionHuevosBandejas() < 0) { ... } // Integer: null → NullPointerException
```

**Observación.** El eslabón asume que `ReglaCamposObligatorios` ya rechazó los valores nulos. Esa precondición no
está escrita en el código ni en una prueba: `ReglaCierreTurnoTest` solo verifica que la consolidación sea el último
eslabón (`laConsolidacionSiempreEsElUltimoEslabon`).

**Criterio.** Robustez; precondiciones explícitas.

**Impacto y riesgo.** Si alguien reordena la cadena en `UseCaseConfig`, un cierre incompleto respondería 500
(`NullPointerException`) en vez de 422. Hoy el orden es correcto (hecho).

**Acción propuesta.** Comparaciones seguras ante `null` en el eslabón y una prueba que fije el orden de
`UseCaseConfig.reglasDeCierreTurno`.

**Verificación.** Prueba nueva del orden; la suite existente no debe cambiar.

### H-04 · `IllegalArgumentException` genérica usada como error de negocio

| | |
|---|---|
| **Tipo** | Riesgo arquitectónico / oportunidad de mejora |
| **Ubicación** | `ReglaValoresValidos.evaluar` L24-31 · `infrastructure/adapter/in/web/GlobalExceptionHandler.java`, `manejarArgumentoInvalido`, L100-102 |
| **Estado** | Pendiente |

**Evidencia**
```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
}
```

**Observación.** Responder 400 ante un dato inválido es una decisión documentada (javadoc de `ReglaValoresValidos`,
L15-16). El problema es que la excepción es genérica de Java: cualquier `IllegalArgumentException` causada por un
error de programación también se respondería como «error del cliente», con su mensaje interno. Además, la misma
cadena valida con dos mecanismos distintos (resultado 422 en `ReglaCamposObligatorios` y excepción 400 aquí).

**Criterio.** Separación entre errores de negocio y errores técnicos.

**Impacto y riesgo.** Errores internos ocultos como 400 y mensajes internos expuestos al cliente (hipótesis: no se
identificó hoy un caso concreto que lo dispare).

**Acción propuesta.** Una excepción de dominio propia (por ejemplo `DatoInvalidoException`) mapeada a 400; las
`IllegalArgumentException` restantes quedarían como error técnico (500). Requiere tocar varias clases.

**Verificación.** Pruebas de integración que esperan 400 deben seguir pasando.

### H-05 · Servicios que crean por dentro sus colaboradores

| | |
|---|---|
| **Tipo** | Deuda técnica |
| **Ubicación** | `domain/service/AtenderAlertaSanitariaService.java`, constructor, L49-50 y L58 (mismo patrón en 10 servicios de `domain/service/`) |
| **Estado** | Pendiente |

**Evidencia**
```java
public AtenderAlertaSanitariaService(..., GalponRepositoryPort galponRepository,
                                     UsuarioRepositoryPort usuarioRepository, ...) {
    ...
    this.resumidor = new ResumidorAlertas(turnoRepository, loteRepository, galponRepository, usuarioRepository);
}
```

**Observación.** El servicio recibe `galponRepository` y `usuarioRepository` solo para construir `ResumidorAlertas`;
no los usa en su lógica. Los servicios con este patrón son: `AtenderAlertaSanitariaService`, `AtenderOrdenService`,
`ConsultarAlertasService`, `ConsultarOrdenesService`, `ConsultarPesajesService`, `ConsultarTurnosService`,
`IniciarTurnoService`, `RegistrarPesajeService`, `ReportarDanoService`, `RevisarPesajeService`.

**Criterio.** Inversión de dependencias / inyección de dependencias.

**Impacto y riesgo.** Bajo: no es un defecto. Dificulta sustituir el colaborador en pruebas y alarga el constructor
(8 parámetros).

**Acción propuesta.** Inyectar `ResumidorAlertas` desde `UseCaseConfig`.

**Verificación.** Suite completa sin cambios.

### H-06 · La transacción de negocio no tiene unidad de trabajo

| | |
|---|---|
| **Tipo** | Riesgo arquitectónico (hipótesis, no reproducible hoy) |
| **Ubicación** | `ReglaDescontarInventario.evaluar` L45-46 · `ReglaMortalidad.evaluar` L61-75 |
| **Estado** | Pendiente (se documenta) |

**Observación.** Un cierre con alerta guarda por separado `Insumo`, `ReporteDiario`, `Lote`, `AlertaSanitaria` y
`Turno`, sin un límite transaccional. Hoy está mitigado: se valida todo antes de cualquier efecto y la persistencia es
en memoria.

**Impacto y riesgo.** Al migrar a una base de datos, un fallo a mitad de camino dejaría, por ejemplo, el alimento
descontado con el turno todavía abierto, y un reintento lo descontaría dos veces. No se puede demostrar sin inyectar
fallos.

**Acción propuesta.** Un puerto de unidad de trabajo (o `@Transactional` en el adaptador) al introducir persistencia
real.

## 4. Priorización

Escala 1 (bajo) a 3 (alto). El esfuerzo alto resta prioridad.

| ID | Impacto | Riesgo | Esfuerzo | Decisión y razón |
|---|---|---|---|---|
| **H-01** | 3 | 3 | 2 | **Seleccionado.** Ya causó un defecto real (commit `3e1d4b4`) y afecta RN-07, ración y KPIs. |
| **H-02** | 2 | 2 | 1 | **Seleccionado.** Se resuelve en el mismo cambio que H-01, con esfuerzo marginal. |
| H-03 | 2 | 2 | 1 | Pendiente. Siguiente intervención recomendada. |
| H-04 | 2 | 2 | 3 | Pendiente. Requiere tocar muchas clases y pruebas. |
| H-05 | 1 | 1 | 2 | Pendiente. Deuda técnica sin efecto observable. |
| H-06 | 3 | 1 | 3 | Pendiente. Riesgo futuro; se aborda al introducir una base de datos. |

## 5. Intervención

**Cambio realizado:** la regla de H-01 pasa al dominio y la invariante de H-02 se protege en la entidad. Las
pruebas existentes no se modificaron.

**Diseño antes**

```
ReglaConsolidacion ──────────┐
AtenderAlertaSanitariaSvc ───┤
DatosDemoInitializer ────────┼─→ reporte.consolidar()  +  lote.registrarBajas(n)   (cada uno por su cuenta,
  (turnoCerrado e historial) ┘                                                     sin validar n)
```

**Diseño después**

```
ReglaConsolidacion ──────────┐
AtenderAlertaSanitariaSvc ───┤
DatosDemoInitializer ────────┼─→ lote.consolidarReporte(reporte)
  (turnoCerrado e historial) ┘      ├ el reporte es de este lote          (si no: IllegalStateException)
                                    ├ 0 ≤ bajas ≤ aves vivas              (si no: IllegalStateException)
                                    ├ reporte.consolidar()
                                    └ registrarBajas(bajas)   ← ahora privado: la única vía es consolidar
```

**Núcleo del cambio** (`domain/model/Lote.java`)

```java
public void consolidarReporte(ReporteDiario reporte) {
    if (!id.equals(reporte.getLoteId())) {
        throw new IllegalStateException("El reporte " + reporte.getId() + " no es del lote " + id);
    }
    Integer bajas = reporte.getCantidadBajas();
    if (bajas == null || bajas < 0 || bajas > poblacionActual) {
        throw new IllegalStateException("Las bajas del reporte (" + bajas + ") deben estar entre 0 y las "
                + poblacionActual + " aves vivas del lote " + id);
    }
    reporte.consolidar();
    registrarBajas(bajas);
}

private void registrarBajas(int cantidadBajas) { ... }   // antes era público
```

**Archivos afectados**

| Archivo | Cambio |
|---|---|
| `backend/src/main/java/com/avimanager/domain/model/Lote.java` | Nuevo `consolidarReporte(ReporteDiario)`; `registrarBajas` pasa a privado |
| `backend/src/main/java/com/avimanager/domain/service/cierre/ReglaConsolidacion.java` | `evaluar` usa `lote.consolidarReporte(reporte)` |
| `backend/src/main/java/com/avimanager/domain/service/AtenderAlertaSanitariaService.java` | `atender` usa `lote.consolidarReporte(reporte)` |
| `backend/src/main/java/com/avimanager/infrastructure/config/DatosDemoInitializer.java` | `turnoCerrado` e `historial` usan `lote.consolidarReporte(reporte)` |
| `backend/src/test/java/com/avimanager/domain/model/LoteTest.java` | Nueva: 7 pruebas del método |

Durante la intervención apareció una **cuarta copia** que el diagnóstico no había contado:
`DatosDemoInitializer.historial` consolidaba reportes sin pasar por el lote. Esos reportes tienen 0 bajas, así que
no producía un error, pero también se migró para que todo el código de producción use un único camino.

**Decisiones de diseño**

- **`IllegalStateException` y no `IllegalArgumentException`.** Por los caminos actuales es imposible violar la
  invariante (`ReglaValoresValidos` ya la revisa al cerrar). Si ocurre, es un error interno y debe responder 500, no
  400. Esto evita repetir el problema descrito en H-04.
- **`ReporteDiario.consolidar()` sigue siendo público.** Hacerlo privado obligaría a modificar dos *fixtures* de
  prueba que lo llaman directamente (`ConsolidacionReporteDiarioTest` L93 y `KpisALaFechaTest` L57). Se prefirió no
  tocar pruebas existentes, porque son la evidencia de conservación del comportamiento. Queda como deuda (sección 7).

**Clasificación del cambio.** Refactorización que conserva el comportamiento observable para todo dato válido, más un
endurecimiento de la invariante. No corrige un defecto observable hoy.

## 6. Verificación

**Pruebas que protegen la funcionalidad**

| Clase | Pruebas | Qué protege |
|---|---|---|
| `CerrarTurnoServiceTest` | 9 | Cierre normal, campos incompletos (RN-05), alerta (RN-01/RN-07), responsable del turno |
| `AlertasServiceTest` | 7 | Atención de la alerta, cierre con alerta, descuento de bajas, liberación del lote |
| `DescontarInventarioTest` | 9 | Descuento de alimento, stock crítico, valores inválidos sin efectos |
| `ReglaCierreTurnoTest` | 5 | Recorrido y orden de la cadena |
| `KpisALaFechaTest` | 3 | Aves vivas a la fecha (depende de las bajas descontadas) |
| `SeguridadIntegracionTest` | 141 | Flujos de punta a punta por HTTP, incluido cerrar con alerta y atenderla |

**Aserciones que protegen exactamente la regla de H-01** (verificadas antes de intervenir):

| Prueba | Línea | Qué comprueba |
|---|---|---|
| `CerrarTurnoServiceTest.cierraElTurnoCuandoLaMortalidadEstaDentroDelUmbral` | L118 | Cierre normal: el lote pasa de 500 a 495 aves |
| `CerrarTurnoServiceTest.bloqueaElCierreYGeneraAlertaSanitariaCuandoSuperaElUmbral_RN01_RN07` | L141 | Cierre bloqueado: las bajas **no** se descuentan todavía |
| `AlertasServiceTest.atenderLaAlertaCierraElTurnoConAlertaYConsolidaElReporte` | L98 | Al atender la alerta, el lote pasa de 800 a 784 aves |
| `AlertasServiceTest.unaAlertaAtendidaNoSePuedeVolverAAtenderNiDescuentaDosVeces` | L122 | Un segundo intento de atención no descuenta dos veces (sigue en 784) |
| `AlertasServiceTest.sinDiagnosticoNoSeTocaNada` | L131 | Una atención inválida no descuenta nada (sigue en 800) |

La copia de `DatosDemoInitializer` queda cubierta indirectamente: `SeguridadIntegracionTest` L228-229 espera una
ración de 10,1 kg para el Galpón 3, calculada con 597 aves (600 menos las 3 bajas del cierre de demo de ayer).

**Resultados antes y después** (`mvn test` en `backend/`)

| | Antes (commit `a9e951e`) | Después (sobre `033e839`) |
|---|---|---|
| Pruebas | 349 | 356 (las mismas 349 + 7 de `LoteTest`) |
| Fallos, errores u omitidas | 0 | 0 |
| Clases de prueba | 31 | 32 |
| Evidencia | [evidencias/pruebas_antes.txt](evidencias/pruebas_antes.txt) | [evidencias/pruebas_despues.txt](evidencias/pruebas_despues.txt) |

La comparación clase por clase de ambos archivos solo difiere en la línea nueva de `LoteTest`: las 31 clases que ya
existían conservan el mismo número de pruebas y siguen en verde.

**Prueba de mutación** ([evidencias/prueba_mutacion.txt](evidencias/prueba_mutacion.txt)). Para comprobar que la red
de seguridad detecta un error real, se comentó de forma temporal `registrarBajas(bajas)` dentro de
`consolidarReporte`. El reporte seguía consolidándose, pero las bajas ya no se descontaban. Resultado: **5 pruebas
fallaron en 3 clases**:

- `LoteTest`: 2 (`consolidarElReporteDescuentaSusBajasDelLote_H01`, `sePuedenDarDeBajaTodasLasAvesVivas`).
- `AlertasServiceTest`: 2 (`atenderLaAlertaCierraElTurnoConAlertaYConsolidaElReporte`, `unaAlertaAtendidaNoSePuedeVolverAAtenderNiDescuentaDosVeces`).
- `CerrarTurnoServiceTest`: 1 (`cierraElTurnoCuandoLaMortalidadEstaDentroDelUmbral`).

Al restaurar el código, las tres clases y la suite completa volvieron a pasar.

**Limitaciones de la verificación**

- El frontend no se modificó y no se probó manualmente: el contrato HTTP (respuestas y códigos) no cambió.
- La copia de la demo se verifica de forma indirecta (ración del Galpón 3), no con una prueba propia.
- La prueba de mutación cubrió un solo tipo de error (no descontar las bajas). No se mutó la validación nueva; esa
  validación la cubren directamente las pruebas de rechazo de `LoteTest`.

## 7. Cierre

**Lecciones**

- La duplicación más peligrosa no es la de código parecido, sino la de una **regla del negocio** repetida. Aquí ya
  había causado un defecto real (commit `3e1d4b4`).
- Las invariantes deben vivir en la entidad: así se cumplen sin importar por qué caso de uso se llegue.
- Las pruebas que verifican el **estado del negocio** (la población del lote) y no solo el código HTTP fueron las
  que dieron confianza para refactorizar. La prueba de mutación mostró que de verdad detectan un error.
- La IA sirvió de apoyo, pero la verificación manual fue necesaria. En la versión del diagnóstico (commit `033e839`)
  se describieron mal dos aserciones de `AlertasServiceTest` (L122 y L131), y se corrigieron en esta versión. Además,
  la cuarta copia (`historial`) solo apareció al revisar todos los usos durante la intervención.

**Deuda técnica pendiente**

| ID | Pendiente |
|---|---|
| H-01 (residuo) | `ReporteDiario.consolidar()` sigue público: un reporte todavía podría consolidarse sin descontar sus bajas. Para cerrarlo hay que ajustar dos *fixtures* de prueba y dejar el método con visibilidad de paquete. |
| H-03 | Comparaciones seguras ante `null` en `ReglaValoresValidos` y una prueba que fije el orden de la cadena. Es la siguiente intervención recomendada (esfuerzo 1). |
| H-04 | Excepción de dominio propia para datos inválidos en lugar de `IllegalArgumentException`. |
| H-05 | Inyectar `Resumidor*` y `GalponAsignadoResolver` en los 10 servicios desde `UseCaseConfig`. |
| H-06 | Unidad de trabajo o transacción al introducir una base de datos. |

**Vínculo con el proceso de desarrollo.** Pregunta de cierre del taller: *¿qué práctica habría permitido detectar estos
problemas antes de entregar?*

- **Revisión de código en los pull requests con una lista de inspección.** El PR #12 corrigió el síntoma (la demo no
  descontaba las bajas), pero no la causa: la regla copiada. Una revisión que preguntara *«¿esta regla ya existe en
  otro lugar?»* y *«¿la entidad protege su invariante?»* habría detectado H-01 y H-02 en ese momento.
- **Refactorizar como parte de la definición de terminado.** Con la regla de *«tres copias, se extrae»*, el problema
  se habría corregido al crear la segunda copia (F-02.5).
- **Pruebas de invariantes del dominio por entidad**, además de las pruebas por caso de uso. `Lote` no tenía una
  clase de pruebas propia hasta esta intervención.

## 8. Git

| Paso | Commit | Contenido |
|---|---|---|
| 1. Diagnóstico | [`033e839`](https://github.com/majito222/Proyecto_IngeSoft_II/commit/033e839) | Este informe con las secciones 1 a 4 y la evidencia de pruebas antes (`evidencias/pruebas_antes.txt`) |
| 2. Intervención | [`c7939ef`](https://github.com/majito222/Proyecto_IngeSoft_II/commit/c7939ef) `refactor: centralizar la consolidacion del reporte y el descuento de bajas en Lote` | Código de la intervención y `LoteTest`. Incluyó también las secciones 5 a 7 del informe y las evidencias después y de mutación |
| 3. Verificación | [`9f60f61`](https://github.com/majito222/Proyecto_IngeSoft_II/commit/9f60f61) `docs: documentar verificacion de refactorizacion` | Registro de los hashes y cierre de la trazabilidad |
| Integración | [`062f215`](https://github.com/majito222/Proyecto_IngeSoft_II/commit/062f215) · [PR #16](https://github.com/majito222/Proyecto_IngeSoft_II/pull/16) | Merge de `refactor/consolidacion-reporte-lote` a `main` |

Rama de trabajo: `refactor/consolidacion-reporte-lote`, integrada a `main` por el PR #16 (el merge conserva los tres
commits con sus hashes).

Comandos para revisar la trazabilidad:

```bash
git log --oneline 033e839^..062f215     # diagnóstico → intervención → verificación → merge
git show c7939ef -- backend              # solo el cambio de código de la intervención
```
