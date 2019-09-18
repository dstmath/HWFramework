package com.android.org.conscrypt.ct;

public final class VerifiedSCT {
    public final SignedCertificateTimestamp sct;
    public final Status status;

    public enum Status {
        VALID,
        INVALID_SIGNATURE,
        UNKNOWN_LOG,
        INVALID_SCT
    }

    public VerifiedSCT(SignedCertificateTimestamp sct2, Status status2) {
        this.sct = sct2;
        this.status = status2;
    }
}
