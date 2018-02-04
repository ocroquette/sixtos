package com.github.ocroquette.sixtos;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SixtosAuthenticator implements Authenticator<BasicCredentials, SixtosPrincipal>, Authorizer<SixtosPrincipal> {

    private final CredentialsFile credentialsFile;

    public SixtosAuthenticator(File credendialFile) {
        credentialsFile = new CredentialsFile(new File("credentials"));
    }

    @Override
    public Optional<SixtosPrincipal> authenticate(BasicCredentials credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        try {
            if (credentialsFile.validateCredentials(credentials.getUsername(), credentials.getPassword()))
                return Optional.of(new SixtosPrincipal(credentials.getUsername()));
            if (!credentialsFile.containsUser(username)) {
                System.err.println("Unknown user: " + username);
            } else {
                System.err.println("Invalid password provided for: " + username);
            }
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
        }
        return Optional.empty();
    }

    @Override
    public boolean authorize(SixtosPrincipal principal, String role) {
        try {
            return credentialsFile.getRoles(principal.getName()).contains(role);
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
            return false;
        }
    }

}
