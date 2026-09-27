# Implementation Plan - Foundation Rebuild & Household System

Rebuild the core foundation of the Family Command Display as a shared household operating system. This plan covers the global architecture, security modes, production-quality onboarding, and complete household/member management.

## User Review Required

> [!IMPORTANT]
> This is a foundational rebuild. Existing placeholder data may be reset due to schema changes.

## Proposed Changes

### 1. Data Layer & Entities

#### [MODIFY] [HouseholdProfile.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/data/entities/HouseholdProfile.kt)
- Add `coverImageUri`, `anniversary`, `address`, `wifiSsid`, `wifiPassword`, `displayName`, `language`, `country`.

#### [MODIFY] [FamilyMember.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/data/entities/FamilyMember.kt)
- Add `allergies`, `medicalNotes`, `favoriteMeals`, `permissionsJson`.

---

### 2. Global State & Security

#### [MODIFY] [FamilyViewModel.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/ui/FamilyViewModel.kt)
- Refine security modes: `Shared`, `Member`, `Parent`.
- Implement auto-return to `Shared` mode after inactivity.
- Support `Quiet Mode` and `Bedtime Mode` logic.

---

### 3. Foundation UI & Navigation

#### [MODIFY] [MainActivity.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/MainActivity.kt)
- Unified Global App Bar with family photo, name, mode, sync, weather, notifications, and emergency.
- Ensure landscape and tablet responsiveness.

---

### 4. Production Onboarding

#### [MODIFY] [OnboardingScreen.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/ui/screens/OnboardingScreen.kt)
- **Step 1**: Create Household (Identity & Location).
- **Step 2**: Family Members (Comprehensive profile creation).
- **Step 3**: Household Priorities (Feature toggles).
- **Step 4**: Parent Security (PIN & Emergency contacts).
- **Step 5**: Review & Launch.

---

### 5. Management Screens

#### [MODIFY] [HouseholdProfileScreen.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/ui/screens/HouseholdProfileScreen.kt)
- Full editing suite for household identity, location, and technical settings (Wi-Fi).

#### [MODIFY] [FamilyMembersScreen.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/ui/screens/FamilyMembersScreen.kt)
- Full CRUD for members with detailed profile fields (Allergies, Medical, etc.).

---

### 6. Global Polish

#### [MODIFY] [M3Components.kt](file:///Users/thatlango/Documents/famsync-hub/app/src/main/java/com/example/ui/components/M3Components.kt)
- Ensure consistent 8dp spacing and Material 3 motion.

## Verification Plan

### Automated Tests
- `gradle_build`: Verify no compilation errors after schema changes.

### Manual Verification
1.  Complete the new 5-step onboarding flow.
2.  Verify the Global App Bar updates correctly when switching members.
3.  Check Parent PIN protection on management screens.
4.  Test landscape orientation on tablet-sized emulator.
5.  Confirm Emergency button is always accessible.
