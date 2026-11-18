package za.co.jozijug.todo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * A single TODO item. This class is both the database table (Jakarta Persistence)
 * and the JSON shape of the API (Jakarta JSON Binding).
 */
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "title must not be blank")
    @Size(max = 120, message = "title must be at most 120 characters")
    private String title;

    private boolean completed;

    private LocalDateTime createdAt;

    /** Called by the persistence provider right before the INSERT. */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

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
