package za.co.jozijug.todo;

import java.time.LocalDateTime;

/**
 * A single TODO item. This class is both the database table (Jakarta Persistence)
 * and the JSON shape of the API (Jakarta JSON Binding).
 *
 * ============================== EXERCISE 1 ==============================
 * Turn this plain Java class into a database entity.
 *
 *   1a. Mark the class as an entity:                 @Entity
 *   1b. Name the table "todos":                      @Table(name = "todos")
 *   1c. Mark the `id` field as the primary key:      @Id
 *   1d. Let the database generate it:                @GeneratedValue(strategy = GenerationType.IDENTITY)
 *   1e. Fill `createdAt` automatically by adding a method annotated with @PrePersist
 *       that sets createdAt = LocalDateTime.now()
 *
 * All annotations live in the jakarta.persistence package.
 * =======================================================================
 */
public class Todo {

    private Long id;

    private String title;

    private boolean completed;

    private LocalDateTime createdAt;

    // TODO Exercise 1e: add the @PrePersist method here

    // TODO Exercise 6 (later): add validation annotations on `title`
    //      @NotBlank and @Size(max = 120)   (package jakarta.validation.constraints)

    // --- getters and setters (required by JPA and JSON-B) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
