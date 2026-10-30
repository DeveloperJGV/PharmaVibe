# PharmaVibe (Internal Project: ControlFarmacia)

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-EADDFF?style=for-the-badge&logo=materialdesign&logoColor=black)
![Hilt](https://img.shields.io/badge/Dagger%20Hilt-00C4B3?style=for-the-badge&logo=android&logoColor=white)

> Smart medication expiration tracker using CameraX & ML Kit. Started as a 100% "vibe code" experiment generated with the Panda 2 AI agent in Android Studio, then reviewed and cleaned up by hand.

## The AI "Vibe Code" Experiment
The initial architecture, UI and feature set were generated via prompting with the **Panda 2 AI Agent** within Android Studio. A later pass reviewed that output, fixed the bugs it shipped with and brought the code up to the practices listed below. The commit history keeps both stages.

## Project Brief
PharmaVibe (ControlFarmacia) keeps track of a home pharmacy. Register a medication by pointing the camera at the box (the name is read with on-device OCR), add the dosage, who it is for (a person or a pet) and the expiration month. The app warns before medications expire and, while a medication is "in treatment", reminds you of each intake with daily alarms.

## Key Features
* **Camera registration:** take a photo of the packaging; ML Kit text recognition fills in the medication name live.
* **Inventory dashboard:** totals, expiring-this-month and expired counts, plus a searchable grid with photos.
* **Expiration alerts:** a daily WorkManager job notifies about expired and soon-to-expire medications.
* **Treatment reminders:** exact alarms at the times you choose, re-armed after reboots, time changes and app updates, with a "mark as taken" action.
* **English and Spanish** UI.

## Architecture

```
domain/      Medication model, ExpiryStatus, repository/scheduler/photo-store interfaces (pure Kotlin)
data/        Room (entity, DAO, mapper), AlarmManager scheduler and receivers, notifications, WorkManager, photo storage
di/          Hilt modules (database, bindings, Clock)
ui/          Compose screens + ViewModels: dashboard, home, registration, detail; camera + OCR analyzer; theme
```

* ViewModels expose a single `StateFlow` of UI state and depend only on domain interfaces, so they are unit-tested with in-memory fakes (`app/src/test`).
* `Clock` is injected wherever "today" matters.
* Room schema is exported to `app/schemas/`; destructive migration is only allowed from the pre-release versions 1 and 2.
* Navigation uses type-safe routes (`@Serializable` route objects).

## Technical Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.4 (built into AGP 9.5), Java 17 target, compile/target SDK 37 |
| **Architecture** | MVVM with a small domain layer |
| **Dependency Injection** | Dagger-Hilt |
| **UI** | Jetpack Compose (Material 3), Navigation Compose, shared element transitions |
| **Persistence** | Room (KSP) |
| **Media & Vision** | CameraX, ML Kit Text Recognition, Coil 3 |
| **Background** | WorkManager (expiration checks), AlarmManager (intake reminders) |
| **Concurrency** | Kotlin Coroutines & Flow |
| **Tests** | JUnit 4, kotlinx-coroutines-test |

## Building

```
./gradlew :app:assembleDebug       # debug APK
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew :app:lintDebug           # lint report
```

Requires JDK 17 and Android SDK 37 (Gradle 9.8, AGP 9.5).
