# Project Plan

Create "EhsaasVerse", a complete offline Urdu Shayari Vault app using Kotlin and Jetpack Compose. The app features a home screen with categories, a shayari list, a detail view with sharing/favoriting, and a saved section. All data is loaded from a local JSON file. UI should follow Material Design 3 with a maroon and beige theme, utilizing Noto Nastaliq Urdu font and supporting RTL.

## Project Brief

# EhsaasVerse Project Brief

EhsaasVerse is a complete offline Urdu Shayari Vault designed to bring the soul of Urdu poetry to a modern Android experience. The app provides a serene, RTL-optimized environment for users to discover, read, and save their favorite verses across different moods.

## Features

*   **Curated Home Experience:** A warm "Assalam-o-Alaikum" greeting paired with a daily "Sher of the Day" card featuring Urdu, Roman script, English translations, and poet credits.
*   **Mood-Based Discovery ("Mehfil by Mood"):** Categorized browsing allowing users to explore poetry based on emotions such as Love, Sadness, Motivation, and Friendship.
*   **Rich Shayari Interactivity:** A dedicated detail view for each verse with options to copy text, share to social platforms, and "Favorite" for offline access.
*   **Personalized Vault:** A "Saved" section that stores bookmarked poetry locally, ensuring your favorite verses are always accessible without an internet connection.

## High-Level Technical Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material Design 3 (M3)
*   **Navigation:** Jetpack Navigation (Compose Navigation)
*   **Concurrency:** Kotlin Coroutines for efficient local JSON parsing and UI state management.
*   **Persistence:** SharedPreferences for lightweight storage of user favorites.

## Design Aesthetics

*   **Color Palette:** Light beige background (#F5F5DC), Deep maroon primary color (#800000).
*   **Typography:** Noto Nastaliq Urdu font.
*   **Layout:** Full Edge-to-Edge display with native RTL support.
*   **Data:** Local JSON in assets folder with 100+ shayari.

## Implementation Steps
**Total Duration:** 5h 18m 50s

### Task_1_Setup_Assets_and_Theme: Setup Material 3 theme (Maroon/Beige), Noto Nastaliq Urdu font, and Shayari data parsing from local JSON assets.
- **Status:** COMPLETED
- **Updates:** Material 3 theme with Maroon (#800000) and Beige (#F5F5DC) implemented. Noto Nastaliq Urdu font integrated. assets/shayari.json created with 100+ entries. Shayari data model and repository for parsing implemented. Project builds successfully.
- **Acceptance Criteria:**
  - Material 3 theme with #800000 and #F5F5DC colors implemented
  - Noto Nastaliq Urdu font integrated and applied
  - JSON data models created and asset file parsed successfully
- **Duration:** 2h 21m 12s

### Task_2_Navigation_and_Main_Screens: Implement Navigation graph and UI for Home (Sher of the Day) and Mood-based Categories screens.
- **Status:** COMPLETED
- **Updates:** Implemented Jetpack Navigation with Bottom Navigation. Home screen features 'Sher of the Day' card and 'Mehfil by Mood' categories. Categories screen displays clickable mood cards. All UI adheres to the Maroon/Beige M3 theme and RTL support. State management handled via ViewModels.
- **Acceptance Criteria:**
  - Navigation between Home, Categories, and Saved screens works
  - Home screen displays 'Sher of the Day' with Urdu/Roman/English text
  - Categories screen displays mood-based list correctly
- **Duration:** 15m 20s

### Task_3_Shayari_List_and_Detail: Create the Shayari List screen and the Detail view with sharing and text copying features.
- **Status:** COMPLETED
- **Updates:** Implemented Shayari List screen with search functionality. Created Shayari Detail screen with Copy and Share actions. Navigation updated to include these screens. Used Noto Nastaliq Urdu font and maintained M3 Maroon/Beige theme. RTL support verified.
- **Acceptance Criteria:**
  - Shayari List screen displays items for selected category
  - Detail view shows full poem with poet info
  - Copy to clipboard and Share functionality works as expected
- **Duration:** 4m 24s

### Task_4_Persistence_and_Favorites: Implement 'Favorite' persistence using DataStore/SharedPreferences and the Saved shayari screen.
- **Status:** COMPLETED
- **Updates:** Implemented 'Favorite' functionality using SharedPreferences. Users can now toggle favorites in the Detail screen. The 'Saved' screen displays all bookmarked shayari with an empty state handler. Favorites persist across app restarts. Unique IDs added to shayari data for reliable tracking.
- **Acceptance Criteria:**
  - Users can favorite/unfavorite shayari in Detail view
  - Saved screen lists all favorited shayari
  - Favorites persist after app restart
- **Duration:** 12m 38s

### Task_5_Final_Polish_and_Verification: Finalize UI with Edge-to-Edge display, create an adaptive app icon, and verify the app's stability and RTL support.
- **Status:** COMPLETED
- **Updates:** The app 'EhsaasVerse' is now fully stabilized and verified.
- **Stability**: Fixed the launch crash by correctly applying the Kotlin Android plugin and stabilizing the build environment (AGP 8.7.0, Gradle 8.10.2, SDK 35).
- **Features**: Verified offline JSON parsing for 100+ shayari, 'Sher of the Day' logic, search functionality, and persistent favorites using SharedPreferences.
- **UI/UX**: Verified the Maroon/Beige Material 3 theme, consistent Noto Nastaliq Urdu typography across all labels, RTL support, and Edge-to-Edge display.
- **Adaptive Icon**: Custom icon with maroon background and quill motif is implemented.
The app is ready for release.
- **Acceptance Criteria:**
  - Full Edge-to-Edge display implemented
  - Adaptive app icon matching the theme created
  - App builds, runs without crashes, and supports RTL layouts
  - Final verification of all features and UI against requirements
  - build pass
  - app does not crash
- **Duration:** 2h 25m 16s

