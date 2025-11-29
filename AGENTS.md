# Repository Guidelines

## Project Structure & Module Organization
- Java 17 Maven library; sources live in `src/main/java/berlin/yuna/typemap` with `logic` (converters/encoders), `model` (TypeMap/TypeList + concurrent variants), and `config` (converter registry). Shared resources sit in `src/main/resources`.  
- Tests reside in `src/test/java` (JUnit Jupiter + AssertJ); sample fixtures can be placed in `src/test/resources`. Build outputs land in `target/`.

## Build, Test, and Development Commands
- `./mvnw clean verify` – full build, unit tests, and JaCoCo coverage.
- `./mvnw test` – run test suite only; use `-Dtest=TypeMapTest` to target a class.
- `./mvnw -DskipTests package` – produce an artifact without executing tests (CI should still run them).
- `./mvnw install` – publish the library to your local Maven cache for downstream usage.
- Java 17 toolchain required; prefer running inside the provided Maven wrapper.

## Coding Style & Naming Conventions
- Four-space indentation, braces on new lines; keep imports explicit and ordered (java.*, javax.*, third-party, project).
- Favor immutable state: mark fields `final` where possible; avoid shared mutable data and reflection; no parallel streams.
- Public APIs should never return `null`—use Optionals or empty collections. Keep converters pure and side-effect free.
- Naming: classes/interfaces in PascalCase, methods/fields in camelCase, constants in UPPER_SNAKE_CASE. Tests mirror class names with `*Test`.

## Testing Guidelines
- Frameworks: JUnit Jupiter 6, AssertJ. Test discovery matches `**/*Test.java`.
- Structure tests with Given/When/Then and cover both happy-path conversions and edge cases (nullables, invalid JSON/XML, concurrent maps).
- Aim for deterministic runs (fixed seeds/Clocks). Generate coverage with `./mvnw jacoco:report` if you need reports locally.

## Commit & Pull Request Guidelines
- Branch from `main`; use semantic commit messages (`feat:`, `fix:`, `chore:`). Keep commits focused and small.
- Ensure new code is covered by tests and passes `./mvnw clean verify` before raising a PR.
- PRs should include a short summary, linked issue, and any relevant screenshots or API notes. Bump versions following semver when publishing.
- Avoid large refactors without prior discussion; prefer incremental, reviewable changes.
