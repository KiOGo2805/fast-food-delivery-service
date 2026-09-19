# Testing Demo — Lecture 3 Companion Project

Standalone Maven project built specifically for the Unit Testing lecture.
Not part of the student scaffold — just working, runnable teaching material.

## What's in here

| Package | Purpose |
|---|---|
| `pricing.DiscountRules` | Pure static utility — no mocking needed. Shows public/package-private/private testability. |
| `pricing.PricingCalculator` | Public API hides a private validation method — shows "test through the public contract." |
| `pricing.OrderPricingService` | Depends on an interface (`ExchangeRateClient`) — the main Mockito demo target. |
| `exchange.ExchangeRateClient` / `HttpExchangeRateClient` | Real HTTP client — the WireMock demo target. |
| `audit.AuditTrail` | Has a **final** method — the Mockito final-method gotcha demo. |

## Running the tests

```bash
mvn test
```

## Running with coverage

```bash
mvn test
```

JaCoCo is wired to run automatically as part of `test` (see `pom.xml`).
Open the report at:

```
target/site/jacoco/index.html
```

Green = executed at least once. **Say this explicitly in the lecture:**
green does not mean correct — see the commented-out "bad test" examples in
`PricingCalculatorTest` for tests that would still show green coverage
while verifying nothing.

## Things to verify locally before presenting (I could not run a live build)

I don't have network/build access in the environment I used to write this,
so a few things are correct to the best of my knowledge but not
build-verified — please run `mvn test` yourself before the lecture and
adjust if anything's off:

1. **WireMock coordinates** — `org.wiremock:wiremock` version `3.9.1` is
   what I used. WireMock's Maven `groupId` changed from
   `com.github.tomakehurst` to `org.wiremock` after the project moved to an
   independent org, but the **Java package names stayed**
   `com.github.tomakehurst.wiremock.*` for backward compatibility. If the
   build fails on missing WireMock classes, check whether a newer/older
   WireMock version or artifact name is needed.

2. **The final-method mocking gotcha** (`AuditTrailMockingGotchaTest`) —
   Mockito 5.x's default mock maker changed to "inline," which *can* mock
   final methods. This demo may not reproduce the classic silent-failure
   gotcha anymore on current versions. Full explanation and a fallback
   narration plan are in the comment at the top of that test file — read it
   before presenting this section.

3. Java 21 / Spring Boot 3.5.16 versions match the rest of the course
   scaffold, but double-check your local Maven/JDK setup resolves
   everything cleanly.

## Suggested live-coding order (see full lecture doc for timing)

1. `DiscountRulesTest` — parameterized tests, package-private testing
2. `PricingCalculatorTest` — testing private behavior through the public
   API, exception-path testing, bad-test examples (commented out, uncomment
   live)
3. `AuditTrailMockingGotchaTest` — final-method gotcha (verify behavior
   first, per note above)
4. `OrderPricingServiceTest` — Mockito mocks/verify/ArgumentCaptor
5. `HttpExchangeRateClientWireMockTest` — WireMock stubbing the real HTTP
   client, success/error/malformed-response scenarios
6. `mvn test` + open the JaCoCo report together
