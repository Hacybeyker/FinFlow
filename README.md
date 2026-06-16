# FinFlow

> Gestor de finanzas personales **offline-first**, cifrado y con widget de pantalla de inicio.

FinFlow es una aplicación Android para controlar **ingresos y gastos** que funciona **100% sin conexión**. El usuario registra movimientos por categorías, consulta su balance y tendencias en gráficos dibujados a mano con Canvas, y dispone de un **widget** en la pantalla de inicio con el resumen del mes. La base de datos es la **única fuente de verdad** (no hay backend) y, por tratarse de datos financieros sensibles, está **cifrada en reposo** y protegida con **biometría**.

---

## ¿Por qué FinFlow y para qué sirve?

La mayoría de gestores de finanzas obligan a crear una cuenta, suben tus datos a la nube y dependen de internet para algo tan privado como cuánto gastas. FinFlow resuelve eso desde el lado contrario:

- **Privacidad real:** tus finanzas nunca salen del dispositivo. La fuente de verdad es una base de datos local cifrada con SQLCipher; la passphrase vive en el Android Keystore.
- **Funciona siempre:** al ser offline-first, no hay pantallas de carga ni errores de red. La UI reacciona al instante a los cambios de datos gracias a Room + Flow.
- **Visión clara de tu dinero:** balance del mes, gasto por categoría (gráfico de dona) y evolución mes a mes (gráfico de barras), todo dibujado con el Canvas de Compose para mantener el APK ligero.
- **Presente fuera de la app:** un widget con Glance muestra el resumen sin abrir la aplicación, y los recordatorios con WorkManager ayudan a no olvidar registrar gastos.

En resumen: **sirve para llevar el control de tus finanzas personales de forma privada, rápida y sin depender de internet ni de ningún servidor.**

---

## Tecnologías

### Lenguaje y UI
- **Kotlin** + **Jetpack Compose** (Material 3 con dynamic color)
- **Navigation 3**
- **Canvas de Compose** para los gráficos (sin librerías de charting)

### Arquitectura
- **Clean Architecture ligera** (capas `domain` / `data` / `ui`)
- **MVI**: un estado inmutable por pantalla (unidirectional data flow)
- **Coroutines + Flow / StateFlow** para el estado reactivo
- **Hilt** para inyección de dependencias

### Datos y seguridad
- **Room** (+ KSP) como única fuente de verdad, con DAOs que devuelven `Flow`
- **SQLCipher** (`net.zetetic:android-database-sqlcipher`) + `androidx.sqlite` — cifrado de la BD
- **Android Keystore** / `androidx.security:security-crypto` — guardado seguro de la passphrase
- **androidx.biometric** — desbloqueo con huella/rostro
- **DataStore** — preferencias (moneda, tema, toggle de biometría)

### Sistema y background
- **Jetpack Glance** (`glance-appwidget`) — widget de pantalla de inicio
- **WorkManager** (`work-runtime-ktx`) — recordatorios y tareas diferidas

### Testing y calidad
- **JUnit**, **MockK**, **Turbine**, **kotlinx-coroutines-test**
- **Roborazzi** (opcional, screenshot test del gráfico)
- **ktlint** + **detekt** + **Android Lint** para calidad de código

---

## Requisitos

- **JDK 11** (el proyecto compila con `sourceCompatibility`/`targetCompatibility` = 11)
- **Android Studio** (versión compatible con AGP `9.2.1`)
- **Android SDK**: `compileSdk 37`, `targetSdk 36`, `minSdk 26`
- No se requiere editar versiones a mano: todas viven en el Version Catalog (`gradle/libs.versions.toml`).

---

## Puesta en marcha

```bash
# Clonar el repositorio
git clone <url-del-repo> FinFlow
cd FinFlow

# (Opcional) Configurar la ruta del SDK en local.properties
# sdk.dir=/ruta/al/Android/Sdk
```

> En Windows usa `gradlew.bat` en lugar de `./gradlew`.

---

## Comandos

### Compilación y ejecución

```bash
# Compilar el proyecto (debug)
./gradlew assembleDebug

# Instalar en un dispositivo/emulador conectado
./gradlew installDebug

# Compilación de release
./gradlew assembleRelease

# Limpiar artefactos de build
./gradlew clean
```

### Formateo de código

```bash
# Formatear automáticamente el código Kotlin con ktlint
./gradlew ktlintFormat
```

### Lint, formateo y verificación de sintaxis

El proyecto incluye **tareas agregadas** definidas en `app/build.gradle.kts`:

```bash
# Verificar estilo con ktlint (sin modificar archivos)
./gradlew ktlintCheck

# Análisis estático con detekt
./gradlew detekt

# Android Lint
./gradlew lint

# Las tres anteriores en un solo comando (lint + ktlint + detekt)
./gradlew codeQuality

# Formatea primero (ktlintFormat) y luego ejecuta toda la verificación
./gradlew formatAndAnalyze
```

> Recomendado antes de cada commit: `./gradlew formatAndAnalyze`.

### Tests

```bash
# Unit tests (JVM)
./gradlew test

# Tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedAndroidTest
```

### Verificación completa

```bash
# Build + tests + chequeos de calidad configurados
./gradlew check
```

---

## Estructura del proyecto

```
FinFlow/
├── app/
│   └── src/main/java/com/hacybeyker/finflow/
│       ├── domain/   # Entidades, casos de uso e interfaces de repositorio (Kotlin puro)
│       ├── data/     # Room, DAOs, mappers e implementación de repositorios
│       └── ui/       # Pantallas Compose, ViewModels (MVI) y tema
├── config/detekt/    # Configuración de detekt
├── gradle/
│   └── libs.versions.toml   # Version Catalog (única fuente de verdad de dependencias)
├── lint.xml          # Reglas de Android Lint
├── .editorconfig     # Estilo de código (lo lee ktlint)
├── CHANGELOG.md      # Historial de versiones
└── AGENTS.md         # Guía para asistentes de IA
```

---

## Convenciones de código

- Estilo `android_studio`, `max_line_length = 120`, indentación de 4 espacios (ver `.editorconfig`).
- **Sin wildcard imports** ni trailing commas (forzado por ktlint).
- Funciones `@Composable`/`@Preview` en PascalCase (excepción configurada en ktlint).

---

## Licencia

Distribuido bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

Copyright © 2026 Carlos Osorio (hacybeyker).
