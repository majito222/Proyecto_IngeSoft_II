# AviManager

Sistema de gestión de galpones avícolas — proyecto de Ingeniería de Software II
(Universidad del Quindío, 2026-2).

**Equipo:** Santiago Ramírez Bernal · María José Valencia Díaz

AviManager digitaliza la operación diaria de una granja de gallinas ponedoras,
que hoy se lleva en planillas de papel. El operario abre y cierra su turno
registrando consumo, bajas y producción; con esos datos el sistema calcula la
mortalidad y bloquea el cierre si supera el 1,5 %, descuenta el alimento de la
bodega y avisa cuando baja del 50 %, calcula la ración diaria según la edad y el
peso de las aves, controla que los daños de infraestructura se reparen en 12
horas y, al final del día, permite al administrador aprobar los reportes,
cuadrar la bodega y consolidar los indicadores para el dueño.

Basado en el Student Book "Software Modeling" y en el Plan de Requisitos
(`Plan_de_Requisitos_Galpon_2026_v4.xlsx`, trazabilidad BR → StR → SyR → SWR).

| | |
|---|---|
| Funcionalidades | 11 (F-01 a F-11) |
| Reglas de negocio | RN-01 a RN-08, todas implementadas y probadas |
| Pruebas | 349 en 31 clases (`mvn test`) |
| Roles | 6, con 18 permisos |

```
backend/   -> API REST en Spring Boot (arquitectura hexagonal)
frontend/  -> interfaz web en React + Vite
```

## Ejecutar

Requisitos: Java 17 o superior, Maven 3.9+ y Node.js 20.19+ (o 22.12+).

```bash
cd backend
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

El backend queda en `http://localhost:8080` y el frontend en
`http://localhost:5173`; Vite redirige `/api` al backend (`vite.config.js`).

La persistencia es en memoria: **al reiniciar el backend se vuelve a cargar la
granja de demostración**. La clave para firmar los JWT se toma de la variable de
entorno `AVIMANAGER_JWT_SECRETO` (mínimo 32 caracteres); la de
`application.yml` es solo para desarrollo.

## Datos de demostración

La contraseña de cada usuario es el usuario seguido de `123`
(por ejemplo `operario` / `operario123`).

| Usuario | Rol | Galpón | Qué se puede probar |
|---|---|---|---|
| `operario` | Operario | Galpón 1 | Iniciar y cerrar turno, pesaje semanal, reportar daños |
| `operario2` | Operario | Galpón 2 | Galpón en riesgo, con alerta sanitaria y orden vencida |
| `operario3` | Operario | Galpón 3 | Turno de hoy ya abierto; cerrar con más de 1,5 % de mortalidad |
| `operario4` | Operario | Galpón 3 | Ve el turno de su compañero, pero no puede cerrarlo |
| `admin` | Administrador | — | Aprobar turnos, consolidar el día, actas de ajuste, galpones y lotes |
| `veterinario` | Veterinario | — | Atender alertas sanitarias |
| `zootecnista` | Zootecnista | — | Aprobar o descartar ajustes de ración por pesaje |
| `tecnico` | Técnico de mantenimiento | — | Iniciar y certificar reparaciones |
| `dueno` | Dueño | — | Indicadores, inventario y alertas en solo lectura |

| Galpón | Lote | Estado inicial |
|---|---|---|
| Galpón 1 (600) | 495 aves, 3 semanas | Sin turno hoy |
| Galpón 2 (900) | 800 aves, 5 semanas, en observación | Turno de ayer bloqueado (2 %), orden de malla vencida |
| Galpón 3 (700) | 597 aves, 2 semanas | Turno de hoy abierto, pesaje pendiente de revisión (−5,8 %) |
| Galpón 4 (1000) | Sin lote | Para registrar un lote nuevo |

La bodega arranca con el alimento en 50,05 % de su capacidad, así que cualquier
cierre que consuma más de 5 kg dispara la alerta de stock crítico. Hay dos
semanas de cierres aprobados para que los indicadores tengan historial.

## Funcionalidades

| ID | Funcionalidad | Reglas | Rol que la ejecuta |
|---|---|---|---|
| F-01 | Cierre de turno operativo con campos obligatorios | RN-05 | Operario |
| F-02 | Alerta sanitaria por mortalidad y atención veterinaria | RN-01, RN-07 | Operario / Veterinario |
| F-03 | Descuento de alimento y alerta de stock crítico | RN-03 | Automático al cerrar |
| F-04 | Aprobación de reportes, cuadre de bodega y consolidación de KPIs | RN-06 | Administrador |
| F-05 | Acta de ajuste de inventario | RN-08 | Administrador |
| F-06 | Ración diaria por galpón | — | Consulta |
| F-07 | Pesaje semanal y ajuste de ración | RN-02 | Operario / Zootecnista |
| F-08 | Reporte de daños y órdenes de mantenimiento | RN-04 | Operario / Técnico |
| F-09 | Autenticación con JWT y autorización por permisos | — | Todos |
| F-10 | Turnos operativos con estados y panel del administrador | — | Operario / Administrador |
| F-11 | Galpones, lotes y asignación de operarios | — | Administrador |

