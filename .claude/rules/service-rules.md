# Service Rules — menus

> **Ownership:** this service owns `Menu` and `Item` and their storage in its own H2 database
> (`com.wifiesta.menus`). There are no upstream systems: no identity field is owned externally, and
> there are no external clients, queues or events. All ids are set by the application; there is no
> generated sequence. The seed data in `Startup` is the reference dataset for the `development`
> and `test` profiles.

## Key concepts

| Entity | Key fields | Notes |
|--------|------------|-------|
| `Menu` | `id` (`long`, assigned explicitly), `isActive`, `type` (`TypeMenu`) | Recursive: `submenus` is a `@OneToMany(cascade = ALL)` list of `Menu`, and `items` a list of `Item`. Submenus are ordinary `Menu` rows, so `findAll()` / `GET /api/menus` returns every level of every tree. |
| `Item` | `id` (`Long`), `price` (`double`), `currency` (`String`, nullable), `ranking` | `ranking` is validated to 1–5. All seed items have `currency = null`. |

## Business Invariants

- Two sets of aggregate rules exist, and they are intentionally different. Never "align" one with the other:
  - **Legacy** `MenuService.getTotalItemsAmount` / `getActiveSubmenus`: walk **every** submenu, including inactive ones. `MenuServiceTest` pins these results.
  - **Summary** `MenuService.buildSummary` (`GET /api/menus/{id}/summary`): an **inactive submenu hides its whole subtree** from both the total and the count. The requested menu's own items always count, and the menu itself is never counted as a submenu. _(from spec my-666)_
- Prices are never converted between currencies. A total is only summed when every counted item has the same `currency`, and `null` counts as a currency of its own. When currencies are mixed, the summary still returns 200, with `totalAmount: null` and `currency: null`. _(from spec my-666)_
- An unknown menu id returns HTTP 404 with an empty body on every `/api/menus/{id}...` endpoint, never 200 with zeroed values. Service lookups return `null` for "not found", and the service aggregates return `0` for unknown ids, so callers must check existence first.

## Fallback and Idempotency

- Every current endpoint is a synchronous read: no repository writes, no events, no external calls. A new read endpoint must keep it that way.
- `Startup` seeds data only when no menu with the description `Menu-1` exists, so re-running it on an existing database changes nothing.

## Architecture Constraints

- Layering: `resource/` (controllers + DTOs) → `service/MenuService` → `repository/` (Spring Data JPA) → `domain/`.
- `Menu.items` / `Menu.submenus` load lazily. Walk the tree inside `MenuService` (class-level `@Transactional`), never in a controller.
- New endpoints return a response DTO in `resource/`: a Lombok `@Data @Builder` class written by hand, following `MenuDTO`. This project does not use MapStruct.
- Dependency injection follows the existing style: `MenuController` and `MenuService` use field `@Autowired`, `Startup` uses constructor injection. Don't convert existing classes as part of an unrelated change.

## Historical Constraints

- `GET /api/menus/{id}` and `GET /api/menus/{id}/items` return JPA entities directly. That contract is existing and must not change without a versioned migration; new endpoints must not copy it.
- Tests share a file-based H2 database (`./Testdb`, `hbm2ddl.auto=update`) loaded with the seed data, and several tests assert exact seed counts (e.g. 4 menus). Tests must never save entities. Cover cases the seed data can't express (inactive subtrees, mixed currencies) with `Menu`/`Item` trees built in memory.

> **Implementation note (verified 2026-11-01):** the build needs JDK 11 or older, because Lombok 1.18.6 doesn't compile on JDK 21 (`JAVA_HOME=~/.sdkman/candidates/java/11.0.19-amzn` works). Surefire runs through the TestNG provider (TestNG arrives via `groovy-all`), so `@Unroll` iterations show up only in `build/spock-reports/`, not in the surefire XML. The SDD coverage gate (`lib/coverage.py`) only recognizes `.java` test files, so Spock specs need a waiver until the plugin learns about `.groovy`.

## Out of Scope (Do Not Add Here)

- Ticket-only decisions
- Temporary rollout notes
- Dates, branch names, or one-off implementation tasks
