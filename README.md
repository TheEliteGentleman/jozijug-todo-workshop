# Jozi JUG Todo workshop

A tiny CRUD app (create, read, update, delete todos) built with standard Jakarta EE 11 APIs only.
The **same Java source code** runs on a Jakarta EE 11 server (as a WAR) and on Quarkus.

```
jozijug-todo-workshop/
├── WORKSHOP.md        <- start here: step-by-step exercises
├── exercises/         <- starter project with TODOs for you to complete
├── solution/          <- finished project (peek only when stuck)
├── server-config/     <- sample Open Liberty server.xml
└── smoke-test.sh      <- curl script that checks all CRUD operations
```

| Technology            | Jakarta EE 11 spec           | Used for                         |
|-----------------------|------------------------------|----------------------------------|
| `@Entity`             | Jakarta Persistence 3.2      | Saving todos in a database       |
| `@Transactional`      | Jakarta Transactions 2.0     | Transaction boundaries           |
| `@ApplicationScoped`, `@Inject` | Jakarta CDI 4.1    | Wiring classes together          |
| `@Path`, `@GET`, ...  | Jakarta REST 4.0             | The HTTP/JSON API                |
| `@NotBlank`, `@Valid` | Jakarta Validation 3.1       | Rejecting bad input              |
| JSON conversion       | Jakarta JSON Binding 3.0     | Java object <-> JSON             |

**Quick start (finished app on Quarkus):**
```bash
cd solution
mvn quarkus:dev -Pquarkus        # then open http://localhost:8080
```

**Quick start (finished app on a Jakarta EE server):**
```bash
cd solution
mvn clean package                # builds target/todo.war
```
Deploy `todo.war` to Payara 7, GlassFish 8 or Open Liberty, then open `http://localhost:8080/todo/`
(Liberty: port 9080). Details in [WORKSHOP.md](WORKSHOP.md).

## Facilitator notes
- Versions used: Jakarta EE Web API 11.0.0, Java 21 (Java 17 also works: set `maven.compiler.release` to 17), H2 2.3.232, Quarkus 3.40.1.2 (LTS).
- Do a dry run on the server you plan to use before the session. Server versions and Docker tags move quickly.
