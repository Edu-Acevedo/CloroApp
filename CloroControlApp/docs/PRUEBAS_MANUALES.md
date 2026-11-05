# Plan de pruebas manuales — CloroControl

Marcar un caso como aprobado solo después de ejecutarlo en emulador o dispositivo y guardar una captura/video como evidencia.

## A. Inicio, roles y navegación

| ID | Caso | Resultado esperado |
|---|---|---|
| A01 | Ingresar correo vacío | Muestra error en correo y no inicia sesión. |
| A02 | Ingresar correo inválido | Mensaje específico de formato. |
| A03 | Ingresar cuenta no registrada o contraseña incorrecta | No crea sesión; muestra aviso de credenciales demo. |
| A04 | Ingresar Operario | Accede al panel y solo visualiza Granja Norte. |
| A05 | Ingresar Supervisor | Puede ver las tres granjas y gestionar alertas. |
| A06 | Ingresar Jefatura | Puede consultar, sin botones para registrar ni validar. |
| A06b | Ingresar con `operario03@demo.cl` y `supervisor02@demo.cl` | Se aplican las granjas ficticias asignadas a cada cuenta. |
| A07 | Navegar por barra inferior y menú lateral | Abre Panel, Mediciones, Alertas e Historial; la ruta activa queda seleccionada. |
| A08 | Cerrar sesión y reabrir | Vuelve al Login y la sesión queda cerrada. |

## B. Formularios y reglas de cloro

| ID | Entrada / acción | Resultado esperado |
|---|---|---|
| B01 | Dejar campos de medición vacíos | Error visual por cada campo obligatorio. |
| B02 | Seleccionar punto que no corresponde a la granja | La validación del ViewModel lo rechaza. |
| B03 | Ingresar `3,50` o `3.50` | Acepta decimal coma/punto; permite hasta dos decimales y muestra uniformemente dos. |
| B03b | Ingresar `3,555` | Rechaza más de dos decimales con mensaje específico. |
| B04 | Ingresar texto `abc` o un valor negativo | Rechaza y muestra mensaje por campo. |
| B04b | Registrar un valor mayor a 10,00 ppm (por ejemplo `12,50`) | Acepta el número y lo clasifica crítico, ya que el caso no define un máximo superior. |
| B05 | Registrar `3,00` y `5,00` | Estado Normal; no genera alerta. |
| B06 | Registrar `2,00`, `2,99`, `5,01`, `6,00` | Estado Advertencia; genera alerta. |
| B07 | Registrar `1,99` o `6,01` | Estado Crítico; genera alerta. |
| B08 | Comparar `2,80` y `5,80` | La app distingue cloro insuficiente de excesivo. |

## C. Acciones correctivas y roles

| ID | Acción | Resultado esperado |
|---|---|---|
| C01 | Guardar descripción vacía/corta | Mensaje de validación; no guarda. |
| C02 | Registrar acción con verificación `2,80` | Se agrega al historial, queda pendiente; alerta sigue abierta. |
| C03 | Supervisor intenta validar con `2,80` | No puede cerrar; informa que la verificación debe estar en rango normal. |
| C04 | Registrar acción con verificación `3,50` | Acción queda pendiente de validación. |
| C05 | Supervisor valida la acción pendiente | Acción cambia a VALIDADA y alerta a CERRADA. |
| C06 | Operario intenta cerrar alerta | No dispone del control de validación y repositorio rechaza otro rol. |
| C07 | Jefatura intenta registrar acción/medición | No puede modificar registros. |
| C08 | Realizar segunda acción sobre una alerta | Se conserva el registro anterior; nueva acción aparece como evento nuevo. |

## D. Persistencia, offline y sincronización

| ID | Acción | Resultado esperado |
|---|---|---|
| D01 | Consultar panel sin Internet | Muestra datos precargados de Room. |
| D02 | Activar modo avión y guardar `2,80` | Guarda la medición y su alerta en SQLite; la cola tiene eventos pendientes. |
| D03 | Cerrar y abrir la aplicación sin red | El registro y alerta siguen presentes. |
| D04 | Apagar el backend, reconectar el teléfono | El envío falla, pero el registro local y la cola permanecen. |
| D05 | Arrancar backend y solicitar sincronización | Eventos reciben HTTP 2xx; se vacía la cola correspondiente y se marca sincronizado. |
| D06 | Repetir mismo envío con `clientId` | El servidor responde el registro existente y no duplica. |
| D07 | Registrar dato desde segundo dispositivo/emulador y sincronizar | Tras descarga, aparece en Room en el otro cliente. |
| D08 | Validar acción en otro dispositivo | Al descargar el cambio, se refleja la acción validada y alerta cerrada localmente. |

## E. Cámara y galería

| ID | Acción | Resultado esperado |
|---|---|---|
| E01 | Abrir Cámara desde acción correctiva | Abre la app/contrato de cámara; la imagen se muestra en pantalla. |
| E02 | Seleccionar imagen desde galería | Abre selector; la imagen se muestra. |
| E03 | Cerrar y volver al formulario | La copia local de evidencia sigue disponible mientras el formulario conserva el estado. |
| E04 | Guardar acción con fotografía | `evidenceUri` apunta a un archivo dentro del almacenamiento privado de la app. |

## F. Historial, diseño y animaciones

| ID | Acción | Resultado esperado |
|---|---|---|
| F01 | Cambiar filtros 24 horas/7 días/30 días | Se muestran solo los registros del período y granjas permitidas. |
| F02 | Abrir panel en pantalla compacta y tableta | El diseño reordena contenido y mantiene controles legibles. |
| F03 | Cambiar estado de medición/alerta | La etiqueta de estado cambia visualmente con transición. |
| F04 | Mostrar/ocultar advertencia de alertas | Aparece/desaparece con animación. |
| F05 | Cambiar valores de estadísticas | Contenido se adapta sin superponer tarjetas. |

## Evidencias para el informe y defensa

- Login de cada rol y menús disponibles.
- Formulario mostrando error por campo.
- Medición Normal y Crítica generando los estados esperados.
- Acción correctiva y verificación pendiente.
- Validación de Supervisor que cierra la alerta.
- Historial filtrado.
- Cámara y galería mostrando evidencia.
- Modo avión con medición local persistente tras reinicio.
- Backend respondiendo `/api/health`, registro recibido y cola sincronizada.
- GitHub: historial de commits auténticos de ambos integrantes.
- Trello: tareas asignadas, movimiento y revisión de tarjetas.
