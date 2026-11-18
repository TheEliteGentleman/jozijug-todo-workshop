package za.co.jozijug.todo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business logic + database access. Every method runs inside a transaction.
 * Changes made to a "managed" entity are saved automatically when the transaction commits.
 */
@ApplicationScoped
@Transactional
public class TodoService {

    // Jakarta EE 11: the EntityManager can be injected with plain CDI @Inject.
    // (Quarkus supports exactly the same line.)
    @Inject
    EntityManager em;

    // ---- READ ----

    public List<Todo> findAll() {
        return em.createQuery("SELECT t FROM Todo t ORDER BY t.id", Todo.class)
                 .getResultList();
    }

    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(em.find(Todo.class, id));
    }

    // ---- CREATE ----

    public Todo create(Todo todo) {
        todo.setId(null);          // the database chooses the id, never the client
        em.persist(todo);
        return todo;
    }

    // ---- UPDATE ----

    public Optional<Todo> update(Long id, Todo changes) {
        Todo existing = em.find(Todo.class, id);   // "managed" entity
        if (existing == null) {
            return Optional.empty();
        }
        existing.setTitle(changes.getTitle());
        existing.setCompleted(changes.isCompleted());
        return Optional.of(existing);              // UPDATE is sent on commit
    }

    // ---- DELETE ----

    public boolean delete(Long id) {
        Todo existing = em.find(Todo.class, id);
        if (existing == null) {
            return false;
        }
        em.remove(existing);
        return true;
    }
}
