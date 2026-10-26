System Architecture — Project Material Exchange

1. Overview

Project Material Exchange is an Android application that helps university students publish, browse, and request reusable project materials.

The application separates responsibilities into three layers: UI, Domain, and Data.

2. UI Layer

The UI layer displays screens and handles user interactions. Compose screens receive UI state and send events to their ViewModels.

Planned classes:

* ItemDetailsRoute
* ItemDetailsScreen
* ItemDetailsViewModel
* MyActivityViewModel

The UI must not access repositories or storage directly.

3. Domain Layer

The Domain layer contains application use cases and repository interfaces.

Planned classes and contracts:

* RequestMaterialUseCase
* ObserveListingUseCase
* ObserveRequestEligibilityUseCase
* ObserveSentRequestsUseCase
* RequestRepository
* ListingRepository
* SessionRepository

The Domain layer must not depend on Android or Firebase SDK types.

4. Data Layer

The Data layer implements repository interfaces and accesses remote data sources.

Planned classes:

* RequestRepositoryImpl
* ListingRepositoryImpl
* SessionRepositoryImpl
* RequestRemoteDataSource
* ListingRemoteDataSource
* SessionDataSource

Firebase-specific access remains in the Data layer.

5. External Services

* Firebase Authentication provides authenticated student identity.
* Cloud Firestore stores application records such as listings and requests.
* Firebase Cloud Storage stores listing images.

6. Unidirectional Data Flow

User events flow from ItemDetailsScreen to ItemDetailsViewModel. The ViewModel invokes RequestMaterialUseCase, which uses repository interfaces to perform the required operations.

The Data layer implements these interfaces and accesses remote data sources. Results return through the Domain layer to the ViewModel.

The ViewModel updates ItemDetailsUiState, and ItemDetailsRoute collects and passes the state to ItemDetailsScreen for rendering.

7. Dependency Rules

1. The UI layer does not access repositories or storage directly.
2. The Domain layer defines use cases and repository interfaces.
3. The Data layer implements repository interfaces defined in Domain.
4. The Domain layer does not depend on Android or Firebase SDK types.
5. UI events flow toward the ViewModel, while UI state flows from the ViewModel toward the UI.

This document describes the planned architecture and does not claim that all listed classes or backend operations have already been implemented.
