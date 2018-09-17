package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.crypto.BadPaddingException;
import javax.security.auth.x500.X500Principal;

public class OpenSSLX509Certificate extends X509Certificate {
    private static final long serialVersionUID = 1992239142393372128L;
    private final transient long mContext;
    private transient Integer mHashCode;

    OpenSSLX509Certificate(long ctx) {
        this.mContext = ctx;
    }

    public static OpenSSLX509Certificate fromX509DerInputStream(InputStream is) throws ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long certCtx = NativeCrypto.d2i_X509_bio(bis.getBioContext());
            if (certCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLX509Certificate openSSLX509Certificate = new OpenSSLX509Certificate(certCtx);
            bis.release();
            return openSSLX509Certificate;
        } catch (Exception e) {
            throw new ParsingException(e);
        } catch (Throwable th) {
            bis.release();
        }
    }

    public static OpenSSLX509Certificate fromX509Der(byte[] encoded) throws CertificateEncodingException {
        try {
            return new OpenSSLX509Certificate(NativeCrypto.d2i_X509(encoded));
        } catch (ParsingException e) {
            throw new CertificateEncodingException(e);
        }
    }

    public static List<OpenSSLX509Certificate> fromPkcs7DerInputStream(InputStream is) throws ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.d2i_PKCS7_bio(bis.getBioContext(), 1);
            bis.release();
            if (certRefs == null) {
                return Collections.emptyList();
            }
            List<org.conscrypt.OpenSSLX509Certificate> certs = new ArrayList(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509Certificate(certRefs[i]));
                }
            }
            return certs;
        } catch (Exception e) {
            throw new ParsingException(e);
        } catch (Throwable th) {
            bis.release();
        }
    }

    public static OpenSSLX509Certificate fromX509PemInputStream(InputStream is) throws ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long certCtx = NativeCrypto.PEM_read_bio_X509(bis.getBioContext());
            if (certCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLX509Certificate openSSLX509Certificate = new OpenSSLX509Certificate(certCtx);
            bis.release();
            return openSSLX509Certificate;
        } catch (Exception e) {
            throw new ParsingException(e);
        } catch (Throwable th) {
            bis.release();
        }
    }

    public static List<OpenSSLX509Certificate> fromPkcs7PemInputStream(InputStream is) throws ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.PEM_read_bio_PKCS7(bis.getBioContext(), 1);
            bis.release();
            List<org.conscrypt.OpenSSLX509Certificate> certs = new ArrayList(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509Certificate(certRefs[i]));
                }
            }
            return certs;
        } catch (Exception e) {
            throw new ParsingException(e);
        } catch (Throwable th) {
            bis.release();
        }
    }

    public static OpenSSLX509Certificate fromCertificate(Certificate cert) throws CertificateEncodingException {
        if (cert instanceof OpenSSLX509Certificate) {
            return (OpenSSLX509Certificate) cert;
        }
        if (cert instanceof X509Certificate) {
            return fromX509Der(cert.getEncoded());
        }
        throw new CertificateEncodingException("Only X.509 certificates are supported");
    }

    public Set<String> getCriticalExtensionOIDs() {
        String[] critOids = NativeCrypto.get_X509_ext_oids(this.mContext, 1);
        if (critOids.length == 0 && NativeCrypto.get_X509_ext_oids(this.mContext, 0).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(critOids));
    }

    public byte[] getExtensionValue(String oid) {
        return NativeCrypto.X509_get_ext_oid(this.mContext, oid);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        String[] nonCritOids = NativeCrypto.get_X509_ext_oids(this.mContext, 0);
        if (nonCritOids.length == 0 && NativeCrypto.get_X509_ext_oids(this.mContext, 1).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(nonCritOids));
    }

    public boolean hasUnsupportedCriticalExtension() {
        return (NativeCrypto.get_X509_ex_flags(this.mContext) & NativeConstants.EXFLAG_CRITICAL) != 0;
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        checkValidity(new Date());
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        if (getNotBefore().compareTo(date) > 0) {
            throw new CertificateNotYetValidException("Certificate not valid until " + getNotBefore().toString() + " (compared to " + date.toString() + ")");
        } else if (getNotAfter().compareTo(date) < 0) {
            throw new CertificateExpiredException("Certificate expired at " + getNotAfter().toString() + " (compared to " + date.toString() + ")");
        }
    }

    public int getVersion() {
        return ((int) NativeCrypto.X509_get_version(this.mContext)) + 1;
    }

    public BigInteger getSerialNumber() {
        return new BigInteger(NativeCrypto.X509_get_serialNumber(this.mContext));
    }

    public Principal getIssuerDN() {
        return getIssuerX500Principal();
    }

    public Principal getSubjectDN() {
        return getSubjectX500Principal();
    }

    public Date getNotBefore() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(14, 0);
        NativeCrypto.ASN1_TIME_to_Calendar(NativeCrypto.X509_get_notBefore(this.mContext), calendar);
        return calendar.getTime();
    }

    public Date getNotAfter() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(14, 0);
        NativeCrypto.ASN1_TIME_to_Calendar(NativeCrypto.X509_get_notAfter(this.mContext), calendar);
        return calendar.getTime();
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return NativeCrypto.get_X509_cert_info_enc(this.mContext);
    }

    public byte[] getSignature() {
        return NativeCrypto.get_X509_signature(this.mContext);
    }

    public String getSigAlgName() {
        String oid = getSigAlgOID();
        String algName = Platform.oidToAlgorithmName(oid);
        if (algName != null) {
            return algName;
        }
        return oid;
    }

    public String getSigAlgOID() {
        return NativeCrypto.get_X509_sig_alg_oid(this.mContext);
    }

    public byte[] getSigAlgParams() {
        return NativeCrypto.get_X509_sig_alg_parameter(this.mContext);
    }

    public boolean[] getIssuerUniqueID() {
        return NativeCrypto.get_X509_issuerUID(this.mContext);
    }

    public boolean[] getSubjectUniqueID() {
        return NativeCrypto.get_X509_subjectUID(this.mContext);
    }

    public boolean[] getKeyUsage() {
        boolean[] kusage = NativeCrypto.get_X509_ex_kusage(this.mContext);
        if (kusage == null) {
            return null;
        }
        if (kusage.length >= 9) {
            return kusage;
        }
        boolean[] resized = new boolean[9];
        System.arraycopy(kusage, 0, resized, 0, kusage.length);
        return resized;
    }

    public int getBasicConstraints() {
        if ((NativeCrypto.get_X509_ex_flags(this.mContext) & 16) == 0) {
            return -1;
        }
        int pathLen = NativeCrypto.get_X509_ex_pathlen(this.mContext);
        if (pathLen == -1) {
            return Integer.MAX_VALUE;
        }
        return pathLen;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return NativeCrypto.i2d_X509(this.mContext);
    }

    private void verifyOpenSSL(OpenSSLKey pkey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            NativeCrypto.X509_verify(this.mContext, pkey.getNativeRef());
        } catch (RuntimeException e) {
            throw new CertificateException(e);
        } catch (BadPaddingException e2) {
            throw new SignatureException();
        }
    }

    private void verifyInternal(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        Signature sig;
        if (sigProvider == null) {
            sig = Signature.getInstance(getSigAlgName());
        } else {
            sig = Signature.getInstance(getSigAlgName(), sigProvider);
        }
        sig.initVerify(key);
        sig.update(getTBSCertificate());
        if (!sig.verify(getSignature())) {
            throw new SignatureException("signature did not verify");
        }
    }

    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if (key instanceof OpenSSLKeyHolder) {
            verifyOpenSSL(((OpenSSLKeyHolder) key).getOpenSSLKey());
        } else {
            verifyInternal(key, (String) null);
        }
    }

    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        verifyInternal(key, sigProvider);
    }

    public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if ((key instanceof OpenSSLKeyHolder) && (sigProvider instanceof OpenSSLProvider)) {
            verifyOpenSSL(((OpenSSLKeyHolder) key).getOpenSSLKey());
            return;
        }
        Signature sig;
        if (sigProvider == null) {
            sig = Signature.getInstance(getSigAlgName());
        } else {
            sig = Signature.getInstance(getSigAlgName(), sigProvider);
        }
        sig.initVerify(key);
        sig.update(getTBSCertificate());
        if (!sig.verify(getSignature())) {
            throw new SignatureException("signature did not verify");
        }
    }

    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        long bioCtx = NativeCrypto.create_BIO_OutputStream(os);
        try {
            NativeCrypto.X509_print_ex(bioCtx, this.mContext, 0, 0);
            String byteArrayOutputStream = os.toString();
            return byteArrayOutputStream;
        } finally {
            NativeCrypto.BIO_free_all(bioCtx);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0010 A:{ExcHandler: java.security.NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002b A:{ExcHandler: java.security.NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter: B:5:0x001d} */
    /* JADX WARNING: Missing block: B:4:0x0011, code:
            r3 = com.android.org.conscrypt.NativeCrypto.get_X509_pubkey_oid(r8.mContext);
            r0 = com.android.org.conscrypt.NativeCrypto.i2d_X509_PUBKEY(r8.mContext);
     */
    /* JADX WARNING: Missing block: B:7:0x002a, code:
            return java.security.KeyFactory.getInstance(r3).generatePublic(new java.security.spec.X509EncodedKeySpec(r0));
     */
    /* JADX WARNING: Missing block: B:10:0x0031, code:
            return new com.android.org.conscrypt.X509PublicKey(r3, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PublicKey getPublicKey() {
        try {
            return new OpenSSLKey(NativeCrypto.X509_get_pubkey(this.mContext)).getPublicKey();
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public X500Principal getIssuerX500Principal() {
        return new X500Principal(NativeCrypto.X509_get_issuer_name(this.mContext));
    }

    public X500Principal getSubjectX500Principal() {
        return new X500Principal(NativeCrypto.X509_get_subject_name(this.mContext));
    }

    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        String[] extUsage = NativeCrypto.get_X509_ex_xkusage(this.mContext);
        if (extUsage == null) {
            return null;
        }
        return Arrays.asList(extUsage);
    }

    private static Collection<List<?>> alternativeNameArrayToList(Object[][] altNameArray) {
        if (altNameArray == null) {
            return null;
        }
        Collection<List<?>> coll = new ArrayList(altNameArray.length);
        for (Object[] asList : altNameArray) {
            coll.add(Collections.unmodifiableList(Arrays.asList(asList)));
        }
        return Collections.unmodifiableCollection(coll);
    }

    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return alternativeNameArrayToList(NativeCrypto.get_X509_GENERAL_NAME_stack(this.mContext, 1));
    }

    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return alternativeNameArrayToList(NativeCrypto.get_X509_GENERAL_NAME_stack(this.mContext, 2));
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof OpenSSLX509Certificate)) {
            return super.equals(other);
        }
        if (NativeCrypto.X509_cmp(this.mContext, ((OpenSSLX509Certificate) other).mContext) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        if (this.mHashCode != null) {
            return this.mHashCode.intValue();
        }
        this.mHashCode = Integer.valueOf(super.hashCode());
        return this.mHashCode.intValue();
    }

    public long getContext() {
        return this.mContext;
    }

    public OpenSSLX509Certificate withDeletedExtension(String oid) {
        OpenSSLX509Certificate copy = new OpenSSLX509Certificate(NativeCrypto.X509_dup(this.mContext));
        NativeCrypto.X509_delete_ext(copy.getContext(), oid);
        return copy;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mContext != 0) {
                NativeCrypto.X509_free(this.mContext);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    static OpenSSLX509Certificate[] createCertChain(long[] certificateRefs) {
        if (certificateRefs == null) {
            return null;
        }
        OpenSSLX509Certificate[] certificates = new OpenSSLX509Certificate[certificateRefs.length];
        for (int i = 0; i < certificateRefs.length; i++) {
            certificates[i] = new OpenSSLX509Certificate(certificateRefs[i]);
        }
        return certificates;
    }
}
