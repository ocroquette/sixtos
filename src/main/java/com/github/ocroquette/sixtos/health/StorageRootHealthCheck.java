package com.github.ocroquette.sixtos.health;

import com.codahale.metrics.health.HealthCheck;

import java.io.File;

public class StorageRootHealthCheck extends HealthCheck {

    private final File storageRoot;

    public StorageRootHealthCheck(File storageRoot) {
        this.storageRoot = storageRoot;
    }

    @Override
    protected Result check() throws Exception {
        if (storageRoot.isDirectory() && storageRoot.canRead()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot access storageRoot: " + storageRoot);
        }
    }
}
