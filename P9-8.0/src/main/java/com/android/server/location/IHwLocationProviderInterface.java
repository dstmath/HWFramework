package com.android.server.location;

public interface IHwLocationProviderInterface {
    boolean reportNLPLocation(int i);

    void resetNLPFlag();
}
