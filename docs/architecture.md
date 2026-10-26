System Architecture — Project Material Exchange

1. Overview

Project Material Exchange is an Android application that helps university students publish, browse, and request reusable project materials.

The application follows a layered architecture consisting of the UI, Domain, and Data layers, which interact with Firebase backend services.

2. UI Layer

The UI layer displays information to the user, receives user interactions, and presents the current screen state.

Planned classes:

* ItemDetailsRoute
* ItemDetailsScreen
* ItemDetailsViewModel
* MyActivityViewModel

UI state and events:

* ItemDetailsUiState
* RequestClicked

Data flow:

* UI events flow from ItemDetailsScreen to ItemDetailsViewModel.
* UI state flows from ItemDetailsViewModel to ItemDetailsRoute and then to ItemDetailsScreen.

The UI layer must not access repositories or Firebase services directly.

3. Domain Layer

The Domain layer contains the application’s business logic and defines repository interfaces.

Planned use cases:

* RequestMaterialUseCase
* ObserveListingUseCase
* ObserveRequestEligibilityUseCase
* ObserveSentRequestsUseCase

Repository interfaces:

* RequestRepository
* ListingRepository
* SessionRepository

The Domain layer defines the operations required by the application without depending directly on Firebase SDK implementation details.

4. Data Layer

The Data layer implements the repository interfaces and communicates with remote data sources.

Planned repository implementations:

* RequestRepositoryImpl
* ListingRepositoryImpl
* SessionRepositoryImpl

Planned data sources:

* RequestRemoteDataSource
* ListingRemoteDataSource
* SessionDataSource

The repository implementations connect the Domain layer to the required remote data sources. Firebase-specific operations are handled within the Data layer.

5. Backend Services

The application uses the following Firebase services:

* Firebase Authentication: Handles user authentication and identity.
* Cloud Firestore: Stores application data, including listings and requests.
* Firebase Cloud Storage: Stores listing images and other uploaded files.

The Data layer communicates with these services through the Firebase SDK.

6. Request Material Data Flow

The request material feature follows these steps:

1. The user interacts with ItemDetailsScreen.
2. The screen sends a RequestClicked event to ItemDetailsViewModel.
3. ItemDetailsViewModel invokes RequestMaterialUseCase.
4. The use case communicates through the RequestRepository interface.
5. RequestRepositoryImpl handles the repository operation and uses RequestRemoteDataSource when remote access is required.
6. The Data layer communicates with the appropriate Firebase service.
7. The operation result returns through the Data and Domain layers to the ViewModel.
8. ItemDetailsViewModel updates ItemDetailsUiState.
9. ItemDetailsRoute passes the updated state to ItemDetailsScreen, which displays the result.

7. Architecture Rules

1. The UI layer handles screen rendering, user events, and UI state.
2. The Domain layer contains use cases and repository interfaces.
3. The Data layer implements the repository interfaces defined by the Domain layer.
4. The UI layer must not access Firebase services directly.
5. UI events flow upward from the screen to the ViewModel, while UI state flows downward to the screen.
6. Firebase-specific implementation details remain in the Data layer.

Note: This document describes the planned architecture and class responsibilities; it does not imply that every listed class or operation has already been implemented.