Documentadas para la siguiente iteración: gestión de usuarios (F-12),
recuperación de contraseña y cierre de sesión seguro (F-13), notificaciones
push/SMS reales (F-14), clasificación de huevos (F-15) y temperatura y humedad
(F-16).

### Reglas de negocio

| Regla | Qué hace el sistema | Dónde vive |
|---|---|---|
| RN-01 | Mortalidad del día > 1,5 % → bloquea el cierre, pone el lote en observación y avisa al veterinario y al administrador | `ReglaMortalidad`, `PoliticaMortalidad` |
| RN-02 | Peso fuera de ±5 % del estándar → el zootecnista decide si se ajusta la ración | `PoliticaPeso`, `RacionAjustadaPorPeso` |
| RN-03 | Alimento por debajo del 50 % de la capacidad → avisa al administrador y al dueño | `ReglaDescontarInventario`, `PoliticaStock` |
| RN-04 | Daño reportado → galpón en riesgo y plazo de reparación de 12 h | `FabricaOrdenMantenimiento`, `OrdenMantenimiento` |
| RN-05 | No se cierra el turno sin consumo, bajas, causa (si hay bajas) y producción | `ReglaCamposObligatorios` |
| RN-06 | No se consolida el día si el conteo físico de la bodega no cuadra con el sistema | `PoliticaConciliacion`, `ConsolidarReporteDiarioService` |
| RN-07 | % mortalidad = bajas de hoy / población del día × 100 | `PoliticaMortalidad` |
| RN-08 | Solo el administrador ajusta el inventario, mediante un acta | `Rol`, `ComandoAjusteInventario` |

## Roles y permisos

Cada endpoint y cada opción del menú exige un **permiso**, no un rol; la matriz
vive en `Rol.java` y la fija `RolTest`. Hay segregación de funciones: el
administrador supervisa en solo lectura y aprueba, pero no ejecuta el trabajo de
los demás roles (quien aprueba no es quien registra).

| Rol | Permisos |
|---|---|
| Operario | Iniciar y cerrar turno, registrar peso, reportar daños, ver lotes y ración |
| Administrador | Ver turnos, aprobar reportes, gestionar galpones, ver y ajustar inventario, ver KPIs, ver alertas, pesajes, órdenes, lotes y ración |
| Veterinario | Ver y atender alertas sanitarias, ver lotes |
| Zootecnista | Ver y revisar pesajes, ver lotes y ración |
| Técnico de mantenimiento | Ver y atender órdenes de mantenimiento, ver lotes |
| Dueño | Ver KPIs, inventario, alertas y lotes |

Además de los permisos, el dominio valida la propiedad: un operario solo cierra
el turno que él inició y solo reporta daños de su galpón asignado.

## Arquitectura

Hexagonal (puertos y adaptadores). El paquete `domain` no depende de Spring ni
de ningún framework; `infrastructure` implementa los puertos y expone la API.
Los servicios se crean en `UseCaseConfig`, el único lugar donde Spring conoce el
dominio.

```
backend/src/main/java/com/avimanager/
  domain/
    model/       -> entidades, estados (State) y builders
    policy/      -> reglas de cálculo puras (mortalidad, peso, stock, conciliación)
    event/       -> eventos de dominio (Observer)
    exception/   -> excepciones de negocio
    port/in/     -> casos de uso, comandos y resultados
    port/out/    -> repositorios, publicador de eventos, cifrado y tokens
    service/     -> implementación de los casos de uso
      cierre/        -> cadena de reglas del cierre de turno
      racion/        -> estrategias de ración
      kpi/           -> calculadoras de indicadores
      inventario/    -> comando de ajuste de inventario
      mantenimiento/ -> fábrica de órdenes
  infrastructure/
    adapter/in/web/           -> controladores REST, DTOs y manejo de errores
    adapter/out/persistence/  -> repositorios en memoria
    adapter/out/event/        -> publicador de eventos en memoria
    adapter/out/notification/ -> notificadores (escriben en el log)
    adapter/out/security/     -> BCrypt y JWT
    config/                   -> cableado, seguridad y datos de demostración
```

Cambiar la persistencia en memoria por una base de datos, o las notificaciones
del log por SMS reales, es escribir un adaptador nuevo para el puerto
correspondiente: el dominio y los casos de uso no cambian.

### Patrones de diseño

| Patrón | Dónde |
|---|---|
| State | `EstadoTurno`, `EstadoOrden` |
| Chain of Responsibility + Template Method | `ReglaCierreTurno` y sus eslabones en `service/cierre/` |
| Observer | `PublicadorEventosPort`, `PublicadorEventosEnMemoria` y los notificadores |
| Strategy | `EstrategiaRacion` (ración) y `CalculadoraKpi` (indicadores) |
| Factory | `FabricaOrdenMantenimiento` |
| Builder | `GalponBuilder`, `LoteBuilder` |
| Command | `ComandoAjusteInventario` |

