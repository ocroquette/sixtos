package com.github.ocroquette.sixtos;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SixtosConfiguration extends Configuration {
    @NotEmpty
    private String storageRoot;

    @JsonProperty
    public String getStorageRoot() {
        return storageRoot;
    }

    @JsonProperty
    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }
}
