package org.bouncycastle.openssl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;

public class MiscPEMGenerator implements PemObjectGenerator {
    private static final ASN1ObjectIdentifier[] dsaOids = {X9ObjectIdentifiers.id_dsa, OIWObjectIdentifiers.dsaWithSHA1};
    private static final byte[] hexEncodingTable = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
    private final PEMEncryptor encryptor;
    private final Object obj;

    public MiscPEMGenerator(Object obj2) {
        this.obj = obj2;
        this.encryptor = null;
    }

    public MiscPEMGenerator(Object obj2, PEMEncryptor pEMEncryptor) {
        this.obj = obj2;
        this.encryptor = pEMEncryptor;
    }

    private PemObject createPemObject(Object obj2) throws IOException {
        byte[] bArr;
        String str;
        if (obj2 instanceof PemObject) {
            return (PemObject) obj2;
        }
        if (obj2 instanceof PemObjectGenerator) {
            return ((PemObjectGenerator) obj2).generate();
        }
        if (obj2 instanceof X509CertificateHolder) {
            str = "CERTIFICATE";
            bArr = ((X509CertificateHolder) obj2).getEncoded();
        } else if (obj2 instanceof X509CRLHolder) {
            str = "X509 CRL";
            bArr = ((X509CRLHolder) obj2).getEncoded();
        } else if (obj2 instanceof X509TrustedCertificateBlock) {
            str = "TRUSTED CERTIFICATE";
            bArr = ((X509TrustedCertificateBlock) obj2).getEncoded();
        } else if (obj2 instanceof PrivateKeyInfo) {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) obj2;
            ASN1ObjectIdentifier algorithm = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
            if (algorithm.equals(PKCSObjectIdentifiers.rsaEncryption)) {
                str = "RSA PRIVATE KEY";
            } else if (algorithm.equals(dsaOids[0]) || algorithm.equals(dsaOids[1])) {
                str = "DSA PRIVATE KEY";
                DSAParameter instance = DSAParameter.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                aSN1EncodableVector.add(new ASN1Integer(0));
                aSN1EncodableVector.add(new ASN1Integer(instance.getP()));
                aSN1EncodableVector.add(new ASN1Integer(instance.getQ()));
                aSN1EncodableVector.add(new ASN1Integer(instance.getG()));
                BigInteger value = ASN1Integer.getInstance(privateKeyInfo.parsePrivateKey()).getValue();
                aSN1EncodableVector.add(new ASN1Integer(instance.getG().modPow(value, instance.getP())));
                aSN1EncodableVector.add(new ASN1Integer(value));
                bArr = new DERSequence(aSN1EncodableVector).getEncoded();
            } else if (algorithm.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
                str = "EC PRIVATE KEY";
            } else {
                throw new IOException("Cannot identify private key");
            }
            bArr = privateKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } else if (obj2 instanceof SubjectPublicKeyInfo) {
            str = "PUBLIC KEY";
            bArr = ((SubjectPublicKeyInfo) obj2).getEncoded();
        } else if (obj2 instanceof X509AttributeCertificateHolder) {
            str = "ATTRIBUTE CERTIFICATE";
            bArr = ((X509AttributeCertificateHolder) obj2).getEncoded();
        } else if (obj2 instanceof PKCS10CertificationRequest) {
            str = "CERTIFICATE REQUEST";
            bArr = ((PKCS10CertificationRequest) obj2).getEncoded();
        } else if (obj2 instanceof PKCS8EncryptedPrivateKeyInfo) {
            str = "ENCRYPTED PRIVATE KEY";
            bArr = ((PKCS8EncryptedPrivateKeyInfo) obj2).getEncoded();
        } else if (obj2 instanceof ContentInfo) {
            str = "PKCS7";
            bArr = ((ContentInfo) obj2).getEncoded();
        } else {
            throw new PemGenerationException("unknown object passed - can't encode.");
        }
        if (this.encryptor == null) {
            return new PemObject(str, bArr);
        }
        String upperCase = Strings.toUpperCase(this.encryptor.getAlgorithm());
        if (upperCase.equals("DESEDE")) {
            upperCase = "DES-EDE3-CBC";
        }
        byte[] iv = this.encryptor.getIV();
        byte[] encrypt = this.encryptor.encrypt(bArr);
        ArrayList arrayList = new ArrayList(2);
        arrayList.add(new PemHeader("Proc-Type", "4,ENCRYPTED"));
        arrayList.add(new PemHeader("DEK-Info", upperCase + "," + getHexEncoded(iv)));
        return new PemObject(str, arrayList, encrypt);
    }

    private String getHexEncoded(byte[] bArr) throws IOException {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i != bArr.length; i++) {
            byte b = bArr[i] & 255;
            int i2 = 2 * i;
            cArr[i2] = (char) hexEncodingTable[b >>> 4];
            cArr[i2 + 1] = (char) hexEncodingTable[b & 15];
        }
        return new String(cArr);
    }

    public PemObject generate() throws PemGenerationException {
        try {
            return createPemObject(this.obj);
        } catch (IOException e) {
            throw new PemGenerationException("encoding exception: " + e.getMessage(), e);
        }
    }
}
