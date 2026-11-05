# CloroControl API — servidor local de demostración

API REST para el MVP académico. Requiere Node.js 18+ y no tiene dependencias npm externas. Está pensada únicamente para datos ficticios en una red local de desarrollo.

## Iniciar

```bash
npm start
```

Por defecto escucha en `0.0.0.0:8080`. Comprueba: `http://localhost:8080/api/health`.

El emulador Android utiliza `http://10.0.2.2:8080/api/`. Para un teléfono físico, configura `API_BASE_URL` en el módulo app con la IP LAN del computador. No publiques este servidor ni uses datos reales: no tiene autenticación ni HTTPS.

## Endpoints

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/health` | Estado del servicio. |
| GET | `/api/measurements` | Lista de mediciones. |
| POST | `/api/measurements` | Agrega medición idempotente por `clientId`. |
| GET | `/api/actions` | Lista de acciones. |
| POST | `/api/actions` | Agrega acción idempotente por `clientId`. |
| PUT | `/api/actions/{clientId}` | Actualiza datos/estado de acción existente. |

Las solicitudes y respuestas usan JSON. Los datos se guardan en `data/clorocontrol.json`, archivo excluido de Git y generado automáticamente al iniciar. La primera ejecución precarga 12 mediciones ficticias (3 granjas × 4 puntos), con IDs estables que coinciden con la semilla local de la app.

## Ejecutar prueba de humo

```bash
npm test
```

La prueba inicia el servidor en un puerto temporal, verifica health, POST/GET de medición, idempotencia, POST/PUT/GET de acción y detiene el proceso.

## Alcance y límites

- Este es un servicio de demostración, no un backend productivo.
- No valida credenciales ni roles en el servidor. Los roles están simulados en la aplicación.
- No utiliza base de datos SQL ni ofrece alta disponibilidad. Persiste un archivo JSON local.
- El binario de fotografías no se sube; la app mantiene copia privada en cada dispositivo y sincroniza solamente metadatos/indicador de evidencia.
- No se integra con AMINO, ERP ni sensores físicos.
