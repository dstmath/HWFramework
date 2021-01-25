package android.net.http;

import android.security.net.config.UserCertificateSource;
import com.android.org.conscrypt.TrustManagerImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.X509TrustManager;

public class X509TrustManagerExtensions {
    private final Method mCheckServerTrusted;
    private final TrustManagerImpl mDelegate;
    private final Method mIsSameTrustConfiguration;
    private final X509TrustManager mTrustManager;

    public X509TrustManagerExtensions(X509TrustManager tm) throws IllegalArgumentException {
        if (tm instanceof TrustManagerImpl) {
            this.mDelegate = (TrustManagerImpl) tm;
            this.mTrustManager = null;
            this.mCheckServerTrusted = null;
            this.mIsSameTrustConfiguration = null;
            return;
        }
        this.mDelegate = null;
        this.mTrustManager = tm;
        try {
            this.mCheckServerTrusted = tm.getClass().getMethod("checkServerTrusted", X509Certificate[].class, String.class, String.class);
            Method isSameTrustConfiguration = null;
            try {
                isSameTrustConfiguration = tm.getClass().getMethod("isSameTrustConfiguration", String.class, String.class);
            } catch (ReflectiveOperationException e) {
            }
            this.mIsSameTrustConfiguration = isSameTrustConfiguration;
        } catch (NoSuchMethodException e2) {
            throw new IllegalArgumentException("Required method checkServerTrusted(X509Certificate[], String, String, String) missing");
        }
    }

    public List<X509Certificate> checkServerTrusted(X509Certificate[] chain, String authType, String host) throws CertificateException {
        TrustManagerImpl trustManagerImpl = this.mDelegate;
        if (trustManagerImpl != null) {
            return trustManagerImpl.checkServerTrusted(chain, authType, host);
        }
        try {
            return (List) this.mCheckServerTrusted.invoke(this.mTrustManager, chain, authType, host);
        } catch (IllegalAccessException e) {
            throw new CertificateException("Failed to call checkServerTrusted", e);
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof CertificateException) {
                throw ((CertificateException) e2.getCause());
            } else if (e2.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e2.getCause());
            } else {
                throw new CertificateException("checkServerTrusted failed", e2.getCause());
            }
        }
    }

    public boolean isUserAddedCertificate(X509Certificate cert) {
        return UserCertificateSource.getInstance().findBySubjectAndPublicKey(cert) != null;
    }

    public boolean isSameTrustConfiguration(String hostname1, String hostname2) {
        Method method = this.mIsSameTrustConfiguration;
        if (method == null) {
            return true;
        }
        try {
            return ((Boolean) method.invoke(this.mTrustManager, hostname1, hostname2)).booleanValue();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to call isSameTrustConfiguration", e);
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e2.getCause());
            }
            throw new RuntimeException("isSameTrustConfiguration failed", e2.getCause());
        }
    }
}
