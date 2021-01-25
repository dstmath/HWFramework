package android.net.http;

import android.annotation.UnsupportedAppUsage;
import java.security.cert.X509Certificate;

public class SslError {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int SSL_DATE_INVALID = 4;
    public static final int SSL_EXPIRED = 1;
    public static final int SSL_IDMISMATCH = 2;
    public static final int SSL_INVALID = 5;
    @Deprecated
    public static final int SSL_MAX_ERROR = 6;
    public static final int SSL_NOTYETVALID = 0;
    public static final int SSL_UNTRUSTED = 3;
    @UnsupportedAppUsage
    final SslCertificate mCertificate;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    int mErrors;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    final String mUrl;

    @Deprecated
    public SslError(int error, SslCertificate certificate) {
        this(error, certificate, "");
    }

    @Deprecated
    public SslError(int error, X509Certificate certificate) {
        this(error, certificate, "");
    }

    public SslError(int error, SslCertificate certificate, String url) {
        addError(error);
        this.mCertificate = certificate;
        this.mUrl = url;
    }

    public SslError(int error, X509Certificate certificate, String url) {
        this(error, new SslCertificate(certificate), url);
    }

    public static SslError SslErrorFromChromiumErrorCode(int error, SslCertificate cert, String url) {
        if (error == -200) {
            return new SslError(2, cert, url);
        }
        if (error == -201) {
            return new SslError(4, cert, url);
        }
        if (error == -202) {
            return new SslError(3, cert, url);
        }
        return new SslError(5, cert, url);
    }

    public SslCertificate getCertificate() {
        return this.mCertificate;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public boolean addError(int error) {
        boolean rval = error >= 0 && error < 6;
        if (rval) {
            this.mErrors = (1 << error) | this.mErrors;
        }
        return rval;
    }

    public boolean hasError(int error) {
        boolean rval = false;
        boolean rval2 = error >= 0 && error < 6;
        if (!rval2) {
            return rval2;
        }
        if ((this.mErrors & (1 << error)) != 0) {
            rval = true;
        }
        return rval;
    }

    public int getPrimaryError() {
        if (this.mErrors == 0) {
            return -1;
        }
        for (int error = 5; error >= 0; error--) {
            if ((this.mErrors & (1 << error)) != 0) {
                return error;
            }
        }
        return -1;
    }

    public String toString() {
        return "primary error: " + getPrimaryError() + " certificate: " + getCertificate() + " on URL: " + getUrl();
    }
}
