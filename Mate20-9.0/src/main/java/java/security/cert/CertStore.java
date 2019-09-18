package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import sun.security.jca.GetInstance;

public class CertStore {
    private static final String CERTSTORE_TYPE = "certstore.type";
    private CertStoreParameters params;
    private Provider provider;
    private CertStoreSpi storeSpi;
    private String type;

    protected CertStore(CertStoreSpi storeSpi2, Provider provider2, String type2, CertStoreParameters params2) {
        this.storeSpi = storeSpi2;
        this.provider = provider2;
        this.type = type2;
        if (params2 != null) {
            this.params = (CertStoreParameters) params2.clone();
        }
    }

    public final Collection<? extends Certificate> getCertificates(CertSelector selector) throws CertStoreException {
        return this.storeSpi.engineGetCertificates(selector);
    }

    public final Collection<? extends CRL> getCRLs(CRLSelector selector) throws CertStoreException {
        return this.storeSpi.engineGetCRLs(selector);
    }

    public static CertStore getInstance(String type2, CertStoreParameters params2) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertStore", (Class<?>) CertStoreSpi.class, type2, (Object) params2);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type2, params2);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    private static CertStore handleException(NoSuchAlgorithmException e) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidAlgorithmParameterException) {
            throw ((InvalidAlgorithmParameterException) cause);
        }
        throw e;
    }

    public static CertStore getInstance(String type2, CertStoreParameters params2, String provider2) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertStore", (Class<?>) CertStoreSpi.class, type2, (Object) params2, provider2);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type2, params2);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    public static CertStore getInstance(String type2, CertStoreParameters params2, Provider provider2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertStore", (Class<?>) CertStoreSpi.class, type2, (Object) params2, provider2);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type2, params2);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    public final CertStoreParameters getCertStoreParameters() {
        if (this.params == null) {
            return null;
        }
        return (CertStoreParameters) this.params.clone();
    }

    public final String getType() {
        return this.type;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public static final String getDefaultType() {
        String cstype = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(CertStore.CERTSTORE_TYPE);
            }
        });
        if (cstype == null) {
            return "LDAP";
        }
        return cstype;
    }
}
