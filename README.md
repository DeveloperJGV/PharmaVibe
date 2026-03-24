# PharmaVibe (Internal Project: ControlFarmacia)

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-EADDFF?style=for-the-badge&logo=materialdesign&logoColor=black)
![Hilt](https://img.shields.io/badge/Dagger%20Hilt-00C4B3?style=for-the-badge&logo=android&logoColor=white)

> Smart medication expiration tracker using CameraX & ML Kit. A 100% "vibe code" experimental project built entirely with the Panda 2 AI agent in Android Studio to test its generative capabilities.

## The AI "Vibe Code" Experiment
This repository is an ongoing experiment in AI-driven development. The entire initial architecture, UI, and feature set were generated via advanced prompting using the **Panda 2 AI Agent** within Android Studio. The goal is to explore the limits of generative AI in building production-ready, highly polished Android applications from scratch.

## Project Brief
PharmaVibe (ControlFarmacia) is a high-performance tracker designed to manage your home pharmacy efficiently. It solves the common problem of mismatched prescription amounts and expiring boxes. By leveraging modern Android APIs, the app simplifies inventory management and ensures you never use expired medication through proactive local notifications.

## Key Features
* **Intelligent Registration:** Capture high-resolution photos of medication packaging and scan barcodes using Google ML Kit for rapid data entry.
* **Smart Inventory Management:** A centralized dashboard to track medication names and expiration dates (Month/Year).
* **Proactive Expiration Alerts:** Automated background monitoring that triggers local notifications to warn users before medications expire.
* **Visual Gallery & Navigation:** Browse the medication inventory with high-quality images and premium shared element transitions.

## High-Level Technical Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Dagger-Hilt |
| **UI Framework** | Jetpack Compose (Material 3), Navigation Compose |
| **Persistence** | Room Database (with KSP) |
| **Media & Vision** | CameraX, Google ML Kit (Barcode), Coil |
| **Background Tasks** | WorkManager |
| **Concurrency** | Kotlin Coroutines & Flow |

## Performance & Design Standards
* **Material 3 Design:** Edge-to-Edge display support with a vibrant, energetic dynamic color scheme and an adaptive app icon.
* **Optimized Hardware Performance:** Animations run at a fluid 60fps, thoroughly optimized for modern flagship hardware such as the Samsung S24.
* **Efficient Media Handling:** Custom `LocalMediaManager` and `ImageCompressor` ensure high-quality photo captures do not bloat internal storage.
