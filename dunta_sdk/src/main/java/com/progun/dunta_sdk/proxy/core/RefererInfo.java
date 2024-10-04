package com.progun.dunta_sdk.proxy.core;

import androidx.annotation.NonNull;

public class RefererInfo {
    private final String install;
    private final String version;

    public RefererInfo(@NonNull String install, @NonNull String version) {
        this.install = install;
        this.version = version;
    }

    public String getInstall() {
        return install;
    }

    public String getVersion() {
        return version;
    }
}
