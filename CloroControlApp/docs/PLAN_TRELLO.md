# Plan de Trello — CloroControl

Crear un tablero real llamado **CloroControl | Evaluación Parcial 2** con estas listas: `Backlog`, `Por hacer`, `En progreso`, `En revisión`, `Hecho`.

## Tarjetas sugeridas para el trabajo en pareja

| Tarjeta | Responsable sugerido | Criterio de terminado |
|---|---|---|
| Configurar Android Studio y sincronizar Gradle | Integrante A | App compila y abre en emulador; dependencias documentadas |
| Definir reglas de cloro y datos de prueba | Integrante A | Se prueban límites de 2, 3, 5 y 6 ppm y valores fuera de ellos |
| Crear entidades Room, DAO y repositorio | Integrante A | Mediciones, alertas y acciones se conservan al reiniciar |
| Implementar sesión y perfiles con DataStore | Integrante B | Tres cuentas ficticias; permisos básicos visibles por rol |
| Diseñar panel Material 3 y navegación | Integrante B | Inicio, mediciones, alertas e historial con navegación funcional |
| Crear formulario de medición validado | A + B, revisión cruzada | Todos los campos muestran errores útiles; validación en ViewModel |
| Crear acción correctiva y verificación de supervisor | Integrante A | Se conserva responsable, fecha, valor verificado y estado |
| Integrar cámara y selector de galería | Integrante B | Cada botón abre el recurso nativo y muestra la evidencia seleccionada |
| Animaciones y adaptación de pantallas | Integrante B | Estados y alertas tienen retroalimentación visual, sin cortar contenido |
| Pruebas de roles, navegación y persistencia | A + B | Checklist de pruebas completado en un dispositivo/emulador |
| README, capturas y preparación de defensa | A + B | README actualizado; ambos integrantes explican los módulos que desarrollaron |
| Revisión final contra la pauta | A + B | Todas las filas de `RUBRICA_CHECKLIST.md` tienen evidencia real |

## Ritmo de cinco semanas

- **Semana 1:** Alcance, requisitos, sincronización, diseño Material 3 y primer commit de cada integrante.
- **Semana 2:** Modelos, reglas de negocio, SQLite/Room y generación de muestras.
- **Semana 3:** Formularios con validaciones desacopladas, navegación y roles.
- **Semana 4:** Cámara/galería, animaciones, historial, persistencia y pruebas.
- **Semana 5:** Correcciones, prueba de defensa, README, capturas y verificación de la entrega.

## Rutina de coordinación

- Cada tarjeta tiene responsable, fecha objetivo y descripción de aceptación.
- Usar ramas breves por funcionalidad, por ejemplo `feature/formulario-medicion` y `feature/evidencia-camara`.
- Hacer commits pequeños con mensajes técnicos; integrar y probar antes de cerrar una tarjeta.
- Anotar errores encontrados y su resolución en la tarjeta correspondiente.
- Mantener participación comprobable de ambos integrantes. Este documento es una propuesta de planificación; **no crea el tablero de Trello automáticamente**.
