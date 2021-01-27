package org.bouncycastle.openssl;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectParser;
import org.bouncycastle.util.io.pem.PemReader;

public class PEMParser extends PemReader {
    private final Map parsers = new HashMap();

    private class DSAKeyPairParser implements PEMKeyPairParser {
        private DSAKeyPairParser() {
        }

        @Override // org.bouncycastle.openssl.PEMKeyPairParser
        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
                if (instance.size() == 6) {
                    ASN1Integer instance2 = ASN1Integer.getInstance(instance.getObjectAt(1));
                    ASN1Integer instance3 = ASN1Integer.getInstance(instance.getObjectAt(2));
                    ASN1Integer instance4 = ASN1Integer.getInstance(instance.getObjectAt(3));
                    return new PEMKeyPair(new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(instance2.getValue(), instance3.getValue(), instance4.getValue())), ASN1Integer.getInstance(instance.getObjectAt(4))), new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(instance2.getValue(), instance3.getValue(), instance4.getValue())), ASN1Integer.getInstance(instance.getObjectAt(5))));
                }
                throw new PEMException("malformed sequence in DSA private key");
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating DSA private key: " + e2.toString(), e2);
            }
        }
    }

    private class ECCurveParamsParser implements PemObjectParser {
        private ECCurveParamsParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                ASN1Primitive fromByteArray = ASN1Primitive.fromByteArray(pemObject.getContent());
                if (fromByteArray instanceof ASN1ObjectIdentifier) {
                    return ASN1Primitive.fromByteArray(pemObject.getContent());
                }
                if (fromByteArray instanceof ASN1Sequence) {
                    return X9ECParameters.getInstance(fromByteArray);
                }
                return null;
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("exception extracting EC named curve: " + e2.toString());
            }
        }
    }

    private class ECDSAKeyPairParser implements PEMKeyPairParser {
        private ECDSAKeyPairParser() {
        }

        @Override // org.bouncycastle.openssl.PEMKeyPairParser
        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ECPrivateKey instance = ECPrivateKey.getInstance(ASN1Sequence.getInstance(bArr));
                AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, instance.getParameters());
                PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algorithmIdentifier, instance);
                return instance.getPublicKey() != null ? new PEMKeyPair(new SubjectPublicKeyInfo(algorithmIdentifier, instance.getPublicKey().getBytes()), privateKeyInfo) : new PEMKeyPair(null, privateKeyInfo);
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating EC private key: " + e2.toString(), e2);
            }
        }
    }

    private class EncryptedPrivateKeyParser implements PemObjectParser {
        public EncryptedPrivateKeyParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new PKCS8EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfo.getInstance(pemObject.getContent()));
            } catch (Exception e) {
                throw new PEMException("problem parsing ENCRYPTED PRIVATE KEY: " + e.toString(), e);
            }
        }
    }

    private class KeyPairParser implements PemObjectParser {
        private final PEMKeyPairParser pemKeyPairParser;

        public KeyPairParser(PEMKeyPairParser pEMKeyPairParser) {
            this.pemKeyPairParser = pEMKeyPairParser;
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            boolean z = false;
            String str = null;
            for (PemHeader pemHeader : pemObject.getHeaders()) {
                if (pemHeader.getName().equals("Proc-Type") && pemHeader.getValue().equals("4,ENCRYPTED")) {
                    z = true;
                } else if (pemHeader.getName().equals("DEK-Info")) {
                    str = pemHeader.getValue();
                }
            }
            byte[] content = pemObject.getContent();
            if (!z) {
                return this.pemKeyPairParser.parse(content);
            }
            try {
                StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
                return new PEMEncryptedKeyPair(stringTokenizer.nextToken(), Hex.decode(stringTokenizer.nextToken()), content, this.pemKeyPairParser);
            } catch (IOException e) {
                if (z) {
                    throw new PEMException("exception decoding - please check password and data.", e);
                }
                throw new PEMException(e.getMessage(), e);
            } catch (IllegalArgumentException e2) {
                if (z) {
                    throw new PEMException("exception decoding - please check password and data.", e2);
                }
                throw new PEMException(e2.getMessage(), e2);
            }
        }
    }

    private class PKCS10CertificationRequestParser implements PemObjectParser {
        private PKCS10CertificationRequestParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new PKCS10CertificationRequest(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing certrequest: " + e.toString(), e);
            }
        }
    }

    private class PKCS7Parser implements PemObjectParser {
        private PKCS7Parser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return ContentInfo.getInstance(new ASN1InputStream(pemObject.getContent()).readObject());
            } catch (Exception e) {
                throw new PEMException("problem parsing PKCS7 object: " + e.toString(), e);
            }
        }
    }

    private class PrivateKeyParser implements PemObjectParser {
        public PrivateKeyParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return PrivateKeyInfo.getInstance(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing PRIVATE KEY: " + e.toString(), e);
            }
        }
    }

    private class PublicKeyParser implements PemObjectParser {
        public PublicKeyParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            return SubjectPublicKeyInfo.getInstance(pemObject.getContent());
        }
    }

    private class RSAKeyPairParser implements PEMKeyPairParser {
        private RSAKeyPairParser() {
        }

        @Override // org.bouncycastle.openssl.PEMKeyPairParser
        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
                if (instance.size() == 9) {
                    RSAPrivateKey instance2 = RSAPrivateKey.getInstance(instance);
                    RSAPublicKey rSAPublicKey = new RSAPublicKey(instance2.getModulus(), instance2.getPublicExponent());
                    AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
                    return new PEMKeyPair(new SubjectPublicKeyInfo(algorithmIdentifier, rSAPublicKey), new PrivateKeyInfo(algorithmIdentifier, instance2));
                }
                throw new PEMException("malformed sequence in RSA private key");
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating RSA private key: " + e2.toString(), e2);
            }
        }
    }

    private class RSAPublicKeyParser implements PemObjectParser {
        public RSAPublicKeyParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), RSAPublicKey.getInstance(pemObject.getContent()));
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem extracting key: " + e2.toString(), e2);
            }
        }
    }

    private class X509AttributeCertificateParser implements PemObjectParser {
        private X509AttributeCertificateParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            return new X509AttributeCertificateHolder(pemObject.getContent());
        }
    }

    private class X509CRLParser implements PemObjectParser {
        private X509CRLParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new X509CRLHolder(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing cert: " + e.toString(), e);
            }
        }
    }

    private class X509CertificateParser implements PemObjectParser {
        private X509CertificateParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new X509CertificateHolder(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing cert: " + e.toString(), e);
            }
        }
    }

    private class X509TrustedCertificateParser implements PemObjectParser {
        private X509TrustedCertificateParser() {
        }

        @Override // org.bouncycastle.util.io.pem.PemObjectParser
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new X509TrustedCertificateBlock(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing cert: " + e.toString(), e);
            }
        }
    }

    public PEMParser(Reader reader) {
        super(reader);
        this.parsers.put("CERTIFICATE REQUEST", new PKCS10CertificationRequestParser());
        this.parsers.put("NEW CERTIFICATE REQUEST", new PKCS10CertificationRequestParser());
        this.parsers.put("CERTIFICATE", new X509CertificateParser());
        this.parsers.put("TRUSTED CERTIFICATE", new X509TrustedCertificateParser());
        this.parsers.put("X509 CERTIFICATE", new X509CertificateParser());
        this.parsers.put("X509 CRL", new X509CRLParser());
        this.parsers.put("PKCS7", new PKCS7Parser());
        this.parsers.put("CMS", new PKCS7Parser());
        this.parsers.put("ATTRIBUTE CERTIFICATE", new X509AttributeCertificateParser());
        this.parsers.put("EC PARAMETERS", new ECCurveParamsParser());
        this.parsers.put("PUBLIC KEY", new PublicKeyParser());
        this.parsers.put("RSA PUBLIC KEY", new RSAPublicKeyParser());
        this.parsers.put("RSA PRIVATE KEY", new KeyPairParser(new RSAKeyPairParser()));
        this.parsers.put("DSA PRIVATE KEY", new KeyPairParser(new DSAKeyPairParser()));
        this.parsers.put("EC PRIVATE KEY", new KeyPairParser(new ECDSAKeyPairParser()));
        this.parsers.put("ENCRYPTED PRIVATE KEY", new EncryptedPrivateKeyParser());
        this.parsers.put("PRIVATE KEY", new PrivateKeyParser());
    }

    public Object readObject() throws IOException {
        PemObject readPemObject = readPemObject();
        if (readPemObject == null) {
            return null;
        }
        String type = readPemObject.getType();
        if (this.parsers.containsKey(type)) {
            return ((PemObjectParser) this.parsers.get(type)).parseObject(readPemObject);
        }
        throw new IOException("unrecognised object: " + type);
    }
}
