package org.bouncycastle.jce;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Strings;

public class PKCS10CertificationRequest extends CertificationRequest {
    private static Hashtable algorithms = new Hashtable();
    private static Hashtable keyAlgorithms = new Hashtable();
    private static Set noParams = new HashSet();
    private static Hashtable oids = new Hashtable();
    private static Hashtable params = new Hashtable();

    static {
        algorithms.put("MD2WITHRSAENCRYPTION", new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD2WITHRSA", new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD5WITHRSAENCRYPTION", new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("MD5WITHRSA", new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("RSAWITHMD5", new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("SHA1WITHRSAENCRYPTION", new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA1WITHRSA", new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA224WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA224WITHRSA", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA256WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA256WITHRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA384WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA384WITHRSA", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA512WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA512WITHRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA1WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA224WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA256WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA384WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA512WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("RSAWITHSHA1", new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("RIPEMD128WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD128WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD160WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD256WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("RIPEMD256WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("SHA1WITHDSA", new ASN1ObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("DSAWITHSHA1", new ASN1ObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("SHA224WITHDSA", NISTObjectIdentifiers.dsa_with_sha224);
        algorithms.put("SHA256WITHDSA", NISTObjectIdentifiers.dsa_with_sha256);
        algorithms.put("SHA384WITHDSA", NISTObjectIdentifiers.dsa_with_sha384);
        algorithms.put("SHA512WITHDSA", NISTObjectIdentifiers.dsa_with_sha512);
        algorithms.put("SHA1WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("SHA224WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA224);
        algorithms.put("SHA256WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA256);
        algorithms.put("SHA384WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA384);
        algorithms.put("SHA512WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA512);
        algorithms.put("ECDSAWITHSHA1", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("GOST3411WITHGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3410WITHGOST3411", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHECGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHECGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "GOST3411WITHECGOST3410");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"), "MD2WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
        oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
        oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");
        keyAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        keyAlgorithms.put(X9ObjectIdentifiers.id_dsa, "DSA");
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA1);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA224);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA256);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA384);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA512);
        noParams.add(X9ObjectIdentifiers.id_dsa_with_sha1);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha224);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha256);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        params.put("SHA1WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), 20));
        params.put("SHA224WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224, DERNull.INSTANCE), 28));
        params.put("SHA256WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE), 32));
        params.put("SHA384WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384, DERNull.INSTANCE), 48));
        params.put("SHA512WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512, DERNull.INSTANCE), 64));
    }

    public PKCS10CertificationRequest(String str, X500Principal x500Principal, PublicKey publicKey, ASN1Set aSN1Set, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(str, convertName(x500Principal), publicKey, aSN1Set, privateKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    public PKCS10CertificationRequest(String str, X500Principal x500Principal, PublicKey publicKey, ASN1Set aSN1Set, PrivateKey privateKey, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(str, convertName(x500Principal), publicKey, aSN1Set, privateKey, str2);
    }

    public PKCS10CertificationRequest(String str, X509Name x509Name, PublicKey publicKey, ASN1Set aSN1Set, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(str, x509Name, publicKey, aSN1Set, privateKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0070  */
    public PKCS10CertificationRequest(String str, X509Name x509Name, PublicKey publicKey, ASN1Set aSN1Set, PrivateKey privateKey, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature instance;
        AlgorithmIdentifier algorithmIdentifier;
        String upperCase = Strings.toUpperCase(str);
        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) algorithms.get(upperCase);
        if (aSN1ObjectIdentifier == null) {
            try {
                aSN1ObjectIdentifier = new ASN1ObjectIdentifier(upperCase);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unknown signature type requested");
            }
        }
        if (x509Name == null) {
            throw new IllegalArgumentException("subject must not be null");
        } else if (publicKey != null) {
            if (noParams.contains(aSN1ObjectIdentifier)) {
                algorithmIdentifier = new AlgorithmIdentifier(aSN1ObjectIdentifier);
            } else if (params.containsKey(upperCase)) {
                this.sigAlgId = new AlgorithmIdentifier(aSN1ObjectIdentifier, (ASN1Encodable) params.get(upperCase));
                this.reqInfo = new CertificationRequestInfo(x509Name, SubjectPublicKeyInfo.getInstance((ASN1Sequence) ASN1Primitive.fromByteArray(publicKey.getEncoded())), aSN1Set);
                instance = str2 != null ? Signature.getInstance(str) : Signature.getInstance(str, str2);
                instance.initSign(privateKey);
                try {
                    instance.update(this.reqInfo.getEncoded(ASN1Encoding.DER));
                    this.sigBits = new DERBitString(instance.sign());
                } catch (Exception e2) {
                    throw new IllegalArgumentException("exception encoding TBS cert request - " + e2);
                }
            } else {
                algorithmIdentifier = new AlgorithmIdentifier(aSN1ObjectIdentifier, DERNull.INSTANCE);
            }
            this.sigAlgId = algorithmIdentifier;
            try {
                this.reqInfo = new CertificationRequestInfo(x509Name, SubjectPublicKeyInfo.getInstance((ASN1Sequence) ASN1Primitive.fromByteArray(publicKey.getEncoded())), aSN1Set);
                if (str2 != null) {
                }
                instance.initSign(privateKey);
                instance.update(this.reqInfo.getEncoded(ASN1Encoding.DER));
                this.sigBits = new DERBitString(instance.sign());
            } catch (IOException e3) {
                throw new IllegalArgumentException("can't encode public key");
            }
        } else {
            throw new IllegalArgumentException("public key must not be null");
        }
    }

    public PKCS10CertificationRequest(ASN1Sequence aSN1Sequence) {
        super(aSN1Sequence);
    }

    public PKCS10CertificationRequest(byte[] bArr) {
        super(toDERSequence(bArr));
    }

    private static X509Name convertName(X500Principal x500Principal) {
        try {
            return new X509Principal(x500Principal.getEncoded());
        } catch (IOException e) {
            throw new IllegalArgumentException("can't convert name");
        }
    }

    private static RSASSAPSSparams creatPSSParams(AlgorithmIdentifier algorithmIdentifier, int i) {
        return new RSASSAPSSparams(algorithmIdentifier, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, algorithmIdentifier), new ASN1Integer((long) i), new ASN1Integer(1));
    }

    private static String getDigestAlgName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return PKCSObjectIdentifiers.md5.equals(aSN1ObjectIdentifier) ? "MD5" : OIWObjectIdentifiers.idSHA1.equals(aSN1ObjectIdentifier) ? "SHA1" : NISTObjectIdentifiers.id_sha224.equals(aSN1ObjectIdentifier) ? "SHA224" : NISTObjectIdentifiers.id_sha256.equals(aSN1ObjectIdentifier) ? "SHA256" : NISTObjectIdentifiers.id_sha384.equals(aSN1ObjectIdentifier) ? "SHA384" : NISTObjectIdentifiers.id_sha512.equals(aSN1ObjectIdentifier) ? "SHA512" : TeleTrusTObjectIdentifiers.ripemd128.equals(aSN1ObjectIdentifier) ? "RIPEMD128" : TeleTrusTObjectIdentifiers.ripemd160.equals(aSN1ObjectIdentifier) ? "RIPEMD160" : TeleTrusTObjectIdentifiers.ripemd256.equals(aSN1ObjectIdentifier) ? "RIPEMD256" : CryptoProObjectIdentifiers.gostR3411.equals(aSN1ObjectIdentifier) ? "GOST3411" : aSN1ObjectIdentifier.getId();
    }

    static String getSignatureName(AlgorithmIdentifier algorithmIdentifier) {
        ASN1Encodable parameters = algorithmIdentifier.getParameters();
        if (parameters == null || DERNull.INSTANCE.equals(parameters) || !algorithmIdentifier.getAlgorithm().equals((ASN1Primitive) PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            return algorithmIdentifier.getAlgorithm().getId();
        }
        RSASSAPSSparams instance = RSASSAPSSparams.getInstance(parameters);
        return getDigestAlgName(instance.getHashAlgorithm().getAlgorithm()) + "withRSAandMGF1";
    }

    private void setSignatureParameters(Signature signature, ASN1Encodable aSN1Encodable) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (aSN1Encodable != null && !DERNull.INSTANCE.equals(aSN1Encodable)) {
            AlgorithmParameters instance = AlgorithmParameters.getInstance(signature.getAlgorithm(), signature.getProvider());
            try {
                instance.init(aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER));
                if (signature.getAlgorithm().endsWith("MGF1")) {
                    try {
                        signature.setParameter(instance.getParameterSpec(PSSParameterSpec.class));
                    } catch (GeneralSecurityException e) {
                        throw new SignatureException("Exception extracting parameters: " + e.getMessage());
                    }
                }
            } catch (IOException e2) {
                throw new SignatureException("IOException decoding parameters: " + e2.getMessage());
            }
        }
    }

    private static ASN1Sequence toDERSequence(byte[] bArr) {
        try {
            return (ASN1Sequence) new ASN1InputStream(bArr).readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("badly encoded request");
        }
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.util.Encodable
    public byte[] getEncoded() {
        try {
            return getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public PublicKey getPublicKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        return getPublicKey(BouncyCastleProvider.PROVIDER_NAME);
    }

    public PublicKey getPublicKey(String str) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        SubjectPublicKeyInfo subjectPublicKeyInfo = this.reqInfo.getSubjectPublicKeyInfo();
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(new DERBitString(subjectPublicKeyInfo).getOctets());
            AlgorithmIdentifier algorithm = subjectPublicKeyInfo.getAlgorithm();
            if (str != null) {
                return KeyFactory.getInstance(algorithm.getAlgorithm().getId(), str).generatePublic(x509EncodedKeySpec);
            }
            try {
                return KeyFactory.getInstance(algorithm.getAlgorithm().getId()).generatePublic(x509EncodedKeySpec);
            } catch (NoSuchAlgorithmException e) {
                if (keyAlgorithms.get(algorithm.getAlgorithm()) != null) {
                    String str2 = (String) keyAlgorithms.get(algorithm.getAlgorithm());
                    return str == null ? KeyFactory.getInstance(str2).generatePublic(x509EncodedKeySpec) : KeyFactory.getInstance(str2, str).generatePublic(x509EncodedKeySpec);
                }
                throw e;
            }
        } catch (InvalidKeySpecException e2) {
            throw new InvalidKeyException("error decoding public key");
        } catch (IOException e3) {
            throw new InvalidKeyException("error decoding public key");
        }
    }

    public boolean verify() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return verify(BouncyCastleProvider.PROVIDER_NAME);
    }

    public boolean verify(String str) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return verify(getPublicKey(str), str);
    }

    public boolean verify(PublicKey publicKey, String str) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature signature;
        if (str == null) {
            try {
                signature = Signature.getInstance(getSignatureName(this.sigAlgId));
            } catch (NoSuchAlgorithmException e) {
                if (oids.get(this.sigAlgId.getAlgorithm()) != null) {
                    String str2 = (String) oids.get(this.sigAlgId.getAlgorithm());
                    signature = str == null ? Signature.getInstance(str2) : Signature.getInstance(str2, str);
                } else {
                    throw e;
                }
            }
        } else {
            signature = Signature.getInstance(getSignatureName(this.sigAlgId), str);
        }
        setSignatureParameters(signature, this.sigAlgId.getParameters());
        signature.initVerify(publicKey);
        try {
            signature.update(this.reqInfo.getEncoded(ASN1Encoding.DER));
            return signature.verify(this.sigBits.getOctets());
        } catch (Exception e2) {
            throw new SignatureException("exception encoding TBS cert request - " + e2);
        }
    }
}
