package com.android.server.pm;

public class PackageVerificationResponse {
    public final int callerUid;
    public final int code;

    public PackageVerificationResponse(int code2, int callerUid2) {
        this.code = code2;
        this.callerUid = callerUid2;
    }
}
