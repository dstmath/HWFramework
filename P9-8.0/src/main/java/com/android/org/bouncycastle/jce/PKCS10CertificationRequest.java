package com.android.org.bouncycastle.jce;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.CertificationRequest;
import com.android.org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Strings;
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

public class PKCS10CertificationRequest extends CertificationRequest {
    private static Hashtable algorithms = new Hashtable();
    private static Hashtable keyAlgorithms = new Hashtable();
    private static Set noParams = new HashSet();
    private static Hashtable oids = new Hashtable();
    private static Hashtable params = new Hashtable();

    static {
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
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
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
        params.put("SHA1WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), 20));
        params.put("SHA224WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224, DERNull.INSTANCE), 28));
        params.put("SHA256WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE), 32));
        params.put("SHA384WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384, DERNull.INSTANCE), 48));
        params.put("SHA512WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512, DERNull.INSTANCE), 64));
    }

    private static RSASSAPSSparams creatPSSParams(AlgorithmIdentifier hashAlgId, int saltSize) {
        return new RSASSAPSSparams(hashAlgId, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hashAlgId), new ASN1Integer((long) saltSize), new ASN1Integer(1));
    }

    private static ASN1Sequence toDERSequence(byte[] bytes) {
        try {
            return (ASN1Sequence) new ASN1InputStream(bytes).readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("badly encoded request");
        }
    }

    public PKCS10CertificationRequest(byte[] bytes) {
        super(toDERSequence(bytes));
    }

    public PKCS10CertificationRequest(ASN1Sequence sequence) {
        super(sequence);
    }

    public PKCS10CertificationRequest(String signatureAlgorithm, X509Name subject, PublicKey key, ASN1Set attributes, PrivateKey signingKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(signatureAlgorithm, subject, key, attributes, signingKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    private static X509Name convertName(X500Principal name) {
        try {
            return new X509Principal(name.getEncoded());
        } catch (IOException e) {
            throw new IllegalArgumentException("can't convert name");
        }
    }

    public PKCS10CertificationRequest(String signatureAlgorithm, X500Principal subject, PublicKey key, ASN1Set attributes, PrivateKey signingKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    public PKCS10CertificationRequest(String signatureAlgorithm, X500Principal subject, PublicKey key, ASN1Set attributes, PrivateKey signingKey, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, provider);
    }

    public PKCS10CertificationRequest(String signatureAlgorithm, X509Name subject, PublicKey key, ASN1Set attributes, PrivateKey signingKey, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        String algorithmName = Strings.toUpperCase(signatureAlgorithm);
        ASN1ObjectIdentifier sigOID = (ASN1ObjectIdentifier) algorithms.get(algorithmName);
        if (sigOID == null) {
            try {
                sigOID = new ASN1ObjectIdentifier(algorithmName);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unknown signature type requested");
            }
        }
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        } else if (key == null) {
            throw new IllegalArgumentException("public key must not be null");
        } else {
            if (noParams.contains(sigOID)) {
                this.sigAlgId = new AlgorithmIdentifier(sigOID);
            } else if (params.containsKey(algorithmName)) {
                this.sigAlgId = new AlgorithmIdentifier(sigOID, (ASN1Encodable) params.get(algorithmName));
            } else {
                this.sigAlgId = new AlgorithmIdentifier(sigOID, DERNull.INSTANCE);
            }
            try {
                Signature sig;
                this.reqInfo = new CertificationRequestInfo(subject, SubjectPublicKeyInfo.getInstance((ASN1Sequence) ASN1Primitive.fromByteArray(key.getEncoded())), attributes);
                if (provider == null) {
                    sig = Signature.getInstance(signatureAlgorithm);
                } else {
                    sig = Signature.getInstance(signatureAlgorithm, provider);
                }
                sig.initSign(signingKey);
                try {
                    sig.update(this.reqInfo.getEncoded(ASN1Encoding.DER));
                    this.sigBits = new DERBitString(sig.sign());
                } catch (Exception e2) {
                    throw new IllegalArgumentException("exception encoding TBS cert request - " + e2);
                }
            } catch (IOException e3) {
                throw new IllegalArgumentException("can't encode public key");
            }
        }
    }

    public PublicKey getPublicKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        return getPublicKey(BouncyCastleProvider.PROVIDER_NAME);
    }

    public PublicKey getPublicKey(String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        ASN1Encodable subjectPKInfo = this.reqInfo.getSubjectPublicKeyInfo();
        X509EncodedKeySpec xspec;
        AlgorithmIdentifier keyAlg;
        try {
            xspec = new X509EncodedKeySpec(new DERBitString(subjectPKInfo).getOctets());
            keyAlg = subjectPKInfo.getAlgorithm();
            if (provider == null) {
                return KeyFactory.getInstance(keyAlg.getAlgorithm().getId()).generatePublic(xspec);
            }
            return KeyFactory.getInstance(keyAlg.getAlgorithm().getId(), provider).generatePublic(xspec);
        } catch (NoSuchAlgorithmException e) {
            if (keyAlgorithms.get(keyAlg.getAlgorithm()) != null) {
                String keyAlgorithm = (String) keyAlgorithms.get(keyAlg.getAlgorithm());
                if (provider == null) {
                    return KeyFactory.getInstance(keyAlgorithm).generatePublic(xspec);
                }
                return KeyFactory.getInstance(keyAlgorithm, provider).generatePublic(xspec);
            }
            throw e;
        } catch (InvalidKeySpecException e2) {
            throw new InvalidKeyException("error decoding public key");
        } catch (IOException e3) {
            throw new InvalidKeyException("error decoding public key");
        }
    }

    public boolean verify() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return verify(BouncyCastleProvider.PROVIDER_NAME);
    }

    public boolean verify(String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return verify(getPublicKey(provider), provider);
    }

    public boolean verify(PublicKey pubKey, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature sig;
        if (provider == null) {
            try {
                sig = Signature.getInstance(getSignatureName(this.sigAlgId));
            } catch (NoSuchAlgorithmException e) {
                if (oids.get(this.sigAlgId.getAlgorithm()) != null) {
                    String signatureAlgorithm = (String) oids.get(this.sigAlgId.getAlgorithm());
                    if (provider == null) {
                        sig = Signature.getInstance(signatureAlgorithm);
                    } else {
                        sig = Signature.getInstance(signatureAlgorithm, provider);
                    }
                } else {
                    throw e;
                }
            }
        }
        sig = Signature.getInstance(getSignatureName(this.sigAlgId), provider);
        setSignatureParameters(sig, this.sigAlgId.getParameters());
        sig.initVerify(pubKey);
        try {
            sig.update(this.reqInfo.getEncoded(ASN1Encoding.DER));
            return sig.verify(this.sigBits.getOctets());
        } catch (Exception e2) {
            throw new SignatureException("exception encoding TBS cert request - " + e2);
        }
    }

    public byte[] getEncoded() {
        try {
            return getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private void setSignatureParameters(Signature signature, ASN1Encodable params) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (params != null && (DERNull.INSTANCE.equals(params) ^ 1) != 0) {
            AlgorithmParameters sigParams = AlgorithmParameters.getInstance(signature.getAlgorithm(), signature.getProvider());
            try {
                sigParams.init(params.toASN1Primitive().getEncoded(ASN1Encoding.DER));
                if (signature.getAlgorithm().endsWith("MGF1")) {
                    try {
                        signature.setParameter(sigParams.getParameterSpec(PSSParameterSpec.class));
                    } catch (GeneralSecurityException e) {
                        throw new SignatureException("Exception extracting parameters: " + e.getMessage());
                    }
                }
            } catch (IOException e2) {
                throw new SignatureException("IOException decoding parameters: " + e2.getMessage());
            }
        }
    }

    static String getSignatureName(AlgorithmIdentifier sigAlgId) {
        ASN1Encodable params = sigAlgId.getParameters();
        if (params == null || (DERNull.INSTANCE.equals(params) ^ 1) == 0 || !sigAlgId.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            return sigAlgId.getAlgorithm().getId();
        }
        return getDigestAlgName(RSASSAPSSparams.getInstance(params).getHashAlgorithm().getAlgorithm()) + "withRSAandMGF1";
    }

    private static String getDigestAlgName(ASN1ObjectIdentifier digestAlgOID) {
        if (PKCSObjectIdentifiers.md5.equals(digestAlgOID)) {
            return "MD5";
        }
        if (OIWObjectIdentifiers.idSHA1.equals(digestAlgOID)) {
            return "SHA1";
        }
        if (NISTObjectIdentifiers.id_sha224.equals(digestAlgOID)) {
            return "SHA224";
        }
        if (NISTObjectIdentifiers.id_sha256.equals(digestAlgOID)) {
            return "SHA256";
        }
        if (NISTObjectIdentifiers.id_sha384.equals(digestAlgOID)) {
            return "SHA384";
        }
        if (NISTObjectIdentifiers.id_sha512.equals(digestAlgOID)) {
            return "SHA512";
        }
        return digestAlgOID.getId();
    }
}
