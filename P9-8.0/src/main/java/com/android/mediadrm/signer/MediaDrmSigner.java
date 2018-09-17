package com.android.mediadrm.signer;

import android.media.DeniedByServerException;
import android.media.MediaDrm;

public final class MediaDrmSigner {
    public static final int CERTIFICATE_TYPE_X509 = 1;

    public static final class Certificate {
        private final android.media.MediaDrm.Certificate mCertificate;

        Certificate(android.media.MediaDrm.Certificate certificate) {
            this.mCertificate = certificate;
        }

        public byte[] getWrappedPrivateKey() {
            return this.mCertificate.getWrappedPrivateKey();
        }

        public byte[] getContent() {
            return this.mCertificate.getContent();
        }
    }

    public static final class CertificateRequest {
        private final android.media.MediaDrm.CertificateRequest mCertRequest;

        CertificateRequest(android.media.MediaDrm.CertificateRequest certRequest) {
            this.mCertRequest = certRequest;
        }

        public byte[] getData() {
            return this.mCertRequest.getData();
        }

        public String getDefaultUrl() {
            return this.mCertRequest.getDefaultUrl();
        }
    }

    private MediaDrmSigner() {
    }

    public static CertificateRequest getCertificateRequest(MediaDrm drm, int certType, String certAuthority) {
        return new CertificateRequest(drm.getCertificateRequest(certType, certAuthority));
    }

    public static Certificate provideCertificateResponse(MediaDrm drm, byte[] response) throws DeniedByServerException {
        return new Certificate(drm.provideCertificateResponse(response));
    }

    public static byte[] signRSA(MediaDrm drm, byte[] sessionId, String algorithm, byte[] wrappedKey, byte[] message) {
        return drm.signRSA(sessionId, algorithm, wrappedKey, message);
    }
}
