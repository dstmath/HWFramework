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
import sun.security.jca.GetInstance.Instance;

public class CertStore {
    private static final String CERTSTORE_TYPE = "certstore.type";
    private CertStoreParameters params;
    private Provider provider;
    private CertStoreSpi storeSpi;
    private String type;

    protected CertStore(CertStoreSpi storeSpi, Provider provider, String type, CertStoreParameters params) {
        this.storeSpi = storeSpi;
        this.provider = provider;
        this.type = type;
        if (params != null) {
            this.params = (CertStoreParameters) params.clone();
        }
    }

    public final Collection<? extends Certificate> getCertificates(CertSelector selector) throws CertStoreException {
        return this.storeSpi.engineGetCertificates(selector);
    }

    public final Collection<? extends CRL> getCRLs(CRLSelector selector) throws CertStoreException {
        return this.storeSpi.engineGetCRLs(selector);
    }

    public static CertStore getInstance(String type, CertStoreParameters params) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        try {
            Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, (Object) params);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
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

    public static CertStore getInstance(String type, CertStoreParameters params, String provider) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        try {
            Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, (Object) params, provider);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    public static CertStore getInstance(String type, CertStoreParameters params, Provider provider) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        try {
            Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, (Object) params, provider);
            return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    public final CertStoreParameters getCertStoreParameters() {
        return this.params == null ? null : (CertStoreParameters) this.params.clone();
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
