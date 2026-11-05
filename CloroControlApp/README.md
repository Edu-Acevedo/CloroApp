# CloroControl — Proyecto Android DSY1105

**Asignatura:** DSY1105 — Desarrollo de Aplicaciones Móviles  
**Evaluación:** Parcial 2 — interfaz funcional  
**Caso:** Medición de cloro en línea  
**Stack:** Kotlin · Jetpack Compose · Material 3 · MVVM · Room/SQLite · DataStore · WorkManager  
**API de demostración:** Node.js 18+ (sin dependencias externas)

> Proyecto académico basado en los documentos entregados para DSY1105. Las granjas, personas, mediciones y credenciales son ficticias. Los umbrales de cloro son los rangos propuestos para el MVP y deben confirmarse con el docente. Esta aplicación no se conecta a sensores reales ni constituye una herramienta operativa de seguridad sanitaria.

## 1. Estado de esta entrega

Esta versión amplía la base `CloroControlAndroidBase` entregada inicialmente e incorpora los cambios acordados durante la conversación: corrección de dependencias para AGP 8.13.2/compileSdk 36, persistencia local offline-first, cola transaccional de sincronización, WorkManager, API REST local, descarga de cambios remotos, historial y reglas de validación para acciones correctivas.

**No se afirma que el APK esté compilado ni probado en un teléfono.** El entorno en el que se preparó este ZIP no dispone de Android SDK ni de Gradle instalado, por lo que la compilación Android final debe comprobarse en Android Studio. La API Node.js sí se puede probar de forma independiente con `npm test`.

## 2. Funcionalidades implementadas en el código

- Inicio de sesión de demostración y perfiles Operario, Supervisor y Jefatura. DataStore conserva la sesión local.
- Panel Material 3 con recuento de mediciones y alertas, promedio, mínimos/máximos, comparación por granja, tendencia y estado de conexión. El panel adapta su distribución a anchos compactos y expandidos.
- Navegación con `NavHost`, `Scaffold`, `TopAppBar`, `NavigationBar` y `ModalNavigationDrawer`.
- Formulario de medición con selección de granja, galpón, punto, ubicación y ppm. Los errores se generan desde el ViewModel y se presentan por campo.
- Clasificación automática del cloro y creación local de alertas para advertencia/crítico. Distingue cloro insuficiente y excesivo.
- Formulario de acción correctiva con descripción, medición de verificación y evidencia opcional de cámara o galería.
- Fotografías copiadas al almacenamiento privado de la aplicación para que sigan disponibles sin conexión.
- Validación/cierre de alertas exclusivamente por Supervisor, después de registrar una acción correctiva y una verificación normal entre 3,00 y 5,00 ppm.
- Historial local filtrable por 24 horas, 7 días y 30 días.
- Room sobre SQLite para mediciones, alertas, acciones y cola de sincronización. Migraciones 1→2→3 incluidas.
- Offline-first: las pantallas leen desde Room; nuevos registros y eventos se escriben localmente antes de programar un envío.
- WorkManager programa sincronización cuando hay red y también una revisión periódica (mínimo 15 minutos). Se envían mediciones, acciones y validaciones; se descargan mediciones/acciones remotas.
- API REST Node.js de demostración con almacenamiento JSON e identificadores idempotentes para evitar duplicados al reintentar.
- Pruebas unitarias iniciales para reglas de cloro, validadores y roles; prueba de humo de API incluida.

## 3. Reglas propuestas del MVP

Los valores siguientes provienen del documento del caso entregado para el proyecto. Son una propuesta académica; no se deben interpretar como umbrales oficiales confirmados.

| Cloro libre | Estado |
|---|---|
| Menor que 2,00 ppm | Crítico — cloro insuficiente |
| Desde 2,00 y menor que 3,00 ppm | Advertencia — cloro insuficiente |
| Desde 3,00 hasta 5,00 ppm, inclusive | Normal |
| Mayor que 5,00 y hasta 6,00 ppm, inclusive | Advertencia — cloro excesivo |
| Mayor que 6,00 ppm | Crítico — cloro excesivo |

La unidad es ppm (mg/L se considera aproximadamente equivalente para agua diluida en el alcance del caso). La UI muestra y valida hasta dos decimales. La validación de ingreso rechaza valores negativos y no fija un máximo superior no indicado en el caso; así cualquier valor por encima de 6,00 ppm puede registrarse como crítico.

