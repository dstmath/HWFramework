package com.android.org.conscrypt;

import com.android.org.conscrypt.OpenSSLX509CertificateFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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

public final class OpenSSLX509Certificate extends X509Certificate {
    private static final long serialVersionUID = 1992239142393372128L;
    private final transient long mContext;
    private transient Integer mHashCode;
    private final Date notAfter;
    private final Date notBefore;

    OpenSSLX509Certificate(long ctx) throws OpenSSLX509CertificateFactory.ParsingException {
        this.mContext = ctx;
        this.notBefore = toDate(NativeCrypto.X509_get_notBefore(this.mContext, this));
        this.notAfter = toDate(NativeCrypto.X509_get_notAfter(this.mContext, this));
    }

    private OpenSSLX509Certificate(long ctx, Date notBefore2, Date notAfter2) {
        this.mContext = ctx;
        this.notBefore = notBefore2;
        this.notAfter = notAfter2;
    }

    private static Date toDate(long asn1time) throws OpenSSLX509CertificateFactory.ParsingException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(14, 0);
        NativeCrypto.ASN1_TIME_to_Calendar(asn1time, calendar);
        return calendar.getTime();
    }

    public static OpenSSLX509Certificate fromX509DerInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
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
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    public static OpenSSLX509Certificate fromX509Der(byte[] encoded) throws CertificateEncodingException {
        try {
            return new OpenSSLX509Certificate(NativeCrypto.d2i_X509(encoded));
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            throw new CertificateEncodingException(e);
        }
    }

    public static List<OpenSSLX509Certificate> fromPkcs7DerInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.d2i_PKCS7_bio(bis.getBioContext(), 1);
            bis.release();
            if (certRefs == null) {
                return Collections.emptyList();
            }
            List<OpenSSLX509Certificate> certs = new ArrayList<>(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509Certificate(certRefs[i]));
                }
            }
            return certs;
        } catch (Exception e) {
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    public static OpenSSLX509Certificate fromX509PemInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
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
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    public static List<OpenSSLX509Certificate> fromPkcs7PemInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.PEM_read_bio_PKCS7(bis.getBioContext(), 1);
            bis.release();
            List<OpenSSLX509Certificate> certs = new ArrayList<>(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509Certificate(certRefs[i]));
                }
            }
            return certs;
        } catch (Exception e) {
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
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
        String[] critOids = NativeCrypto.get_X509_ext_oids(this.mContext, this, 1);
        if (critOids.length == 0 && NativeCrypto.get_X509_ext_oids(this.mContext, this, 0).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(critOids));
    }

    public byte[] getExtensionValue(String oid) {
        return NativeCrypto.X509_get_ext_oid(this.mContext, this, oid);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        String[] nonCritOids = NativeCrypto.get_X509_ext_oids(this.mContext, this, 0);
        if (nonCritOids.length == 0 && NativeCrypto.get_X509_ext_oids(this.mContext, this, 1).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(nonCritOids));
    }

    public boolean hasUnsupportedCriticalExtension() {
        return (NativeCrypto.get_X509_ex_flags(this.mContext, this) & 512) != 0;
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
        return ((int) NativeCrypto.X509_get_version(this.mContext, this)) + 1;
    }

    public BigInteger getSerialNumber() {
        return new BigInteger(NativeCrypto.X509_get_serialNumber(this.mContext, this));
    }

    public Principal getIssuerDN() {
        return getIssuerX500Principal();
    }

    public Principal getSubjectDN() {
        return getSubjectX500Principal();
    }

    public Date getNotBefore() {
        return (Date) this.notBefore.clone();
    }

    public Date getNotAfter() {
        return (Date) this.notAfter.clone();
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return NativeCrypto.get_X509_cert_info_enc(this.mContext, this);
    }

    public byte[] getSignature() {
        return NativeCrypto.get_X509_signature(this.mContext, this);
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
        return NativeCrypto.get_X509_sig_alg_oid(this.mContext, this);
    }

    public byte[] getSigAlgParams() {
        return NativeCrypto.get_X509_sig_alg_parameter(this.mContext, this);
    }

    public boolean[] getIssuerUniqueID() {
        return NativeCrypto.get_X509_issuerUID(this.mContext, this);
    }

    public boolean[] getSubjectUniqueID() {
        return NativeCrypto.get_X509_subjectUID(this.mContext, this);
    }

    public boolean[] getKeyUsage() {
        boolean[] kusage = NativeCrypto.get_X509_ex_kusage(this.mContext, this);
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
        if ((NativeCrypto.get_X509_ex_flags(this.mContext, this) & 16) == 0) {
            return -1;
        }
        int pathLen = NativeCrypto.get_X509_ex_pathlen(this.mContext, this);
        if (pathLen == -1) {
            return Integer.MAX_VALUE;
        }
        return pathLen;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return NativeCrypto.i2d_X509(this.mContext, this);
    }

    private void verifyOpenSSL(OpenSSLKey pkey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            NativeCrypto.X509_verify(this.mContext, this, pkey.getNativeRef());
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
            verifyInternal(key, null);
        }
    }

    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        verifyInternal(key, sigProvider);
    }

    public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig;
        if (!(key instanceof OpenSSLKeyHolder) || !(sigProvider instanceof OpenSSLProvider)) {
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
            return;
        }
        verifyOpenSSL(((OpenSSLKeyHolder) key).getOpenSSLKey());
    }

    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        long bioCtx = NativeCrypto.create_BIO_OutputStream(os);
        try {
            NativeCrypto.X509_print_ex(bioCtx, this.mContext, this, 0, 0);
            return os.toString();
        } finally {
            NativeCrypto.BIO_free_all(bioCtx);
        }
    }

    public PublicKey getPublicKey() {
        try {
            return new OpenSSLKey(NativeCrypto.X509_get_pubkey(this.mContext, this)).getPublicKey();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            String oid = NativeCrypto.get_X509_pubkey_oid(this.mContext, this);
            byte[] encoded = NativeCrypto.i2d_X509_PUBKEY(this.mContext, this);
            try {
                return KeyFactory.getInstance(oid).generatePublic(new X509EncodedKeySpec(encoded));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e2) {
                return new X509PublicKey(oid, encoded);
            }
        }
    }

    public X500Principal getIssuerX500Principal() {
        return new X500Principal(NativeCrypto.X509_get_issuer_name(this.mContext, this));
    }

    public X500Principal getSubjectX500Principal() {
        return new X500Principal(NativeCrypto.X509_get_subject_name(this.mContext, this));
    }

    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        String[] extUsage = NativeCrypto.get_X509_ex_xkusage(this.mContext, this);
        if (extUsage == null) {
            return null;
        }
        return Arrays.asList(extUsage);
    }

    private static Collection<List<?>> alternativeNameArrayToList(Object[][] altNameArray) {
        if (altNameArray == null) {
            return null;
        }
        Collection<List<?>> coll = new ArrayList<>(altNameArray.length);
        for (Object[] asList : altNameArray) {
            coll.add(Collections.unmodifiableList(Arrays.asList(asList)));
        }
        return Collections.unmodifiableCollection(coll);
    }

    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return alternativeNameArrayToList(NativeCrypto.get_X509_GENERAL_NAME_stack(this.mContext, this, 1));
    }

    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return alternativeNameArrayToList(NativeCrypto.get_X509_GENERAL_NAME_stack(this.mContext, this, 2));
    }

    public boolean equals(Object other) {
        if (!(other instanceof OpenSSLX509Certificate)) {
            return super.equals(other);
        }
        OpenSSLX509Certificate o = (OpenSSLX509Certificate) other;
        return NativeCrypto.X509_cmp(this.mContext, this, o.mContext, o) == 0;
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
        OpenSSLX509Certificate copy = new OpenSSLX509Certificate(NativeCrypto.X509_dup(this.mContext, this), this.notBefore, this.notAfter);
        NativeCrypto.X509_delete_ext(copy.getContext(), copy, oid);
        return copy;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mContext != 0) {
                NativeCrypto.X509_free(this.mContext, this);
            }
        } finally {
            super.finalize();
        }
    }
}
