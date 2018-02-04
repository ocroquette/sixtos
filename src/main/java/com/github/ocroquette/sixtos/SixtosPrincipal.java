package com.github.ocroquette.sixtos;

import java.security.Principal;

public class SixtosPrincipal implements Principal {
    private String name;

    public SixtosPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
