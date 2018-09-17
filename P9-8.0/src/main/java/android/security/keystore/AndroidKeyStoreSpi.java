package android.security.keystore;

import android.security.Credentials;
import android.security.KeyStore;
import android.security.KeyStoreParameter;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.BlockMode;
import android.security.keystore.KeyProperties.Digest;
import android.security.keystore.KeyProperties.EncryptionPadding;
import android.security.keystore.KeyProperties.KeyAlgorithm;
import android.security.keystore.KeyProperties.Purpose;
import android.security.keystore.KeyProtection.Builder;
import android.util.Log;
import android.util.LogException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore.Entry;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
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
        if (isPrivateKeyEntry(alias)) {
            return AndroidKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(this.mKeyStore, Credentials.USER_PRIVATE_KEY + alias, this.mUid);
        } else if (!isSecretKeyEntry(alias)) {
            return null;
        } else {
            return AndroidKeyStoreProvider.loadAndroidKeyStoreSecretKeyFromKeystore(this.mKeyStore, Credentials.USER_SECRET_KEY + alias, this.mUid);
        }
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        X509Certificate leaf = (X509Certificate) engineGetCertificate(alias);
        if (leaf == null) {
            return null;
        }
        Certificate[] caList;
        byte[] caBytes = this.mKeyStore.get(Credentials.CA_CERTIFICATE + alias, this.mUid);
        if (caBytes != null) {
            Collection<X509Certificate> caChain = toCertificates(caBytes);
            caList = new Certificate[(caChain.size() + 1)];
            int i = 1;
            for (Certificate certificate : caChain) {
                int i2 = i + 1;
                caList[i] = certificate;
                i = i2;
            }
        } else {
            caList = new Certificate[1];
        }
        caList[0] = leaf;
        return caList;
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        byte[] encodedCert = this.mKeyStore.get(Credentials.USER_CERTIFICATE + alias, this.mUid);
        if (encodedCert != null) {
            return getCertificateForPrivateKeyEntry(alias, encodedCert);
        }
        encodedCert = this.mKeyStore.get(Credentials.CA_CERTIFICATE + alias, this.mUid);
        if (encodedCert != null) {
            return getCertificateForTrustedCertificateEntry(encodedCert);
        }
        return null;
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
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        Date d = getModificationDate(Credentials.USER_PRIVATE_KEY + alias);
        if (d != null) {
            return d;
        }
        d = getModificationDate(Credentials.USER_SECRET_KEY + alias);
        if (d != null) {
            return d;
        }
        d = getModificationDate(Credentials.USER_CERTIFICATE + alias);
        if (d != null) {
            return d;
        }
        return getModificationDate(Credentials.CA_CERTIFICATE + alias);
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
        Builder specBuilder;
        String keyAlgorithm = key.getAlgorithm();
        if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
            specBuilder = new Builder(12);
            specBuilder.setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512);
        } else if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
            specBuilder = new Builder(15);
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

    /* JADX WARNING: Removed duplicated region for block: B:112:0x0351 A:{SYNTHETIC, Splitter: B:112:0x0351} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01c7 A:{SYNTHETIC, Splitter: B:69:0x01c7} */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x03a1 A:{Catch:{ all -> 0x020f }} */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0394 A:{Catch:{ all -> 0x020f }} */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x02f8 A:{Splitter: B:90:0x0275, ExcHandler: java.lang.IllegalArgumentException (r14_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:104:0x02f8, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:106:0x02fe, code:
            throw new java.security.KeyStoreException(r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setPrivateKeyEntry(String alias, PrivateKey key, Certificate[] chain, ProtectionParameter param) throws KeyStoreException {
        KeyProtection spec;
        int flags = 0;
        if (param == null) {
            spec = getLegacyKeyProtectionParameter(key);
        } else if (param instanceof KeyStoreParameter) {
            spec = getLegacyKeyProtectionParameter(key);
            if (((KeyStoreParameter) param).isEncryptionRequired()) {
                flags = 1;
            }
        } else if (param instanceof KeyProtection) {
            spec = (KeyProtection) param;
            if (spec.isCriticalToDeviceEncryption()) {
                flags = 8;
            }
        } else {
            throw new KeyStoreException("Unsupported protection parameter class:" + param.getClass().getName() + ". Supported: " + KeyProtection.class.getName() + ", " + KeyStoreParameter.class.getName());
        }
        if (chain == null || chain.length == 0) {
            throw new KeyStoreException("Must supply at least one Certificate with PrivateKey");
        }
        X509Certificate[] x509chain = new X509Certificate[chain.length];
        int i = 0;
        while (i < chain.length) {
            if (!"X.509".equals(chain[i].getType())) {
                throw new KeyStoreException("Certificates must be in X.509 format: invalid cert #" + i);
            } else if (chain[i] instanceof X509Certificate) {
                x509chain[i] = (X509Certificate) chain[i];
                i++;
            } else {
                throw new KeyStoreException("Certificates must be in X.509 format: invalid cert #" + i);
            }
        }
        try {
            byte[] bArr;
            String pkeyAlias;
            boolean shouldReplacePrivateKey;
            KeymasterArguments importArgs;
            byte[] pkcs8EncodedPrivateKeyBytes;
            int errorCode;
            byte[] userCertBytes = x509chain[0].getEncoded();
            if (chain.length > 1) {
                byte[][] certsBytes = new byte[(x509chain.length - 1)][];
                int totalCertLength = 0;
                i = 0;
                while (i < certsBytes.length) {
                    try {
                        certsBytes[i] = x509chain[i + 1].getEncoded();
                        totalCertLength += certsBytes[i].length;
                        i++;
                    } catch (CertificateEncodingException e) {
                        throw new KeyStoreException("Failed to encode certificate #" + i, e);
                    }
                }
                bArr = new byte[totalCertLength];
                int outputOffset = 0;
                for (i = 0; i < certsBytes.length; i++) {
                    int certLength = certsBytes[i].length;
                    System.arraycopy(certsBytes[i], 0, bArr, outputOffset, certLength);
                    outputOffset += certLength;
                    certsBytes[i] = null;
                }
            } else {
                bArr = null;
            }
            if (key instanceof AndroidKeyStorePrivateKey) {
                pkeyAlias = ((AndroidKeyStoreKey) key).getAlias();
            } else {
                pkeyAlias = null;
            }
            if (pkeyAlias != null) {
                if (pkeyAlias.startsWith(Credentials.USER_PRIVATE_KEY)) {
                    String keySubalias = pkeyAlias.substring(Credentials.USER_PRIVATE_KEY.length());
                    if (alias.equals(keySubalias)) {
                        shouldReplacePrivateKey = false;
                        importArgs = null;
                        pkcs8EncodedPrivateKeyBytes = null;
                        if (shouldReplacePrivateKey) {
                            Credentials.deleteCertificateTypesForAlias(this.mKeyStore, alias, this.mUid);
                            Credentials.deleteSecretKeyTypeForAlias(this.mKeyStore, alias, this.mUid);
                        } else {
                            try {
                                Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
                                KeymasterArguments keymasterArguments = importArgs;
                                errorCode = this.mKeyStore.importKey(Credentials.USER_PRIVATE_KEY + alias, keymasterArguments, 1, pkcs8EncodedPrivateKeyBytes, this.mUid, flags, new KeyCharacteristics());
                                if (errorCode != 1) {
                                    throw new KeyStoreException("Failed to store private key", KeyStore.getKeyStoreException(errorCode));
                                }
                            } catch (Throwable th) {
                                if (!false) {
                                    if (shouldReplacePrivateKey) {
                                        Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
                                    } else {
                                        Credentials.deleteCertificateTypesForAlias(this.mKeyStore, alias, this.mUid);
                                        Credentials.deleteSecretKeyTypeForAlias(this.mKeyStore, alias, this.mUid);
                                    }
                                }
                            }
                        }
                        errorCode = this.mKeyStore.insert(Credentials.USER_CERTIFICATE + alias, userCertBytes, this.mUid, flags);
                        if (errorCode == 1) {
                            throw new KeyStoreException("Failed to store certificate #0", KeyStore.getKeyStoreException(errorCode));
                        }
                        errorCode = this.mKeyStore.insert(Credentials.CA_CERTIFICATE + alias, bArr, this.mUid, flags);
                        if (errorCode != 1) {
                            throw new KeyStoreException("Failed to store certificate chain", KeyStore.getKeyStoreException(errorCode));
                        } else if (!true) {
                            if (shouldReplacePrivateKey) {
                                Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
                                return;
                            }
                            Credentials.deleteCertificateTypesForAlias(this.mKeyStore, alias, this.mUid);
                            Credentials.deleteSecretKeyTypeForAlias(this.mKeyStore, alias, this.mUid);
                            return;
                        } else {
                            return;
                        }
                    }
                    throw new KeyStoreException("Can only replace keys with same alias: " + alias + " != " + keySubalias);
                }
            }
            shouldReplacePrivateKey = true;
            String keyFormat = key.getFormat();
            if (keyFormat == null || ("PKCS#8".equals(keyFormat) ^ 1) != 0) {
                throw new KeyStoreException("Unsupported private key export format: " + keyFormat + ". Only private keys which export their key material in PKCS#8 format are" + " supported.");
            }
            byte[] pkcs8EncodedPrivateKeyBytes2 = key.getEncoded();
            if (pkcs8EncodedPrivateKeyBytes2 == null) {
                throw new KeyStoreException("Private key did not export any key material");
            }
            importArgs = new KeymasterArguments();
            try {
                importArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(key.getAlgorithm()));
                int purposes = spec.getPurposes();
                importArgs.addEnums(KeymasterDefs.KM_TAG_PURPOSE, Purpose.allToKeymaster(purposes));
                if (spec.isDigestsSpecified()) {
                    importArgs.addEnums(KeymasterDefs.KM_TAG_DIGEST, Digest.allToKeymaster(spec.getDigests()));
                }
                importArgs.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, BlockMode.allToKeymaster(spec.getBlockModes()));
                int[] keymasterEncryptionPaddings = EncryptionPadding.allToKeymaster(spec.getEncryptionPaddings());
                if ((purposes & 1) != 0 && spec.isRandomizedEncryptionRequired()) {
                    int i2 = 0;
                    int length = keymasterEncryptionPaddings.length;
                    while (i2 < length) {
                        int keymasterPadding = keymasterEncryptionPaddings[i2];
                        if (KeymasterUtils.isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(keymasterPadding)) {
                            i2++;
                        } else {
                            throw new KeyStoreException("Randomized encryption (IND-CPA) required but is violated by encryption padding mode: " + EncryptionPadding.fromKeymaster(keymasterPadding) + ". See KeyProtection documentation.");
                        }
                    }
                }
                importArgs.addEnums(KeymasterDefs.KM_TAG_PADDING, keymasterEncryptionPaddings);
                importArgs.addEnums(KeymasterDefs.KM_TAG_PADDING, SignaturePadding.allToKeymaster(spec.getSignaturePaddings()));
                KeymasterUtils.addUserAuthArgs(importArgs, spec.isUserAuthenticationRequired(), spec.getUserAuthenticationValidityDurationSeconds(), spec.isUserAuthenticationValidWhileOnBody(), spec.isInvalidatedByBiometricEnrollment(), spec.getBoundToSpecificSecureUserId());
                importArgs.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, spec.getKeyValidityStart());
                importArgs.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, spec.getKeyValidityForOriginationEnd());
                importArgs.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, spec.getKeyValidityForConsumptionEnd());
                pkcs8EncodedPrivateKeyBytes = pkcs8EncodedPrivateKeyBytes2;
                if (shouldReplacePrivateKey) {
                }
                errorCode = this.mKeyStore.insert(Credentials.USER_CERTIFICATE + alias, userCertBytes, this.mUid, flags);
                if (errorCode == 1) {
                }
            } catch (RuntimeException e2) {
            }
        } catch (CertificateEncodingException e3) {
            throw new KeyStoreException("Failed to encode certificate #0", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0157 A:{Splitter: B:39:0x0116, ExcHandler: java.lang.IllegalArgumentException (r4_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:46:0x0157, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:48:0x015d, code:
            throw new java.security.KeyStoreException(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setSecretKeyEntry(String entryAlias, SecretKey key, ProtectionParameter param) throws KeyStoreException {
        if (param == null || ((param instanceof KeyProtection) ^ 1) == 0) {
            KeyProtection params = (KeyProtection) param;
            if (key instanceof AndroidKeyStoreSecretKey) {
                String keyAliasInKeystore = ((AndroidKeyStoreSecretKey) key).getAlias();
                if (keyAliasInKeystore == null) {
                    throw new KeyStoreException("KeyStore-backed secret key does not have an alias");
                } else if (keyAliasInKeystore.startsWith(Credentials.USER_SECRET_KEY)) {
                    String keyEntryAlias = keyAliasInKeystore.substring(Credentials.USER_SECRET_KEY.length());
                    if (!entryAlias.equals(keyEntryAlias)) {
                        throw new KeyStoreException("Can only replace KeyStore-backed keys with same alias: " + entryAlias + " != " + keyEntryAlias);
                    } else if (params != null) {
                        throw new KeyStoreException("Modifying KeyStore-backed key using protection parameters not supported");
                    } else {
                        return;
                    }
                } else {
                    throw new KeyStoreException("KeyStore-backed secret key has invalid alias: " + keyAliasInKeystore);
                }
            } else if (params == null) {
                throw new KeyStoreException("Protection parameters must be specified when importing a symmetric key");
            } else {
                String keyExportFormat = key.getFormat();
                if (keyExportFormat == null) {
                    throw new KeyStoreException("Only secret keys that export their key material are supported");
                } else if ("RAW".equals(keyExportFormat)) {
                    byte[] keyMaterial = key.getEncoded();
                    if (keyMaterial == null) {
                        throw new KeyStoreException("Key did not export its key material despite supporting RAW format export");
                    }
                    KeymasterArguments args = new KeymasterArguments();
                    try {
                        int[] keymasterDigests;
                        int keymasterAlgorithm = KeyAlgorithm.toKeymasterSecretKeyAlgorithm(key.getAlgorithm());
                        args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, keymasterAlgorithm);
                        if (keymasterAlgorithm == 128) {
                            int keymasterImpliedDigest = KeyAlgorithm.toKeymasterDigest(key.getAlgorithm());
                            if (keymasterImpliedDigest == -1) {
                                throw new ProviderException("HMAC key algorithm digest unknown for key algorithm " + key.getAlgorithm());
                            }
                            keymasterDigests = new int[]{keymasterImpliedDigest};
                            if (params.isDigestsSpecified()) {
                                int[] keymasterDigestsFromParams = Digest.allToKeymaster(params.getDigests());
                                if (!(keymasterDigestsFromParams.length == 1 && keymasterDigestsFromParams[0] == keymasterImpliedDigest)) {
                                    throw new KeyStoreException("Unsupported digests specification: " + Arrays.asList(params.getDigests()) + ". Only " + Digest.fromKeymaster(keymasterImpliedDigest) + " supported for HMAC key algorithm " + key.getAlgorithm());
                                }
                            }
                        } else if (params.isDigestsSpecified()) {
                            keymasterDigests = Digest.allToKeymaster(params.getDigests());
                        } else {
                            keymasterDigests = EmptyArray.INT;
                        }
                        args.addEnums(KeymasterDefs.KM_TAG_DIGEST, keymasterDigests);
                        int purposes = params.getPurposes();
                        int[] keymasterBlockModes = BlockMode.allToKeymaster(params.getBlockModes());
                        if ((purposes & 1) != 0 && params.isRandomizedEncryptionRequired()) {
                            int i = 0;
                            int length = keymasterBlockModes.length;
                            while (i < length) {
                                int keymasterBlockMode = keymasterBlockModes[i];
                                if (KeymasterUtils.isKeymasterBlockModeIndCpaCompatibleWithSymmetricCrypto(keymasterBlockMode)) {
                                    i++;
                                } else {
                                    throw new KeyStoreException("Randomized encryption (IND-CPA) required but may be violated by block mode: " + BlockMode.fromKeymaster(keymasterBlockMode) + ". See KeyProtection documentation.");
                                }
                            }
                        }
                        args.addEnums(KeymasterDefs.KM_TAG_PURPOSE, Purpose.allToKeymaster(purposes));
                        args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, keymasterBlockModes);
                        if (params.getSignaturePaddings().length > 0) {
                            throw new KeyStoreException("Signature paddings not supported for symmetric keys");
                        }
                        args.addEnums(KeymasterDefs.KM_TAG_PADDING, EncryptionPadding.allToKeymaster(params.getEncryptionPaddings()));
                        KeymasterUtils.addUserAuthArgs(args, params.isUserAuthenticationRequired(), params.getUserAuthenticationValidityDurationSeconds(), params.isUserAuthenticationValidWhileOnBody(), params.isInvalidatedByBiometricEnrollment(), params.getBoundToSpecificSecureUserId());
                        KeymasterUtils.addMinMacLengthAuthorizationIfNecessary(args, keymasterAlgorithm, keymasterBlockModes, keymasterDigests);
                        args.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, params.getKeyValidityStart());
                        args.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, params.getKeyValidityForOriginationEnd());
                        args.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, params.getKeyValidityForConsumptionEnd());
                        if (!((purposes & 1) == 0 || (params.isRandomizedEncryptionRequired() ^ 1) == 0)) {
                            args.addBoolean(KeymasterDefs.KM_TAG_CALLER_NONCE);
                        }
                        int flags = 0;
                        if (params.isCriticalToDeviceEncryption()) {
                            flags = 8;
                        }
                        Credentials.deleteAllTypesForAlias(this.mKeyStore, entryAlias, this.mUid);
                        int errorCode = this.mKeyStore.importKey(Credentials.USER_SECRET_KEY + entryAlias, args, 3, keyMaterial, this.mUid, flags, new KeyCharacteristics());
                        if (errorCode != 1) {
                            throw new KeyStoreException("Failed to import secret key. Keystore error code: " + errorCode);
                        }
                        return;
                    } catch (RuntimeException e) {
                    }
                } else {
                    throw new KeyStoreException("Unsupported secret key material export format: " + keyExportFormat);
                }
            }
        }
        throw new KeyStoreException("Unsupported protection parameter class: " + param.getClass().getName() + ". Supported: " + KeyProtection.class.getName());
    }

    public void engineSetKeyEntry(String alias, byte[] userKey, Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException("Operation not supported because key encoding is unknown");
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (isKeyEntry(alias)) {
            throw new KeyStoreException("Entry exists and is not a trusted certificate");
        } else if (cert == null) {
            throw new NullPointerException("cert == null");
        } else {
            try {
                if (!this.mKeyStore.put(Credentials.CA_CERTIFICATE + alias, cert.getEncoded(), this.mUid, 0)) {
                    throw new KeyStoreException("Couldn't insert certificate; is KeyStore initialized?");
                }
            } catch (CertificateEncodingException e) {
                throw new KeyStoreException(e);
            }
        }
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (!Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid)) {
            throw new KeyStoreException("Failed to delete entry: " + alias);
        }
    }

    private Set<String> getUniqueAliases() {
        String[] rawAliases = this.mKeyStore.list(LogException.NO_VALUE, this.mUid);
        if (rawAliases == null) {
            return new HashSet();
        }
        Set<String> aliases = new HashSet(rawAliases.length);
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
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (this.mKeyStore.contains(Credentials.USER_PRIVATE_KEY + alias, this.mUid) || this.mKeyStore.contains(Credentials.USER_SECRET_KEY + alias, this.mUid) || this.mKeyStore.contains(Credentials.USER_CERTIFICATE + alias, this.mUid)) {
            return true;
        } else {
            return this.mKeyStore.contains(Credentials.CA_CERTIFICATE + alias, this.mUid);
        }
    }

    public int engineSize() {
        return getUniqueAliases().size();
    }

    public boolean engineIsKeyEntry(String alias) {
        return isKeyEntry(alias);
    }

    private boolean isKeyEntry(String alias) {
        return !isPrivateKeyEntry(alias) ? isSecretKeyEntry(alias) : true;
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            return this.mKeyStore.contains(Credentials.USER_PRIVATE_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isSecretKeyEntry(String alias) {
        if (alias != null) {
            return this.mKeyStore.contains(Credentials.USER_SECRET_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isCertificateEntry(String alias) {
        if (alias != null) {
            return this.mKeyStore.contains(Credentials.CA_CERTIFICATE + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    public boolean engineIsCertificateEntry(String alias) {
        return !isKeyEntry(alias) ? isCertificateEntry(alias) : false;
    }

    public String engineGetCertificateAlias(Certificate cert) {
        int i = 0;
        if (cert == null || !"X.509".equalsIgnoreCase(cert.getType())) {
            return null;
        }
        try {
            byte[] targetCertBytes = cert.getEncoded();
            if (targetCertBytes == null) {
                return null;
            }
            int length;
            byte[] certBytes;
            String alias;
            Set<String> nonCaEntries = new HashSet();
            String[] certAliases = this.mKeyStore.list(Credentials.USER_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                for (String alias2 : certAliases) {
                    certBytes = this.mKeyStore.get(Credentials.USER_CERTIFICATE + alias2, this.mUid);
                    if (certBytes != null) {
                        nonCaEntries.add(alias2);
                        if (Arrays.equals(certBytes, targetCertBytes)) {
                            return alias2;
                        }
                    }
                }
            }
            String[] caAliases = this.mKeyStore.list(Credentials.CA_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                length = caAliases.length;
                while (i < length) {
                    alias2 = caAliases[i];
                    if (!nonCaEntries.contains(alias2)) {
                        certBytes = this.mKeyStore.get(Credentials.CA_CERTIFICATE + alias2, this.mUid);
                        if (certBytes != null && Arrays.equals(certBytes, targetCertBytes)) {
                            return alias2;
                        }
                    }
                    i++;
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
        } else if (password != null) {
            throw new IllegalArgumentException("password not supported");
        } else {
            this.mKeyStore = KeyStore.getInstance();
            this.mUid = -1;
        }
    }

    public void engineLoad(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        int uid = -1;
        if (param != null) {
            if (param instanceof AndroidKeyStoreLoadStoreParameter) {
                uid = ((AndroidKeyStoreLoadStoreParameter) param).getUid();
            } else {
                throw new IllegalArgumentException("Unsupported param type: " + param.getClass());
            }
        }
        this.mKeyStore = KeyStore.getInstance();
        this.mUid = uid;
    }

    public void engineSetEntry(String alias, Entry entry, ProtectionParameter param) throws KeyStoreException {
        if (entry == null) {
            throw new KeyStoreException("entry == null");
        }
        Credentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
        if (entry instanceof TrustedCertificateEntry) {
            engineSetCertificateEntry(alias, ((TrustedCertificateEntry) entry).getTrustedCertificate());
            return;
        }
        if (entry instanceof PrivateKeyEntry) {
            PrivateKeyEntry prE = (PrivateKeyEntry) entry;
            setPrivateKeyEntry(alias, prE.getPrivateKey(), prE.getCertificateChain(), param);
        } else if (entry instanceof SecretKeyEntry) {
            setSecretKeyEntry(alias, ((SecretKeyEntry) entry).getSecretKey(), param);
        } else {
            throw new KeyStoreException("Entry must be a PrivateKeyEntry, SecretKeyEntry or TrustedCertificateEntry; was " + entry);
        }
    }
}
