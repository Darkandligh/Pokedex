# Pokédex — Sprint 1

Clon simplificado de la Pokédex en español, hecho para la materia Ingeniería de Software I
(Fundación Escuela Tecnológica de Neiva — FET).
Equipo: Eduardo Ascencio, Juan Diego Trujillo y Juan José Garzón.

**Objetivo del sprint:** un visitante puede buscar un Pokémon, ver el listado y abrir su ficha completa.
**Sprint 1:** 24 de septiembre al 8 de octubre de 2026 · 4 historias · 18 puntos.
Las historias completas están en [historias-pokedex.pdf.pdf](historias-pokedex.pdf.pdf).

| Capa | Tecnología |
|------|------------|
| Backend | Java 21 · Spring Boot 3.5 · Spring Data JPA · Maven Wrapper |
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

Cuando el Pokémon no existe, la respuesta es `404` con `{"estado": 404, "mensaje": "Pokémon no encontrado"}`.
Una búsqueda sin coincidencias devuelve `200` con una lista vacía, y el frontend muestra «No se encontraron Pokémon».

## Comprometido vs. Hecho

Estado del código en este repositorio al cierre del sprint.

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

## Pruebas automáticas

```bash
cd backend && ./mvnw test                 # 38 pruebas: servicio (unitarias) y endpoints (MockMvc), perfil datosfijos
cd frontend && npx ng test --watch=false  # 12 pruebas: ficha, listado y formato del número
```

Las pruebas de backend corren sin internet sobre el perfil `datosfijos` y cubren cada criterio de
aceptación, incluidos «Pokémon no encontrado» y las búsquedas sin coincidencias.

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

## Estructura

```
backend/   API REST (controller → service → repository, DTOs, importador de PokéAPI)
frontend/  App Ionic (páginas listado y ficha, servicio de la API)
docs/      Lista de verificación manual
```
