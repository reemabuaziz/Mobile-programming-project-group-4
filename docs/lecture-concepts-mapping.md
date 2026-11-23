# CSC 402 Lecture Concepts — Project Mapping

This document maps the concepts from the supplied CSC 402 Lectures 6, 7, 8 and 12 to the actual Material Exchange implementation. “Implemented” means there is code in the app; “design choice” means the lecture topic is intentionally not used because the project uses the newer single-Activity Compose architecture.

| Lecture | Concept | Project implementation | Status |
|---|---|---|---|
| 6 | Activity lifecycle and callbacks | `MainActivity.onCreate()` hosts the Compose app; Android owns the Activity lifecycle. | Implemented |
| 6 | `rememberSaveable` | Sign-in fields, listing form fields, feed filters, profile edits and reminder/rating UI state. | Implemented |
| 6 | ViewModel | `ExchangeViewModel` owns app actions and work. | Implemented |
| 6 | State hoisting / stateless UI / UDF | Screens receive data and callbacks; state is owned above reusable UI components and events are sent upward to the ViewModel. | Implemented |
| 7 | Views and Compose interoperability | `ProfileScreen` embeds the native Android `RatingBar` using `AndroidView(factory, update)`. | Implemented |
| 7 | Single-Activity architecture | One `MainActivity` hosts `MaterialExchangeApp` and the `NavHost` routes. | Implemented |
| 7 | Views, ViewGroups, XML and Fragments / Fragment transactions | Covered as legacy Android concepts; the app deliberately uses Compose Navigation rather than adding a second Activity or Fragment-based navigation stack. | Design choice; not used in app navigation |
| 7 | `ComposeView` / `AndroidFragment` | These are migration/interoperability options for a View/Fragment-hosted app. This project is Compose-first, so they are not required in its current architecture. | Design choice; not used |
| 8 | State and recomposition | Compose state (`rememberSaveable`) drives interactive forms, filters and profile controls. | Implemented |
| 8 | `StateFlow` and lifecycle-aware collection | Repository and ViewModel expose flows; `AppNavGraph` collects them with `collectAsStateWithLifecycle()`. | Implemented |
| 8 | Coroutines and `viewModelScope` | `ExchangeViewModel` runs repository actions inside `viewModelScope.launch`. | Implemented |
| 8 | `LaunchedEffect` | Handles error snackbars and authentication/navigation changes when keys change. | Implemented |
| 8 | `rememberCoroutineScope` | Profile's test-reminder button launches a UI snackbar from a click callback. | Implemented |
| 8 | `DisposableEffect` | Registers and unregisters the system receiver while the app composable is in composition. | Implemented |
| 12 | Broadcasts / `BroadcastReceiver` | `ExchangeSystemReceiver` responds to airplane-mode, power and battery broadcasts while the app UI is active. | Implemented |
| 12 | Register/unregister with `DisposableEffect` | `MaterialExchangeApp` registers the receiver and unregisters it in `onDispose`. | Implemented |
| 12 | Notification channel and runtime permission | `ExchangeNotifications` creates the channel and checks Android 13+ notification permission; Profile requests permission. | Implemented |
| 12 | WorkManager `Worker` | `ExchangeReminderWorker` posts a reminder notification. | Implemented |
| 12 | One-time and periodic work | Profile can queue a one-time test reminder or enable a unique daily periodic reminder. | Implemented |
| 12 | Work constraints | Reminder requests require the battery not to be low. | Implemented |

## Important implementation boundaries

- The current data repository is local on-device storage. Firebase authentication, Firestore synchronization and Cloud Storage are not connected in this alpha.
- WorkManager reminders are local notifications, not Firebase push notifications. Periodic work is approximate (Android chooses the execution time); it is not an exact alarm.
- System broadcasts are registered dynamically while the app UI is active. The app does not claim to monitor these events continuously while closed.
- Legacy Fragment and XML examples from Lecture 7 are documented as alternatives, not added to this Compose-first app just to duplicate navigation systems.
