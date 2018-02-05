package com.github.ocroquette.sixtos.resources;

import com.codahale.metrics.annotation.Timed;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

@Path("/")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class FileResource {

    private final File storageRoot;

    public FileResource(File storageRoot) {
        this.storageRoot = storageRoot;
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Timed
    @Path("{relativePath : .+}")
    @RolesAllowed("FETCHER")
    public Response get(@PathParam("relativePath") String path) {
        File file = new File(storageRoot, path);

        if (!file.exists()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } else {
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"") //optional
                    .build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{relativePath  : .+}")
    @RolesAllowed("UPLOADER")
    public Response put(@PathParam("relativePath") String path, InputStream inputStream) {
        File file = new File(storageRoot, path);

        if (file.exists()) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("ERROR: File already exists")
                    .build();
        } else {
            try {
                receive(inputStream, file);
            } catch (IOException e) {
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("ERROR: " + e.toString())
                        .build();
            }
            return Response
                    .ok()
                    .entity("File has been uploaded successfuly to \"" + path + "\"")
                    .build();
        }
    }

    private static long receive(InputStream from, File file) throws IOException {
        long total = 0;
        FileOutputStream out = null;

        try {
            file.getParentFile().mkdirs();
            if ( ! file.getParentFile().isDirectory() )
                throw new IOException("Failed to create parent directory: " + file.getParentFile().getCanonicalPath());
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024 * 1024];
            while (true) {
                int r = from.read(buf);
                if (r == -1) {
                    break;
                }
                out.write(buf, 0, r);
                total += r;
            }
        } finally {
            if (out != null)
                out.close();
        }

        return total;
    }
}