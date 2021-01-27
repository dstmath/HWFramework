package ohos.net.http;

import java.security.cert.X509Certificate;

public class SslError {
    public static final int SSL_CERT_AUTHORITY_INVALID = 3;
    public static final int SSL_CERT_DATE_INVALID = 4;
    public static final int SSL_CERT_EXPIRED = 1;
    public static final int SSL_CERT_INVALID = 5;
    public static final int SSL_CERT_MAX_ERROR = 6;
    public static final int SSL_CERT_NAME_INVALID = 2;
    public static final int SSL_CERT_NOT_YET_VALID = 0;
    int mErrors;
    final String mUrl;
    final X509Certificate mX509Certificate;

    public SslError(int i, X509Certificate x509Certificate, String str) {
        addError(i);
        this.mX509Certificate = x509Certificate;
        this.mUrl = str;
    }

    public boolean addError(int i) {
        boolean z = i >= 0 && i <= 6;
        if (z) {
            this.mErrors = (1 << i) | this.mErrors;
        }
        return z;
    }

    public boolean hasError(int i) {
        boolean z = i >= 0 && i <= 6;
        return z ? isGetError(i) : z;
    }

    public X509Certificate getCertificate() {
        return this.mX509Certificate;
    }

    public int getCriticalError() {
        if (this.mErrors == 0) {
            return -1;
        }
        for (int i = 5; i >= 0; i--) {
            if (isGetError(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isGetError(int i) {
        return (this.mErrors & (1 << i)) != 0;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public String toString() {
        return SslError.class.getSimpleName() + ": mErrors = " + this.mErrors + ", mX509Certificate = " + this.mX509Certificate + ", mUrl = " + this.mUrl;
    }
}
