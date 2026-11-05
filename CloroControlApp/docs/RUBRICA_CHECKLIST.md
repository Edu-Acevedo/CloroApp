# Matriz de cumplimiento — Rúbrica Evaluación Parcial 2 DSY1105

La siguiente matriz reproduce las siete dimensiones y sus ponderaciones del archivo `Ev. Parcial 2_Estudiante_Encargo_DSY1105.pdf`. El 100% requiere evidencias observables y pruebas reales; la presencia de código por sí sola no demuestra que una función está operativa.

| Indicador de la pauta | Peso | Implementación en este proyecto | Evidencia para marcar 100% | Estado honesto |
|---|---:|---|---|---|
| IE 2.1.1 Interfaz coherente, jerarquía, distribución y navegación funcional | 15% | Material 3, panel, navegación inferior/lateral, rutas Compose, distribución adaptable en panel. | Recorrido completo sin rutas rotas, captura en teléfono y pantalla expandida, revisión de legibilidad y retorno. | Código incorporado; falta validación visual en dispositivo. |
| IE 2.1.2 Formularios completos con validaciones visuales por campo, mensajes e iconos | 15% | Login, medición y acción correctiva; estado de errores separado para cada campo; iconos y supportingText. | Probar campos vacíos, mal formateados y fuera de rango; capturar cada formulario con errores y guardado válido. | Código incorporado; falta prueba interactiva. |
| IE 2.2.1 Validación centralizada y desacoplada de UI | 10% | `FormValidators`, `ChlorineRules` y validaciones en ViewModel. | Unit tests pasan; la UI solo observa estado y dispara eventos; comprobar límites del caso. | Pruebas de dominio escritas; ejecución Android pendiente. |
| IE 2.2.2 Animaciones funcionales con fluidez/feedback | 10% | `animateColorAsState`, `AnimatedVisibility`, `animateContentSize`, retroalimentación de estados. | Video mostrando transición de estado y alerta; verificar fluidez y que no distraiga. | Código incorporado; falta prueba visual. |
| IE 2.3.1 Modularidad, responsabilidades separadas y almacenamiento local | 15% | UI/ViewModel/domain/repository/data/local; Room/SQLite, DataStore, persistencia y cola offline-first; WorkManager. | Guardar sin red, cerrar/reiniciar app y comprobar Room; recuperar red y verificar API + cola vaciada; migraciones verificadas. | Arquitectura y flujo implementados; falta compilación Android y prueba end-to-end en dispositivo. |
| IE 2.3.2 GitHub/Trello y participación colaborativa comprobable | 20% | README, plan Trello, lista de tareas, convención de ramas/commits propuestos. | Repo público con acceso docente/equipo, commits reales de ambos, tarjetas asignadas y actualizadas, issues/revisión. | Pendiente: requiere acciones reales de la pareja en sus cuentas. No se pueden fabricar evidencias. |
| IE 2.4.1 Acceso seguro/funcional a dos o más recursos nativos | 15% | Contrato de cámara y selector de galería; evidencia copiada a filesDir privado y asociada a acción correctiva. | Probar los dos botones en un dispositivo/emulador, mostrar imagen y verificar persistencia del archivo local. | Código incorporado; falta prueba con cámara/galería real. |

## Criterios funcionales adicionales extraídos del caso

- Tres granjas y cuatro puntos por granja son datos ficticios propuestos para el MVP.
- Inicio de sesión con correo y contraseña ficticios; roles Operario, Supervisor y Jefatura.
- Las mediciones se registran a dos decimales y clasifican Normal/Advertencia/Crítico mediante umbrales propuestos.
- Una desviación genera alerta automática y distingue cloro insuficiente de excesivo.
- La acción correctiva registra descripción, responsable, fecha, punto y medición de verificación.
- Una alerta solo puede cerrarse con validación de Supervisor luego de una acción y nueva verificación normal.
- Historial por 24 horas, 7 días y 30 días; tablero con promedio, mínimo, máximo, tendencia y comparación por granja.
- Sin conexión se consulta Room, se registra localmente y se mantiene una cola pendiente para sincronización posterior.
- AMINO, ERP, sensores reales, notificaciones push y análisis predictivo se dejan fuera del MVP porque el documento los presenta como evolución futura/no obligatoria.

## Requisitos de entrega que no dependen del código

1. Completar nombres y número de equipo en la documentación solicitada por el docente.
2. Crear y mantener tablero Trello real.
3. Crear repositorio público y dar acceso al docente y al equipo.
4. Mantener commits técnicos y participación real de ambos integrantes.
5. Subir el código fuente en AVA grupal y activar el enlace AVA individual.
6. Respetar las condiciones de fecha límite y cambios posteriores descritas en la pauta.
7. Confirmar con el docente los rangos y cantidades simuladas.

**No marcar una fila como lograda al 100% hasta haber realizado y guardado la evidencia indicada.**
