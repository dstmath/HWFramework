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

    @Override // java.security.cert.X509Extension
    public Set<String> getCriticalExtensionOIDs() {
        return this.mDelegate.getCriticalExtensionOIDs();
    }

    @Override // java.security.cert.X509Extension
    public byte[] getExtensionValue(String oid) {
        return this.mDelegate.getExtensionValue(oid);
    }

    @Override // java.security.cert.X509Extension
    public Set<String> getNonCriticalExtensionOIDs() {
        return this.mDelegate.getNonCriticalExtensionOIDs();
    }

    @Override // java.security.cert.X509Extension
    public boolean hasUnsupportedCriticalExtension() {
        return this.mDelegate.hasUnsupportedCriticalExtension();
    }

    @Override // java.security.cert.X509Certificate
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        this.mDelegate.checkValidity();
    }

    @Override // java.security.cert.X509Certificate
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        this.mDelegate.checkValidity(date);
    }

    @Override // java.security.cert.X509Certificate
    public int getBasicConstraints() {
        return this.mDelegate.getBasicConstraints();
    }

    @Override // java.security.cert.X509Certificate
    public Principal getIssuerDN() {
        return this.mDelegate.getIssuerDN();
    }

    @Override // java.security.cert.X509Certificate
    public boolean[] getIssuerUniqueID() {
        return this.mDelegate.getIssuerUniqueID();
    }

    @Override // java.security.cert.X509Certificate
    public boolean[] getKeyUsage() {
        return this.mDelegate.getKeyUsage();
    }

    @Override // java.security.cert.X509Certificate
    public Date getNotAfter() {
        return this.mDelegate.getNotAfter();
    }

    @Override // java.security.cert.X509Certificate
    public Date getNotBefore() {
        return this.mDelegate.getNotBefore();
    }

    @Override // java.security.cert.X509Certificate
    public BigInteger getSerialNumber() {
        return this.mDelegate.getSerialNumber();
    }

    @Override // java.security.cert.X509Certificate
    public String getSigAlgName() {
        return this.mDelegate.getSigAlgName();
    }

    @Override // java.security.cert.X509Certificate
    public String getSigAlgOID() {
        return this.mDelegate.getSigAlgOID();
    }

    @Override // java.security.cert.X509Certificate
    public byte[] getSigAlgParams() {
        return this.mDelegate.getSigAlgParams();
    }

    @Override // java.security.cert.X509Certificate
    public byte[] getSignature() {
        return this.mDelegate.getSignature();
    }

    @Override // java.security.cert.X509Certificate
    public Principal getSubjectDN() {
        return this.mDelegate.getSubjectDN();
    }

    @Override // java.security.cert.X509Certificate
    public boolean[] getSubjectUniqueID() {
        return this.mDelegate.getSubjectUniqueID();
    }

    @Override // java.security.cert.X509Certificate
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return this.mDelegate.getTBSCertificate();
    }

    @Override // java.security.cert.X509Certificate
    public int getVersion() {
        return this.mDelegate.getVersion();
    }

    @Override // java.security.cert.Certificate
    public byte[] getEncoded() throws CertificateEncodingException {
        return this.mDelegate.getEncoded();
    }

    @Override // java.security.cert.Certificate
    public PublicKey getPublicKey() {
        return this.mDelegate.getPublicKey();
    }

    @Override // java.security.cert.Certificate, java.lang.Object
    public String toString() {
        return this.mDelegate.toString();
    }

    @Override // java.security.cert.Certificate
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mDelegate.verify(key);
    }

    @Override // java.security.cert.Certificate
    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mDelegate.verify(key, sigProvider);
    }

    @Override // java.security.cert.X509Certificate
    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        return this.mDelegate.getExtendedKeyUsage();
    }

    @Override // java.security.cert.X509Certificate
    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return this.mDelegate.getIssuerAlternativeNames();
    }

    @Override // java.security.cert.X509Certificate
    public X500Principal getIssuerX500Principal() {
        return this.mDelegate.getIssuerX500Principal();
    }

    @Override // java.security.cert.X509Certificate
    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return this.mDelegate.getSubjectAlternativeNames();
    }

    @Override // java.security.cert.X509Certificate
    public X500Principal getSubjectX500Principal() {
        return this.mDelegate.getSubjectX500Principal();
    }
}
