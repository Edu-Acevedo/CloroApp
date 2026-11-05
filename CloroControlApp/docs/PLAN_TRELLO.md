# Plan de trabajo en pareja — CloroControl

Este archivo es una propuesta para crear un tablero real; no modifica ninguna cuenta de Trello ni genera actividad automáticamente. La pauta pondera GitHub/Trello en 20%, por lo que ambos integrantes deben trabajar y dejar evidencia auténtica.

## Configuración del tablero

**Nombre:** `CloroControl | DSY1105 | Evaluación Parcial 2`

**Listas:** `Backlog` → `Por hacer` → `En progreso` → `En revisión` → `Hecho`.

Cada tarjeta debe tener descripción, responsable, fecha, checklist de aceptación, enlace al PR/commit y evidencia/captura.

## Tarjetas y distribución sugerida

| ID | Tarjeta | Responsable sugerido | Criterio de terminado |
|---|---|---|---|
| T01 | Abrir proyecto, JDK/SDK y sincronizar Gradle | A | Gradle sync y Make Project correctos; versión documentada. |
| T02 | Revisar navegación y diseño Material 3 | B | Inicio, mediciones, alertas, historial y formularios accesibles. |
| T03 | Verificar reglas de cloro y pruebas unitarias | A | Límites 2, 3, 5 y 6 ppm y valores críticos cubiertos. |
| T04 | Revisar formularios y errores por campo | B | Login, medición y acción muestran errores con icono/mensaje. |
| T05 | Validar roles y permisos | A | Operario, Supervisor y Jefatura tienen el flujo de permisos demostrado. |
| T06 | Room: mediciones, alertas, acciones y migraciones | A | Datos permanecen después de cerrar/reabrir; transacciones verificadas. |
| T07 | Probar modo sin conexión | A + B | Registro local offline, persistencia y contador pendiente. |
| T08 | Arrancar y probar API REST Node | A | GET health, POST medición, POST/PUT acción y GET listados correctos. |
| T09 | Probar WorkManager y sincronización bidireccional | A | Cola enviada/reintentada y datos remotos importados en Room. |
| T10 | Cámara y galería con evidencia privada | B | Ambos recursos nativos abren, muestran y conservan una copia local. |
| T11 | Animaciones y adaptabilidad | B | Estado/alertas animados; panel usable en pantalla compacta y expandida. |
| T12 | Historial y dashboard de métricas | B | Filtros 24h/7d/30d, promedio, mínimo, máximo y comparativa. |
| T13 | Prueba integral por ambos integrantes | A + B | Ejecutar `docs/PRUEBAS_MANUALES.md`, registrar resultados y defectos. |
| T14 | README, checklist y preparación de defensa | A + B | Ambos pueden explicar módulos, flujo offline y decisiones del caso. |
| T15 | Revisión final de AVA y fecha de congelamiento | A + B | Repo/AVA/activación individual comprobados antes del cierre. |

A y B son marcadores: reemplazarlos por los nombres reales de la pareja y ajustar las tareas según la contribución efectiva. No registrar tareas como Hechas hasta que la prueba esté ejecutada.

## Plan de cinco semanas

- **Semana 1 — Entorno y diseño:** T01, T02, crear repositorio/tablero, un commit auténtico por integrante.
- **Semana 2 — Datos y reglas:** T03, T05, T06, verificar transacciones y semilla ficticia.
- **Semana 3 — Formularios y navegación:** T04, T12, probar rutas, mensajes e historial.
- **Semana 4 — Funciones nativas y offline:** T07, T08, T09, T10, T11.
- **Semana 5 — QA y entrega:** T13, T14, T15; revisar todos los indicadores y preparar defensa.

## Convención de Git

Ramas sugeridas: `feature/formulario-medicion`, `feature/offline-sync`, `feature/evidencia-camara`, `fix/gradle-compat`.

Mensajes de ejemplo (solo usar cuando el cambio realmente se realizó):

- `fix: alinear Navigation y Lifecycle con AGP 8.13.2`
- `feat: persistir mediciones y alertas en Room`
- `feat: agregar cola offline y worker de sincronizacion`
- `feat: adjuntar evidencia desde camara y galeria`
- `test: cubrir rangos limite de cloro libre`
- `docs: documentar arquitectura offline-first y pruebas`

No se deben crear commits ficticios ni reescribir la historia para simular participación. La pauta indica una restricción estricta sobre cambios después de la fecha de entrega y antes de la evaluación: verificar su aplicación con el docente y congelar el repositorio a tiempo.
