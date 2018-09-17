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
import java.util.Hashtable;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public class PKCS10CertificationRequest extends CertificationRequest {
    private static Hashtable algorithms;
    private static Hashtable keyAlgorithms;
    private static Set noParams;
    private static Hashtable oids;
    private static Hashtable params;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jce.PKCS10CertificationRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jce.PKCS10CertificationRequest.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.PKCS10CertificationRequest.<clinit>():void");
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
        if (params != null && !DERNull.INSTANCE.equals(params)) {
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
        if (params == null || DERNull.INSTANCE.equals(params) || !sigAlgId.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
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
