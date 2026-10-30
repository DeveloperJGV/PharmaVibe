# PharmaVibe (ControlFarmacia) — Plan de revisión y limpieza

Repo: https://github.com/DeveloperJGV/PharmaVibe · Rama de trabajo: `ocr-pivot`
Fecha de la revisión: 2026-10-30

## Contexto

App Android (Kotlin, Compose M3, Hilt, Room, CameraX + ML Kit OCR, WorkManager) generada
al 100 % con el agente Panda 2. Se registra un medicamento con foto, OCR del nombre, dosis,
paciente (humano/mascota) y mes/año de caducidad. Avisa cuando algo caduca y, si el
medicamento está "en tratamiento", recuerda las tomas con alarmas.

Base elegida: rama `ocr-pivot` **incluyendo** el trabajo que estaba sin commitear
(recordatorios de tratamiento, dosis, paciente). Alcance elegido: limpieza profunda sin
cambiar cómo se ve la app, más actualización de todo el toolchain.

## Hallazgos de la revisión (todos atendidos)

### Bugs
- [x] La cámara y el OCR seguían corriendo tras cerrar la vista de cámara → `CameraPreview` ahora libera todo en `DisposableEffect`.
- [x] Borrar un medicamento dejaba la foto huérfana → el repositorio borra la foto al eliminar.
- [x] `fallbackToDestructiveMigration()` borraba la base → solo se permite desde v1/v2; `exportSchema = true` (esquema en `app/schemas/`).
- [x] Pedía `RECEIVE_BOOT_COMPLETED` sin receiver → `RescheduleRemindersReceiver` reprograma tras reinicio, cambio de hora/zona, actualización de la app y cambio del permiso de alarmas exactas.
- [x] `RegistrationScreenErrorPreview` con `TODO()` → arreglado.
- [x] IDs de notificación chocaban → `NotificationIds` con rangos separados (probado).
- [x] La alarma se reprogramaba a "ahora + 24 h" → se reprograma a la hora configurada (`ReminderTiming`, probado).
- [x] Cancelación de alarmas por conteo dejaba huérfanas → se cancelan todos los slots con `FLAG_NO_CREATE`; el receiver relee la base y no notifica si el recordatorio ya no existe.
- [x] Fallo al guardar foto se tragaba → ahora muestra error y no guarda el medicamento.
- [x] `dosageUnit` se guardaba sin dosis → la dosis es `Dosage?` (valor + unidad) o nada.
- [x] Detalle no observaba la base → `observeById` como `Flow`, id por `SavedStateHandle`.
- [x] `DetailUiState.error` nunca se mostraba → estado "no encontrado" visible.
- [x] Dashboard → inventario apilaba pantallas → `launchSingleTop`.

### Prácticas
- [x] Modelo de dominio `Medication` con `YearMonth`, `LocalTime`, enums `DosageUnit` / `PatientCategory`; la entidad de Room ya no llega a la UI.
- [x] Cálculo de vencimiento en un solo lugar (`Medication.expiryStatus`, probado); `Clock` inyectado.
- [x] Todos los textos en `strings.xml` (+ traducción `values-es`), plurales para notificaciones.
- [x] Código muerto eliminado: `BarcodeAnalyzer` + dependencia, `WorkerModule`, `onDosageFormChange`, `resetError`, `buildConfig`, `colors.xml`, `placeholder.png`.
- [x] Deprecaciones: sin `statusBarColor`, `DockedSearchBar` con `InputField`, `AutoMirrored.ArrowBack`, `ExposedDropdownMenuAnchorType`, `hiltViewModel` del paquete nuevo.
- [x] Sin `!!`, sin `catch (Exception)` genéricos en ViewModels, sin `printStackTrace`.
- [x] Permisos: quitados `INTERNET`, `WAKE_LOCK`, `VIBRATE` (vibración vía `LocalHapticFeedback`); `uses-feature camera required=false`.
- [x] Imports explícitos; archivos con el nombre de su clase.
- [x] 25 pruebas unitarias (dominio, mapper, IDs, 3 ViewModels) con fakes en memoria.
- [x] Lint: 0 errores, 0 advertencias.
- [x] README actualizado (OCR en vez de código de barras, arquitectura real, cómo compilar).

