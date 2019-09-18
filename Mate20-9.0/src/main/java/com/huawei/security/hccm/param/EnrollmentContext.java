package com.huawei.security.hccm.param;

import java.security.cert.Certificate;
import java.util.Arrays;

public final class EnrollmentContext {
    private Certificate[] mClientCertificateChain = null;
    private EnrollmentParamsSpec mEnrollmentParamsSpec = null;

    public EnrollmentContext(EnrollmentParamsSpec params) {
        this.mEnrollmentParamsSpec = params;
    }

    public EnrollmentParamsSpec getEnrollmentParams() {
        return this.mEnrollmentParamsSpec;
    }

    public synchronized Certificate[] getClientCertificateChain() {
        return (Certificate[]) Arrays.copyOf(this.mClientCertificateChain, this.mClientCertificateChain.length);
    }

    public synchronized void setClientCertificateChain(Certificate[] clientCertificateChain) {
        this.mClientCertificateChain = (Certificate[]) Arrays.copyOf(clientCertificateChain, clientCertificateChain.length);
    }
}
