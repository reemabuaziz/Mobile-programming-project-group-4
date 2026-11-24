# Item Details - Firebase Database Integration Testing

## Overview
The Item Details screen was verified against the existing Firebase-backed data flow. It reads the listing selected on the Home Feed from `ExchangeData.listings` (kept in sync with Firestore by `FirebaseExchangeRepository`) and sends requests through `ExchangeViewModel.request` -> `RequestMaterialUseCase` -> `FirebaseExchangeRepository` -> `FirebaseRequestDataSource` -> Firestore. No Firebase code, database structure, or theme was changed.

## Changes
* Loading state: a progress indicator is shown while data is still syncing, instead of "Item not found".
* Sending state: the request button shows "Sending..." until the request finishes.
* Request status: when the signed-in student already requested the item, the screen shows "Your request: <status>".

## Manual Tests (Android emulator, Firebase Firestore)
| Test | Result |
|---|---|
| Open an item from the Home Feed and display photo, title, category, condition, quantity, exchange type, pickup area, owner, status, and description from Firestore | Passed |
| Already-requested item shows the request message and keeps Request Material disabled | Passed |

## Automated Tests
Request rules (own item, unavailable item, duplicate request, retry after rejection) are covered by the existing ExchangeRulesTest unit tests.

## Conclusion
Item Details displays real listing data from Firebase Firestore and sends material requests through the existing Firebase request flow, with loading, not-found, sending, and already-requested states handled.
