package com.huawei.security.hccm.param;

import java.security.cert.X509Certificate;

public final class ProtocolParamCMP extends ProtocolParam<ProtocolParamCMP> {
    private X509Certificate mRaCertificate;
    private X509Certificate mRootCertificate;

    public X509Certificate getRaCertificate() {
        return this.mRaCertificate;
    }

    public void setRaCertificate(X509Certificate certificate) {
        this.mRaCertificate = certificate;
    }

    public X509Certificate getRootCertificate() {
        return this.mRootCertificate;
    }

    public void setRootCertificate(X509Certificate certificate) {
        this.mRootCertificate = certificate;
    }
}
