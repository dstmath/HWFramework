package com.android.org.conscrypt;

import com.android.org.conscrypt.OpenSSLX509CertificateFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.security.auth.x500.X500Principal;

final class OpenSSLX509CRL extends X509CRL {
    private final long mContext;
    private final Date nextUpdate = toDate(NativeCrypto.X509_CRL_get_nextUpdate(this.mContext, this));
    private final Date thisUpdate = toDate(NativeCrypto.X509_CRL_get_lastUpdate(this.mContext, this));

    private OpenSSLX509CRL(long ctx) throws OpenSSLX509CertificateFactory.ParsingException {
        this.mContext = ctx;
    }

    static Date toDate(long asn1time) throws OpenSSLX509CertificateFactory.ParsingException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(14, 0);
        NativeCrypto.ASN1_TIME_to_Calendar(asn1time, calendar);
        return calendar.getTime();
    }

    static OpenSSLX509CRL fromX509DerInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long crlCtx = NativeCrypto.d2i_X509_CRL_bio(bis.getBioContext());
            if (crlCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLX509CRL openSSLX509CRL = new OpenSSLX509CRL(crlCtx);
            bis.release();
            return openSSLX509CRL;
        } catch (Exception e) {
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    static List<OpenSSLX509CRL> fromPkcs7DerInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.d2i_PKCS7_bio(bis.getBioContext(), 2);
            bis.release();
            List<OpenSSLX509CRL> certs = new ArrayList<>(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509CRL(certRefs[i]));
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

    static OpenSSLX509CRL fromX509PemInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long crlCtx = NativeCrypto.PEM_read_bio_X509_CRL(bis.getBioContext());
            if (crlCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLX509CRL openSSLX509CRL = new OpenSSLX509CRL(crlCtx);
            bis.release();
            return openSSLX509CRL;
        } catch (Exception e) {
            throw new OpenSSLX509CertificateFactory.ParsingException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    static List<OpenSSLX509CRL> fromPkcs7PemInputStream(InputStream is) throws OpenSSLX509CertificateFactory.ParsingException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long[] certRefs = NativeCrypto.PEM_read_bio_PKCS7(bis.getBioContext(), 2);
            bis.release();
            List<OpenSSLX509CRL> certs = new ArrayList<>(certRefs.length);
            for (int i = 0; i < certRefs.length; i++) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509CRL(certRefs[i]));
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

    public Set<String> getCriticalExtensionOIDs() {
        String[] critOids = NativeCrypto.get_X509_CRL_ext_oids(this.mContext, this, 1);
        if (critOids.length == 0 && NativeCrypto.get_X509_CRL_ext_oids(this.mContext, this, 0).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(critOids));
    }

    public byte[] getExtensionValue(String oid) {
        return NativeCrypto.X509_CRL_get_ext_oid(this.mContext, this, oid);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        String[] nonCritOids = NativeCrypto.get_X509_CRL_ext_oids(this.mContext, this, 0);
        if (nonCritOids.length == 0 && NativeCrypto.get_X509_CRL_ext_oids(this.mContext, this, 1).length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(nonCritOids));
    }

    public boolean hasUnsupportedCriticalExtension() {
        for (String oid : NativeCrypto.get_X509_CRL_ext_oids(this.mContext, this, 1)) {
            if (NativeCrypto.X509_supported_extension(NativeCrypto.X509_CRL_get_ext(this.mContext, this, oid)) != 1) {
                return true;
            }
        }
        return false;
    }

    public byte[] getEncoded() throws CRLException {
        return NativeCrypto.i2d_X509_CRL(this.mContext, this);
    }

    private void verifyOpenSSL(OpenSSLKey pkey) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        NativeCrypto.X509_CRL_verify(this.mContext, this, pkey.getNativeRef());
    }

    private void verifyInternal(PublicKey key, String sigProvider) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        Signature sig;
        String sigAlg = getSigAlgName();
        if (sigAlg == null) {
            sigAlg = getSigAlgOID();
        }
        if (sigProvider == null) {
            sig = Signature.getInstance(sigAlg);
        } else {
            sig = Signature.getInstance(sigAlg, sigProvider);
        }
        sig.initVerify(key);
        sig.update(getTBSCertList());
        if (!sig.verify(getSignature())) {
            throw new SignatureException("signature did not verify");
        }
    }

    public void verify(PublicKey key) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if (key instanceof OpenSSLKeyHolder) {
            verifyOpenSSL(((OpenSSLKeyHolder) key).getOpenSSLKey());
        } else {
            verifyInternal(key, null);
        }
    }

    public void verify(PublicKey key, String sigProvider) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        verifyInternal(key, sigProvider);
    }

    public int getVersion() {
        return ((int) NativeCrypto.X509_CRL_get_version(this.mContext, this)) + 1;
    }

    public Principal getIssuerDN() {
        return getIssuerX500Principal();
    }

    public X500Principal getIssuerX500Principal() {
        return new X500Principal(NativeCrypto.X509_CRL_get_issuer_name(this.mContext, this));
    }

    public Date getThisUpdate() {
        return (Date) this.thisUpdate.clone();
    }

    public Date getNextUpdate() {
        return (Date) this.nextUpdate.clone();
    }

    public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
        long revokedRef = NativeCrypto.X509_CRL_get0_by_serial(this.mContext, this, serialNumber.toByteArray());
        if (revokedRef == 0) {
            return null;
        }
        try {
            return new OpenSSLX509CRLEntry(NativeCrypto.X509_REVOKED_dup(revokedRef));
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            return null;
        }
    }

    public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        if (!(certificate instanceof OpenSSLX509Certificate)) {
            return getRevokedCertificate(certificate.getSerialNumber());
        }
        OpenSSLX509Certificate osslCert = (OpenSSLX509Certificate) certificate;
        long x509RevokedRef = NativeCrypto.X509_CRL_get0_by_cert(this.mContext, this, osslCert.getContext(), osslCert);
        if (x509RevokedRef == 0) {
            return null;
        }
        try {
            return new OpenSSLX509CRLEntry(NativeCrypto.X509_REVOKED_dup(x509RevokedRef));
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            return null;
        }
    }

    public Set<? extends X509CRLEntry> getRevokedCertificates() {
        long[] entryRefs = NativeCrypto.X509_CRL_get_REVOKED(this.mContext, this);
        if (entryRefs == null || entryRefs.length == 0) {
            return null;
        }
        Set<OpenSSLX509CRLEntry> crlSet = new HashSet<>();
        for (long entryRef : entryRefs) {
            try {
                crlSet.add(new OpenSSLX509CRLEntry(entryRef));
            } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            }
        }
        return crlSet;
    }

    public byte[] getTBSCertList() throws CRLException {
        return NativeCrypto.get_X509_CRL_crl_enc(this.mContext, this);
    }

    public byte[] getSignature() {
        return NativeCrypto.get_X509_CRL_signature(this.mContext, this);
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
        return NativeCrypto.get_X509_CRL_sig_alg_oid(this.mContext, this);
    }

    public byte[] getSigAlgParams() {
        return NativeCrypto.get_X509_CRL_sig_alg_parameter(this.mContext, this);
    }

    public boolean isRevoked(Certificate cert) {
        OpenSSLX509Certificate osslCert;
        boolean z = false;
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        if (cert instanceof OpenSSLX509Certificate) {
            osslCert = (OpenSSLX509Certificate) cert;
        } else {
            try {
                osslCert = OpenSSLX509Certificate.fromX509DerInputStream(new ByteArrayInputStream(cert.getEncoded()));
            } catch (Exception e) {
                throw new RuntimeException("cannot convert certificate", e);
            }
        }
        if (NativeCrypto.X509_CRL_get0_by_cert(this.mContext, this, osslCert.getContext(), osslCert) != 0) {
            z = true;
        }
        return z;
    }

    public String toString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        long bioCtx = NativeCrypto.create_BIO_OutputStream(os);
        try {
            NativeCrypto.X509_CRL_print(bioCtx, this.mContext, this);
            return os.toString();
        } finally {
            NativeCrypto.BIO_free_all(bioCtx);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mContext != 0) {
                NativeCrypto.X509_CRL_free(this.mContext, this);
            }
        } finally {
            super.finalize();
        }
    }
}
