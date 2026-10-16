# Pokédex — Sprints 1 y 2

Clon simplificado de la Pokédex en español, hecho para la materia Ingeniería de Software I
(Fundación Escuela Tecnológica de Neiva — FET).
Equipo: Eduardo Ascencio, Juan Diego Trujillo y Juan José Garzón.

**Sprint 1:** 24 de septiembre al 8 de octubre de 2026 · 4 historias · 18 puntos.
Objetivo: un visitante puede buscar un Pokémon, ver el listado y abrir su ficha completa.

**Sprint 2 · Admin y noticias:** 8 al 22 de octubre de 2026 · 3 historias · 18 puntos.
Objetivo: un visitante puede filtrar la Pokédex por tipo y ver noticias en la portada, y un
administrador puede registrar, editar y eliminar Pokémon.

Las historias completas están en [historias-pokedex.pdf.pdf](historias-pokedex.pdf.pdf).

| Capa | Tecnología |
|------|------------|
| Backend | Java 21 · Spring Boot 3.5 · Spring Data JPA · Spring Security · Maven Wrapper |
| Base de datos | H2 en archivo (por defecto) · PostgreSQL (perfil opcional) |
| Frontend | Ionic 9 · Angular 22 (componentes standalone) |
| Datos | Importados una vez desde [PokéAPI](https://pokeapi.co) a nuestra base |

## Requisitos

- **Java 21** (JDK), con `JAVA_HOME` configurado
- **Node.js** y **npm**
- **Ionic CLI** (`npm install -g @ionic/cli`)
- Internet solo la primera vez que arranca el backend, para importar los datos.
  Las pruebas automáticas no necesitan internet.

No hace falta instalar Maven ni una base de datos.

## Cómo levantar el proyecto

Primera vez, instalar las dependencias del frontend: `cd frontend` y luego `npm install`.

**1. Backend** (http://localhost:8080):

```bash
cd backend
./mvnw spring-boot:run          # en Windows (PowerShell): .\mvnw spring-boot:run
```

**2. Frontend** (http://localhost:8100), en otra terminal:

```bash
cd frontend
ionic serve
```

La primera vez, el backend importa los 151 Pokémon de la primera generación desde PokéAPI.
Tarda alrededor de 1 a 2 minutos; al terminar aparece en el log `Importación terminada: 151 nuevos`.
Los datos quedan en `backend/data/` y en los siguientes arranques no se vuelve a consultar PokéAPI.
Si PokéAPI no responde, el backend arranca igual con lo que ya haya en la base.
Cuando la importación termina completa queda registrada, así los Pokémon que elimine un
administrador no vuelven a importarse al reiniciar.

La app abre en la **portada** (noticias y botón «Ver la Pokédex»). Las noticias de ejemplo se
cargan solas la primera vez, desde `backend/src/main/resources/noticias-ejemplo.json`.

### Administrador (HU-13)

Registrar, editar y eliminar Pokémon exige iniciar sesión como administrador («Administrar» en
el listado). Usuario y clave por defecto, **solo para desarrollo**:

| Usuario | Clave | Cómo cambiarlos |
|---------|-------|-----------------|
| `admin` | `pokedex2026` | Variables de entorno `POKEDEX_ADMIN_USUARIO` y `POKEDEX_ADMIN_CLAVE` |

La sesión dura mientras la pestaña del navegador esté abierta.

Para probar en un **celular** de la misma red wifi, usar `ionic serve --external` y abrir
`http://IP-DEL-COMPUTADOR:8100`. La app busca la API en esa misma IP, en el puerto 8080.

### Perfiles del backend

| Perfil | Cómo activarlo | Para qué |
|--------|----------------|----------|
| *(por defecto)* | `.\mvnw spring-boot:run` | H2 en archivo + importación desde PokéAPI |
| `datosfijos` | `.\mvnw spring-boot:run "-Dspring-boot.run.profiles=datosfijos"` | Pokémon N.º 0001 a 0009 en memoria, sin internet. Lo usan las pruebas |
| `postgres` | `.\mvnw spring-boot:run "-Dspring-boot.run.profiles=postgres"` | PostgreSQL (variables `POSTGRES_URL`, `POSTGRES_USUARIO`, `POSTGRES_CLAVE`) |

La cantidad a importar se configura con `pokedex.importacion.cantidad` (151 por defecto) en
`backend/src/main/resources/application.properties`.

## Endpoints

| Método | Ruta | Descripción | Historia |
|--------|------|-------------|----------|
| GET | `/api/pokemon` | Listado ordenado por número (número, nombre, imagen, tipos) | HU-02 |
| GET | `/api/pokemon?q=char` | Filtra por nombre parcial, sin distinguir mayúsculas | HU-03 |
| GET | `/api/pokemon?q=0025` | Filtra por número; acepta `25`, `025` y `0025` | HU-03 |
| GET | `/api/pokemon/{numero}` | Ficha completa, con cadena evolutiva y `anterior`/`siguiente` (null en los extremos) | HU-01, HU-05 |
| GET | `/api/pokemon/nombre/{nombre}` | Ficha completa por nombre | HU-01 |
| GET | `/api/pokemon?tipo=fire` | Filtra por tipo (principal o secundario); se combina con `q` | HU-04 |
| GET | `/api/tipos` | Tipos en el orden oficial, para el filtro y el formulario | HU-04, HU-13 |
| GET | `/api/noticias?limite=6` | Noticias de la más reciente a la más antigua (límite de 1 a 20) | HU-06 |
| POST | `/api/pokemon` | Registrar un Pokémon (`201` con la ficha) · **administrador** | HU-13 |
| PUT | `/api/pokemon/{numero}` | Editar un Pokémon; el número no cambia · **administrador** | HU-13 |
| DELETE | `/api/pokemon/{numero}` | Eliminar un Pokémon (`204`) · **administrador** | HU-13 |
| GET | `/api/admin/sesion` | Comprueba las credenciales del administrador | HU-13 |

Las consultas (GET) son públicas. Las operaciones de **administrador** usan HTTP Basic
(`Authorization: Basic ...`); sin credenciales responden `401` y con un usuario sin el rol, `403`.

Cuando el Pokémon no existe, la respuesta es `404` con `{"estado": 404, "mensaje": "Pokémon no encontrado"}`.
Una búsqueda sin coincidencias devuelve `200` con una lista vacía, y el frontend muestra «No se encontraron Pokémon».
Si los datos al registrar o editar no son válidos, la respuesta es `400` con el mensaje de cada campo
(`{"estado": 400, "mensaje": "Datos inválidos", "errores": {"nombre": "El nombre es obligatorio"}}`);
un número o nombre repetido responde `409`.

## Comprometido vs. Hecho

### Sprint 1

Estado del código al cierre del Sprint 1.

| Historia | Criterio de aceptación comprometido | Hecho | Evidencia (pruebas automáticas) |
|----------|-------------------------------------|:-----:|---------------------------------|
| **HU-01** Ver la ficha de un Pokémon · Must · 8 pts (SCRUM-9) | CA1. Veo número, nombre, imagen y descripción | ✅ | `CriteriosAceptacionHu01Test.ca1…`, `ficha.page.spec.ts` |
| | CA2. Veo altura, peso, género, categoría, habilidad, tipos y debilidades | ✅ | `CriteriosAceptacionHu01Test.ca2…`, `CalculadorDebilidadesTest` |
| | CA3. Veo las 6 estadísticas base y la cadena evolutiva con enlace a cada Pokémon | ✅ | `CriteriosAceptacionHu01Test.ca3…`, `ficha.page.spec.ts` |
| | CA4. Si no existe, veo «Pokémon no encontrado» | ✅ | `CriteriosAceptacionHu01Test.ca4…`, `ficha.page.spec.ts` |
| **HU-02** Ver el listado de la Pokédex · Must · 5 pts (SCRUM-10) | CA1. Veo los Pokémon ordenados por número con imagen, nombre y tipos | ✅ | `CriteriosAceptacionHu02Test.ca1…`, `listado.page.spec.ts` |
| | CA2. Al seleccionar uno se abre su ficha | ✅ | `CriteriosAceptacionHu02Test.ca2…`, `listado.page.spec.ts` |
| **HU-03** Buscar por nombre o número · Must · 3 pts (SCRUM-11) | CA1. Al buscar veo los Pokémon que coinciden | ✅ | `CriteriosAceptacionHu03Test.ca1…`, `listado.page.spec.ts` |
| | CA2. Sin coincidencias veo «No se encontraron Pokémon» | ✅ | `CriteriosAceptacionHu03Test.ca2…`, `listado.page.spec.ts` |
| **HU-05** Navegar al anterior o siguiente · Should · 2 pts (SCRUM-13) | CA1. En el N.º 0002, «anterior» va al 0001 y «siguiente» al 0003 | ✅ | `CriteriosAceptacionHu05Test.ca1…`, `ficha.page.spec.ts` |

**Puntos:** 18 comprometidos · 18 hechos.

### Subtareas de la HU-01

| Subtarea | Capa | Hecho | Commit |
|----------|------|:-----:|--------|
| T-01.1 Modelar entidades Pokémon, Tipo y Evolución | Backend | ✅ | `SCRUM-9 T-01.1 …` |
| T-01.2 Implementar la consulta de Pokémon por nombre en la API | Backend | ✅ | `SCRUM-9 T-01.2 …` |
| T-01.3 Pantalla de la ficha en Ionic (datos, tipos y estadísticas) | Frontend | ✅ | `SCRUM-9 T-01.3 …` |
| T-01.4 Cadena evolutiva y mensaje de «no encontrado» | Frontend | ✅ | `SCRUM-9 T-01.4 …` |
| T-01.5 Datos de prueba y conexión app–API | Integración | ✅ | `SCRUM-9 T-01.5 …` |
| T-01.6 Probar CA1 a CA4 en navegador y móvil | QA | ⚠️ Parcial | `SCRUM-9 T-01.6 …` |

T-01.6: las pruebas automáticas están hechas y pasan. La prueba manual en navegador y en celular
se hace con la lista [docs/verificacion-manual.md](docs/verificacion-manual.md), que el equipo
debe completar y marcar.

### Sprint 2 · Admin y noticias

Estado del código en las ramas `feature/hu-13-administrar-pokemon`, `feature/hu-04-filtrar-por-tipo`
y `feature/hu-06-noticias-portada`. Los criterios son los de cada historia en Jira.

| Historia | Criterio de aceptación comprometido | Hecho | Evidencia (pruebas automáticas) |
|----------|-------------------------------------|:-----:|---------------------------------|
| **HU-13** Registrar, editar y eliminar Pokémon · Must · 8 pts (SCRUM-21) | CA1. Al registrar un Pokémon aparece en el listado y tiene su ficha | ✅ | `CriteriosAceptacionHu13Test.ca1…`, `formulario.page.spec.ts` |
| | CA2. Al editar un Pokémon su ficha muestra los cambios | ✅ | `CriteriosAceptacionHu13Test.ca2…`, `formulario.page.spec.ts` |
| | CA3. Con un campo obligatorio vacío o un número repetido veo un error y no se guarda nada | ✅ | `CriteriosAceptacionHu13Test.ca3…`, `formulario.page.spec.ts` |
| | CA4. Al eliminar y confirmar deja de aparecer y su ficha muestra «Pokémon no encontrado» | ✅ | `CriteriosAceptacionHu13Test.ca4…`, `panel.page.spec.ts` |
| | CA5. Si no soy administrador se me niega el acceso | ✅ | `CriteriosAceptacionHu13Test.ca5…`, `auth.spec.ts` |
| **HU-04** Filtrar Pokémon por tipo · Should · 5 pts (SCRUM-12) | CA1. Al elegir un tipo veo solo los Pokémon de ese tipo, ordenados por número | ✅ | `CriteriosAceptacionHu04Test.ca1…`, `listado.page.spec.ts` |
| | CA2. Con un tipo elegido, la búsqueda por nombre o número se aplica solo sobre ese tipo | ✅ | `CriteriosAceptacionHu04Test.ca2…`, `listado.page.spec.ts` |
| | CA3. Al quitar el filtro vuelvo a ver el listado completo | ✅ | `CriteriosAceptacionHu04Test.ca3…`, `listado.page.spec.ts` |
| **HU-06** Ver noticias recientes en la portada · Should · 5 pts (SCRUM-14) | CA1. Veo las noticias de la más reciente a la más antigua, con imagen, fecha, título y resumen | ✅ | `CriteriosAceptacionHu06Test.ca1…`, `portada.page.spec.ts` |
| | CA2. En la portada veo un acceso a la Pokédex | ✅ | `portada.page.spec.ts` |
| | CA3. Sin noticias veo «No hay noticias por ahora» | ✅ | `CriteriosAceptacionHu06Test.ca3…`, `portada.page.spec.ts` |

| Tarea | Capa | Hecho | Commit |
|-------|------|:-----:|--------|
| T-13.1 Endpoints para registrar, editar y eliminar con validaciones | Backend | ✅ | `SCRUM-21 T-13.1 …` |
| T-13.2 Proteger la administración con rol de administrador | Backend | ✅ | `SCRUM-21 T-13.2 …` |
| T-13.3 Formulario de registro, edición y eliminación en Ionic | Frontend | ✅ | `SCRUM-21 T-13.3 …` |
| T-13.4 Probar CA1 a CA5 en navegador y móvil | QA | ⚠️ Falta marcar | `SCRUM-21 T-13.4 …` |
| T-04.1 Filtro por tipo en la API | Backend | ✅ | `SCRUM-12 T-04.1 …` |
| T-04.2 Selector de tipos en el listado | Frontend | ✅ | `SCRUM-12 T-04.2 …` |
| T-04.3 Probar CA1 a CA3 en navegador y móvil | QA | ⚠️ Falta marcar | `SCRUM-12 T-04.3 …` |
| T-06.1 Entidad Noticia, datos de ejemplo y endpoint de noticias | Backend | ✅ | `SCRUM-14 T-06.1 …` |
| T-06.2 Portada con tarjetas de noticias y acceso a la Pokédex | Frontend | ✅ | `SCRUM-14 T-06.2 …` |
| T-06.3 Probar CA1 a CA3 en navegador y móvil | QA | ⚠️ Falta marcar | `SCRUM-14 T-06.3 …` |

Las tareas de QA tienen sus casos en [docs/verificacion-manual.md](docs/verificacion-manual.md);
quedan pendientes hasta que el equipo los pruebe y marque en navegador y celular.

## Pruebas automáticas

```bash
cd backend && ./mvnw test                 # 66 pruebas: servicio (unitarias) y endpoints (MockMvc), perfil datosfijos
cd frontend && npx ng test --watch=false  # 34 pruebas: portada, listado, ficha, administración y sesión
```

Las pruebas de backend corren sin internet sobre el perfil `datosfijos` y cubren cada criterio de
aceptación, incluidos «Pokémon no encontrado», las búsquedas sin coincidencias, las validaciones
del formulario y el acceso sin ser administrador.

## Decisiones de diseño

- **El frontend nunca consulta PokéAPI.** Todos los datos salen de nuestra API. Solo las imágenes
  (official-artwork) se cargan desde el repositorio público de sprites de PokéAPI, a partir de la
  URL guardada en nuestra base.
- **Debilidades:** se calculan multiplicando las relaciones de daño (×2, ×½, ×0) de los tipos del
  Pokémon. Así, en un Pokémon de dos tipos, una resistencia de un tipo anula la debilidad del otro.
  Por ejemplo, Bulbasaur (Planta/Veneno) no es débil a Veneno ni a Bicho.
- **Cadenas evolutivas fuera del rango importado** (por ejemplo, Pichu en la cadena de Pikachu):
  se muestran atenuadas y sin enlace, para no llevar a una ficha inexistente.
- **Habilidad:** se muestra la primera habilidad no oculta, con su nombre en español.
- **Género:** se deriva de `gender_rate` de PokéAPI («Macho y hembra», «Macho», «Hembra», «Desconocido»).
- El diseño y el ícono son propios; no se usan logos ni recursos gráficos de pokemon.com.
- **Debilidades de los Pokémon registrados por un administrador** (HU-13): se calculan con la tabla
  de daño entre tipos guardada en el backend (`TablaTipos`), la misma que entrega PokéAPI, así no
  hace falta internet para registrar.
- **Noticias de ejemplo** (HU-06): cuentan el avance del propio proyecto y usan como imagen la
  ilustración oficial de un Pokémon; publicar noticias desde la app es la HU-12, que sigue en el backlog.

## Estructura

```
backend/   API REST (controller → service → repository, DTOs, seguridad, importador de PokéAPI, noticias de ejemplo)
frontend/  App Ionic (páginas portada, listado, ficha y administración; servicios de la API y de la sesión)
docs/      Lista de verificación manual
```

## Flujo de trabajo con ramas

| Rama | Para qué | Cómo recibe cambios |
|------|----------|---------------------|
| `main` | Producción: la versión entregada | Solo por Pull Request desde `develop`, con aprobación |
| `develop` | Integración y validación | Solo por Pull Request desde una rama de tarea, con aprobación |
| `feature/...` | Una rama por tarea o historia | Libre: cada quien trabaja en la suya |

`main` y `develop` están protegidas: no se puede subir directo a ellas, ni hacer *force push*, ni borrarlas.

**Para trabajar en una tarea:**

```bash
git switch develop
git pull                                   # traer lo último de develop
git switch -c feature/hu-04-nombre-corto   # crear la rama de la tarea
# ... hacer cambios ...
git add .
git commit -m "SCRUM-XX descripción del cambio"
git push -u origin feature/hu-04-nombre-corto
```

Luego, en GitHub, abrir un **Pull Request** de `feature/hu-04-nombre-corto` hacia `develop`.
Antes de abrirlo, correr las pruebas (ver [Pruebas automáticas](#pruebas-automáticas)) y verificar que pasan.

**Nombres de rama:** `feature/hu-XX-descripcion` para historias nuevas y `fix/descripcion` para
corregir errores. En minúsculas, con guiones y sin tildes.
