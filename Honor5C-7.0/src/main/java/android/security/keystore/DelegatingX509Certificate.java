package android.security.keystore;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

class DelegatingX509Certificate extends X509Certificate {
    private final X509Certificate mDelegate;

    DelegatingX509Certificate(X509Certificate delegate) {
        this.mDelegate = delegate;
    }

    public Set<String> getCriticalExtensionOIDs() {
        return this.mDelegate.getCriticalExtensionOIDs();
    }

    public byte[] getExtensionValue(String oid) {
        return this.mDelegate.getExtensionValue(oid);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        return this.mDelegate.getNonCriticalExtensionOIDs();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.mDelegate.hasUnsupportedCriticalExtension();
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        this.mDelegate.checkValidity();
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        this.mDelegate.checkValidity(date);
    }

    public int getBasicConstraints() {
        return this.mDelegate.getBasicConstraints();
    }

    public Principal getIssuerDN() {
        return this.mDelegate.getIssuerDN();
    }

    public boolean[] getIssuerUniqueID() {
        return this.mDelegate.getIssuerUniqueID();
    }

    public boolean[] getKeyUsage() {
        return this.mDelegate.getKeyUsage();
    }

    public Date getNotAfter() {
        return this.mDelegate.getNotAfter();
    }

    public Date getNotBefore() {
        return this.mDelegate.getNotBefore();
    }

    public BigInteger getSerialNumber() {
        return this.mDelegate.getSerialNumber();
    }

    public String getSigAlgName() {
        return this.mDelegate.getSigAlgName();
    }

    public String getSigAlgOID() {
        return this.mDelegate.getSigAlgOID();
    }

    public byte[] getSigAlgParams() {
        return this.mDelegate.getSigAlgParams();
    }

    public byte[] getSignature() {
        return this.mDelegate.getSignature();
    }

    public Principal getSubjectDN() {
        return this.mDelegate.getSubjectDN();
    }

    public boolean[] getSubjectUniqueID() {
        return this.mDelegate.getSubjectUniqueID();
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return this.mDelegate.getTBSCertificate();
    }

    public int getVersion() {
        return this.mDelegate.getVersion();
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return this.mDelegate.getEncoded();
    }

    public PublicKey getPublicKey() {
        return this.mDelegate.getPublicKey();
    }

    public String toString() {
        return this.mDelegate.toString();
    }

    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mDelegate.verify(key);
    }

    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mDelegate.verify(key, sigProvider);
    }

    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        return this.mDelegate.getExtendedKeyUsage();
    }

    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return this.mDelegate.getIssuerAlternativeNames();
    }

    public X500Principal getIssuerX500Principal() {
        return this.mDelegate.getIssuerX500Principal();
    }

    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return this.mDelegate.getSubjectAlternativeNames();
    }

    public X500Principal getSubjectX500Principal() {
        return this.mDelegate.getSubjectX500Principal();
    }
}
