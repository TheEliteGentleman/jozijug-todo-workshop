package za.co.jozijug.todo;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activates Jakarta REST and puts every resource under /api.
 * Nothing else to write: the server (or Quarkus) discovers the resources by itself.
 */
@ApplicationPath("/api")
public class TodoApplication extends Application {
}
