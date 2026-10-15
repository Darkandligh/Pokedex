# Lista de verificación manual — Sprints 1 y 2

Pruebas para hacer a mano en **navegador de escritorio** y en **celular**, criterio por
criterio. Marcar cada casilla en las dos columnas.

## Preparación

1. Levantar el backend (`cd backend` y `.\mvnw spring-boot:run`). La primera vez importa los
   151 Pokémon desde PokéAPI (1 a 2 minutos); esperar el mensaje «Importación terminada».
2. Levantar el frontend (`cd frontend` y `ionic serve --external`).
3. **Navegador:** abrir `http://localhost:8100`.
4. **Celular:** conectarlo a la misma red wifi que el computador y abrir
   `http://IP-DEL-COMPUTADOR:8100` (la IP aparece en la salida de `ionic serve --external`).
   Si no carga, permitir Java y Node en el firewall de Windows para redes privadas.
   Alternativa sin celular: en Chrome, F12 → modo dispositivo (Ctrl+Shift+M) → «iPhone SE» o «Pixel 7».

## HU-01 · Ver la ficha de un Pokémon (SCRUM-9)

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA1 | Abrir la ficha de Bulbasaur (`/pokemon/1`) | Se ve «Bulbasaur N.º 0001», la imagen y la descripción en español | ☐ | ☐ |
| CA2 | En la misma ficha, revisar el recuadro de datos | Altura 0,7 m · Peso 6,9 kg · Género · Categoría «Semilla» · Habilidad «Espesura» · Tipos Planta y Veneno con color · Debilidades Volador, Fuego, Psíquico, Hielo | ☐ | ☐ |
| CA2 | Abrir Charizard (`/pokemon/6`) | Debilidades: Roca, Agua, Eléctrico (Tierra no aparece por ser Volador) | ☐ | ☐ |
| CA3 | Revisar «Estadísticas base» | 6 barras: PS, Ataque, Defensa, Ataque Especial, Defensa Especial, Velocidad, con su valor | ☐ | ☐ |
| CA3 | En «Evoluciones», pulsar Ivysaur y luego Venusaur | Cada enlace abre la ficha de ese Pokémon; el actual queda resaltado | ☐ | ☐ |
| CA3 | Abrir Pikachu (`/pokemon/25`) | Pichu aparece atenuado y sin enlace (no está entre los 151 importados); Raichu sí enlaza | ☐ | ☐ |
| CA4 | Abrir `/pokemon/9999` | Mensaje «Pokémon no encontrado» | ☐ | ☐ |
| CA4 | Abrir `/pokemon/missingno` | Mensaje «Pokémon no encontrado» | ☐ | ☐ |

## HU-02 · Ver el listado de la Pokédex (SCRUM-10)

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA1 | Abrir `http://localhost:8100` y pulsar «Ver la Pokédex» (desde el Sprint 2 la app abre en la portada) | Se ve el listado; el primero es Bulbasaur N.º 0001 y siguen en orden hasta Mew N.º 0151, cada uno con imagen, nombre y tipos con color | ☐ | ☐ |
| CA2 | Pulsar la tarjeta de Pikachu | Se abre la ficha de Pikachu N.º 0025 | ☐ | ☐ |
| CA2 | En la ficha, pulsar «Pokédex» (volver) | Regresa al listado | ☐ | ☐ |

## HU-03 · Buscar un Pokémon por nombre o número (SCRUM-11)

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA1 | En el buscador escribir `char` | Aparecen Charmander, Charmeleon y Charizard | ☐ | ☐ |
| CA1 | Escribir `PIKA` (mayúsculas) | Aparece Pikachu | ☐ | ☐ |
| CA1 | Escribir `25`, luego `025` y luego `0025` | En los tres casos aparece solo Pikachu N.º 0025 | ☐ | ☐ |
| CA1 | Pulsar el resultado | Se abre su ficha | ☐ | ☐ |
| CA2 | Escribir `xyz` | Mensaje «No se encontraron Pokémon» | ☐ | ☐ |
| CA2 | Escribir `9999` | Mensaje «No se encontraron Pokémon» | ☐ | ☐ |
| — | Borrar el texto del buscador | Vuelve el listado completo | ☐ | ☐ |

## HU-05 · Navegar al Pokémon anterior o siguiente (SCRUM-13)

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA1 | Abrir la ficha de Ivysaur N.º 0002 y pulsar «Anterior» | Se abre Bulbasaur N.º 0001 | ☐ | ☐ |
| CA1 | Volver a Ivysaur y pulsar «Siguiente» | Se abre Venusaur N.º 0003 | ☐ | ☐ |
| — | En Bulbasaur N.º 0001 | No aparece «Anterior» | ☐ | ☐ |
| — | En Mew N.º 0151 | No aparece «Siguiente» | ☐ | ☐ |

## HU-04 · Filtrar Pokémon por tipo (SCRUM-12) — Sprint 2

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| — | Abrir el listado | Debajo del buscador aparece «Todos» (marcado) y un botón por tipo con su color | ☐ | ☐ |
| CA1 | Pulsar «Fuego» | Solo Pokémon de tipo Fuego (Charmander, Charmeleon, Charizard, Vulpix…), en orden de número | ☐ | ☐ |
| CA1 | Pulsar «Volador» | Aparece también Charizard (Fuego/Volador): cuenta el tipo secundario | ☐ | ☐ |
| CA2 | Con «Fuego» elegido, escribir `char` | Charmander, Charmeleon y Charizard | ☐ | ☐ |
| CA2 | Con «Agua» elegido, escribir `pika` | Mensaje «No se encontraron Pokémon» | ☐ | ☐ |
| CA3 | Pulsar «Todos» | Vuelve el listado completo (con la búsqueda que haya) | ☐ | ☐ |
| CA3 | Pulsar «Fuego» dos veces | La segunda vez se quita el filtro | ☐ | ☐ |
| — | En el celular, deslizar la fila de tipos | Se desplaza de lado sin mover la página | — | ☐ |

