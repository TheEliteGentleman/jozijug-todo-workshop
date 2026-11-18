# Workshop: build a Todo app with Jakarta EE 11

**Level:** beginner (you know basic Java: classes, methods, annotations)
**Time:** about 2 to 3 hours
**You will build:** a REST API plus a small web page to create, read, update and delete todos, and run the *same code* on a Jakarta EE server and on Quarkus.

---

## 0. What you need

| Tool | Version | Check with |
|------|---------|------------|
| JDK | 21 (17 also works) | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| curl | any | `curl --version` |
| An IDE | IntelliJ IDEA, Eclipse, VS Code... | |
| **One** runtime to test on | see Part 3 | |

For the fastest start, use **Quarkus dev mode** (Part 3B): it only needs Java and Maven and reloads your code on every change.

---

## 1. The big picture

```
 Browser / curl
      │  HTTP + JSON
      ▼
 TodoResource      Jakarta REST      "the waiter": takes the HTTP request, returns the HTTP response
      │  Java call
      ▼
 TodoService       CDI + Transactions  "the kitchen": business logic, one transaction per call
      │  EntityManager
      ▼
 Todo (@Entity)    Jakarta Persistence "the recipe card": maps a Java object to a table row
      │  SQL
      ▼
 H2 database (in memory, resets on every restart)
```

### Five ideas to remember

1. **Entity**: a Java class mapped to a database table (`@Entity`). One object = one row.
2. **EntityManager**: the object that reads and writes entities (`find`, `persist`, `remove`, queries).
3. **Transaction**: a group of database operations that all succeed or all fail (`@Transactional`).
4. **CDI bean**: a class the runtime creates and injects for you (`@ApplicationScoped`, `@Inject`). You never write `new TodoService()`.
5. **REST resource**: a class whose methods answer HTTP requests (`@Path`, `@GET`, `@POST`, ...).

### The CRUD mapping

| Operation | HTTP | URL | Success status | Failure status |
|-----------|------|-----|----------------|----------------|
| **C**reate | `POST` | `/api/todos` | 201 Created | 400 invalid input |
| **R**ead all | `GET` | `/api/todos` | 200 | |
| **R**ead one | `GET` | `/api/todos/{id}` | 200 | 404 not found |
| **U**pdate | `PUT` | `/api/todos/{id}` | 200 | 400 / 404 |
| **D**elete | `DELETE` | `/api/todos/{id}` | 204 No Content | 404 |

---

## 2. Project tour

Open the `exercises/` folder in your IDE.

```
exercises/
├── pom.xml                                   build file (two profiles: server WAR / Quarkus)
└── src/main
    ├── java/com/example/todo/
    │   ├── TodoApplication.java              ✅ given: turns on REST under /api
    │   ├── Todo.java                         ✏️ Exercises 1 and 6
    │   ├── TodoService.java                  ✏️ Exercises 2, 3, 4, 5
    │   └── TodoResource.java                 ✏️ Exercises 2, 3, 4, 5, 6
    ├── resources/
    │   ├── META-INF/persistence.xml          ✅ given: JPA setup for Jakarta EE servers
    │   └── application.properties            ✅ given: same setup for Quarkus
    └── webapp/
        ├── index.html                        ✅ given: the web page (plain HTML + JavaScript)
        └── WEB-INF/web.xml, beans.xml        ✅ given: data source + CDI setup for servers
```

You only edit **three Java files**. Look for `TODO Exercise` comments.

### Why does one codebase run in two places?

The Java code uses only standard `jakarta.*` APIs. Only the *configuration files* differ, and each runtime ignores the other's:

| File | Jakarta EE server | Quarkus |
|------|:-----------------:|:-------:|
| `src/main/java/**` | used | used |
| `index.html` | served from the WAR | copied to `META-INF/resources` by the `quarkus` profile |
| `WEB-INF/web.xml` (data source) | used | ignored |
| `META-INF/persistence.xml` | used | left out by the `quarkus` profile |
| `application.properties` | ignored | used |

In `pom.xml`, the default profile (`jakartaee`) builds a **WAR** and expects the server to provide the APIs (`provided` scope). The `quarkus` profile builds a **JAR** and brings Quarkus extensions instead.

