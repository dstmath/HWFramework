package com.android.server.biometrics;

public interface Constants {
    int acquireVendorCode();

    int actionBiometricAuth();

    int actionBiometricEnroll();

    String logTag();

    String tagAuthStartError();

    String tagAuthToken();

    String tagEnrollStartError();

    String tagEnumerateStartError();

    String tagHalDied();

    String tagRemoveStartError();
}
