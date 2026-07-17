# ShareStack 🚀
### Pool Together. Invest Smarter.

ShareStack is a modern, security-first Fintech application built natively for Android using Kotlin and Jetpack Compose. It enables retail investor groups, project partners, and investment clubs to seamlessly form co-investment pools ("Stacks"), vote on stock acquisition pitches, dynamically split funding targets, and maintain an unalterable, transparent ledger of all group financial transactions.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel) with unidirectional data flow
- **Asynchronous Engine:** Kotlin Coroutines & Asynchronous StateFlow observation
- **Networking:** Retrofit & Ktor Client (Multi-tiered HTTP architecture)
- **Market Data API:** Finnhub Stock REST API (Live tracking for NVDA, TSLA, AAPL, AMZN, GOOGL)
- **Local Storage:** SQLite (`SQLiteOpenHelper`) for offline-first state resilience
- **Cloud Backend:** Supabase (Remote database persistence layer for user/group multi-tenancy)
- **Security:** One-way Cryptographic SHA-256 Hashing Engine

---

## ✨ Key Features

### 1. Multi-Tenant Cryptographic Isolation
Each user environment is fully siloed. Upon registration, users start with a clean slate (zero stacks). The dashboard leverages state combining flows to selectively filter and display only the specific investment pools where the currently authenticated session user is an explicit, registered legal member.

### 2. Dynamic Equity Dilution Engine
Moving away from rigid, static group shares, ShareStack implements an advanced financial dilution algorithm. If a member under-contributes or opts out of a funding target, remaining members can manually step up to cover the shortfall. The app recalculates each member's total ownership stake based on the real-time mathematical weight of their cumulative capital contributions relative to the changing valuation of the entire pool.

### 3. Transparent Shared Ledger
A dedicated transaction history tab renders a chronological, immutable audit trail of the group's investment history. Every time an investment proposal successfully clears, a detailed digital transaction receipt is generated, tracking the overall asset acquisition cost alongside the exact fraction of Shillings contributed by every participant.

### 4. Enterprise-Grade Front-End Validation
- **Authentication Safeguards:** Strict regex pattern filtering on email fields, minimum 6-character constraints on passwords, and real-time confirmation equality verification.
- **Dynamic Asset Droplists:** Material 3 `ExposedDropdownMenuBox` forms protect asset fields, restricting entry exclusively to API-tracked global stock tickers to prevent invalid database injection.
- **Ledger Balancing Barriers:** The transaction popup strictly blocks execution unless the ledger balances perfectly to the exact cent, preventing unauthorized overfunding or capital gaps.

---

## 📁 Core Directory Structural Breakdown

```text
com.sharestack
│
├── data
│   ├── local
│   │   └── DatabaseHelper.kt        # Local SQLite state mirroring
│   ├── remote
│   │   ├── NetworkModule.kt         # Retrofit REST orchestration
│   │   └── SupabaseService.kt       # Remote persistence cloud driver
│   ├── MockPriceService.kt          # Secondary fallback ticker generator
│   └── ShareStackRepository.kt      # Single source of truth data repository
│
├── models
│   └── SharedModels.kt              # Clean, serializable data blueprints
│
├── viewmodel
│   └── ShareStackViewModel.kt       # State flow emission & business math logic
│
└── ui
    ├── screens
    │   ├── LoginScreen.kt           # Regex protected authentication
    │   ├── RegisterScreen.kt        # Password validation onboarding
    │   ├── HomeDashboardScreen.kt   # Selective stack viewport & macro portfolio total
    │   ├── GroupDetailScreen.kt     # Tabbed view separating active pitches and ledger cards
    │   ├── CreateGroupScreen.kt     # Multi-member composition manager (Up to 6)
    │   ├── CreateProposalScreen.kt  # Restrictive dropdown stock pitch sheet
    │   └── StackDetailScreen.kt     # Interactive contribution splitting panel
    └── theme
        ├── Color.kt                 # Fintech palette configurations
        └── Theme.kt                 # Material 3 color scheme bindings