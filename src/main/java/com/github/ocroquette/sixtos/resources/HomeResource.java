package com.github.ocroquette.sixtos.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class HomeResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public String get() {
        return "Welcome to this Sixtos server";
    }
}