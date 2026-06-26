# Shingar Sallon Manager

A professional Android salon management app built with Kotlin and Jetpack Compose. Designed for beauty salon owners to manage appointments, customers, services, staff, offers, payments, and more.

## Features

- **Dashboard** — Overview of today's appointments, earnings, customers, and quick actions
- **Appointment Management** — Book, edit, delete appointments with status tracking
- **Customer Management** — Customer profiles with visit history, spending, and birthday reminders
- **Services** — Manage salon services with pricing, duration, and categories (Bridal Makeup, Hair Styling, Facial, Nails, etc.)
- **Staff Management** — Track staff performance, ratings, and appointment assignments
- **Offers** — Create and share promotional offers via WhatsApp
- **Payments** — Track paid/unpaid payments with daily and monthly reports
- **Reports** — Revenue analytics, popular services, customer growth insights
- **AI Business Assistant** — Mock AI assistant for poster ideas, offer suggestions, Instagram captions
- **WhatsApp Integration** — Send appointment confirmations, reminders, and share offers
- **Settings** — Configure salon details, business hours, and app preferences

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Clean Architecture (data / domain / presentation)
- **Navigation:** Jetpack Navigation Compose
- **Backend Ready:** Firebase Auth, Firestore, Storage (commented out, easily enabled)
- **State Management:** StateFlow + Compose collectAsState

## Brand Style

- Premium beauty salon aesthetic
- Color palette: Pink, Dark Blue, White, Soft Purple
- Rounded corners, gradient cards, smooth animations
- Professional business app feel

## Project Structure

```
app/src/main/java/com/shingar/salon/
├── data/
│   ├── model/         — Data classes (Appointment, Customer, Service, etc.)
│   └── repository/    — SalonRepository with sample data
├── navigation/        — NavGraph and routes
├── presentation/
│   ├── components/    — Reusable UI components
│   └── screens/       — All app screens
│       ├── splash/
│       ├── login/
│       ├── dashboard/
│       ├── appointments/
│       ├── customers/
│       ├── services/
│       ├── staff/
│       ├── offers/
│       ├── payments/
│       ├── reports/
│       ├── ai/
│       └── settings/
├── ui/theme/          — Colors, Theme
├── utils/             — WhatsApp utils, Extensions
└── MainActivity.kt
```

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   ```

2. **Open in Android Studio**
   - Use Android Studio Hedgehog (2023.1.1) or later
   - SDK: compileSdk 34, minSdk 26

3. **Build and Run**
   - The app works immediately in demo mode with sample data
   - No Firebase configuration required for demo

4. **Enable Firebase (Optional)**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Download `google-services.json` and place in `app/` directory
   - Uncomment Firebase dependencies in `app/build.gradle.kts`
   - Uncomment the Google Services plugin in the root `build.gradle.kts`

## Screens

| Screen | Description |
|--------|-------------|
| Splash | Animated brand splash screen |
| Login | Owner/Staff login with demo option |
| Dashboard | Business overview with stats and quick actions |
| Appointments | List, filter, search, add/edit/delete appointments |
| Customers | Customer list with profiles and spending history |
| Services | Salon services with categories and pricing |
| Staff | Staff management with performance metrics |
| Offers | Create and share promotional offers |
| Payments | Payment tracking with daily/monthly reports |
| Reports | Revenue analytics and business insights |
| AI Assistant | AI-powered business suggestions |
| Settings | Salon configuration and app info |

## Demo Mode

The app comes with pre-loaded sample data including:
- 6 appointments (various statuses)
- 7 customers with visit history
- 10 salon services across categories
- 4 staff members with ratings
- 4 active promotional offers
- 9 payment records

## Requirements

- Android Studio Hedgehog 2023.1.1+
- Kotlin 1.9.22+
- Android SDK 34
- Minimum Android 8.0 (API 26)

## License

This project is for educational and business use.
