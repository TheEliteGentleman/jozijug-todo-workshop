package za.co.jozijug.todo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
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
 * The org.eclipse.microprofile.openapi annotations only DESCRIBE the API for the generated
 * OpenAPI document. They do not change how the code behaves.
 */
@Path("/todos")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Todos", description = "Create, read, update and delete todo items")
public class TodoResource {

    // Descriptions used several times. Annotation values must be compile-time constants.
    private static final String ID_DESCRIPTION = "Id of the todo";
    private static final String NOT_FOUND = "No todo with that id exists";
    private static final String INVALID = "Invalid input: the title must not be blank and "
                                        + "must have at most 120 characters";

    @Inject
    TodoService service;

    @GET
    @Operation(operationId = "listTodos",
               summary = "List all todos",
               description = "Returns every todo, ordered by id.")
    @APIResponse(responseCode = "200",
                 description = "The todos. The array is empty when there are none.",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(type = SchemaType.ARRAY, implementation = Todo.class)))
    public List<Todo> list() {
        return service.findAll();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getTodo",
               summary = "Get one todo",
               description = "Returns the todo with the given id.")
    @APIResponse(responseCode = "200",
                 description = "The todo",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Todo.class)))
    @APIResponse(responseCode = "404", description = NOT_FOUND)
    public Todo get(@Parameter(description = ID_DESCRIPTION, required = true, example = "1")
                    @PathParam("id") Long id) {
        return service.findById(id).orElseThrow(NotFoundException::new);   // -> 404
    }

    @POST
    @Operation(operationId = "createTodo",
               summary = "Create a todo",
               description = "Creates a todo. The database chooses the id and the creation time.")
    @APIResponse(responseCode = "201",
                 description = "The todo was created",
                 headers = @Header(name = "Location",
                                   description = "URL of the new todo",
                                   schema = @Schema(type = SchemaType.STRING)),
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Todo.class)))
    @APIResponse(responseCode = "400", description = INVALID)
    public Response create(
            @RequestBody(description = "The todo to create. The fields id and createdAt are ignored.",
                         required = true,
                         content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(implementation = Todo.class),
                                            examples = @ExampleObject(
                                                    name = "Buy milk",
                                                    value = "{\"title\": \"Buy milk\", \"completed\": false}")))
            @Valid Todo todo,                                              // @Valid -> 400 on bad input
            @Context UriInfo uriInfo) {
        Todo created = service.create(todo);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(String.valueOf(created.getId()))
                              .build();
        return Response.created(location).entity(created).build();         // 201 + Location header
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updateTodo",
               summary = "Update a todo",
               description = "Replaces the title and the completed flag of an existing todo.")
    @APIResponse(responseCode = "200",
                 description = "The updated todo",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Todo.class)))
    @APIResponse(responseCode = "400", description = INVALID)
    @APIResponse(responseCode = "404", description = NOT_FOUND)
    public Todo update(
            @Parameter(description = ID_DESCRIPTION, required = true, example = "1")
            @PathParam("id") Long id,
            @RequestBody(description = "The new title and completed state. The fields id and createdAt are ignored.",
                         required = true,
                         content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                            schema = @Schema(implementation = Todo.class),
                                            examples = @ExampleObject(
                                                    name = "Mark as done",
                                                    value = "{\"title\": \"Buy milk\", \"completed\": true}")))
            @Valid Todo todo) {
        return service.update(id, todo).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteTodo",
               summary = "Delete a todo",
               description = "Permanently removes the todo with the given id.")
    @APIResponse(responseCode = "204", description = "The todo was deleted. The response has no body.")
    @APIResponse(responseCode = "404", description = NOT_FOUND)
    public Response delete(@Parameter(description = ID_DESCRIPTION, required = true, example = "1")
                           @PathParam("id") Long id) {
        if (!service.delete(id)) {
            throw new NotFoundException();
        }
        return Response.noContent().build();                                // 204
    }
}