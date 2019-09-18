package org.bouncycastle.jcajce.provider.keystore.bcfks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.bc.EncryptedObjectStoreData;
import org.bouncycastle.asn1.bc.EncryptedPrivateKeyData;
import org.bouncycastle.asn1.bc.EncryptedSecretKeyData;
import org.bouncycastle.asn1.bc.ObjectData;
import org.bouncycastle.asn1.bc.ObjectDataSequence;
import org.bouncycastle.asn1.bc.ObjectStore;
import org.bouncycastle.asn1.bc.ObjectStoreData;
import org.bouncycastle.asn1.bc.ObjectStoreIntegrityCheck;
import org.bouncycastle.asn1.bc.PbkdMacIntegrityCheck;
import org.bouncycastle.asn1.bc.SecretKeyData;
import org.bouncycastle.asn1.cms.CCMParameters;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.ScryptParams;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.nsri.NSRIObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.crypto.util.PBKDFConfig;
import org.bouncycastle.crypto.util.ScryptConfig;
import org.bouncycastle.jcajce.BCFKSStoreParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

class BcFKSKeyStoreSpi extends KeyStoreSpi {
    private static final BigInteger CERTIFICATE = BigInteger.valueOf(0);
    private static final BigInteger PRIVATE_KEY = BigInteger.valueOf(1);
    private static final BigInteger PROTECTED_PRIVATE_KEY = BigInteger.valueOf(3);
    private static final BigInteger PROTECTED_SECRET_KEY = BigInteger.valueOf(4);
    private static final BigInteger SECRET_KEY = BigInteger.valueOf(2);
    private static final Map<String, ASN1ObjectIdentifier> oidMap = new HashMap();
    private static final Map<ASN1ObjectIdentifier, String> publicAlgMap = new HashMap();
    private Date creationDate;
    private final Map<String, ObjectData> entries = new HashMap();
    private AlgorithmIdentifier hmacAlgorithm;
    private KeyDerivationFunc hmacPkbdAlgorithm;
    private Date lastModifiedDate;
    private final Map<String, PrivateKey> privateKeyCache = new HashMap();
    private final BouncyCastleProvider provider;

    public static class Def extends BcFKSKeyStoreSpi {
        public Def() {
            super(null);
        }

        public /* bridge */ /* synthetic */ Enumeration engineAliases() {
            return BcFKSKeyStoreSpi.super.engineAliases();
        }

        public /* bridge */ /* synthetic */ boolean engineContainsAlias(String str) {
            return BcFKSKeyStoreSpi.super.engineContainsAlias(str);
        }

        public /* bridge */ /* synthetic */ void engineDeleteEntry(String str) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineDeleteEntry(str);
        }

        public /* bridge */ /* synthetic */ Certificate engineGetCertificate(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCertificate(str);
        }

        public /* bridge */ /* synthetic */ String engineGetCertificateAlias(Certificate certificate) {
            return BcFKSKeyStoreSpi.super.engineGetCertificateAlias(certificate);
        }

        public /* bridge */ /* synthetic */ Certificate[] engineGetCertificateChain(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCertificateChain(str);
        }

        public /* bridge */ /* synthetic */ Date engineGetCreationDate(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCreationDate(str);
        }