## 4. Cuentas de demostración

Todas usan la contraseña `Demo123`.

| Perfil | Cuentas disponibles | Permisos de demostración |
|---|---|---|
| Operario (5) | `operario@demo.cl`, `operario02@demo.cl`, `operario03@demo.cl`, `operario04@demo.cl`, `operario05@demo.cl` | Consulta las granjas asignadas; registra mediciones y acciones. |
| Supervisor (5) | `supervisor@demo.cl`, `supervisor02@demo.cl`, `supervisor03@demo.cl`, `supervisor04@demo.cl`, `supervisor05@demo.cl` | Consulta las granjas asignadas; registra acciones; valida y cierra alertas. |
| Jefatura (5) | `jefatura@demo.cl`, `jefatura02@demo.cl`, `jefatura03@demo.cl`, `jefatura04@demo.cl`, `jefatura05@demo.cl` | Vista general, indicadores e historial; solo consulta. |

En total se incluyen 15 usuarios ficticios: cinco por cada rol, distribuidos entre las tres granjas de muestra.

La autenticación es simulada y los permisos de la app se aplican en el cliente. No usar información, RUT ni contraseñas reales. La API de demostración no implementa autenticación de usuarios: no publicarla ni exponerla fuera de una red de desarrollo.

## 5. Requisitos para abrir el proyecto

- Android Studio con soporte para proyectos Compose/Kotlin.
- Gradle 8.13 y Android Gradle Plugin 8.13.2.
- Kotlin 2.3.10 + plugin Compose y KSP 2.3.10.
- Android SDK Platform 36 (compileSdk 36).
- JDK 17 o posterior compatible con Gradle 8.13; el bytecode de la app se genera para Java 17.
- Emulador o dispositivo Android con API 26 o superior.

Las versiones de Navigation Compose 2.9.8 y Lifecycle 2.9.4 reemplazan las versiones 2.10.2/2.11.0 de la primera base, que provocaron el error de AAR Metadata porque exigían AGP 9.1 y API 37. AGP 8.13.2 admite hasta API 36.

### Abrir y ejecutar

1. Descomprime el ZIP y abre la carpeta raíz `CloroControlAndroidBase` desde Android Studio (**Open**).
2. Comprueba `Settings → Build, Execution, Deployment → Build Tools → Gradle` y selecciona un Gradle JDK compatible.
3. En **Tools → SDK Manager**, instala Android SDK Platform 36 y las Build Tools solicitadas por AGP.
4. Ejecuta **File → Sync Project with Gradle Files**.
5. Ejecuta **Build → Make Project**.
6. Inicia un emulador o conecta un dispositivo y pulsa **Run**.

### Nota del Gradle Wrapper

El paquete incluye `gradle/wrapper/gradle-wrapper.properties`, pero el ZIP recibido de la base inicial no contenía `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`. No se generó un wrapper falso. Si Android Studio exige los lanzadores del wrapper y tienes Gradle 8.13 instalado, desde PowerShell en la carpeta raíz ejecuta `gradle wrapper --gradle-version 8.13 --distribution-type bin`; después comprueba `.\gradlew.bat --version`. Si no tienes el comando `gradle`, instala Gradle 8.13 o regenera el wrapper desde la configuración de Gradle de Android Studio. Los pasos están detallados en `docs/COMENZAR_AQUI.md`.

## 6. Estructura del código

```text
CloroControlAndroidBase/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/cl/duoc/clorocontrol/
│       │   │   ├── CloroControlApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── evidence/       # Copia privada de fotos
│       │   │   │   ├── local/          # Room, entidades, DAO, migraciones
│       │   │   │   ├── network/        # Estado de conectividad
│       │   │   │   ├── preferences/    # DataStore
│       │   │   │   ├── repository/     # Reglas de persistencia y datos demo
│       │   │   │   └── sync/           # API HTTP, WorkManager, cola
│       │   │   ├── domain/             # Reglas de cloro y validadores
│       │   │   ├── model/              # Sesión y roles
│       │   │   └── ui/
│       │   │       ├── navigation/
│       │   │       ├── screen/
│       │   │       ├── theme/
│       │   │       └── viewmodel/
│       │   └── res/
│       └── test/                       # Pruebas unitarias
├── backend/                            # API REST local Node.js
└── docs/                               # Arquitectura, pruebas y rúbrica
```

## 7. Ejecutar la API REST local

