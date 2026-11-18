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
 *
 * Useful EntityManager methods:
 *   em.createQuery("JPQL", Todo.class).getResultList()   run a query
 *   em.find(Todo.class, id)                              read by id (null if missing)
 *   em.persist(entity)                                   INSERT
 *   em.remove(entity)                                    DELETE
 */
@ApplicationScoped
@Transactional
public class TodoService {

    // Jakarta EE 11: the EntityManager can be injected with plain CDI @Inject.
    // (Quarkus supports exactly the same line.)
    @Inject
    EntityManager em;

    // ============================ EXERCISE 2 (READ) ============================
    // 2a. Return all todos ordered by id. JPQL: "SELECT t FROM Todo t ORDER BY t.id"
    public List<Todo> findAll() {
        throw new UnsupportedOperationException("TODO Exercise 2a");
    }

    // 2b. Find one todo by id and wrap it in an Optional (Optional.ofNullable).
    public Optional<Todo> findById(Long id) {
        throw new UnsupportedOperationException("TODO Exercise 2b");
    }

    // ============================ EXERCISE 3 (CREATE) ==========================
    // 3. Set the id to null (the database chooses it), persist the todo, return it.
    public Todo create(Todo todo) {
        throw new UnsupportedOperationException("TODO Exercise 3");
    }

    // ============================ EXERCISE 4 (UPDATE) ==========================
    // 4. Load the existing todo. If it does not exist return Optional.empty().
    //    Otherwise copy title and completed from `changes` onto it and return it.
    //    No explicit "save" call is needed: the entity is managed, so the UPDATE
    //    is sent automatically when the transaction commits.
    public Optional<Todo> update(Long id, Todo changes) {
        throw new UnsupportedOperationException("TODO Exercise 4");
    }

    // ============================ EXERCISE 5 (DELETE) ==========================
    // 5. Load the todo. If missing return false. Otherwise remove it and return true.
    public boolean delete(Long id) {
        throw new UnsupportedOperationException("TODO Exercise 5");
    }
}