        public /* bridge */ /* synthetic */ Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            return BcFKSKeyStoreSpi.super.engineGetKey(str, cArr);
        }

        public /* bridge */ /* synthetic */ boolean engineIsCertificateEntry(String str) {
            return BcFKSKeyStoreSpi.super.engineIsCertificateEntry(str);
        }

        public /* bridge */ /* synthetic */ boolean engineIsKeyEntry(String str) {
            return BcFKSKeyStoreSpi.super.engineIsKeyEntry(str);
        }

        public /* bridge */ /* synthetic */ void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            BcFKSKeyStoreSpi.super.engineLoad(inputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetCertificateEntry(str, certificate);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetKeyEntry(str, key, cArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetKeyEntry(str, bArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ int engineSize() {
            return BcFKSKeyStoreSpi.super.engineSize();
        }

        public /* bridge */ /* synthetic */ void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            BcFKSKeyStoreSpi.super.engineStore(outputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
            BcFKSKeyStoreSpi.super.engineStore(loadStoreParameter);
        }
    }

    public static class DefShared extends SharedKeyStoreSpi {
        public DefShared() {
            super(null);
        }

        public /* bridge */ /* synthetic */ Enumeration engineAliases() {
            return super.engineAliases();
        }

        public /* bridge */ /* synthetic */ boolean engineContainsAlias(String str) {
            return super.engineContainsAlias(str);
        }

        public /* bridge */ /* synthetic */ void engineDeleteEntry(String str) throws KeyStoreException {
            super.engineDeleteEntry(str);
        }

        public /* bridge */ /* synthetic */ Certificate engineGetCertificate(String str) {
            return super.engineGetCertificate(str);
        }

        public /* bridge */ /* synthetic */ String engineGetCertificateAlias(Certificate certificate) {
            return super.engineGetCertificateAlias(certificate);
        }

        public /* bridge */ /* synthetic */ Certificate[] engineGetCertificateChain(String str) {
            return super.engineGetCertificateChain(str);
        }

        public /* bridge */ /* synthetic */ Date engineGetCreationDate(String str) {
            return super.engineGetCreationDate(str);
        }

        public /* bridge */ /* synthetic */ Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            return super.engineGetKey(str, cArr);
        }

        public /* bridge */ /* synthetic */ boolean engineIsCertificateEntry(String str) {
            return super.engineIsCertificateEntry(str);
        }

        public /* bridge */ /* synthetic */ boolean engineIsKeyEntry(String str) {
            return super.engineIsKeyEntry(str);
        }

        public /* bridge */ /* synthetic */ void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            super.engineLoad(inputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
            super.engineSetCertificateEntry(str, certificate);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
            super.engineSetKeyEntry(str, key, cArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
            super.engineSetKeyEntry(str, bArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ int engineSize() {
            return super.engineSize();
        }

        public /* bridge */ /* synthetic */ void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            super.engineStore(outputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
            super.engineStore(loadStoreParameter);
        }
    }

    private static class ExtKeyStoreException extends KeyStoreException {
        private final Throwable cause;

        ExtKeyStoreException(String str, Throwable th) {
            super(str);
            this.cause = th;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

    private static class SharedKeyStoreSpi extends BcFKSKeyStoreSpi implements PKCSObjectIdentifiers, X509ObjectIdentifiers {
        private final Map<String, byte[]> cache;
        private final byte[] seedKey;

        public SharedKeyStoreSpi(BouncyCastleProvider bouncyCastleProvider) {
            super(bouncyCastleProvider);
            SecureRandom instance;
            byte[] bArr;
            try {
                this.seedKey = new byte[32];
                if (bouncyCastleProvider != null) {
                    instance = SecureRandom.getInstance("DEFAULT", bouncyCastleProvider);
                    bArr = this.seedKey;
                } else {
                    instance = SecureRandom.getInstance("DEFAULT");
                    bArr = this.seedKey;
                }
                instance.nextBytes(bArr);
                this.cache = new HashMap();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("can't create cert factory - " + e.toString());
            }
        }

        private byte[] calculateMac(String str, char[] cArr) throws NoSuchAlgorithmException, InvalidKeyException {
            return SCrypt.generate(Arrays.concatenate(cArr != null ? Strings.toUTF8ByteArray(cArr) : this.seedKey, Strings.toUTF8ByteArray(str)), this.seedKey, 16384, 8, 1, 32);
        }

        public void engineDeleteEntry(String str) throws KeyStoreException {
            this.cache.remove(str);
            BcFKSKeyStoreSpi.super.engineDeleteEntry(str);
        }

        public Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            try {
                byte[] calculateMac = calculateMac(str, cArr);
                if (!this.cache.containsKey(str) || Arrays.constantTimeAreEqual(this.cache.get(str), calculateMac)) {
                    Key engineGetKey = BcFKSKeyStoreSpi.super.engineGetKey(str, cArr);
                    if (engineGetKey != null && !this.cache.containsKey(str)) {
                        this.cache.put(str, calculateMac);
                    }
                    return engineGetKey;
                }
                throw new UnrecoverableKeyException("unable to recover key (" + str + ")");
            } catch (InvalidKeyException e) {
                throw new UnrecoverableKeyException("unable to recover key (" + str + "): " + e.getMessage());
            }
        }
    }

    public static class Std extends BcFKSKeyStoreSpi {
        public Std() {
            super(new BouncyCastleProvider());
        }

        public /* bridge */ /* synthetic */ Enumeration engineAliases() {
            return BcFKSKeyStoreSpi.super.engineAliases();
        }

        public /* bridge */ /* synthetic */ boolean engineContainsAlias(String str) {
            return BcFKSKeyStoreSpi.super.engineContainsAlias(str);
        }

        public /* bridge */ /* synthetic */ void engineDeleteEntry(String str) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineDeleteEntry(str);
        }

        public /* bridge */ /* synthetic */ Certificate engineGetCertificate(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCertificate(str);
        }

        public /* bridge */ /* synthetic */ String engineGetCertificateAlias(Certificate certificate) {
            return BcFKSKeyStoreSpi.super.engineGetCertificateAlias(certificate);
        }

        public /* bridge */ /* synthetic */ Certificate[] engineGetCertificateChain(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCertificateChain(str);
        }

        public /* bridge */ /* synthetic */ Date engineGetCreationDate(String str) {
            return BcFKSKeyStoreSpi.super.engineGetCreationDate(str);
        }

        public /* bridge */ /* synthetic */ Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            return BcFKSKeyStoreSpi.super.engineGetKey(str, cArr);
        }

        public /* bridge */ /* synthetic */ boolean engineIsCertificateEntry(String str) {
            return BcFKSKeyStoreSpi.super.engineIsCertificateEntry(str);
        }

        public /* bridge */ /* synthetic */ boolean engineIsKeyEntry(String str) {
            return BcFKSKeyStoreSpi.super.engineIsKeyEntry(str);
        }

        public /* bridge */ /* synthetic */ void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            BcFKSKeyStoreSpi.super.engineLoad(inputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetCertificateEntry(str, certificate);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetKeyEntry(str, key, cArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
            BcFKSKeyStoreSpi.super.engineSetKeyEntry(str, bArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ int engineSize() {
            return BcFKSKeyStoreSpi.super.engineSize();
        }

        public /* bridge */ /* synthetic */ void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            BcFKSKeyStoreSpi.super.engineStore(outputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
            BcFKSKeyStoreSpi.super.engineStore(loadStoreParameter);
        }
    }

    public static class StdShared extends SharedKeyStoreSpi {
        public StdShared() {
            super(new BouncyCastleProvider());
        }

        public /* bridge */ /* synthetic */ Enumeration engineAliases() {
            return super.engineAliases();
        }

        public /* bridge */ /* synthetic */ boolean engineContainsAlias(String str) {
            return super.engineContainsAlias(str);
        }

        public /* bridge */ /* synthetic */ void engineDeleteEntry(String str) throws KeyStoreException {
            super.engineDeleteEntry(str);
        }

        public /* bridge */ /* synthetic */ Certificate engineGetCertificate(String str) {
            return super.engineGetCertificate(str);
        }

        public /* bridge */ /* synthetic */ String engineGetCertificateAlias(Certificate certificate) {
            return super.engineGetCertificateAlias(certificate);
        }

        public /* bridge */ /* synthetic */ Certificate[] engineGetCertificateChain(String str) {
            return super.engineGetCertificateChain(str);
        }

        public /* bridge */ /* synthetic */ Date engineGetCreationDate(String str) {
            return super.engineGetCreationDate(str);
        }

        public /* bridge */ /* synthetic */ Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            return super.engineGetKey(str, cArr);
        }

        public /* bridge */ /* synthetic */ boolean engineIsCertificateEntry(String str) {
            return super.engineIsCertificateEntry(str);
        }

        public /* bridge */ /* synthetic */ boolean engineIsKeyEntry(String str) {
            return super.engineIsKeyEntry(str);
        }

        public /* bridge */ /* synthetic */ void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            super.engineLoad(inputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
            super.engineSetCertificateEntry(str, certificate);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
            super.engineSetKeyEntry(str, key, cArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
            super.engineSetKeyEntry(str, bArr, certificateArr);
        }

        public /* bridge */ /* synthetic */ int engineSize() {
            return super.engineSize();
        }

        public /* bridge */ /* synthetic */ void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
            super.engineStore(outputStream, cArr);
        }

        public /* bridge */ /* synthetic */ void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
            super.engineStore(loadStoreParameter);
        }
    }

    static {
        oidMap.put("DESEDE", OIWObjectIdentifiers.desEDE);
        oidMap.put("TRIPLEDES", OIWObjectIdentifiers.desEDE);
        oidMap.put("TDEA", OIWObjectIdentifiers.desEDE);
        oidMap.put("HMACSHA1", PKCSObjectIdentifiers.id_hmacWithSHA1);
        oidMap.put("HMACSHA224", PKCSObjectIdentifiers.id_hmacWithSHA224);
        oidMap.put("HMACSHA256", PKCSObjectIdentifiers.id_hmacWithSHA256);
        oidMap.put("HMACSHA384", PKCSObjectIdentifiers.id_hmacWithSHA384);
        oidMap.put("HMACSHA512", PKCSObjectIdentifiers.id_hmacWithSHA512);
        oidMap.put("SEED", KISAObjectIdentifiers.id_seedCBC);
        oidMap.put("CAMELLIA.128", NTTObjectIdentifiers.id_camellia128_cbc);
        oidMap.put("CAMELLIA.192", NTTObjectIdentifiers.id_camellia192_cbc);
        oidMap.put("CAMELLIA.256", NTTObjectIdentifiers.id_camellia256_cbc);
        oidMap.put("ARIA.128", NSRIObjectIdentifiers.id_aria128_cbc);
        oidMap.put("ARIA.192", NSRIObjectIdentifiers.id_aria192_cbc);
        oidMap.put("ARIA.256", NSRIObjectIdentifiers.id_aria256_cbc);
        publicAlgMap.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        publicAlgMap.put(X9ObjectIdentifiers.id_ecPublicKey, "EC");
        publicAlgMap.put(OIWObjectIdentifiers.elGamalAlgorithm, "DH");
        publicAlgMap.put(PKCSObjectIdentifiers.dhKeyAgreement, "DH");
        publicAlgMap.put(X9ObjectIdentifiers.id_dsa, "DSA");
    }

    BcFKSKeyStoreSpi(BouncyCastleProvider bouncyCastleProvider) {
        this.provider = bouncyCastleProvider;
    }

    private byte[] calculateMac(byte[] bArr, AlgorithmIdentifier algorithmIdentifier, KeyDerivationFunc keyDerivationFunc, char[] cArr) throws NoSuchAlgorithmException, IOException {
        String id = algorithmIdentifier.getAlgorithm().getId();
        Mac instance = this.provider != null ? Mac.getInstance(id, this.provider) : Mac.getInstance(id);
        try {
            if (cArr == null) {
                cArr = new char[0];
            }
            instance.init(new SecretKeySpec(generateKey(keyDerivationFunc, "INTEGRITY_CHECK", cArr, -1), id));
            return instance.doFinal(bArr);
        } catch (InvalidKeyException e) {
            throw new IOException("Cannot set up MAC calculation: " + e.getMessage());
        }
    }

    private Cipher createCipher(String str, byte[] bArr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher instance = this.provider == null ? Cipher.getInstance(str) : Cipher.getInstance(str, this.provider);
        instance.init(1, new SecretKeySpec(bArr, "AES"));
        return instance;
    }

    private EncryptedPrivateKeyData createPrivateKeySequence(EncryptedPrivateKeyInfo encryptedPrivateKeyInfo, Certificate[] certificateArr) throws CertificateEncodingException {
        org.bouncycastle.asn1.x509.Certificate[] certificateArr2 = new org.bouncycastle.asn1.x509.Certificate[certificateArr.length];
        for (int i = 0; i != certificateArr.length; i++) {
            certificateArr2[i] = org.bouncycastle.asn1.x509.Certificate.getInstance(certificateArr[i].getEncoded());
        }
        return new EncryptedPrivateKeyData(encryptedPrivateKeyInfo, certificateArr2);
    }

    private Certificate decodeCertificate(Object obj) {
        if (this.provider != null) {
            try {
                return CertificateFactory.getInstance("X.509", this.provider).generateCertificate(new ByteArrayInputStream(org.bouncycastle.asn1.x509.Certificate.getInstance(obj).getEncoded()));
            } catch (Exception e) {
                return null;
            }
        } else {
            try {
                return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(org.bouncycastle.asn1.x509.Certificate.getInstance(obj).getEncoded()));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private byte[] decryptData(String str, AlgorithmIdentifier algorithmIdentifier, char[] cArr, byte[] bArr) throws IOException {
        Cipher cipher;
        if (algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBES2)) {
            PBES2Parameters instance = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
            EncryptionScheme encryptionScheme = instance.getEncryptionScheme();
            try {
                AlgorithmParameters algorithmParameters = null;
                if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                    if (this.provider == null) {
                        cipher = Cipher.getInstance("AES/CCM/NoPadding");
                        algorithmParameters = AlgorithmParameters.getInstance("CCM");
                    } else {
                        cipher = Cipher.getInstance("AES/CCM/NoPadding", this.provider);
                        algorithmParameters = AlgorithmParameters.getInstance("CCM", this.provider);
                    }
                    algorithmParameters.init(CCMParameters.getInstance(encryptionScheme.getParameters()).getEncoded());
                } else if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_wrap_pad)) {
                    cipher = this.provider == null ? Cipher.getInstance("AESKWP") : Cipher.getInstance("AESKWP", this.provider);
                } else {
                    throw new IOException("BCFKS KeyStore cannot recognize protection encryption algorithm.");
                }
                KeyDerivationFunc keyDerivationFunc = instance.getKeyDerivationFunc();
                if (cArr == null) {
                    cArr = new char[0];
                }
                cipher.init(2, new SecretKeySpec(generateKey(keyDerivationFunc, str, cArr, 32), "AES"), algorithmParameters);
                return cipher.doFinal(bArr);
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new IOException(e2.toString());
            }
        } else {
            throw new IOException("BCFKS KeyStore cannot recognize protection algorithm.");
        }
    }

    private Date extractCreationDate(ObjectData objectData, Date date) {
        try {
            return objectData.getCreationDate().getDate();
        } catch (ParseException e) {
            return date;
        }
    }

    private byte[] generateKey(KeyDerivationFunc keyDerivationFunc, String str, char[] cArr, int i) throws IOException {
        byte[] PKCS12PasswordToBytes = PBEParametersGenerator.PKCS12PasswordToBytes(cArr);
        byte[] PKCS12PasswordToBytes2 = PBEParametersGenerator.PKCS12PasswordToBytes(str.toCharArray());
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams instance = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            if (instance.getKeyLength() != null) {
                i = instance.getKeyLength().intValue();
            } else if (i == -1) {
                throw new IOException("no keyLength found in ScryptParams");
            }
            return SCrypt.generate(Arrays.concatenate(PKCS12PasswordToBytes, PKCS12PasswordToBytes2), instance.getSalt(), instance.getCostParameter().intValue(), instance.getBlockSize().intValue(), instance.getBlockSize().intValue(), i);
        } else if (keyDerivationFunc.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBKDF2)) {
            PBKDF2Params instance2 = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
            if (instance2.getKeyLength() != null) {
                i = instance2.getKeyLength().intValue();
            } else if (i == -1) {
                throw new IOException("no keyLength found in PBKDF2Params");
            }
            if (instance2.getPrf().getAlgorithm().equals(PKCSObjectIdentifiers.id_hmacWithSHA512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA512Digest());
                pKCS5S2ParametersGenerator.init(Arrays.concatenate(PKCS12PasswordToBytes, PKCS12PasswordToBytes2), instance2.getSalt(), instance2.getIterationCount().intValue());
                return ((KeyParameter) pKCS5S2ParametersGenerator.generateDerivedParameters(i * 8)).getKey();
            } else if (instance2.getPrf().getAlgorithm().equals(NISTObjectIdentifiers.id_hmacWithSHA3_512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator2 = new PKCS5S2ParametersGenerator(new SHA3Digest(512));
                pKCS5S2ParametersGenerator2.init(Arrays.concatenate(PKCS12PasswordToBytes, PKCS12PasswordToBytes2), instance2.getSalt(), instance2.getIterationCount().intValue());
                return ((KeyParameter) pKCS5S2ParametersGenerator2.generateDerivedParameters(i * 8)).getKey();
            } else {
                throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD PRF: " + instance2.getPrf().getAlgorithm());
            }
        } else {
            throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD.");
        }
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i) {
        byte[] bArr = new byte[64];
        getDefaultSecureRandom().nextBytes(bArr);
        if (PKCSObjectIdentifiers.id_PBKDF2.equals(aSN1ObjectIdentifier)) {
            return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(bArr, 51200, i, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE)));
        }
        throw new IllegalStateException("unknown derivation algorithm: " + aSN1ObjectIdentifier);
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(KeyDerivationFunc keyDerivationFunc, int i) {
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams instance = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            byte[] bArr = new byte[instance.getSalt().length];
            getDefaultSecureRandom().nextBytes(bArr);
            ScryptParams scryptParams = new ScryptParams(bArr, instance.getCostParameter(), instance.getBlockSize(), instance.getParallelizationParameter(), BigInteger.valueOf((long) i));
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams);
        }
        PBKDF2Params instance2 = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
        byte[] bArr2 = new byte[instance2.getSalt().length];
        getDefaultSecureRandom().nextBytes(bArr2);
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(bArr2, instance2.getIterationCount().intValue(), i, instance2.getPrf()));
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(PBKDFConfig pBKDFConfig, int i) {
        if (MiscObjectIdentifiers.id_scrypt.equals(pBKDFConfig.getAlgorithm())) {
            ScryptConfig scryptConfig = (ScryptConfig) pBKDFConfig;
            byte[] bArr = new byte[scryptConfig.getSaltLength()];
            getDefaultSecureRandom().nextBytes(bArr);
            ScryptParams scryptParams = new ScryptParams(bArr, scryptConfig.getCostParameter(), scryptConfig.getBlockSize(), scryptConfig.getParallelizationParameter(), i);
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams);
        }
        PBKDF2Config pBKDF2Config = (PBKDF2Config) pBKDFConfig;
        byte[] bArr2 = new byte[pBKDF2Config.getSaltLength()];
        getDefaultSecureRandom().nextBytes(bArr2);
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(bArr2, pBKDF2Config.getIterationCount(), i, pBKDF2Config.getPRF()));
    }

    private SecureRandom getDefaultSecureRandom() {
        return CryptoServicesRegistrar.getSecureRandom();
    }

    private static String getPublicKeyAlg(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String str = publicAlgMap.get(aSN1ObjectIdentifier);
        return str != null ? str : aSN1ObjectIdentifier.getId();
    }

    private void verifyMac(byte[] bArr, PbkdMacIntegrityCheck pbkdMacIntegrityCheck, char[] cArr) throws NoSuchAlgorithmException, IOException {
        if (!Arrays.constantTimeAreEqual(calculateMac(bArr, pbkdMacIntegrityCheck.getMacAlgorithm(), pbkdMacIntegrityCheck.getPbkdAlgorithm(), cArr), pbkdMacIntegrityCheck.getMac())) {
            throw new IOException("BCFKS KeyStore corrupted: MAC calculation failed.");
        }
    }

    public Enumeration<String> engineAliases() {
        final Iterator it = new HashSet(this.entries.keySet()).iterator();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public Object nextElement() {
                return it.next();
            }
        };
    }

    public boolean engineContainsAlias(String str) {
        if (str != null) {
            return this.entries.containsKey(str);
        }
        throw new NullPointerException("alias value is null");
    }

    public void engineDeleteEntry(String str) throws KeyStoreException {
        if (this.entries.get(str) != null) {
            this.privateKeyCache.remove(str);
            this.entries.remove(str);
            this.lastModifiedDate = new Date();
        }
    }

    public Certificate engineGetCertificate(String str) {
        Object obj;
        ObjectData objectData = this.entries.get(str);
        if (objectData != null) {
            if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                obj = EncryptedPrivateKeyData.getInstance(objectData.getData()).getCertificateChain()[0];
            } else if (objectData.getType().equals(CERTIFICATE)) {
                obj = objectData.getData();
            }
            return decodeCertificate(obj);
        }
        return null;
    }

    public String engineGetCertificateAlias(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        try {
            byte[] encoded = certificate.getEncoded();
            for (String next : this.entries.keySet()) {
                ObjectData objectData = this.entries.get(next);
                if (objectData.getType().equals(CERTIFICATE)) {
                    if (Arrays.areEqual(objectData.getData(), encoded)) {
                        return next;
                    }
                } else if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                    try {
                        if (Arrays.areEqual(EncryptedPrivateKeyData.getInstance(objectData.getData()).getCertificateChain()[0].toASN1Primitive().getEncoded(), encoded)) {
                            return next;
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        } catch (CertificateEncodingException e2) {
            return null;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: java.security.cert.X509Certificate[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public Certificate[] engineGetCertificateChain(String str) {
        ObjectData objectData = this.entries.get(str);
        if (objectData == null || (!objectData.getType().equals(PRIVATE_KEY) && !objectData.getType().equals(PROTECTED_PRIVATE_KEY))) {
            return null;
        }
        org.bouncycastle.asn1.x509.Certificate[] certificateChain = EncryptedPrivateKeyData.getInstance(objectData.getData()).getCertificateChain();
        X509Certificate[] x509CertificateArr = new X509Certificate[certificateChain.length];
        for (int i = 0; i != x509CertificateArr.length; i++) {
            x509CertificateArr[i] = decodeCertificate(certificateChain[i]);
        }
        return x509CertificateArr;
    }

    public Date engineGetCreationDate(String str) {
        ObjectData objectData = this.entries.get(str);
        if (objectData == null) {
            return null;
        }
        try {
            return objectData.getLastModifiedDate().getDate();
        } catch (ParseException e) {
            return new Date();
        }
    }

    public Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        ObjectData objectData = this.entries.get(str);
        if (objectData == null) {
            return null;
        }
        if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
            PrivateKey privateKey = this.privateKeyCache.get(str);
            if (privateKey != null) {
                return privateKey;
            }
            EncryptedPrivateKeyInfo instance = EncryptedPrivateKeyInfo.getInstance(EncryptedPrivateKeyData.getInstance(objectData.getData()).getEncryptedPrivateKeyInfo());
            try {
                PrivateKeyInfo instance2 = PrivateKeyInfo.getInstance(decryptData("PRIVATE_KEY_ENCRYPTION", instance.getEncryptionAlgorithm(), cArr, instance.getEncryptedData()));
                PrivateKey generatePrivate = (this.provider != null ? KeyFactory.getInstance(instance2.getPrivateKeyAlgorithm().getAlgorithm().getId(), this.provider) : KeyFactory.getInstance(getPublicKeyAlg(instance2.getPrivateKeyAlgorithm().getAlgorithm()))).generatePrivate(new PKCS8EncodedKeySpec(instance2.getEncoded()));
                this.privateKeyCache.put(str, generatePrivate);
                return generatePrivate;
            } catch (Exception e) {
                throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover private key (" + str + "): " + e.getMessage());
            }
        } else if (objectData.getType().equals(SECRET_KEY) || objectData.getType().equals(PROTECTED_SECRET_KEY)) {
            EncryptedSecretKeyData instance3 = EncryptedSecretKeyData.getInstance(objectData.getData());
            try {
                SecretKeyData instance4 = SecretKeyData.getInstance(decryptData("SECRET_KEY_ENCRYPTION", instance3.getKeyEncryptionAlgorithm(), cArr, instance3.getEncryptedKeyData()));
                return (this.provider != null ? SecretKeyFactory.getInstance(instance4.getKeyAlgorithm().getId(), this.provider) : SecretKeyFactory.getInstance(instance4.getKeyAlgorithm().getId())).generateSecret(new SecretKeySpec(instance4.getKeyBytes(), instance4.getKeyAlgorithm().getId()));
            } catch (Exception e2) {
                throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + str + "): " + e2.getMessage());
            }
        } else {
            throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + str + "): type not recognized");
        }
    }

    public boolean engineIsCertificateEntry(String str) {
        ObjectData objectData = this.entries.get(str);
        if (objectData != null) {
            return objectData.getType().equals(CERTIFICATE);
        }
        return false;
    }

    public boolean engineIsKeyEntry(String str) {
        ObjectData objectData = this.entries.get(str);
        if (objectData == null) {
            return false;
        }
        BigInteger type = objectData.getType();
        return type.equals(PRIVATE_KEY) || type.equals(SECRET_KEY) || type.equals(PROTECTED_PRIVATE_KEY) || type.equals(PROTECTED_SECRET_KEY);
    }

    public void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.entries.clear();
        this.privateKeyCache.clear();
        this.creationDate = null;
        this.lastModifiedDate = null;
        this.hmacAlgorithm = null;
        if (inputStream == null) {
            Date date = new Date();
            this.creationDate = date;
            this.lastModifiedDate = date;
            this.hmacAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE);
            this.hmacPkbdAlgorithm = generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 64);
            return;
        }
        try {
            ObjectStore instance = ObjectStore.getInstance(new ASN1InputStream(inputStream).readObject());
            ObjectStoreIntegrityCheck integrityCheck = instance.getIntegrityCheck();
            if (integrityCheck.getType() == 0) {
                PbkdMacIntegrityCheck instance2 = PbkdMacIntegrityCheck.getInstance(integrityCheck.getIntegrityCheck());
                this.hmacAlgorithm = instance2.getMacAlgorithm();
                this.hmacPkbdAlgorithm = instance2.getPbkdAlgorithm();
                verifyMac(instance.getStoreData().toASN1Primitive().getEncoded(), instance2, cArr);
                Object storeData = instance.getStoreData();
                if (storeData instanceof EncryptedObjectStoreData) {
                    EncryptedObjectStoreData encryptedObjectStoreData = (EncryptedObjectStoreData) storeData;
                    storeData = decryptData("STORE_ENCRYPTION", encryptedObjectStoreData.getEncryptionAlgorithm(), cArr, encryptedObjectStoreData.getEncryptedContent().getOctets());
                }
                ObjectStoreData instance3 = ObjectStoreData.getInstance(storeData);
                try {
                    this.creationDate = instance3.getCreationDate().getDate();
                    this.lastModifiedDate = instance3.getLastModifiedDate().getDate();
                    if (instance3.getIntegrityAlgorithm().equals(this.hmacAlgorithm)) {
                        Iterator<ASN1Encodable> it = instance3.getObjectDataSequence().iterator();
                        while (it.hasNext()) {
                            ObjectData instance4 = ObjectData.getInstance(it.next());
                            this.entries.put(instance4.getIdentifier(), instance4);
                        }
                        return;
                    }
                    throw new IOException("BCFKS KeyStore storeData integrity algorithm does not match store integrity algorithm.");
                } catch (ParseException e) {
                    throw new IOException("BCFKS KeyStore unable to parse store data information.");
                }
            } else {
                throw new IOException("BCFKS KeyStore unable to recognize integrity check.");
            }
        } catch (Exception e2) {
            throw new IOException(e2.getMessage());
        }
    }

    public void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
        Date date;
        ObjectData objectData = this.entries.get(str);
        Date date2 = new Date();
        if (objectData == null) {
            date = date2;
        } else if (objectData.getType().equals(CERTIFICATE)) {
            date = extractCreationDate(objectData, date2);
        } else {
            throw new KeyStoreException("BCFKS KeyStore already has a key entry with alias " + str);
        }
        try {
            Map<String, ObjectData> map = this.entries;
            ObjectData objectData2 = new ObjectData(CERTIFICATE, str, date, date2, certificate.getEncoded(), null);
            map.put(str, objectData2);
            this.lastModifiedDate = date2;
        } catch (CertificateEncodingException e) {
            throw new ExtKeyStoreException("BCFKS KeyStore unable to handle certificate: " + e.getMessage(), e);
        }
    }

    public void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
        SecretKeyData secretKeyData;
        Date date = new Date();
        ObjectData objectData = this.entries.get(str);
        Date extractCreationDate = objectData != null ? extractCreationDate(objectData, date) : date;
        this.privateKeyCache.remove(str);
        if (key instanceof PrivateKey) {
            if (certificateArr != null) {
                try {
                    byte[] encoded = key.getEncoded();
                    KeyDerivationFunc generatePkbdAlgorithmIdentifier = generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                    if (cArr == null) {
                        cArr = new char[0];
                    }
                    Cipher createCipher = createCipher("AES/CCM/NoPadding", generateKey(generatePkbdAlgorithmIdentifier, "PRIVATE_KEY_ENCRYPTION", cArr, 32));
                    byte[] doFinal = createCipher.doFinal(encoded);
                    AlgorithmParameters parameters = createCipher.getParameters();
                    EncryptedPrivateKeyData createPrivateKeySequence = createPrivateKeySequence(new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, parameters != null ? new PBES2Parameters(generatePkbdAlgorithmIdentifier, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(parameters.getEncoded()))) : new PBES2Parameters(generatePkbdAlgorithmIdentifier, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad, null))), doFinal), certificateArr);
                    Map<String, ObjectData> map = this.entries;
                    ObjectData objectData2 = new ObjectData(PRIVATE_KEY, str, extractCreationDate, date, createPrivateKeySequence.getEncoded(), null);
                    map.put(str, objectData2);
                } catch (Exception e) {
                    throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + e.toString(), e);
                }
            } else {
                throw new KeyStoreException("BCFKS KeyStore requires a certificate chain for private key storage.");
            }
        } else if (!(key instanceof SecretKey)) {
            throw new KeyStoreException("BCFKS KeyStore unable to recognize key.");
        } else if (certificateArr == null) {
            try {
                byte[] encoded2 = key.getEncoded();
                KeyDerivationFunc generatePkbdAlgorithmIdentifier2 = generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                if (cArr == null) {
                    cArr = new char[0];
                }
                Cipher createCipher2 = createCipher("AES/CCM/NoPadding", generateKey(generatePkbdAlgorithmIdentifier2, "SECRET_KEY_ENCRYPTION", cArr, 32));
                String upperCase = Strings.toUpperCase(key.getAlgorithm());
                if (upperCase.indexOf("AES") > -1) {
                    secretKeyData = new SecretKeyData(NISTObjectIdentifiers.aes, encoded2);
                } else {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier = oidMap.get(upperCase);
                    if (aSN1ObjectIdentifier != null) {
                        secretKeyData = new SecretKeyData(aSN1ObjectIdentifier, encoded2);
                    } else {
                        Map<String, ASN1ObjectIdentifier> map2 = oidMap;
                        ASN1ObjectIdentifier aSN1ObjectIdentifier2 = map2.get(upperCase + "." + (encoded2.length * 8));
                        if (aSN1ObjectIdentifier2 != null) {
                            secretKeyData = new SecretKeyData(aSN1ObjectIdentifier2, encoded2);
                        } else {
                            throw new KeyStoreException("BCFKS KeyStore cannot recognize secret key (" + upperCase + ") for storage.");
                        }
                    }
                }
                byte[] doFinal2 = createCipher2.doFinal(secretKeyData.getEncoded());
                AlgorithmParameters parameters2 = createCipher2.getParameters();
                EncryptedSecretKeyData encryptedSecretKeyData = new EncryptedSecretKeyData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, parameters2 != null ? new PBES2Parameters(generatePkbdAlgorithmIdentifier2, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(parameters2.getEncoded()))) : new PBES2Parameters(generatePkbdAlgorithmIdentifier2, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad, null))), doFinal2);
                Map<String, ObjectData> map3 = this.entries;
                ObjectData objectData3 = new ObjectData(SECRET_KEY, str, extractCreationDate, date, encryptedSecretKeyData.getEncoded(), null);
                map3.put(str, objectData3);
            } catch (Exception e2) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + e2.toString(), e2);
            }
        } else {
            throw new KeyStoreException("BCFKS KeyStore cannot store certificate chain with secret key.");
        }
        this.lastModifiedDate = date;
    }

    public void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
        Date date = new Date();
        ObjectData objectData = this.entries.get(str);
        Date extractCreationDate = objectData != null ? extractCreationDate(objectData, date) : date;
        if (certificateArr != null) {
            try {
                EncryptedPrivateKeyInfo instance = EncryptedPrivateKeyInfo.getInstance(bArr);
                try {
                    this.privateKeyCache.remove(str);
                    Map<String, ObjectData> map = this.entries;
                    ObjectData objectData2 = new ObjectData(PROTECTED_PRIVATE_KEY, str, extractCreationDate, date, createPrivateKeySequence(instance, certificateArr).getEncoded(), null);
                    map.put(str, objectData2);
                } catch (Exception e) {
                    throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + e.toString(), e);
                }
            } catch (Exception e2) {
                throw new ExtKeyStoreException("BCFKS KeyStore private key encoding must be an EncryptedPrivateKeyInfo.", e2);
            }
        } else {
            try {
                Map<String, ObjectData> map2 = this.entries;
                ObjectData objectData3 = new ObjectData(PROTECTED_SECRET_KEY, str, extractCreationDate, date, bArr, null);
                map2.put(str, objectData3);
            } catch (Exception e3) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + e3.toString(), e3);
            }
        }
        this.lastModifiedDate = date;
    }

    public int engineSize() {
        return this.entries.size();
    }

    public void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
        KeyDerivationFunc keyDerivationFunc;
        BigInteger keyLength;
        ObjectData[] objectDataArr = (ObjectData[]) this.entries.values().toArray(new ObjectData[this.entries.size()]);
        KeyDerivationFunc generatePkbdAlgorithmIdentifier = generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, 32);
        byte[] generateKey = generateKey(generatePkbdAlgorithmIdentifier, "STORE_ENCRYPTION", cArr != null ? cArr : new char[0], 32);
        ObjectStoreData objectStoreData = new ObjectStoreData(this.hmacAlgorithm, this.creationDate, this.lastModifiedDate, new ObjectDataSequence(objectDataArr), null);
        try {
            Cipher instance = this.provider == null ? Cipher.getInstance("AES/CCM/NoPadding") : Cipher.getInstance("AES/CCM/NoPadding", this.provider);
            instance.init(1, new SecretKeySpec(generateKey, "AES"));
            EncryptedObjectStoreData encryptedObjectStoreData = new EncryptedObjectStoreData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, new PBES2Parameters(generatePkbdAlgorithmIdentifier, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(instance.getParameters().getEncoded())))), instance.doFinal(objectStoreData.getEncoded()));
            if (MiscObjectIdentifiers.id_scrypt.equals(this.hmacPkbdAlgorithm.getAlgorithm())) {
                ScryptParams instance2 = ScryptParams.getInstance(this.hmacPkbdAlgorithm.getParameters());
                keyDerivationFunc = this.hmacPkbdAlgorithm;
                keyLength = instance2.getKeyLength();
            } else {
                PBKDF2Params instance3 = PBKDF2Params.getInstance(this.hmacPkbdAlgorithm.getParameters());
                keyDerivationFunc = this.hmacPkbdAlgorithm;
                keyLength = instance3.getKeyLength();
            }
            this.hmacPkbdAlgorithm = generatePkbdAlgorithmIdentifier(keyDerivationFunc, keyLength.intValue());
            outputStream.write(new ObjectStore(encryptedObjectStoreData, new ObjectStoreIntegrityCheck(new PbkdMacIntegrityCheck(this.hmacAlgorithm, this.hmacPkbdAlgorithm, calculateMac(encryptedObjectStoreData.getEncoded(), this.hmacAlgorithm, this.hmacPkbdAlgorithm, cArr)))).getEncoded());
            outputStream.flush();
        } catch (NoSuchPaddingException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (BadPaddingException e2) {
            throw new IOException(e2.toString());
        } catch (IllegalBlockSizeException e3) {
            throw new IOException(e3.toString());
        } catch (InvalidKeyException e4) {
            throw new IOException(e4.toString());
        }
    }

    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
        char[] cArr;
        if (loadStoreParameter == null) {
            throw new IllegalArgumentException("'parameter' arg cannot be null");
        } else if (loadStoreParameter instanceof BCFKSStoreParameter) {
            BCFKSStoreParameter bCFKSStoreParameter = (BCFKSStoreParameter) loadStoreParameter;
            KeyStore.ProtectionParameter protectionParameter = bCFKSStoreParameter.getProtectionParameter();
            if (protectionParameter == null) {
                cArr = null;
            } else if (protectionParameter instanceof KeyStore.PasswordProtection) {
                cArr = ((KeyStore.PasswordProtection) protectionParameter).getPassword();
            } else if (protectionParameter instanceof KeyStore.CallbackHandlerProtection) {
                CallbackHandler callbackHandler = ((KeyStore.CallbackHandlerProtection) protectionParameter).getCallbackHandler();
                PasswordCallback passwordCallback = new PasswordCallback("password: ", false);
                try {
                    callbackHandler.handle(new Callback[]{passwordCallback});
                    cArr = passwordCallback.getPassword();
                } catch (UnsupportedCallbackException e) {
                    throw new IllegalArgumentException("PasswordCallback not recognised: " + e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException("no support for protection parameter of type " + protectionParameter.getClass().getName());
            }
            boolean equals = bCFKSStoreParameter.getStorePBKDFConfig().getAlgorithm().equals(MiscObjectIdentifiers.id_scrypt);
            this.hmacPkbdAlgorithm = generatePkbdAlgorithmIdentifier(bCFKSStoreParameter.getStorePBKDFConfig(), 64);
            engineStore(bCFKSStoreParameter.getOutputStream(), cArr);
        } else {
            throw new IllegalArgumentException("no support for 'parameter' of type " + loadStoreParameter.getClass().getName());
        }
    }
}
