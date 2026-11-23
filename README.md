
<div align="center">

# PROJECT MATERIAL EXCHANGE

**Reuse, exchange, and give academic project materials a second life.**

A student-focused Android application designed to reduce costs and minimize project waste.

[![Technologies](https://skillicons.dev/icons?i=kotlin,androidstudio,firebase,gradle,git,github)](https://skillicons.dev)

</div>

---

## About the Project

Project Material Exchange is a student-focused Android application that helps university students reuse and exchange materials from academic and graduation projects.

Usable components such as sensors, Arduino or ESP32 boards, cables, motors, electronic parts, 3D-printing filament, and mechanical components often remain after projects are completed.

The application helps students list those materials, browse and search for items, filter listings, and send requests to item owners instead of buying duplicate supplies.

Listings and requests use clear states such as **Available, Pending, Reserved, and Completed**.

The project promotes sustainability, affordability, and student collaboration by reducing unnecessary purchases and material waste.

## Core Features

- **Student authentication and profile:** Account and profile-related screens.
- **Material listings:** Create, browse, and manage reusable project material listings.
- **Browse, search, and filters:** Discover materials using available search and filtering options.
- **Material requests:** Send and manage exchange requests.
- **Activity management:** Review listings and exchange activity.
- **Local notifications:** Notification channel, test reminders, and scheduled daily reminders.
- **System event handling:** Receive selected Android system events, including airplane mode and power connection changes.
- **Profile preferences:** Manage the daily reminder setting.
- **Rating interface:** Native Android RatingBar integrated into the Compose interface.

## Technology Stack

- Kotlin
- Android Studio
- Jetpack Compose
- Navigation Compose
- Kotlin Coroutines and StateFlow
- WorkManager
- Android BroadcastReceiver
- Android Notification APIs
- Gradle
- Git and GitHub

Firebase Authentication, Cloud Firestore, and Cloud Storage are planned services and are not currently connected to the application implementation.

## Android Application Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/group04/materialexchange/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   ├── navigation/
│   │   │   ├── platform/
│   │   │   │   ├── ExchangeNotifications.kt
│   │   │   │   ├── ExchangeReminderWorker.kt
│   │   │   │   └── ExchangeSystemReceiver.kt
│   │   │   └── ui/
│   │   │       └── screens/
│   │   └── AndroidManifest.xml
│   └── test/
```

The application follows a single-activity, Jetpack Compose-based architecture.

## Android System Integration

### Broadcast Receiver

The application includes a BroadcastReceiver for selected system events:

- Airplane mode changes.
- Power connected.
- Power disconnected.
- Battery low.
- Battery okay.

The receiver is registered and unregistered with the application UI lifecycle.

### Notifications and WorkManager

The application includes:

- Notification channel creation.
- Notification permission handling for Android 13 and newer.
- Local notification delivery.
- One-time test reminder.
- Daily periodic reminder scheduling.
- Reminder cancellation.
- Battery-not-low constraint for scheduled work.

**Note:** These are local notifications. Firebase Cloud Messaging and remote push notifications have not been implemented.

### Compose and Android UI Integration

The application uses Jetpack Compose and includes Android UI interoperability through `AndroidView` for the native RatingBar component.

## Lecture Concepts Integration

Lecture concepts are mapped to their corresponding implementation locations in the source code.

See:

- [Lecture Concepts Mapping](docs/lecture-concepts-mapping.md)

The project uses a Compose-first, single-activity architecture. Legacy Fragment-based navigation is not part of the active application architecture.

## Milestone 2 — Stage 1

### UI Prototype

[Open Project Material Exchange in Figma](https://www.figma.com/design/pYJ3tZiuEZlltdclmaNnrE/Project-Material-Exchange---Milestone-2?node-id=3-20&t=GHMxwafS3Qg6O6Cy-1)

### Documentation

- [All documentation](docs/)
- [Wireframes — eight core screens](docs/wireframes/)
- [Navigation map](docs/navigation-map.png)
- [UI style guide](docs/style-guide.md)

## Milestone 2 — Stage 2

- [System architecture diagram](docs/architecture.png)
- [System architecture description](docs/architecture.md)
- [AI use log](docs/ai-use-log.md)

The architecture and class responsibilities documented in the earlier milestones describe the planned design. They do not imply that every planned service or operation has been implemented.

## Project Progress and Remaining Requirements

| Area | Current Status |
|---|---|
| Android application structure | Implemented |
| Jetpack Compose UI | Implemented |
| Navigation | Implemented |
| Listings and exchange workflows | Implemented in the current application |
| ViewModel and StateFlow | Implemented |
| State preservation | Implemented in selected screens |
| Local notifications | Implemented |
| WorkManager reminders | Implemented |
| BroadcastReceiver integration | Added; device verification required |
| Native Android RatingBar | Integrated |
| Firebase Authentication | Not connected |
| Cloud Firestore | Not connected |
| Firebase Cloud Storage | Not connected |
| Firebase Cloud Messaging | Not implemented |
| Exact-time alarm scheduling | Not implemented |
| Automated build and test verification | Pending |

### Milestone Checklist

**Milestone 1**
- Proposal and project documentation are available in the repository.
- Team roles and GitHub Project board should be verified by the team.

**Milestone 2**
- Wireframes, navigation map, architecture documentation, and UI style guide are available.
- Team should verify that the documented flows match the final application.

**Milestone 3**
- Android application source code and project files are included.
- Build verification, device testing, and demonstration evidence should be reviewed before submission.

**Milestone 4**
- Core local application functionality and additional Android integrations are included.
- Firebase integration and remote push notifications remain outstanding if required by the approved project scope.

**Milestone 5 and Final Submission**
- Review and execute the available tests.
- Verify the final application on the required device sizes.
- Prepare the final demonstration video and presentation.
- Verify GitHub Project board updates, contribution history, and release requirements.
- Complete the required AI-use documentation.

GitHub Project board activity, pull request history, CI results, release tags, and demonstration submissions must be verified separately; they cannot be confirmed from the application source files alone.

## Setup and Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle Sync to finish.
4. Connect an Android device or start an emulator.
5. Run the application using Android Studio.

You can also build the project from the terminal:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

Run unit tests with:

```bash
./gradlew test
```

Run instrumented tests with:

```bash
./gradlew connectedAndroidTest
```

Build and test results depend on the installed Android SDK, Gradle availability, and the configured environment.

## AI Use and Acknowledgment

The team documents meaningful AI assistance and human review in:

[AI Use Log](docs/ai-use-log.md)

AI-assisted implementation and documentation should be reviewed and understood by the team members before submission.

---

**Project Material Exchange — Reuse More. Waste Less.**
