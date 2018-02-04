package com.github.ocroquette.sixtos

import io.dropwizard.testing.ConfigOverride
import io.dropwizard.testing.DropwizardTestSupport
import io.dropwizard.testing.ResourceHelpers
import org.glassfish.jersey.client.JerseyClientBuilder
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class SixtosApplicationTest extends Specification {

    static TemporaryFolder temporaryFolder;

    static DropwizardTestSupport<SixtosConfiguration> dropWizardTestSupport;
    static File storageRoot
    static File credentials

    def setupSpec() {
        temporaryFolder = new TemporaryFolder()
        temporaryFolder.create()

        storageRoot = temporaryFolder.newFolder("storageRoot")
        credentials = temporaryFolder.newFile("credentials")

        // All passwords are "x"
        credentials.text = """fetcher:9454df8ba63d4b5ea623ae182a133609b5f81b468a7ca4c6aa750333fc99bb721b9d9f18:FETCHER
uploader:46ea7073d52945728c79b8df7f3deea4b80c0cc4146c38a0bcd1c50989749541b743f551:UPLOADER
"""
        new File(storageRoot, "preexistingfile").text = "Hello"
        new File(storageRoot, "preexistingdir").mkdirs()
        new File(storageRoot, "preexistingdir/fileinsubdir").text = "fileinsubdir"

        dropWizardTestSupport = new DropwizardTestSupport<SixtosConfiguration>(SixtosApplication.class,
                ResourceHelpers.resourceFilePath("config.yml"),
                ConfigOverride.config("server.applicationConnectors[0].port", "0"), // Use dynamic port
                ConfigOverride.config("storageRoot", storageRoot.getCanonicalPath()),
                ConfigOverride.config("credentialsFile", credentials.getCanonicalPath())
        )
        dropWizardTestSupport.before();
    }

    def cleanupSpec() {
        if (dropWizardTestSupport != null)
            dropWizardTestSupport.after();
        temporaryFolder.delete()
    }

    def "Anonymous file GET"() {
        given:
        Client client = new JerseyClientBuilder().build();

        when:
        Response response = client.target(
                String.format("http://localhost:%d/preexistingfile", dropWizardTestSupport.getLocalPort()))
                .request()
                .get();

        then:
        response.getStatus() == 401
    }

    def "Authorized file GET"() {
        given:
        HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic("fetcher", "x");
        Client client = new JerseyClientBuilder().register(auth).build()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/preexistingfile", dropWizardTestSupport.getLocalPort()))
                .request()
                .get()

        then:
        response.getStatus() == 200
        response.readEntity(String.class) == "Hello"
    }

    def "Anonymous list GET"() {
        given:
        Client client = new JerseyClientBuilder().build();

        when:
        Response response = client.target(
                String.format("http://localhost:%d/", dropWizardTestSupport.getLocalPort()))
                .request()
                .get();

        then:
        response.getStatus() == 401
    }

    def "Authorized list GET"() {
        given:
        HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic("fetcher", "x");
        Client client = new JerseyClientBuilder().register(auth).build()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/", dropWizardTestSupport.getLocalPort()))
                .request()
                .get()

        then:
        response.getStatus() == 200
        response.readEntity(String.class) == "preexistingdir/fileinsubdir\npreexistingfile"
    }


    def "Anonymous PUT"() {
        given:
        Client client = new JerseyClientBuilder().build()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/newfile", dropWizardTestSupport.getLocalPort()))
                .request()
                .put(Entity.entity("Hello".bytes, MediaType.APPLICATION_OCTET_STREAM))

        then:
        response.getStatus() == 401
    }

    def "Unauthorized PUT"() {
        given:
        HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic("fetcher", "x");
        Client client = new JerseyClientBuilder().register(auth).build()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/newfile", dropWizardTestSupport.getLocalPort()))
                .request()
                .put(Entity.entity("Hello".bytes, MediaType.APPLICATION_OCTET_STREAM))

        then:
        response.getStatus() == 403
    }

    def "Authorized PUT"() {
        given:
        HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic("uploader", "x");
        Client client = new JerseyClientBuilder().register(auth).build()

        when:
        Response response = client.target(
                String.format("http://localhost:%d/newfile", dropWizardTestSupport.getLocalPort()))
                .request()
                .put(Entity.entity("Content of the new file".bytes, MediaType.APPLICATION_OCTET_STREAM))

        then:
        response.getStatus() == 200
        new File(storageRoot, "newfile").text == "Content of the new file"
    }
}
