package com.github.ocroquette.sixtos.resources;

import com.codahale.metrics.annotation.Timed;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/file/{path}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class FileResource {
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Timed
    @RolesAllowed("FETCHER")
    public Response get(@PathParam("path") String path) {
        File file = new File("config.yml");
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("UPLOADER")
    public Response put(@PathParam("path") String path)
    {
        return Response.ok().entity(path).build();
    }
}