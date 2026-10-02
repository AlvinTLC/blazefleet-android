# BlazeFleet Android — Native Fleet Monitoring Application 🛰️🤖

Aplicación nativa en **Kotlin (Jetpack Compose / Material 3 / Android SDK 34)** diseñada para la monitorización en tiempo real, gestión satelital de vehículos y ejecución de comandos remotos en la plataforma BlazeFleet.

---

## 🚀 Características Principales

- **🛰️ Streaming Satelital en Tiempo Real**:
  - Conexión **WebSocket v2** (`/ws/live?ticket={ticket}&v=2`) gestionada con OkHttp y Kotlin Coroutines StateFlow.
  - Actualización atómica de latitud, longitud, rumbo (curso en grados), velocidad e ignición.
- **🗺️ Mapa Satelital Nativo (Google Maps / Custom Dark Tiles)**:
  - Marcadores personalizados con rotación de rumbo y halos de estado en movimiento/ralentí/detenido.
  - Fusión de telemetría y vista de detalle en hoja inferior.
- **🛡️ Inmovilizador & Comandos Remotos**:
  - Apagado remoto de motor (`engine_stop`) y habilitación (`engine_resume`).
  - Modal de confirmación de seguridad conectado a `/api/v1/mobile/vehicles/{id}/command`.
- **🔔 Notificaciones Push Nativas (Firebase Cloud Messaging - FCM)**:
  - Registro de token de dispositivo en `/api/v1/mobile/push-token`.
  - Canales de notificación del sistema Android (`blazefleet_critical` para SOS y `blazefleet_alerts` para geocercas).
  - Prioridad alta con vibración y sonido en segundo plano.
- **🔐 Seguridad y Sesión**:
  - Almacenamiento seguro de tokens JWT con **EncryptedSharedPreferences** (cifrado por hardware AES-256-GCM).
  - OkHttp Authenticator con rotación automática de refresh token (ventana de gracia de 60s).

---

## 🏗️ Arquitectura de la Aplicación

Construida bajo arquitectura **MVVM (Model-View-ViewModel)** y **Unidirectional Data Flow (UDF)** con Jetpack Compose:

```
apps/android/app/src/main/java/do/blaze/fleet/
├── MainActivity.kt                      # Actividad principal con Compose Navigation
├── data/
│   ├── model/Models.kt                 # Modelos de datos DTO, Enums y Envelopes
│   ├── local/TokenManager.kt           # Manejador seguro de tokens (EncryptedSharedPreferences)
│   └── remote/
│       ├── BlazeFleetApi.kt            # Interfaz de endpoints Retrofit
│       ├── RetrofitClient.kt           # Cliente HTTP con Authenticator y logging
│       └── WebSocketClient.kt          # Cliente WebSocket OkHttp con StateFlow y reconexión
├── service/
│   └── BlazeFleetFirebaseMessagingService.kt # Manejo de push FCM y canales de alerta
├── ui/
│   ├── theme/                          # Tokens de diseño BlazeTheme, Color y Tipografía
│   ├── components/Components.kt        # StatusChip, VehicleCard, TelemetryGauge
│   ├── screens/Screens.kt              # LoginScreen, FleetListScreen, VehicleDetailScreen
│   └── viewmodel/
│       ├── AuthViewModel.kt            # Manejo de login y sesión
│       ├── FleetViewModel.kt           # Búsqueda, filtrado y telemetría en vivo
│       └── VehicleDetailViewModel.kt   # Control de comandos remotos y telemetría unitaria
```

---

## 🛠️ Requisitos de Compilación

- **Android Studio Iguana | 2023.2.1+** (o Koala / Ladybug)
- **JDK 17+**
- **Android SDK 34 (compileSdk 34, minSdk 26 - Android 8.0 Oreo)**
- **Kotlin 2.0+**
- **Gradle 8.5+**

---

## ⚙️ Configuración del Servidor

El cliente Retrofit se conecta por defecto a `https://fleet.blaze.do`.
Para entornos de desarrollo locales o staging, la URL base se puede configurar dinámicamente mediante `TokenManager.saveServerUrl(...)`.
