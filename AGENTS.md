# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-module Maven monorepo rooted at `pom.xml`. Business services are grouped by domain, usually with `*-api` and `*-biz` submodules, for example `xiaohashu-note/xiaohashu-note-api` and `xiaohashu-note/xiaohashu-note-biz`. Shared libraries and Spring Boot starters live under `xiaoha-framework/`. Follow the standard Maven layout: Java code in `src/main/java`, tests in `src/test/java`, and service config in `src/main/resources/config`. MyBatis XML, Lua scripts, and generator configs are typically under `src/main/resources/{mapper,lua}`. Treat `target/` and `logs/` as generated output.

## Build, Test, and Development Commands
- `mvn clean test`: build the full reactor and run all tests.
- `mvn clean package -DskipTests`: produce all jars quickly when tests are not needed.
- `mvn -pl xiaohashu-note/xiaohashu-note-biz -am test`: test one service and any required upstream modules.
- `mvn -pl xiaohashu-note/xiaohashu-note-biz -am spring-boot:run`: run a single service locally.
- `mvn -pl xiaohashu-note/xiaohashu-note-biz mybatis-generator:generate`: regenerate mapper artifacts after editing `generatorConfig.xml`.

## Coding Style & Naming Conventions
Use Java 17, UTF-8, and 4-space indentation. Keep packages under `com.quanxiaoha...` and follow the existing Spring naming scheme: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, `*DO`, `*DTO`, `*ReqVO`, and `*RespVO`. Lombok is already used heavily; prefer it over manual boilerplate when consistent with nearby code. No formatter or Checkstyle rule is enforced in Maven, so format in the IDE before committing.

## Testing Guidelines
Tests use `spring-boot-starter-test` with JUnit 5, commonly via `@SpringBootTest`. Mirror the production package path in `src/test/java` and prefer class names ending in `Tests`. Add focused tests when changing controllers, service logic, MQ consumers, or MyBatis mappings. No coverage gate is configured, so local discipline matters: run the affected module tests before pushing.

## Commit & Pull Request Guidelines
Recent history includes generic messages such as `update`; prefer specific, imperative subjects with a module scope, for example `note: fix cache invalidation on delete`. Keep each commit limited to one concern. Pull requests should name affected modules, note config or schema changes, list the Maven commands used for verification, and include example requests or responses when API behavior changes.

## Security & Configuration Tips
Do not commit secrets or environment-specific values in `application-prod.yaml`, `application-dev.yaml`, or `bootstrap.yaml`. Prefer externalized configuration for Nacos, databases, Redis, and MQ endpoints. Leave IDE files, local logs, and generated build output out of reviews.
