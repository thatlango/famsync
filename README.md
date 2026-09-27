# FamSync

A shared, landscape-first family console for an Android tablet. The everyday display is child-safe; parents unlock management with a PIN. Household data currently lives on the device in Room.

## Current capabilities

- Family display with calendar, chores, grocery list, mood check-ins, weather, and an emergency entry point.
- Household setup and members.
- Parent PIN gate with a salted hash for new households.
- Android Home intent: the tablet can select FamSync as its default Home app.
- Local data persists across app restarts.

Some visible modules are still prototypes. Meal plans, notes, goals, routines, smart home, backup and export need real persistence and complete flows. Do not rely on them for household records yet.

## Build

Open the repository in Android Studio and run `app`. For command-line validation run `./gradlew :app:assembleDebug :app:testDebugUnitTest`. The project uses Android SDK 36 and JDK 17. A `.env` with `GEMINI_API_KEY` may be needed for AI features; keep it out of Git.

## Set as the tablet Home screen

1. Install the APK, open FamSync, and complete household setup with a unique parent PIN.
2. In Android Settings, choose **Apps → Default apps → Home app → FamSync**. The exact labels vary by manufacturer.
3. Press Home and restart the tablet to verify FamSync appears automatically.
4. Keep the tablet's Settings app available to the parent during initial setup. Use the Android default-app settings to switch back to the stock launcher for maintenance.

The Home intent alone does **not** enforce kiosk mode or prevent children leaving through Settings, notifications, or other system UI. True dedicated-device lock task mode requires device-owner provisioning and a policy controller, which is a separate device setup phase. Test maintenance exit before enabling any lock-down policy.

## Data and migration

Room version 9 no longer permits silent destructive migration. If an older schema needs upgrading, provide a tested migration before installing a new build on a tablet with household data. Back up the device before upgrading. Existing installations with the legacy plain-text PIN can enter Parent Center with their current PIN and change it to a new hashed PIN.

## Delivery priorities

1. Complete daily core flows: correct event dates, recurring chores, approval before reward points, and reliable parent management.
2. Persist meal plans, notes, goals, routines, emergency contacts, and reward catalogue; remove inert controls.
3. Add private check-in visibility, offline emergency contacts, export/restore, and screen schedule.
4. Provision and certify the chosen Android tablet as a dedicated device with a tested parent maintenance exit.