La cadena del cierre de turno es: campos obligatorios → valores válidos →
descuento de inventario → mortalidad → consolidación.

## API

Todos los endpoints, excepto el login, exigen `Authorization: Bearer <token>`.
Sin token responden 401 y sin el permiso, 403.

| Método y ruta | Permiso |
|---|---|
| `POST /api/auth/login` · `GET /api/auth/me` | Público · autenticado |
| `POST /api/turnos/inicio` · `GET /api/turnos/actual` | `INICIAR_TURNO` |
| `POST /api/lotes/{loteId}/turnos/cierre` | `CERRAR_TURNO` |
| `GET /api/turnos` · `POST /api/turnos/{id}/aprobacion` | `VER_TURNOS` · `APROBAR_REPORTES` |
| `GET /api/alertas` · `POST /api/alertas/{id}/atencion` | `VER_ALERTAS_SANITARIAS` · `ATENDER_ALERTAS_SANITARIAS` |
| `GET /api/racion` | `VER_RACION` |
| `GET /api/pesajes/referencia` · `POST /api/pesajes` | `REGISTRAR_PESO` |
| `GET /api/pesajes` · `POST /api/pesajes/{id}/revision` | `VER_PESAJES` · `REVISAR_PESAJES` |
| `GET /api/danos/referencia` · `POST /api/danos` | `REPORTAR_DANOS` |
| `GET /api/ordenes-mantenimiento` · `POST .../{id}/inicio` · `POST .../{id}/certificacion` | `VER_ORDENES_MANTENIMIENTO` · `ATENDER_ORDENES_MANTENIMIENTO` |
| `GET /api/inventario` · `GET /api/inventario/ajustes` · `POST /api/inventario/{id}/ajustes` | `VER_INVENTARIO` · `AJUSTAR_INVENTARIO` |
| `GET /api/kpis` · `GET /api/kpis/consolidados` · `POST /api/kpis/consolidacion` | `VER_DASHBOARD_KPI` · `APROBAR_REPORTES` |
| `GET /api/galpones` | `VER_LOTES` |
| `POST /api/galpones` · `POST /api/galpones/{id}/lotes` · `POST /api/lotes/{id}/finalizacion` · `GET /api/workers` · `PUT /api/usuarios/{id}/galpon` | `GESTIONAR_GALPONES` |

Códigos de negocio: 400 dato inválido, 404 no existe, 409 choca con el estado
actual (alerta sanitaria, turno ya iniciado, día ya consolidado, inventario sin
cuadrar…) y 422 cierre con campos incompletos.

### Ejemplo con curl

Con el backend corriendo, inicia sesión como `operario` (Galpón 1, `lote-1`) y
guarda el token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"operario","contrasena":"operario123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

Inicia el turno del día (responde 201):

```bash
curl -X POST http://localhost:8080/api/turnos/inicio -H "Authorization: Bearer $TOKEN"
```

Cierre con campos incompletos (RN-05, responde 422 y el turno sigue abierto):

```bash
curl -X POST http://localhost:8080/api/lotes/lote-1/turnos/cierre \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"cantidadBajas":3,"causaProbableMortalidad":"jadeo","produccionHuevosBandejas":0}'
```

Cierre con mortalidad crítica: 10 bajas sobre 495 aves = 2,02 % (RN-01 y
RN-07, responde 409, bloquea el turno y genera la alerta):

```bash
curl -X POST http://localhost:8080/api/lotes/lote-1/turnos/cierre \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"consumoAlimentoKg":11.0,"cantidadBajas":10,"causaProbableMortalidad":"brote sospechoso","produccionHuevosBandejas":0,"novedades":"aves decaídas"}'
```

Con 7 bajas o menos (≤ 1,41 %) el turno se cierra normal y responde 200.

## Pruebas

```bash
cd backend
mvn test
```

- **Dominio** (sin Spring): servicios, políticas, entidades y patrones, con
  repositorios falsos (`Fake*`) y un reloj fijo.
- **Integración** (`SeguridadIntegracionTest`, MockMvc): la matriz de permisos
  de cada endpoint por rol y los flujos completos de punta a punta.

Los nombres de las pruebas indican la regla que verifican, por ejemplo
`bloqueaElCierreYGeneraAlertaSanitariaCuandoSuperaElUmbral_RN01_RN07`.

## Limitaciones conocidas

- Persistencia en memoria, sin transacciones: los datos se pierden al reiniciar.
- Las notificaciones se escriben en el log del backend.
- Cerrar sesión descarta el token en el navegador; el token expira a las 8 horas.
- La edad del lote se guarda al registrarlo y no avanza sola.
- Los usuarios vienen precargados; su gestión es la funcionalidad F-12.

## Flujo de trabajo

Una rama por funcionalidad (`feature/F-xx-...`, `fix/...`, `refactor/...`) y
pull request hacia `main`. La gestión del proyecto se lleva en Jira por sprints.
