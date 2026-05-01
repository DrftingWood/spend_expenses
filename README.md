# Spend Tracker

An Android app that reads transaction SMS, auto-categorizes spend, prompts you for a category when it can't figure it out, and shows analytics.

## Features
- Imports last 90 days of SMS on first launch (with permission) and listens for new SMS via a `BroadcastReceiver`.
- Parses INR transaction SMS (Rs / INR / ₹) for both **debits and credits/refunds**.
- Auto-categorizes via keyword rules (Swiggy → Food, Uber → Transport, …) and remembers per-merchant choices you make.
- Home screen surfaces uncategorized transactions in a "needs attention" banner; tap to assign a category from a bottom sheet.
- Analytics screen: net total / debit-only toggle, by-category bars, by-month bars, top 5 merchants. Range filter: This month / Last 3 months / All.

## Tech
Kotlin · Jetpack Compose · Material 3 · Room · Coroutines/Flow · MVVM. Min SDK 26, Target SDK 34.

## Build
```
./gradlew :app:assembleDebug
./gradlew :app:test
```

Open in Android Studio (Hedgehog or newer). On first run grant SMS + (Android 13+) notifications permissions when prompted.

## Project layout
```
app/src/main/java/com/spendexpenses/app/
├── data/         Room entities, DAOs, repository
├── sms/          Parser, importer, broadcast receiver
├── categorize/   Category enum + rule-based Categorizer
└── ui/           Compose screens (home, analytics) + nav
```

## Privacy
SMS contents never leave the device. All data is stored in a local Room database (`spend.db`).

## Roadmap
- Budgets and alerts
- CSV export
- ML-based merchant classification
- Multi-currency support
