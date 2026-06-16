# AGENTS.md

Guía para agentes de IA (Claude Code, Gemini en Android Studio, Cursor, Copilot, etc.) que
contribuyen a **FinFlow**. Sigue el estándar de [agents.md](https://agents.md/).

Este archivo define **los estándares, la arquitectura y las buenas prácticas** con los que se
implementa cualquier cambio en el repositorio. Para la visión del producto y los comandos de
usuario, ver [README.md](README.md).

---

## Resumen del proyecto

FinFlow es una app Android de **finanzas personales offline-first**: registro de ingresos/gastos por
categorías, balance y gráficos, con base de datos **local cifrada** y desbloqueo **biométrico**.
**No hay backend**: la base de datos Room es la **única fuente de verdad** (SSOT).

- **Lenguaje:** Kotlin · **UI:** Jetpack Compose + Material 3 · **DI:** Hilt
- **Arquitectura:** Clean Architecture + **MVI** (un estado inmutable por pantalla)
- **Estado reactivo:** Coroutines + Flow / StateFlow
- **Datos:** Room (+ KSP) sobre SQLCipher · **Preferencias:** DataStore
- **Seguridad:** Android Keystore + `androidx.biometric`
- **Sistema:** Glance (widget) + WorkManager (background)
- **package:** `com.hacybeyker.finflow`

---

## Clean Architecture

Tres capas con una **regla de dependencias estricta**: las dependencias apuntan **hacia el dominio**.

```
app/src/main/java/com/hacybeyker/finflow/
├── domain/   # Modelos, casos de uso e interfaces de repositorio. Kotlin PURO (sin Android).
├── data/     # Room (DB, DAOs, entities), mappers entity↔domain, implementación de repositorios.
└── ui/       # Pantallas Compose, ViewModels (MVI), theme/. Orquesta casos de uso.
```

Regla de dependencias: **`ui → domain ← data`**.

- El **dominio no conoce** Android, Room, Compose ni Hilt; no importa nada de framework.
- La **UI depende del dominio** (casos de uso e interfaces), **nunca** de `data` directamente.
- **`data` implementa** las interfaces declaradas en `domain` (inversión de dependencias).
- Los **mappers** entity↔domain viven en `data`. Un modelo de dominio nunca expone una `@Entity`.
- Cada capa tiene su **propio modelo**: entity (data) ↔ modelo de dominio ↔ UI state. No se filtran
  entities a la UI.

---

## Principios SOLID (cómo se aplican aquí)

- **S — Responsabilidad única:** un caso de uso = una operación de negocio (`AddTransaction`,
  `GetBalance`). Los ViewModels orquestan estado de UI; no contienen lógica de persistencia. Los
  Composables solo pintan estado y emiten intents.
- **O — Abierto/cerrado:** extiende vía nuevos casos de uso o nuevas implementaciones de interfaz,
  sin modificar las existentes. Evita `when` gigantes sobre tipos que crecen.
- **L — Sustitución de Liskov:** los fakes/mocks de los repositorios deben respetar el contrato de la
  interfaz de dominio para que los tests sean fiables.
- **I — Segregación de interfaces:** repositorios pequeños y cohesivos
  (`TransactionRepository`, `CategoryRepository`, `PreferencesRepository`), no un mega-repositorio.
- **D — Inversión de dependencias:** el dominio define interfaces; `data` las implementa; Hilt las
  enlaza. La UI depende de abstracciones (casos de uso/interfaces), no de implementaciones.

---

## Patrones y reglas de implementación (obligatorios)

- **MVI por pantalla:** cada pantalla expone un único `StateFlow<XxxUiState>` **inmutable** desde el
  ViewModel. Las interacciones entran como **intents** (`sealed interface`), no llamando métodos
  sueltos del ViewModel de forma ad-hoc. Estados explícitos: `loading / empty / content / error`.
- **SSOT con Room:** los DAOs devuelven `Flow`. La UI **reacciona** al Flow; no refresca a mano ni
  mantiene cachés paralelas a la BD.
- **Lógica de negocio en `domain`:** cálculos (balance, agregados) y validaciones viven en casos de
  uso o ViewModel, **nunca en Composables**. La validación de formularios va en el ViewModel.
- **Coroutines:** usa `viewModelScope`; expón estado con `StateFlow`; combina flujos con
  `combine` / `flatMapLatest`. **Inyecta** el `CoroutineDispatcher` (no hardcodees `Dispatchers.IO`)
  para poder testear.
- **Hilt:** módulos en `data` para BD, DAOs y repositorios. Interfaces de dominio enlazadas con
  `@Binds`. ViewModels con `@HiltViewModel` + `@Inject`.
- **Migraciones Room versionadas.** **Nunca** `fallbackToDestructiveMigration` en código real.
- **Seguridad:** la passphrase de SQLCipher se genera y guarda en **Keystore** /
  EncryptedSharedPreferences; nunca hardcodeada ni en texto plano. No loguear datos sensibles.
- **Gráficos con Canvas de Compose**, sin librerías de charting (decisión consciente: APK ligero).
- **Inmutabilidad:** modelos de dominio y UI state como `data class` inmutables; colecciones como
  `List` de solo lectura. Evita estado mutable compartido.

---

## Flujo para implementar un feature / fix / enhancement

1. **Ubica la capa correcta.** ¿Es regla de negocio? → `domain`. ¿Persistencia/mapeo? → `data`.
   ¿Presentación? → `ui`. Respeta `ui → domain ← data`.
2. **Modela primero el dominio** (si aplica): modelo + caso de uso + interfaz de repositorio en
   Kotlin puro, con su unit test.
3. **Implementa en `data`:** entity/DAO/migración + mapper + implementación de repositorio; enlaza
   con Hilt.
4. **Conecta la UI:** define/extiende `UiState` e intents; el ViewModel orquesta los casos de uso y
   expone `StateFlow`; el Composable consume estado y emite intents.
5. **Tests:** unit tests de la lógica nueva (caso de uso y/o ViewModel). Screenshot test si hay UI
   visual relevante (p. ej. el gráfico).
6. **Verifica:** `./gradlew formatAndAnalyze` y `./gradlew test` en verde.
7. **Documenta:** añade una entrada en `CHANGELOG.md` bajo `[Unreleased]` con el tipo
   (`Added` / `Fixed` / `Changed`/`Enhancement` / `Security`).

Mantén el cambio **enfocado y atómico**: un feature/fix por vez, sin tocar de paso código no
relacionado.

---

## Convenciones de código

- Estilo ktlint `android_studio`, **`max_line_length = 120`**, indentación **4 espacios**,
  `end_of_line = lf` (ver `.editorconfig`).
- **Prohibidos los wildcard imports** (`import x.*`) y las **trailing commas**. Lo fuerza ktlint en
  el build, no solo el editor.
- Funciones `@Composable`/`@Preview` en **PascalCase** (excepción ya configurada). El resto de
  funciones en camelCase; clases en PascalCase.
- Nombres descriptivos y orientados a intención; nada de abreviaturas crípticas.
- Comenta solo lo **no obvio** (el porqué), no lo evidente.
- Dependencias **siempre** en el Version Catalog (`gradle/libs.versions.toml`), referenciadas con
  `libs.*`. Nunca versiones inline en `build.gradle.kts`.

---

## Testing

### Unit tests (obligatorios para lógica nueva)
- **Casos de uso y ViewModels** con **MockK** + **Turbine** (para `Flow`/`StateFlow`) +
  `kotlinx-coroutines-test` (usa un `TestDispatcher` inyectado).
- El **dominio se testea sin emulador** (Kotlin puro, repositorios fakeados que respetan el contrato).
- Patrón **Arrange–Act–Assert**; un comportamiento por test; nombres que describen el caso.
- Toda nueva lógica de negocio o de ViewModel **debe** ir acompañada de su test.

### Screenshot testing
- Componentes visuales clave (en especial los **gráficos con Canvas**) se cubren con **Roborazzi**.
- Flujo: generar baseline (`recordRoborazziDebug`) y verificar en cada cambio
  (`verifyRoborazziDebug`). Las imágenes baseline se versionan; cualquier diff visual debe ser
  intencional y revisado.
- Usa datos deterministas y fija dimensiones/tema para que el render sea reproducible.

### Verificación previa a cerrar
```bash
./gradlew formatAndAnalyze   # ktlintFormat → ktlintCheck + detekt + lint
./gradlew test               # unit tests JVM
```

---

## Comandos

| Acción                       | Comando                          |
|------------------------------|----------------------------------|
| Formatear                    | `./gradlew ktlintFormat`         |
| Verificar estilo             | `./gradlew ktlintCheck`          |
| Análisis estático            | `./gradlew detekt`               |
| Android Lint                 | `./gradlew lint`                 |
| Lint + ktlint + detekt       | `./gradlew codeQuality`          |
| Formatear + verificar todo   | `./gradlew formatAndAnalyze`     |
| Unit tests (JVM)             | `./gradlew test`                 |
| Tests instrumentados         | `./gradlew connectedAndroidTest` |
| Compilar (debug)             | `./gradlew assembleDebug`        |

> En Windows: `gradlew.bat`. Config: `compileSdk 37`, `targetSdk 36`, `minSdk 26`, **JDK 11**.

---

## Qué hacer y qué NO hacer

**Sí:**
- Respetar Clean Architecture y el flujo `ui → domain ← data`.
- Aplicar SOLID: responsabilidad única, interfaces pequeñas, dependencias hacia abstracciones.
- Un único `UiState` inmutable por pantalla con estados explícitos.
- Acompañar la lógica nueva con unit tests y, si hay UI visual, screenshot test.
- Centralizar dependencias en el Version Catalog y justificar cada alta.
- Dejar `formatAndAnalyze` y los tests en verde, y actualizar el `CHANGELOG.md`.

**No:**
- No meter lógica de negocio o validación en Composables.
- No filtrar `@Entity` de Room ni detalles de `data` hacia `ui`.
- No usar `fallbackToDestructiveMigration`, wildcard imports ni trailing commas.
- No hardcodear secretos, claves ni passphrases; no loguear datos financieros.
- No añadir librerías pesadas de charting (los gráficos se hacen con Canvas).
- No mezclar varios features/fixes en un mismo cambio.
