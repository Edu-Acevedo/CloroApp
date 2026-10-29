# Checklist de la rúbrica — Evaluación Parcial 2

La rúbrica entregada asigna 15% a interfaz, 15% a formularios, 10% a validación desacoplada, 10% a animaciones, 15% a estructura/persistencia, 20% a colaboración GitHub/Trello y 15% a recursos nativos. Marcar `Hecho` únicamente después de probarlo y guardar evidencia.

| Indicador | Evidencia necesaria para aspirar al 100% | Estado inicial |
|---|---|---|
| IE 2.1.1 — Interfaz y navegación (15%) | Jerarquía visual consistente, Material 3, navegación fluida entre vistas, sin controles rotos ni contenido cortado | Base creada; falta probar en dispositivos |
| IE 2.1.2 — Formularios (15%) | Formulario de acceso, medición y acción correctiva; cada campo tiene regla, mensaje y señal visual de error | Implementado en código; falta prueba funcional |
| IE 2.2.1 — Validación desacoplada (10%) | Validaciones en ViewModel y reglas del dominio; Compose refleja el estado sin contener la regla de negocio | Implementado en código; falta prueba funcional |
| IE 2.2.2 — Animaciones (10%) | Retroalimentación útil de estados y aparición de alerta; coherentes con el diseño y sin errores | Implementado en código; falta prueba visual |
| IE 2.3.1 — Modularidad y persistencia (15%) | Separación UI/ViewModel/dominio/repositorio/Room; datos permanecen tras cerrar y volver a abrir | Implementado en código; falta prueba de reinicio |
| IE 2.3.2 — GitHub y Trello (20%) | Repositorio con acceso para docente, commits reales y distribuidos, tablero con responsables y tareas visibles | Pendiente de crear en cuentas reales |
| IE 2.4.1 — Recursos nativos (15%) | Cámara y galería funcionan, se integran en el registro de acción y se prueban en emulador/dispositivo | Implementado en código; falta prueba con recursos reales |

## Pruebas mínimas de negocio

- `3,00` y `5,00` ppm → Normal.
- `2,00`, `2,99`, `5,01` y `6,00` ppm → Advertencia.
- `1,99` y `6,01` ppm → Crítico.
- Valor vacío, texto, negativo y sobre 10,00 → formulario muestra error y no guarda.
- Medición no normal → crea alerta automáticamente.
- Operario registra acción, pero no cierra alerta.
- Supervisor solo valida/cierra con verificación normal entre 3,00 y 5,00 ppm.
- Jefatura consulta sin controles para crear medición o validar alertas.
- Datos siguen disponibles después de reiniciar la aplicación.
- Cámara y galería pueden seleccionarse por separado y muestran la evidencia.

## Condiciones de entrega extraídas de la pauta

- Repositorio público con fuente, `README.md`, commits técnicos y acceso al docente/equipo.
- Planificación compartida en Trello o herramienta equivalente.
- Subir el código al enlace grupal de AVA según las instrucciones del docente.
- Activar el enlace individual de AVA con nombre, equipo y nombre de aplicación/contexto.
- Confirmar fechas y condiciones exactas con la pauta entregada en clases. La documentación cargada advierte de consecuencias por entregas tardías o cambios posteriores a la fecha fijada.
