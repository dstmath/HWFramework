package android.security.keystore;

import android.security.Credentials;
import android.security.GateKeeper;
import android.security.KeyStore;
import android.security.KeyStoreParameter;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import libcore.util.EmptyArray;

public class AndroidKeyStoreSpi extends KeyStoreSpi {
    public static final String NAME = "AndroidKeyStore";
    private KeyStore mKeyStore;
    private int mUid = -1;

    static class KeyStoreX509Certificate extends DelegatingX509Certificate {
        private final String mPrivateKeyAlias;
        private final int mPrivateKeyUid;

        KeyStoreX509Certificate(String privateKeyAlias, int privateKeyUid, X509Certificate delegate) {
            super(delegate);
            this.mPrivateKeyAlias = privateKeyAlias;
            this.mPrivateKeyUid = privateKeyUid;
        }

        public PublicKey getPublicKey() {
            PublicKey original = super.getPublicKey();
            return AndroidKeyStoreProvider.getAndroidKeyStorePublicKey(this.mPrivateKeyAlias, this.mPrivateKeyUid, original.getAlgorithm(), original.getEncoded());
        }
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        String userKeyAlias = Credentials.USER_PRIVATE_KEY + alias;
        if (!this.mKeyStore.contains(userKeyAlias, this.mUid)) {
            userKeyAlias = Credentials.USER_SECRET_KEY + alias;
            if (!this.mKeyStore.contains(userKeyAlias, this.mUid)) {
                return null;
            }
        }
        return AndroidKeyStoreProvider.loadAndroidKeyStoreKeyFromKeystore(this.mKeyStore, userKeyAlias, this.mUid);
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] caList;
        if (alias != null) {
            X509Certificate leaf = (X509Certificate) engineGetCertificate(alias);
            if (leaf == null) {
                return null;
            }
            KeyStore keyStore = this.mKeyStore;
            byte[] caBytes = keyStore.get(Credentials.CA_CERTIFICATE + alias, this.mUid);
            int i = 1;
            if (caBytes != null) {
                Collection<X509Certificate> caChain = toCertificates(caBytes);
                caList = new Certificate[(caChain.size() + 1)];
                for (X509Certificate x509Certificate : caChain) {
                    caList[i] = x509Certificate;
                    i++;
                }
            } else {
                caList = new Certificate[1];
            }
            Certificate[] caList2 = caList;
            caList2[0] = leaf;
            return caList2;
        }
        throw new NullPointerException("alias == null");
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias != null) {
            KeyStore keyStore = this.mKeyStore;
            byte[] encodedCert = keyStore.get(Credentials.USER_CERTIFICATE + alias, this.mUid);
            if (encodedCert != null) {
                return getCertificateForPrivateKeyEntry(alias, encodedCert);
            }
            KeyStore keyStore2 = this.mKeyStore;
            byte[] encodedCert2 = keyStore2.get(Credentials.CA_CERTIFICATE + alias, this.mUid);
            if (encodedCert2 != null) {
                return getCertificateForTrustedCertificateEntry(encodedCert2);
            }
            return null;
        }
        throw new NullPointerException("alias == null");
    }

    private Certificate getCertificateForTrustedCertificateEntry(byte[] encodedCert) {
        return toCertificate(encodedCert);
    }

    private Certificate getCertificateForPrivateKeyEntry(String alias, byte[] encodedCert) {
        X509Certificate cert = toCertificate(encodedCert);
        if (cert == null) {
            return null;
        }
        String privateKeyAlias = Credentials.USER_PRIVATE_KEY + alias;
        if (this.mKeyStore.contains(privateKeyAlias, this.mUid)) {
            return wrapIntoKeyStoreCertificate(privateKeyAlias, this.mUid, cert);
        }
        return cert;
    }

    private static KeyStoreX509Certificate wrapIntoKeyStoreCertificate(String privateKeyAlias, int uid, X509Certificate certificate) {
        if (certificate != null) {
            return new KeyStoreX509Certificate(privateKeyAlias, uid, certificate);
        }
        return null;
    }

    private static X509Certificate toCertificate(byte[] bytes) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w("AndroidKeyStore", "Couldn't parse certificate in keystore", e);
            return null;
        }
    }

    private static Collection<X509Certificate> toCertificates(byte[] bytes) {
        try {
            return CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w("AndroidKeyStore", "Couldn't parse certificates in keystore", e);
            return new ArrayList();
        }
    }

    private Date getModificationDate(String alias) {
        long epochMillis = this.mKeyStore.getmtime(alias, this.mUid);
        if (epochMillis == -1) {
            return null;
        }
        return new Date(epochMillis);
    }

    public Date engineGetCreationDate(String alias) {
        if (alias != null) {
            Date d = getModificationDate(Credentials.USER_PRIVATE_KEY + alias);
            if (d != null) {
                return d;
            }
            Date d2 = getModificationDate(Credentials.USER_SECRET_KEY + alias);
            if (d2 != null) {
                return d2;
            }
            Date d3 = getModificationDate(Credentials.USER_CERTIFICATE + alias);
            if (d3 != null) {
                return d3;
            }
            return getModificationDate(Credentials.CA_CERTIFICATE + alias);
        }
        throw new NullPointerException("alias == null");
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (password != null && password.length > 0) {
            throw new KeyStoreException("entries cannot be protected with passwords");
        } else if (key instanceof PrivateKey) {
            setPrivateKeyEntry(alias, (PrivateKey) key, chain, null);
        } else if (key instanceof SecretKey) {
            setSecretKeyEntry(alias, (SecretKey) key, null);
        } else {
            throw new KeyStoreException("Only PrivateKey and SecretKey are supported");
        }
    }

    private static KeyProtection getLegacyKeyProtectionParameter(PrivateKey key) throws KeyStoreException {
        KeyProtection.Builder specBuilder;
        String keyAlgorithm = key.getAlgorithm();
        if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
            specBuilder = new KeyProtection.Builder(12);
            specBuilder.setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512);
        } else if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
            specBuilder = new KeyProtection.Builder(15);
            specBuilder.setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_MD5, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512);
            specBuilder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE, KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, KeyProperties.ENCRYPTION_PADDING_RSA_OAEP);
            specBuilder.setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1, KeyProperties.SIGNATURE_PADDING_RSA_PSS);
            specBuilder.setRandomizedEncryptionRequired(false);
        } else {
            throw new KeyStoreException("Unsupported key algorithm: " + keyAlgorithm);
        }
        specBuilder.setUserAuthenticationRequired(false);
        return specBuilder.build();
    }

    /* JADX WARNING: Removed duplicated region for block: B:156:0x0345  */
    private void setPrivateKeyEntry(String alias, PrivateKey key, Certificate[] chain, KeyStore.ProtectionParameter param) throws KeyStoreException {
        KeyProtection spec;
        byte[] chainBytes;
        String pkeyAlias;
        boolean shouldReplacePrivateKey;
        byte[] pkcs8EncodedPrivateKeyBytes;
        KeymasterArguments importArgs;
        byte[] userCertBytes;
        byte[] chainBytes2;
        int i;
        String str = alias;
        PrivateKey privateKey = key;
        Certificate[] certificateArr = chain;
        KeyStore.ProtectionParameter protectionParameter = param;
        int flags = 0;
        if (protectionParameter == null) {
            spec = getLegacyKeyProtectionParameter(key);
        } else if (protectionParameter instanceof KeyStoreParameter) {
            spec = getLegacyKeyProtectionParameter(key);
            if (((KeyStoreParameter) protectionParameter).isEncryptionRequired()) {
                flags = 1;
            }
        } else if (protectionParameter instanceof KeyProtection) {
            spec = (KeyProtection) protectionParameter;
            if (spec.isCriticalToDeviceEncryption()) {
                flags = 0 | 8;
            }
            if (spec.isStrongBoxBacked()) {
                flags |= 16;
            }
        } else {
            throw new KeyStoreException("Unsupported protection parameter class:" + param.getClass().getName() + ". Supported: " + KeyProtection.class.getName() + ", " + KeyStoreParameter.class.getName());
        }
        int flags2 = flags;
        if (certificateArr == null || certificateArr.length == 0) {
            throw new KeyStoreException("Must supply at least one Certificate with PrivateKey");
        }
        X509Certificate[] x509chain = new X509Certificate[certificateArr.length];
        int i2 = 0;
        while (i2 < certificateArr.length) {
            if (!"X.509".equals(certificateArr[i2].getType())) {
                throw new KeyStoreException("Certificates must be in X.509 format: invalid cert #" + i2);
            } else if (certificateArr[i2] instanceof X509Certificate) {
                x509chain[i2] = (X509Certificate) certificateArr[i2];
                i2++;
            } else {
                throw new KeyStoreException("Certificates must be in X.509 format: invalid cert #" + i2);
            }
        }
        try {
            byte[] chainBytes3 = x509chain[0].getEncoded();
            if (certificateArr.length > 1) {
                byte[][] certsBytes = new byte[(x509chain.length - 1)][];
                int totalCertLength = 0;
                int i3 = 0;
                while (i3 < certsBytes.length) {
                    try {
                        certsBytes[i3] = x509chain[i3 + 1].getEncoded();
                        totalCertLength += certsBytes[i3].length;
                        i3++;
                    } catch (CertificateEncodingException e) {
                        throw new KeyStoreException("Failed to encode certificate #" + i3, e);
                    }
                }
                chainBytes = new byte[totalCertLength];
                int outputOffset = 0;
                int i4 = 0;
                while (i4 < certsBytes.length) {
                    int certLength = certsBytes[i4].length;
                    System.arraycopy(certsBytes[i4], 0, chainBytes, outputOffset, certLength);
                    outputOffset += certLength;
                    certsBytes[i4] = null;
                    i4++;
                    Certificate[] certificateArr2 = chain;
                }
                pkeyAlias = null;
            } else {
                pkeyAlias = null;
                chainBytes = null;
            }
            byte[] chainBytes4 = chainBytes;
            if (privateKey instanceof AndroidKeyStorePrivateKey) {
                pkeyAlias = ((AndroidKeyStoreKey) privateKey).getAlias();
            }
            if (pkeyAlias == null || !pkeyAlias.startsWith(Credentials.USER_PRIVATE_KEY)) {
                String keyFormat = key.getFormat();
                if (keyFormat == null || !"PKCS#8".equals(keyFormat)) {
                    X509Certificate[] x509CertificateArr = x509chain;
                    byte[] bArr = chainBytes3;
                    byte[] userCertBytes2 = chainBytes4;
                    byte[] chainBytes5 = bArr;
                    throw new KeyStoreException("Unsupported private key export format: " + keyFormat + ". Only private keys which export their key material in PKCS#8 format are supported.");
                }
                byte[] pkcs8EncodedPrivateKeyBytes2 = key.getEncoded();
                if (pkcs8EncodedPrivateKeyBytes2 != null) {
                    KeymasterArguments importArgs2 = new KeymasterArguments();
                    try {
                        importArgs2.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, KeyProperties.KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(key.getAlgorithm()));
                        int purposes = spec.getPurposes();
                        importArgs2.addEnums(KeymasterDefs.KM_TAG_PURPOSE, KeyProperties.Purpose.allToKeymaster(purposes));
                        if (spec.isDigestsSpecified()) {
                            try {
                                importArgs2.addEnums(KeymasterDefs.KM_TAG_DIGEST, KeyProperties.Digest.allToKeymaster(spec.getDigests()));
                            } catch (IllegalArgumentException | IllegalStateException e2) {
                                e = e2;
                                String str2 = pkeyAlias;
                            }
                        }
                        importArgs2.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, KeyProperties.BlockMode.allToKeymaster(spec.getBlockModes()));
                        int[] keymasterEncryptionPaddings = KeyProperties.EncryptionPadding.allToKeymaster(spec.getEncryptionPaddings());
                        if ((purposes & 1) != 0) {
                            try {
                                if (spec.isRandomizedEncryptionRequired()) {
                                    int length = keymasterEncryptionPaddings.length;
                                    int i5 = purposes;
                                    int purposes2 = 0;
                                    while (purposes2 < length) {
                                        String pkeyAlias2 = pkeyAlias;
                                        int keymasterPadding = keymasterEncryptionPaddings[purposes2];
                                        try {
                                            if (KeymasterUtils.isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(keymasterPadding)) {
                                                purposes2++;
                                                pkeyAlias = pkeyAlias2;
                                            } else {
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    sb.append("Randomized encryption (IND-CPA) required but is violated by encryption padding mode: ");
                                                    sb.append(KeyProperties.EncryptionPadding.fromKeymaster(keymasterPadding));
                                                    sb.append(". See KeyProtection documentation.");
                                                    throw new KeyStoreException(sb.toString());
                                                } catch (IllegalArgumentException | IllegalStateException e3) {
                                                    e = e3;
                                                    byte[] bArr2 = chainBytes3;
                                                    byte[] userCertBytes3 = bArr2;
                                                    throw new KeyStoreException(e);
                                                }
                                            }
                                        } catch (IllegalArgumentException | IllegalStateException e4) {
                                            e = e4;
                                            X509Certificate[] x509CertificateArr2 = x509chain;
                                            byte[] bArr3 = chainBytes3;
                                            byte[] userCertBytes4 = chainBytes4;
                                            byte[] chainBytes6 = bArr3;
                                            throw new KeyStoreException(e);
                                        }
                                    }
                                    shouldReplacePrivateKey = true;
                                    importArgs2.addEnums(KeymasterDefs.KM_TAG_PADDING, keymasterEncryptionPaddings);
                                    importArgs2.addEnums(KeymasterDefs.KM_TAG_PADDING, KeyProperties.SignaturePadding.allToKeymaster(spec.getSignaturePaddings()));
                                    KeymasterUtils.addUserAuthArgs(importArgs2, spec);
                                    importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, spec.getKeyValidityStart());
                                    importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, spec.getKeyValidityForOriginationEnd());
                                    importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, spec.getKeyValidityForConsumptionEnd());
                                    pkcs8EncodedPrivateKeyBytes = pkcs8EncodedPrivateKeyBytes2;
                                    importArgs = importArgs2;
                                }
                            } catch (IllegalArgumentException | IllegalStateException e5) {
                                e = e5;
                                String str3 = pkeyAlias;
                                X509Certificate[] x509CertificateArr3 = x509chain;
                                byte[] bArr4 = chainBytes3;
                                byte[] userCertBytes5 = chainBytes4;
                                byte[] chainBytes7 = bArr4;
                                throw new KeyStoreException(e);
                            }
                        }
                        int i6 = purposes;
                        String str4 = pkeyAlias;
                        shouldReplacePrivateKey = true;
                        try {
                            importArgs2.addEnums(KeymasterDefs.KM_TAG_PADDING, keymasterEncryptionPaddings);
                            importArgs2.addEnums(KeymasterDefs.KM_TAG_PADDING, KeyProperties.SignaturePadding.allToKeymaster(spec.getSignaturePaddings()));
                            KeymasterUtils.addUserAuthArgs(importArgs2, spec);
                            importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, spec.getKeyValidityStart());
                            importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, spec.getKeyValidityForOriginationEnd());
                            importArgs2.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, spec.getKeyValidityForConsumptionEnd());
                            pkcs8EncodedPrivateKeyBytes = pkcs8EncodedPrivateKeyBytes2;
                            importArgs = importArgs2;
                        } catch (IllegalArgumentException | IllegalStateException e6) {
                            e = e6;
                            X509Certificate[] x509CertificateArr4 = x509chain;
                            byte[] bArr5 = chainBytes3;
                            byte[] userCertBytes6 = chainBytes4;
                            byte[] chainBytes8 = bArr5;
                            throw new KeyStoreException(e);
                        }
                    } catch (IllegalArgumentException | IllegalStateException e7) {
                        e = e7;
                        String str5 = pkeyAlias;
                        X509Certificate[] x509CertificateArr5 = x509chain;
                        byte[] bArr6 = chainBytes3;
                        byte[] userCertBytes7 = chainBytes4;
                        byte[] chainBytes9 = bArr6;
                        throw new KeyStoreException(e);
                    }
                } else {
                    X509Certificate[] x509CertificateArr6 = x509chain;
                    byte[] bArr7 = chainBytes3;
                    byte[] userCertBytes8 = chainBytes4;
                    byte[] chainBytes10 = bArr7;
                    throw new KeyStoreException("Private key did not export any key material");
                }
            } else {
                if (str.equals(pkeyAlias.substring(Credentials.USER_PRIVATE_KEY.length()))) {
                    String str6 = pkeyAlias;
                    pkcs8EncodedPrivateKeyBytes = null;
                    shouldReplacePrivateKey = false;
                    importArgs = null;
                } else {
                    throw new KeyStoreException("Can only replace keys with same alias: " + str + " != " + keySubalias);
                }
            }
            if (shouldReplacePrivateKey) {
                try {
                    Credentials.deleteAllTypesForAlias(this.mKeyStore, str, this.mUid);
                    KeyCharacteristics resultingKeyCharacteristics = new KeyCharacteristics();
                    chainBytes2 = chainBytes4;
                    userCertBytes = chainBytes3;
                    X509Certificate[] x509CertificateArr7 = x509chain;
                    try {
                        int errorCode = this.mKeyStore.importKey(Credentials.USER_PRIVATE_KEY + str, importArgs, 1, pkcs8EncodedPrivateKeyBytes, this.mUid, flags2, resultingKeyCharacteristics);
                        i = 1;
                        if (errorCode != 1) {
                            throw new KeyStoreException("Failed to store private key", android.security.KeyStore.getKeyStoreException(errorCode));
                        }
                    } catch (Throwable th) {
                        th = th;
                        byte[] bArr8 = chainBytes2;
                        byte[] bArr9 = userCertBytes;
                        if (0 == 0) {
                            if (shouldReplacePrivateKey) {
                                Credentials.deleteAllTypesForAlias(this.mKeyStore, str, this.mUid);
                            } else {
                                Credentials.deleteCertificateTypesForAlias(this.mKeyStore, str, this.mUid);
                                Credentials.deleteLegacyKeyForAlias(this.mKeyStore, str, this.mUid);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    X509Certificate[] x509CertificateArr8 = x509chain;
                    byte[] bArr10 = chainBytes3;
                    byte[] userCertBytes9 = chainBytes4;
                    byte[] chainBytes11 = bArr10;
                    if (0 == 0) {
                    }
                    throw th;
                }
            } else {
                chainBytes2 = chainBytes4;
                userCertBytes = chainBytes3;
                X509Certificate[] x509CertificateArr9 = x509chain;
                i = 1;
                try {
                    Credentials.deleteCertificateTypesForAlias(this.mKeyStore, str, this.mUid);
                    Credentials.deleteLegacyKeyForAlias(this.mKeyStore, str, this.mUid);
                } catch (Throwable th3) {
                    th = th3;
                    byte[] bArr11 = chainBytes2;
                    byte[] bArr12 = userCertBytes;
                    if (0 == 0) {
                    }
                    throw th;
                }
            }
            try {
                int errorCode2 = this.mKeyStore.insert(Credentials.USER_CERTIFICATE + str, userCertBytes, this.mUid, flags2);
                if (errorCode2 == i) {
                    try {
                        int errorCode3 = this.mKeyStore.insert(Credentials.CA_CERTIFICATE + str, chainBytes2, this.mUid, flags2);
                        if (errorCode3 != i) {
                            throw new KeyStoreException("Failed to store certificate chain", android.security.KeyStore.getKeyStoreException(errorCode3));
                        } else if (1 != 0) {
                        } else {
                            if (shouldReplacePrivateKey) {
                                Credentials.deleteAllTypesForAlias(this.mKeyStore, str, this.mUid);
                                return;
                            }
                            Credentials.deleteCertificateTypesForAlias(this.mKeyStore, str, this.mUid);
                            Credentials.deleteLegacyKeyForAlias(this.mKeyStore, str, this.mUid);
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (0 == 0) {
                        }
                        throw th;
                    }
                } else {
                    throw new KeyStoreException("Failed to store certificate #0", android.security.KeyStore.getKeyStoreException(errorCode2));
                }
            } catch (Throwable th5) {
                th = th5;
                byte[] bArr13 = chainBytes2;
                if (0 == 0) {
                }
                throw th;
            }
        } catch (CertificateEncodingException e8) {
            X509Certificate[] x509CertificateArr10 = x509chain;
            throw new KeyStoreException("Failed to encode certificate #0", e8);
        }
    }

    private void setSecretKeyEntry(String entryAlias, SecretKey key, KeyStore.ProtectionParameter param) throws KeyStoreException {
        int[] keymasterDigests;
        String str = entryAlias;
        SecretKey secretKey = key;
        KeyStore.ProtectionParameter protectionParameter = param;
        if (protectionParameter == null || (protectionParameter instanceof KeyProtection)) {
            KeyProtection params = (KeyProtection) protectionParameter;
            if (secretKey instanceof AndroidKeyStoreSecretKey) {
                String keyAliasInKeystore = ((AndroidKeyStoreSecretKey) secretKey).getAlias();
                if (keyAliasInKeystore != null) {
                    String keyAliasPrefix = Credentials.USER_PRIVATE_KEY;
                    if (!keyAliasInKeystore.startsWith(keyAliasPrefix)) {
                        keyAliasPrefix = Credentials.USER_SECRET_KEY;
                        if (!keyAliasInKeystore.startsWith(keyAliasPrefix)) {
                            throw new KeyStoreException("KeyStore-backed secret key has invalid alias: " + keyAliasInKeystore);
                        }
                    }
                    if (!str.equals(keyAliasInKeystore.substring(keyAliasPrefix.length()))) {
                        throw new KeyStoreException("Can only replace KeyStore-backed keys with same alias: " + str + " != " + keyEntryAlias);
                    } else if (params != null) {
                        throw new KeyStoreException("Modifying KeyStore-backed key using protection parameters not supported");
                    }
                } else {
                    throw new KeyStoreException("KeyStore-backed secret key does not have an alias");
                }
            } else if (params != null) {
                String keyExportFormat = key.getFormat();
                if (keyExportFormat == null) {
                    throw new KeyStoreException("Only secret keys that export their key material are supported");
                } else if ("RAW".equals(keyExportFormat)) {
                    byte[] keyMaterial = key.getEncoded();
                    if (keyMaterial != null) {
                        KeymasterArguments args = new KeymasterArguments();
                        try {
                            int keymasterAlgorithm = KeyProperties.KeyAlgorithm.toKeymasterSecretKeyAlgorithm(key.getAlgorithm());
                            args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, keymasterAlgorithm);
                            int i = 0;
                            if (keymasterAlgorithm == 128) {
                                try {
                                    int keymasterImpliedDigest = KeyProperties.KeyAlgorithm.toKeymasterDigest(key.getAlgorithm());
                                    if (keymasterImpliedDigest != -1) {
                                        keymasterDigests = new int[]{keymasterImpliedDigest};
                                        if (params.isDigestsSpecified()) {
                                            int[] keymasterDigestsFromParams = KeyProperties.Digest.allToKeymaster(params.getDigests());
                                            if (keymasterDigestsFromParams.length != 1 || keymasterDigestsFromParams[0] != keymasterImpliedDigest) {
                                                throw new KeyStoreException("Unsupported digests specification: " + Arrays.asList(params.getDigests()) + ". Only " + KeyProperties.Digest.fromKeymaster(keymasterImpliedDigest) + " supported for HMAC key algorithm " + key.getAlgorithm());
                                            }
                                        }
                                    } else {
                                        throw new ProviderException("HMAC key algorithm digest unknown for key algorithm " + key.getAlgorithm());
                                    }
                                } catch (IllegalArgumentException | IllegalStateException e) {
                                    e = e;
                                    KeymasterArguments keymasterArguments = args;
                                    throw new KeyStoreException(e);
                                }
                            } else if (params.isDigestsSpecified()) {
                                keymasterDigests = KeyProperties.Digest.allToKeymaster(params.getDigests());
                            } else {
                                keymasterDigests = EmptyArray.INT;
                            }
                            int[] keymasterDigests2 = keymasterDigests;
                            args.addEnums(KeymasterDefs.KM_TAG_DIGEST, keymasterDigests2);
                            int purposes = params.getPurposes();
                            int[] keymasterBlockModes = KeyProperties.BlockMode.allToKeymaster(params.getBlockModes());
                            if ((purposes & 1) != 0) {
                                if (params.isRandomizedEncryptionRequired()) {
                                    int length = keymasterBlockModes.length;
                                    while (i < length) {
                                        if (KeymasterUtils.isKeymasterBlockModeIndCpaCompatibleWithSymmetricCrypto(keymasterBlockModes[i])) {
                                            i++;
                                        } else {
                                            throw new KeyStoreException("Randomized encryption (IND-CPA) required but may be violated by block mode: " + KeyProperties.BlockMode.fromKeymaster(keymasterBlockMode) + ". See KeyProtection documentation.");
                                        }
                                    }
                                }
                            }
                            args.addEnums(KeymasterDefs.KM_TAG_PURPOSE, KeyProperties.Purpose.allToKeymaster(purposes));
                            if (params.isRollbackResistant()) {
                                args.addEnum(KeymasterDefs.KM_TAG_PURPOSE, KeymasterDefs.KM_PURPOSE_ROLLBACK_RESISTANT);
                            }
                            args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, keymasterBlockModes);
                            if (params.getSignaturePaddings().length <= 0) {
                                args.addEnums(KeymasterDefs.KM_TAG_PADDING, KeyProperties.EncryptionPadding.allToKeymaster(params.getEncryptionPaddings()));
                                KeymasterUtils.addUserAuthArgs(args, params);
                                KeymasterUtils.addMinMacLengthAuthorizationIfNecessary(args, keymasterAlgorithm, keymasterBlockModes, keymasterDigests2);
                                args.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, params.getKeyValidityStart());
                                args.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, params.getKeyValidityForOriginationEnd());
                                args.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, params.getKeyValidityForConsumptionEnd());
                                if ((purposes & 1) != 0) {
                                    if (!params.isRandomizedEncryptionRequired()) {
                                        args.addBoolean(KeymasterDefs.KM_TAG_CALLER_NONCE);
                                    }
                                }
                                int flags = 0;
                                if (params.isCriticalToDeviceEncryption()) {
                                    flags = 0 | 8;
                                }
                                if (params.isStrongBoxBacked()) {
                                    flags |= 16;
                                }
                                Credentials.deleteAllTypesForAlias(this.mKeyStore, str, this.mUid);
                                KeymasterArguments keymasterArguments2 = args;
                                if (this.mKeyStore.importKey(Credentials.USER_PRIVATE_KEY + str, args, 3, keyMaterial, this.mUid, flags, new KeyCharacteristics()) != 1) {
                                    throw new KeyStoreException("Failed to import secret key. Keystore error code: " + errorCode);
                                }
                                return;
                            }
                            try {
                                throw new KeyStoreException("Signature paddings not supported for symmetric keys");
                            } catch (IllegalArgumentException | IllegalStateException e2) {
                                e = e2;
                                throw new KeyStoreException(e);
                            }
                        } catch (IllegalArgumentException | IllegalStateException e3) {
                            e = e3;
                            KeymasterArguments keymasterArguments3 = args;
                            throw new KeyStoreException(e);
                        }
                    } else {
                        throw new KeyStoreException("Key did not export its key material despite supporting RAW format export");
                    }
                } else {
                    throw new KeyStoreException("Unsupported secret key material export format: " + keyExportFormat);
                }
            } else {
                throw new KeyStoreException("Protection parameters must be specified when importing a symmetric key");
            }
        } else {
            throw new KeyStoreException("Unsupported protection parameter class: " + param.getClass().getName() + ". Supported: " + KeyProtection.class.getName());
        }
    }

    private void setWrappedKeyEntry(String alias, WrappedKeyEntry entry, KeyStore.ProtectionParameter param) throws KeyStoreException {
        if (param == null) {
            byte[] maskingKey = new byte[32];
            KeymasterArguments args = new KeymasterArguments();
            String[] parts = entry.getTransformation().split("/");
            String algorithm = parts[0];
            if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(algorithm)) {
                args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 1);
            } else if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(algorithm)) {
                args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 1);
            }
            if (parts.length > 1) {
                String mode = parts[1];
                if (KeyProperties.BLOCK_MODE_ECB.equalsIgnoreCase(mode)) {
                    args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, 1);
                } else if (KeyProperties.BLOCK_MODE_CBC.equalsIgnoreCase(mode)) {
                    args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, 2);
                } else if (KeyProperties.BLOCK_MODE_CTR.equalsIgnoreCase(mode)) {
                    args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, 3);
                } else if (KeyProperties.BLOCK_MODE_GCM.equalsIgnoreCase(mode)) {
                    args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, 32);
                }
            }
            if (parts.length > 2) {
                String padding = parts[2];
                if (!KeyProperties.ENCRYPTION_PADDING_NONE.equalsIgnoreCase(padding)) {
                    if (KeyProperties.ENCRYPTION_PADDING_PKCS7.equalsIgnoreCase(padding)) {
                        args.addEnums(KeymasterDefs.KM_TAG_PADDING, 64);
                    } else if (KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1.equalsIgnoreCase(padding)) {
                        args.addEnums(KeymasterDefs.KM_TAG_PADDING, 4);
                    } else if (KeyProperties.ENCRYPTION_PADDING_RSA_OAEP.equalsIgnoreCase(padding)) {
                        args.addEnums(KeymasterDefs.KM_TAG_PADDING, 2);
                    }
                }
            }
            KeyGenParameterSpec spec = (KeyGenParameterSpec) entry.getAlgorithmParameterSpec();
            if (spec.isDigestsSpecified()) {
                String digest = spec.getDigests()[0];
                if (!KeyProperties.DIGEST_NONE.equalsIgnoreCase(digest)) {
                    if (KeyProperties.DIGEST_MD5.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 1);
                    } else if (KeyProperties.DIGEST_SHA1.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 2);
                    } else if (KeyProperties.DIGEST_SHA224.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 3);
                    } else if (KeyProperties.DIGEST_SHA256.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 4);
                    } else if (KeyProperties.DIGEST_SHA384.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 5);
                    } else if (KeyProperties.DIGEST_SHA512.equalsIgnoreCase(digest)) {
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, 6);
                    }
                }
            }
            long secureUserId = GateKeeper.getSecureUserId();
            String str = algorithm;
            String[] strArr = parts;
            KeymasterArguments keymasterArguments = args;
            int errorCode = this.mKeyStore.importWrappedKey(Credentials.USER_SECRET_KEY + alias, entry.getWrappedKeyBytes(), Credentials.USER_PRIVATE_KEY + entry.getWrappingKeyAlias(), maskingKey, args, secureUserId, 0, this.mUid, new KeyCharacteristics());
            if (errorCode == -100) {
                throw new SecureKeyImportUnavailableException("Could not import wrapped key");
            } else if (errorCode != 1) {
                throw new KeyStoreException("Failed to import wrapped key. Keystore error code: " + errorCode);
            }
        } else {
            throw new KeyStoreException("Protection parameters are specified inside wrapped keys");
        }
    }

    public void engineSetKeyEntry(String alias, byte[] userKey, Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException("Operation not supported because key encoding is unknown");
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (isKeyEntry(alias)) {
            throw new KeyStoreException("Entry exists and is not a trusted certificate");
        } else if (cert != null) {
            try {
                byte[] encoded = cert.getEncoded();
                android.security.KeyStore keyStore = this.mKeyStore;
                if (!keyStore.put(Credentials.CA_CERTIFICATE + alias, encoded, this.mUid, 0)) {
                    throw new KeyStoreException("Couldn't insert certificate; is KeyStore initialized?");
                }
            } catch (CertificateEncodingException e) {
                throw new KeyStoreException(e);
            }
        } else {
            throw new NullPointerException("cert == null");
        }
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (!Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid)) {
            throw new KeyStoreException("Failed to delete entry: " + alias);
        }
    }

    private Set<String> getUniqueAliases() {
        String[] rawAliases = this.mKeyStore.list("", this.mUid);
        if (rawAliases == null) {
            return new HashSet();
        }
        Set<String> aliases = new HashSet<>(rawAliases.length);
        for (String alias : rawAliases) {
            int idx = alias.indexOf(95);
            if (idx == -1 || alias.length() <= idx) {
                Log.e("AndroidKeyStore", "invalid alias: " + alias);
            } else {
                aliases.add(new String(alias.substring(idx + 1)));
            }
        }
        return aliases;
    }

    public Enumeration<String> engineAliases() {
        return Collections.enumeration(getUniqueAliases());
    }

    public boolean engineContainsAlias(String alias) {
        if (alias != null) {
            android.security.KeyStore keyStore = this.mKeyStore;
            if (!keyStore.contains(Credentials.USER_PRIVATE_KEY + alias, this.mUid)) {
                android.security.KeyStore keyStore2 = this.mKeyStore;
                if (!keyStore2.contains(Credentials.USER_SECRET_KEY + alias, this.mUid)) {
                    android.security.KeyStore keyStore3 = this.mKeyStore;
                    if (!keyStore3.contains(Credentials.USER_CERTIFICATE + alias, this.mUid)) {
                        android.security.KeyStore keyStore4 = this.mKeyStore;
                        if (!keyStore4.contains(Credentials.CA_CERTIFICATE + alias, this.mUid)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        throw new NullPointerException("alias == null");
    }

    public int engineSize() {
        return getUniqueAliases().size();
    }

    public boolean engineIsKeyEntry(String alias) {
        return isKeyEntry(alias);
    }

    private boolean isKeyEntry(String alias) {
        android.security.KeyStore keyStore = this.mKeyStore;
        if (!keyStore.contains(Credentials.USER_PRIVATE_KEY + alias, this.mUid)) {
            android.security.KeyStore keyStore2 = this.mKeyStore;
            if (!keyStore2.contains(Credentials.USER_SECRET_KEY + alias, this.mUid)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCertificateEntry(String alias) {
        if (alias != null) {
            android.security.KeyStore keyStore = this.mKeyStore;
            return keyStore.contains(Credentials.CA_CERTIFICATE + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    public boolean engineIsCertificateEntry(String alias) {
        return !isKeyEntry(alias) && isCertificateEntry(alias);
    }

    public String engineGetCertificateAlias(Certificate cert) {
        if (cert == null || !"X.509".equalsIgnoreCase(cert.getType())) {
            return null;
        }
        try {
            byte[] targetCertBytes = cert.getEncoded();
            if (targetCertBytes == null) {
                return null;
            }
            Set<String> nonCaEntries = new HashSet<>();
            String[] certAliases = this.mKeyStore.list(Credentials.USER_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                for (String alias : certAliases) {
                    byte[] certBytes = this.mKeyStore.get(Credentials.USER_CERTIFICATE + alias, this.mUid);
                    if (certBytes != null) {
                        nonCaEntries.add(alias);
                        if (Arrays.equals(certBytes, targetCertBytes)) {
                            return alias;
                        }
                    }
                }
            }
            String[] caAliases = this.mKeyStore.list(Credentials.CA_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                for (String alias2 : caAliases) {
                    if (!nonCaEntries.contains(alias2)) {
                        byte[] certBytes2 = this.mKeyStore.get(Credentials.CA_CERTIFICATE + alias2, this.mUid);
                        if (certBytes2 != null && Arrays.equals(certBytes2, targetCertBytes)) {
                            return alias2;
                        }
                    }
                }
            }
            return null;
        } catch (CertificateEncodingException e) {
            return null;
        }
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException("Can not serialize AndroidKeyStore to OutputStream");
    }

    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (stream != null) {
            throw new IllegalArgumentException("InputStream not supported");
        } else if (password == null) {
            this.mKeyStore = android.security.KeyStore.getInstance();
            this.mUid = -1;
        } else {
            throw new IllegalArgumentException("password not supported");
        }
    }

    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        int uid = -1;
        if (param != null) {
            if (param instanceof AndroidKeyStoreLoadStoreParameter) {
                uid = ((AndroidKeyStoreLoadStoreParameter) param).getUid();
            } else {
                throw new IllegalArgumentException("Unsupported param type: " + param.getClass());
            }
        }
        this.mKeyStore = android.security.KeyStore.getInstance();
        this.mUid = uid;
    }

    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter param) throws KeyStoreException {
        if (entry != null) {
            Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
            if (entry instanceof KeyStore.TrustedCertificateEntry) {
                engineSetCertificateEntry(alias, ((KeyStore.TrustedCertificateEntry) entry).getTrustedCertificate());
                return;
            }
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                KeyStore.PrivateKeyEntry prE = (KeyStore.PrivateKeyEntry) entry;
                setPrivateKeyEntry(alias, prE.getPrivateKey(), prE.getCertificateChain(), param);
            } else if (entry instanceof KeyStore.SecretKeyEntry) {
                setSecretKeyEntry(alias, ((KeyStore.SecretKeyEntry) entry).getSecretKey(), param);
            } else if (entry instanceof WrappedKeyEntry) {
                setWrappedKeyEntry(alias, (WrappedKeyEntry) entry, param);
            } else {
                throw new KeyStoreException("Entry must be a PrivateKeyEntry, SecretKeyEntry or TrustedCertificateEntry; was " + entry);
            }
            return;
        }
        throw new KeyStoreException("entry == null");
    }
}
