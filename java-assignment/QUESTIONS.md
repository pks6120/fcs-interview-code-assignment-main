# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
The code base mixes three styles: Store uses the Panache active-record pattern (static methods like
Store.listAll()), Product uses the repository pattern (ProductRepository), and Warehouse uses a
hexagonal approach with a separate domain model (Warehouse), a JPA entity (DbWarehouse) and a port
(WarehouseStore) implemented by a repository adapter.

Yes, I would refactor, mainly for consistency. A developer should not need to learn three patterns
to change one codebase. My choice:
- Move Store to the repository pattern. Static active-record calls are hard to mock, so unit-testing
  StoreResource without a database is awkward. A repository can be injected and mocked.
- Keep the hexagonal approach for Warehouse, since it has real business rules (limits, capacity,
  replace/archive). The domain logic stays free of JPA/HTTP and can be unit-tested quickly.
- For simple CRUD like Product, a plain repository is enough. Full ports and adapters would be
  over-engineering there.
- Align the ID handling (Store extends PanacheEntity, Product declares its own @Id) and add schema
  migrations (e.g. Flyway) instead of relying on Hibernate auto-generation.
I would do this incrementally, one aggregate at a time, protected by tests, not as a big-bang rewrite.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Contract-first (OpenAPI + generated code, as in Warehouse):
Pros: the spec is the single source of truth, so docs, clients and server stay in sync; teams can
work in parallel against the contract; breaking changes are visible in review; consistent
request/response models.
Cons: extra build setup and generated code that is harder to debug; a learning curve for the
generator; the spec can feel heavy for small, fast-changing endpoints; customizing generated code
is limited.

Code-first (Product and Store):
Pros: fast to write, simple, full control, less tooling.
Cons: documentation and behavior can drift apart, there is no formal contract for consumers, and
conventions (status codes, error format) end up inconsistent between endpoints.

My choice: contract-first for all public or cross-team APIs, so I would move Product and Store to
OpenAPI too and keep one consistent error format. Code-first only fits throwaway or purely internal
endpoints. In CI I would also validate the spec (linting and breaking-change detection).

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would follow the testing pyramid, prioritizing by business risk and cost:
1. Unit tests (most): the Warehouse use cases and validator. They hold the business rules (limits,
   capacity, stock matching, replace/archive), run in milliseconds, and need no database
   (mock WarehouseStore and LocationResolver).
2. Integration tests (some): @QuarkusTest with a real database (Dev Services/Testcontainers) for the
   REST endpoints and repositories: status codes, JSON, transactions, and the "legacy call only
   after commit" behaviour of the Store.
3. A few end-to-end/smoke tests for critical flows such as create then replace then archive.

To keep coverage effective over time: run all tests in CI on every pull request; use JaCoCo with a
threshold focused on the domain/use-case packages (not a vanity number for the whole project);
require a test with every bug fix and new rule; keep tests fast and deterministic to avoid flaky
suites; and review coverage trends rather than chasing 100%. Mutation testing (e.g. PIT) can be
added later on the domain layer to check that tests actually catch regressions.

```