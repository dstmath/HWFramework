package java.security.cert;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class CertificateFactory {
    private CertificateFactorySpi certFacSpi;
    private Provider provider;
    private String type;

    protected CertificateFactory(CertificateFactorySpi certFacSpi2, Provider provider2, String type2) {
        this.certFacSpi = certFacSpi2;
        this.provider = provider2;
        this.type = type2;
    }

    public static final CertificateFactory getInstance(String type2) throws CertificateException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", (Class<?>) CertificateFactorySpi.class, type2);
            return new CertificateFactory((CertificateFactorySpi) instance.impl, instance.provider, type2);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type2 + " not found", e);
        }
    }

    public static final CertificateFactory getInstance(String type2, String provider2) throws CertificateException, NoSuchProviderException {
        try {
            Providers.checkBouncyCastleDeprecation(provider2, "CertificateFactory", type2);
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", (Class<?>) CertificateFactorySpi.class, type2, provider2);
            return new CertificateFactory((CertificateFactorySpi) instance.impl, instance.provider, type2);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type2 + " not found", e);
        }
    }

    public static final CertificateFactory getInstance(String type2, Provider provider2) throws CertificateException {
        try {
            Providers.checkBouncyCastleDeprecation(provider2, "CertificateFactory", type2);
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory", (Class<?>) CertificateFactorySpi.class, type2, provider2);
            return new CertificateFactory((CertificateFactorySpi) instance.impl, instance.provider, type2);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type2 + " not found", e);
        }
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getType() {
        return this.type;
    }

    public final Certificate generateCertificate(InputStream inStream) throws CertificateException {
        return this.certFacSpi.engineGenerateCertificate(inStream);
    }

    public final Iterator<String> getCertPathEncodings() {
        return this.certFacSpi.engineGetCertPathEncodings();
    }

    public final CertPath generateCertPath(InputStream inStream) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(inStream);
    }

    public final CertPath generateCertPath(InputStream inStream, String encoding) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(inStream, encoding);
    }

    public final CertPath generateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        return this.certFacSpi.engineGenerateCertPath(certificates);
    }

    public final Collection<? extends Certificate> generateCertificates(InputStream inStream) throws CertificateException {
        return this.certFacSpi.engineGenerateCertificates(inStream);
    }

    public final CRL generateCRL(InputStream inStream) throws CRLException {
        return this.certFacSpi.engineGenerateCRL(inStream);
    }

    public final Collection<? extends CRL> generateCRLs(InputStream inStream) throws CRLException {
        return this.certFacSpi.engineGenerateCRLs(inStream);
    }
}
