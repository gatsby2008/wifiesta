# menu-summary-endpoint — Menu summary endpoint: total price and active submenus for a menu

> Source: Free text

## Summary

Add a read-only `GET /api/menus/{id}/summary` endpoint that returns a menu's id,
description, total item price (recursive over submenus) and count of active
submenus (recursive), reusing the aggregates `MenuService` already computes, so
clients stop downloading the whole menu tree to recompute them.

## Scope

### In scope
- New endpoint `GET /api/menus/{id}/summary` in `MenuController`
- New response DTO (e.g. `MenuSummaryDTO`) in `resource/`, following the
  `MenuDTO` pattern (Lombok `@Data @Builder`)
- Reusing the existing `MenuService` aggregates (`getTotalItemsAmount`,
  `getActiveSubmenus` or their `*Helper(Menu)` variants)
- A summary-specific traversal in `MenuService` (new method(s)), because the
  rules in #2 / #3 / #5 differ from the existing aggregates: an inactive
  submenu hides its whole subtree, and the total is only computed for a single
  currency. The existing aggregates stay unchanged.
- A `currency` field in the response (from #1 = d)
- Integration tests for the new endpoint against the seeded `test` profile data

### Out of scope
- Changing the existing endpoints (`/api/menus`, `/api/menus/{id}`,
  `/api/menus/{id}/items`) or their responses
- Changing the entities (`Menu`, `Item`) or the mock data in `Startup`
- Pagination, caching
- Currency conversion (#1 resolved: totals are never converted, only summed when the currency is uniform)
- Changing the results of the existing `getTotalItemsAmount` / `getActiveSubmenus`

## Behavior

1. **B1** — `GET /api/menus/{id}/summary` for an existing menu returns HTTP 200
   with a JSON body containing exactly the fields `menuId`, `description`,
   `totalAmount`, `currency` and `activeSubmenus`. `menuId` and `description` are the
   requested menu's `id` and `description`. (`currency` added per #1 = d.)
2. **B2** — `totalAmount` is the sum of the `price` of the menu's own items plus
   the items of its **active** submenus, recursing at any depth only through
   active submenus. An inactive submenu contributes nothing, and neither does
   anything below it (#2, #3). The requested menu's own items always count,
   even if the menu itself is inactive. This differs from
   `MenuService.getTotalItemsAmount`, which also adds inactive submenus.
   With seed data (all menus active): menu 1 → `497.0`, menu 2 → `100.0`,
   menu 3 → `250.0`, menu 4 → `150.0`.
3. **B3** — `activeSubmenus` counts the active submenus at any depth and never
   counts the menu itself, whether or not the menu itself is active. An inactive
   submenu hides its whole subtree: active submenus below an inactive one are
   **not** counted (#3). This differs from `MenuService.getActiveSubmenus` for
   that case only. With seed data: menu 1 → `3`,
   menu 2 → `0`, menu 3 → `1`, menu 4 → `0`.
4. **B4** — An unknown menu id (e.g. `1004`) returns HTTP 404 with an empty
   body, matching `GET /api/menus/{id}`. It must not return 200 with zeroed
   aggregates (the service aggregates return `0` / `0.0` for unknown ids, so
   the endpoint has to check whether the menu exists).
5. **B5** — The endpoint is read-only and synchronous: it publishes no event to
   Kafka or any message queue, and it does not write to the database. Calling
   it leaves the stored menus and items unchanged.
6. **B6** — Currency uniformity (#1 = d, #5): the counted items are the same
   set as in B2. When they all share the same `currency` value, `currency` is
   that value and `totalAmount` is their sum. When they have **different**
   currency values, the response is still HTTP 200 with `totalAmount: null`
   and `currency: null`, and `menuId`, `description` and `activeSubmenus` are
   still filled in. Prices are never converted.
7. **B7** — Null currency (#6): `null` counts as a currency value of its own.
   If every counted item has `null`, the currencies are uniform, so `currency`
   is `null` and `totalAmount` is the sum (seed data: menu 1 → `497.0`). If
   `null` and a non-null currency (e.g. `"USD"`) both appear, they are mixed
   and B6 applies. A tree with no counted items has `totalAmount: 0.0` and
   `currency: null`.

## Implementation Context
- `src/main/java/com/wifiesta/menus/resource/MenuController.java`: `@RestController`
  under `/api` with field-`@Autowired` `MenuService`. The existing handlers return
  `ResponseEntity.status(NOT_FOUND).body(null)` for a missing menu and log
  elapsed time. `getMenuById` (`/menus/{id}`) is the 404 pattern to copy.
- `src/main/java/com/wifiesta/menus/resource/MenuDTO.java`: the DTO pattern to
  follow (Lombok `@Data @Builder`, plain fields).
- `src/main/java/com/wifiesta/menus/service/MenuService.java`:
  - `getMenu(Long id)` returns `null` when the id is unknown. This is the existence check.
  - `getTotalItemsAmount(Long)` / `getTotalItemsAmountHelper(Menu)` return a
    `double`. They recurse into **all** submenus, including inactive ones (see #2).
  - `getActiveSubmenus(Long)` / `getActiveSubmenusHelper(Menu)` count the active
    menus in the tree minus 1 when the root is active. The helper recurses into
    the children of inactive submenus too (see #3).
  - Each `Long` overload calls `getMenu` (one `findById`) again, so calling
    `getMenu` and then both helpers on the loaded `Menu` avoids three lookups.
  - The class is `@Transactional` (read-write). It is a read path only, so no writes happen.
  - Because of #2 / #3 / #5, the summary can't simply call the existing
    helpers. It needs its own traversal that skips inactive submenus (and
    their subtrees) and collects the counted items' prices and currencies.
    `MenuServiceTest` keeps asserting the old aggregates, so leave those
    methods untouched.
- `src/main/java/com/wifiesta/menus/domain/Menu.java` / `Item.java`: `Menu.items` and
  `Menu.submenus` are `@OneToMany(cascade = ALL)`. `Item.price` is a `double`
  and `Item.currency` is a `String` (null in all seed data).
- `src/main/java/com/wifiesta/menus/Startup.java`: seeds Menu-1 (items 45.2,
  50.9, 50.9) → Menu-2 (100.0), Menu-3 (100.0) → Menu-4 (150.0), all active, under the
  `development` and `test` profiles.
- Tests:
  - `src/test/groovy/com.wifiest.menus.resource/MenuControllerTest.groovy`: Spock +
    `TestRestTemplate`, RANDOM_PORT, `@ActiveProfiles("test")`.
  - `src/test/java/com/wifiesta/menus/resource/MenuControllerMockTest.java`: JUnit 4 + MockMvc + `jsonPath`.
  - `src/test/groovy/com.wifiest.menus.service/MenuServiceTest.groovy`: already
    asserts the aggregate values above.
  - The seed data is all active and all `currency = null`, so B2/B3 (inactive
    subtrees) and B6 (mixed currency) can't be tested against the seeded
    `test` DB. Adding menus to `Testdb` would also break the existing
    `menus.size() == 4` assertions. Cover those cases with tests that build
    `Menu`/`Item` trees in memory and pass them to the new traversal, and keep
    the HTTP integration tests on the seed data (B1, B4, B7).

## Expected Change Scope
- **Expected files touched**: 4–6 (1 new DTO, `MenuController`, `MenuService`, 1–3 test files)
- **Expected layers**: controller (resource) + DTO + service (new summary traversal; existing methods unchanged)
- **Avoid touching**:
  - `Menu`, `Item`, `TypeMenu` entities
  - `Startup` seed data
  - Existing endpoints and their response shapes
  - Existing aggregate semantics in `MenuService`. #2 / #3 changed the behavior, so the new rules go only into new methods and `MenuServiceTest` keeps passing.
  - `.claude/rules/service-rules.md` (#4: not part of this feature)

## Safe Constraints
**Safe**:
- Add a new DTO class in `resource/` and a new handler method in `MenuController`
- Call `MenuService.getMenu` plus the public `*Helper(Menu)` methods to avoid repeated lookups
- Keep `totalAmount` as a nullable `Double` (it is `null` when currencies are mixed, B6)
- Inject nothing new, or follow the existing field-`@Autowired` style in `MenuController` (#4)
- Add new Spock and/or MockMvc tests for the endpoint

**Unsafe**:
- Returning the `Menu` entity from the new endpoint
- Returning 200 with zeroed aggregates for an unknown id
- Any repository `save`/`delete`, or any event or message publishing, from this path
- Changing the existing `getTotalItemsAmount` / `getActiveSubmenus` results (covered by `MenuServiceTest`)
- Modifying the entities or the `Startup` seed data
- Converting prices between currencies, or summing prices of different currencies (B6)
- Inserting extra menus into the shared `test` DB from tests (breaks the existing count assertions)

## Open Questions

- [x] **#1** What should `totalAmount` be when the items in one menu tree use different `currency` values? Options: (a) ignore currency and sum the raw prices (current `getTotalItemsAmount` behavior), (b) return an error (e.g. 409/422), (c) return one total per currency, (d) add a `currency` field and only sum when the currencies are uniform. All seed items have `currency = null`. — resolved: (d). Add a `currency` field and sum only when the counted items share one currency (see B6, B7, #5, #6).
- [x] **#2** Should `totalAmount` include the items of **inactive** submenus (current `getTotalItemsAmount` behavior) or only the items of the menu plus its active submenus? — resolved: only the items of the menu plus its active submenus (B2).
- [x] **#3** For `activeSubmenus`, should an active submenu nested under an **inactive** submenu be counted? The current `getActiveSubmenusHelper` counts it. AC3 says "active submenus at any depth" and doesn't cover this case. The answer to #2 probably decides it too: should an inactive submenu hide its whole subtree? — resolved: an inactive submenu hides its whole subtree, for both `activeSubmenus` and `totalAmount` (B2, B3).
- [x] **#4** The scaffolded `.claude/rules/service-rules.md` template includes "JPA entities must never be returned directly from controllers. Always map through MapStruct to a response DTO", "Constructor injection only — no `@Autowired`", and a `bash commands/check.sh` pre-push requirement. This project has no MapStruct and no `commands/check.sh`. `MenuController` uses field `@Autowired`, and the source asks for a Lombok `@Data @Builder` DTO built by hand. Confirm that the hand-built Lombok DTO and the existing field-injection style are correct here, and that these template rules don't apply to this repo (or should be removed or replaced in `service-rules.md`). — resolved (user: "you decide"): the template rules don't apply to this feature. Use a hand-built Lombok `@Data @Builder` DTO (no MapStruct, as the source requires). Add no new injected dependencies, and where one is needed follow the existing field-`@Autowired` style in `MenuController`. `commands/check.sh` doesn't exist, so the pre-push validation is `./mvnw clean test`. `service-rules.md` itself is left as-is and should be filled in or cleaned up separately.
- [x] **#5** With #1 = (d), what does the endpoint return when the counted items have different currencies? — resolved: HTTP 200 with `totalAmount: null` and `currency: null`. The other fields are still filled in (B6).
- [x] **#6** How does a `null` item currency count in the uniformity check? — resolved: `null` is a value of its own. All-null is uniform (`currency: null`, sum returned), and null mixed with a non-null currency counts as mixed (B7).