## Actualización del toolchain (2026-10-30)

| Pieza | Antes | Ahora |
| --- | --- | --- |
| Gradle | 9.3.1 | 9.8.2 |
| AGP | 8.7.3 | 9.5.0 (Kotlin integrado; sin plugin `kotlin-android`) |
| Kotlin / KSP | 2.0.21 / 2.0.21-1.0.27 | 2.4.21 / 2.3.12 (fijados en `buildscript` del root) |
| compileSdk / targetSdk | 35 / 35 | 37 / 37 (las librerías nuevas exigen API 37) |
| Java target | 11 | 17 |
| Compose BOM | 2024.11.00 | 2026.09.00 (Material3 1.4) |
| Room | 2.6.1 | 2.8.5 (`room-ktx` ya no hace falta) |
| Hilt / androidx.hilt | 2.52 / 1.2.0 | 2.60.1 / 1.4.0 (`hilt-lifecycle-viewmodel-compose`) |
| Navigation | 2.8.4 | 2.10.2 |
| Lifecycle | 2.8.7 | 2.11.0 |
| CameraX | 1.4.0 | 1.6.2 |
| Coil | 2.7.0 | 3.6.3 (`coil3.compose.AsyncImage`) |
| WorkManager | 2.10.0 | 2.12.0 |
| Coroutines / Serialization | 1.9.0 / 1.7.3 | 1.11.0 / 1.12.0 |
| Accompanist permissions | 0.36.0 | 0.37.3 |
| Release | sin minify | minify + shrink (R8 verificado) |

Única advertencia que queda en el build: `Configuration.setVisible` deprecado en Gradle 11, viene de un plugin externo, no del proyecto.

## Verificación en emulador (Pixel 9a, Android 17)
- Registro con foto + OCR en vivo, validación de nombre vacío, guardado con dosis y mascota.
- La cámara se apaga al cerrar la vista (desaparece el indicador verde del sistema).
- Tratamiento activo + horario: la alarma aparece en `dumpsys alarm`; al conceder el permiso de
  alarma exacta se reprogramó sola como exacta (receiver de cambio de permiso).
- Alarma real disparada a las 15:05:00 en punto: notificación con texto, dosis, paciente y acción
  "Mark as taken" (que cierra la notificación); reprogramada para el día siguiente a la misma hora.
- Eliminar: diálogo de confirmación, cero alarmas pendientes, cero fotos huérfanas.
- Todo lo anterior se repitió en humo con el toolchain actualizado (Coil 3 incluido).

## Decisiones tomadas (sin preguntar, por ser menores)
- Inglés como idioma base de la UI; se agrega traducción `values-es`.
- Esquema de base de datos v3 intacto (mismas columnas) para no necesitar migración en teléfonos
  que ya la tienen. La columna `dosageForm` (siempre "Tablet", nunca editable) se conserva en la
  tabla pero sale del modelo de dominio y de la UI; en su lugar se muestra la dosis.
- Se elimina el escaneo de código de barras (dependencia y clase): la app pivotó a OCR.
- Color dinámico de Android 12+ se mantiene como estaba.
- "Marcar como tomada" sigue solo cerrando la notificación; llevar historial de tomas es función nueva.
- Pequeñas mejoras de UX que salieron de los bugs: confirmación antes de eliminar un medicamento,
  botón para quitar un horario individual, mensaje "sin resultados" distinto de "sin medicamentos".
- Las pruebas se corren en el emulador Pixel 9a, nunca en el teléfono conectado.

## Pendientes / ideas futuras
- Historial de tomas ("marcar como tomada" persistente).
- Pedir el permiso de alarmas exactas con un enlace a Ajustes en Android 12+ (hoy cae a alarma inexacta de ±1 h).
- Editar un medicamento ya registrado.
- Pruebas de migración de Room cuando haya una migración real (el esquema ya se exporta a `app/schemas/`).
- Pruebas instrumentadas de UI (hoy no hay ninguna; se quitó la de plantilla).
- Revisar los cambios de comportamiento de Android 17 (targetSdk 37) en un dispositivo real antes de publicar.
