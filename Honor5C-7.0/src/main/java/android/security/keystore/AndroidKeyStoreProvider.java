package android.security.keystore;

import android.security.KeyStore;
import android.security.keymaster.ExportResult;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.KeyAlgorithm;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class AndroidKeyStoreProvider extends Provider {
    private static final String PACKAGE_NAME = "android.security.keystore";
    public static final String PROVIDER_NAME = "AndroidKeyStore";

    public AndroidKeyStoreProvider() {
        super(PROVIDER_NAME, 1.0d, "Android KeyStore security provider");
        put("KeyStore.AndroidKeyStore", "android.security.keystore.AndroidKeyStoreSpi");
        put("KeyPairGenerator.EC", "android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi$EC");
        put("KeyPairGenerator.RSA", "android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi$RSA");
        putKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_EC);
        putKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_RSA);
        put("KeyGenerator.AES", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$AES");
        put("KeyGenerator.HmacSHA1", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$HmacSHA1");
        put("KeyGenerator.HmacSHA224", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$HmacSHA224");
        put("KeyGenerator.HmacSHA256", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$HmacSHA256");
        put("KeyGenerator.HmacSHA384", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$HmacSHA384");
        put("KeyGenerator.HmacSHA512", "android.security.keystore.AndroidKeyStoreKeyGeneratorSpi$HmacSHA512");
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_AES);
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA1);
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA224);
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA384);
        putSecretKeyFactoryImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA512);
    }

    public static void install() {
        Provider[] providers = Security.getProviders();
        int bcProviderIndex = -1;
        for (int i = 0; i < providers.length; i++) {
            if ("BC".equals(providers[i].getName())) {
                bcProviderIndex = i;
                break;
            }
        }
        Security.addProvider(new AndroidKeyStoreProvider());
        Provider workaroundProvider = new AndroidKeyStoreBCWorkaroundProvider();
        if (bcProviderIndex != -1) {
            Security.insertProviderAt(workaroundProvider, bcProviderIndex + 1);
        } else {
            Security.addProvider(workaroundProvider);
        }
    }

    private void putSecretKeyFactoryImpl(String algorithm) {
        put("SecretKeyFactory." + algorithm, "android.security.keystore.AndroidKeyStoreSecretKeyFactorySpi");
    }

    private void putKeyFactoryImpl(String algorithm) {
        put("KeyFactory." + algorithm, "android.security.keystore.AndroidKeyStoreKeyFactorySpi");
    }

    public static long getKeyStoreOperationHandle(Object cryptoPrimitive) {
        if (cryptoPrimitive == null) {
            throw new NullPointerException();
        }
        Object spi;
        if (cryptoPrimitive instanceof Signature) {
            spi = ((Signature) cryptoPrimitive).getCurrentSpi();
        } else if (cryptoPrimitive instanceof Mac) {
            spi = ((Mac) cryptoPrimitive).getCurrentSpi();
        } else if (cryptoPrimitive instanceof Cipher) {
            spi = ((Cipher) cryptoPrimitive).getCurrentSpi();
        } else {
            throw new IllegalArgumentException("Unsupported crypto primitive: " + cryptoPrimitive + ". Supported: Signature, Mac, Cipher");
        }
        if (spi == null) {
            throw new IllegalStateException("Crypto primitive not initialized");
        } else if (spi instanceof KeyStoreCryptoOperation) {
            return ((KeyStoreCryptoOperation) spi).getOperationHandle();
        } else {
            throw new IllegalArgumentException("Crypto primitive not backed by AndroidKeyStore provider: " + cryptoPrimitive + ", spi: " + spi);
        }
    }

    public static AndroidKeyStorePublicKey getAndroidKeyStorePublicKey(String alias, int uid, String keyAlgorithm, byte[] x509EncodedForm) {
        try {
            PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(x509EncodedForm));
            if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
                return new AndroidKeyStoreECPublicKey(alias, uid, (ECPublicKey) publicKey);
            }
            if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
                return new AndroidKeyStoreRSAPublicKey(alias, uid, (RSAPublicKey) publicKey);
            }
            throw new ProviderException("Unsupported Android Keystore public key algorithm: " + keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to obtain " + keyAlgorithm + " KeyFactory", e);
        } catch (InvalidKeySpecException e2) {
            throw new ProviderException("Invalid X.509 encoding of public key", e2);
        }
    }

    public static AndroidKeyStorePrivateKey getAndroidKeyStorePrivateKey(AndroidKeyStorePublicKey publicKey) {
        String keyAlgorithm = publicKey.getAlgorithm();
        if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
            return new AndroidKeyStoreECPrivateKey(publicKey.getAlias(), publicKey.getUid(), ((ECKey) publicKey).getParams());
        }
        if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
            return new AndroidKeyStoreRSAPrivateKey(publicKey.getAlias(), publicKey.getUid(), ((RSAKey) publicKey).getModulus());
        }
        throw new ProviderException("Unsupported Android Keystore public key algorithm: " + keyAlgorithm);
    }

    public static AndroidKeyStorePublicKey loadAndroidKeyStorePublicKeyFromKeystore(KeyStore keyStore, String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
        int errorCode = keyStore.getKeyCharacteristics(privateKeyAlias, null, null, uid, keyCharacteristics);
        if (errorCode != 1) {
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to obtain information about private key").initCause(KeyStore.getKeyStoreException(errorCode)));
        }
        ExportResult exportResult = keyStore.exportKey(privateKeyAlias, 0, null, null, uid);
        if (exportResult.resultCode != 1) {
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to obtain X.509 form of public key").initCause(KeyStore.getKeyStoreException(errorCode)));
        }
        byte[] x509EncodedPublicKey = exportResult.exportData;
        Integer keymasterAlgorithm = keyCharacteristics.getEnum(KeymasterDefs.KM_TAG_ALGORITHM);
        if (keymasterAlgorithm == null) {
            throw new UnrecoverableKeyException("Key algorithm unknown");
        }
        try {
            return getAndroidKeyStorePublicKey(privateKeyAlias, uid, KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(keymasterAlgorithm.intValue()), x509EncodedPublicKey);
        } catch (Throwable e) {
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to load private key").initCause(e));
        }
    }

    public static KeyPair loadAndroidKeyStoreKeyPairFromKeystore(KeyStore keyStore, String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        AndroidKeyStorePublicKey publicKey = loadAndroidKeyStorePublicKeyFromKeystore(keyStore, privateKeyAlias, uid);
        return new KeyPair(publicKey, getAndroidKeyStorePrivateKey(publicKey));
    }

    public static AndroidKeyStorePrivateKey loadAndroidKeyStorePrivateKeyFromKeystore(KeyStore keyStore, String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        return (AndroidKeyStorePrivateKey) loadAndroidKeyStoreKeyPairFromKeystore(keyStore, privateKeyAlias, uid).getPrivate();
    }

    public static AndroidKeyStoreSecretKey loadAndroidKeyStoreSecretKeyFromKeystore(KeyStore keyStore, String secretKeyAlias, int uid) throws UnrecoverableKeyException {
        KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
        int errorCode = keyStore.getKeyCharacteristics(secretKeyAlias, null, null, uid, keyCharacteristics);
        if (errorCode != 1) {
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to obtain information about key").initCause(KeyStore.getKeyStoreException(errorCode)));
        }
        Integer keymasterAlgorithm = keyCharacteristics.getEnum(KeymasterDefs.KM_TAG_ALGORITHM);
        if (keymasterAlgorithm == null) {
            throw new UnrecoverableKeyException("Key algorithm unknown");
        }
        int keymasterDigest;
        List<Integer> keymasterDigests = keyCharacteristics.getEnums(KeymasterDefs.KM_TAG_DIGEST);
        if (keymasterDigests.isEmpty()) {
            keymasterDigest = -1;
        } else {
            keymasterDigest = ((Integer) keymasterDigests.get(0)).intValue();
        }
        try {
            return new AndroidKeyStoreSecretKey(secretKeyAlias, uid, KeyAlgorithm.fromKeymasterSecretKeyAlgorithm(keymasterAlgorithm.intValue(), keymasterDigest));
        } catch (IllegalArgumentException e) {
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Unsupported secret key type").initCause(e));
        }
    }

    public static java.security.KeyStore getKeyStoreForUid(int uid) throws KeyStoreException, NoSuchProviderException {
        java.security.KeyStore result = java.security.KeyStore.getInstance(PROVIDER_NAME, PROVIDER_NAME);
        try {
            result.load(new AndroidKeyStoreLoadStoreParameter(uid));
            return result;
        } catch (Exception e) {
            throw new KeyStoreException("Failed to load AndroidKeyStore KeyStore for UID " + uid, e);
        }
    }
}
