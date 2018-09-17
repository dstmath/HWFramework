package android.net.http;

import android.net.ProxyInfo;
import java.security.cert.X509Certificate;

public class SslError {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int SSL_DATE_INVALID = 4;
    public static final int SSL_EXPIRED = 1;
    public static final int SSL_IDMISMATCH = 2;
    public static final int SSL_INVALID = 5;
    @Deprecated
    public static final int SSL_MAX_ERROR = 6;
    public static final int SSL_NOTYETVALID = 0;
    public static final int SSL_UNTRUSTED = 3;
    final SslCertificate mCertificate;
    int mErrors;
    final String mUrl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.http.SslError.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.http.SslError.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.http.SslError.<clinit>():void");
    }

    @Deprecated
    public SslError(int error, SslCertificate certificate) {
        this(error, certificate, ProxyInfo.LOCAL_EXCL_LIST);
    }

    @Deprecated
    public SslError(int error, X509Certificate certificate) {
        this(error, certificate, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public SslError(int error, SslCertificate certificate, String url) {
        Object obj = SSL_EXPIRED;
        if (!-assertionsDisabled) {
            if ((certificate != null ? SSL_EXPIRED : SSL_NOTYETVALID) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if (url == null) {
                obj = SSL_NOTYETVALID;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        addError(error);
        this.mCertificate = certificate;
        this.mUrl = url;
    }

    public SslError(int error, X509Certificate certificate, String url) {
        this(error, new SslCertificate(certificate), url);
    }

    public static SslError SslErrorFromChromiumErrorCode(int error, SslCertificate cert, String url) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (error >= -299 && error <= -200) {
                obj = SSL_EXPIRED;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (error == -200) {
            return new SslError((int) SSL_IDMISMATCH, cert, url);
        }
        if (error == -201) {
            return new SslError((int) SSL_DATE_INVALID, cert, url);
        }
        if (error == -202) {
            return new SslError((int) SSL_UNTRUSTED, cert, url);
        }
        return new SslError((int) SSL_INVALID, cert, url);
    }

    public SslCertificate getCertificate() {
        return this.mCertificate;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public boolean addError(int error) {
        boolean rval = -assertionsDisabled;
        if (error >= 0 && error < SSL_MAX_ERROR) {
            rval = true;
        }
        if (rval) {
            this.mErrors = (SSL_EXPIRED << error) | this.mErrors;
        }
        return rval;
    }

    public boolean hasError(int error) {
        boolean rval = -assertionsDisabled;
        if (error >= 0 && error < SSL_MAX_ERROR) {
            rval = true;
        }
        if (!rval) {
            return rval;
        }
        return ((SSL_EXPIRED << error) & this.mErrors) != 0 ? true : -assertionsDisabled;
    }

    public int getPrimaryError() {
        if (this.mErrors != 0) {
            for (int error = SSL_INVALID; error >= 0; error--) {
                if ((this.mErrors & (SSL_EXPIRED << error)) != 0) {
                    return error;
                }
            }
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        return -1;
    }

    public String toString() {
        return "primary error: " + getPrimaryError() + " certificate: " + getCertificate() + " on URL: " + getUrl();
    }
}
