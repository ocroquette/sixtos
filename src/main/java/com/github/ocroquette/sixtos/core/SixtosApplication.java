package com.github.ocroquette.sixtos.core;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.github.ocroquette.sixtos.resources.FileResource;
import com.github.ocroquette.sixtos.resources.HomeResource;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.io.File;

public class SixtosApplication extends Application<SixtosConfiguration> {

    public static void main(final String[] args) throws Exception {
        new SixtosApplication().run(args);
    }

    @Override
    public String getName() {
        return "Silfis server";
    }

    @Override
    public void initialize(final Bootstrap<SixtosConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final SixtosConfiguration configuration,
                    final Environment environment) {

        environment.jersey().register(new FileResource());
        environment.jersey().register(new HomeResource());

        File credentialsFile = new File("credentials");
        SixtosAuthenticator sifisAuthenticator = new SixtosAuthenticator(credentialsFile);

        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<SixtosPrincipal>()
                        .setAuthenticator(sifisAuthenticator)
                        .setAuthorizer(sifisAuthenticator)
                        .setRealm(getName())
                        .buildAuthFilter()));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

}