---

## 3. How to run and check your work

Pick **one** runtime now. You can try the other at the end (Part 5).

### 3A. Jakarta EE server (WAR)

```bash
cd exercises
mvn clean package              # creates target/todo.war
```

Then deploy `target/todo.war`:

| Server | How | URL |
|--------|-----|-----|
| **Payara 7** (Docker) | `docker run --rm -p 8080:8080 -v "$PWD/target/todo.war:/opt/payara/deployments/todo.war" payara/server-full:7.2026.3` | http://localhost:8080/todo/ |
| **Payara 7 / GlassFish 8** (zip download) | `bin/asadmin start-domain` then `bin/asadmin deploy --contextroot todo /path/to/target/todo.war` | http://localhost:8080/todo/ |
| **Open Liberty** | copy `server-config/liberty/server.xml` to `usr/servers/defaultServer/`, copy the WAR to `usr/servers/defaultServer/apps/`, run `bin/server run` | http://localhost:9080/todo/ |

After each code change: rebuild, redeploy (`asadmin redeploy ...` on Payara/GlassFish; Liberty reloads the WAR by itself).

Set this helper variable for the curl commands below:
```bash
BASE=http://localhost:8080/todo        # Liberty: http://localhost:9080/todo
```

### 3B. Quarkus (dev mode, recommended for the exercises)

```bash
cd exercises
mvn quarkus:dev -Pquarkus
```

Leave it running. Quarkus recompiles and reloads when you save a file and send a request.

```bash
BASE=http://localhost:8080
```

> ⚠️ Always add `-Pquarkus` for Quarkus commands. Without it Maven builds the server WAR.

---

## 4. The exercises

### Exercise 1: make `Todo` a database entity (`Todo.java`)

Add these annotations (package `jakarta.persistence`):

- on the class: `@Entity` and `@Table(name = "todos")`
- on the `id` field: `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- a new method `void onCreate()` annotated with `@PrePersist` that sets `createdAt = LocalDateTime.now()`

**Why?** `@Entity` tells the persistence provider "store this class in a table". `IDENTITY` lets the database hand out ids (1, 2, 3...). `@PrePersist` runs just before the INSERT.

**Checkpoint:** the app must **start without errors**. (Until now it fails at startup because `persistence.xml` lists `Todo` but it is not an entity.)
`curl -i $BASE/api/todos` currently returns **500**. That is expected: the service is not written yet.

<details><summary>Show solution</summary>

```java
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    ...
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
```
</details>

---

### Exercise 2: Read (`TodoService.java` and `TodoResource.java`)

**2a.** `findAll()`: run the JPQL query `SELECT t FROM Todo t ORDER BY t.id` with `em.createQuery(..., Todo.class).getResultList()`.

**2b.** `findById(id)`: `em.find(Todo.class, id)` returns `null` when nothing exists. Wrap it: `Optional.ofNullable(...)`.

**2c.** In `TodoResource`, finish `get(...)`: add `@GET`, `@Path("/{id}")`, and `@PathParam("id")` on the parameter. Return `service.findById(id).orElseThrow(NotFoundException::new)`.

Study the `list()` method first: it is already done and shows the pattern.

**Checkpoint:**
```bash
curl -i $BASE/api/todos          # 200 and []
curl -i $BASE/api/todos/1        # 404
```

<details><summary>Show solution</summary>

```java
// TodoService
public List<Todo> findAll() {
    return em.createQuery("SELECT t FROM Todo t ORDER BY t.id", Todo.class).getResultList();
}
public Optional<Todo> findById(Long id) {
    return Optional.ofNullable(em.find(Todo.class, id));
}

// TodoResource
@GET
@Path("/{id}")
public Todo get(@PathParam("id") Long id) {
    return service.findById(id).orElseThrow(NotFoundException::new);
}
```
</details>

---

### Exercise 3: Create

**3a. `TodoService.create`:** `todo.setId(null)`, then `em.persist(todo)`, then `return todo`.

**3b. `TodoResource.create`:** add `@POST` and `@Context` on `UriInfo`. Call the service, build the `Location` header and answer `201 Created`:

```java
URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
return Response.created(location).entity(created).build();
```

**Why `setId(null)`?** A client could send its own `"id": 99` in the JSON. The database must choose the id, so we throw the client's value away.

**Checkpoint:**
```bash
curl -i -H "Content-Type: application/json" \
     -d '{"title":"Learn Jakarta EE","completed":false}' $BASE/api/todos
