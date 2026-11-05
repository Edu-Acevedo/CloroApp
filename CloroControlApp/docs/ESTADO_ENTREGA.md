# Estado de preparación de esta versión

Fecha de preparación: 2 de noviembre de 2026.

## Comprobaciones realizadas en este entorno

- [x] Se inspeccionó el proyecto ZIP original y se continuó sobre `CloroControlAndroidBase`.
- [x] Se leyeron los ocho documentos iniciales de DSY1105/caso.
- [x] Se actualizaron Navigation Compose/Lifecycle para evitar los requisitos AGP 9.1/API 37 que originaban el error reportado.
- [x] Las clases Kotlin puras de reglas, validadores y sesión compilaron con el compilador Kotlin instalado.
- [x] No se detectaron errores de sintaxis Kotlin evidentes en el análisis estático; un build completo no es posible sin AndroidX/SDK/Gradle.
- [x] La migración SQLite desde el esquema original v1 hasta las columnas/índices de v3 fue probada con SQLite local.
- [x] `node --check backend/server.js` pasó.
- [x] `npm test` del backend pasó: GET health, 12 muestras, POST, reenvío idempotente, GET y ciclo POST/PUT/GET de acción.
- [x] Se comprobó que los archivos de prueba temporales no se incluyan en el ZIP.

## Comprobaciones que todavía requieren Android Studio/dispositivo

- [ ] Gradle Sync con la instalación local del usuario.
- [ ] Build APK/debug y ejecución en emulador o teléfono.
- [ ] Ejecución de las pruebas JUnit desde Gradle.
- [ ] Pruebas de cámara y galería en dispositivo/emulador.
- [ ] Prueba offline real con modo avión y reinicio de la app.
- [ ] Prueba integrada entre la app Android y el backend Node desde emulador/teléfono.
- [ ] Prueba en dos dispositivos para validar la descarga de cambios remotos.
- [ ] Capturas/evidencias de cada indicador de rúbrica.
- [ ] Crear repositorio GitHub, Trello real y demostrar commits/tareas de ambos integrantes.

## Limitaciones conocidas

1. El ZIP original no incluía los lanzadores `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`; esta versión conserva `gradle-wrapper.properties` de Gradle 8.13 y documenta cómo regenerar el wrapper oficial en el equipo. No se añadió un wrapper ficticio.
2. El entorno de preparación no tenía Android SDK ni Gradle instalado, por lo que no se puede declarar un APK compilado.
3. Las cuentas/roles de login son demostrativas y la autorización se simula en el cliente. El backend local no autentica usuarios ni se debe usar con información real.
4. La API de demostración usa JSON local en vez de un servicio productivo con base de datos administrada.
5. Los datos medidos no vienen de sensores; los valores son ficticios y manuales.
6. Las evidencias fotográficas quedan guardadas localmente en el dispositivo, pero esta versión no transmite el archivo binario de las fotos al backend ni a otros teléfonos; sincroniza los metadatos/indicador de evidencia.
7. Los rangos de cloro son propuestas del caso para el MVP, no una norma certificada; confirmarlos con el docente.
8. El código y los documentos ayudan a cubrir la rúbrica, pero no se puede garantizar la calificación máxima sin ejecución real, evidencia, colaboración auténtica y aprobación docente.
