# Project Plan

ControlFarmacia: A Medication Expiration Tracker application.
The app allows users to register medications by taking a photo of the box, optionally scanning a barcode, and inputting the expiration date (Month/Year).
It acts as a smart inventory with local notifications for upcoming expirations.

Key Technical Requirements:
- Architecture: MVVM + Clean Architecture.
- DI: Dagger-Hilt.
- Persistence: Room Database.
- UI: Jetpack Compose (Material 3), Navigation Compose, Shared Element Transitions.
- Camera: CameraX for high-res capture.
- Scanning: Google ML Kit for barcode scanning.
- Image Loading: Coil.
- Background: WorkManager for expiration checks and notifications.
- Media: Image compression before saving to internal storage.
- Design: Vibrant, energetic color scheme, Material 3, Edge-to-Edge, adaptive icon.
- Performance: Smooth 60fps animations, optimized for flagship devices.

## Project Brief

# Project Brief: ControlFarmacia

ControlFarmacia is a high-performance Medication Expiration Tracker designed to help users manage their home pharmacy efficiently. By leveraging modern Android capabilities like CameraX and ML Kit, the app simplifies
 inventory management and ensures users never use expired medication through timely local notifications.

## Features
*   **Intelligent Registration:** Capture high-resolution photos of medication packaging and scan barcodes using Google ML Kit for rapid data entry.
*   **Smart Inventory Management:** A centralized dashboard to track medication names, dosage forms, and expiration dates
 with a focus on Material 3 design.
*   **Proactive Expiration Alerts:** Automated background monitoring that triggers local notifications to warn users before medications expire.
*   **Visual Gallery & Search:** Browse the medication inventory with high-quality images and smooth shared element transitions for a premium user experience.

## High-Level Technical Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3) with Navigation Compose
*   **Architecture:** MVVM + Clean Architecture using Dagger-Hilt for Dependency Injection
*   **Persistence:** Room Database (using KSP for code generation)
*   **Concurrency:** Kotlin Coroutines and Flow
*   **Media & Vision:** CameraX for image capture, Google ML Kit for barcode scanning, and Coil for image loading
*   **Background Processing:** WorkManager for scheduling expiration checks and notifications
*   **Design:** Edge-to-Edge display support
 with a vibrant, energetic Material 3 color scheme

## Implementation Steps
**Total Duration:** 5h 38m 45s

### Task_1_Setup_Architecture_Data: Setup project infrastructure including Dagger-Hilt for DI, Room for local persistence, and core data models.
- **Status:** COMPLETED
- **Updates:** Configured Dagger-Hilt, Room Database, and Medication Repository. Implemented a custom Application class and updated AndroidManifest. Created a vibrant Material 3 theme and an adaptive app icon. Project infrastructure is robust and builds successfully.
- **Acceptance Criteria:**
  - Dagger-Hilt is configured in build scripts and Application class
  - Room database and Medication DAO are implemented
  - Medication repository is established
  - Project builds successfully
- **Duration:** 1h 7m 33s

### Task_2_Medication_Registration_Vision: Implement medication registration using CameraX for image capture and Google ML Kit for barcode scanning.
- **Status:** COMPLETED
- **Updates:** Implemented medication registration using CameraX and Google ML Kit. Created 'LocalMediaManager' and 'ImageCompressor' for efficient image handling. Developed the 'AddMedicationScreen' for capturing photos, scanning barcodes, and inputting expiration dates. Built a 'HomeScreen' to display registered medications in a grid. Configured 'NavigationCompose' to handle screen transitions. Project builds and allows users to register medications with optimized images.
- **Acceptance Criteria:**
  - CameraX provides a functional preview and captures high-res images
  - ML Kit successfully detects barcodes
  - Medications can be saved to the database with image URI and expiration date
- **Duration:** 1h 16m 53s

### Task_3_UI_Dashboard_Inventory: Develop the main UI with Jetpack Compose (Material 3), including the dashboard, medication list, and detail views.
- **Status:** COMPLETED
- **Updates:** Developed the main UI for 'ControlFarmacia' with Jetpack Compose and Material 3. Created a 'DashboardScreen' with medication statistics and a refined 'MedicationList' with dynamic expiry indicators. Implemented a 'MedicationDetail' screen and configured 'NavigationCompose' with premium 'SharedElementTransitions' between the list and detail views. The UI follows Material 3 guidelines and supports full 'Edge-to-Edge' display.
- **Acceptance Criteria:**
  - Dashboard displays medication statistics
  - Medication list shows images using Coil
  - Navigation is implemented between Dashboard, List, and Detail screens
  - Shared element transitions are applied for a premium feel
- **Duration:** 1h 1m 48s

### Task_4_Expiration_Alerts_Notifications: Implement background monitoring using WorkManager to trigger local notifications for upcoming medication expirations.
- **Status:** COMPLETED
- **Updates:** Implemented background monitoring using WorkManager for 'ControlFarmacia'. Developed 'ExpirationCheckWorker' to query the Room database daily for expiring medications. Created 'AppNotificationManager' for managing notification channels and triggering alerts. Added support for 'POST_NOTIFICATIONS' permission (Android 13+). Integrated periodic scheduling with 'WorkManager' in the 'ControlFarmaciaApp' class. The app now provides proactive local notifications for upcoming expirations.
- **Acceptance Criteria:**
  - WorkManager is scheduled to check expirations periodically
  - Local notifications are triggered correctly with relevant medication info
  - App handles notification permissions appropriately
- **Duration:** 1h 5m 50s

### Task_5_Design_Polish_Run_Verify: Refine UI design with Material 3, apply vibrant colors, enable Edge-to-Edge, create adaptive icon, and perform final verification.
- **Status:** COMPLETED
- **Updates:** Refined UI design with Material 3, applied vibrant colors, enabled Edge-to-Edge, and created an adaptive icon for 'ControlFarmacia'. Final verification shows that the app is highly stable and functionally complete. All core features (medication registration, CameraX/ML Kit integration, dashboard, inventory list with shared element transitions, and background notifications) are implemented and working correctly. The UI follows Material 3 guidelines and supports full Edge-to-Edge display.
- **Acceptance Criteria:**
  - Material 3 theme with vibrant/energetic color scheme is implemented
  - Full Edge-to-Edge display is enabled
  - Adaptive app icon matches the core function
  - Final verification shows no crashes and stable performance
- **Duration:** 1h 6m 41s

