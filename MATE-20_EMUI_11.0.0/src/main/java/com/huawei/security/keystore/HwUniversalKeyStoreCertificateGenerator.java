package com.huawei.security.keystore;

import android.util.Log;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.X509CertificateObject;
import com.android.org.bouncycastle.x509.X509V3CertificateGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

public class HwUniversalKeyStoreCertificateGenerator {
    private static final Date DEFAULT_CERT_NOT_AFTER = new Date(2461449600000L);
    private static final Date DEFAULT_CERT_NOT_BEFORE = new Date(0);
    private static final BigInteger DEFAULT_CERT_SERIAL_NUMBER = new BigInteger("1");
    private static final X500Principal DEFAULT_CERT_SUBJECT = new X500Principal("CN=fake");
    public static final int KM_ALGORITHM_AES = 32;
    public static final int KM_ALGORITHM_EC = 3;
    public static final int KM_ALGORITHM_HMAC = 128;
    public static final int KM_ALGORITHM_RSA = 1;
    public static final String TAG = "HwKeyPairGenerator";
    private final Date mCertificateNotAfter;
    private final Date mCertificateNotBefore;
    private final BigInteger mCertificateSerialNumber;
    private final X500Principal mCertificateSubject;

    public HwUniversalKeyStoreCertificateGenerator(X500Principal certificateSubject, BigInteger certificateSerialNumber, Date certificateNotBefore, Date certificateNotAfter) {
        certificateSubject = certificateSubject == null ? DEFAULT_CERT_SUBJECT : certificateSubject;
        certificateNotBefore = certificateNotBefore == null ? DEFAULT_CERT_NOT_BEFORE : certificateNotBefore;
        certificateNotAfter = certificateNotAfter == null ? DEFAULT_CERT_NOT_AFTER : certificateNotAfter;
        certificateSerialNumber = certificateSerialNumber == null ? DEFAULT_CERT_SERIAL_NUMBER : certificateSerialNumber;
        if (!certificateNotAfter.before(certificateNotBefore)) {
            this.mCertificateSubject = certificateSubject;
            this.mCertificateSerialNumber = certificateSerialNumber;
            this.mCertificateNotBefore = (Date) certificateNotBefore.clone();
            this.mCertificateNotAfter = (Date) certificateNotAfter.clone();
            return;
        }
        throw new IllegalArgumentException("certificateNotAfter < certificateNotBefore");
    }

    public X509Certificate generateCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws CertificateEncodingException {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        try {
            certGen.setPublicKey(publicKey);
            certGen.setSerialNumber(this.mCertificateSerialNumber);
            certGen.setSubjectDN(this.mCertificateSubject);
            certGen.setIssuerDN(this.mCertificateSubject);
            certGen.setNotBefore(this.mCertificateNotBefore);
            certGen.setNotAfter(this.mCertificateNotAfter);
            certGen.setSignatureAlgorithm(signatureAlgorithm);
            return certGen.generate(privateKey);
        } catch (IllegalArgumentException | IllegalStateException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | CertificateEncodingException e) {
            throw new CertificateEncodingException("Generate certificate with valid signature failed!" + e.getMessage());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005d, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0062, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0063, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        throw r6;
     */
    public X509Certificate generateCertificateWithFakeSignature(PublicKey publicKey, int keymasterAlgorithm) throws IOException, CertificateParsingException {
        byte[] signature;
        ASN1ObjectIdentifier sigAlgOid;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        if (keymasterAlgorithm == 1) {
            signature = new byte[1];
            sigAlgOid = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        } else if (keymasterAlgorithm == 3) {
            sigAlgOid = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
            signature = new DERSequence().getEncoded();
        } else {
            throw new ProviderException("Unsupported key algorithm: " + keymasterAlgorithm);
        }
        try {
            ASN1InputStream publicKeyInfoIn = new ASN1InputStream(publicKey.getEncoded());
            tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn.readObject()));
            publicKeyInfoIn.close();
        } catch (IOException e) {
            Log.e(TAG, "try-with-resources close resources failed!");
        }
        tbsGenerator.setSerialNumber(new ASN1Integer(this.mCertificateSerialNumber));
        X509Principal subject = new X509Principal(this.mCertificateSubject.getEncoded());
        tbsGenerator.setSubject(subject);
        tbsGenerator.setIssuer(subject);
        tbsGenerator.setStartDate(new Time(this.mCertificateNotBefore));
        tbsGenerator.setEndDate(new Time(this.mCertificateNotAfter));
        tbsGenerator.setSignature(sigAlgOid);
        TBSCertificate tbsCertificate = tbsGenerator.generateTBSCertificate();
        ASN1EncodableVector result = new ASN1EncodableVector();
        result.add(tbsCertificate);
        result.add(sigAlgOid);
        result.add(new DERBitString(signature));
        return new X509CertificateObject(Certificate.getInstance(new DERSequence(result)));
    }
}
