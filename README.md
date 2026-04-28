# AMRO Movie Explorer

## Overview

**AMRO Movie Explorer** is an Android MVP built for the Advanced Movie Recommendation Organisation assignment.

The app integrates with [TMDB (The Movie Database)](https://www.themoviedb.org/) as its initial data source and allows users to:

- Browse this week’s trending movies
- Filter movies by genre
- Sort results by popularity, title, or release date
- View detailed information for each movie

The project was intentionally designed as a **scalable foundation** for future teams, rather than just a one-off MVP.

---

## Design Philosophy

The focus of this project is **not only feature delivery**, but building a **clean, testable, and scalable architecture** that can evolve with future requirements.

---

## Features

### Core

| Area | Capability |
|------|------------|
| **Trending feed** | Display **top 100 trending movies** (weekly TMDB trending) |
| **Filtering** | Genre-based filtering |
| **Sorting** | Popularity, title, release date (**ascending / descending**) |

### Movie detail screen

- Title, tagline, overview  
- Poster and backdrop images  
- Genres  
- Ratings and votes  
- Budget and revenue  
- Runtime and release date  
- **IMDb** deep link  

### Full UI state handling

| State | Notes |
|-------|--------|
| Loading | Initial & refreshing |
| Content | Successful data |
| Empty | No items / empty outcome |
| Error | Retryable & non-retryable cases |

---

## Tech Stack

| Category | Choice |
|----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture + Feature-based modularization |
| Navigation | Navigation Compose |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp |
| Serialization | kotlinx.serialization |
| Image Loading | Coil |
| Async | Kotlin Coroutines + StateFlow |
| Unit testing | JUnit 4 · kotlinx-coroutines-test · MockK |
| Integration testing | MockWebServer (OkHttp) |
| Instrumented UI testing | AndroidX Test (JUnit) · Espresso · Compose UI Test |

---

## Project Structure

```
:app                   → App entry point, navigation
:core                  → Shared UI components, utilities, mappers
:domain                → Business logic, models, use cases
:data                  → API, DTOs, repository implementation
:feature:movie-list    → Movie list UI + ViewModel
:feature:movie-detail  → Movie detail UI + ViewModel
```

### Dependency direction

```
UI → ViewModel → Use Cases → Repository (interface) → Data layer
```

**Rules:**

- **`domain`** is pure Kotlin (no Android dependencies)
- **`data`** implements repository contracts
- **Feature modules** depend only on **`domain`** and **`core`**

---

## Architecture Decisions

### 1. Clean Architecture

Business logic (filtering, sorting, validation) lives in **domain use cases**.

**Benefits:**

- Reusability  
- Testability  
- Independence from UI and frameworks  

### 2. Unidirectional Data Flow

```
UI → Action → ViewModel → State → UI
```

- ViewModels expose **`StateFlow`**
- UI is **stateless** and reacts to state
- **No business logic inside composables**

### 3. UI State Modeling

Explicit UI states per screen reduce bugs and flicker.

**Movie list**

| State | Role |
|-------|------|
| Loading | Initial / refreshing |
| Content | Data ready |
| Empty | Nothing to show |
| Error | User feedback + optional retry |

**Movie detail**

| State | Role |
|-------|------|
| Loading | Optional “previous data” while reloading |
| Content | Detail shown |
| Error | Failure with mapped message |

### 4. Separation of Mapping Logic

Dedicated mappers keep ViewModels thin:

- `MovieListUiMapper`  
- `MovieDetailUiMapper`  

Mapping stays **testable**, and **UI models stay decoupled** from domain types.

### 5. Data Layer Design

- **Retrofit + OkHttp** for networking  
- Centralized **`apiCall`** wrapper for consistent error mapping  
- Errors become **`DomainError`** before they reach the UI  
- Repository responsibilities include:  
  - Pagination (up to **100** trending movies)  
  - Deduplication  
  - Genre resolution  
  - Image URL construction  

### 6. Error Handling

Errors flow across layers:

```
Network / HTTP → DomainError → UI (UiText)
```

**Handled cases include:**

- Network failure  
- Unauthorized (**401**)  
- Rate limiting (**429**)  
- Server errors (**5xx**)  
- Serialization issues  
- Configuration issues (missing API key)  

### 7. Image Handling

TMDB returns **relative** image paths. **`TmdbImageUrlBuilder`** builds full URLs with size strategies:

- List → smaller images (performance)  
- Detail → larger images (quality)  

### 8. External Links

External URLs (e.g. IMDb) are centralized, e.g.:

```kotlin
ExternalUrls.imdbTitleUrl(id)
```

This avoids duplication and keeps links consistent.

### 9. Feature Modularization

Screens and their presentation logic are organized into dedicated Gradle feature modules (e.g. `:feature:movie-list`, `:feature:movie-detail`) instead of a single monolithic app module.

This approach was chosen to support scalability and team ownership:

- **Clear boundaries** — each feature owns its UI, ViewModel, and tests  
- **Controlled dependencies** — features depend only on `domain` and `core`, not on each other  
- **Easier onboarding** — developers can work within a single feature without navigating the entire codebase  
- **Scalability** — new features can be added as separate modules without impacting existing ones  
- **Focused builds & testing** — modules can be compiled and tested independently  

The `:app` module remains a thin shell responsible for navigation and dependency injection.

---

## Testing Strategy

The project includes **unit**, **integration**, and **UI** tests.

### Unit tests (JVM)

| Layer | Examples |
|-------|----------|
| **Domain** | Use cases (filtering, sorting, validation), `DomainResult` helpers |
| **Data** | API error mapping, repository (pagination, caching, dedupe), DTO → domain |
| **Presentation** | ViewModel transitions, UI mappers |

Most business logic is covered by unit tests for **correctness** and **maintainability**.

### Integration tests

- **Location:** `data` module  
- **Tooling:** [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)  

**Covers:**

- Retrofit + OkHttp + repository wiring  
- Authentication headers  
- Pagination and caching behavior  
- Error handling (401, 404, etc.)  

### UI tests (Compose)

- Loading, content, error, and empty **rendering**  
- User flows: filter, sort, retry, navigation  
- **`testTag`** for stable selectors  

---

## Configuration

The app requires a **TMDB API token**.

### `gradle.properties`

```properties
TMDB_BEARER_TOKEN=your_token_here
```

### Optional

```properties
TMDB_BASE_URL=https://api.themoviedb.org/3/
TMDB_IMAGE_BASE_URL=https://image.tmdb.org/t/p/
```

The build **fails if the token is missing** so misconfigured builds are caught early.

---

## Build & Run

### Requirements

- Android Studio  
- **JDK 11+**  
- Android SDK (**compile SDK 36**)  

### Commands

```bash
./gradlew assembleDebug
./gradlew installDebug
```

On Windows (PowerShell):

```powershell
.\gradlew assembleDebug
.\gradlew installDebug
```

---

## Accessibility Considerations

Basic accessibility practices have been applied:

- **Content descriptions** for images (e.g. list posters, detail header banner) and **click labels** on custom tappable areas (hero card, list rows) so screen readers announce the action (e.g. “Open details for …”)  
- **Loading indicators** (`CircularProgressIndicator`, inline `LinearProgressIndicator` on refresh) expose a **loading** description  
- **Material 3** components help ensure proper contrast and touch target sizes  
- **Compose semantics** merge visible **text** on buttons, chips, and outlined actions with platform defaults; **`testTag`** is used for tests alongside these semantics  

Further improvements could include enhanced semantic grouping and full TalkBack optimization.

---

## Trade-offs

To keep the MVP **focused** and **maintainable**, some areas are intentionally simplified:

| Topic | Decision |
|-------|----------|
| **Pagination** | Fetched in the **repository** (capped at 100 movies); no Paging 3 in the UI yet |
| **Persistence** | No local cache (e.g. Room) yet |
| **TMDB config API** | **`/configuration`** not used; image base URL via build config |
| **Accessibility** | Implemented with content descriptions, **click labels**, loading semantics, and Material defaults—not exhaustive TalkBack/opt-in grouping yet; see [Accessibility Considerations](#accessibility-considerations) |
| **UI tests** | Emphasis on **behavior**, not full nav graphs or screenshot baselines |

These choices keep the codebase lean while leaving **clear extension points**.

---

## Future improvements

- Local caching (**Room**)  
- Proper UI paging (**Paging 3**)  
- Use TMDB **`/configuration`** for image bases  
- Analytics / logging  
- More feature modules (cast, profiles, etc.)  

---

## Final notes

This project emphasizes:

- **Clean architecture**  
- **Strong separation of concerns**  
- **Testability**  
- **Scalability** for future teams  

The goal is not only to ship features, but to provide a **maintainable, extensible foundation** for continued development.
