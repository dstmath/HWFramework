package ohos.security.keystore.provider;

import java.security.Provider;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.keystore.KeyStoreConstants;

public class HarmonyKeyStoreProvider extends Provider {
    private static final HiLogLabel LABEL = KeyStoreLogger.getLabel(TAG);
    private static final String PACKAGE_NAME = "ohos.security.keystore.provider";
    private static final String PROVIDER_NAME = "HarmonyKeyStore";
    private static final String TAG = "HarmonyKeyStoreProvider";
    private static final double VERSION = 1.0d;
    private static final long serialVersionUID = -5496901824738987295L;

    public HarmonyKeyStoreProvider() {
        super(PROVIDER_NAME, VERSION, "Harmony KeyStore security provider");
        put("KeyStore.HarmonyKeyStore", "ohos.security.keystore.provider.HarmonyKeyStoreSpi");
        put("KeyPairGenerator.EC", "ohos.security.keystore.provider.HarmonyKeyStoreKeyPairGeneratorSpi$EC");
        put("KeyPairGenerator.RSA", "ohos.security.keystore.provider.HarmonyKeyStoreKeyPairGeneratorSpi$RSA");
        putKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
        putKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("KeyGenerator.AES", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$AES");
        put("KeyGenerator.HmacSHA1", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$HmacSHA1");
        put("KeyGenerator.HmacSHA224", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$HmacSHA224");
        put("KeyGenerator.HmacSHA256", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$HmacSHA256");
        put("KeyGenerator.HmacSHA384", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$HmacSHA384");
        put("KeyGenerator.HmacSHA512", "ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi$HmacSHA512");
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_AES);
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        putSecretKeyFactoryImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
    }

    public static void install() {
        new Thread(new HarmonyProviderLoader()).start();
    }

    private void putSecretKeyFactoryImpl(String str) {
        put("SecretKeyFactory." + str, "ohos.security.keystore.provider.HarmonyKeyStoreSecretKeyFactorySpi");
    }

    private void putKeyFactoryImpl(String str) {
        put("KeyFactory." + str, "ohos.security.keystore.provider.HarmonyKeyStoreKeyFactorySpi");
    }
}
