package org.bouncycastle.jcajce.provider.keystore.pkcs12;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import java.security.PublicKey;
import java.security.SecureRandom;
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
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
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
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
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
            super(new BCJcaJceHelper(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd40BitRC2_CBC);
        }
    }

    public static class BCPKCS12KeyStore3DES extends PKCS12KeyStoreSpi {
        public BCPKCS12KeyStore3DES() {
            super(new BCJcaJceHelper(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    /* access modifiers changed from: private */
    public class CertId {
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
            super(new DefaultJcaJceHelper(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd40BitRC2_CBC);
        }
    }

    public static class DefPKCS12KeyStore3DES extends PKCS12KeyStoreSpi {
        public DefPKCS12KeyStore3DES() {
            super(new DefaultJcaJceHelper(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    /* access modifiers changed from: private */
    public static class DefaultSecretKeyProvider {
        private final Map KEY_SIZES;

        DefaultSecretKeyProvider() {
            HashMap hashMap = new HashMap();
            hashMap.put(new ASN1ObjectIdentifier(CMSEnvelopedGenerator.CAST5_CBC), Integers.valueOf(128));
            hashMap.put(PKCSObjectIdentifiers.des_EDE3_CBC, Integers.valueOf(CertificateHolderAuthorization.CVCA));
            hashMap.put(NISTObjectIdentifiers.id_aes128_CBC, Integers.valueOf(128));
            hashMap.put(NISTObjectIdentifiers.id_aes192_CBC, Integers.valueOf(CertificateHolderAuthorization.CVCA));
            hashMap.put(NISTObjectIdentifiers.id_aes256_CBC, Integers.valueOf(256));
            hashMap.put(NTTObjectIdentifiers.id_camellia128_cbc, Integers.valueOf(128));
            hashMap.put(NTTObjectIdentifiers.id_camellia192_cbc, Integers.valueOf(CertificateHolderAuthorization.CVCA));
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

    /* access modifiers changed from: private */
    public static class IgnoresCaseHashtable {
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

        public int size() {
            return this.orig.size();
        }
    }

    public PKCS12KeyStoreSpi(JcaJceHelper jcaJceHelper, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this.keyAlgorithm = aSN1ObjectIdentifier;
        this.certAlgorithm = aSN1ObjectIdentifier2;
        try {
            this.certFact = jcaJceHelper.createCertificateFactory("X.509");
        } catch (Exception e) {
            throw new IllegalArgumentException("can't create cert factory - " + e.toString());
        }
    }

    private byte[] calculatePbeMac(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] bArr, int i, char[] cArr, boolean z, byte[] bArr2) throws Exception {
        PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(bArr, i);
        Mac createMac = this.helper.createMac(aSN1ObjectIdentifier.getId());
        createMac.init(new PKCS12Key(cArr, z), pBEParameterSpec);
        createMac.update(bArr2);
        return createMac.doFinal();
    }

    private Cipher createCipher(int i, char[] cArr, AlgorithmIdentifier algorithmIdentifier) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        AlgorithmParameterSpec algorithmParameterSpec;
        PBES2Parameters instance = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
        PBKDF2Params instance2 = PBKDF2Params.getInstance(instance.getKeyDerivationFunc().getParameters());
        AlgorithmIdentifier instance3 = AlgorithmIdentifier.getInstance(instance.getEncryptionScheme());
        SecretKeyFactory createSecretKeyFactory = this.helper.createSecretKeyFactory(instance.getKeyDerivationFunc().getAlgorithm().getId());
        SecretKey generateSecret = instance2.isDefaultPrf() ? createSecretKeyFactory.generateSecret(new PBEKeySpec(cArr, instance2.getSalt(), validateIterationCount(instance2.getIterationCount()), keySizeProvider.getKeySize(instance3))) : createSecretKeyFactory.generateSecret(new PBKDF2KeySpec(cArr, instance2.getSalt(), validateIterationCount(instance2.getIterationCount()), keySizeProvider.getKeySize(instance3), instance2.getPrf()));
        Cipher instance4 = Cipher.getInstance(instance.getEncryptionScheme().getAlgorithm().getId());
        ASN1Encodable parameters = instance.getEncryptionScheme().getParameters();
        if (parameters instanceof ASN1OctetString) {
            algorithmParameterSpec = new IvParameterSpec(ASN1OctetString.getInstance(parameters).getOctets());
        } else {
            GOST28147Parameters instance5 = GOST28147Parameters.getInstance(parameters);
            algorithmParameterSpec = new GOST28147ParameterSpec(instance5.getEncryptionParamSet(), instance5.getIV());
        }
        instance4.init(i, generateSecret, algorithmParameterSpec);
        return instance4;
    }

    private SafeBag createSafeBag(String str, Certificate certificate) throws CertificateEncodingException {
        CertBag certBag = new CertBag(x509Certificate, new DEROctetString(certificate.getEncoded()));
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        boolean z = false;
        if (certificate instanceof PKCS12BagAttributeCarrier) {
            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = (PKCS12BagAttributeCarrier) certificate;
            DERBMPString dERBMPString = (DERBMPString) pKCS12BagAttributeCarrier.getBagAttribute(pkcs_9_at_friendlyName);
            if ((dERBMPString == null || !dERBMPString.getString().equals(str)) && str != null) {
                pKCS12BagAttributeCarrier.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str));
            }
            Enumeration bagAttributeKeys = pKCS12BagAttributeCarrier.getBagAttributeKeys();
            while (bagAttributeKeys.hasMoreElements()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) bagAttributeKeys.nextElement();
                if (!aSN1ObjectIdentifier.equals((ASN1Primitive) PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                    ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                    aSN1EncodableVector2.add(aSN1ObjectIdentifier);
                    aSN1EncodableVector2.add(new DERSet(pKCS12BagAttributeCarrier.getBagAttribute(aSN1ObjectIdentifier)));
                    aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
                    z = true;
                }
            }
        }
        if (!z) {
            ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
            aSN1EncodableVector3.add(pkcs_9_at_friendlyName);
            aSN1EncodableVector3.add(new DERSet(new DERBMPString(str)));
            aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector3));
        }
        return new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(aSN1EncodableVector));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) {
        try {
            return new SubjectKeyIdentifier(getDigest(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())));
        } catch (Exception e) {
            throw new RuntimeException("error creating key");
        }
    }

    private void doStore(OutputStream outputStream, char[] cArr, boolean z) throws IOException {
        String str;
        boolean z2;
        boolean z3;
        ContentInfo contentInfo;
        int size = this.keys.size();
        String str2 = ASN1Encoding.BER;
        String str3 = ASN1Encoding.DER;
        if (size == 0) {
            if (cArr == null) {
                Enumeration keys2 = this.certs.keys();
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                while (keys2.hasMoreElements()) {
                    try {
                        String str4 = (String) keys2.nextElement();
                        aSN1EncodableVector.add(createSafeBag(str4, (Certificate) this.certs.get(str4)));
                    } catch (CertificateEncodingException e) {
                        throw new IOException("Error encoding certificate: " + e.toString());
                    }
                }
                ASN1ObjectIdentifier aSN1ObjectIdentifier = PKCSObjectIdentifiers.data;
                if (z) {
                    new DEROctetString(new DERSequence(aSN1EncodableVector).getEncoded());
                    new Pfx(new ContentInfo(PKCSObjectIdentifiers.data, new DEROctetString(new DERSequence(contentInfo).getEncoded())), null).encodeTo(outputStream, str3);
                    return;
                }
                contentInfo = new ContentInfo(aSN1ObjectIdentifier, new BEROctetString(new BERSequence(aSN1EncodableVector).getEncoded()));
                new Pfx(new ContentInfo(PKCSObjectIdentifiers.data, new BEROctetString(new BERSequence(contentInfo).getEncoded())), null).encodeTo(outputStream, str2);
                return;
            }
        } else if (cArr == null) {
            throw new NullPointerException("no password supplied for PKCS#12 KeyStore");
        }
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        Enumeration keys3 = this.keys.keys();
        while (keys3.hasMoreElements()) {
            byte[] bArr = new byte[20];
            this.random.nextBytes(bArr);
            String str5 = (String) keys3.nextElement();
            PrivateKey privateKey = (PrivateKey) this.keys.get(str5);
            PKCS12PBEParams pKCS12PBEParams = new PKCS12PBEParams(bArr, MIN_ITERATIONS);
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(this.keyAlgorithm, pKCS12PBEParams.toASN1Primitive()), wrapKey(this.keyAlgorithm.getId(), privateKey, pKCS12PBEParams, cArr));
            ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
            if (privateKey instanceof PKCS12BagAttributeCarrier) {
                PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = (PKCS12BagAttributeCarrier) privateKey;
                DERBMPString dERBMPString = (DERBMPString) pKCS12BagAttributeCarrier.getBagAttribute(pkcs_9_at_friendlyName);
                if (dERBMPString == null || !dERBMPString.getString().equals(str5)) {
                    pKCS12BagAttributeCarrier.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str5));
                }
                if (pKCS12BagAttributeCarrier.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                    pKCS12BagAttributeCarrier.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(engineGetCertificate(str5).getPublicKey()));
                }
                Enumeration bagAttributeKeys = pKCS12BagAttributeCarrier.getBagAttributeKeys();
                z3 = false;
                while (bagAttributeKeys.hasMoreElements()) {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier2 = (ASN1ObjectIdentifier) bagAttributeKeys.nextElement();
                    ASN1EncodableVector aSN1EncodableVector4 = new ASN1EncodableVector();
                    aSN1EncodableVector4.add(aSN1ObjectIdentifier2);
                    aSN1EncodableVector4.add(new DERSet(pKCS12BagAttributeCarrier.getBagAttribute(aSN1ObjectIdentifier2)));
                    aSN1EncodableVector3.add(new DERSequence(aSN1EncodableVector4));
                    z3 = true;
                }
            } else {
                z3 = false;
            }
            if (!z3) {
                ASN1EncodableVector aSN1EncodableVector5 = new ASN1EncodableVector();
                Certificate engineGetCertificate = engineGetCertificate(str5);
                aSN1EncodableVector5.add(pkcs_9_at_localKeyId);
                aSN1EncodableVector5.add(new DERSet(createSubjectKeyId(engineGetCertificate.getPublicKey())));
                aSN1EncodableVector3.add(new DERSequence(aSN1EncodableVector5));
                ASN1EncodableVector aSN1EncodableVector6 = new ASN1EncodableVector();
                aSN1EncodableVector6.add(pkcs_9_at_friendlyName);
                aSN1EncodableVector6.add(new DERSet(new DERBMPString(str5)));
                aSN1EncodableVector3.add(new DERSequence(aSN1EncodableVector6));
            }
            aSN1EncodableVector2.add(new SafeBag(pkcs8ShroudedKeyBag, encryptedPrivateKeyInfo.toASN1Primitive(), new DERSet(aSN1EncodableVector3)));
        }
        BEROctetString bEROctetString = new BEROctetString(new DERSequence(aSN1EncodableVector2).getEncoded(str3));
        byte[] bArr2 = new byte[20];
        this.random.nextBytes(bArr2);
        ASN1EncodableVector aSN1EncodableVector7 = new ASN1EncodableVector();
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(this.certAlgorithm, new PKCS12PBEParams(bArr2, MIN_ITERATIONS).toASN1Primitive());
        Hashtable hashtable = new Hashtable();
        Enumeration keys4 = this.keys.keys();
        while (keys4.hasMoreElements()) {
            try {
                String str6 = (String) keys4.nextElement();
                Certificate engineGetCertificate2 = engineGetCertificate(str6);
                CertBag certBag = new CertBag(x509Certificate, new DEROctetString(engineGetCertificate2.getEncoded()));
                ASN1EncodableVector aSN1EncodableVector8 = new ASN1EncodableVector();
                if (engineGetCertificate2 instanceof PKCS12BagAttributeCarrier) {
                    PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier2 = (PKCS12BagAttributeCarrier) engineGetCertificate2;
                    DERBMPString dERBMPString2 = (DERBMPString) pKCS12BagAttributeCarrier2.getBagAttribute(pkcs_9_at_friendlyName);
                    if (dERBMPString2 == null || !dERBMPString2.getString().equals(str6)) {
                        pKCS12BagAttributeCarrier2.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(str6));
                    }
                    if (pKCS12BagAttributeCarrier2.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                        pKCS12BagAttributeCarrier2.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(engineGetCertificate2.getPublicKey()));
                    }
                    Enumeration bagAttributeKeys2 = pKCS12BagAttributeCarrier2.getBagAttributeKeys();
                    z2 = false;
                    while (bagAttributeKeys2.hasMoreElements()) {
                        ASN1ObjectIdentifier aSN1ObjectIdentifier3 = (ASN1ObjectIdentifier) bagAttributeKeys2.nextElement();
                        ASN1EncodableVector aSN1EncodableVector9 = new ASN1EncodableVector();
                        aSN1EncodableVector9.add(aSN1ObjectIdentifier3);
                        aSN1EncodableVector9.add(new DERSet(pKCS12BagAttributeCarrier2.getBagAttribute(aSN1ObjectIdentifier3)));
                        aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector9));
                        bagAttributeKeys2 = bagAttributeKeys2;
                        str2 = str2;
                        z2 = true;
                    }
                    str = str2;
                } else {
                    str = str2;
                    z2 = false;
                }
                if (!z2) {
                    ASN1EncodableVector aSN1EncodableVector10 = new ASN1EncodableVector();
                    aSN1EncodableVector10.add(pkcs_9_at_localKeyId);
                    aSN1EncodableVector10.add(new DERSet(createSubjectKeyId(engineGetCertificate2.getPublicKey())));
                    aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector10));
                    ASN1EncodableVector aSN1EncodableVector11 = new ASN1EncodableVector();
                    aSN1EncodableVector11.add(pkcs_9_at_friendlyName);
                    aSN1EncodableVector11.add(new DERSet(new DERBMPString(str6)));
                    aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector11));
                }
                aSN1EncodableVector7.add(new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(aSN1EncodableVector8)));
                hashtable.put(engineGetCertificate2, engineGetCertificate2);
                keys4 = keys4;
                str2 = str;
            } catch (CertificateEncodingException e2) {
                throw new IOException("Error encoding certificate: " + e2.toString());
            }
        }
        Enumeration keys5 = this.certs.keys();
        while (keys5.hasMoreElements()) {
            try {
                String str7 = (String) keys5.nextElement();
                Certificate certificate = (Certificate) this.certs.get(str7);
                if (this.keys.get(str7) == null) {
                    aSN1EncodableVector7.add(createSafeBag(str7, certificate));
                    hashtable.put(certificate, certificate);
                }
            } catch (CertificateEncodingException e3) {
                throw new IOException("Error encoding certificate: " + e3.toString());
            }
        }
        Set usedCertificateSet = getUsedCertificateSet();
        Enumeration keys6 = this.chainCerts.keys();
        while (keys6.hasMoreElements()) {
            try {
                Certificate certificate2 = (Certificate) this.chainCerts.get((CertId) keys6.nextElement());
                if (usedCertificateSet.contains(certificate2)) {
                    if (hashtable.get(certificate2) == null) {
                        CertBag certBag2 = new CertBag(x509Certificate, new DEROctetString(certificate2.getEncoded()));
                        ASN1EncodableVector aSN1EncodableVector12 = new ASN1EncodableVector();
                        if (certificate2 instanceof PKCS12BagAttributeCarrier) {
                            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier3 = (PKCS12BagAttributeCarrier) certificate2;
                            Enumeration bagAttributeKeys3 = pKCS12BagAttributeCarrier3.getBagAttributeKeys();
                            while (bagAttributeKeys3.hasMoreElements()) {
                                ASN1ObjectIdentifier aSN1ObjectIdentifier4 = (ASN1ObjectIdentifier) bagAttributeKeys3.nextElement();
                                if (!aSN1ObjectIdentifier4.equals((ASN1Primitive) PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                    ASN1EncodableVector aSN1EncodableVector13 = new ASN1EncodableVector();
                                    aSN1EncodableVector13.add(aSN1ObjectIdentifier4);
                                    aSN1EncodableVector13.add(new DERSet(pKCS12BagAttributeCarrier3.getBagAttribute(aSN1ObjectIdentifier4)));
                                    aSN1EncodableVector12.add(new DERSequence(aSN1EncodableVector13));
                                    hashtable = hashtable;
                                }
                            }
                        }
                        aSN1EncodableVector7.add(new SafeBag(certBag, certBag2.toASN1Primitive(), new DERSet(aSN1EncodableVector12)));
                        hashtable = hashtable;
                    }
                }
            } catch (CertificateEncodingException e4) {
                throw new IOException("Error encoding certificate: " + e4.toString());
            }
        }
        ContentInfo contentInfo2 = new ContentInfo(data, new BEROctetString(new AuthenticatedSafe(new ContentInfo[]{new ContentInfo(data, bEROctetString), new ContentInfo(encryptedData, new EncryptedData(data, algorithmIdentifier, new BEROctetString(cryptData(true, algorithmIdentifier, cArr, false, new DERSequence(aSN1EncodableVector7).getEncoded(str3)))).toASN1Primitive())}).getEncoded(z ? str3 : str2)));
        byte[] bArr3 = new byte[this.saltLength];
        this.random.nextBytes(bArr3);
        try {
            Pfx pfx = new Pfx(contentInfo2, new MacData(new DigestInfo(this.macAlgorithm, calculatePbeMac(this.macAlgorithm.getAlgorithm(), bArr3, this.itCount, cArr, false, ((ASN1OctetString) contentInfo2.getContent()).getOctets())), bArr3, this.itCount));
            if (!z) {
                str3 = str2;
            }
            pfx.encodeTo(outputStream, str3);
        } catch (Exception e5) {
            throw new IOException("error constructing MAC: " + e5.toString());
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
        } else if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.id_PBES2)) {
            try {
                return createCipher(i, cArr, algorithmIdentifier).doFinal(bArr);
            } catch (Exception e2) {
                throw new IOException("exception decrypting data - " + e2.toString());
            }
        } else {
            throw new IOException("unknown PBE algorithm: " + algorithm);
        }
    }

    @Override // java.security.KeyStoreSpi
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

    @Override // java.security.KeyStoreSpi
    public boolean engineContainsAlias(String str) {
        return (this.certs.get(str) == null && this.keys.get(str) == null) ? false : true;
    }

    @Override // java.security.KeyStoreSpi
    public void engineDeleteEntry(String str) throws KeyStoreException {
        Key key = (Key) this.keys.remove(str);
        Certificate certificate = (Certificate) this.certs.remove(str);
        if (certificate != null) {
            this.chainCerts.remove(new CertId(certificate.getPublicKey()));
        }
        if (key != null) {
            String str2 = (String) this.localIds.remove(str);
            if (str2 != null) {
                certificate = (Certificate) this.keyCerts.remove(str2);
            }
            if (certificate != null) {
                this.chainCerts.remove(new CertId(certificate.getPublicKey()));
            }
        }
    }

    @Override // java.security.KeyStoreSpi
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

    @Override // java.security.KeyStoreSpi
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

    @Override // java.security.KeyStoreSpi
    public Certificate[] engineGetCertificateChain(String str) {
        Certificate engineGetCertificate;
        byte[] keyIdentifier;
        if (str == null) {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        } else if (!engineIsKeyEntry(str) || (engineGetCertificate = engineGetCertificate(str)) == null) {
            return null;
        } else {
            Vector vector = new Vector();
            while (engineGetCertificate != null) {
                X509Certificate x509Certificate = (X509Certificate) engineGetCertificate;
                byte[] extensionValue = x509Certificate.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                X509Certificate x509Certificate2 = (extensionValue == null || (keyIdentifier = AuthorityKeyIdentifier.getInstance(ASN1OctetString.getInstance(extensionValue).getOctets()).getKeyIdentifier()) == null) ? null : (Certificate) this.chainCerts.get(new CertId(keyIdentifier));
                if (x509Certificate2 == null) {
                    Principal issuerDN = x509Certificate.getIssuerDN();
                    if (!issuerDN.equals(x509Certificate.getSubjectDN())) {
                        Enumeration keys2 = this.chainCerts.keys();
                        while (true) {
                            if (!keys2.hasMoreElements()) {
                                break;
                            }
                            X509Certificate x509Certificate3 = (X509Certificate) this.chainCerts.get(keys2.nextElement());
                            if (x509Certificate3.getSubjectDN().equals(issuerDN)) {
                                try {
                                    x509Certificate.verify(x509Certificate3.getPublicKey());
                                    x509Certificate2 = x509Certificate3;
                                    break;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                if (!vector.contains(engineGetCertificate)) {
                    vector.addElement(engineGetCertificate);
                    if (x509Certificate2 != engineGetCertificate) {
                        engineGetCertificate = x509Certificate2;
                    }
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

    @Override // java.security.KeyStoreSpi
    public Date engineGetCreationDate(String str) {
        if (str == null) {
            throw new NullPointerException("alias == null");
        } else if (this.keys.get(str) == null && this.certs.get(str) == null) {
            return null;
        } else {
            return new Date();
        }
    }

    @Override // java.security.KeyStoreSpi
    public Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (str != null) {
            return (Key) this.keys.get(str);
        }
        throw new IllegalArgumentException("null alias passed to getKey.");
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineIsCertificateEntry(String str) {
        return this.certs.get(str) != null && this.keys.get(str) == null;
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineIsKeyEntry(String str) {
        return this.keys.get(str) != null;
    }

    @Override // java.security.KeyStoreSpi
    public void engineLoad(InputStream inputStream, char[] cArr) throws IOException {
        boolean z;
        boolean z2;
        String str;
        ASN1OctetString aSN1OctetString;
        int i;
        ASN1Sequence aSN1Sequence;
        ASN1Primitive aSN1Primitive;
        String str2;
        String str3;
        ASN1Primitive aSN1Primitive2;
        boolean z3;
        if (inputStream != null) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedInputStream.mark(10);
            if (bufferedInputStream.read() == 48) {
                bufferedInputStream.reset();
                try {
                    Pfx instance = Pfx.getInstance(new ASN1InputStream(bufferedInputStream).readObject());
                    ContentInfo authSafe = instance.getAuthSafe();
                    Vector vector = new Vector();
                    int i2 = 1;
                    int i3 = 0;
                    if (instance.getMacData() != null) {
                        if (cArr != null) {
                            MacData macData = instance.getMacData();
                            DigestInfo mac = macData.getMac();
                            this.macAlgorithm = mac.getAlgorithmId();
                            byte[] salt = macData.getSalt();
                            this.itCount = validateIterationCount(macData.getIterationCount());
                            this.saltLength = salt.length;
                            byte[] octets = ((ASN1OctetString) authSafe.getContent()).getOctets();
                            try {
                                byte[] calculatePbeMac = calculatePbeMac(this.macAlgorithm.getAlgorithm(), salt, this.itCount, cArr, false, octets);
                                byte[] digest = mac.getDigest();
                                if (Arrays.constantTimeAreEqual(calculatePbeMac, digest)) {
                                    z3 = false;
                                } else if (cArr.length > 0) {
                                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                                } else if (Arrays.constantTimeAreEqual(calculatePbeMac(this.macAlgorithm.getAlgorithm(), salt, this.itCount, cArr, true, octets), digest)) {
                                    z3 = true;
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
                            throw new NullPointerException("no password supplied when one expected");
                        }
                    } else if (cArr == null || Properties.isOverrideSet("org.bouncycastle.pkcs12.ignore_useless_passwd")) {
                        z = false;
                    } else {
                        throw new IOException("password supplied for keystore that does not require one");
                    }
                    ASN1OctetString aSN1OctetString2 = null;
                    this.keys = new IgnoresCaseHashtable();
                    this.localIds = new Hashtable();
                    if (authSafe.getContentType().equals((ASN1Primitive) data)) {
                        ContentInfo[] contentInfo = AuthenticatedSafe.getInstance(ASN1OctetString.getInstance(authSafe.getContent()).getOctets()).getContentInfo();
                        int i4 = 0;
                        z2 = false;
                        while (i4 != contentInfo.length) {
                            if (contentInfo[i4].getContentType().equals((ASN1Primitive) data)) {
                                ASN1Sequence instance2 = ASN1Sequence.getInstance(ASN1OctetString.getInstance(contentInfo[i4].getContent()).getOctets());
                                int i5 = i3;
                                while (i5 != instance2.size()) {
                                    SafeBag instance3 = SafeBag.getInstance(instance2.getObjectAt(i5));
                                    if (instance3.getBagId().equals((ASN1Primitive) pkcs8ShroudedKeyBag)) {
                                        EncryptedPrivateKeyInfo instance4 = EncryptedPrivateKeyInfo.getInstance(instance3.getBagValue());
                                        PrivateKey unwrapKey = unwrapKey(instance4.getEncryptionAlgorithm(), instance4.getEncryptedData(), cArr, z);
                                        if (instance3.getBagAttributes() != null) {
                                            Enumeration objects = instance3.getBagAttributes().getObjects();
                                            str3 = aSN1OctetString2;
                                            str2 = str3;
                                            while (objects.hasMoreElements()) {
                                                ASN1Sequence aSN1Sequence2 = (ASN1Sequence) objects.nextElement();
                                                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) aSN1Sequence2.getObjectAt(i3);
                                                ASN1Set aSN1Set = (ASN1Set) aSN1Sequence2.getObjectAt(i2);
                                                if (aSN1Set.size() > 0) {
                                                    aSN1Primitive2 = (ASN1Primitive) aSN1Set.getObjectAt(0);
                                                    if (unwrapKey instanceof PKCS12BagAttributeCarrier) {
                                                        PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = (PKCS12BagAttributeCarrier) unwrapKey;
                                                        ASN1Encodable bagAttribute = pKCS12BagAttributeCarrier.getBagAttribute(aSN1ObjectIdentifier);
                                                        if (bagAttribute == null) {
                                                            pKCS12BagAttributeCarrier.setBagAttribute(aSN1ObjectIdentifier, aSN1Primitive2);
                                                        } else if (!bagAttribute.toASN1Primitive().equals(aSN1Primitive2)) {
                                                            throw new IOException("attempt to add existing attribute with different value");
                                                        }
                                                    }
                                                } else {
                                                    aSN1Primitive2 = null;
                                                }
                                                if (aSN1ObjectIdentifier.equals((ASN1Primitive) pkcs_9_at_friendlyName)) {
                                                    str3 = ((DERBMPString) aSN1Primitive2).getString();
                                                    this.keys.put(str3, unwrapKey);
                                                } else if (aSN1ObjectIdentifier.equals((ASN1Primitive) pkcs_9_at_localKeyId)) {
                                                    str2 = (ASN1OctetString) aSN1Primitive2;
                                                }
                                                i2 = 1;
                                                i3 = 0;
                                            }
                                        } else {
                                            str3 = null;
                                            str2 = null;
                                        }
                                        if (str2 != null) {
                                            String str4 = new String(Hex.encode(str2.getOctets()));
                                            if (str3 == null) {
                                                this.keys.put(str4, unwrapKey);
                                            } else {
                                                this.localIds.put(str3, str4);
                                            }
                                        } else {
                                            this.keys.put("unmarked", unwrapKey);
                                            z2 = true;
                                        }
                                    } else if (instance3.getBagId().equals((ASN1Primitive) certBag)) {
                                        vector.addElement(instance3);
                                    } else {
                                        System.out.println("extra in data " + instance3.getBagId());
                                        System.out.println(ASN1Dump.dumpAsString(instance3));
                                    }
                                    i5++;
                                    i2 = 1;
                                    i3 = 0;
                                    aSN1OctetString2 = null;
                                }
                                i = i4;
                            } else if (contentInfo[i4].getContentType().equals((ASN1Primitive) encryptedData)) {
                                EncryptedData instance5 = EncryptedData.getInstance(contentInfo[i4].getContent());
                                i = i4;
                                ASN1Sequence instance6 = ASN1Sequence.getInstance(cryptData(false, instance5.getEncryptionAlgorithm(), cArr, z, instance5.getContent().getOctets()));
                                int i6 = 0;
                                while (i6 != instance6.size()) {
                                    SafeBag instance7 = SafeBag.getInstance(instance6.getObjectAt(i6));
                                    if (instance7.getBagId().equals((ASN1Primitive) certBag)) {
                                        vector.addElement(instance7);
                                        aSN1Sequence = instance6;
                                    } else if (instance7.getBagId().equals((ASN1Primitive) pkcs8ShroudedKeyBag)) {
                                        EncryptedPrivateKeyInfo instance8 = EncryptedPrivateKeyInfo.getInstance(instance7.getBagValue());
                                        PrivateKey unwrapKey2 = unwrapKey(instance8.getEncryptionAlgorithm(), instance8.getEncryptedData(), cArr, z);
                                        PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier2 = (PKCS12BagAttributeCarrier) unwrapKey2;
                                        ASN1OctetString aSN1OctetString3 = null;
                                        String str5 = null;
                                        for (Enumeration objects2 = instance7.getBagAttributes().getObjects(); objects2.hasMoreElements(); objects2 = objects2) {
                                            ASN1Sequence aSN1Sequence3 = (ASN1Sequence) objects2.nextElement();
                                            ASN1ObjectIdentifier aSN1ObjectIdentifier2 = (ASN1ObjectIdentifier) aSN1Sequence3.getObjectAt(0);
                                            ASN1Set aSN1Set2 = (ASN1Set) aSN1Sequence3.getObjectAt(1);
                                            if (aSN1Set2.size() > 0) {
                                                aSN1Primitive = (ASN1Primitive) aSN1Set2.getObjectAt(0);
                                                ASN1Encodable bagAttribute2 = pKCS12BagAttributeCarrier2.getBagAttribute(aSN1ObjectIdentifier2);
                                                if (bagAttribute2 == null) {
                                                    pKCS12BagAttributeCarrier2.setBagAttribute(aSN1ObjectIdentifier2, aSN1Primitive);
                                                } else if (!bagAttribute2.toASN1Primitive().equals(aSN1Primitive)) {
                                                    throw new IOException("attempt to add existing attribute with different value");
                                                }
                                            } else {
                                                aSN1Primitive = null;
                                            }
                                            if (aSN1ObjectIdentifier2.equals((ASN1Primitive) pkcs_9_at_friendlyName)) {
                                                String string = ((DERBMPString) aSN1Primitive).getString();
                                                this.keys.put(string, unwrapKey2);
                                                str5 = string;
                                            } else if (aSN1ObjectIdentifier2.equals((ASN1Primitive) pkcs_9_at_localKeyId)) {
                                                aSN1OctetString3 = (ASN1OctetString) aSN1Primitive;
                                            }
                                            instance6 = instance6;
                                        }
                                        aSN1Sequence = instance6;
                                        String str6 = new String(Hex.encode(aSN1OctetString3.getOctets()));
                                        if (str5 == null) {
                                            this.keys.put(str6, unwrapKey2);
                                        } else {
                                            this.localIds.put(str5, str6);
                                        }
                                    } else {
                                        aSN1Sequence = instance6;
                                        if (instance7.getBagId().equals((ASN1Primitive) keyBag)) {
                                            PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(instance7.getBagValue()));
                                            PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier3 = (PKCS12BagAttributeCarrier) privateKey;
                                            ASN1OctetString aSN1OctetString4 = null;
                                            String str7 = null;
                                            for (Enumeration objects3 = instance7.getBagAttributes().getObjects(); objects3.hasMoreElements(); objects3 = objects3) {
                                                ASN1Sequence instance9 = ASN1Sequence.getInstance(objects3.nextElement());
                                                ASN1ObjectIdentifier instance10 = ASN1ObjectIdentifier.getInstance(instance9.getObjectAt(0));
                                                ASN1Set instance11 = ASN1Set.getInstance(instance9.getObjectAt(1));
                                                if (instance11.size() > 0) {
                                                    ASN1Primitive aSN1Primitive3 = (ASN1Primitive) instance11.getObjectAt(0);
                                                    ASN1Encodable bagAttribute3 = pKCS12BagAttributeCarrier3.getBagAttribute(instance10);
                                                    if (bagAttribute3 == null) {
                                                        pKCS12BagAttributeCarrier3.setBagAttribute(instance10, aSN1Primitive3);
                                                    } else if (!bagAttribute3.toASN1Primitive().equals(aSN1Primitive3)) {
                                                        throw new IOException("attempt to add existing attribute with different value");
                                                    }
                                                    if (instance10.equals((ASN1Primitive) pkcs_9_at_friendlyName)) {
                                                        String string2 = ((DERBMPString) aSN1Primitive3).getString();
                                                        this.keys.put(string2, privateKey);
                                                        str7 = string2;
                                                    } else if (instance10.equals((ASN1Primitive) pkcs_9_at_localKeyId)) {
                                                        aSN1OctetString4 = (ASN1OctetString) aSN1Primitive3;
                                                    }
                                                }
                                            }
                                            String str8 = new String(Hex.encode(aSN1OctetString4.getOctets()));
                                            if (str7 == null) {
                                                this.keys.put(str8, privateKey);
                                            } else {
                                                this.localIds.put(str7, str8);
                                            }
                                        } else {
                                            System.out.println("extra in encryptedData " + instance7.getBagId());
                                            System.out.println(ASN1Dump.dumpAsString(instance7));
                                        }
                                    }
                                    i6++;
                                    instance6 = aSN1Sequence;
                                }
                                continue;
                            } else {
                                i = i4;
                                System.out.println("extra " + contentInfo[i].getContentType().getId());
                                System.out.println("extra " + ASN1Dump.dumpAsString(contentInfo[i].getContent()));
                            }
                            i4 = i + 1;
                            i2 = 1;
                            i3 = 0;
                            aSN1OctetString2 = null;
                        }
                    } else {
                        z2 = false;
                    }
                    this.certs = new IgnoresCaseHashtable();
                    this.chainCerts = new Hashtable();
                    this.keyCerts = new Hashtable();
                    for (int i7 = 0; i7 != vector.size(); i7++) {
                        SafeBag safeBag = (SafeBag) vector.elementAt(i7);
                        CertBag instance12 = CertBag.getInstance(safeBag.getBagValue());
                        if (instance12.getCertId().equals((ASN1Primitive) x509Certificate)) {
                            try {
                                Certificate generateCertificate = this.certFact.generateCertificate(new ByteArrayInputStream(((ASN1OctetString) instance12.getCertValue()).getOctets()));
                                if (safeBag.getBagAttributes() != null) {
                                    Enumeration objects4 = safeBag.getBagAttributes().getObjects();
                                    aSN1OctetString = null;
                                    str = null;
                                    while (objects4.hasMoreElements()) {
                                        ASN1Sequence instance13 = ASN1Sequence.getInstance(objects4.nextElement());
                                        ASN1ObjectIdentifier instance14 = ASN1ObjectIdentifier.getInstance(instance13.getObjectAt(0));
                                        ASN1Set instance15 = ASN1Set.getInstance(instance13.getObjectAt(1));
                                        if (instance15.size() > 0) {
                                            ASN1Primitive aSN1Primitive4 = (ASN1Primitive) instance15.getObjectAt(0);
                                            if (generateCertificate instanceof PKCS12BagAttributeCarrier) {
                                                PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier4 = (PKCS12BagAttributeCarrier) generateCertificate;
                                                ASN1Encodable bagAttribute4 = pKCS12BagAttributeCarrier4.getBagAttribute(instance14);
                                                if (bagAttribute4 == null) {
                                                    pKCS12BagAttributeCarrier4.setBagAttribute(instance14, aSN1Primitive4);
                                                } else if (!bagAttribute4.toASN1Primitive().equals(aSN1Primitive4)) {
                                                    throw new IOException("attempt to add existing attribute with different value");
                                                }
                                            }
                                            if (instance14.equals((ASN1Primitive) pkcs_9_at_friendlyName)) {
                                                str = ((DERBMPString) aSN1Primitive4).getString();
                                            } else if (instance14.equals((ASN1Primitive) pkcs_9_at_localKeyId)) {
                                                aSN1OctetString = (ASN1OctetString) aSN1Primitive4;
                                            }
                                        }
                                    }
                                } else {
                                    aSN1OctetString = null;
                                    str = null;
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
                                    String str9 = new String(Hex.encode(createSubjectKeyId(generateCertificate.getPublicKey()).getKeyIdentifier()));
                                    this.keyCerts.put(str9, generateCertificate);
                                    IgnoresCaseHashtable ignoresCaseHashtable = this.keys;
                                    ignoresCaseHashtable.put(str9, ignoresCaseHashtable.remove("unmarked"));
                                }
                            } catch (Exception e3) {
                                throw new RuntimeException(e3.toString());
                            }
                        } else {
                            throw new RuntimeException("Unsupported certificate type: " + instance12.getCertId());
                        }
                    }
                } catch (Exception e4) {
                    throw new IOException(e4.getMessage());
                }
            } else {
                throw new IOException("stream does not represent a PKCS12 key store");
            }
        }
    }

    @Override // java.security.KeyStoreSpi
    public void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
        if (this.keys.get(str) == null) {
            this.certs.put(str, certificate);
            this.chainCerts.put(new CertId(certificate.getPublicKey()), certificate);
            return;
        }
        throw new KeyStoreException("There is a key entry with the name " + str + ".");
    }

    @Override // java.security.KeyStoreSpi
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

    @Override // java.security.KeyStoreSpi
    public void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
        throw new RuntimeException("operation not supported");
    }

    @Override // java.security.KeyStoreSpi
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

    @Override // java.security.KeyStoreSpi
    public void engineStore(OutputStream outputStream, char[] cArr) throws IOException {
        doStore(outputStream, cArr, false);
    }

    @Override // java.security.KeyStoreSpi
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

    @Override // org.bouncycastle.jce.interfaces.BCKeyStore
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
            } else if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.id_PBES2)) {
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
