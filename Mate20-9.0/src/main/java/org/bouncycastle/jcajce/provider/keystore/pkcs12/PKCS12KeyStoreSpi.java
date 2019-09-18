package org.bouncycastle.jcajce.provider.keystore.pkcs12;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BEROutputStream;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cms.CMSEnvelopedGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.PKCS12Key;
import org.bouncycastle.jcajce.PKCS12StoreParameter;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.interfaces.BCKeyStore;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKPKCS12StoreParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class PKCS12KeyStoreSpi extends KeyStoreSpi implements PKCSObjectIdentifiers, X509ObjectIdentifiers, BCKeyStore {
    static final int CERTIFICATE = 1;
    static final int KEY = 2;
    static final int KEY_PRIVATE = 0;
    static final int KEY_PUBLIC = 1;
    static final int KEY_SECRET = 2;
    private static final int MIN_ITERATIONS = 51200;
    static final int NULL = 0;
    static final String PKCS12_MAX_IT_COUNT_PROPERTY = "org.bouncycastle.pkcs12.max_it_count";
    private static final int SALT_SIZE = 20;
    static final int SEALED = 4;
    static final int SECRET = 3;
    private static final DefaultSecretKeyProvider keySizeProvider = new DefaultSecretKeyProvider();
    private static Provider provider = null;
    private ASN1ObjectIdentifier certAlgorithm;
    private CertificateFactory certFact;
    private IgnoresCaseHashtable certs = new IgnoresCaseHashtable();
    private Hashtable chainCerts = new Hashtable();
    private final JcaJceHelper helper = new BCJcaJceHelper();
    private int itCount = 102400;
    private ASN1ObjectIdentifier keyAlgorithm;
    private Hashtable keyCerts = new Hashtable();
    private IgnoresCaseHashtable keys = new IgnoresCaseHashtable();
    private Hashtable localIds = new Hashtable();
    private AlgorithmIdentifier macAlgorithm = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE);
    protected SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    private int saltLength = 20;

    public static class BCPKCS12KeyStore extends PKCS12KeyStoreSpi {
        public BCPKCS12KeyStore() {
            super(PKCS12KeyStoreSpi.getBouncyCastleProvider(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd40BitRC2_CBC);
        }
    }

    public static class BCPKCS12KeyStore3DES extends PKCS12KeyStoreSpi {
        public BCPKCS12KeyStore3DES() {
            super(PKCS12KeyStoreSpi.getBouncyCastleProvider(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    private class CertId {
        byte[] id;

        CertId(PublicKey publicKey) {
            this.id = PKCS12KeyStoreSpi.this.createSubjectKeyId(publicKey).getKeyIdentifier();
        }

        CertId(byte[] bArr) {
            this.id = bArr;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CertId)) {
                return false;
            }
            return Arrays.areEqual(this.id, ((CertId) obj).id);
        }

        public int hashCode() {
            return Arrays.hashCode(this.id);
        }
    }

    public static class DefPKCS12KeyStore extends PKCS12KeyStoreSpi {
        public DefPKCS12KeyStore() {
            super(null, pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd40BitRC2_CBC);
        }
    }

    public static class DefPKCS12KeyStore3DES extends PKCS12KeyStoreSpi {
        public DefPKCS12KeyStore3DES() {
            super(null, pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    private static class DefaultSecretKeyProvider {
        private final Map KEY_SIZES;

        DefaultSecretKeyProvider() {
            HashMap hashMap = new HashMap();
            hashMap.put(new ASN1ObjectIdentifier(CMSEnvelopedGenerator.CAST5_CBC), Integers.valueOf(128));
            hashMap.put(PKCSObjectIdentifiers.des_EDE3_CBC, Integers.valueOf(192));
            hashMap.put(NISTObjectIdentifiers.id_aes128_CBC, Integers.valueOf(128));
            hashMap.put(NISTObjectIdentifiers.id_aes192_CBC, Integers.valueOf(192));
            hashMap.put(NISTObjectIdentifiers.id_aes256_CBC, Integers.valueOf(256));
            hashMap.put(NTTObjectIdentifiers.id_camellia128_cbc, Integers.valueOf(128));
            hashMap.put(NTTObjectIdentifiers.id_camellia192_cbc, Integers.valueOf(192));
            hashMap.put(NTTObjectIdentifiers.id_camellia256_cbc, Integers.valueOf(256));
            hashMap.put(CryptoProObjectIdentifiers.gostR28147_gcfb, Integers.valueOf(256));
            this.KEY_SIZES = Collections.unmodifiableMap(hashMap);
        }

        public int getKeySize(AlgorithmIdentifier algorithmIdentifier) {
            Integer num = (Integer) this.KEY_SIZES.get(algorithmIdentifier.getAlgorithm());
            if (num != null) {
                return num.intValue();
            }
            return -1;
        }
    }

    private static class IgnoresCaseHashtable {
        private Hashtable keys;
        private Hashtable orig;

        private IgnoresCaseHashtable() {
            this.orig = new Hashtable();
            this.keys = new Hashtable();
        }

        public Enumeration elements() {
            return this.orig.elements();
        }

        public Object get(String str) {
            String str2 = (String) this.keys.get(str == null ? null : Strings.toLowerCase(str));
            if (str2 == null) {
                return null;
            }
            return this.orig.get(str2);
        }

        public Enumeration keys() {
            return this.orig.keys();
        }

        public void put(String str, Object obj) {
            String lowerCase = str == null ? null : Strings.toLowerCase(str);
            String str2 = (String) this.keys.get(lowerCase);
            if (str2 != null) {
                this.orig.remove(str2);
            }
            this.keys.put(lowerCase, str);
            this.orig.put(str, obj);
        }

        public Object remove(String str) {
            String str2 = (String) this.keys.remove(str == null ? null : Strings.toLowerCase(str));
            if (str2 == null) {
                return null;
            }
            return this.orig.remove(str2);
        }
    }

    public PKCS12KeyStoreSpi(Provider provider2, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        CertificateFactory instance;
        this.keyAlgorithm = aSN1ObjectIdentifier;
        this.certAlgorithm = aSN1ObjectIdentifier2;
        if (provider2 != null) {
            try {
                instance = CertificateFactory.getInstance("X.509", provider2);
            } catch (Exception e) {
                throw new IllegalArgumentException("can't create cert factory - " + e.toString());
            }
        } else {
            instance = CertificateFactory.getInstance("X.509");
        }
        this.certFact = instance;
    }

    private byte[] calculatePbeMac(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] bArr, int i, char[] cArr, boolean z, byte[] bArr2) throws Exception {
        PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(bArr, i);
        Mac createMac = this.helper.createMac(aSN1ObjectIdentifier.getId());
        createMac.init(new PKCS12Key(cArr, z), pBEParameterSpec);
        createMac.update(bArr2);
        return createMac.doFinal();
    }

    private Cipher createCipher(int i, char[] cArr, AlgorithmIdentifier algorithmIdentifier) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        SecretKey secretKey;
        AlgorithmParameterSpec gOST28147ParameterSpec;
        PBES2Parameters instance = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
        PBKDF2Params instance2 = PBKDF2Params.getInstance(instance.getKeyDerivationFunc().getParameters());
        AlgorithmIdentifier instance3 = AlgorithmIdentifier.getInstance(instance.getEncryptionScheme());
        SecretKeyFactory createSecretKeyFactory = this.helper.createSecretKeyFactory(instance.getKeyDerivationFunc().getAlgorithm().getId());
        if (instance2.isDefaultPrf()) {
            secretKey = createSecretKeyFactory.generateSecret(new PBEKeySpec(cArr, instance2.getSalt(), validateIterationCount(instance2.getIterationCount()), keySizeProvider.getKeySize(instance3)));
        } else {
            PBKDF2KeySpec pBKDF2KeySpec = new PBKDF2KeySpec(cArr, instance2.getSalt(), validateIterationCount(instance2.getIterationCount()), keySizeProvider.getKeySize(instance3), instance2.getPrf());
            secretKey = createSecretKeyFactory.generateSecret(pBKDF2KeySpec);
        }
        Cipher instance4 = Cipher.getInstance(instance.getEncryptionScheme().getAlgorithm().getId());
        ASN1Encodable parameters = instance.getEncryptionScheme().getParameters();
        if (parameters instanceof ASN1OctetString) {
            gOST28147ParameterSpec = new IvParameterSpec(ASN1OctetString.getInstance(parameters).getOctets());
        } else {
            GOST28147Parameters instance5 = GOST28147Parameters.getInstance(parameters);
            gOST28147ParameterSpec = new GOST28147ParameterSpec(instance5.getEncryptionParamSet(), instance5.getIV());
        }
        instance4.init(i, secretKey, gOST28147ParameterSpec);
        return instance4;
    }

    /* access modifiers changed from: private */
    public SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) {
        try {
            return new SubjectKeyIdentifier(getDigest(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())));
        } catch (Exception e) {
            throw new RuntimeException("error creating key");
        }
    }

    private void doStore(OutputStream outputStream, char[] cArr, boolean z) throws IOException {
        Enumeration enumeration;
        boolean z2;
        Enumeration enumeration2;
        boolean z3;
        boolean z4;
        OutputStream outputStream2 = outputStream;
        char[] cArr2 = cArr;
        if (cArr2 != null) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            Enumeration keys2 = this.keys.keys();
            while (keys2.hasMoreElements()) {
                byte[] bArr = new byte[20];
                this.random.nextBytes(bArr);
                String str = (String) keys2.nextElement();
                PrivateKey privateKey = (PrivateKey) this.keys.get(str);
                PKCS12PBEParams pKCS12PBEParams = new PKCS12PBEParams(bArr, MIN_ITERATIONS);
                EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(this.keyAlgorithm, pKCS12PBEParams.toASN1Primitive()), wrapKey(this.keyAlgorithm.getId(), privateKey, pKCS12PBEParams, cArr2));
                ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                if (privateKey instanceof PKCS12BagAttributeCarrier) {
                    PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = (PKCS12BagAttributeCarrier) privateKey;
                    DERBMPString dERBMPString = (DERBMPString) pKCS12BagAttributeCarrier.getBagAttribute(pkcs_9_at_friendlyName);
                    if (dERBMPString == null || !dERBMPString.getString().equals(str)) {
                        pKCS12BagAttributeCarrier.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str));
                    }
                    if (pKCS12BagAttributeCarrier.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                        pKCS12BagAttributeCarrier.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(engineGetCertificate(str).getPublicKey()));
                    }
                    Enumeration bagAttributeKeys = pKCS12BagAttributeCarrier.getBagAttributeKeys();
                    z4 = false;
                    while (bagAttributeKeys.hasMoreElements()) {
                        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) bagAttributeKeys.nextElement();
                        ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
                        aSN1EncodableVector3.add(aSN1ObjectIdentifier);
                        aSN1EncodableVector3.add(new DERSet(pKCS12BagAttributeCarrier.getBagAttribute(aSN1ObjectIdentifier)));
                        aSN1EncodableVector2.add(new DERSequence(aSN1EncodableVector3));
                        z4 = true;
                    }
                } else {
                    z4 = false;
                }
                if (!z4) {
                    ASN1EncodableVector aSN1EncodableVector4 = new ASN1EncodableVector();
                    Certificate engineGetCertificate = engineGetCertificate(str);
                    aSN1EncodableVector4.add(pkcs_9_at_localKeyId);
                    aSN1EncodableVector4.add(new DERSet((ASN1Encodable) createSubjectKeyId(engineGetCertificate.getPublicKey())));
                    aSN1EncodableVector2.add(new DERSequence(aSN1EncodableVector4));
                    ASN1EncodableVector aSN1EncodableVector5 = new ASN1EncodableVector();
                    aSN1EncodableVector5.add(pkcs_9_at_friendlyName);
                    aSN1EncodableVector5.add(new DERSet((ASN1Encodable) new DERBMPString(str)));
                    aSN1EncodableVector2.add(new DERSequence(aSN1EncodableVector5));
                }
                aSN1EncodableVector.add(new SafeBag(pkcs8ShroudedKeyBag, encryptedPrivateKeyInfo.toASN1Primitive(), new DERSet(aSN1EncodableVector2)));
            }
            BEROctetString bEROctetString = new BEROctetString(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
            byte[] bArr2 = new byte[20];
            this.random.nextBytes(bArr2);
            ASN1EncodableVector aSN1EncodableVector6 = new ASN1EncodableVector();
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(this.certAlgorithm, new PKCS12PBEParams(bArr2, MIN_ITERATIONS).toASN1Primitive());
            Hashtable hashtable = new Hashtable();
            Enumeration keys3 = this.keys.keys();
            while (keys3.hasMoreElements()) {
                try {
                    String str2 = (String) keys3.nextElement();
                    Certificate engineGetCertificate2 = engineGetCertificate(str2);
                    CertBag certBag = new CertBag(x509Certificate, new DEROctetString(engineGetCertificate2.getEncoded()));
                    ASN1EncodableVector aSN1EncodableVector7 = new ASN1EncodableVector();
                    if (engineGetCertificate2 instanceof PKCS12BagAttributeCarrier) {
                        PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier2 = (PKCS12BagAttributeCarrier) engineGetCertificate2;
                        DERBMPString dERBMPString2 = (DERBMPString) pKCS12BagAttributeCarrier2.getBagAttribute(pkcs_9_at_friendlyName);
                        if (dERBMPString2 == null || !dERBMPString2.getString().equals(str2)) {
                            pKCS12BagAttributeCarrier2.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str2));
                        }
                        if (pKCS12BagAttributeCarrier2.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                            pKCS12BagAttributeCarrier2.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(engineGetCertificate2.getPublicKey()));
                        }
                        Enumeration bagAttributeKeys2 = pKCS12BagAttributeCarrier2.getBagAttributeKeys();
                        z3 = false;
                        while (bagAttributeKeys2.hasMoreElements()) {
                            ASN1ObjectIdentifier aSN1ObjectIdentifier2 = (ASN1ObjectIdentifier) bagAttributeKeys2.nextElement();
                            Enumeration enumeration3 = keys3;
                            ASN1EncodableVector aSN1EncodableVector8 = new ASN1EncodableVector();
                            aSN1EncodableVector8.add(aSN1ObjectIdentifier2);
                            aSN1EncodableVector8.add(new DERSet(pKCS12BagAttributeCarrier2.getBagAttribute(aSN1ObjectIdentifier2)));
                            aSN1EncodableVector7.add(new DERSequence(aSN1EncodableVector8));
                            keys3 = enumeration3;
                            bagAttributeKeys2 = bagAttributeKeys2;
                            z3 = true;
                        }
                        enumeration2 = keys3;
                    } else {
                        enumeration2 = keys3;
                        z3 = false;
                    }
                    if (!z3) {
                        ASN1EncodableVector aSN1EncodableVector9 = new ASN1EncodableVector();
                        aSN1EncodableVector9.add(pkcs_9_at_localKeyId);
                        aSN1EncodableVector9.add(new DERSet((ASN1Encodable) createSubjectKeyId(engineGetCertificate2.getPublicKey())));
                        aSN1EncodableVector7.add(new DERSequence(aSN1EncodableVector9));
                        ASN1EncodableVector aSN1EncodableVector10 = new ASN1EncodableVector();
                        aSN1EncodableVector10.add(pkcs_9_at_friendlyName);
                        aSN1EncodableVector10.add(new DERSet((ASN1Encodable) new DERBMPString(str2)));
                        aSN1EncodableVector7.add(new DERSequence(aSN1EncodableVector10));
                    }
                    aSN1EncodableVector6.add(new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(aSN1EncodableVector7)));
                    hashtable.put(engineGetCertificate2, engineGetCertificate2);
                    keys3 = enumeration2;
                } catch (CertificateEncodingException e) {
                    throw new IOException("Error encoding certificate: " + e.toString());
                }
            }
            Enumeration keys4 = this.certs.keys();
            while (keys4.hasMoreElements()) {
                try {
                    String str3 = (String) keys4.nextElement();
                    Certificate certificate = (Certificate) this.certs.get(str3);
                    if (this.keys.get(str3) == null) {
                        CertBag certBag2 = new CertBag(x509Certificate, new DEROctetString(certificate.getEncoded()));
                        ASN1EncodableVector aSN1EncodableVector11 = new ASN1EncodableVector();
                        if (certificate instanceof PKCS12BagAttributeCarrier) {
                            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier3 = (PKCS12BagAttributeCarrier) certificate;
                            DERBMPString dERBMPString3 = (DERBMPString) pKCS12BagAttributeCarrier3.getBagAttribute(pkcs_9_at_friendlyName);
                            if (dERBMPString3 == null || !dERBMPString3.getString().equals(str3)) {
                                pKCS12BagAttributeCarrier3.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str3));
                            }
                            Enumeration bagAttributeKeys3 = pKCS12BagAttributeCarrier3.getBagAttributeKeys();
                            z2 = false;
                            while (bagAttributeKeys3.hasMoreElements()) {
                                Enumeration enumeration4 = keys4;
                                ASN1ObjectIdentifier aSN1ObjectIdentifier3 = (ASN1ObjectIdentifier) bagAttributeKeys3.nextElement();
                                Enumeration enumeration5 = bagAttributeKeys3;
                                if (aSN1ObjectIdentifier3.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                    keys4 = enumeration4;
                                    bagAttributeKeys3 = enumeration5;
                                } else {
                                    ASN1EncodableVector aSN1EncodableVector12 = new ASN1EncodableVector();
                                    aSN1EncodableVector12.add(aSN1ObjectIdentifier3);
                                    aSN1EncodableVector12.add(new DERSet(pKCS12BagAttributeCarrier3.getBagAttribute(aSN1ObjectIdentifier3)));
                                    aSN1EncodableVector11.add(new DERSequence(aSN1EncodableVector12));
                                    keys4 = enumeration4;
                                    bagAttributeKeys3 = enumeration5;
                                    z2 = true;
                                }
                            }
                            enumeration = keys4;
                        } else {
                            enumeration = keys4;
                            z2 = false;
                        }
                        if (!z2) {
                            ASN1EncodableVector aSN1EncodableVector13 = new ASN1EncodableVector();
                            aSN1EncodableVector13.add(pkcs_9_at_friendlyName);
                            aSN1EncodableVector13.add(new DERSet((ASN1Encodable) new DERBMPString(str3)));
                            aSN1EncodableVector11.add(new DERSequence(aSN1EncodableVector13));
                        }
                        aSN1EncodableVector6.add(new SafeBag(certBag, certBag2.toASN1Primitive(), new DERSet(aSN1EncodableVector11)));
                        hashtable.put(certificate, certificate);
                        keys4 = enumeration;
                    }
                } catch (CertificateEncodingException e2) {
                    throw new IOException("Error encoding certificate: " + e2.toString());
                }
            }
            Set usedCertificateSet = getUsedCertificateSet();
            Enumeration keys5 = this.chainCerts.keys();
            while (keys5.hasMoreElements()) {
                try {
                    Certificate certificate2 = (Certificate) this.chainCerts.get((CertId) keys5.nextElement());
                    if (usedCertificateSet.contains(certificate2)) {
                        if (hashtable.get(certificate2) == null) {
                            CertBag certBag3 = new CertBag(x509Certificate, new DEROctetString(certificate2.getEncoded()));
                            ASN1EncodableVector aSN1EncodableVector14 = new ASN1EncodableVector();
                            if (certificate2 instanceof PKCS12BagAttributeCarrier) {
                                PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier4 = (PKCS12BagAttributeCarrier) certificate2;
                                Enumeration bagAttributeKeys4 = pKCS12BagAttributeCarrier4.getBagAttributeKeys();
                                while (bagAttributeKeys4.hasMoreElements()) {
                                    ASN1ObjectIdentifier aSN1ObjectIdentifier4 = (ASN1ObjectIdentifier) bagAttributeKeys4.nextElement();
                                    if (!aSN1ObjectIdentifier4.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                        ASN1EncodableVector aSN1EncodableVector15 = new ASN1EncodableVector();
                                        aSN1EncodableVector15.add(aSN1ObjectIdentifier4);
                                        aSN1EncodableVector15.add(new DERSet(pKCS12BagAttributeCarrier4.getBagAttribute(aSN1ObjectIdentifier4)));
                                        aSN1EncodableVector14.add(new DERSequence(aSN1EncodableVector15));
                                        hashtable = hashtable;
                                    }
                                }
                            }
                            Hashtable hashtable2 = hashtable;
                            aSN1EncodableVector6.add(new SafeBag(certBag, certBag3.toASN1Primitive(), new DERSet(aSN1EncodableVector14)));
                            hashtable = hashtable2;
                        }
                    }
                } catch (CertificateEncodingException e3) {
                    throw new IOException("Error encoding certificate: " + e3.toString());
                }
            }
            AuthenticatedSafe authenticatedSafe = new AuthenticatedSafe(new ContentInfo[]{new ContentInfo(data, bEROctetString), new ContentInfo(encryptedData, new EncryptedData(data, algorithmIdentifier, new BEROctetString(cryptData(true, algorithmIdentifier, cArr2, false, new DERSequence(aSN1EncodableVector6).getEncoded(ASN1Encoding.DER)))).toASN1Primitive())});
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            (z ? new DEROutputStream(byteArrayOutputStream) : new BEROutputStream(byteArrayOutputStream)).writeObject(authenticatedSafe);
            ContentInfo contentInfo = new ContentInfo(data, new BEROctetString(byteArrayOutputStream.toByteArray()));
            byte[] bArr3 = new byte[this.saltLength];
            this.random.nextBytes(bArr3);
            try {
                (z ? new DEROutputStream(outputStream2) : new BEROutputStream(outputStream2)).writeObject(new Pfx(contentInfo, new MacData(new DigestInfo(this.macAlgorithm, calculatePbeMac(this.macAlgorithm.getAlgorithm(), bArr3, this.itCount, cArr2, false, ((ASN1OctetString) contentInfo.getContent()).getOctets())), bArr3, this.itCount)));
            } catch (Exception e4) {
                throw new IOException("error constructing MAC: " + e4.toString());
            }
        } else {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }
    }

    /* access modifiers changed from: private */
    public static synchronized Provider getBouncyCastleProvider() {
        synchronized (PKCS12KeyStoreSpi.class) {
            if (Security.getProvider("BC") != null) {
                Provider provider2 = Security.getProvider("BC");
                return provider2;
            }
            if (provider == null) {
                provider = new BouncyCastleProvider();
            }
            Provider provider3 = provider;
            return provider3;
        }
    }

    private static byte[] getDigest(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        Digest createSHA1 = DigestFactory.createSHA1();
        byte[] bArr = new byte[createSHA1.getDigestSize()];
        byte[] bytes = subjectPublicKeyInfo.getPublicKeyData().getBytes();
        createSHA1.update(bytes, 0, bytes.length);
        createSHA1.doFinal(bArr, 0);
        return bArr;
    }

    private Set getUsedCertificateSet() {
        HashSet hashSet = new HashSet();
        Enumeration keys2 = this.keys.keys();
        while (keys2.hasMoreElements()) {
            Certificate[] engineGetCertificateChain = engineGetCertificateChain((String) keys2.nextElement());
            for (int i = 0; i != engineGetCertificateChain.length; i++) {
                hashSet.add(engineGetCertificateChain[i]);
            }
        }
        Enumeration keys3 = this.certs.keys();
        while (keys3.hasMoreElements()) {
            hashSet.add(engineGetCertificate((String) keys3.nextElement()));
        }
        return hashSet;
    }

    private int validateIterationCount(BigInteger bigInteger) {
        int intValue = bigInteger.intValue();
        if (intValue >= 0) {
            BigInteger asBigInteger = Properties.asBigInteger(PKCS12_MAX_IT_COUNT_PROPERTY);
            if (asBigInteger == null || asBigInteger.intValue() >= intValue) {
                return intValue;
            }
            throw new IllegalStateException("iteration count " + intValue + " greater than " + asBigInteger.intValue());
        }
        throw new IllegalStateException("negative iteration count found");
    }

    /* access modifiers changed from: protected */
    public byte[] cryptData(boolean z, AlgorithmIdentifier algorithmIdentifier, char[] cArr, boolean z2, byte[] bArr) throws IOException {
        ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
        int i = z ? 1 : 2;
        if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
            PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
            try {
                PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(instance.getIV(), instance.getIterations().intValue());
                PKCS12Key pKCS12Key = new PKCS12Key(cArr, z2);
                Cipher createCipher = this.helper.createCipher(algorithm.getId());
                createCipher.init(i, pKCS12Key, pBEParameterSpec);
                return createCipher.doFinal(bArr);
            } catch (Exception e) {
                throw new IOException("exception decrypting data - " + e.toString());
            }
        } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
            try {
                return createCipher(i, cArr, algorithmIdentifier).doFinal(bArr);
            } catch (Exception e2) {
                throw new IOException("exception decrypting data - " + e2.toString());
            }
        } else {
            throw new IOException("unknown PBE algorithm: " + algorithm);
        }
    }

    public Enumeration engineAliases() {
        Hashtable hashtable = new Hashtable();
        Enumeration keys2 = this.certs.keys();
        while (keys2.hasMoreElements()) {
            hashtable.put(keys2.nextElement(), "cert");
        }
        Enumeration keys3 = this.keys.keys();
        while (keys3.hasMoreElements()) {
            String str = (String) keys3.nextElement();
            if (hashtable.get(str) == null) {
                hashtable.put(str, "key");
            }
        }
        return hashtable.keys();
    }

    public boolean engineContainsAlias(String str) {
        return (this.certs.get(str) == null && this.keys.get(str) == null) ? false : true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: java.security.cert.Certificate} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void engineDeleteEntry(String str) throws KeyStoreException {
        Key key = (Key) this.keys.remove(str);
        Certificate certificate = (Certificate) this.certs.remove(str);
        if (certificate != null) {
            this.chainCerts.remove(new CertId(certificate.getPublicKey()));
        }
        if (key != null) {
            String str2 = (String) this.localIds.remove(str);
            if (str2 != null) {
                certificate = this.keyCerts.remove(str2);
            }
            if (certificate != null) {
                this.chainCerts.remove(new CertId(certificate.getPublicKey()));
            }
        }
    }

    public Certificate engineGetCertificate(String str) {
        if (str != null) {
            Certificate certificate = (Certificate) this.certs.get(str);
            if (certificate != null) {
                return certificate;
            }
            String str2 = (String) this.localIds.get(str);
            return (Certificate) (str2 != null ? this.keyCerts.get(str2) : this.keyCerts.get(str));
        }
        throw new IllegalArgumentException("null alias passed to getCertificate.");
    }

    public String engineGetCertificateAlias(Certificate certificate) {
        Enumeration elements = this.certs.elements();
        Enumeration keys2 = this.certs.keys();
        while (elements.hasMoreElements()) {
            String str = (String) keys2.nextElement();
            if (((Certificate) elements.nextElement()).equals(certificate)) {
                return str;
            }
        }
        Enumeration elements2 = this.keyCerts.elements();
        Enumeration keys3 = this.keyCerts.keys();
        while (elements2.hasMoreElements()) {
            String str2 = (String) keys3.nextElement();
            if (((Certificate) elements2.nextElement()).equals(certificate)) {
                return str2;
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ac  */
    public Certificate[] engineGetCertificateChain(String str) {
        X509Certificate x509Certificate;
        if (str == null) {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        } else if (!engineIsKeyEntry(str)) {
            return null;
        } else {
            Certificate engineGetCertificate = engineGetCertificate(str);
            if (engineGetCertificate == null) {
                return null;
            }
            Vector vector = new Vector();
            while (engineGetCertificate != null) {
                X509Certificate x509Certificate2 = (X509Certificate) engineGetCertificate;
                byte[] extensionValue = x509Certificate2.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                if (extensionValue != null) {
                    try {
                        AuthorityKeyIdentifier instance = AuthorityKeyIdentifier.getInstance(new ASN1InputStream(((ASN1OctetString) new ASN1InputStream(extensionValue).readObject()).getOctets()).readObject());
                        if (instance.getKeyIdentifier() != null) {
                            x509Certificate = (Certificate) this.chainCerts.get(new CertId(instance.getKeyIdentifier()));
                            if (x509Certificate == null) {
                                Principal issuerDN = x509Certificate2.getIssuerDN();
                                if (!issuerDN.equals(x509Certificate2.getSubjectDN())) {
                                    Enumeration keys2 = this.chainCerts.keys();
                                    while (true) {
                                        if (!keys2.hasMoreElements()) {
                                            break;
                                        }
                                        X509Certificate x509Certificate3 = (X509Certificate) this.chainCerts.get(keys2.nextElement());
                                        if (x509Certificate3.getSubjectDN().equals(issuerDN)) {
                                            try {
                                                x509Certificate2.verify(x509Certificate3.getPublicKey());
                                                x509Certificate = x509Certificate3;
                                                break;
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                }
                            }
                            if (!vector.contains(engineGetCertificate)) {
                                vector.addElement(engineGetCertificate);
                                if (x509Certificate != engineGetCertificate) {
                                    engineGetCertificate = x509Certificate;
                                }
                            }
                            engineGetCertificate = null;
                        }
                    } catch (IOException e2) {
                        throw new RuntimeException(e2.toString());
                    }
                }
                x509Certificate = null;
                if (x509Certificate == null) {
                }
                if (!vector.contains(engineGetCertificate)) {
                }
                engineGetCertificate = null;
            }
            Certificate[] certificateArr = new Certificate[vector.size()];
            for (int i = 0; i != certificateArr.length; i++) {
                certificateArr[i] = (Certificate) vector.elementAt(i);
            }
            return certificateArr;
        }
    }

    public Date engineGetCreationDate(String str) {
        if (str == null) {
            throw new NullPointerException("alias == null");
        } else if (this.keys.get(str) == null && this.certs.get(str) == null) {
            return null;
        } else {
            return new Date();
        }
    }

    public Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (str != null) {
            return (Key) this.keys.get(str);
        }
        throw new IllegalArgumentException("null alias passed to getKey.");
    }

    public boolean engineIsCertificateEntry(String str) {
        return this.certs.get(str) != null && this.keys.get(str) == null;
    }

    public boolean engineIsKeyEntry(String str) {
        return this.keys.get(str) != null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v21, resolved type: org.bouncycastle.asn1.ASN1Primitive} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v27, resolved type: org.bouncycastle.asn1.ASN1OctetString} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v30, resolved type: org.bouncycastle.asn1.ASN1OctetString} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v31, resolved type: org.bouncycastle.asn1.ASN1OctetString} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v33, resolved type: org.bouncycastle.asn1.ASN1OctetString} */
    /* JADX WARNING: type inference failed for: r6v12, types: [org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Encodable, java.lang.Object] */
    /* JADX WARNING: type inference failed for: r12v24, types: [org.bouncycastle.asn1.ASN1Encodable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void engineLoad(InputStream inputStream, char[] cArr) throws IOException {
        boolean z;
        boolean z2;
        ASN1OctetString aSN1OctetString;
        String str;
        ASN1Sequence aSN1Sequence;
        ASN1Primitive aSN1Primitive;
        ASN1Sequence aSN1Sequence2;
        ASN1OctetString aSN1OctetString2;
        ASN1OctetString aSN1OctetString3;
        ASN1Sequence aSN1Sequence3;
        ASN1Primitive aSN1Primitive2;
        ASN1OctetString aSN1OctetString4;
        boolean z3;
        InputStream inputStream2 = inputStream;
        char[] cArr2 = cArr;
        if (inputStream2 != null) {
            if (cArr2 != null) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream2);
                bufferedInputStream.mark(10);
                if (bufferedInputStream.read() == 48) {
                    bufferedInputStream.reset();
                    try {
                        Pfx instance = Pfx.getInstance(new ASN1InputStream((InputStream) bufferedInputStream).readObject());
                        ContentInfo authSafe = instance.getAuthSafe();
                        Vector vector = new Vector();
                        int i = 1;
                        int i2 = 0;
                        if (instance.getMacData() != null) {
                            MacData macData = instance.getMacData();
                            DigestInfo mac = macData.getMac();
                            this.macAlgorithm = mac.getAlgorithmId();
                            byte[] salt = macData.getSalt();
                            this.itCount = validateIterationCount(macData.getIterationCount());
                            this.saltLength = salt.length;
                            byte[] octets = ((ASN1OctetString) authSafe.getContent()).getOctets();
                            try {
                                byte[] calculatePbeMac = calculatePbeMac(this.macAlgorithm.getAlgorithm(), salt, this.itCount, cArr2, false, octets);
                                byte[] digest = mac.getDigest();
                                if (Arrays.constantTimeAreEqual(calculatePbeMac, digest)) {
                                    z3 = false;
                                } else if (cArr2.length <= 0) {
                                    if (Arrays.constantTimeAreEqual(calculatePbeMac(this.macAlgorithm.getAlgorithm(), salt, this.itCount, cArr2, true, octets), digest)) {
                                        z3 = true;
                                    } else {
                                        throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                                    }
                                } else {
                                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                                }
                                z = z3;
                            } catch (IOException e) {
                                throw e;
                            } catch (Exception e2) {
                                throw new IOException("error constructing MAC: " + e2.toString());
                            }
                        } else {
                            z = false;
                        }
                        ASN1OctetString aSN1OctetString5 = null;
                        this.keys = new IgnoresCaseHashtable();
                        this.localIds = new Hashtable();
                        if (authSafe.getContentType().equals(data)) {
                            ContentInfo[] contentInfo = AuthenticatedSafe.getInstance(new ASN1InputStream(((ASN1OctetString) authSafe.getContent()).getOctets()).readObject()).getContentInfo();
                            int i3 = 0;
                            z2 = false;
                            while (i3 != contentInfo.length) {
                                if (contentInfo[i3].getContentType().equals(data)) {
                                    ASN1Sequence aSN1Sequence4 = (ASN1Sequence) new ASN1InputStream(((ASN1OctetString) contentInfo[i3].getContent()).getOctets()).readObject();
                                    int i4 = i2;
                                    while (i4 != aSN1Sequence4.size()) {
                                        SafeBag instance2 = SafeBag.getInstance(aSN1Sequence4.getObjectAt(i4));
                                        if (instance2.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                            EncryptedPrivateKeyInfo instance3 = EncryptedPrivateKeyInfo.getInstance(instance2.getBagValue());
                                            PrivateKey unwrapKey = unwrapKey(instance3.getEncryptionAlgorithm(), instance3.getEncryptedData(), cArr2, z);
                                            if (instance2.getBagAttributes() != null) {
                                                Enumeration objects = instance2.getBagAttributes().getObjects();
                                                aSN1OctetString3 = aSN1OctetString5;
                                                ASN1OctetString aSN1OctetString6 = aSN1OctetString3;
                                                while (objects.hasMoreElements()) {
                                                    ASN1Sequence aSN1Sequence5 = (ASN1Sequence) objects.nextElement();
                                                    ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) aSN1Sequence5.getObjectAt(i2);
                                                    ASN1Set aSN1Set = (ASN1Set) aSN1Sequence5.getObjectAt(i);
                                                    if (aSN1Set.size() > 0) {
                                                        ASN1Primitive aSN1Primitive3 = (ASN1Primitive) aSN1Set.getObjectAt(0);
                                                        if (unwrapKey instanceof PKCS12BagAttributeCarrier) {
                                                            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = (PKCS12BagAttributeCarrier) unwrapKey;
                                                            aSN1Sequence3 = aSN1Sequence4;
                                                            ASN1Encodable bagAttribute = pKCS12BagAttributeCarrier.getBagAttribute(aSN1ObjectIdentifier);
                                                            if (bagAttribute != null) {
                                                                boolean equals = bagAttribute.toASN1Primitive().equals(aSN1Primitive3);
                                                                aSN1Primitive2 = aSN1Primitive3;
                                                                if (!equals) {
                                                                    throw new IOException("attempt to add existing attribute with different value");
                                                                }
                                                            } else {
                                                                pKCS12BagAttributeCarrier.setBagAttribute(aSN1ObjectIdentifier, aSN1Primitive3);
                                                                aSN1Primitive2 = aSN1Primitive3;
                                                            }
                                                        } else {
                                                            aSN1Sequence3 = aSN1Sequence4;
                                                            aSN1Primitive2 = aSN1Primitive3;
                                                        }
                                                    } else {
                                                        aSN1Sequence3 = aSN1Sequence4;
                                                        aSN1Primitive2 = null;
                                                    }
                                                    if (aSN1ObjectIdentifier.equals(pkcs_9_at_friendlyName)) {
                                                        String string = ((DERBMPString) aSN1Primitive2).getString();
                                                        this.keys.put(string, unwrapKey);
                                                        aSN1OctetString4 = string;
                                                    } else if (aSN1ObjectIdentifier.equals(pkcs_9_at_localKeyId)) {
                                                        aSN1OctetString3 = (ASN1OctetString) aSN1Primitive2;
                                                        aSN1OctetString4 = aSN1OctetString6;
                                                    } else {
                                                        aSN1OctetString4 = aSN1OctetString6;
                                                    }
                                                    aSN1Sequence4 = aSN1Sequence3;
                                                    i = 1;
                                                    i2 = 0;
                                                    aSN1OctetString6 = aSN1OctetString4;
                                                }
                                                aSN1Sequence2 = aSN1Sequence4;
                                                aSN1OctetString2 = aSN1OctetString6;
                                            } else {
                                                aSN1Sequence2 = aSN1Sequence4;
                                                aSN1OctetString3 = null;
                                                aSN1OctetString2 = null;
                                            }
                                            if (aSN1OctetString3 != null) {
                                                String str2 = new String(Hex.encode(aSN1OctetString3.getOctets()));
                                                if (aSN1OctetString2 == null) {
                                                    this.keys.put(str2, unwrapKey);
                                                } else {
                                                    this.localIds.put(aSN1OctetString2, str2);
                                                }
                                            } else {
                                                this.keys.put("unmarked", unwrapKey);
                                                z2 = true;
                                            }
                                        } else {
                                            aSN1Sequence2 = aSN1Sequence4;
                                            if (instance2.getBagId().equals(certBag)) {
                                                vector.addElement(instance2);
                                            } else {
                                                System.out.println("extra in data " + instance2.getBagId());
                                                System.out.println(ASN1Dump.dumpAsString(instance2));
                                            }
                                        }
                                        i4++;
                                        aSN1Sequence4 = aSN1Sequence2;
                                        i = 1;
                                        i2 = 0;
                                        aSN1OctetString5 = null;
                                    }
                                    continue;
                                } else if (contentInfo[i3].getContentType().equals(encryptedData)) {
                                    EncryptedData instance4 = EncryptedData.getInstance(contentInfo[i3].getContent());
                                    ASN1Sequence aSN1Sequence6 = (ASN1Sequence) ASN1Primitive.fromByteArray(cryptData(false, instance4.getEncryptionAlgorithm(), cArr2, z, instance4.getContent().getOctets()));
                                    int i5 = 0;
                                    while (i5 != aSN1Sequence6.size()) {
                                        SafeBag instance5 = SafeBag.getInstance(aSN1Sequence6.getObjectAt(i5));
                                        if (instance5.getBagId().equals(certBag)) {
                                            vector.addElement(instance5);
                                            aSN1Sequence = aSN1Sequence6;
                                        } else if (instance5.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                            EncryptedPrivateKeyInfo instance6 = EncryptedPrivateKeyInfo.getInstance(instance5.getBagValue());
                                            PrivateKey unwrapKey2 = unwrapKey(instance6.getEncryptionAlgorithm(), instance6.getEncryptedData(), cArr2, z);
                                            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier2 = (PKCS12BagAttributeCarrier) unwrapKey2;
                                            Enumeration objects2 = instance5.getBagAttributes().getObjects();
                                            ASN1OctetString aSN1OctetString7 = null;
                                            String str3 = null;
                                            while (objects2.hasMoreElements()) {
                                                ASN1Sequence aSN1Sequence7 = (ASN1Sequence) objects2.nextElement();
                                                ASN1ObjectIdentifier aSN1ObjectIdentifier2 = (ASN1ObjectIdentifier) aSN1Sequence7.getObjectAt(0);
                                                ASN1Sequence aSN1Sequence8 = aSN1Sequence6;
                                                ASN1Set aSN1Set2 = (ASN1Set) aSN1Sequence7.getObjectAt(1);
                                                if (aSN1Set2.size() > 0) {
                                                    aSN1Primitive = aSN1Set2.getObjectAt(0);
                                                    ASN1Encodable bagAttribute2 = pKCS12BagAttributeCarrier2.getBagAttribute(aSN1ObjectIdentifier2);
                                                    if (bagAttribute2 == null) {
                                                        pKCS12BagAttributeCarrier2.setBagAttribute(aSN1ObjectIdentifier2, aSN1Primitive);
                                                    } else if (!bagAttribute2.toASN1Primitive().equals(aSN1Primitive)) {
                                                        throw new IOException("attempt to add existing attribute with different value");
                                                    }
                                                } else {
                                                    aSN1Primitive = null;
                                                }
                                                if (aSN1ObjectIdentifier2.equals(pkcs_9_at_friendlyName)) {
                                                    String string2 = ((DERBMPString) aSN1Primitive).getString();
                                                    this.keys.put(string2, unwrapKey2);
                                                    str3 = string2;
                                                } else if (aSN1ObjectIdentifier2.equals(pkcs_9_at_localKeyId)) {
                                                    aSN1OctetString7 = (ASN1OctetString) aSN1Primitive;
                                                }
                                                aSN1Sequence6 = aSN1Sequence8;
                                            }
                                            aSN1Sequence = aSN1Sequence6;
                                            String str4 = new String(Hex.encode(aSN1OctetString7.getOctets()));
                                            if (str3 == null) {
                                                this.keys.put(str4, unwrapKey2);
                                            } else {
                                                this.localIds.put(str3, str4);
                                            }
                                        } else {
                                            aSN1Sequence = aSN1Sequence6;
                                            if (instance5.getBagId().equals(keyBag)) {
                                                PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(instance5.getBagValue()));
                                                PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier3 = (PKCS12BagAttributeCarrier) privateKey;
                                                Enumeration objects3 = instance5.getBagAttributes().getObjects();
                                                ASN1OctetString aSN1OctetString8 = null;
                                                String str5 = null;
                                                while (objects3.hasMoreElements()) {
                                                    ASN1Sequence instance7 = ASN1Sequence.getInstance(objects3.nextElement());
                                                    ASN1ObjectIdentifier instance8 = ASN1ObjectIdentifier.getInstance(instance7.getObjectAt(0));
                                                    ASN1Set instance9 = ASN1Set.getInstance(instance7.getObjectAt(1));
                                                    if (instance9.size() > 0) {
                                                        ASN1Primitive aSN1Primitive4 = (ASN1Primitive) instance9.getObjectAt(0);
                                                        ASN1Encodable bagAttribute3 = pKCS12BagAttributeCarrier3.getBagAttribute(instance8);
                                                        if (bagAttribute3 == null) {
                                                            pKCS12BagAttributeCarrier3.setBagAttribute(instance8, aSN1Primitive4);
                                                        } else if (!bagAttribute3.toASN1Primitive().equals(aSN1Primitive4)) {
                                                            throw new IOException("attempt to add existing attribute with different value");
                                                        }
                                                        if (instance8.equals(pkcs_9_at_friendlyName)) {
                                                            str5 = ((DERBMPString) aSN1Primitive4).getString();
                                                            this.keys.put(str5, privateKey);
                                                        } else if (instance8.equals(pkcs_9_at_localKeyId)) {
                                                            aSN1OctetString8 = (ASN1OctetString) aSN1Primitive4;
                                                        }
                                                    }
                                                }
                                                String str6 = new String(Hex.encode(aSN1OctetString8.getOctets()));
                                                if (str5 == null) {
                                                    this.keys.put(str6, privateKey);
                                                } else {
                                                    this.localIds.put(str5, str6);
                                                }
                                            } else {
                                                System.out.println("extra in encryptedData " + instance5.getBagId());
                                                System.out.println(ASN1Dump.dumpAsString(instance5));
                                            }
                                        }
                                        i5++;
                                        aSN1Sequence6 = aSN1Sequence;
                                    }
                                    continue;
                                } else {
                                    System.out.println("extra " + contentInfo[i3].getContentType().getId());
                                    System.out.println("extra " + ASN1Dump.dumpAsString(contentInfo[i3].getContent()));
                                }
                                i3++;
                                i = 1;
                                i2 = 0;
                                aSN1OctetString5 = null;
                            }
                        } else {
                            z2 = false;
                        }
                        this.certs = new IgnoresCaseHashtable();
                        this.chainCerts = new Hashtable();
                        this.keyCerts = new Hashtable();
                        int i6 = 0;
                        while (i6 != vector.size()) {
                            SafeBag safeBag = (SafeBag) vector.elementAt(i6);
                            CertBag instance10 = CertBag.getInstance(safeBag.getBagValue());
                            if (instance10.getCertId().equals(x509Certificate)) {
                                try {
                                    Certificate generateCertificate = this.certFact.generateCertificate(new ByteArrayInputStream(((ASN1OctetString) instance10.getCertValue()).getOctets()));
                                    if (safeBag.getBagAttributes() != null) {
                                        Enumeration objects4 = safeBag.getBagAttributes().getObjects();
                                        str = null;
                                        aSN1OctetString = null;
                                        while (objects4.hasMoreElements()) {
                                            ASN1Sequence instance11 = ASN1Sequence.getInstance(objects4.nextElement());
                                            ASN1ObjectIdentifier instance12 = ASN1ObjectIdentifier.getInstance(instance11.getObjectAt(0));
                                            ASN1Set instance13 = ASN1Set.getInstance(instance11.getObjectAt(1));
                                            if (instance13.size() > 0) {
                                                ? r6 = (ASN1Primitive) instance13.getObjectAt(0);
                                                if (generateCertificate instanceof PKCS12BagAttributeCarrier) {
                                                    PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier4 = (PKCS12BagAttributeCarrier) generateCertificate;
                                                    ASN1Encodable bagAttribute4 = pKCS12BagAttributeCarrier4.getBagAttribute(instance12);
                                                    if (bagAttribute4 == null) {
                                                        pKCS12BagAttributeCarrier4.setBagAttribute(instance12, r6);
                                                    } else if (!bagAttribute4.toASN1Primitive().equals(r6)) {
                                                        throw new IOException("attempt to add existing attribute with different value");
                                                    }
                                                }
                                                if (instance12.equals(pkcs_9_at_friendlyName)) {
                                                    str = ((DERBMPString) r6).getString();
                                                } else if (instance12.equals(pkcs_9_at_localKeyId)) {
                                                    aSN1OctetString = r6;
                                                }
                                            }
                                        }
                                    } else {
                                        str = null;
                                        aSN1OctetString = null;
                                    }
                                    this.chainCerts.put(new CertId(generateCertificate.getPublicKey()), generateCertificate);
                                    if (!z2) {
                                        if (aSN1OctetString != null) {
                                            this.keyCerts.put(new String(Hex.encode(aSN1OctetString.getOctets())), generateCertificate);
                                        }
                                        if (str != null) {
                                            this.certs.put(str, generateCertificate);
                                        }
                                    } else if (this.keyCerts.isEmpty()) {
                                        String str7 = new String(Hex.encode(createSubjectKeyId(generateCertificate.getPublicKey()).getKeyIdentifier()));
                                        this.keyCerts.put(str7, generateCertificate);
                                        this.keys.put(str7, this.keys.remove("unmarked"));
                                    }
                                    i6++;
                                } catch (Exception e3) {
                                    throw new RuntimeException(e3.toString());
                                }
                            } else {
                                throw new RuntimeException("Unsupported certificate type: " + instance10.getCertId());
                            }
                        }
                    } catch (Exception e4) {
                        throw new IOException(e4.getMessage());
                    }
                } else {
                    throw new IOException("stream does not represent a PKCS12 key store");
                }
            } else {
                throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
            }
        }
    }

    public void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
        if (this.keys.get(str) == null) {
            this.certs.put(str, certificate);
            this.chainCerts.put(new CertId(certificate.getPublicKey()), certificate);
            return;
        }
        throw new KeyStoreException("There is a key entry with the name " + str + ".");
    }

    public void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
        boolean z = key instanceof PrivateKey;
        if (!z) {
            throw new KeyStoreException("PKCS12 does not support non-PrivateKeys");
        } else if (!z || certificateArr != null) {
            if (this.keys.get(str) != null) {
                engineDeleteEntry(str);
            }
            this.keys.put(str, key);
            if (certificateArr != null) {
                this.certs.put(str, certificateArr[0]);
                for (int i = 0; i != certificateArr.length; i++) {
                    this.chainCerts.put(new CertId(certificateArr[i].getPublicKey()), certificateArr[i]);
                }
            }
        } else {
            throw new KeyStoreException("no certificate chain for private key");
        }
    }

    public void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
        throw new RuntimeException("operation not supported");
    }

    public int engineSize() {
        Hashtable hashtable = new Hashtable();
        Enumeration keys2 = this.certs.keys();
        while (keys2.hasMoreElements()) {
            hashtable.put(keys2.nextElement(), "cert");
        }
        Enumeration keys3 = this.keys.keys();
        while (keys3.hasMoreElements()) {
            String str = (String) keys3.nextElement();
            if (hashtable.get(str) == null) {
                hashtable.put(str, "key");
            }
        }
        return hashtable.size();
    }

    public void engineStore(OutputStream outputStream, char[] cArr) throws IOException {
        doStore(outputStream, cArr, false);
    }

    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        PKCS12StoreParameter pKCS12StoreParameter;
        char[] cArr;
        if (loadStoreParameter != null) {
            boolean z = loadStoreParameter instanceof PKCS12StoreParameter;
            if (z || (loadStoreParameter instanceof JDKPKCS12StoreParameter)) {
                if (z) {
                    pKCS12StoreParameter = (PKCS12StoreParameter) loadStoreParameter;
                } else {
                    JDKPKCS12StoreParameter jDKPKCS12StoreParameter = (JDKPKCS12StoreParameter) loadStoreParameter;
                    pKCS12StoreParameter = new PKCS12StoreParameter(jDKPKCS12StoreParameter.getOutputStream(), loadStoreParameter.getProtectionParameter(), jDKPKCS12StoreParameter.isUseDEREncoding());
                }
                KeyStore.ProtectionParameter protectionParameter = loadStoreParameter.getProtectionParameter();
                if (protectionParameter == null) {
                    cArr = null;
                } else if (protectionParameter instanceof KeyStore.PasswordProtection) {
                    cArr = ((KeyStore.PasswordProtection) protectionParameter).getPassword();
                } else {
                    throw new IllegalArgumentException("No support for protection parameter of type " + protectionParameter.getClass().getName());
                }
                doStore(pKCS12StoreParameter.getOutputStream(), cArr, pKCS12StoreParameter.isForDEREncoding());
                return;
            }
            throw new IllegalArgumentException("No support for 'param' of type " + loadStoreParameter.getClass().getName());
        }
        throw new IllegalArgumentException("'param' arg cannot be null");
    }

    public void setRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
    }

    /* access modifiers changed from: protected */
    public PrivateKey unwrapKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, char[] cArr, boolean z) throws IOException {
        ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
        try {
            if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(instance.getIV(), validateIterationCount(instance.getIterations()));
                Cipher createCipher = this.helper.createCipher(algorithm.getId());
                createCipher.init(4, new PKCS12Key(cArr, z), pBEParameterSpec);
                return (PrivateKey) createCipher.unwrap(bArr, "", 2);
            } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
                return (PrivateKey) createCipher(4, cArr, algorithmIdentifier).unwrap(bArr, "", 2);
            } else {
                throw new IOException("exception unwrapping private key - cannot recognise: " + algorithm);
            }
        } catch (Exception e) {
            throw new IOException("exception unwrapping private key - " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public byte[] wrapKey(String str, Key key, PKCS12PBEParams pKCS12PBEParams, char[] cArr) throws IOException {
        PBEKeySpec pBEKeySpec = new PBEKeySpec(cArr);
        try {
            SecretKeyFactory createSecretKeyFactory = this.helper.createSecretKeyFactory(str);
            PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue());
            Cipher createCipher = this.helper.createCipher(str);
            createCipher.init(3, createSecretKeyFactory.generateSecret(pBEKeySpec), pBEParameterSpec);
            return createCipher.wrap(key);
        } catch (Exception e) {
            throw new IOException("exception encrypting data - " + e.toString());
        }
    }
}
