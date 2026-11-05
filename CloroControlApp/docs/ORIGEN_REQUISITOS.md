# Trazabilidad con los documentos de DSY1105

Este documento relaciona los archivos entregados al inicio con las decisiones de diseño de CloroControl. Los umbrales, cantidades y reglas del caso se mantienen como datos académicos ficticios y deben confirmarse con el docente.

| Archivo revisado | Contenido relevante | Aplicación en CloroControl |
|---|---|---|
| `Ev. Parcial 2_Estudiante_Encargo_DSY1105.pdf` | Indicadores IL2.1–IL2.4, requisitos, entrega y rúbrica ponderada. | Material 3, formularios con iconos/errores, validación en ViewModel, animaciones, MVVM/Room, colaboración y dos recursos nativos. |
| `Preguntas Sección 002D - Medición de cloro en línea.pdf` | Parámetro, umbrales propuestos, roles, puntos, alertas, acciones, historial, offline y alcance MVP. | 3 granjas × 4 puntos, medición de cloro libre, valores a dos decimales, roles Operario/Supervisor/Jefatura, alertas automáticas, acción/verificación, filtros de historial, Room + cola de sincronización y API demo. |
| `GUÍA 9.docx` | Adaptabilidad en Jetpack Compose, MVVM y layouts pequeño/expandido. | Panel con `BoxWithConstraints` y distribución distinta desde 600dp. Se incorpora recurso drawable para identidad visual. |
| `Guía 10.docx` | Navigation Compose, rutas, NavHost, TopAppBar, NavigationBar, NavigationDrawer, Scaffold y MVVM. | `AppRoutes.kt`, `AppNavigation.kt`, barra superior, navegación inferior y drawer. |
| `Guía 11(1).docx` | Modelado de estado, StateFlow, ViewModel, validación desacoplada, formularios Material 3 y navegación. | `AuthViewModel`, `MeasurementFormViewModel`, `ActionFormViewModel`, estados UI, validadores de dominio y mensajes por campo. El ejemplo de registro/resumen se adapta a los flujos de medición y acción del caso. |
| `Guía 12.docx` | ViewModel, StateFlow, DataStore y animaciones declarativas. | `SessionStore`, estado observable, `animateColorAsState`, `AnimatedVisibility` y `animateContentSize`. |
| `Guía 13.docx` | Contratos Android para cámara y selector de galería, Bitmap/Uri y MVVM. | Cámara y galería integradas en el formulario de acción; imagen copiada a almacenamiento privado local. |
| `Guia 14 - SQLITE.docx` | Room/SQLite, Entity, DAO, Database, Repository, ViewModel y operaciones CRUD. | Entidades, DAO, Room Database, Repository, ViewModels y flujo reactivo local. No se editan ni eliminan mediciones originales: el caso pide conservarlas y registrar correcciones como eventos nuevos. |

## Decisiones de alcance

- Se mantiene el MVP académico con usuarios y datos ficticios; no se conectan sensores reales.
- AMINO/ERP, push real y análisis predictivo no se incluyen porque el caso los presenta como evolución posterior u opcionales.
- El backend JSON de demostración permite mostrar una API REST simulada/desarrollada por el equipo.
- Las fotos quedan guardadas en el teléfono. Esta versión no transmite el archivo binario a otros dispositivos; solo envía metadatos que indican que existe evidencia.
- La autenticación y roles son demostrativos y se validan en el cliente. No es una implementación de seguridad productiva.
