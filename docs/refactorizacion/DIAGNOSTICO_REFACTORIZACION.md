# Diagnóstico técnico y refactorización — AviManager

Taller de refactorización y cierre de arquitectura · Ingeniería de Software II · Universidad del Quindío · 2026-2

## 1. Contexto

| | |
|---|---|
| **Equipo** | Santiago Ramírez Bernal · María José Valencia Díaz |
| **Repositorio** | https://github.com/majito222/Proyecto_IngeSoft_II |
| **Tecnología** | Java 17, Spring Boot 3.3, Spring Security (JWT); React 19 + Vite en el frontend |
| **Arquitectura** | Hexagonal (puertos y adaptadores): `domain/` sin dependencias de Spring, `infrastructure/` con adaptadores web y de persistencia en memoria |
| **Funcionalidad analizada** | Cierre de turno operativo con bloqueo por alerta sanitaria y atención de la alerta |
| **Requisitos relacionados** | F-01 (cierre de turno), F-02 (alerta sanitaria), F-02.5 (atención de la alerta), F-03 (descuento de alimento), F-10 (turnos) · RN-01, RN-03, RN-05, RN-07 |

**Por qué esta funcionalidad.** Es una transacción de negocio que, en dos casos de uso, modifica cinco entidades
(`Turno`, `ReporteDiario`, `Lote`, `AlertaSanitaria`, `Insumo`) y publica eventos de dominio. Además está cubierta por
pruebas unitarias y de integración, lo que permite verificar que una refactorización conserva el comportamiento.

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
| **Estado** | Seleccionado |

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
| **Estado** | Seleccionado (se resuelve con la misma intervención que H-01) |

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

**Cambio elegido:** mover la regla de H-01 al dominio y proteger la invariante de H-02.

**Diseño antes**

```
ReglaConsolidacion ─────────┐
AtenderAlertaSanitariaSvc ──┼─→ reporte.consolidar()  +  lote.registrarBajas(n)   (cada uno por su cuenta)
DatosDemoInitializer ───────┘                              (sin validar n)
```

**Diseño después (propuesto)**

```
ReglaConsolidacion ─────────┐
AtenderAlertaSanitariaSvc ──┼─→ lote.consolidarReporte(reporte)
DatosDemoInitializer ───────┘       ├ verifica que el reporte sea de este lote
                                    ├ valida 0 ≤ bajas ≤ aves vivas
                                    ├ reporte.consolidar()
                                    └ descuenta las bajas
```

**Archivos que se prevé modificar:** `Lote.java`, `ReglaConsolidacion.java`, `AtenderAlertaSanitariaService.java`,
`DatosDemoInitializer.java` y pruebas nuevas del dominio.

**Clasificación del cambio.** Refactorización que conserva el comportamiento para todo dato válido. La validación
nueva es un endurecimiento: solo rechaza casos que los caminos actuales ya impiden; no corrige un defecto observable.

## 6. Verificación

**Antes de la intervención:** `mvn test` en `backend/` → **349 pruebas, 0 fallos** en 31 clases
([evidencias/pruebas_antes.txt](evidencias/pruebas_antes.txt)).

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
| `CerrarTurnoServiceTest` | L118 | Cierre normal: el lote pasa de 500 a 495 aves (las bajas se descuentan) |
| `CerrarTurnoServiceTest` | L141 | Cierre bloqueado por alerta: las bajas **no** se descuentan todavía |
| `AlertasServiceTest` | L98, L122 | Al atender la alerta, el lote pasa de 800 a 784 aves |
| `AlertasServiceTest` | L131 | Una alerta ya atendida no descuenta dos veces (sigue en 800) |

La copia de `DatosDemoInitializer` queda cubierta indirectamente: `SeguridadIntegracionTest` L228-229 espera una
ración de 10,1 kg para el Galpón 3, calculada con 597 aves (600 menos las 3 bajas del cierre de demo de ayer).

**Después de la intervención:** pendiente.

## 7. Cierre

Pendiente de completar tras la intervención.

## 8. Git

| Paso | Commit |
|---|---|
| Diagnóstico (`docs: registrar diagnostico tecnico`) | pendiente |
| Intervención (`refactor: ...`) | pendiente |
| Verificación (`docs: documentar verificacion de refactorizacion`) | pendiente |

Rama de trabajo: `refactor/consolidacion-reporte-lote`.
