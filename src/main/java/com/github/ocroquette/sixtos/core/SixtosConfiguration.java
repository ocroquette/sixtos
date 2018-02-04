package com.github.ocroquette.sixtos.core;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SixtosConfiguration extends Configuration {
    // @NotEmpty
    private String template;

    // @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
}
