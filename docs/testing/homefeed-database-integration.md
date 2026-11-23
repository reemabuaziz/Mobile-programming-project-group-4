Home Feed – Firebase Database Integration Testing
Overview
The Home Feed screen was tested to verify that it displays materials retrieved from Firebase Firestore.
Tests Performed
Test	Result
Display materials from Firestore	Passed
Display a newly added material	Passed
Update a material title in Firestore and verify the Home Feed updates automatically	Passed
Search for an existing material	Passed
Search for a non-existing material	Passed
Filter materials by category	Passed
Open material details	Passed
Verify materials remain available after restarting the app	Passed
Test Example
* Material: SG90 Micro Servo Motor
* Category: Motors
* Database: Firebase Firestore
* Result: The material appeared in the Home Feed. Changes made in Firestore were reflected automatically without restarting the app.
  Conclusion
  The Home Feed successfully displays materials from Firebase Firestore. The tested search, category filtering, and real-time updates worked as expected.
The Home Feed was tested with Firebase Firestore to verify that materials are retrieved and displayed correctly. I also enhanced the Home Feed by adding a dynamic materials count and displaying the quantity of each material.
