package com.android.org.bouncycastle.jcajce.provider.keystore.pkcs12;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.BEROctetString;
import com.android.org.bouncycastle.asn1.BEROutputStream;
import com.android.org.bouncycastle.asn1.DERBMPString;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DEROutputStream;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERSet;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import com.android.org.bouncycastle.asn1.pkcs.CertBag;
import com.android.org.bouncycastle.asn1.pkcs.ContentInfo;
import com.android.org.bouncycastle.asn1.pkcs.EncryptedData;
import com.android.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import com.android.org.bouncycastle.asn1.pkcs.MacData;
import com.android.org.bouncycastle.asn1.pkcs.PBES2Parameters;
import com.android.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import com.android.org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.Pfx;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.pkcs.SafeBag;
import com.android.org.bouncycastle.asn1.util.ASN1Dump;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import com.android.org.bouncycastle.asn1.x509.DigestInfo;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.jcajce.PKCS12Key;
import com.android.org.bouncycastle.jcajce.PKCS12StoreParameter;
import com.android.org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.interfaces.BCKeyStore;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.provider.JDKPKCS12StoreParameter;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Integers;
import com.android.org.bouncycastle.util.Strings;
import com.android.org.bouncycastle.util.encoders.Hex;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

public class PKCS12KeyStoreSpi extends KeyStoreSpi implements PKCSObjectIdentifiers, X509ObjectIdentifiers, BCKeyStore {
    static final int CERTIFICATE = 1;
    static final int KEY = 2;
    static final int KEY_PRIVATE = 0;
    static final int KEY_PUBLIC = 1;
    static final int KEY_SECRET = 2;
    private static final int MIN_ITERATIONS = 1024;
    static final int NULL = 0;
    private static final int SALT_SIZE = 20;
    static final int SEALED = 4;
    static final int SECRET = 3;
    private static final DefaultSecretKeyProvider keySizeProvider = new DefaultSecretKeyProvider();
    private ASN1ObjectIdentifier certAlgorithm;
    private CertificateFactory certFact;
    private IgnoresCaseHashtable certs = new IgnoresCaseHashtable();
    private Hashtable chainCerts = new Hashtable();
    private final JcaJceHelper helper = new DefaultJcaJceHelper();
    private ASN1ObjectIdentifier keyAlgorithm;
    private Hashtable keyCerts = new Hashtable();
    private IgnoresCaseHashtable keys = new IgnoresCaseHashtable();
    private Hashtable localIds = new Hashtable();
    protected SecureRandom random = new SecureRandom();

    public static class BCPKCS12KeyStore extends PKCS12KeyStoreSpi {
        public BCPKCS12KeyStore() {
            super(new BouncyCastleProvider(), pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd40BitRC2_CBC);
        }
    }

    private class CertId {
        byte[] id;

        CertId(PublicKey key) {
            this.id = PKCS12KeyStoreSpi.this.createSubjectKeyId(key).getKeyIdentifier();
        }

        CertId(byte[] id2) {
            this.id = id2;
        }

