# Comenzar aquí — CloroControl

Este proyecto continúa la base Android Studio CloroControl compartida previamente. No crees otro proyecto ni copies los archivos a una app diferente: abre esta carpeta raíz.

## Paso 1 — Abrir y preparar Android Studio

1. Extrae el ZIP completo.
2. En Android Studio usa **Open** y selecciona la carpeta `CloroControlAndroidBase` (la que contiene `settings.gradle.kts`).
3. Revisa Gradle JDK compatible (JDK 17 o posterior).
4. En SDK Manager instala Android SDK Platform 36.
5. Usa **File → Sync Project with Gradle Files**.
6. Ejecuta **Build → Make Project**.
7. Cuando compile, inicia un emulador/API 26+ o conecta un teléfono Android y pulsa **Run**.

## Paso 2 — Error AAR Metadata que se corrigió

La base anterior usaba Navigation Compose 2.10.2 y Lifecycle 2.11.0. Estas dependencias exigían AGP 9.1 y API 37, incompatibles con AGP 8.13.2 y compileSdk 36. En esta versión se reemplazaron por:

- `androidx.navigation:navigation-compose:2.9.8`
- `androidx.lifecycle:lifecycle-runtime-compose:2.9.4`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4`

Mantén `compileSdk = 36`, AGP 8.13.2 y Gradle 8.13. No actualices a API 37 sin actualizar toda la cadena de herramientas.

## Paso 3 — Si dice que falta Gradle Wrapper

El material inicial no tenía `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`; el ZIP contiene `gradle-wrapper.properties`. No borres ese archivo ni agregues un JAR de origen dudoso. Si tienes Gradle 8.13 instalado y el comando `gradle` funciona en la terminal de Windows, abre la terminal en la carpeta raíz (donde está `settings.gradle.kts`) y ejecuta:

```powershell
gradle --version
gradle wrapper --gradle-version 8.13 --distribution-type bin
.\gradlew.bat --version
```

Esto genera los lanzadores y el JAR oficiales del wrapper. Si `gradle --version` no funciona, primero instala Gradle 8.13 o utiliza la configuración de Gradle disponible en Android Studio. No descargues un `gradle-wrapper.jar` desde páginas no oficiales.

## Paso 4 — Ejecutar por primera vez

Cuentas de muestra (contraseña para todas: `Demo123`):

- Operarios: `operario@demo.cl`, `operario02@demo.cl`, `operario03@demo.cl`, `operario04@demo.cl`, `operario05@demo.cl`.
- Supervisores: `supervisor@demo.cl`, `supervisor02@demo.cl`, `supervisor03@demo.cl`, `supervisor04@demo.cl`, `supervisor05@demo.cl`.
- Jefatura: `jefatura@demo.cl`, `jefatura02@demo.cl`, `jefatura03@demo.cl`, `jefatura04@demo.cl`, `jefatura05@demo.cl`.

Se incluyen 15 usuarios ficticios, cinco por rol. Sus asignaciones de granja están definidas en `model/UserSession.kt`.

Después de entrar revisa Panel, Mediciones, Alertas e Historial. El proyecto inserta 12 mediciones ficticias (3 granjas × 4 puntos) si la base está vacía. Para la lista completa de las 15 cuentas y sus asignaciones revisa la sección 4 del README.

## Paso 5 — Arrancar el backend local

En una terminal del computador:

```bash
cd backend
npm start
```

Comprueba `http://localhost:8080/api/health`. El emulador se conecta a `10.0.2.2:8080`. Para teléfono físico, sustituye `API_BASE_URL` en `app/build.gradle.kts` por la IP LAN del computador, en configuración de desarrollo.

## Paso 6 — Qué validar antes de añadir más cambios

- El proyecto sincroniza y compila.
- Puedes entrar con las tres cuentas.
- 12 registros demo aparecen en el panel/historial.
- Registrar 3,50 ppm produce estado Normal.
- Registrar 1,50 ppm produce alerta crítica.
- La medición local se conserva después de cerrar/reabrir.
- Cámara y galería abren desde el formulario de acción correctiva y la imagen se copia al almacenamiento privado.

Si una etapa falla, detén el avance y guarda el mensaje completo del Build/Run. No continúes pegando nuevos fragmentos encima de una compilación fallida.