# 201 Created, a Location header, and the todo with its generated id and createdAt

curl $BASE/api/todos             # the list now has one item
```
Open the web page in your browser and add a todo there too.

<details><summary>Show solution</summary>

```java
// TodoService
public Todo create(Todo todo) {
    todo.setId(null);
    em.persist(todo);
    return todo;
}

// TodoResource
@POST
public Response create(Todo todo, @Context UriInfo uriInfo) {
    Todo created = service.create(todo);
    URI location = uriInfo.getAbsolutePathBuilder()
                          .path(String.valueOf(created.getId())).build();
    return Response.created(location).entity(created).build();
}
```
</details>

---

### Exercise 4: Update

**4a. `TodoService.update`:** `em.find` the existing todo. If it is `null`, return `Optional.empty()`. Otherwise copy `title` and `completed` from `changes` and return `Optional.of(existing)`.

> 💡 There is **no `save()` call**. `existing` is a *managed* entity: the persistence provider notices the changes and sends the `UPDATE` when the transaction commits (when the method returns). This is called *dirty checking*.

**4b. `TodoResource.update`:** add `@PUT`, `@Path("/{id}")`, `@PathParam("id")`. Return `service.update(id, todo).orElseThrow(NotFoundException::new)`.

**Checkpoint:**
```bash
curl -i -X PUT -H "Content-Type: application/json" \
     -d '{"title":"Learn Jakarta EE 11","completed":true}' $BASE/api/todos/1
# 200, completed is now true

curl -i -X PUT -H "Content-Type: application/json" \
     -d '{"title":"x","completed":false}' $BASE/api/todos/999
# 404
```

<details><summary>Show solution</summary>

```java
// TodoService
public Optional<Todo> update(Long id, Todo changes) {
    Todo existing = em.find(Todo.class, id);
    if (existing == null) {
        return Optional.empty();
    }
    existing.setTitle(changes.getTitle());
    existing.setCompleted(changes.isCompleted());
    return Optional.of(existing);
}

// TodoResource
@PUT
@Path("/{id}")
public Todo update(@PathParam("id") Long id, Todo todo) {
    return service.update(id, todo).orElseThrow(NotFoundException::new);
}
```
</details>

---

### Exercise 5: Delete

**5a. `TodoService.delete`:** find the todo; if `null` return `false`; otherwise `em.remove(existing)` and return `true`.

**5b. `TodoResource.delete`:** add `@DELETE`, `@Path("/{id}")`, `@PathParam("id")`. When the service returns `false` throw `NotFoundException`, otherwise return `Response.noContent().build()`.

**Checkpoint:**
```bash
curl -i -X DELETE $BASE/api/todos/1     # 204 (no body)
curl -i -X DELETE $BASE/api/todos/1     # 404 (already gone)
```

<details><summary>Show solution</summary>

```java
// TodoService
public boolean delete(Long id) {
    Todo existing = em.find(Todo.class, id);
    if (existing == null) {
        return false;
    }
    em.remove(existing);
    return true;
}

// TodoResource
@DELETE
@Path("/{id}")
public Response delete(@PathParam("id") Long id) {
    if (!service.delete(id)) {
        throw new NotFoundException();
    }
    return Response.noContent().build();
}
```
</details>

---

### Exercise 6: validate the input

Right now `{"title": ""}` is accepted. Fix it with Jakarta Validation:

1. In `Todo.java`, annotate `title` with `@NotBlank` and `@Size(max = 120)` (package `jakarta.validation.constraints`). Add a `message` if you like.
2. In `TodoResource`, put `@Valid` (`jakarta.validation.Valid`) in front of the `Todo todo` parameter of **both** `create` and `update`.

**Checkpoint:**
```bash
curl -i -H "Content-Type: application/json" -d '{"title":"   ","completed":false}' $BASE/api/todos
# 400 Bad Request
```

<details><summary>Show solution</summary>

```java
// Todo
@NotBlank(message = "title must not be blank")
@Size(max = 120, message = "title must be at most 120 characters")
private String title;

