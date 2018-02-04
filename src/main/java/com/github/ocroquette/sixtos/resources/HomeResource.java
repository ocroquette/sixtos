package com.github.ocroquette.sixtos.resources;

import com.codahale.metrics.annotation.Timed;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class HomeResource {
    private final File storageRoot;

    public HomeResource(File storageRoot) {
        this.storageRoot = storageRoot;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    @RolesAllowed("FETCHER")
    public Response get() {
        try {
            String listing = Files.walk(storageRoot.toPath())
                    .filter(Files::isRegularFile)
                    .map(p -> storageRoot.toPath().relativize(p).toString() )
                    .sorted()
                    .collect(Collectors.joining("\n"));
            return Response
                    .status(Response.Status.OK)
                    .entity(listing)
                    .build();
        } catch (IOException e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ERROR: " + e.toString())
                    .build();
        }
    }
}