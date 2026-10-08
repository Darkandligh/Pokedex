// Configuración de desarrollo. `ng build` la reemplaza por environment.prod.ts.
// La API se busca en el mismo equipo que sirve la app: así funciona en el navegador
// (localhost) y en un celular de la misma red (http://IP-del-equipo:8100).
export const environment = {
  production: false,
  apiUrl: `http://${window.location.hostname}:8080/api`,
};
