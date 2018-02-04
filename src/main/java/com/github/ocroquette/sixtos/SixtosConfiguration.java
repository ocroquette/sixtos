package com.github.ocroquette.sixtos;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SixtosConfiguration extends Configuration {
    @NotEmpty
    private String storageRoot;

    @NotEmpty
    private String credentialsFile;

    @JsonProperty
    public String getStorageRoot() {
        return storageRoot;
    }

    @JsonProperty
    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    @JsonProperty
    public String getCredentialsFile() {
        return credentialsFile;
    }

    @JsonProperty
    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }
}
