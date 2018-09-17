package com.android.server.location;

public interface IHwLocalLocationProvider {
    public static final String LOCAL_PROVIDER = "local_database";

    void enable();

    String getName();

    boolean isEnabled();

    void requestLocation();
}