## HU-06 · Ver noticias recientes en la portada (SCRUM-14) — Sprint 2

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA1 | Abrir `http://localhost:8100` | Se abre la portada con «Noticias recientes»: 6 tarjetas con imagen, fecha, título y resumen | ☐ | ☐ |
| CA1 | Revisar el orden de las tarjetas | De la más reciente a la más antigua: la primera es «Filtra la Pokédex por tipo» (15 de octubre de 2026) | ☐ | ☐ |
| CA2 | Pulsar «Ver la Pokédex» | Se abre el listado | ☐ | ☐ |
| CA2 | En el listado, pulsar «Inicio» | Vuelve a la portada | ☐ | ☐ |
| — | Con el backend detenido, recargar la portada | Mensaje «No se pudieron cargar las noticias.» y el botón «Ver la Pokédex» sigue ahí | ☐ | ☐ |
| — | Abrir una dirección que no existe (`/xyz`) | Lleva a la portada | ☐ | ☐ |
| — | En el celular | Las tarjetas quedan una debajo de otra, sin desbordes | — | ☐ |

El caso «sin noticias» (CA3, mensaje «No hay noticias por ahora») está cubierto por las pruebas
automáticas (`CriteriosAceptacionHu06Test.ca3…` y `portada.page.spec.ts`), porque la base siempre
arranca con las noticias de ejemplo.

## HU-13 · Registrar, editar y eliminar Pokémon (SCRUM-21) — Sprint 2

Administrador por defecto: usuario `admin`, clave `pokedex2026` (se cambian con las variables
`POKEDEX_ADMIN_USUARIO` y `POKEDEX_ADMIN_CLAVE`). Las pruebas usan el N.º 0152 (Chikorita), que no
está entre los 151 importados.

| # | Paso | Resultado esperado | Navegador | Móvil |
|---|------|--------------------|:---------:|:-----:|
| CA5 | En el listado pulsar «Administrar» sin haber iniciado sesión | Se abre «Iniciar sesión» | ☐ | ☐ |
| CA5 | Ingresar con usuario `admin` y clave `otra` | Mensaje «Usuario o clave incorrectos»; no entra | ☐ | ☐ |
| CA5 | Abrir directamente `/admin/pokemon/nuevo` en una pestaña nueva | Pide iniciar sesión y, al ingresar, abre el formulario | ☐ | ☐ |
| — | Ingresar con `admin` / `pokedex2026` | Se abre el panel con los 151 Pokémon y los botones Editar y Eliminar | ☐ | ☐ |
| CA1 | «+ Registrar Pokémon»: N.º 152, Chikorita, categoría Hoja, habilidad Espesura, 0.9 m, 6.4 kg, tipo Planta, estadísticas 45 49 65 49 65 45 | Aviso «Se registró a Chikorita.»; aparece al final del panel | ☐ | ☐ |
| CA1 | Abrir `/pokemon/152` | Ficha de Chikorita con imagen oficial, tipo Planta y debilidades Volador, Veneno, Bicho, Fuego, Hielo | ☐ | ☐ |
| CA2 | En el panel, «Editar» Chikorita: cambiar el nombre a «Chikorita Shiny» y la velocidad a 80 | Aviso «Se guardaron los cambios…»; la ficha muestra el nombre y la velocidad nuevos | ☐ | ☐ |
| CA2 | Editar un Pokémon y mirar el campo «Número» | Está bloqueado: el número no se puede cambiar | ☐ | ☐ |
| CA3 | «Registrar» con el formulario vacío | No se guarda; «Revisa los campos marcados» y cada campo obligatorio marcado en rojo | ☐ | ☐ |
| CA3 | Escribir una estadística de 300 y la URL de imagen `foto.png` | Mensajes «Debe ser menor o igual a 255» y «Debe ser una URL que empiece por http:// o https://» | ☐ | ☐ |
| CA3 | Registrar con el N.º 1 | Mensaje «Ya existe un Pokémon con el N.º 0001» | ☐ | ☐ |
| CA3 | Registrar con el nombre «pikachu» (otro número) | Mensaje «Ya existe un Pokémon llamado Pikachu» | ☐ | ☐ |
| CA4 | «Eliminar» Chikorita y pulsar «Cancelar» | No se elimina | ☐ | ☐ |
| CA4 | «Eliminar» Chikorita y pulsar «Eliminar» | Aviso «Se eliminó a Chikorita Shiny.»; `/pokemon/152` muestra «Pokémon no encontrado» | ☐ | ☐ |
| — | Pulsar «Salir» en el panel | Vuelve al listado; «Administrar» vuelve a pedir la clave | ☐ | ☐ |

## Comprobaciones generales

| Paso | Resultado esperado | Navegador | Móvil |
|------|--------------------|:---------:|:-----:|
| Con la ficha abierta, mirar la pestaña Red (F12) | Las peticiones van a `:8080/api/...` (nuestra API), ninguna a `pokeapi.co` | ☐ | — |
| Detener el backend y recargar una ficha | Mensaje «No se pudo conectar con la API de la Pokédex.» | ☐ | ☐ |
| Girar el celular / reducir el ancho de la ventana | El diseño se reacomoda sin desbordes | ☐ | ☐ |
