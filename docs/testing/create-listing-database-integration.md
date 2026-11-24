
# Create Listing Database Integration Testing

## 1. Objective
Verify that the Create Listing and Edit Listing features correctly integrate with Firebase Cloud Firestore and Firebase Storage.

## 2. Test Environment
- Application: Project Material Exchange
- Platform: Android (Kotlin / Jetpack Compose)
- Emulator: Pixel 10
- Database: Firebase Cloud Firestore
- File Storage: Firebase Storage
- Git Branch: `feature/create-listing-database-integration`

## 3. Test Cases and Results

| ID | Test Case | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| TC01 | Create a listing with valid information | Listing is saved in Firestore | Listing successfully saved | PASS |
| TC02 | Verify the created listing in My Activity | Listing appears under My Listings | Listing appeared successfully | PASS |
| TC03 | Verify listing data in Firestore | Document contains correct listing fields | Data verified in Firestore | PASS |
| TC04 | Edit listing title and quantity | Existing document is updated | Title and quantity updated successfully | PASS |
| TC05 | Verify document ID after editing | Same document ID is retained | No duplicate listing created | PASS |
| TC06 | Select an image from the device | Selected image appears in Edit Listing | Image displayed successfully | PASS |
| TC07 | Upload the image to Firebase Storage | Image is stored in the correct path | JPEG image successfully uploaded | PASS |
| TC08 | Display uploaded image in Item Details | Image loads from Firebase Storage | Image displayed successfully | PASS |

## 4. Test Data
- Listing Title: Arduino Board - Test
- Updated Title: Arduino Board - Updated Test
- Category: Electronics
- Condition: Good
- Initial Quantity: 1
- Updated Quantity: 2
- Exchange Type: Free
- Pickup Area: Computer Science College
- Description: Testing Create Listing Firebase integration

## 5. Issue Encountered and Resolution

**Issue:** Uploading an image initially failed with the error:
`User does not have permission to access this object.`

**Cause:** Firebase Storage security rules did not permit the upload.

**Resolution:** Updated and published the Firebase Storage security rules to allow authenticated users to upload supported images to their own listing directories.

**Retest Result:** Image upload succeeded. The uploaded JPEG file was verified in Firebase Storage and displayed correctly in Item Details.

## 6. Final Result

All eight documented test cases passed.

Create Listing and Edit Listing successfully integrate with Firebase Cloud Firestore. Device image upload and retrieval through Firebase Storage were also verified.

The integration is ready for further review and testing before merging into the main branch.
