package com.android.org.bouncycastle.jcajce.provider.keystore.bc;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.digests.SHA1Digest;
import com.android.org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import com.android.org.bouncycastle.crypto.io.DigestInputStream;
import com.android.org.bouncycastle.crypto.io.DigestOutputStream;
import com.android.org.bouncycastle.crypto.io.MacInputStream;
import com.android.org.bouncycastle.crypto.io.MacOutputStream;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.interfaces.BCKeyStore;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.io.Streams;
import com.android.org.bouncycastle.util.io.TeeOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BcKeyStoreSpi extends KeyStoreSpi implements BCKeyStore {
    static final int CERTIFICATE = 1;
    static final int KEY = 2;
    private static final String KEY_CIPHER = "PBEWithSHAAnd3-KeyTripleDES-CBC";
    static final int KEY_PRIVATE = 0;
    static final int KEY_PUBLIC = 1;
    private static final int KEY_SALT_SIZE = 20;
    static final int KEY_SECRET = 2;
    private static final int MIN_ITERATIONS = 1024;
    static final int NULL = 0;
    static final int SEALED = 4;
    static final int SECRET = 3;
    private static final String STORE_CIPHER = "PBEWithSHAAndTwofish-CBC";
    private static final int STORE_SALT_SIZE = 20;
    private static final int STORE_VERSION = 2;
    private final JcaJceHelper helper = new DefaultJcaJceHelper();
    protected SecureRandom random = new SecureRandom();
    protected Hashtable table = new Hashtable();
    protected int version;

    public static class BouncyCastleStore extends BcKeyStoreSpi {
        public BouncyCastleStore() {
            super(1);
        }

        public void engineLoad(InputStream stream, char[] password) throws IOException {
            String cipherAlg;
            this.table.clear();
            if (stream != null) {
                DataInputStream dIn = new DataInputStream(stream);
                int version = dIn.readInt();
                if (version == 2 || version == 0 || version == 1) {
                    byte[] salt = new byte[dIn.readInt()];
                    if (salt.length == 20) {
                        dIn.readFully(salt);
                        int iterationCount = dIn.readInt();
                        if (iterationCount < 0 || iterationCount > 4096) {
                            throw new IOException("Key store corrupted.");
                        }
                        if (version == 0) {
                            cipherAlg = "OldPBEWithSHAAndTwofish-CBC";
                        } else {
                            cipherAlg = BcKeyStoreSpi.STORE_CIPHER;
                        }
                        CipherInputStream cIn = new CipherInputStream(dIn, makePBECipher(cipherAlg, 2, password, salt, iterationCount));
                        Digest dig = new SHA1Digest();
                        loadStore(new DigestInputStream(cIn, dig));
                        byte[] hash = new byte[dig.getDigestSize()];
                        dig.doFinal(hash, 0);
                        byte[] oldHash = new byte[dig.getDigestSize()];
                        Streams.readFully(cIn, oldHash);
                        if (!Arrays.constantTimeAreEqual(hash, oldHash)) {
                            this.table.clear();
                            throw new IOException("KeyStore integrity check failed.");
                        }
                        return;
                    }
                    throw new IOException("Key store corrupted.");
                }
                throw new IOException("Wrong version of key store.");
            }
        }

        public void engineStore(OutputStream stream, char[] password) throws IOException {
            DataOutputStream dOut = new DataOutputStream(stream);
            byte[] salt = new byte[20];
            int iterationCount = BcKeyStoreSpi.MIN_ITERATIONS + (this.random.nextInt() & 1023);
            this.random.nextBytes(salt);
            dOut.writeInt(this.version);
            dOut.writeInt(salt.length);
            dOut.write(salt);
            dOut.writeInt(iterationCount);
            CipherOutputStream cOut = new CipherOutputStream(dOut, makePBECipher(BcKeyStoreSpi.STORE_CIPHER, 1, password, salt, iterationCount));
            DigestOutputStream dgOut = new DigestOutputStream(new SHA1Digest());
            saveStore(new TeeOutputStream(cOut, dgOut));
            cOut.write(dgOut.getDigest());
            cOut.close();
        }
    }

    public static class Std extends BcKeyStoreSpi {
        public Std() {
            super(2);
        }
    }

    private class StoreEntry {
        String alias;
        Certificate[] certChain;
        Date date;
        Object obj;
        int type;

        StoreEntry(String alias2, Certificate obj2) {
            this.date = new Date();
            this.type = 1;
            this.alias = alias2;
            this.obj = obj2;
            this.certChain = null;
        }

        StoreEntry(String alias2, byte[] obj2, Certificate[] certChain2) {
            this.date = new Date();
            this.type = 3;
            this.alias = alias2;
            this.obj = obj2;
            this.certChain = certChain2;
        }

        StoreEntry(String alias2, Key key, char[] password, Certificate[] certChain2) throws Exception {
            this.date = new Date();
            this.type = 4;
            this.alias = alias2;
            this.certChain = certChain2;
            byte[] salt = new byte[20];
            BcKeyStoreSpi.this.random.setSeed(System.currentTimeMillis());
            BcKeyStoreSpi.this.random.nextBytes(salt);
            int iterationCount = BcKeyStoreSpi.MIN_ITERATIONS + (BcKeyStoreSpi.this.random.nextInt() & 1023);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            DataOutputStream dOut = new DataOutputStream(bOut);
            dOut.writeInt(salt.length);
            dOut.write(salt);
            dOut.writeInt(iterationCount);
            DataOutputStream dOut2 = new DataOutputStream(new CipherOutputStream(dOut, BcKeyStoreSpi.this.makePBECipher(BcKeyStoreSpi.KEY_CIPHER, 1, password, salt, iterationCount)));
            BcKeyStoreSpi.this.encodeKey(key, dOut2);
            dOut2.close();
            this.obj = bOut.toByteArray();
        }

        StoreEntry(String alias2, Date date2, int type2, Object obj2) {
            this.date = new Date();
            this.alias = alias2;
            this.date = date2;
            this.type = type2;
            this.obj = obj2;
        }

        StoreEntry(String alias2, Date date2, int type2, Object obj2, Certificate[] certChain2) {
            this.date = new Date();
            this.alias = alias2;
            this.date = date2;
            this.type = type2;
            this.obj = obj2;
            this.certChain = certChain2;
        }

        /* access modifiers changed from: package-private */
        public int getType() {
            return this.type;
        }

        /* access modifiers changed from: package-private */
        public String getAlias() {
            return this.alias;
        }

        /* access modifiers changed from: package-private */
        public Object getObject() {
            return this.obj;
        }

        /* access modifiers changed from: package-private */
        public Object getObject(char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            Key k;
            DataInputStream dIn;
            ByteArrayInputStream bIn;
            char[] cArr = password;
            if ((cArr == null || cArr.length == 0) && (this.obj instanceof Key)) {
                return this.obj;
            }
            if (this.type == 4) {
                DataInputStream dIn2 = new DataInputStream(new ByteArrayInputStream((byte[]) this.obj));
                try {
                    byte[] salt = new byte[dIn2.readInt()];
                    dIn2.readFully(salt);
                    try {
                        return BcKeyStoreSpi.this.decodeKey(new DataInputStream(new CipherInputStream(dIn2, BcKeyStoreSpi.this.makePBECipher(BcKeyStoreSpi.KEY_CIPHER, 2, cArr, salt, dIn2.readInt()))));
                    } catch (Exception e) {
                        Exception exc = e;
                        ByteArrayInputStream bIn2 = new ByteArrayInputStream((byte[]) this.obj);
                        DataInputStream dIn3 = new DataInputStream(bIn2);
                        byte[] salt2 = new byte[dIn3.readInt()];
                        dIn3.readFully(salt2);
                        int iterationCount = dIn3.readInt();
                        try {
                            k = BcKeyStoreSpi.this.decodeKey(new DataInputStream(new CipherInputStream(dIn3, BcKeyStoreSpi.this.makePBECipher("BrokenPBEWithSHAAnd3-KeyTripleDES-CBC", 2, cArr, salt2, iterationCount))));
                        } catch (Exception e2) {
                            bIn2 = new ByteArrayInputStream((byte[]) this.obj);
                            dIn3 = new DataInputStream(bIn2);
                            salt2 = new byte[dIn3.readInt()];
                            dIn3.readFully(salt2);
                            iterationCount = dIn3.readInt();
                            k = BcKeyStoreSpi.this.decodeKey(new DataInputStream(new CipherInputStream(dIn3, BcKeyStoreSpi.this.makePBECipher("OldPBEWithSHAAnd3-KeyTripleDES-CBC", 2, cArr, salt2, iterationCount))));
                        }
                        dIn = dIn3;
                        bIn = bIn2;
                        int iterationCount2 = iterationCount;
                        if (k != null) {
                            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                            DataOutputStream dOut = new DataOutputStream(bOut);
                            dOut.writeInt(salt2.length);
                            dOut.write(salt2);
                            dOut.writeInt(iterationCount2);
                            DataOutputStream dOut2 = new DataOutputStream(new CipherOutputStream(dOut, BcKeyStoreSpi.this.makePBECipher(BcKeyStoreSpi.KEY_CIPHER, 1, cArr, salt2, iterationCount2)));
                            BcKeyStoreSpi.this.encodeKey(k, dOut2);
                            dOut2.close();
                            this.obj = bOut.toByteArray();
                            return k;
                        }
                        throw new UnrecoverableKeyException("no match");
                    } catch (Exception e3) {
                        ByteArrayInputStream byteArrayInputStream = bIn;
                        DataInputStream dataInputStream = dIn;
                        throw new UnrecoverableKeyException("no match");
                    }
                } catch (Exception e4) {
                    throw new UnrecoverableKeyException("no match");
                }
            } else {
                throw new RuntimeException("forget something!");
            }
        }

        /* access modifiers changed from: package-private */
        public Certificate[] getCertificateChain() {
            return this.certChain;
        }

        /* access modifiers changed from: package-private */
        public Date getDate() {
            return this.date;
        }
    }

    public static class Version1 extends BcKeyStoreSpi {
        public Version1() {
            super(1);
        }
    }

    public BcKeyStoreSpi(int version2) {
        this.version = version2;
    }

    private void encodeCertificate(Certificate cert, DataOutputStream dOut) throws IOException {
        try {
            byte[] cEnc = cert.getEncoded();
            dOut.writeUTF(cert.getType());
            dOut.writeInt(cEnc.length);
            dOut.write(cEnc);
        } catch (CertificateEncodingException ex) {
            throw new IOException(ex.toString());
        }
    }

    private Certificate decodeCertificate(DataInputStream dIn) throws IOException {
        String type = dIn.readUTF();
        byte[] cEnc = new byte[dIn.readInt()];
        dIn.readFully(cEnc);
        try {
            return this.helper.createCertificateFactory(type).generateCertificate(new ByteArrayInputStream(cEnc));
        } catch (NoSuchProviderException ex) {
            throw new IOException(ex.toString());
        } catch (CertificateException ex2) {
            throw new IOException(ex2.toString());
        }
    }

    /* access modifiers changed from: private */
    public void encodeKey(Key key, DataOutputStream dOut) throws IOException {
        byte[] enc = key.getEncoded();
        if (key instanceof PrivateKey) {
            dOut.write(0);
        } else if (key instanceof PublicKey) {
            dOut.write(1);
        } else {
            dOut.write(2);
        }
        dOut.writeUTF(key.getFormat());
        dOut.writeUTF(key.getAlgorithm());
        dOut.writeInt(enc.length);
        dOut.write(enc);
    }

    /* access modifiers changed from: private */
    public Key decodeKey(DataInputStream dIn) throws IOException {
        KeySpec spec;
        int keyType = dIn.read();
        String format = dIn.readUTF();
        String algorithm = dIn.readUTF();
        byte[] enc = new byte[dIn.readInt()];
        dIn.readFully(enc);
        if (format.equals("PKCS#8") || format.equals("PKCS8")) {
            spec = new PKCS8EncodedKeySpec(enc);
        } else if (format.equals("X.509") || format.equals("X509")) {
            spec = new X509EncodedKeySpec(enc);
        } else if (format.equals("RAW")) {
            return new SecretKeySpec(enc, algorithm);
        } else {
            throw new IOException("Key format " + format + " not recognised!");
        }
        switch (keyType) {
            case 0:
                return this.helper.createKeyFactory(algorithm).generatePrivate(spec);
            case 1:
                return this.helper.createKeyFactory(algorithm).generatePublic(spec);
            case 2:
                return this.helper.createSecretKeyFactory(algorithm).generateSecret(spec);
            default:
                try {
                    throw new IOException("Key type " + keyType + " not recognised!");
                } catch (Exception e) {
                    throw new IOException("Exception creating key: " + e.toString());
                }
        }
    }

    /* access modifiers changed from: protected */
    public Cipher makePBECipher(String algorithm, int mode, char[] password, byte[] salt, int iterationCount) throws IOException {
        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = this.helper.createSecretKeyFactory(algorithm);
            PBEParameterSpec defParams = new PBEParameterSpec(salt, iterationCount);
            Cipher cipher = this.helper.createCipher(algorithm);
            cipher.init(mode, keyFact.generateSecret(pbeSpec), defParams);
            return cipher;
        } catch (Exception e) {
            throw new IOException("Error initialising store of key store: " + e);
        }
    }

    public void setRandom(SecureRandom rand) {
        this.random = rand;
    }

    public Enumeration engineAliases() {
        return this.table.keys();
    }

    public boolean engineContainsAlias(String alias) {
        return this.table.get(alias) != null;
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (this.table.get(alias) != null) {
            this.table.remove(alias);
        }
    }

    public Certificate engineGetCertificate(String alias) {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry != null) {
            if (entry.getType() == 1) {
                return (Certificate) entry.getObject();
            }
            Certificate[] chain = entry.getCertificateChain();
            if (chain != null) {
                return chain[0];
            }
        }
        return null;
    }

    public String engineGetCertificateAlias(Certificate cert) {
        Enumeration e = this.table.elements();
        while (e.hasMoreElements()) {
            StoreEntry entry = (StoreEntry) e.nextElement();
            if (!(entry.getObject() instanceof Certificate)) {
                Certificate[] chain = entry.getCertificateChain();
                if (chain != null && chain[0].equals(cert)) {
                    return entry.getAlias();
                }
            } else if (((Certificate) entry.getObject()).equals(cert)) {
                return entry.getAlias();
            }
        }
        return null;
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry != null) {
            return entry.getCertificateChain();
        }
        return null;
    }

    public Date engineGetCreationDate(String alias) {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry != null) {
            return entry.getDate();
        }
        return null;
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry == null || entry.getType() == 1) {
            return null;
        }
        return (Key) entry.getObject(password);
    }

    public boolean engineIsCertificateEntry(String alias) {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry == null || entry.getType() != 1) {
            return false;
        }
        return true;
    }

    public boolean engineIsKeyEntry(String alias) {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry == null || entry.getType() == 1) {
            return false;
        }
        return true;
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        StoreEntry entry = (StoreEntry) this.table.get(alias);
        if (entry == null || entry.getType() == 1) {
            this.table.put(alias, new StoreEntry(alias, cert));
            return;
        }
        throw new KeyStoreException("key store already has a key entry with alias " + alias);
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        this.table.put(alias, new StoreEntry(alias, key, chain));
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (!(key instanceof PrivateKey) || chain != null) {
            try {
                Hashtable hashtable = this.table;
                StoreEntry storeEntry = new StoreEntry(alias, key, password, chain);
                hashtable.put(alias, storeEntry);
            } catch (Exception e) {
                throw new KeyStoreException(e.toString());
            }
        } else {
            throw new KeyStoreException("no certificate chain for private key");
        }
    }

    public int engineSize() {
        return this.table.size();
    }

    /* access modifiers changed from: protected */
    public void loadStore(InputStream in) throws IOException {
        DataInputStream dIn = new DataInputStream(in);
        int type = dIn.read();
        while (true) {
            int type2 = type;
            if (type2 > 0) {
                String alias = dIn.readUTF();
                Date date = new Date(dIn.readLong());
                int chainLength = dIn.readInt();
                Certificate[] chain = null;
                if (chainLength != 0) {
                    chain = new Certificate[chainLength];
                    for (int i = 0; i != chainLength; i++) {
                        chain[i] = decodeCertificate(dIn);
                    }
                }
                Certificate[] chain2 = chain;
                switch (type2) {
                    case 1:
                        Certificate cert = decodeCertificate(dIn);
                        Hashtable hashtable = this.table;
                        StoreEntry storeEntry = new StoreEntry(alias, date, 1, (Object) cert);
                        hashtable.put(alias, storeEntry);
                        break;
                    case 2:
                        Key key = decodeKey(dIn);
                        Hashtable hashtable2 = this.table;
                        int i2 = chainLength;
                        StoreEntry storeEntry2 = new StoreEntry(alias, date, 2, key, chain2);
                        hashtable2.put(alias, storeEntry2);
                        break;
                    case 3:
                    case 4:
                        byte[] b = new byte[dIn.readInt()];
                        dIn.readFully(b);
                        Hashtable hashtable3 = this.table;
                        StoreEntry storeEntry3 = new StoreEntry(alias, date, type2, b, chain2);
                        hashtable3.put(alias, storeEntry3);
                        int i3 = chainLength;
                        break;
                    default:
                        int i4 = chainLength;
                        throw new RuntimeException("Unknown object type in store.");
                }
                type = dIn.read();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void saveStore(OutputStream out) throws IOException {
        Enumeration e = this.table.elements();
        DataOutputStream dOut = new DataOutputStream(out);
        while (true) {
            if (e.hasMoreElements()) {
                StoreEntry entry = (StoreEntry) e.nextElement();
                dOut.write(entry.getType());
                dOut.writeUTF(entry.getAlias());
                dOut.writeLong(entry.getDate().getTime());
                Certificate[] chain = entry.getCertificateChain();
                if (chain == null) {
                    dOut.writeInt(0);
                } else {
                    dOut.writeInt(chain.length);
                    for (int i = 0; i != chain.length; i++) {
                        encodeCertificate(chain[i], dOut);
                    }
                }
                switch (entry.getType()) {
                    case 1:
                        encodeCertificate((Certificate) entry.getObject(), dOut);
                        break;
                    case 2:
                        encodeKey((Key) entry.getObject(), dOut);
                        break;
                    case 3:
                    case 4:
                        byte[] b = (byte[]) entry.getObject();
                        dOut.writeInt(b.length);
                        dOut.write(b);
                        break;
                    default:
                        throw new RuntimeException("Unknown object type in store.");
                }
            } else {
                dOut.write(0);
                return;
            }
        }
    }

    public void engineLoad(InputStream stream, char[] password) throws IOException {
        CipherParameters macParams;
        InputStream inputStream = stream;
        char[] cArr = password;
        this.table.clear();
        if (inputStream != null) {
            DataInputStream dIn = new DataInputStream(inputStream);
            int version2 = dIn.readInt();
            if (version2 == 2 || version2 == 0 || version2 == 1) {
                int saltLength = dIn.readInt();
                if (saltLength > 0) {
                    byte[] salt = new byte[saltLength];
                    dIn.readFully(salt);
                    int iterationCount = dIn.readInt();
                    HMac hMac = new HMac(new SHA1Digest());
                    if (cArr == null || cArr.length == 0) {
                        loadStore(dIn);
                        dIn.readFully(new byte[hMac.getMacSize()]);
                    } else {
                        byte[] passKey = PBEParametersGenerator.PKCS12PasswordToBytes(password);
                        PBEParametersGenerator pbeGen = new PKCS12ParametersGenerator(new SHA1Digest());
                        pbeGen.init(passKey, salt, iterationCount);
                        if (version2 != 2) {
                            macParams = pbeGen.generateDerivedMacParameters(hMac.getMacSize());
                        } else {
                            macParams = pbeGen.generateDerivedMacParameters(hMac.getMacSize() * 8);
                        }
                        Arrays.fill(passKey, (byte) 0);
                        hMac.init(macParams);
                        loadStore(new MacInputStream(dIn, hMac));
                        byte[] mac = new byte[hMac.getMacSize()];
                        hMac.doFinal(mac, 0);
                        byte[] oldMac = new byte[hMac.getMacSize()];
                        dIn.readFully(oldMac);
                        if (!Arrays.constantTimeAreEqual(mac, oldMac)) {
                            this.table.clear();
                            throw new IOException("KeyStore integrity check failed.");
                        }
                    }
                    return;
                }
                throw new IOException("Invalid salt detected");
            }
            throw new IOException("Wrong version of key store.");
        }
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException {
        DataOutputStream dOut = new DataOutputStream(stream);
        byte[] salt = new byte[20];
        int iterationCount = MIN_ITERATIONS + (this.random.nextInt() & 1023);
        this.random.nextBytes(salt);
        dOut.writeInt(this.version);
        dOut.writeInt(salt.length);
        dOut.write(salt);
        dOut.writeInt(iterationCount);
        HMac hMac = new HMac(new SHA1Digest());
        MacOutputStream mOut = new MacOutputStream(hMac);
        PBEParametersGenerator pbeGen = new PKCS12ParametersGenerator(new SHA1Digest());
        byte[] passKey = PBEParametersGenerator.PKCS12PasswordToBytes(password);
        pbeGen.init(passKey, salt, iterationCount);
        if (this.version < 2) {
            hMac.init(pbeGen.generateDerivedMacParameters(hMac.getMacSize()));
        } else {
            hMac.init(pbeGen.generateDerivedMacParameters(hMac.getMacSize() * 8));
        }
        for (int i = 0; i != passKey.length; i++) {
            passKey[i] = 0;
        }
        saveStore(new TeeOutputStream(dOut, mOut));
        byte[] mac = new byte[hMac.getMacSize()];
        hMac.doFinal(mac, 0);
        dOut.write(mac);
        dOut.close();
    }

    static Provider getBouncyCastleProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
            return Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        return new BouncyCastleProvider();
    }
}