Requiere Node.js 18 o posterior. No necesita `npm install` ni bibliotecas externas.

```bash
cd backend
npm start
```

Comprobar desde el navegador del computador: `http://localhost:8080/api/health`.

El emulador Android accede al computador anfitrión mediante `http://10.0.2.2:8080/api/`, dirección ya configurada en `app/build.gradle.kts`. En un teléfono físico, cambia `API_BASE_URL` por la IP LAN del computador, ambos deben estar en la misma red y usa únicamente para pruebas. El manifiesto debug permite HTTP local sin cifrar; la configuración de release no habilita tráfico HTTP cleartext.

Los datos del backend se guardan en `backend/data/clorocontrol.json`, que se crea al iniciar con 12 registros ficticios y está excluido de Git. No se debe incluir datos personales ni mediciones reales.

Para probar la API: `cd backend && npm test`.

## 8. Qué se sincroniza y qué permanece local

- Se sincronizan los eventos de medición, acciones correctivas y validaciones/cierres.
- Al volver la conectividad, WorkManager envía la cola local en orden; una respuesta satisfactoria elimina el evento de la cola y marca el registro como sincronizado. Si falla la red, conserva el evento para reintentar.
- Se descargan mediciones y acciones del servidor y se incorporan a Room; la UI sigue leyendo Room, no la red.
- Las fotos se copian al directorio privado del teléfono y siguen visibles localmente. En esta versión la API sincroniza el indicador de que hay evidencia y su origen, **pero no transmite el archivo binario de la fotografía a otros teléfonos**.
- La API es de desarrollo, local, sin autenticación ni cifrado de transporte. No es una integración productiva con AMINO, ERP ni sensores.

Más detalle en `docs/ARQUITECTURA_OFFLINE_FIRST.md` y `backend/README.md`.

## 9. Pruebas

### Pruebas Android

Desde Android Studio, cuando el proyecto esté sincronizado: **Run → Run 'app'** y **app → Tasks → verification → testDebugUnitTest**. También se puede usar `gradlew testDebugUnitTest` si el Gradle Wrapper ha sido regenerado.

Las pruebas unitarias incluidas cubren límites de estado de cloro, validadores de campos, formato de correo y permisos por rol. Todavía se debe comprobar en dispositivo: navegación, recomposición, Room tras reinicio, conectividad, cámara y galería.

### Prueba offline manual

1. Iniciar sesión y esperar los datos iniciales.
2. Desactivar Wi-Fi/datos o activar modo avión.
3. Registrar una medición (por ejemplo `2,80 ppm`).
4. Confirmar que aparece en Mediciones/Historial, genera alerta y queda pendiente.
5. Cerrar completamente y volver a abrir la app. Confirmar que el registro persiste.
6. Volver a conectar Internet y tener el backend ejecutándose.
7. Pulsar sincronizar/actualizar o esperar a WorkManager. Confirmar que desaparece el pendiente y que la medición aparece en `GET /api/measurements`.
8. Apagar el backend antes de otra sincronización y comprobar que los eventos siguen pendientes.

Consulta `docs/PRUEBAS_MANUALES.md` para todos los escenarios.

## 10. GitHub, Trello y entrega

La app y la documentación están preparadas para subir a un repositorio, pero este ZIP **no crea por sí mismo** un repositorio GitHub ni un tablero Trello. La pauta otorga 20% a evidencia de colaboración: ambos integrantes deben registrar trabajo auténtico con commits técnicos distribuidos, tareas asignadas, revisión y participación. No inventar commits ni evidencias.

Antes de la entrega: completar nombres/equipo, confirmar rangos y requisitos con el docente, ejecutar pruebas reales, subir el código fuente al espacio grupal AVA, activar el enlace individual AVA y respetar las condiciones de fecha/cierre de la pauta.

## 11. Documentos de apoyo

- `docs/COMENZAR_AQUI.md`: primer inicio y resolución de configuración.
- `docs/ARQUITECTURA_OFFLINE_FIRST.md`: almacenamiento, cola y sincronización.
- `docs/PRUEBAS_MANUALES.md`: casos de prueba y evidencias.
- `docs/RUBRICA_CHECKLIST.md`: matriz de los siete criterios de la rúbrica.
- `docs/PLAN_TRELLO.md`: propuesta de tablero y distribución de trabajo.
- `backend/README.md`: API, endpoints y pruebas.
