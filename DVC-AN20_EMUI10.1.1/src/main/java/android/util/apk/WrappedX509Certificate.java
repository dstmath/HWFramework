package android.util.apk;

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
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/* access modifiers changed from: package-private */
public class WrappedX509Certificate extends X509Certificate {
    private final X509Certificate mWrapped;

    WrappedX509Certificate(X509Certificate wrapped) {
        this.mWrapped = wrapped;
    }

    @Override // java.security.cert.X509Extension
    public Set<String> getCriticalExtensionOIDs() {
        return this.mWrapped.getCriticalExtensionOIDs();
    }

    public byte[] getExtensionValue(String oid) {
        return this.mWrapped.getExtensionValue(oid);
    }

    @Override // java.security.cert.X509Extension
    public Set<String> getNonCriticalExtensionOIDs() {
        return this.mWrapped.getNonCriticalExtensionOIDs();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.mWrapped.hasUnsupportedCriticalExtension();
    }

    @Override // java.security.cert.X509Certificate
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        this.mWrapped.checkValidity();
    }

    @Override // java.security.cert.X509Certificate
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        this.mWrapped.checkValidity(date);
    }

    public int getVersion() {
        return this.mWrapped.getVersion();
    }

    public BigInteger getSerialNumber() {
        return this.mWrapped.getSerialNumber();
    }

    public Principal getIssuerDN() {
        return this.mWrapped.getIssuerDN();
    }

    public Principal getSubjectDN() {
        return this.mWrapped.getSubjectDN();
    }

    public Date getNotBefore() {
        return this.mWrapped.getNotBefore();
    }

    public Date getNotAfter() {
        return this.mWrapped.getNotAfter();
    }

    @Override // java.security.cert.X509Certificate
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return this.mWrapped.getTBSCertificate();
    }

    public byte[] getSignature() {
        return this.mWrapped.getSignature();
    }

    public String getSigAlgName() {
        return this.mWrapped.getSigAlgName();
    }

    public String getSigAlgOID() {
        return this.mWrapped.getSigAlgOID();
    }

    public byte[] getSigAlgParams() {
        return this.mWrapped.getSigAlgParams();
    }

    public boolean[] getIssuerUniqueID() {
        return this.mWrapped.getIssuerUniqueID();
    }

    public boolean[] getSubjectUniqueID() {
        return this.mWrapped.getSubjectUniqueID();
    }

    public boolean[] getKeyUsage() {
        return this.mWrapped.getKeyUsage();
    }

    public int getBasicConstraints() {
        return this.mWrapped.getBasicConstraints();
    }

    @Override // java.security.cert.Certificate
    public byte[] getEncoded() throws CertificateEncodingException {
        return this.mWrapped.getEncoded();
    }

    @Override // java.security.cert.Certificate
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mWrapped.verify(key);
    }

    @Override // java.security.cert.Certificate
    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.mWrapped.verify(key, sigProvider);
    }

    public String toString() {
        return this.mWrapped.toString();
    }

    public PublicKey getPublicKey() {
        return this.mWrapped.getPublicKey();
    }
}