        public int hashCode() {
            return Arrays.hashCode(this.id);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof CertId)) {
                return false;
            }
            return Arrays.areEqual(this.id, ((CertId) o).id);
        }
    }

    private static class DefaultSecretKeyProvider {
        private final Map KEY_SIZES;

        DefaultSecretKeyProvider() {
            Map keySizes = new HashMap();
            keySizes.put(new ASN1ObjectIdentifier("1.2.840.113533.7.66.10"), Integers.valueOf(128));
            keySizes.put(PKCSObjectIdentifiers.des_EDE3_CBC, Integers.valueOf(192));
            keySizes.put(NISTObjectIdentifiers.id_aes128_CBC, Integers.valueOf(128));
            keySizes.put(NISTObjectIdentifiers.id_aes192_CBC, Integers.valueOf(192));
            keySizes.put(NISTObjectIdentifiers.id_aes256_CBC, Integers.valueOf(256));
            keySizes.put(NTTObjectIdentifiers.id_camellia128_cbc, Integers.valueOf(128));
            keySizes.put(NTTObjectIdentifiers.id_camellia192_cbc, Integers.valueOf(192));
            keySizes.put(NTTObjectIdentifiers.id_camellia256_cbc, Integers.valueOf(256));
            this.KEY_SIZES = Collections.unmodifiableMap(keySizes);
        }

        public int getKeySize(AlgorithmIdentifier algorithmIdentifier) {
            Integer keySize = (Integer) this.KEY_SIZES.get(algorithmIdentifier.getAlgorithm());
            if (keySize != null) {
                return keySize.intValue();
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

        public void put(String key, Object value) {
            String lower = key == null ? null : Strings.toLowerCase(key);
            String k = (String) this.keys.get(lower);
            if (k != null) {
                this.orig.remove(k);
            }
            this.keys.put(lower, key);
            this.orig.put(key, value);
        }

        public Enumeration keys() {
            return this.orig.keys();
        }

        public Object remove(String alias) {
            String k = (String) this.keys.remove(alias == null ? null : Strings.toLowerCase(alias));
            if (k == null) {
                return null;
            }
            return this.orig.remove(k);
        }

        public Object get(String alias) {
            String k = (String) this.keys.get(alias == null ? null : Strings.toLowerCase(alias));
            if (k == null) {
                return null;
            }
            return this.orig.get(k);
        }

        public Enumeration elements() {
            return this.orig.elements();
        }
    }

    public PKCS12KeyStoreSpi(Provider provider, ASN1ObjectIdentifier keyAlgorithm2, ASN1ObjectIdentifier certAlgorithm2) {
        this.keyAlgorithm = keyAlgorithm2;
        this.certAlgorithm = certAlgorithm2;
        if (provider != null) {
            try {
                this.certFact = CertificateFactory.getInstance("X.509", provider);
            } catch (Exception e) {
                throw new IllegalArgumentException("can't create cert factory - " + e.toString());
            }
        } else {
            this.certFact = CertificateFactory.getInstance("X.509");
        }
    }

    /* access modifiers changed from: private */
    public SubjectKeyIdentifier createSubjectKeyId(PublicKey pubKey) {
        try {
            return new SubjectKeyIdentifier(getDigest(SubjectPublicKeyInfo.getInstance(pubKey.getEncoded())));
        } catch (Exception e) {
            throw new RuntimeException("error creating key");
        }
    }

    private static byte[] getDigest(SubjectPublicKeyInfo spki) {
        Digest digest = AndroidDigestFactory.getSHA1();
        byte[] resBuf = new byte[digest.getDigestSize()];
        byte[] bytes = spki.getPublicKeyData().getBytes();
        digest.update(bytes, 0, bytes.length);
        digest.doFinal(resBuf, 0);
        return resBuf;
    }

    public void setRandom(SecureRandom rand) {
        this.random = rand;
    }

    public Enumeration engineAliases() {
        Hashtable tab = new Hashtable();
        Enumeration e = this.certs.keys();
        while (e.hasMoreElements()) {
            tab.put(e.nextElement(), "cert");
        }
        Enumeration e2 = this.keys.keys();
        while (e2.hasMoreElements()) {
            String a = (String) e2.nextElement();
            if (tab.get(a) == null) {
                tab.put(a, "key");
            }
        }
        return tab.keys();
    }

    public boolean engineContainsAlias(String alias) {
        return (this.certs.get(alias) == null && this.keys.get(alias) == null) ? false : true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.security.cert.Certificate} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        Key k = (Key) this.keys.remove(alias);
        Certificate c = (Certificate) this.certs.remove(alias);
        if (c != null) {
            this.chainCerts.remove(new CertId(c.getPublicKey()));
        }
        if (k != null) {
            String id = (String) this.localIds.remove(alias);
            if (id != null) {
                c = this.keyCerts.remove(id);
            }
            if (c != null) {
                this.chainCerts.remove(new CertId(c.getPublicKey()));
            }
        }
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias != null) {
            Certificate c = (Certificate) this.certs.get(alias);
            if (c != null) {
                return c;
            }
            String id = (String) this.localIds.get(alias);
            if (id != null) {
                return (Certificate) this.keyCerts.get(id);
            }
            return (Certificate) this.keyCerts.get(alias);
        }
        throw new IllegalArgumentException("null alias passed to getCertificate.");
    }

    public String engineGetCertificateAlias(Certificate cert) {
        Enumeration c = this.certs.elements();
        Enumeration k = this.certs.keys();
        while (c.hasMoreElements()) {
            String ta = (String) k.nextElement();
            if (((Certificate) c.nextElement()).equals(cert)) {
                return ta;
            }
        }
        Enumeration c2 = this.keyCerts.elements();
        Enumeration k2 = this.keyCerts.keys();
        while (c2.hasMoreElements()) {
            String ta2 = (String) k2.nextElement();
            if (((Certificate) c2.nextElement()).equals(cert)) {
                return ta2;
            }
        }
        return null;
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        } else if (!engineIsKeyEntry(alias)) {
            return null;
        } else {
            Certificate c = engineGetCertificate(alias);
            if (c == null) {
                return null;
            }
            Vector cs = new Vector();
            while (c != null) {
                X509Certificate x509c = (X509Certificate) c;
                Certificate nextC = null;
                byte[] bytes = x509c.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                if (bytes != null) {
                    try {
                        AuthorityKeyIdentifier id = AuthorityKeyIdentifier.getInstance(new ASN1InputStream(((ASN1OctetString) new ASN1InputStream(bytes).readObject()).getOctets()).readObject());
                        if (id.getKeyIdentifier() != null) {
                            nextC = (Certificate) this.chainCerts.get(new CertId(id.getKeyIdentifier()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.toString());
                    }
                }
                if (nextC == null) {
                    Principal i = x509c.getIssuerDN();
                    if (!i.equals(x509c.getSubjectDN())) {
                        Enumeration e2 = this.chainCerts.keys();
                        while (true) {
                            if (!e2.hasMoreElements()) {
                                break;
                            }
                            X509Certificate crt = (X509Certificate) this.chainCerts.get(e2.nextElement());
                            if (crt.getSubjectDN().equals(i)) {
                                try {
                                    x509c.verify(crt.getPublicKey());
                                    nextC = crt;
                                    break;
                                } catch (Exception e3) {
                                }
                            }
                        }
                    }
                }
                if (cs.contains(c)) {
                    c = null;
                } else {
                    cs.addElement(c);
                    if (nextC != c) {
                        c = nextC;
                    } else {
                        c = null;
                    }
                }
            }
            Certificate[] certChain = new Certificate[cs.size()];
            for (int i2 = 0; i2 != certChain.length; i2++) {
                certChain[i2] = (Certificate) cs.elementAt(i2);
            }
            return certChain;
        }
    }

    public Date engineGetCreationDate(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (this.keys.get(alias) == null && this.certs.get(alias) == null) {
            return null;
        } else {
            return new Date();
        }
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (alias != null) {
            return (Key) this.keys.get(alias);
        }
        throw new IllegalArgumentException("null alias passed to getKey.");
    }

    public boolean engineIsCertificateEntry(String alias) {
        return this.certs.get(alias) != null && this.keys.get(alias) == null;
    }

    public boolean engineIsKeyEntry(String alias) {
        return this.keys.get(alias) != null;
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (this.keys.get(alias) == null) {
            this.certs.put(alias, cert);
            this.chainCerts.put(new CertId(cert.getPublicKey()), cert);
            return;
        }
        throw new KeyStoreException("There is a key entry with the name " + alias + ".");
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        throw new RuntimeException("operation not supported");
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (!(key instanceof PrivateKey)) {
            throw new KeyStoreException("PKCS12 does not support non-PrivateKeys");
        } else if (!(key instanceof PrivateKey) || chain != null) {
            if (this.keys.get(alias) != null) {
                engineDeleteEntry(alias);
            }
            this.keys.put(alias, key);
            if (chain != null) {
                int i = 0;
                this.certs.put(alias, chain[0]);
                while (true) {
                    int i2 = i;
                    if (i2 != chain.length) {
                        this.chainCerts.put(new CertId(chain[i2].getPublicKey()), chain[i2]);
                        i = i2 + 1;
                    } else {
                        return;
                    }
                }
            }
        } else {
            throw new KeyStoreException("no certificate chain for private key");
        }
    }

    public int engineSize() {
        Hashtable tab = new Hashtable();
        Enumeration e = this.certs.keys();
        while (e.hasMoreElements()) {
            tab.put(e.nextElement(), "cert");
        }
        Enumeration e2 = this.keys.keys();
        while (e2.hasMoreElements()) {
            String a = (String) e2.nextElement();
            if (tab.get(a) == null) {
                tab.put(a, "key");
            }
        }
        return tab.size();
    }

    /* access modifiers changed from: protected */
    public PrivateKey unwrapKey(AlgorithmIdentifier algId, byte[] data, char[] password, boolean wrongPKCS12Zero) throws IOException {
        ASN1ObjectIdentifier algorithm = algId.getAlgorithm();
        try {
            if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                PKCS12PBEParams pbeParams = PKCS12PBEParams.getInstance(algId.getParameters());
                PBEParameterSpec defParams = new PBEParameterSpec(pbeParams.getIV(), pbeParams.getIterations().intValue());
                Cipher cipher = this.helper.createCipher(algorithm.getId());
                cipher.init(4, new PKCS12Key(password, wrongPKCS12Zero), defParams);
                return (PrivateKey) cipher.unwrap(data, "", 2);
            } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
                return (PrivateKey) createCipher(4, password, algId).unwrap(data, "", 2);
            } else {
                throw new IOException("exception unwrapping private key - cannot recognise: " + algorithm);
            }
        } catch (Exception e) {
            throw new IOException("exception unwrapping private key - " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public byte[] wrapKey(String algorithm, Key key, PKCS12PBEParams pbeParams, char[] password) throws IOException {
        PBEKeySpec pbeSpec = new PBEKeySpec(password);
        try {
            SecretKeyFactory keyFact = this.helper.createSecretKeyFactory(algorithm);
            PBEParameterSpec defParams = new PBEParameterSpec(pbeParams.getIV(), pbeParams.getIterations().intValue());
            Cipher cipher = this.helper.createCipher(algorithm);
            cipher.init(3, keyFact.generateSecret(pbeSpec), defParams);
            return cipher.wrap(key);
        } catch (Exception e) {
            throw new IOException("exception encrypting data - " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public byte[] cryptData(boolean forEncryption, AlgorithmIdentifier algId, char[] password, boolean wrongPKCS12Zero, byte[] data) throws IOException {
        ASN1ObjectIdentifier algorithm = algId.getAlgorithm();
        int mode = forEncryption ? 1 : 2;
        if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
            PKCS12PBEParams pbeParams = PKCS12PBEParams.getInstance(algId.getParameters());
            new PBEKeySpec(password);
            try {
                PBEParameterSpec defParams = new PBEParameterSpec(pbeParams.getIV(), pbeParams.getIterations().intValue());
                PKCS12Key key = new PKCS12Key(password, wrongPKCS12Zero);
                Cipher cipher = this.helper.createCipher(algorithm.getId());
                cipher.init(mode, key, defParams);
                return cipher.doFinal(data);
            } catch (Exception e) {
                throw new IOException("exception decrypting data - " + e.toString());
            }
        } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
            try {
                return createCipher(mode, password, algId).doFinal(data);
            } catch (Exception e2) {
                throw new IOException("exception decrypting data - " + e2.toString());
            }
        } else {
            throw new IOException("unknown PBE algorithm: " + algorithm);
        }
    }

    private Cipher createCipher(int mode, char[] password, AlgorithmIdentifier algId) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        SecretKey key;
        PBES2Parameters alg = PBES2Parameters.getInstance(algId.getParameters());
        PBKDF2Params func = PBKDF2Params.getInstance(alg.getKeyDerivationFunc().getParameters());
        AlgorithmIdentifier encScheme = AlgorithmIdentifier.getInstance(alg.getEncryptionScheme());
        SecretKeyFactory keyFact = this.helper.createSecretKeyFactory(alg.getKeyDerivationFunc().getAlgorithm().getId());
        if (func.isDefaultPrf()) {
            key = keyFact.generateSecret(new PBEKeySpec(password, func.getSalt(), func.getIterationCount().intValue(), keySizeProvider.getKeySize(encScheme)));
        } else {
            PBKDF2KeySpec pBKDF2KeySpec = new PBKDF2KeySpec(password, func.getSalt(), func.getIterationCount().intValue(), keySizeProvider.getKeySize(encScheme), func.getPrf());
            key = keyFact.generateSecret(pBKDF2KeySpec);
        }
        Cipher cipher = Cipher.getInstance(alg.getEncryptionScheme().getAlgorithm().getId());
        AlgorithmIdentifier instance = AlgorithmIdentifier.getInstance(alg.getEncryptionScheme());
        ASN1Encodable encParams = alg.getEncryptionScheme().getParameters();
        if (encParams instanceof ASN1OctetString) {
            cipher.init(mode, key, new IvParameterSpec(ASN1OctetString.getInstance(encParams).getOctets()));
        }
        return cipher;
    }

    /* JADX WARNING: type inference failed for: r14v3, types: [java.lang.Object, com.android.org.bouncycastle.asn1.ASN1Primitive, com.android.org.bouncycastle.asn1.ASN1Encodable] */
    /* JADX WARNING: type inference failed for: r9v17, types: [java.lang.Object, com.android.org.bouncycastle.asn1.ASN1Primitive, com.android.org.bouncycastle.asn1.ASN1Encodable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 2 */
    public void engineLoad(InputStream stream, char[] password) throws IOException {
        ContentInfo info;
        Vector chain;
        boolean wrongPKCS12Zero;
        char[] cArr;
        ASN1InputStream bIn;
        Vector chain2;
        SafeBag b;
        ASN1OctetString localId;
        ContentInfo info2;
        boolean wrongPKCS12Zero2;
        ASN1InputStream bIn2;
        char[] cArr2;
        Vector chain3;
        ContentInfo info3;
        boolean wrongPKCS12Zero3;
        ASN1Sequence seq;
        byte[] octets;
        EncryptedData d;
        ContentInfo info4;
        ASN1Object aSN1Object;
        ASN1Sequence seq2;
        ASN1InputStream dIn;
        ASN1InputStream bIn3;
        Vector chain4;
        EncryptedPrivateKeyInfo eIn;
        ASN1Primitive attr;
        InputStream inputStream = stream;
        char[] cArr3 = password;
        if (inputStream != null) {
            if (cArr3 != null) {
                BufferedInputStream bufIn = new BufferedInputStream(inputStream);
                bufIn.mark(10);
                int head = bufIn.read();
                if (head == 48) {
                    bufIn.reset();
                    ASN1InputStream bIn4 = new ASN1InputStream((InputStream) bufIn);
                    ASN1Sequence obj = (ASN1Sequence) bIn4.readObject();
                    Pfx bag = Pfx.getInstance(obj);
                    ContentInfo info5 = bag.getAuthSafe();
                    Vector chain5 = new Vector();
                    boolean unmarkedKey = false;
                    boolean wrongPKCS12Zero4 = false;
                    if (bag.getMacData() != null) {
                        MacData mData = bag.getMacData();
                        DigestInfo dInfo = mData.getMac();
                        AlgorithmIdentifier algId = dInfo.getAlgorithmId();
                        byte[] salt = mData.getSalt();
                        int itCount = mData.getIterationCount().intValue();
                        byte[] data = ((ASN1OctetString) info5.getContent()).getOctets();
                        try {
                            AlgorithmIdentifier algId2 = algId;
                            DigestInfo dInfo2 = dInfo;
                            MacData macData = mData;
                            chain = chain5;
                            info = info5;
                            try {
                                byte[] res = calculatePbeMac(algId.getAlgorithm(), salt, itCount, cArr3, false, data);
                                try {
                                    byte[] dig = dInfo2.getDigest();
                                    if (Arrays.constantTimeAreEqual(res, dig)) {
                                        bIn = bIn4;
                                        int i = head;
                                        BufferedInputStream bufferedInputStream = bufIn;
                                        cArr = cArr3;
                                        AlgorithmIdentifier algorithmIdentifier = algId2;
                                        ASN1Sequence aSN1Sequence = obj;
                                    } else if (cArr3.length <= 0) {
                                        try {
                                            ASN1ObjectIdentifier algorithm = algId2.getAlgorithm();
                                            Pfx pfx = bag;
                                            ASN1Sequence aSN1Sequence2 = obj;
                                            ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithm;
                                            bIn = bIn4;
                                            int i2 = head;
                                            BufferedInputStream bufferedInputStream2 = bufIn;
                                            cArr = cArr3;
                                            try {
                                                if (Arrays.constantTimeAreEqual(calculatePbeMac(aSN1ObjectIdentifier, salt, itCount, cArr3, true, data), dig)) {
                                                    wrongPKCS12Zero4 = true;
                                                } else {
                                                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                                                }
                                            } catch (IOException e) {
                                                e = e;
                                                throw e;
                                            } catch (Exception e2) {
                                                e = e2;
                                                throw new IOException("error constructing MAC: " + e.toString());
                                            }
                                        } catch (IOException e3) {
                                            e = e3;
                                            Pfx pfx2 = bag;
                                            ASN1Sequence aSN1Sequence3 = obj;
                                            ASN1InputStream aSN1InputStream = bIn4;
                                            int i3 = head;
                                            BufferedInputStream bufferedInputStream3 = bufIn;
                                            char[] cArr4 = cArr3;
                                            throw e;
                                        } catch (Exception e4) {
                                            e = e4;
                                            Pfx pfx3 = bag;
                                            ASN1Sequence aSN1Sequence4 = obj;
                                            ASN1InputStream aSN1InputStream2 = bIn4;
                                            int i4 = head;
                                            BufferedInputStream bufferedInputStream4 = bufIn;
                                            char[] cArr5 = cArr3;
                                            throw new IOException("error constructing MAC: " + e.toString());
                                        }
                                    } else {
                                        ASN1InputStream aSN1InputStream3 = bIn4;
                                        int i5 = head;
                                        BufferedInputStream bufferedInputStream5 = bufIn;
                                        char[] cArr6 = cArr3;
                                        AlgorithmIdentifier algorithmIdentifier2 = algId2;
                                        ASN1Sequence aSN1Sequence5 = obj;
                                        throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                                    }
                                    wrongPKCS12Zero = wrongPKCS12Zero4;
                                } catch (IOException e5) {
                                    e = e5;
                                    Pfx pfx4 = bag;
                                    ASN1InputStream aSN1InputStream4 = bIn4;
                                    int i6 = head;
                                    BufferedInputStream bufferedInputStream6 = bufIn;
                                    char[] cArr7 = cArr3;
                                    AlgorithmIdentifier algorithmIdentifier3 = algId2;
                                    ASN1Sequence aSN1Sequence6 = obj;
                                    throw e;
                                } catch (Exception e6) {
                                    e = e6;
                                    Pfx pfx5 = bag;
                                    ASN1InputStream aSN1InputStream5 = bIn4;
                                    int i7 = head;
                                    BufferedInputStream bufferedInputStream7 = bufIn;
                                    char[] cArr8 = cArr3;
                                    AlgorithmIdentifier algorithmIdentifier4 = algId2;
                                    ASN1Sequence aSN1Sequence7 = obj;
                                    throw new IOException("error constructing MAC: " + e.toString());
                                }
                            } catch (IOException e7) {
                                e = e7;
                                Pfx pfx6 = bag;
                                ASN1InputStream aSN1InputStream6 = bIn4;
                                BufferedInputStream bufferedInputStream8 = bufIn;
                                char[] cArr9 = cArr3;
                                AlgorithmIdentifier algorithmIdentifier5 = algId2;
                                DigestInfo digestInfo = dInfo2;
                                ASN1Sequence aSN1Sequence8 = obj;
                                int i8 = head;
                                throw e;
                            } catch (Exception e8) {
                                e = e8;
                                Pfx pfx7 = bag;
                                ASN1InputStream aSN1InputStream7 = bIn4;
                                BufferedInputStream bufferedInputStream9 = bufIn;
                                char[] cArr10 = cArr3;
                                AlgorithmIdentifier algorithmIdentifier6 = algId2;
                                DigestInfo digestInfo2 = dInfo2;
                                ASN1Sequence aSN1Sequence9 = obj;
                                int i9 = head;
                                throw new IOException("error constructing MAC: " + e.toString());
                            }
                        } catch (IOException e9) {
                            e = e9;
                            DigestInfo digestInfo3 = dInfo;
                            MacData macData2 = mData;
                            Vector vector = chain5;
                            ContentInfo contentInfo = info5;
                            Pfx pfx8 = bag;
                            ASN1Sequence aSN1Sequence10 = obj;
                            ASN1InputStream aSN1InputStream8 = bIn4;
                            int i10 = head;
                            BufferedInputStream bufferedInputStream10 = bufIn;
                            char[] cArr11 = cArr3;
                            AlgorithmIdentifier algorithmIdentifier7 = algId;
                            throw e;
                        } catch (Exception e10) {
                            e = e10;
                            DigestInfo digestInfo4 = dInfo;
                            MacData macData3 = mData;
                            Vector vector2 = chain5;
                            ContentInfo contentInfo2 = info5;
                            Pfx pfx9 = bag;
                            ASN1Sequence aSN1Sequence11 = obj;
                            ASN1InputStream aSN1InputStream9 = bIn4;
                            int i11 = head;
                            BufferedInputStream bufferedInputStream11 = bufIn;
                            char[] cArr12 = cArr3;
                            AlgorithmIdentifier algorithmIdentifier8 = algId;
                            throw new IOException("error constructing MAC: " + e.toString());
                        }
                    } else {
                        chain = chain5;
                        info = info5;
                        Pfx pfx10 = bag;
                        ASN1Sequence aSN1Sequence12 = obj;
                        bIn = bIn4;
                        int i12 = head;
                        BufferedInputStream bufferedInputStream12 = bufIn;
                        cArr = cArr3;
                        wrongPKCS12Zero = false;
                    }
                    this.keys = new IgnoresCaseHashtable();
                    this.localIds = new Hashtable();
                    ContentInfo info6 = info;
                    if (info6.getContentType().equals(data)) {
                        ASN1InputStream bIn5 = new ASN1InputStream(((ASN1OctetString) info6.getContent()).getOctets());
                        ContentInfo[] c = AuthenticatedSafe.getInstance(bIn5.readObject()).getContentInfo();
                        int i13 = 0;
                        while (true) {
                            int i14 = i13;
                            if (i14 == c.length) {
                                char[] cArr13 = cArr;
                                boolean z = wrongPKCS12Zero;
                                ContentInfo contentInfo3 = info6;
                                chain2 = chain;
                                break;
                            }
                            if (c[i14].getContentType().equals(data)) {
                                ASN1InputStream dIn2 = new ASN1InputStream(((ASN1OctetString) c[i14].getContent()).getOctets());
                                ASN1Sequence seq3 = (ASN1Sequence) dIn2.readObject();
                                int j = 0;
                                while (j != seq3.size()) {
                                    SafeBag b2 = SafeBag.getInstance(seq3.getObjectAt(j));
                                    if (b2.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                        EncryptedPrivateKeyInfo eIn2 = EncryptedPrivateKeyInfo.getInstance(b2.getBagValue());
                                        PrivateKey privKey = unwrapKey(eIn2.getEncryptionAlgorithm(), eIn2.getEncryptedData(), cArr, wrongPKCS12Zero);
                                        PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) privKey;
                                        String alias = null;
                                        ASN1OctetString localId2 = null;
                                        if (b2.getBagAttributes() != null) {
                                            Enumeration e11 = b2.getBagAttributes().getObjects();
                                            while (e11.hasMoreElements()) {
                                                ASN1InputStream bIn6 = bIn5;
                                                ASN1Sequence sq = (ASN1Sequence) e11.nextElement();
                                                ASN1InputStream dIn3 = dIn2;
                                                ASN1ObjectIdentifier aOid = (ASN1ObjectIdentifier) sq.getObjectAt(0);
                                                ASN1Sequence seq4 = seq3;
                                                ASN1Set attrSet = (ASN1Set) sq.getObjectAt(1);
                                                if (attrSet.size() > 0) {
                                                    ASN1Sequence aSN1Sequence13 = sq;
                                                    attr = (ASN1Primitive) attrSet.getObjectAt(0);
                                                    ASN1Set aSN1Set = attrSet;
                                                    ASN1Encodable existing = bagAttr.getBagAttribute(aOid);
                                                    if (existing != null) {
                                                        eIn = eIn2;
                                                        if (!existing.toASN1Primitive().equals(attr)) {
                                                            ASN1Encodable aSN1Encodable = existing;
                                                            throw new IOException("attempt to add existing attribute with different value");
                                                        }
                                                    } else {
                                                        eIn = eIn2;
                                                        bagAttr.setBagAttribute(aOid, attr);
                                                    }
                                                } else {
                                                    ASN1Set aSN1Set2 = attrSet;
                                                    eIn = eIn2;
                                                    attr = null;
                                                }
                                                if (aOid.equals(pkcs_9_at_friendlyName)) {
                                                    String alias2 = ((DERBMPString) attr).getString();
                                                    this.keys.put(alias2, privKey);
                                                    alias = alias2;
                                                } else if (aOid.equals(pkcs_9_at_localKeyId)) {
                                                    localId2 = (ASN1OctetString) attr;
                                                }
                                                bIn5 = bIn6;
                                                dIn2 = dIn3;
                                                seq3 = seq4;
                                                eIn2 = eIn;
                                            }
                                        }
                                        bIn3 = bIn5;
                                        dIn = dIn2;
                                        seq2 = seq3;
                                        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = eIn2;
                                        String alias3 = alias;
                                        ASN1OctetString localId3 = localId2;
                                        if (localId3 != null) {
                                            String name = new String(Hex.encode(localId3.getOctets()));
                                            if (alias3 == null) {
                                                this.keys.put(name, privKey);
                                            } else {
                                                this.localIds.put(alias3, name);
                                            }
                                        } else {
                                            unmarkedKey = true;
                                            this.keys.put("unmarked", privKey);
                                        }
                                        chain4 = chain;
                                    } else {
                                        bIn3 = bIn5;
                                        dIn = dIn2;
                                        seq2 = seq3;
                                        if (b2.getBagId().equals(certBag)) {
                                            chain4 = chain;
                                            chain4.addElement(b2);
                                        } else {
                                            chain4 = chain;
                                            System.out.println("extra in data " + b2.getBagId());
                                            System.out.println(ASN1Dump.dumpAsString(b2));
                                        }
                                    }
                                    j++;
                                    chain = chain4;
                                    bIn5 = bIn3;
                                    dIn2 = dIn;
                                    seq3 = seq2;
                                }
                                bIn2 = bIn5;
                                chain3 = chain;
                                cArr2 = cArr;
                                wrongPKCS12Zero2 = wrongPKCS12Zero;
                                info2 = info6;
                            } else {
                                bIn2 = bIn5;
                                chain3 = chain;
                                if (c[i14].getContentType().equals(encryptedData)) {
                                    EncryptedData d2 = EncryptedData.getInstance(c[i14].getContent());
                                    cArr2 = cArr;
                                    byte[] octets2 = cryptData(false, d2.getEncryptionAlgorithm(), cArr, wrongPKCS12Zero, d2.getContent().getOctets());
                                    ASN1Sequence seq5 = (ASN1Sequence) ASN1Primitive.fromByteArray(octets2);
                                    int j2 = 0;
                                    while (j2 != seq5.size()) {
                                        SafeBag b3 = SafeBag.getInstance(seq5.getObjectAt(j2));
                                        if (b3.getBagId().equals(certBag)) {
                                            chain3.addElement(b3);
                                            d = d2;
                                            octets = octets2;
                                            seq = seq5;
                                            wrongPKCS12Zero3 = wrongPKCS12Zero;
                                            info3 = info6;
                                        } else if (b3.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                            EncryptedPrivateKeyInfo eIn3 = EncryptedPrivateKeyInfo.getInstance(b3.getBagValue());
                                            PrivateKey privKey2 = unwrapKey(eIn3.getEncryptionAlgorithm(), eIn3.getEncryptedData(), cArr2, wrongPKCS12Zero);
                                            PKCS12BagAttributeCarrier bagAttr2 = (PKCS12BagAttributeCarrier) privKey2;
                                            d = d2;
                                            Enumeration e12 = b3.getBagAttributes().getObjects();
                                            octets = octets2;
                                            seq = seq5;
                                            String alias4 = null;
                                            ASN1OctetString localId4 = null;
                                            while (e12.hasMoreElements()) {
                                                Enumeration e13 = e12;
                                                ASN1Sequence sq2 = (ASN1Sequence) e12.nextElement();
                                                EncryptedPrivateKeyInfo eIn4 = eIn3;
                                                ASN1ObjectIdentifier aOid2 = (ASN1ObjectIdentifier) sq2.getObjectAt(0);
                                                boolean wrongPKCS12Zero5 = wrongPKCS12Zero;
                                                ASN1Set attrSet2 = (ASN1Set) sq2.getObjectAt(1);
                                                if (attrSet2.size() > 0) {
                                                    ASN1Sequence aSN1Sequence14 = sq2;
                                                    aSN1Object = (ASN1Primitive) attrSet2.getObjectAt(0);
                                                    ASN1Set aSN1Set3 = attrSet2;
                                                    ASN1Encodable existing2 = bagAttr2.getBagAttribute(aOid2);
                                                    if (existing2 != null) {
                                                        info4 = info6;
                                                        if (!existing2.toASN1Primitive().equals(aSN1Object)) {
                                                            ASN1Encodable aSN1Encodable2 = existing2;
                                                            throw new IOException("attempt to add existing attribute with different value");
                                                        }
                                                    } else {
                                                        info4 = info6;
                                                        bagAttr2.setBagAttribute(aOid2, aSN1Object);
                                                    }
                                                } else {
                                                    ASN1Set aSN1Set4 = attrSet2;
                                                    info4 = info6;
                                                    aSN1Object = null;
                                                }
                                                if (aOid2.equals(pkcs_9_at_friendlyName)) {
                                                    alias4 = ((DERBMPString) aSN1Object).getString();
                                                    this.keys.put(alias4, privKey2);
                                                } else if (aOid2.equals(pkcs_9_at_localKeyId)) {
                                                    localId4 = (ASN1OctetString) aSN1Object;
                                                }
                                                e12 = e13;
                                                eIn3 = eIn4;
                                                wrongPKCS12Zero = wrongPKCS12Zero5;
                                                info6 = info4;
                                            }
                                            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo2 = eIn3;
                                            wrongPKCS12Zero3 = wrongPKCS12Zero;
                                            info3 = info6;
                                            String name2 = new String(Hex.encode(localId4.getOctets()));
                                            if (alias4 == null) {
                                                this.keys.put(name2, privKey2);
                                            } else {
                                                this.localIds.put(alias4, name2);
                                            }
                                        } else {
                                            d = d2;
                                            octets = octets2;
                                            seq = seq5;
                                            wrongPKCS12Zero3 = wrongPKCS12Zero;
                                            info3 = info6;
                                            if (b3.getBagId().equals(keyBag)) {
                                                PrivateKeyInfo kInfo = PrivateKeyInfo.getInstance(b3.getBagValue());
                                                PrivateKey privKey3 = BouncyCastleProvider.getPrivateKey(kInfo);
                                                PKCS12BagAttributeCarrier bagAttr3 = (PKCS12BagAttributeCarrier) privKey3;
                                                String alias5 = null;
                                                ASN1OctetString localId5 = null;
                                                Enumeration e14 = b3.getBagAttributes().getObjects();
                                                while (e14.hasMoreElements()) {
                                                    ASN1Sequence sq3 = ASN1Sequence.getInstance(e14.nextElement());
                                                    PrivateKeyInfo kInfo2 = kInfo;
                                                    ASN1ObjectIdentifier aOid3 = ASN1ObjectIdentifier.getInstance(sq3.getObjectAt(0));
                                                    Enumeration e15 = e14;
                                                    ASN1Set attrSet3 = ASN1Set.getInstance(sq3.getObjectAt(1));
                                                    if (attrSet3.size() > 0) {
                                                        ASN1Sequence aSN1Sequence15 = sq3;
                                                        ? aOid4 = (ASN1Primitive) attrSet3.getObjectAt(0);
                                                        ASN1Encodable existing3 = bagAttr3.getBagAttribute(aOid3);
                                                        if (existing3 != null) {
                                                            ASN1Set aSN1Set5 = attrSet3;
                                                            if (existing3.toASN1Primitive().equals(aOid4)) {
                                                                ASN1Encodable aSN1Encodable3 = existing3;
                                                            } else {
                                                                ASN1Encodable aSN1Encodable4 = existing3;
                                                                throw new IOException("attempt to add existing attribute with different value");
                                                            }
                                                        } else {
                                                            ASN1Encodable aSN1Encodable5 = existing3;
                                                            bagAttr3.setBagAttribute(aOid3, aOid4);
                                                        }
                                                        if (aOid3.equals(pkcs_9_at_friendlyName)) {
                                                            alias5 = aOid4.getString();
                                                            this.keys.put(alias5, privKey3);
                                                        } else if (aOid3.equals(pkcs_9_at_localKeyId)) {
                                                            localId5 = (ASN1OctetString) aOid4;
                                                        }
                                                    }
                                                    kInfo = kInfo2;
                                                    e14 = e15;
                                                }
                                                Enumeration enumeration = e14;
                                                String name3 = new String(Hex.encode(localId5.getOctets()));
                                                if (alias5 == null) {
                                                    this.keys.put(name3, privKey3);
                                                } else {
                                                    this.localIds.put(alias5, name3);
                                                }
                                            } else {
                                                System.out.println("extra in encryptedData " + b3.getBagId());
                                                System.out.println(ASN1Dump.dumpAsString(b3));
                                            }
                                        }
                                        j2++;
                                        d2 = d;
                                        octets2 = octets;
                                        seq5 = seq;
                                        wrongPKCS12Zero = wrongPKCS12Zero3;
                                        info6 = info3;
                                    }
                                    wrongPKCS12Zero2 = wrongPKCS12Zero;
                                    info2 = info6;
                                } else {
                                    cArr2 = cArr;
                                    wrongPKCS12Zero2 = wrongPKCS12Zero;
                                    info2 = info6;
                                    System.out.println("extra " + c[i14].getContentType().getId());
                                    System.out.println("extra " + ASN1Dump.dumpAsString(c[i14].getContent()));
                                }
                            }
                            i13 = i14 + 1;
                            chain = chain3;
                            cArr = cArr2;
                            bIn5 = bIn2;
                            wrongPKCS12Zero = wrongPKCS12Zero2;
                            info6 = info2;
                        }
                    } else {
                        char[] cArr14 = cArr;
                        boolean z2 = wrongPKCS12Zero;
                        ContentInfo contentInfo4 = info6;
                        chain2 = chain;
                        ASN1InputStream aSN1InputStream10 = bIn;
                    }
                    this.certs = new IgnoresCaseHashtable();
                    this.chainCerts = new Hashtable();
                    this.keyCerts = new Hashtable();
                    int i15 = 0;
                    while (true) {
                        int i16 = i15;
                        if (i16 != chain2.size()) {
                            SafeBag b4 = (SafeBag) chain2.elementAt(i16);
                            CertBag cb = CertBag.getInstance(b4.getBagValue());
                            if (cb.getCertId().equals(x509Certificate)) {
                                try {
                                    Certificate cert = this.certFact.generateCertificate(new ByteArrayInputStream(((ASN1OctetString) cb.getCertValue()).getOctets()));
                                    ASN1OctetString localId6 = null;
                                    String alias6 = null;
                                    if (b4.getBagAttributes() != null) {
                                        Enumeration e16 = b4.getBagAttributes().getObjects();
                                        while (e16.hasMoreElements()) {
                                            ASN1Sequence sq4 = ASN1Sequence.getInstance(e16.nextElement());
                                            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(sq4.getObjectAt(0));
                                            ASN1Set attrSet4 = ASN1Set.getInstance(sq4.getObjectAt(1));
                                            if (attrSet4.size() > 0) {
                                                ? r14 = (ASN1Primitive) attrSet4.getObjectAt(0);
                                                if (cert instanceof PKCS12BagAttributeCarrier) {
                                                    PKCS12BagAttributeCarrier bagAttr4 = (PKCS12BagAttributeCarrier) cert;
                                                    ASN1Encodable existing4 = bagAttr4.getBagAttribute(oid);
                                                    if (existing4 != null) {
                                                        b = b4;
                                                        if (existing4.toASN1Primitive().equals(r14)) {
                                                            localId = localId6;
                                                        } else {
                                                            ASN1OctetString aSN1OctetString = localId6;
                                                            throw new IOException("attempt to add existing attribute with different value");
                                                        }
                                                    } else {
                                                        b = b4;
                                                        localId = localId6;
                                                        bagAttr4.setBagAttribute(oid, r14);
                                                    }
                                                    PKCS12BagAttributeCarrier pKCS12BagAttributeCarrier = bagAttr4;
                                                } else {
                                                    b = b4;
                                                    localId = localId6;
                                                }
                                                if (oid.equals(pkcs_9_at_friendlyName)) {
                                                    alias6 = r14.getString();
                                                } else if (oid.equals(pkcs_9_at_localKeyId)) {
                                                    localId6 = r14;
                                                    b4 = b;
                                                }
                                            } else {
                                                b = b4;
                                                localId = localId6;
                                            }
                                            localId6 = localId;
                                            b4 = b;
                                        }
                                        ASN1OctetString aSN1OctetString2 = localId6;
                                    }
                                    this.chainCerts.put(new CertId(cert.getPublicKey()), cert);
                                    if (!unmarkedKey) {
                                        if (localId6 != null) {
                                            this.keyCerts.put(new String(Hex.encode(localId6.getOctets())), cert);
                                        }
                                        if (alias6 != null) {
                                            this.certs.put(alias6, cert);
                                        }
                                    } else if (this.keyCerts.isEmpty()) {
                                        String name4 = new String(Hex.encode(createSubjectKeyId(cert.getPublicKey()).getKeyIdentifier()));
                                        this.keyCerts.put(name4, cert);
                                        this.keys.put(name4, this.keys.remove("unmarked"));
                                    }
                                    i15 = i16 + 1;
                                } catch (Exception e17) {
                                    SafeBag safeBag = b4;
                                    throw new RuntimeException(e17.toString());
                                }
                            } else {
                                throw new RuntimeException("Unsupported certificate type: " + cb.getCertId());
                            }
                        } else {
                            return;
                        }
                    }
                } else {
                    BufferedInputStream bufferedInputStream13 = bufIn;
                    char[] cArr15 = cArr3;
                    throw new IOException("stream does not represent a PKCS12 key store");
                }
            } else {
                char[] cArr16 = cArr3;
                throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
            }
        }
    }

    public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        PKCS12StoreParameter bcParam;
        char[] password;
        if (param == null) {
            throw new IllegalArgumentException("'param' arg cannot be null");
        } else if ((param instanceof PKCS12StoreParameter) || (param instanceof JDKPKCS12StoreParameter)) {
            if (param instanceof PKCS12StoreParameter) {
                bcParam = (PKCS12StoreParameter) param;
            } else {
                bcParam = new PKCS12StoreParameter(((JDKPKCS12StoreParameter) param).getOutputStream(), param.getProtectionParameter(), ((JDKPKCS12StoreParameter) param).isUseDEREncoding());
            }
            KeyStore.ProtectionParameter protParam = param.getProtectionParameter();
            if (protParam == null) {
                password = null;
            } else if (protParam instanceof KeyStore.PasswordProtection) {
                password = ((KeyStore.PasswordProtection) protParam).getPassword();
            } else {
                throw new IllegalArgumentException("No support for protection parameter of type " + protParam.getClass().getName());
            }
            doStore(bcParam.getOutputStream(), password, bcParam.isForDEREncoding());
        } else {
            throw new IllegalArgumentException("No support for 'param' of type " + param.getClass().getName());
        }
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException {
        doStore(stream, password, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:116:0x0393 A[Catch:{ CertificateEncodingException -> 0x03f7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00b2 A[LOOP:1: B:19:0x00ac->B:21:0x00b2, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01f6 A[SYNTHETIC, Splitter:B:56:0x01f6] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x020d A[Catch:{ CertificateEncodingException -> 0x0248 }] */
    private void doStore(OutputStream stream, char[] password, boolean useDEREncoding) throws IOException {
        Enumeration ks;
        Enumeration cs;
        DEROutputStream asn1Out;
        DEROutputStream asn1Out2;
        Hashtable doneCerts;
        Certificate cert;
        boolean cAttrSet;
        Enumeration cs2;
        Enumeration e;
        AlgorithmIdentifier cAlgId;
        byte[] keySEncoded;
        Enumeration ks2;
        ASN1EncodableVector keyS;
        Enumeration e2;
        byte[] keySEncoded2;
        Enumeration e3;
        OutputStream outputStream = stream;
        char[] cArr = password;
        if (cArr != null) {
            ASN1EncodableVector keyS2 = new ASN1EncodableVector();
            Enumeration ks3 = this.keys.keys();
            while (true) {
                ks = ks3;
                if (!ks.hasMoreElements()) {
                    break;
                }
                byte[] kSalt = new byte[20];
                this.random.nextBytes(kSalt);
                String name = (String) ks.nextElement();
                PrivateKey privKey = (PrivateKey) this.keys.get(name);
                PKCS12PBEParams kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
                byte[] kBytes = wrapKey(this.keyAlgorithm.getId(), privKey, kParams, cArr);
                EncryptedPrivateKeyInfo kInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(this.keyAlgorithm, kParams.toASN1Primitive()), kBytes);
                boolean attrSet = false;
                ASN1EncodableVector kName = new ASN1EncodableVector();
                if (privKey instanceof PKCS12BagAttributeCarrier) {
                    PKCS12BagAttributeCarrier bagAttrs = (PKCS12BagAttributeCarrier) privKey;
                    byte[] bArr = kSalt;
                    DERBMPString nm = (DERBMPString) bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm != null) {
                        PKCS12PBEParams pKCS12PBEParams = kParams;
                        if (nm.getString().equals(name)) {
                            DERBMPString dERBMPString = nm;
                            if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) != null) {
                                PrivateKey privateKey = privKey;
                                bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(engineGetCertificate(name).getPublicKey()));
                            }
                            e3 = bagAttrs.getBagAttributeKeys();
                            while (e3.hasMoreElements()) {
                                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e3.nextElement();
                                ASN1EncodableVector kSeq = new ASN1EncodableVector();
                                kSeq.add(oid);
                                kSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                                attrSet = true;
                                kName.add(new DERSequence(kSeq));
                                e3 = e3;
                                kBytes = kBytes;
                            }
                        }
                    }
                    DERBMPString dERBMPString2 = nm;
                    bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                    if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) != null) {
                    }
                    e3 = bagAttrs.getBagAttributeKeys();
                    while (e3.hasMoreElements()) {
                    }
                } else {
                    PKCS12PBEParams pKCS12PBEParams2 = kParams;
                    PrivateKey privateKey2 = privKey;
                    byte[] bArr2 = kBytes;
                }
                if (!attrSet) {
                    ASN1EncodableVector kSeq2 = new ASN1EncodableVector();
                    Certificate ct = engineGetCertificate(name);
                    kSeq2.add(pkcs_9_at_localKeyId);
                    kSeq2.add(new DERSet((ASN1Encodable) createSubjectKeyId(ct.getPublicKey())));
                    kName.add(new DERSequence(kSeq2));
                    ASN1EncodableVector kSeq3 = new ASN1EncodableVector();
                    kSeq3.add(pkcs_9_at_friendlyName);
                    kSeq3.add(new DERSet((ASN1Encodable) new DERBMPString(name)));
                    kName.add(new DERSequence(kSeq3));
                }
                keyS2.add(new SafeBag(pkcs8ShroudedKeyBag, kInfo.toASN1Primitive(), new DERSet(kName)));
                ks3 = ks;
            }
            byte[] keySEncoded3 = new DERSequence(keyS2).getEncoded(ASN1Encoding.DER);
            BEROctetString keyString = new BEROctetString(keySEncoded3);
            byte[] cSalt = new byte[20];
            this.random.nextBytes(cSalt);
            ASN1EncodableVector certSeq = new ASN1EncodableVector();
            PKCS12PBEParams cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
            AlgorithmIdentifier cAlgId2 = new AlgorithmIdentifier(this.certAlgorithm, cParams.toASN1Primitive());
            Hashtable doneCerts2 = new Hashtable();
            Enumeration cs3 = this.keys.keys();
            while (true) {
                cs = cs3;
                if (!cs.hasMoreElements()) {
                    break;
                }
                try {
                    String name2 = (String) cs.nextElement();
                    boolean cAttrSet2 = false;
                    Enumeration cs4 = cs;
                    try {
                        cAlgId = cAlgId2;
                    } catch (CertificateEncodingException e4) {
                        e = e4;
                        AlgorithmIdentifier algorithmIdentifier = cAlgId2;
                        PKCS12PBEParams pKCS12PBEParams3 = cParams;
                        byte[] bArr3 = cSalt;
                        ASN1EncodableVector aSN1EncodableVector = keyS2;
                        Enumeration enumeration = ks;
                        byte[] bArr4 = keySEncoded3;
                        throw new IOException("Error encoding certificate: " + e.toString());
                    }
                    try {
                        PKCS12PBEParams cParams2 = cParams;
                        byte[] cSalt2 = cSalt;
                        Certificate cert2 = engineGetCertificate(name2);
                        try {
                            CertBag cBag = new CertBag(x509Certificate, new DEROctetString(cert2.getEncoded()));
                            ASN1EncodableVector fName = new ASN1EncodableVector();
                            if (cert2 instanceof PKCS12BagAttributeCarrier) {
                                PKCS12BagAttributeCarrier bagAttrs2 = (PKCS12BagAttributeCarrier) cert2;
                                DERBMPString nm2 = (DERBMPString) bagAttrs2.getBagAttribute(pkcs_9_at_friendlyName);
                                if (nm2 != null) {
                                    keyS = keyS2;
                                    try {
                                        if (nm2.getString().equals(name2)) {
                                            DERBMPString dERBMPString3 = nm2;
                                            if (bagAttrs2.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                                                bagAttrs2.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(cert2.getPublicKey()));
                                            }
                                            e2 = bagAttrs2.getBagAttributeKeys();
                                            while (e2.hasMoreElements()) {
                                                ASN1ObjectIdentifier oid2 = (ASN1ObjectIdentifier) e2.nextElement();
                                                Enumeration e5 = e2;
                                                ASN1EncodableVector fSeq = new ASN1EncodableVector();
                                                fSeq.add(oid2);
                                                Enumeration ks4 = ks;
                                                try {
                                                    keySEncoded2 = keySEncoded3;
                                                } catch (CertificateEncodingException e6) {
                                                    e = e6;
                                                    byte[] bArr5 = keySEncoded3;
                                                    throw new IOException("Error encoding certificate: " + e.toString());
                                                }
                                                try {
                                                    fSeq.add(new DERSet(bagAttrs2.getBagAttribute(oid2)));
                                                    fName.add(new DERSequence(fSeq));
                                                    cAttrSet2 = true;
                                                    e2 = e5;
                                                    ks = ks4;
                                                    keySEncoded3 = keySEncoded2;
                                                } catch (CertificateEncodingException e7) {
                                                    e = e7;
                                                    throw new IOException("Error encoding certificate: " + e.toString());
                                                }
                                            }
                                            ks2 = ks;
                                            keySEncoded = keySEncoded3;
                                        }
                                    } catch (CertificateEncodingException e8) {
                                        e = e8;
                                        Enumeration enumeration2 = ks;
                                        byte[] bArr6 = keySEncoded3;
                                        throw new IOException("Error encoding certificate: " + e.toString());
                                    }
                                } else {
                                    keyS = keyS2;
                                }
                                try {
                                    DERBMPString dERBMPString4 = nm2;
                                    bagAttrs2.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name2));
                                    if (bagAttrs2.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                                    }
                                    e2 = bagAttrs2.getBagAttributeKeys();
                                    while (e2.hasMoreElements()) {
                                    }
                                    ks2 = ks;
                                    keySEncoded = keySEncoded3;
                                } catch (CertificateEncodingException e9) {
                                    e = e9;
                                    Enumeration enumeration3 = ks;
                                    byte[] bArr7 = keySEncoded3;
                                    throw new IOException("Error encoding certificate: " + e.toString());
                                }
                            } else {
                                keyS = keyS2;
                                ks2 = ks;
                                keySEncoded = keySEncoded3;
                            }
                            if (!cAttrSet2) {
                                ASN1EncodableVector fSeq2 = new ASN1EncodableVector();
                                fSeq2.add(pkcs_9_at_localKeyId);
                                fSeq2.add(new DERSet((ASN1Encodable) createSubjectKeyId(cert2.getPublicKey())));
                                fName.add(new DERSequence(fSeq2));
                                ASN1EncodableVector fSeq3 = new ASN1EncodableVector();
                                fSeq3.add(pkcs_9_at_friendlyName);
                                fSeq3.add(new DERSet((ASN1Encodable) new DERBMPString(name2)));
                                fName.add(new DERSequence(fSeq3));
                            }
                            certSeq.add(new SafeBag(certBag, cBag.toASN1Primitive(), new DERSet(fName)));
                            doneCerts2.put(cert2, cert2);
                            cs3 = cs4;
                            cAlgId2 = cAlgId;
                            cParams = cParams2;
                            cSalt = cSalt2;
                            keyS2 = keyS;
                            ks = ks2;
                            keySEncoded3 = keySEncoded;
                        } catch (CertificateEncodingException e10) {
                            e = e10;
                            ASN1EncodableVector aSN1EncodableVector2 = keyS2;
                            Enumeration enumeration4 = ks;
                            byte[] bArr8 = keySEncoded3;
                            throw new IOException("Error encoding certificate: " + e.toString());
                        }
                    } catch (CertificateEncodingException e11) {
                        e = e11;
                        PKCS12PBEParams pKCS12PBEParams4 = cParams;
                        byte[] bArr9 = cSalt;
                        ASN1EncodableVector aSN1EncodableVector3 = keyS2;
                        Enumeration enumeration5 = ks;
                        byte[] bArr10 = keySEncoded3;
                        throw new IOException("Error encoding certificate: " + e.toString());
                    }
                } catch (CertificateEncodingException e12) {
                    e = e12;
                    Enumeration enumeration6 = cs;
                    AlgorithmIdentifier algorithmIdentifier2 = cAlgId2;
                    PKCS12PBEParams pKCS12PBEParams5 = cParams;
                    byte[] bArr11 = cSalt;
                    ASN1EncodableVector aSN1EncodableVector4 = keyS2;
                    Enumeration enumeration7 = ks;
                    byte[] bArr12 = keySEncoded3;
                    throw new IOException("Error encoding certificate: " + e.toString());
                }
            }
            AlgorithmIdentifier cAlgId3 = cAlgId2;
            PKCS12PBEParams cParams3 = cParams;
            byte[] cSalt3 = cSalt;
            ASN1EncodableVector aSN1EncodableVector5 = keyS2;
            Enumeration enumeration8 = ks;
            byte[] bArr13 = keySEncoded3;
            Enumeration cs5 = this.certs.keys();
            while (cs5.hasMoreElements()) {
                try {
                    String certId = (String) cs5.nextElement();
                    Certificate cert3 = (Certificate) this.certs.get(certId);
                    boolean cAttrSet3 = false;
                    if (this.keys.get(certId) == null) {
                        CertBag cBag2 = new CertBag(x509Certificate, new DEROctetString(cert3.getEncoded()));
                        ASN1EncodableVector fName2 = new ASN1EncodableVector();
                        if (cert3 instanceof PKCS12BagAttributeCarrier) {
                            PKCS12BagAttributeCarrier bagAttrs3 = (PKCS12BagAttributeCarrier) cert3;
                            DERBMPString nm3 = (DERBMPString) bagAttrs3.getBagAttribute(pkcs_9_at_friendlyName);
                            if (nm3 != null) {
                                try {
                                    if (nm3.getString().equals(certId)) {
                                        cs2 = cs5;
                                        e = bagAttrs3.getBagAttributeKeys();
                                        while (e.hasMoreElements()) {
                                            ASN1ObjectIdentifier oid3 = (ASN1ObjectIdentifier) e.nextElement();
                                            Enumeration e13 = e;
                                            if (oid3.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                                e = e13;
                                            } else {
                                                ASN1EncodableVector fSeq4 = new ASN1EncodableVector();
                                                fSeq4.add(oid3);
                                                boolean z = cAttrSet3;
                                                fSeq4.add(new DERSet(bagAttrs3.getBagAttribute(oid3)));
                                                fName2.add(new DERSequence(fSeq4));
                                                cAttrSet3 = true;
                                                e = e13;
                                                nm3 = nm3;
                                            }
                                        }
                                        cAttrSet = cAttrSet3;
                                    }
                                } catch (CertificateEncodingException e14) {
                                    e = e14;
                                    Enumeration enumeration9 = cs5;
                                    throw new IOException("Error encoding certificate: " + e.toString());
                                }
                            }
                            cs2 = cs5;
                            try {
                                bagAttrs3.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(certId));
                                e = bagAttrs3.getBagAttributeKeys();
                                while (e.hasMoreElements()) {
                                }
                                cAttrSet = cAttrSet3;
                            } catch (CertificateEncodingException e15) {
                                e = e15;
                                throw new IOException("Error encoding certificate: " + e.toString());
                            }
                        } else {
                            cs2 = cs5;
                            cAttrSet = false;
                        }
                        if (!cAttrSet) {
                            ASN1EncodableVector fSeq5 = new ASN1EncodableVector();
                            fSeq5.add(pkcs_9_at_friendlyName);
                            fSeq5.add(new DERSet((ASN1Encodable) new DERBMPString(certId)));
                            fName2.add(new DERSequence(fSeq5));
                        }
                        certSeq.add(new SafeBag(certBag, cBag2.toASN1Primitive(), new DERSet(fName2)));
                        doneCerts2.put(cert3, cert3);
                        cs5 = cs2;
                    }
                } catch (CertificateEncodingException e16) {
                    e = e16;
                    Enumeration enumeration10 = cs5;
                    throw new IOException("Error encoding certificate: " + e.toString());
                }
            }
            Set usedSet = getUsedCertificateSet();
            Enumeration cs6 = this.chainCerts.keys();
            while (cs6.hasMoreElements()) {
                try {
                    CertId certId2 = (CertId) cs6.nextElement();
                    Certificate cert4 = (Certificate) this.chainCerts.get(certId2);
                    if (usedSet.contains(cert4)) {
                        if (doneCerts2.get(cert4) == null) {
                            CertBag cBag3 = new CertBag(x509Certificate, new DEROctetString(cert4.getEncoded()));
                            ASN1EncodableVector fName3 = new ASN1EncodableVector();
                            if (cert4 instanceof PKCS12BagAttributeCarrier) {
                                PKCS12BagAttributeCarrier bagAttrs4 = (PKCS12BagAttributeCarrier) cert4;
                                Enumeration e17 = bagAttrs4.getBagAttributeKeys();
                                while (e17.hasMoreElements()) {
                                    ASN1ObjectIdentifier oid4 = (ASN1ObjectIdentifier) e17.nextElement();
                                    CertId certId3 = certId2;
                                    if (oid4.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                        cert = cert4;
                                        doneCerts = doneCerts2;
                                    } else {
                                        ASN1EncodableVector fSeq6 = new ASN1EncodableVector();
                                        fSeq6.add(oid4);
                                        cert = cert4;
                                        doneCerts = doneCerts2;
                                        try {
                                            fSeq6.add(new DERSet(bagAttrs4.getBagAttribute(oid4)));
                                            fName3.add(new DERSequence(fSeq6));
                                        } catch (CertificateEncodingException e18) {
                                            e = e18;
                                            throw new IOException("Error encoding certificate: " + e.toString());
                                        }
                                    }
                                    certId2 = certId3;
                                    cert4 = cert;
                                    doneCerts2 = doneCerts;
                                }
                            }
                            Certificate certificate = cert4;
                            Hashtable doneCerts3 = doneCerts2;
                            certSeq.add(new SafeBag(certBag, cBag3.toASN1Primitive(), new DERSet(fName3)));
                            doneCerts2 = doneCerts3;
                        }
                    }
                } catch (CertificateEncodingException e19) {
                    e = e19;
                    Hashtable hashtable = doneCerts2;
                    throw new IOException("Error encoding certificate: " + e.toString());
                }
            }
            Hashtable hashtable2 = doneCerts2;
            AlgorithmIdentifier cAlgId4 = cAlgId3;
            PKCS12PBEParams pKCS12PBEParams6 = cParams3;
            ASN1EncodableVector aSN1EncodableVector6 = certSeq;
            byte[] bArr14 = cSalt3;
            byte[] certBytes = cryptData(true, cAlgId4, cArr, false, new DERSequence(certSeq).getEncoded(ASN1Encoding.DER));
            EncryptedData cInfo = new EncryptedData(data, cAlgId4, new BEROctetString(certBytes));
            ContentInfo[] info = {new ContentInfo(data, keyString), new ContentInfo(encryptedData, cInfo.toASN1Primitive())};
            AuthenticatedSafe auth = new AuthenticatedSafe(info);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            if (useDEREncoding) {
                asn1Out = new DEROutputStream(bOut);
            } else {
                asn1Out = new BEROutputStream(bOut);
            }
            DEROutputStream asn1Out3 = asn1Out;
            asn1Out3.writeObject(auth);
            byte[] certBytes2 = certBytes;
            byte[] certBytes3 = bOut.toByteArray();
            DEROutputStream asn1Out4 = asn1Out3;
            ByteArrayOutputStream bOut2 = bOut;
            ContentInfo mainInfo = new ContentInfo(data, new BEROctetString(certBytes3));
            byte[] mSalt = new byte[20];
            byte[] pkg = certBytes3;
            this.random.nextBytes(mSalt);
            AlgorithmIdentifier algorithmIdentifier3 = cAlgId4;
            byte[] data = ((ASN1OctetString) mainInfo.getContent()).getOctets();
            try {
                byte[] mSalt2 = mSalt;
                DEROutputStream dEROutputStream = asn1Out4;
                ByteArrayOutputStream byteArrayOutputStream = bOut2;
                AuthenticatedSafe authenticatedSafe = auth;
                ContentInfo[] contentInfoArr = info;
                EncryptedData encryptedData = cInfo;
                byte[] bArr15 = certBytes2;
                byte[] bArr16 = pkg;
                try {
                } catch (Exception e20) {
                    e = e20;
                    byte[] bArr17 = mSalt2;
                    throw new IOException("error constructing MAC: " + e.toString());
                }
                try {
                    Pfx pfx = new Pfx(mainInfo, new MacData(new DigestInfo(new AlgorithmIdentifier(id_SHA1, DERNull.INSTANCE), calculatePbeMac(id_SHA1, mSalt2, MIN_ITERATIONS, cArr, false, data)), mSalt2, MIN_ITERATIONS));
                    if (useDEREncoding) {
                        asn1Out2 = new DEROutputStream(outputStream);
                    } else {
                        asn1Out2 = new BEROutputStream(outputStream);
                    }
                    asn1Out2.writeObject(pfx);
                } catch (Exception e21) {
                    e = e21;
                    throw new IOException("error constructing MAC: " + e.toString());
                }
            } catch (Exception e22) {
                e = e22;
                ContentInfo contentInfo = mainInfo;
                AuthenticatedSafe authenticatedSafe2 = auth;
                ContentInfo[] contentInfoArr2 = info;
                EncryptedData encryptedData2 = cInfo;
                boolean z2 = MIN_ITERATIONS;
                byte[] bArr18 = certBytes2;
                DEROutputStream dEROutputStream2 = asn1Out4;
                ByteArrayOutputStream byteArrayOutputStream2 = bOut2;
                byte[] bArr19 = pkg;
                byte[] bArr20 = mSalt;
                throw new IOException("error constructing MAC: " + e.toString());
            }
        } else {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }
    }

    private Set getUsedCertificateSet() {
        Set usedSet = new HashSet();
        Enumeration en = this.keys.keys();
        while (en.hasMoreElements()) {
            Certificate[] certs2 = engineGetCertificateChain((String) en.nextElement());
            for (int i = 0; i != certs2.length; i++) {
                usedSet.add(certs2[i]);
            }
        }
        Enumeration en2 = this.certs.keys();
        while (en2.hasMoreElements()) {
            usedSet.add(engineGetCertificate((String) en2.nextElement()));
        }
        return usedSet;
    }

    private byte[] calculatePbeMac(ASN1ObjectIdentifier oid, byte[] salt, int itCount, char[] password, boolean wrongPkcs12Zero, byte[] data) throws Exception {
        PBEParameterSpec defParams = new PBEParameterSpec(salt, itCount);
        Mac mac = this.helper.createMac(oid.getId());
        mac.init(new PKCS12Key(password, wrongPkcs12Zero), defParams);
        mac.update(data);
        return mac.doFinal();
    }
}
