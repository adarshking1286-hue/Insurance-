<p align="center">
  <img src="docs/logo.png" alt="Insurance Bazaar" width="360">
</p>

<h1 align="center">Insurance Bazaar</h1>

<p align="center">
  An offline Android app for managing insurance customers, policies and renewals.<br>
  Built with Kotlin, Jetpack Compose and Room.
</p>

## Features

- **Account:** welcome screen, local sign-up and login (salted and hashed password, stored on the device only)
- **Customers:** add, edit, delete and search by name or phone
- **Policies:** add, edit, delete and assign to a customer. Vehicle number, insurer, type, start and expiry dates, premium. Search, Active/Expired filter and status colours
- **Dashboard:** total customers and policies, active, expired, expiring in 30 days, this month's premium, active premium, premium by month
- **Profile:** change display name, log out
- **Storage:** Room (SQLite) database `insurance_bazaar.db`, no backend and no internet needed at runtime

Deleting a customer also deletes their policies (foreign key cascade).

## Database

The app uses a Room (SQLite) database named `insurance_bazaar.db`. Room creates the tables the first time the app starts, so there is nothing to install or configure.

| Table | Purpose |
| --- | --- |
| `customers` | name, phone, email, address |
| `policies` | customer (foreign key, cascade delete), vehicle number, insurer, type, start and expiry dates, premium |

- `database/schema.sql` is a reference copy of the schema.
- `database/sample_data.sql` inserts demo records with dates relative to today. Use it in DB Browser for SQLite or the Android Studio Database Inspector.
- In the app, **Profile -> Data** has **Load sample data** (available while the database is empty) and **Delete all customers and policies**.
- Dates are stored as UTC-midnight epoch milliseconds.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Database | Room 2.6 (KSP) |
| Min / target SDK | 24 / 34 |

## Getting started

1. Install [Android Studio](https://developer.android.com/studio) (Koala or newer) with JDK 17.
2. Clone the repository and open the folder in Android Studio.
3. Wait for Gradle sync, then run the `app` configuration on an emulator or device.

Command line (requires Gradle 8.9+):

```bash
gradle assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

## Project structure

```
app/src/main/java/com/insurancebazaar/app/
├── MainActivity.kt        # navigation and bottom bar
├── data/                  # Room entities, DAOs, database, local session
└── ui/                    # ViewModel and Compose screens
database/schema.sql        # reference copy of the database schema
docs/logo.png              # project logo
```

## Continuous integration

`.github/workflows/android.yml` builds a debug APK on every push and pull request and uploads it as a workflow artifact.
