# Lista de verificación manual — Sprint 1

Pruebas para hacer a mano en **navegador de escritorio** y en **celular**, criterio por
criterio. Marcar cada casilla en las dos columnas.

## Preparación

1. Levantar el backend (`cd backend` y `mvnw spring-boot:run`). La primera vez importa los
   151 Pokémon desde PokéAPI (2 a 3 minutos); esperar el mensaje «Importación terminada».
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

## Comprobaciones generales

| Paso | Resultado esperado | Navegador | Móvil |
|------|--------------------|:---------:|:-----:|
| Con la ficha abierta, mirar la pestaña Red (F12) | Las peticiones van a `:8080/api/...` (nuestra API), ninguna a `pokeapi.co` | ☐ | — |
| Detener el backend y recargar una ficha | Mensaje «No se pudo conectar con la API de la Pokédex.» | ☐ | ☐ |
| Girar el celular / reducir el ancho de la ventana | El diseño se reacomoda sin desbordes | ☐ | ☐ |
