# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 2.1.4 (Java 1.8) REST service for restaurant menus, backed by a file-based H2 database. Lombok is used for entities and DTOs. Tests are written with Spock (Groovy) and JUnit 4.

## Commands

Use `./mvnw` (or `mvn`):

```bash
./mvnw clean test                       # all tests (Spock + JUnit)
./mvnw test -Dtest=MenuServiceTest      # one test class (works for Spock specs too)
./mvnw test -Dtest='MenuControllerMockTest#getMenuById_MenuNotExist_Ok_1'   # one JUnit method
./mvnw clean package
java -jar -Dspring.profiles.active=development target/menus-0.0.1-SNAPSHOT.jar
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

Spock HTML reports are written by `spock-reports` to `build/spock-reports/` after a test run. There is no lint step.

## Build quirks

- Groovy is compiled by the `maven-antrun-plugin` running `groovyc` (no GMavenPlus). It handles `src/main/groovy` (currently empty) and `src/test/groovy`.
- The Groovy test folders use dotted names (`src/test/groovy/com.wifiest.menus.resource/`). The real package comes from the `package com.wifiesta.menus...` line in each file, not from the folder path.

## Architecture

The layers are `resource/` (controller + DTO) → `service/MenuService` → `repository/` (Spring Data JPA) → `domain/`.

- **Domain model is a recursive tree.** `Menu` has `@OneToMany(cascade = ALL)` lists of `items` and of `submenus` (which are `Menu`s too). Submenus are stored as ordinary `Menu` rows, so `GET /api/menus` / `findAll()` returns every menu at every level, including the submenus. `Item` has `price` (double) and `currency`, plus `ranking` validated to 1–5.
- **`MenuService`** is class-level `@Transactional`. The recursive aggregates are `getTotalItemsAmount(menuId)` (sums item prices at all depths, including inactive submenus) and `getActiveSubmenus(menuId)` (counts active menus in the tree, then subtracts 1 when the root is active so the root is never counted). `getMenu(id)` returns `null` when the id is unknown, and controllers turn that into a 404 with a `null` body.
- **`MenuController`** (`/api`) returns entities directly from `GET /menus/{id}` and `GET /menus/{id}/items?groupBy=price|ranking`. Only the list endpoint uses `MenuDTO` (Lombok `@Data @Builder`), and it builds each `uri` with `ServletUriComponentsBuilder.fromCurrentRequest()`, so that code only works during an HTTP request.
- **Seed data:** `Startup.LocalDevelopmentVersion` runs only under the `development` and `test` profiles. It saves a fixed tree (Menu-1 → Menu-2, Menu-3 → Menu-4; items 1–6) when no menu with the description `Menu-1` exists yet. Every test assumes this data (4 menus, specific prices and rankings).

## Profiles & data

- `application-development.properties` / `application.properties` → `jdbc:h2:file:./Database`. `application-test.properties` → `jdbc:h2:file:./Testdb`. Both use `hbm2ddl.auto=update`, so the DB files stay in the working directory between runs. If you change the entities or the seed data, delete `Testdb*` / `Database*` or you will get stale data.
- Every test sets `@ActiveProfiles("test")` and boots the full context (`@SpringBootTest`, either RANDOM_PORT + `TestRestTemplate` or `MockMvc`).
