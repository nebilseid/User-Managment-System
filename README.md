# Sliide User Management — KMP Technical Challenge

A cross-platform User Management app built with Kotlin Multiplatform and Compose Multiplatform,
targeting Android and iOS from a single shared codebase.

> **Branch:** `dummyjson-integration` — backend is [DummyJSON](https://dummyjson.com), a public
> read-only fake API that requires no setup. See `main` for the Supabase backend with full
> server-side persistence and a clean data layer without the workarounds described below.

---

## Building and Running

**Android**
```shell
./gradlew :composeApp:assembleDebug
```
Or run the `composeApp` configuration directly from Android Studio.

**iOS**
Open `/iosApp` in Xcode and run, or use the iOS run configuration in Fleet/Android Studio.

**Tests**
```shell
./gradlew :composeApp:allTests
```

> **Note:** The app uses [DummyJSON](https://dummyjson.com) as the backend (`GET/POST/DELETE /users`).
> It requires no API key and needs no local setup — clone and run.

---

## Architecture

The project follows Clean Architecture with three clearly separated layers:

```
domain/          — Pure Kotlin. Models, repository interface, use cases. No Android, no Ktor.
data/            — Repository implementation, Ktor API client, SQLDelight local data source.
presentation/    — ViewModel, UiState, UserError. Bridges domain and UI.
ui/              — Compose screens, components, dialogs. No business logic.
```

**Why this layering?**
The domain layer has zero platform dependencies — it's trivially testable and could be reused
in a future backend (Ktor server) or desktop target without touching business logic. The
data layer owns all I/O concerns. The presentation layer owns state — the UI is a pure function
of `UsersUiState` and never talks to the domain directly.

**ViewModel (MVVM + StateFlow)**
I chose MVVM over MVI because the state mutations here are simple enough that a full intent/reducer
cycle would add boilerplate without benefit. `UsersUiState` is an immutable data class; every
state change goes through `_uiState.update { }`, which gives the same unidirectional guarantees
without the ceremony.

**Use Cases**
The use cases are currently thin delegation wrappers. This was an intentional decision — they're
the right place to enforce business rules (e.g. validating that a deleted user isn't currently
being viewed in master-detail, composing multiple repository calls, adding retry logic). The
scaffolding is in place; the value comes as the product grows.

---

## Key Technical Decisions

**Infinite scroll pagination**
`GET /users?limit=30&skip=N` — the app loads the first page on launch and appends subsequent
pages as the user scrolls within 5 items of the list end. Pull-to-refresh resets to page 0.
The `hasMore` flag is derived from the returned page size (`users.size < PAGE_SIZE` → no more
pages). Initial load uses shimmer placeholders (see below); subsequent page loads show a
`CircularProgressIndicator` at the list footer — shimmer mid-scroll would be disruptive and
imply the whole list is reloading, which it isn't.

**Optimistic delete with timed commit**
Rather than waiting for the API to confirm deletion before updating the UI, I remove the item
immediately and give the user 4 seconds to undo. The actual `DELETE` call fires after the timeout.
If the API fails, the user is restored to their original list position. This pattern feels
native-quality — it's what iOS Mail and Gmail do — and it was worth the added state complexity
(`deletionJob`, `PendingDeletion(user, index)`).

**Offline-first fallback**
`UserRepositoryImpl` uses `recoverCatching` — if the network call fails and there's cached data
in SQLDelight, it returns that silently. The user sees their last-known list rather than an error
screen. The error screen only appears if the cache is also empty.

**Refresh failure snackbar**
Pull-to-refresh uses a `forceRefresh` flag that bypasses the offline cache fallback, so a network
failure during a manual refresh surfaces as a `NetworkError` rather than silently returning stale
data. A custom `ErrorSnackbar` is shown over the existing list — the user keeps their data visible
while being informed the refresh failed.

**Shimmer over spinner**
The `LoadingContent` renders 8 `ShimmerUserCard` placeholders rather than a `CircularProgressIndicator`.
This reduces perceived load time and avoids layout shift when content arrives — the card
dimensions are identical to real `UserCard` items.

**Progressive form validation**
The Add User dialog uses three validation modes together:
- Text fields: validate on blur (after the field loses focus), not on every keystroke
- Gender / Status chips: no error text — instead, a primary-coloured animated border appears on the
  chip group that needs attention once name and email are valid, guiding the user step-by-step
- Submit button: disabled until all four fields are complete, so the user never taps into a dead end

**Adaptive layout**
The app detects container width at runtime (`LocalWindowInfo`). Below 600dp it renders a single
`LazyColumn`. Above 600dp (tablets, landscape) it switches to a master-detail layout: a two-column
`LazyVerticalGrid` on the left and a detail panel on the right. This is all shared Compose code —
no platform-specific layout files.

---

## Where I Directed the AI and How I Curated Its Output

I used AI for implementation velocity. The architectural and UX decisions — and the code review
passes that caught several AI mistakes — were mine. A few concrete examples of where I pushed
back or redirected:

**Kept thin:** The AI initially suggested making use cases interfaces so they could be mocked in
tests. I rejected this — it's over-engineering for delegation wrappers. I used a `FakeUserRepository`
at the repository boundary instead, which is the correct seam.

**Rejected:** The AI generated an `AnimatedVisibility(visible = true)` wrapper around every list
item — visually dead code (always visible). I caught it in a code review pass and removed it,
replacing it with `Modifier.animateItem()` on keyed `LazyColumn` items, which correctly animates
both insertion (new user at top) and removal (delete).

**Caught and fixed:** `name.take(2).uppercase()` for avatar initials — this produces "AL" for
"Alice Johnson" instead of "AJ". The AI didn't flag it. I did, and replaced it with a per-word
first-letter extraction.

**Architectural coupling:** The AI coupled `ErrorBlock` directly to the `UserError` sealed class
from the presentation layer. A UI component has no business importing a presentation model. I
refactored it to accept `title: String` and `message: String`, with the mapping pushed up to the
call site in `UsersScreen` where the presentation layer is already in scope.

**UX disagreement:** The AI's first instinct for gender/status validation was to show error text
after the submit button was tapped. I argued this is wrong for chip selections — you can't "enter
an invalid gender", you either select one or you haven't yet. We landed on progressive prompting
with an animated border + disabled submit, which matches the mental model of a guided form rather
than a validation error form.

---

## Known Tradeoffs

**DummyJSON is a read-only fake API** — writes are simulated server-side but not actually persisted.
This created five concrete workarounds, all confined to the data and presentation layers:

1. **Duplicate id on create.** `POST /users/add` always returns `id: 209` regardless of payload.
   The repository discards it and assigns `Clock.System.now().toEpochMilliseconds()` instead,
   giving every locally-created user a unique id that won't collide with the server range (1–208)
   or repeat across creates.

2. **`status` ignored on create.** DummyJSON echoes back `status: "active"` regardless of what
   was sent. The repository overrides the field in the same `.copy()` call used to fix the id,
   preserving the user's chosen value locally.

3. **404 on delete for locally-created users.** `DELETE /users/{id}` returns 404 for any id > 208
   (the resource was never persisted server-side). `UserApi` catches `ResponseException` and treats
   404 as success — the delete intent is satisfied even if the server never knew the user existed.

4. **Deletes not persisted — deleted users reappear on refresh.** Even for server users (id 1–208),
   DummyJSON returns 200 on DELETE but doesn't actually remove them, so pull-to-refresh would
   restore them. The ViewModel maintains a `deletedIds: MutableSet<Long>` for the session lifetime.
   Every server response is filtered against this set before updating the UI, so deleted users stay
   gone for the entire session regardless of how many refreshes follow.

5. **Locally-created users survive pull-to-refresh and app restarts.** On a first-page fetch,
   the repository calls `clearServerUsers()` — a targeted SQL `DELETE WHERE id <= 208` — rather
   than wiping the entire cache. Locally-created users (id = epoch-ms timestamp, >> 208) are never
   touched by a refresh, so they persist across sessions automatically without any rescue logic.

**No Turbine** — the ViewModel tests use `StateFlow.value` snapshots rather than a flow-testing
library. This means tests don't catch multi-emission sequences, but for a `StateFlow` with
synchronous state updates (via `StandardTestDispatcher`) it's correct and avoids adding a
test-only dependency.

**Use cases are pure delegation** — acknowledged above. The tradeoff is a thin abstraction layer
that adds an indirection hop with no current benefit. It pays off when business logic accrues.

---

## What I'd Do With More Time

**Jetpack Paging 3** — the current infinite scroll uses a manual offset cursor (`skip`/`limit`)
which is correct but doesn't give backpressure, prefetch distance control, or retry semantics out
of the box. A `PagingSource` backed by Paging 3 would handle all of that and integrate with
`LazyPagingItems` on the UI side.

**Create user animation** — the new user appears at position 0 in the list. `animateItem()` handles
this, but I'd also briefly highlight the new card (a subtle background pulse) so the user's eye is
drawn to it.

**Error recovery granularity** — the current `GenericError` carries a message from the use case
layer but doesn't distinguish between 500, 503, 429, etc. I'd model these as distinct sealed class
variants with appropriate copy ("Service unavailable — try again shortly" vs "Rate limited").

**iOS-specific polish** — the Compose Multiplatform UI renders correctly on iOS but doesn't feel
fully native (no safe area handling refinements, no iOS-style swipe-to-delete). With more time I'd
add `expect/actual` for platform-specific gestures.

**End-to-end / screenshot tests** — unit tests cover the ViewModel and pure logic thoroughly.
I'd add Paparazzi (Android) screenshot tests for the major UI states (loading shimmer, error block,
populated list, dialogs) to prevent visual regressions.

**Localisation / string resources** — all UI strings are currently hardcoded inline. A production
KMP app would use Compose Multiplatform's `composeResources` with a shared `strings.xml` string
table, enabling localisation and centralising copy changes without touching composables.
