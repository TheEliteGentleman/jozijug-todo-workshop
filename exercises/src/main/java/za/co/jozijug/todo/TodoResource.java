package za.co.jozijug.todo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/**
 * The HTTP layer: translates HTTP requests into calls on TodoService.
 *
 *   GET    /api/todos        -> list all
 *   GET    /api/todos/{id}   -> read one
 *   POST   /api/todos        -> create
 *   PUT    /api/todos/{id}   -> update
 *   DELETE /api/todos/{id}   -> delete
 *
 * Add the missing imports as you go (your IDE can do this for you).
 */
@Path("/todos")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    @Inject
    TodoService service;

    // ===== WORKED EXAMPLE: study this one, then do the others yourself =====
    // @GET maps the HTTP method. The class-level @Path("/todos") gives the URL.
    // The returned List<Todo> is converted to a JSON array automatically.
    @GET
    public List<Todo> list() {
        return service.findAll();
    }

    // ============================ EXERCISE 2c (READ ONE) ============================
    // Add:  @GET  @Path("/{id}")  and  @PathParam("id") on the parameter.
    // Return the todo, or throw new NotFoundException() (-> HTTP 404) when it is missing.
    // Hint: service.findById(id).orElseThrow(NotFoundException::new)
    public Todo get(Long id) {
        throw new UnsupportedOperationException("TODO Exercise 2c");
    }

    // ============================ EXERCISE 3 (CREATE) ===============================
    // Add:  @POST
    // Add @Context on the UriInfo parameter.
    // Call service.create(todo), then answer HTTP 201 with a Location header:
    //   URI location = uriInfo.getAbsolutePathBuilder()
    //                         .path(String.valueOf(created.getId())).build();
    //   return Response.created(location).entity(created).build();
    public Response create(Todo todo, UriInfo uriInfo) {
        throw new UnsupportedOperationException("TODO Exercise 3");
    }

    // ============================ EXERCISE 4 (UPDATE) ===============================
    // Add:  @PUT  @Path("/{id}")  and  @PathParam("id")
    // Return the updated todo, or throw NotFoundException when service.update(...) is empty.
    public Todo update(Long id, Todo todo) {
        throw new UnsupportedOperationException("TODO Exercise 4");
    }

    // ============================ EXERCISE 5 (DELETE) ===============================
    // Add:  @DELETE  @Path("/{id}")  and  @PathParam("id")
    // If service.delete(id) returns false throw NotFoundException.
    // Otherwise return Response.noContent().build()   (HTTP 204, empty body).
    public Response delete(Long id) {
        throw new UnsupportedOperationException("TODO Exercise 5");
    }

    // ============================ EXERCISE 6 (VALIDATION) ===========================
    // Add @Valid (jakarta.validation.Valid) in front of the `Todo todo` parameter of
    // create(...) and update(...). Then add @NotBlank/@Size to Todo.title.
    // Blank titles must now be rejected with HTTP 400.
}
