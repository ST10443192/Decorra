# Decorra

**Excellence in every event.**

[![Android CI](https://github.com/ST10443192/REPOSITORY/actions/workflows/android.yml/badge.svg)](https://github.com/ST10443192/REPOSITORY/actions/workflows/android.yml)

Decorra is an Android app for an event decor business. Clients browse themed events, build and
submit quotes, approve them and chat with their decorator. Decorators manage themes, items,
stock, quotes, events and messages. The app was built by Revive Technologies for Bee's Events and
Decor as part of the module XISD6329.

## Contents

- [Features](#features)
- [Technology](#technology)
- [How the data is stored](#how-the-data-is-stored)
- [Getting started](#getting-started)
- [Firebase setup](#firebase-setup)
- [Running the tests](#running-the-tests)
- [Continuous integration and DevOps](#continuous-integration-and-devops)
- [Building a release](#building-a-release)
- [Project structure](#project-structure)
- [Business rules](#business-rules)
- [Security and privacy](#security-and-privacy)
- [Troubleshooting](#troubleshooting)
- [Current limitations and roadmap](#current-limitations-and-roadmap)
- [Team](#team)

## Features

### Clients

- Register and sign in with email and password, or with Google (single sign-on).
- Browse the portfolio of themes. Each theme shows a photo, a description, tags and the items it
  comes with.
- **Customize a theme.** The quote starts from the theme's own items and quantities. Clients can
  change any quantity, and add more items.
- **Rent items on their own** without choosing a theme.
- Pick an event date, number of guests, event type and venue.
- See a quote breakdown with VAT, then submit it or save it as a draft.
- Approve or decline a quote that the decorator has sent.
- Follow the quote status, the setup tasks, payment details and an event countdown.
- Chat live with the decorator, with push notifications when the app is closed.
- Keep a private inspiration board of photos, and share a quote as text.

### Decorators

- Dashboard with pending quotations, active bookings, approval rate, low stock alerts and the
  events of the next 14 days.
- Quote requests with filters (All, Pending, Approved, Rejected), a quote builder, status changes
  and private notes.
- Events calendar with setup tasks and progress.
- **Themes and items.** Add a theme with its photo, then list the items used in that event and
  how many of each. Existing items can be picked, or new ones created on the spot.
- Inventory with stock, minimum stock, unit cost, total value and restocking.
- Stock is checked per event date, so an item cannot be booked twice.
- Messages per client, alerts, business profile and bank details.
- Team management.

### Both roles

- Settings: edit profile, change password (for password accounts), notification preferences and
  language.
- English, Afrikaans and isiZulu, switched in Settings. No text is hard-coded in the screens.
- Handles invalid input, missing network and offline use without crashing.

## Technology

| Area | Choice |
|---|---|
| Language | Kotlin |
| Screens | Jetpack Compose with Material 3, plus XML layouts for splash, sign-in and registration |
| Minimum Android version | 7.0 (API 24) |
| Accounts and single sign-on | Firebase Authentication (email and password, Google through Credential Manager) |
| Live messaging | Cloud Firestore |
| Push notifications | Firebase Cloud Messaging, sent by a Cloud Function |
| Local data | JSON files in the app's private storage |
| Images | Picked from the gallery, resized and kept in the app's private storage |
| Tests | JUnit 4 unit tests |
| Build and delivery | Gradle, GitHub Actions, Azure DevOps |

Exact library versions are in `gradle/libs.versions.toml` and `app/build.gradle.kts`.

## How the data is stored

| Data | Where it lives |
|---|---|
| Accounts, passwords, Google sign-in | Firebase Authentication |
| User profiles and roles | Firestore, collection `users` |
| Chat conversations and messages | Firestore, collections `threads` and `threads/{id}/messages` |
| Push tokens | Firestore, field `fcmTokens` on each user |
| Decorator access code | Firestore, document `config/registration` (readable only from the Firebase console) |
| Themes, items, quotes, in-app alerts, business details, inspiration photos, preferences | JSON files on the phone |
| Photos | The phone's private app storage |

Passwords are never stored by the app. Firebase keeps a salted hash.

The next phase moves themes, items, quotes and photos to a PHP REST API with a MySQL database so
that every phone shares them. See [Current limitations and roadmap](#current-limitations-and-roadmap).

## Getting started

### Requirements

- Android Studio, recent enough for the Android Gradle Plugin version in `gradle/libs.versions.toml`
- Android SDK platform matching `compileSdk` in `app/build.gradle.kts`
- A Firebase project (free Spark plan is enough for everything except the push Cloud Function)
- An Android phone with USB debugging, or an emulator image that includes Google Play

### Steps

1. Clone the repository and open it in Android Studio.

   ```
  https://github.com/ST10443192/Decorra.git
   ```

2. Complete the [Firebase setup](#firebase-setup) and place `google-services.json` in the `app`
   folder. The file is not in the repository.
3. Let Gradle sync, then choose **Run**.
4. To test on a real phone, enable Developer options and USB debugging, connect it with a cable
   and choose it as the run target.

## Firebase setup

1. In the [Firebase console](https://console.firebase.google.com), create a project and add an
   **Android app**. The package name must equal `applicationId` in `app/build.gradle.kts`.
2. **Authentication:** enable the **Email/Password** and **Google** sign-in methods.
3. **SHA-1:** in Android Studio open Gradle, then `app`, Tasks, `android`, run `signingReport` and
   copy the debug SHA-1. Add it under Project settings, Your apps, Add fingerprint. Add your
   release key's SHA-1 too before publishing.
4. Download `google-services.json` after steps 2 and 3 and save it as `app/google-services.json`.
   The file must be downloaded after the Google provider and the SHA-1 exist, otherwise the build
   fails on `default_web_client_id`.
5. **Firestore:** create a database in production mode, in the region nearest you.
6. Paste the security rules below into the Rules tab and publish them.
7. **Decorator access code:** in Firestore create a collection `config` with a document
   `registration` and a string field `decoratorCode`. Use 16 or more characters, capital letters
   and digits only. Registering as a decorator requires this code.
   When all decorators have registered, delete the `config/registration` document to close
   decorator sign-up.
8. **Push notifications:** deploy the Cloud Function below. Cloud Functions need the Blaze
   (pay-as-you-go) plan.

<details>
<summary>Firestore security rules</summary>

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function signedIn() {
      return request.auth != null;
    }
    function isOwner(uid) {
      return signedIn() && request.auth.uid == uid;
    }
    function isDecorator() {
      return signedIn()
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'decorator';
    }
    function validThreadKeys() {
      return request.resource.data.keys().hasOnly(
        ['clientUid', 'clientName', 'clientEmail', 'lastText', 'lastFromClient', 'lastTime']);
    }

    // Only the Firebase console can read or change the access code.
    match /config/{document} {
      allow read, write: if false;
    }

    match /users/{uid} {
      allow read: if isOwner(uid) || isDecorator();

      allow create: if isOwner(uid)
        && request.resource.data.keys().hasOnly(
          ['firstName', 'lastName', 'email', 'phone', 'role', 'fcmTokens', 'createdAt', 'accessCode'])
        && (
          request.resource.data.role == 'client'
          || (request.resource.data.role == 'decorator'
              && request.resource.data.accessCode ==
                 get(/databases/$(database)/documents/config/registration).data.decoratorCode)
        );

      allow update: if isOwner(uid)
        && request.resource.data.diff(resource.data).affectedKeys()
            .hasOnly(['firstName', 'lastName', 'phone', 'fcmTokens', 'pushMessages']);

      allow delete: if false;
    }

    match /threads/{threadId} {
      allow read: if isOwner(threadId) || isDecorator();

      allow create, update: if validThreadKeys()
        && ((isOwner(threadId) && request.resource.data.clientUid == threadId) || isDecorator());

      match /messages/{messageId} {
        allow read: if isOwner(threadId) || isDecorator();

        allow create: if request.resource.data.senderUid == request.auth.uid
          && request.resource.data.text is string
          && request.resource.data.text.size() > 0
          && request.resource.data.text.size() <= 2000
          && ((isOwner(threadId) && request.resource.data.fromClient == true)
              || (isDecorator() && request.resource.data.fromClient == false));

        allow update, delete: if false;
      }
    }
  }
}
```

</details>

<details>
<summary>Cloud Function that sends chat push notifications</summary>

Create it with `firebase init functions` (JavaScript), replace `functions/index.js` with the code
below, set `REGION` to your Firestore region, then run `firebase deploy --only functions`.

```js
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

// Use the same region as your Firestore database.
const REGION = "europe-west1";

exports.notifyOnChatMessage = onDocumentCreated(
  { document: "threads/{threadId}/messages/{messageId}", region: REGION },
  async (event) => {
    const message = event.data && event.data.data();
    if (!message) return;

    const db = getFirestore();
    const threadId = event.params.threadId;

    // Who wrote it, for the notification title.
    let title = "Decorra";
    if (message.senderUid) {
      const sender = await db.collection("users").doc(message.senderUid).get();
      if (sender.exists) {
        const name = `${sender.data().firstName || ""} ${sender.data().lastName || ""}`.trim();
        if (name) title = name;
      }
    }

    // A client writes to the decorators; a decorator writes to that client.
    const tokens = new Set();
    if (message.fromClient) {
      const decorators = await db.collection("users").where("role", "==", "decorator").get();
      decorators.forEach((doc) => {
        if (doc.data().pushMessages === false) return;
        (doc.data().fcmTokens || []).forEach((t) => tokens.add(t));
      });
    } else {
      const client = await db.collection("users").doc(threadId).get();
      if (client.exists && client.data().pushMessages !== false) {
        (client.data().fcmTokens || []).forEach((t) => tokens.add(t));
      }
    }
    if (tokens.size === 0) return;

    const list = [...tokens];
    const response = await getMessaging().sendEachForMulticast({
      tokens: list,
      data: {
        type: "chat",
        threadId,
        title,
        body: String(message.text || "").slice(0, 200),
      },
      android: { priority: "high" },
    });

    // Forget tokens that are no longer valid (uninstalled app, signed-out phone).
    const dead = [];
    response.responses.forEach((r, i) => {
      const code = r.error && r.error.code;
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        dead.push(list[i]);
      }
    });
    for (const token of dead) {
      const owners = await db.collection("users").where("fcmTokens", "array-contains", token).get();
      for (const doc of owners.docs) {
        await doc.ref.update({ fcmTokens: FieldValue.arrayRemove(token) });
      }
    }
  }
);
```

</details>

## Running the tests

The unit tests cover the business rules that must never be wrong: pricing and VAT, deposits,
stock reservation per date, stock levels, theme item lists, notification preferences, name
handling for Google accounts and saving and loading of data.

```
./gradlew testDebugUnitTest
```

On Windows use `gradlew.bat testDebugUnitTest`. The HTML report is written to
`app/build/reports/tests/`. In Android Studio you can also right-click the `test` folder and
choose **Run Tests**.

## Continuous integration and DevOps

The project follows the DevOps life cycle:

| Stage | Tool |
|---|---|
| Plan | Azure Boards: epics, user stories and tasks with owners and sprints |
| Code | GitHub, linked to Azure Boards by work item numbers in commit messages |
| Build and test | GitHub Actions runs the unit tests and builds the app on every push |
| Release | A signed release bundle, versioned in `app/build.gradle.kts` |
| Operate | Firebase console for authentication, database and push |

### GitHub Actions

The workflow in `.github/workflows/android.yml` sets up Java, runs `testDebugUnitTest`, builds the
debug APK, and uploads the test reports and the APK as artifacts.

The build needs `google-services.json`, which is not committed. Add it as a repository secret:

1. Encode the file. In PowerShell:
   `[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\google-services.json"))`
2. In GitHub open Settings, Secrets and variables, Actions and create a secret named
   `GOOGLE_SERVICES_JSON` with that value.
3. The workflow writes the file back with this step:

   ```yaml
   - name: Add Firebase config
     env:
       GOOGLE_SERVICES_JSON: ${{ secrets.GOOGLE_SERVICES_JSON }}
     run: echo "$GOOGLE_SERVICES_JSON" | base64 --decode > app/google-services.json
   ```

Update the secret whenever you download a new `google-services.json`.

### Azure DevOps

Work is planned in Azure Boards. Reference the work item number in commit messages so each commit
links to its task:

```
AB#14 Add change password screen
Fixes AB#15
```

## Building a release

1. Increase `versionCode` and update `versionName` in `app/build.gradle.kts`.
2. Choose Build, Generate Signed App Bundle / APK. Create a key store outside the repository and
   back it up with its passwords in two safe places. Losing the key means updates can no longer be
   published under the same listing.
3. Add the release key's SHA-1 to the Firebase project so Google sign-in works in the release build.
4. Install the release APK on a real phone and repeat the main flows before publishing, because
   code shrinking is the one setting that behaves differently from a debug build.

## Project structure

```
app/src/main/java/com/example/decorra
  DecorraApp.kt              application start-up and notification channel
  MainActivity.kt            hosts the Compose screens after sign-in
  data/                      models, repositories, storage, pricing and stock rules
  navigation/                routes and the client and decorator navigation graphs
  push/                      push token handling and the messaging service
  ui/auth/                   splash, sign-in and registration screens
  ui/client/                 client screens and the settings pages
  ui/admin/                  decorator screens
  ui/components/             shared building blocks
  ui/theme/                  colours and theme
  util/                      language, validation, formatting and Google sign-in helpers
app/src/main/res
  layout/                    XML layouts for splash, sign-in, registration and dialogs
  values/                    English text, colours and styles
  values-af/, values-zu/     Afrikaans and isiZulu text
app/src/test/                unit tests
```

## Business rules

- Money is stored in cents, which avoids rounding errors.
- VAT is 15% of the subtotal. The deposit is 40% of the total.
- A quote moves through: Draft, Pending approval, Approved or Rejected, Deposit paid, In
  progress, Complete.
- Quotes that are **Approved**, **Deposit paid** or **In progress** reserve stock on their event
  date. A quote cannot be submitted or approved if it would take more than is free on that date.
- Items without a stock figure are not tracked and are never limited.
- A quote keeps the prices it was made with, so changing an item's price later does not change
  existing quotes.
- Stock is Healthy, Low (at or below the minimum) or Critical (sold out or at half the minimum).
- Deleting an item also removes it from every theme's item list.

## Security and privacy

- `google-services.json`, signing keys and passwords are not stored in the repository.
- The Firestore rules, not the app screens, decide who can read and write data. Clients can only
  reach their own profile and conversation. Roles cannot be changed after an account is created.
- Registering as a decorator needs an access code that only the project owner can see.
- Chat messages and profile details (name, email, phone) are stored on Google's servers through
  Firebase. A privacy policy is needed before the app is published, together with a way to delete
  a person's data on request, in line with POPIA.

## Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| Build error on `default_web_client_id` | `google-services.json` was downloaded before the Google sign-in provider and the SHA-1 existed. Add them, then download the file again. |
| "No matching client found for package name" | The Firebase app's package name differs from `applicationId`. |
| Gradle says a library requires a newer `compileSdk` or Android Gradle Plugin | Use the older library version listed in the build notes, or update Android Studio and the Gradle wrapper together. |
| Google sign-in says it is not available | The SHA-1 of the key used to build the app is missing in Firebase, or the phone has no Google account. |
| Permission denied when registering as a decorator | The access code is wrong, or the `config/registration` document does not exist. |
| No push notifications | The Cloud Function is not deployed, the phone denied the notification permission, or Message notifications is switched off in Settings. |
| GitHub Actions build fails | The `GOOGLE_SERVICES_JSON` secret is missing or out of date. |

## Current limitations and roadmap

- **Themes, items, quotes and photos are stored on each phone.** A decorator on one phone does not
  yet see a quote made on another. Accounts, roles and chat are already shared through Firebase.
  Moving the rest to a PHP REST API with a MySQL database is the next phase.
- In-app alerts such as a new request or an approved quote come from the local store. Only chat
  messages trigger push notifications until quotes are shared.
- Deposits are recorded by the decorator. There is no payment gateway.
- A help screen and account deletion are planned.
- The isiZulu translation is a draft and needs review by a native speaker.

## Team

Revive Technologies

- Fortunate Lukhele
- Palesa Mhlanga
- Letlhogonolo Chabalala

Module XISD6329, The Independent Institute of Education.

