# AviManager

Sistema de gestión de galpones avícolas — proyecto de Ingeniería de Software II.

Basado en el student book "Software Modeling" (dominio: gestión de galpones,
inventario, mortalidad y mantenimiento de una granja avícola) y en el
`Plan_de_Requisitos_Galpon` (trazabilidad BR → StR → SyR → SWR).

Monorepo dividido en:

```
backend/   -> API REST en Spring Boot (arquitectura hexagonal)
frontend/  -> Interfaz web en React + Vite
```

## Backend

### Arquitectura

Hexagonal (puertos y adaptadores). El paquete `domain` no depende de Spring
ni de ningún framework; solo define modelo, políticas de negocio y puertos
(interfaces). El paquete `infrastructure` implementa esos puertos (adaptadores)
y expone la API REST.

```
backend/src/main/java/com/avimanager/
  domain/
    model/       -> entidades (Lote, Galpon, ReporteDiario, AlertaSanitaria...)
    policy/      -> reglas de negocio puras (RN-01, RN-07...)
    port/in/     -> casos de uso (lo que la app ofrece)
    port/out/    -> lo que la app necesita de afuera (repos, notificador)
    service/     -> implementación de los casos de uso (orquesta los puertos)
    exception/
  infrastructure/
    adapter/in/web/          -> controladores REST + DTOs
    adapter/out/persistence/ -> repositorios en memoria (implementan port/out)
    adapter/out/notification/-> notificador simulado (push/SMS al veterinario)
    config/                  -> wiring de Spring (beans) y datos de demo
```

Cambiar de persistencia en memoria a una base de datos real, o el canal de
notificación por uno real, implica escribir un nuevo adaptador que implemente
el puerto correspondiente — el dominio y los casos de uso no cambian.

### Funcionalidades implementadas

Trazabilidad con `Plan_de_Requisitos_Galpon_2026_v3.xlsx`:

- **F-01 — Registrar cierre de turno operativo** (RN-05): el sistema bloquea
  el cierre de turno ("Guardar y Salir") si faltan campos obligatorios
  (consumo de alimento, mortalidad, causa probable si hubo bajas, producción).
- **F-02 — Notificar alerta sanitaria de mortalidad** (RN-01 / RN-07): al
  registrar la mortalidad del día, el sistema calcula automáticamente
  `% Mortalidad = (Aves Muertas Hoy / Población Inicial del Día) × 100`;
  si supera 1.5%, bloquea el cierre, pone el lote en observación, genera una
  alerta sanitaria y notifica al veterinario.

Ambas comparten el mismo caso de uso (`CerrarTurnoUseCase` /
`CerrarTurnoService`), tal como lo describe el flujo de negocio del student
book: el registro de mortalidad es un subflujo del cierre de turno.

### Ejecutar

```bash
cd backend
mvn spring-boot:run
```

### Probar la API

Con la app corriendo (puerto 8080), hay un lote de demo precargado:
`lote-1` (galpón `galpon-1`, población inicial del día = 500 aves).

Turno normal (cierra exitosamente):

```bash
curl -X POST http://localhost:8080/api/lotes/lote-1/turnos/cierre \
  -H "Content-Type: application/json" \
  -d '{"consumoAlimentoKg":45.0,"cantidadBajas":5,"causaProbableMortalidad":"jadeo","produccionHuevosBandejas":12,"novedades":"sin novedad"}'
```

Campos incompletos (F-01 / RN-05 — responde 422):

```bash
curl -X POST http://localhost:8080/api/lotes/lote-1/turnos/cierre \
  -H "Content-Type: application/json" \
  -d '{"cantidadBajas":3,"causaProbableMortalidad":"jadeo","produccionHuevosBandejas":10}'
```

Mortalidad crítica (F-02 / RN-01, RN-07 — responde 409, genera alerta y notifica):

```bash
curl -X POST http://localhost:8080/api/lotes/lote-1/turnos/cierre \
  -H "Content-Type: application/json" \
  -d '{"consumoAlimentoKg":45.0,"cantidadBajas":10,"causaProbableMortalidad":"enfermedad","produccionHuevosBandejas":12,"novedades":"brote sospechoso"}'
```

### Pruebas

```bash
cd backend
mvn test
```

Las pruebas del dominio (`CerrarTurnoServiceTest`, `PoliticaMortalidadTest`)
no levantan Spring: usan dobles de prueba simples de los puertos, lo que
confirma que la lógica de negocio es independiente de la infraestructura.

### Próximas funcionalidades (F-03 a F-08)

Cada nueva funcionalidad del `Plan_de_Requisitos_Galpon` se agrega como un
nuevo caso de uso en `domain/port/in` + `domain/service`, reutilizando o
extendiendo los puertos de salida existentes (o agregando nuevos) sin romper
lo ya construido.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Levanta en `http://localhost:5173` y consume la API del backend
(`http://localhost:8080`) a través del proxy configurado en `vite.config.js`.
