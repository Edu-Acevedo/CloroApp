# Arquitectura offline-first — CloroControl

## Objetivo

La aplicación debe servir en terreno con conexión intermitente. Las pantallas deben consultar los datos locales; guardar una medición no puede depender de Internet. Cuando se recupere la conectividad, los eventos se envían al servicio central y los cambios remotos se incorporan a Room.

## Flujo local

```text
Compose Screen
    ↓ eventos del usuario
ViewModel + StateFlow
    ↓
ChlorineRepository
    ↓ transacción Room
SQLite (measurements, alerts, corrective_actions, sync_queue)
    ↓
La UI observa Flow/StateFlow y se actualiza
```

## Flujo de sincronización

```text
Registro offline
    ↓
Room guarda el dato + evento en sync_queue (misma transacción)
    ↓
SyncScheduler crea trabajo con NetworkType.CONNECTED
    ↓
WorkManager espera conectividad y ejecuta MeasurementSyncWorker
    ↓
POST measurements / POST actions / PUT actions/{clientId}
    ↓ HTTP 2xx
Marcar sincronizado y quitar evento de sync_queue
    ↓
GET measurements + GET actions
    ↓
Fusionar filas remotas en Room
    ↓
Compose muestra lo almacenado localmente
```

WorkManager programa además una revisión periódica de 15 minutos (el sistema controla la ejecución exacta; no es tiempo real ni garantiza segundo exacto). Las reejecuciones utilizan backoff exponencial. La pantalla considera disponible una red con capacidad INTERNET sin exigir el indicador VALIDATED, para permitir pruebas con un backend servido en una LAN aunque esa Wi-Fi no tenga salida a Internet.

## Reglas de integridad

1. Mediciones originales son inmutables: las correcciones se registran como nuevos eventos.
2. La medición, la alerta asociada y su evento de sincronización se escriben dentro de una misma transacción.
3. La acción correctiva, su medición de verificación y los eventos correspondientes se guardan localmente antes de intentar la red.
4. No se elimina ningún elemento de la cola ante errores de red.
5. Se usan `clientId` idempotentes para no duplicar filas en reintentos.
6. Solo un perfil Supervisor puede validar; la alerta se cierra únicamente si existe una acción pendiente y la verificación está en el rango normal 3,00–5,00 ppm.
7. Una medición de verificación fuera de rango también se incorpora al historial y, si corresponde, genera otra alerta.

## Almacenamiento local

| Tabla / componente | Uso |
|---|---|
| `measurements` | Mediciones originales y verificaciones, valor, punto, fecha, estado, responsable, sincronización. |
| `alerts` | Desviaciones, gravedad y estado abierta/cerrada. |
| `corrective_actions` | Descripción, responsable, fecha, valor verificado, evidencia y validación. |
| `sync_queue` | Tipo de evento, identificador de cliente, JSON, intentos y último error. |
| DataStore `clorocontrol_session` | Correo/rol de la sesión ficticia. |
| `filesDir/evidence` | Copias privadas de fotos de cámara/galería. |

## API de demostración

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/health` | Verifica que el servicio está levantado. |
| GET | `/api/measurements` | Descarga mediciones centrales. |
| POST | `/api/measurements` | Recibe una medición; `clientId` evita duplicados. |
| GET | `/api/actions` | Descarga acciones correctivas. |
| POST | `/api/actions` | Recibe una acción correctiva. |
| PUT | `/api/actions/{clientId}` | Actualiza el estado de validación. |

El backend local guarda un JSON en `backend/data/clorocontrol.json`; está pensado para demostración académica en una red privada de desarrollo. No implementa autenticación/autorización del servidor, transporte HTTPS ni despliegue empresarial. No publicar el puerto en Internet ni usar datos reales.

## Evidencia fotográfica: alcance exacto

La cámara y la galería se invocan mediante contratos de Activity Result. La imagen seleccionada se copia a `filesDir/evidence`, por lo que no depende de conservar un permiso temporal de URI. En la sincronización actual solo se comparte el indicador de que existe evidencia y el origen; no se transmite el binario de la foto a otros teléfonos. La evidencia permanece en el dispositivo donde se capturó.

## Configurar la URL

`app/build.gradle.kts` define `API_BASE_URL`. En emulador se utiliza `http://10.0.2.2:8080/api/`. Para dispositivo físico se debe usar la IP LAN del computador que ejecuta Node.js y ambos deben estar en la misma red. El manifiesto de debug permite HTTP únicamente durante pruebas; para una versión distribuida se debe configurar HTTPS.
