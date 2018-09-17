package com.android.org.bouncycastle.jcajce.provider.keystore.pkcs12;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
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
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
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
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
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
    private final JcaJceHelper helper = new BCJcaJceHelper();
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

        CertId(byte[] id) {
            this.id = id;
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

        /* synthetic */ IgnoresCaseHashtable(IgnoresCaseHashtable -this0) {
            this();
        }

        private IgnoresCaseHashtable() {
            this.orig = new Hashtable();
            this.keys = new Hashtable();
        }

        public void put(String key, Object value) {
            Object lower = key == null ? null : Strings.toLowerCase(key);
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

    public PKCS12KeyStoreSpi(Provider provider, ASN1ObjectIdentifier keyAlgorithm, ASN1ObjectIdentifier certAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.certAlgorithm = certAlgorithm;
        if (provider != null) {
            try {
                this.certFact = CertificateFactory.getInstance("X.509", provider);
                return;
            } catch (Exception e) {
                throw new IllegalArgumentException("can't create cert factory - " + e.toString());
            }
        }
        this.certFact = CertificateFactory.getInstance("X.509");
    }

    private SubjectKeyIdentifier createSubjectKeyId(PublicKey pubKey) {
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
        e = this.keys.keys();
        while (e.hasMoreElements()) {
            String a = (String) e.nextElement();
            if (tab.get(a) == null) {
                tab.put(a, "key");
            }
        }
        return tab.keys();
    }

    public boolean engineContainsAlias(String alias) {
        return (this.certs.get(alias) == null && this.keys.get(alias) == null) ? false : true;
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        Key k = (Key) this.keys.remove(alias);
        Certificate c = (Certificate) this.certs.remove(alias);
        if (c != null) {
            this.chainCerts.remove(new CertId(c.getPublicKey()));
        }
        if (k != null) {
            String id = (String) this.localIds.remove(alias);
            if (id != null) {
                c = (Certificate) this.keyCerts.remove(id);
            }
            if (c != null) {
                this.chainCerts.remove(new CertId(c.getPublicKey()));
            }
        }
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("null alias passed to getCertificate.");
        }
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

    public String engineGetCertificateAlias(Certificate cert) {
        String ta;
        Enumeration c = this.certs.elements();
        Enumeration k = this.certs.keys();
        while (c.hasMoreElements()) {
            ta = (String) k.nextElement();
            if (((Certificate) c.nextElement()).equals(cert)) {
                return ta;
            }
        }
        c = this.keyCerts.elements();
        k = this.keyCerts.keys();
        while (c.hasMoreElements()) {
            ta = (String) k.nextElement();
            if (((Certificate) c.nextElement()).equals(cert)) {
                return ta;
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
                Certificate certificate = null;
                byte[] bytes = x509c.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                if (bytes != null) {
                    try {
                        AuthorityKeyIdentifier id = AuthorityKeyIdentifier.getInstance(new ASN1InputStream(((ASN1OctetString) new ASN1InputStream(bytes).readObject()).getOctets()).readObject());
                        if (id.getKeyIdentifier() != null) {
                            certificate = (Certificate) this.chainCerts.get(new CertId(id.getKeyIdentifier()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.toString());
                    }
                }
                if (certificate == null) {
                    Principal i = x509c.getIssuerDN();
                    if (!i.equals(x509c.getSubjectDN())) {
                        Enumeration e2 = this.chainCerts.keys();
                        while (e2.hasMoreElements()) {
                            Certificate crt = (X509Certificate) this.chainCerts.get(e2.nextElement());
                            if (crt.getSubjectDN().equals(i)) {
                                try {
                                    x509c.verify(crt.getPublicKey());
                                    certificate = crt;
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
                    if (certificate != c) {
                        c = certificate;
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
        if (this.keys.get(alias) != null) {
            throw new KeyStoreException("There is a key entry with the name " + alias + ".");
        }
        this.certs.put(alias, cert);
        this.chainCerts.put(new CertId(cert.getPublicKey()), cert);
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        throw new RuntimeException("operation not supported");
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (!(key instanceof PrivateKey)) {
            throw new KeyStoreException("PKCS12 does not support non-PrivateKeys");
        } else if ((key instanceof PrivateKey) && chain == null) {
            throw new KeyStoreException("no certificate chain for private key");
        } else {
            if (this.keys.get(alias) != null) {
                engineDeleteEntry(alias);
            }
            this.keys.put(alias, key);
            if (chain != null) {
                this.certs.put(alias, chain[0]);
                for (int i = 0; i != chain.length; i++) {
                    this.chainCerts.put(new CertId(chain[i].getPublicKey()), chain[i]);
                }
            }
        }
    }

    public int engineSize() {
        Hashtable tab = new Hashtable();
        Enumeration e = this.certs.keys();
        while (e.hasMoreElements()) {
            tab.put(e.nextElement(), "cert");
        }
        e = this.keys.keys();
        while (e.hasMoreElements()) {
            String a = (String) e.nextElement();
            if (tab.get(a) == null) {
                tab.put(a, "key");
            }
        }
        return tab.size();
    }

    protected PrivateKey unwrapKey(AlgorithmIdentifier algId, byte[] data, char[] password, boolean wrongPKCS12Zero) throws IOException {
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

    protected byte[] wrapKey(String algorithm, Key key, PKCS12PBEParams pbeParams, char[] password) throws IOException {
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

    protected byte[] cryptData(boolean forEncryption, AlgorithmIdentifier algId, char[] password, boolean wrongPKCS12Zero, byte[] data) throws IOException {
        ASN1ObjectIdentifier algorithm = algId.getAlgorithm();
        int mode = forEncryption ? 1 : 2;
        if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
            PKCS12PBEParams pbeParams = PKCS12PBEParams.getInstance(algId.getParameters());
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
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
            key = keyFact.generateSecret(new PBKDF2KeySpec(password, func.getSalt(), func.getIterationCount().intValue(), keySizeProvider.getKeySize(encScheme), func.getPrf()));
        }
        Cipher cipher = Cipher.getInstance(alg.getEncryptionScheme().getAlgorithm().getId());
        AlgorithmIdentifier encryptionAlg = AlgorithmIdentifier.getInstance(alg.getEncryptionScheme());
        ASN1Encodable encParams = alg.getEncryptionScheme().getParameters();
        if (encParams instanceof ASN1OctetString) {
            cipher.init(mode, key, new IvParameterSpec(ASN1OctetString.getInstance(encParams).getOctets()));
        }
        return cipher;
    }

    public void engineLoad(InputStream stream, char[] password) throws IOException {
        if (stream != null) {
            if (password == null) {
                throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
            }
            InputStream bufferedInputStream = new BufferedInputStream(stream);
            bufferedInputStream.mark(10);
            if (bufferedInputStream.read() != 48) {
                throw new IOException("stream does not represent a PKCS12 key store");
            }
            int i;
            SafeBag b;
            PKCS12BagAttributeCarrier bagAttr;
            String alias;
            ASN1OctetString localId;
            Enumeration e;
            ASN1Sequence sq;
            ASN1Set attrSet;
            ASN1Primitive attr;
            ASN1Encodable existing;
            String str;
            bufferedInputStream.reset();
            Pfx bag = Pfx.getInstance((ASN1Sequence) new ASN1InputStream(bufferedInputStream).readObject());
            ContentInfo info = bag.getAuthSafe();
            Vector chain = new Vector();
            boolean unmarkedKey = false;
            boolean wrongPKCS12Zero = false;
            if (bag.getMacData() != null) {
                MacData mData = bag.getMacData();
                DigestInfo dInfo = mData.getMac();
                AlgorithmIdentifier algId = dInfo.getAlgorithmId();
                byte[] salt = mData.getSalt();
                int itCount = mData.getIterationCount().intValue();
                byte[] data = ((ASN1OctetString) info.getContent()).getOctets();
                try {
                    byte[] res = calculatePbeMac(algId.getAlgorithm(), salt, itCount, password, false, data);
                    byte[] dig = dInfo.getDigest();
                    if (!Arrays.constantTimeAreEqual(res, dig)) {
                        if (password.length > 0) {
                            throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                        }
                        if (Arrays.constantTimeAreEqual(calculatePbeMac(algId.getAlgorithm(), salt, itCount, password, true, data), dig)) {
                            wrongPKCS12Zero = true;
                        } else {
                            throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                        }
                    }
                } catch (IOException e2) {
                    throw e2;
                } catch (Exception e3) {
                    throw new IOException("error constructing MAC: " + e3.toString());
                }
            }
            this.keys = new IgnoresCaseHashtable();
            this.localIds = new Hashtable();
            if (info.getContentType().equals(data)) {
                ContentInfo[] c = AuthenticatedSafe.getInstance(new ASN1InputStream(((ASN1OctetString) info.getContent()).getOctets()).readObject()).getContentInfo();
                for (i = 0; i != c.length; i++) {
                    ASN1Sequence seq;
                    int j;
                    EncryptedPrivateKeyInfo eIn;
                    PrivateKey privKey;
                    ASN1ObjectIdentifier aOid;
                    if (c[i].getContentType().equals(data)) {
                        seq = (ASN1Sequence) new ASN1InputStream(((ASN1OctetString) c[i].getContent()).getOctets()).readObject();
                        for (j = 0; j != seq.size(); j++) {
                            b = SafeBag.getInstance(seq.getObjectAt(j));
                            if (b.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                eIn = EncryptedPrivateKeyInfo.getInstance(b.getBagValue());
                                privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);
                                bagAttr = (PKCS12BagAttributeCarrier) privKey;
                                alias = null;
                                localId = null;
                                if (b.getBagAttributes() != null) {
                                    e = b.getBagAttributes().getObjects();
                                    while (e.hasMoreElements()) {
                                        sq = (ASN1Sequence) e.nextElement();
                                        aOid = (ASN1ObjectIdentifier) sq.getObjectAt(0);
                                        attrSet = (ASN1Set) sq.getObjectAt(1);
                                        attr = null;
                                        if (attrSet.size() > 0) {
                                            attr = (ASN1Primitive) attrSet.getObjectAt(0);
                                            existing = bagAttr.getBagAttribute(aOid);
                                            if (existing == null) {
                                                bagAttr.setBagAttribute(aOid, attr);
                                            } else if (!existing.toASN1Primitive().equals(attr)) {
                                                throw new IOException("attempt to add existing attribute with different value");
                                            }
                                        }
                                        if (aOid.equals(pkcs_9_at_friendlyName)) {
                                            alias = ((DERBMPString) attr).getString();
                                            this.keys.put(alias, privKey);
                                        } else {
                                            if (aOid.equals(pkcs_9_at_localKeyId)) {
                                                localId = (ASN1OctetString) attr;
                                            }
                                        }
                                    }
                                }
                                if (localId != null) {
                                    str = new String(Hex.encode(localId.getOctets()));
                                    if (alias == null) {
                                        this.keys.put(str, privKey);
                                    } else {
                                        this.localIds.put(alias, str);
                                    }
                                } else {
                                    unmarkedKey = true;
                                    this.keys.put("unmarked", privKey);
                                }
                            } else if (b.getBagId().equals(certBag)) {
                                chain.addElement(b);
                            } else {
                                System.out.println("extra in data " + b.getBagId());
                                System.out.println(ASN1Dump.dumpAsString(b));
                            }
                        }
                        continue;
                    } else if (c[i].getContentType().equals(encryptedData)) {
                        EncryptedData d = EncryptedData.getInstance(c[i].getContent());
                        seq = (ASN1Sequence) ASN1Primitive.fromByteArray(cryptData(false, d.getEncryptionAlgorithm(), password, wrongPKCS12Zero, d.getContent().getOctets()));
                        for (j = 0; j != seq.size(); j++) {
                            b = SafeBag.getInstance(seq.getObjectAt(j));
                            if (b.getBagId().equals(certBag)) {
                                chain.addElement(b);
                            } else if (b.getBagId().equals(pkcs8ShroudedKeyBag)) {
                                eIn = EncryptedPrivateKeyInfo.getInstance(b.getBagValue());
                                privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);
                                bagAttr = (PKCS12BagAttributeCarrier) privKey;
                                alias = null;
                                localId = null;
                                e = b.getBagAttributes().getObjects();
                                while (e.hasMoreElements()) {
                                    sq = (ASN1Sequence) e.nextElement();
                                    aOid = (ASN1ObjectIdentifier) sq.getObjectAt(0);
                                    attrSet = (ASN1Set) sq.getObjectAt(1);
                                    attr = null;
                                    if (attrSet.size() > 0) {
                                        attr = (ASN1Primitive) attrSet.getObjectAt(0);
                                        existing = bagAttr.getBagAttribute(aOid);
                                        if (existing == null) {
                                            bagAttr.setBagAttribute(aOid, attr);
                                        } else if (!existing.toASN1Primitive().equals(attr)) {
                                            throw new IOException("attempt to add existing attribute with different value");
                                        }
                                    }
                                    if (aOid.equals(pkcs_9_at_friendlyName)) {
                                        alias = ((DERBMPString) attr).getString();
                                        this.keys.put(alias, privKey);
                                    } else {
                                        if (aOid.equals(pkcs_9_at_localKeyId)) {
                                            localId = (ASN1OctetString) attr;
                                        }
                                    }
                                }
                                str = new String(Hex.encode(localId.getOctets()));
                                if (alias == null) {
                                    this.keys.put(str, privKey);
                                } else {
                                    this.localIds.put(alias, str);
                                }
                            } else if (b.getBagId().equals(keyBag)) {
                                privKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(b.getBagValue()));
                                bagAttr = (PKCS12BagAttributeCarrier) privKey;
                                alias = null;
                                localId = null;
                                e = b.getBagAttributes().getObjects();
                                while (e.hasMoreElements()) {
                                    sq = ASN1Sequence.getInstance(e.nextElement());
                                    aOid = ASN1ObjectIdentifier.getInstance(sq.getObjectAt(0));
                                    attrSet = ASN1Set.getInstance(sq.getObjectAt(1));
                                    if (attrSet.size() > 0) {
                                        attr = (ASN1Primitive) attrSet.getObjectAt(0);
                                        existing = bagAttr.getBagAttribute(aOid);
                                        if (existing == null) {
                                            bagAttr.setBagAttribute(aOid, attr);
                                        } else if (!existing.toASN1Primitive().equals(attr)) {
                                            throw new IOException("attempt to add existing attribute with different value");
                                        }
                                        if (aOid.equals(pkcs_9_at_friendlyName)) {
                                            alias = ((DERBMPString) attr).getString();
                                            this.keys.put(alias, privKey);
                                        } else {
                                            if (aOid.equals(pkcs_9_at_localKeyId)) {
                                                localId = (ASN1OctetString) attr;
                                            }
                                        }
                                    }
                                }
                                str = new String(Hex.encode(localId.getOctets()));
                                if (alias == null) {
                                    this.keys.put(str, privKey);
                                } else {
                                    this.localIds.put(alias, str);
                                }
                            } else {
                                System.out.println("extra in encryptedData " + b.getBagId());
                                System.out.println(ASN1Dump.dumpAsString(b));
                            }
                        }
                        continue;
                    } else {
                        System.out.println("extra " + c[i].getContentType().getId());
                        System.out.println("extra " + ASN1Dump.dumpAsString(c[i].getContent()));
                    }
                }
            }
            this.certs = new IgnoresCaseHashtable();
            this.chainCerts = new Hashtable();
            this.keyCerts = new Hashtable();
            i = 0;
            while (i != chain.size()) {
                b = (SafeBag) chain.elementAt(i);
                CertBag cb = CertBag.getInstance(b.getBagValue());
                if (cb.getCertId().equals(x509Certificate)) {
                    try {
                        Certificate cert = this.certFact.generateCertificate(new ByteArrayInputStream(((ASN1OctetString) cb.getCertValue()).getOctets()));
                        localId = null;
                        alias = null;
                        if (b.getBagAttributes() != null) {
                            e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements()) {
                                sq = ASN1Sequence.getInstance(e.nextElement());
                                ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(sq.getObjectAt(0));
                                attrSet = ASN1Set.getInstance(sq.getObjectAt(1));
                                if (attrSet.size() > 0) {
                                    attr = (ASN1Primitive) attrSet.getObjectAt(0);
                                    if (cert instanceof PKCS12BagAttributeCarrier) {
                                        bagAttr = (PKCS12BagAttributeCarrier) cert;
                                        existing = bagAttr.getBagAttribute(oid);
                                        if (existing == null) {
                                            bagAttr.setBagAttribute(oid, attr);
                                        } else if (!existing.toASN1Primitive().equals(attr)) {
                                            throw new IOException("attempt to add existing attribute with different value");
                                        }
                                    }
                                    if (oid.equals(pkcs_9_at_friendlyName)) {
                                        alias = ((DERBMPString) attr).getString();
                                    } else {
                                        if (oid.equals(pkcs_9_at_localKeyId)) {
                                            localId = (ASN1OctetString) attr;
                                        }
                                    }
                                }
                            }
                        }
                        this.chainCerts.put(new CertId(cert.getPublicKey()), cert);
                        if (!unmarkedKey) {
                            if (localId != null) {
                                this.keyCerts.put(new String(Hex.encode(localId.getOctets())), cert);
                            }
                            if (alias != null) {
                                this.certs.put(alias, cert);
                            }
                        } else if (this.keyCerts.isEmpty()) {
                            str = new String(Hex.encode(createSubjectKeyId(cert.getPublicKey()).getKeyIdentifier()));
                            this.keyCerts.put(str, cert);
                            this.keys.put(str, this.keys.remove("unmarked"));
                        }
                        i++;
                    } catch (Exception e32) {
                        throw new RuntimeException(e32.toString());
                    }
                }
                throw new RuntimeException("Unsupported certificate type: " + cb.getCertId());
            }
        }
    }

    public void engineStore(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            throw new IllegalArgumentException("'param' arg cannot be null");
        }
        if (!(param instanceof PKCS12StoreParameter) ? param instanceof JDKPKCS12StoreParameter : true) {
            PKCS12StoreParameter bcParam;
            char[] password;
            if (param instanceof PKCS12StoreParameter) {
                bcParam = (PKCS12StoreParameter) param;
            } else {
                bcParam = new PKCS12StoreParameter(((JDKPKCS12StoreParameter) param).getOutputStream(), param.getProtectionParameter(), ((JDKPKCS12StoreParameter) param).isUseDEREncoding());
            }
            ProtectionParameter protParam = param.getProtectionParameter();
            if (protParam == null) {
                password = null;
            } else if (protParam instanceof PasswordProtection) {
                password = ((PasswordProtection) protParam).getPassword();
            } else {
                throw new IllegalArgumentException("No support for protection parameter of type " + protParam.getClass().getName());
            }
            doStore(bcParam.getOutputStream(), password, bcParam.isForDEREncoding());
            return;
        }
        throw new IllegalArgumentException("No support for 'param' of type " + param.getClass().getName());
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException {
        doStore(stream, password, false);
    }

    private void doStore(OutputStream stream, char[] password, boolean useDEREncoding) throws IOException {
        if (password == null) {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }
        String name;
        PKCS12BagAttributeCarrier bagAttrs;
        DERBMPString nm;
        Enumeration e;
        ASN1ObjectIdentifier oid;
        ASN1EncodableVector aSN1EncodableVector;
        Certificate cert;
        boolean cAttrSet;
        CertBag certBag;
        ASN1EncodableVector fName;
        ASN1EncodableVector fSeq;
        DEROutputStream dEROutputStream;
        ASN1EncodableVector keyS = new ASN1EncodableVector();
        Enumeration ks = this.keys.keys();
        while (ks.hasMoreElements()) {
            Certificate ct;
            ASN1EncodableVector kSeq;
            byte[] kSalt = new byte[20];
            this.random.nextBytes(kSalt);
            name = (String) ks.nextElement();
            PrivateKey privKey = (PrivateKey) this.keys.get(name);
            PKCS12PBEParams pKCS12PBEParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(this.keyAlgorithm, pKCS12PBEParams.toASN1Primitive()), wrapKey(this.keyAlgorithm.getId(), privKey, pKCS12PBEParams, password));
            boolean attrSet = false;
            ASN1EncodableVector kName = new ASN1EncodableVector();
            if (privKey instanceof PKCS12BagAttributeCarrier) {
                bagAttrs = (PKCS12BagAttributeCarrier) privKey;
                nm = (DERBMPString) bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                if (nm == null || (nm.getString().equals(name) ^ 1) != 0) {
                    bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                }
                if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                    ct = engineGetCertificate(name);
                    bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(ct.getPublicKey()));
                }
                e = bagAttrs.getBagAttributeKeys();
                while (e.hasMoreElements()) {
                    oid = (ASN1ObjectIdentifier) e.nextElement();
                    kSeq = new ASN1EncodableVector();
                    kSeq.add(oid);
                    kSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                    attrSet = true;
                    kName.add(new DERSequence(kSeq));
                }
            }
            if (!attrSet) {
                kSeq = new ASN1EncodableVector();
                ct = engineGetCertificate(name);
                kSeq.add(pkcs_9_at_localKeyId);
                aSN1EncodableVector = kSeq;
                aSN1EncodableVector.add(new DERSet(createSubjectKeyId(ct.getPublicKey())));
                kName.add(new DERSequence(kSeq));
                kSeq = new ASN1EncodableVector();
                kSeq.add(pkcs_9_at_friendlyName);
                kSeq.add(new DERSet(new DERBMPString(name)));
                kName.add(new DERSequence(kSeq));
            }
            keyS.add(new SafeBag(pkcs8ShroudedKeyBag, encryptedPrivateKeyInfo.toASN1Primitive(), new DERSet(kName)));
        }
        BEROctetString bEROctetString = new BEROctetString(new DERSequence(keyS).getEncoded(ASN1Encoding.DER));
        byte[] cSalt = new byte[20];
        this.random.nextBytes(cSalt);
        ASN1EncodableVector certSeq = new ASN1EncodableVector();
        AlgorithmIdentifier cAlgId = new AlgorithmIdentifier(this.certAlgorithm, new PKCS12PBEParams(cSalt, MIN_ITERATIONS).toASN1Primitive());
        Hashtable doneCerts = new Hashtable();
        Enumeration cs = this.keys.keys();
        while (cs.hasMoreElements()) {
            try {
                name = (String) cs.nextElement();
                cert = engineGetCertificate(name);
                cAttrSet = false;
                certBag = new CertBag(x509Certificate, new DEROctetString(cert.getEncoded()));
                fName = new ASN1EncodableVector();
                if (cert instanceof PKCS12BagAttributeCarrier) {
                    bagAttrs = (PKCS12BagAttributeCarrier) cert;
                    nm = (DERBMPString) bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm == null || (nm.getString().equals(name) ^ 1) != 0) {
                        bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                    }
                    if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                        bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(cert.getPublicKey()));
                    }
                    e = bagAttrs.getBagAttributeKeys();
                    while (e.hasMoreElements()) {
                        oid = (ASN1ObjectIdentifier) e.nextElement();
                        fSeq = new ASN1EncodableVector();
                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));
                        cAttrSet = true;
                    }
                }
                if (!cAttrSet) {
                    fSeq = new ASN1EncodableVector();
                    fSeq.add(pkcs_9_at_localKeyId);
                    aSN1EncodableVector = fSeq;
                    aSN1EncodableVector.add(new DERSet(createSubjectKeyId(cert.getPublicKey())));
                    fName.add(new DERSequence(fSeq));
                    fSeq = new ASN1EncodableVector();
                    fSeq.add(pkcs_9_at_friendlyName);
                    fSeq.add(new DERSet(new DERBMPString(name)));
                    fName.add(new DERSequence(fSeq));
                }
                certSeq.add(new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(fName)));
                doneCerts.put(cert, cert);
            } catch (CertificateEncodingException e2) {
                throw new IOException("Error encoding certificate: " + e2.toString());
            }
        }
        cs = this.certs.keys();
        while (cs.hasMoreElements()) {
            try {
                String certId = (String) cs.nextElement();
                cert = (Certificate) this.certs.get(certId);
                cAttrSet = false;
                if (this.keys.get(certId) == null) {
                    certBag = new CertBag(x509Certificate, new DEROctetString(cert.getEncoded()));
                    fName = new ASN1EncodableVector();
                    if (cert instanceof PKCS12BagAttributeCarrier) {
                        bagAttrs = (PKCS12BagAttributeCarrier) cert;
                        nm = (DERBMPString) bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                        if (nm == null || (nm.getString().equals(certId) ^ 1) != 0) {
                            bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(certId));
                        }
                        e = bagAttrs.getBagAttributeKeys();
                        while (e.hasMoreElements()) {
                            oid = (ASN1ObjectIdentifier) e.nextElement();
                            if (!oid.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                fSeq = new ASN1EncodableVector();
                                fSeq.add(oid);
                                fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                                fName.add(new DERSequence(fSeq));
                                cAttrSet = true;
                            }
                        }
                    }
                    if (!cAttrSet) {
                        fSeq = new ASN1EncodableVector();
                        fSeq.add(pkcs_9_at_friendlyName);
                        fSeq.add(new DERSet(new DERBMPString(certId)));
                        fName.add(new DERSequence(fSeq));
                    }
                    certSeq.add(new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(fName)));
                    doneCerts.put(cert, cert);
                }
            } catch (CertificateEncodingException e22) {
                throw new IOException("Error encoding certificate: " + e22.toString());
            }
        }
        Set usedSet = getUsedCertificateSet();
        cs = this.chainCerts.keys();
        while (cs.hasMoreElements()) {
            try {
                cert = (Certificate) this.chainCerts.get((CertId) cs.nextElement());
                if (usedSet.contains(cert) && doneCerts.get(cert) == null) {
                    certBag = new CertBag(x509Certificate, new DEROctetString(cert.getEncoded()));
                    fName = new ASN1EncodableVector();
                    if (cert instanceof PKCS12BagAttributeCarrier) {
                        bagAttrs = (PKCS12BagAttributeCarrier) cert;
                        e = bagAttrs.getBagAttributeKeys();
                        while (e.hasMoreElements()) {
                            oid = (ASN1ObjectIdentifier) e.nextElement();
                            if (!oid.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) {
                                fSeq = new ASN1EncodableVector();
                                fSeq.add(oid);
                                fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                                fName.add(new DERSequence(fSeq));
                            }
                        }
                    }
                    certSeq.add(new SafeBag(certBag, certBag.toASN1Primitive(), new DERSet(fName)));
                }
            } catch (CertificateEncodingException e222) {
                throw new IOException("Error encoding certificate: " + e222.toString());
            }
        }
        EncryptedData encryptedData = new EncryptedData(data, cAlgId, new BEROctetString(cryptData(true, cAlgId, password, false, new DERSequence(certSeq).getEncoded(ASN1Encoding.DER))));
        AuthenticatedSafe authenticatedSafe = new AuthenticatedSafe(new ContentInfo[]{new ContentInfo(data, bEROctetString), new ContentInfo(encryptedData, encryptedData.toASN1Primitive())});
        OutputStream bOut = new ByteArrayOutputStream();
        if (useDEREncoding) {
            dEROutputStream = new DEROutputStream(bOut);
        } else {
            dEROutputStream = new BEROutputStream(bOut);
        }
        asn1Out.writeObject(authenticatedSafe);
        ContentInfo contentInfo = new ContentInfo(data, new BEROctetString(bOut.toByteArray()));
        byte[] mSalt = new byte[20];
        this.random.nextBytes(mSalt);
        try {
            Pfx pfx = new Pfx(contentInfo, new MacData(new DigestInfo(new AlgorithmIdentifier(id_SHA1, DERNull.INSTANCE), calculatePbeMac(id_SHA1, mSalt, MIN_ITERATIONS, password, false, ((ASN1OctetString) contentInfo.getContent()).getOctets())), mSalt, MIN_ITERATIONS));
            if (useDEREncoding) {
                dEROutputStream = new DEROutputStream(stream);
            } else {
                dEROutputStream = new BEROutputStream(stream);
            }
            asn1Out.writeObject(pfx);
        } catch (Exception e3) {
            throw new IOException("error constructing MAC: " + e3.toString());
        }
    }

    private Set getUsedCertificateSet() {
        Set usedSet = new HashSet();
        Enumeration en = this.keys.keys();
        while (en.hasMoreElements()) {
            Certificate[] certs = engineGetCertificateChain((String) en.nextElement());
            for (int i = 0; i != certs.length; i++) {
                usedSet.add(certs[i]);
            }
        }
        en = this.certs.keys();
        while (en.hasMoreElements()) {
            usedSet.add(engineGetCertificate((String) en.nextElement()));
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
