package org.acme;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@jakarta.ws.rs.Path("/")
public class GreetingResource {

    @Inject
    Logger log;

    @GET
    @jakarta.ws.rs.Path("view/{path: .*}")
    @Parameter(
        name = "path",
        example = "path/to/file",
        schema = @Schema(implementation = String.class)
    )
    @Produces(MediaType.TEXT_PLAIN)
    public String view(@PathParam("path") java.nio.file.Path path) {
        log.infof("GreetingResource.view(%s)", path.toString());

        return path.toString();
    }
    @GET
    @jakarta.ws.rs.Path("viewwrapper/{path: .*}")
    @Parameter(
        name = "path",
        example = "path/to/file",
        schema = @Schema(implementation = String.class)
    )
    @Produces(MediaType.TEXT_PLAIN)
    public String viewWrapper(@PathParam("path") PathWrapper path) {
        log.infof("GreetingResource.viewWrapper(%s)", path.toString());

        return path.toString();
    }

}

