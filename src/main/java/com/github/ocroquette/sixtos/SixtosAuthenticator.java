package com.github.ocroquette.sixtos;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SixtosAuthenticator implements Authenticator<BasicCredentials, SixtosPrincipal>, Authorizer<SixtosPrincipal> {

    private final CredentialsFile credentialsFile;

    public SixtosAuthenticator(File credentialsFile) {
        this.credentialsFile = new CredentialsFile(credentialsFile);
    }

    private Logger log = Logger.getLogger(this.getClass().getName());

    @Override
    public Optional<SixtosPrincipal> authenticate(BasicCredentials credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        try {
            if (credentialsFile.validateCredentials(username, password))
                return Optional.of(new SixtosPrincipal(username));
            if (!credentialsFile.containsUser(username)) {
                log.info("Unknown user: " + username);
            } else {
                log.info("Invalid password provided for: " + username);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean authorize(SixtosPrincipal principal, String role) {
        try {
            return credentialsFile.getRoles(principal.getName()).contains(role);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

}