// TodoResource
public Response create(@Valid Todo todo, @Context UriInfo uriInfo) { ... }
public Todo update(@PathParam("id") Long id, @Valid Todo todo) { ... }
```
</details>

---

### Final check: the smoke test

From the workshop root folder:
```bash
./smoke-test.sh http://localhost:8080          # Quarkus
./smoke-test.sh http://localhost:8080/todo     # Jakarta EE server
```
Every line should say `PASS`. Then open the web page and try everything by hand: add, tick, edit, delete.

Stuck? Compare your file with the same file in `solution/`.

---

## 5. Prove it is portable

Run your finished code (or `solution/`) on the runtime you have **not** used yet:

- Did Part 3A? Now do Part 3B (`mvn quarkus:dev -Pquarkus`).
- Did Part 3B? Now do Part 3A (`mvn clean package`, deploy the WAR).

**Discussion questions**
1. Which files did you have to change to switch runtime? (Answer: none. Only the Maven profile.)
2. Which annotations come from `jakarta.*`? Is there any `io.quarkus` or server-specific import in the Java code?
3. Why is `jakarta.jakartaee-web-api` marked `provided`, and what replaces it on Quarkus?

---

## 6. Troubleshooting

| Symptom | Likely cause and fix |
|---------|----------------------|
| Deployment fails mentioning `com.example.todo.Todo` is not an entity | Exercise 1 is not finished (`@Entity` missing). |
| HTTP 500 and `UnsupportedOperationException` in the log | An exercise method is still a stub. The message names the exercise. |
| HTTP 404 on a server | The URL needs the context root: `/todo/api/todos`, not `/api/todos`. |
| `quarkus:dev` is not found, or Maven builds a WAR instead of starting Quarkus | You forgot `-Pquarkus` on the Maven command. |
| `ClassNotFoundException: org.h2.jdbcx.JdbcDataSource` on a server | The server cannot see the H2 driver bundled in the WAR. Install H2 as a server library (Payara/GlassFish: `asadmin add-library h2.jar`; Liberty: add a `<library>`), or point `web.xml` at a data source your server already has. |
| `Unsatisfied dependency for type EntityManager` on a server | The server does not yet support Jakarta EE 11's `@Inject EntityManager`. On servers (not Quarkus) you can switch to `@PersistenceContext EntityManager em;`. |
| `invalid target release: 21` | Use JDK 21, or set `maven.compiler.release` to `17` in `pom.xml`. |
| Port 8080 already in use | Stop the other process, or on Quarkus run `mvn quarkus:dev -Pquarkus -Dquarkus.http.port=8081`. |
| Data disappears on restart | Expected: H2 is in memory and the schema is recreated at startup. |

---

## 7. Bonus exercises

1. **Add a field.** Give `Todo` an optional `description` (max 500 chars). Restart and see the new column appear. Update `index.html` to show it.
2. **Filter.** Support `GET /api/todos?completed=true` using `@QueryParam("completed") Boolean completed` and a JPQL query with a `WHERE` clause.
3. **Use a DTO.** Today the entity is also the JSON body. Create a Java `record TodoInput(String title, boolean completed)` with the validation annotations, and use it for `POST` and `PUT`. Why is this safer?
4. **Better errors.** Write an `ExceptionMapper<ConstraintViolationException>` that returns a JSON list of `{field, message}`.
5. **Real database.** Replace H2 with PostgreSQL: change the data source in `web.xml` (server) and `application.properties` + the `quarkus-jdbc-postgresql` extension (Quarkus). Switch `drop-and-create` to `none` or `update`.
6. **Read about Jakarta Data 1.0**, new in Jakarta EE 11. A repository interface can replace much of `TodoService`. Check that your runtime supports it before you use it.

---

## 8. Where to go next

- Jakarta EE 11 specifications: https://jakarta.ee/specifications/
- Quarkus guides (Hibernate ORM, REST): https://quarkus.io/guides/
- Payara docs: https://docs.payara.fish/ · GlassFish: https://glassfish.org/ · Open Liberty: https://openliberty.io/docs/
