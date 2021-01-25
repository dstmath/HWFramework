package com.android.server.biometrics.face;

import com.android.server.biometrics.Constants;

public class FaceConstants implements Constants {
    @Override // com.android.server.biometrics.Constants
    public String logTag() {
        return "FaceService";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagHalDied() {
        return "faced_died";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagAuthToken() {
        return "face_token";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagAuthStartError() {
        return "faced_auth_start_error";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagEnrollStartError() {
        return "faced_enroll_start_error";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagEnumerateStartError() {
        return "faced_enum_start_error";
    }

    @Override // com.android.server.biometrics.Constants
    public String tagRemoveStartError() {
        return "faced_remove_start_error";
    }

    @Override // com.android.server.biometrics.Constants
    public int actionBiometricAuth() {
        return 1504;
    }

    @Override // com.android.server.biometrics.Constants
    public int actionBiometricEnroll() {
        return 1505;
    }

    @Override // com.android.server.biometrics.Constants
    public int acquireVendorCode() {
        return 22;
    }
}
