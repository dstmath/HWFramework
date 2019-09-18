package android.security.keystore;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.storage.VolumeInfo;
import android.security.Credentials;
import android.security.KeyStore;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class AndroidKeyStoreKeyFactorySpi extends KeyFactorySpi {
    private final KeyStore mKeyStore = KeyStore.getInstance();

    /* access modifiers changed from: protected */
    public <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpecClass) throws InvalidKeySpecException {
        if (key == null) {
            throw new InvalidKeySpecException("key == null");
        } else if (!(key instanceof AndroidKeyStorePrivateKey) && !(key instanceof AndroidKeyStorePublicKey)) {
            throw new InvalidKeySpecException("Unsupported key type: " + key.getClass().getName() + ". This KeyFactory supports only Android Keystore asymmetric keys");
        } else if (keySpecClass == null) {
            throw new InvalidKeySpecException("keySpecClass == null");
        } else if (KeyInfo.class.equals(keySpecClass)) {
            if (key instanceof AndroidKeyStorePrivateKey) {
                AndroidKeyStorePrivateKey keystorePrivateKey = (AndroidKeyStorePrivateKey) key;
                String keyAliasInKeystore = keystorePrivateKey.getAlias();
                if (keyAliasInKeystore.startsWith(Credentials.USER_PRIVATE_KEY)) {
                    return AndroidKeyStoreSecretKeyFactorySpi.getKeyInfo(this.mKeyStore, keyAliasInKeystore.substring(Credentials.USER_PRIVATE_KEY.length()), keyAliasInKeystore, keystorePrivateKey.getUid());
                }
                throw new InvalidKeySpecException("Invalid key alias: " + keyAliasInKeystore);
            }
            throw new InvalidKeySpecException("Unsupported key type: " + key.getClass().getName() + ". KeyInfo can be obtained only for Android Keystore private keys");
        } else if (X509EncodedKeySpec.class.equals(keySpecClass)) {
            if (key instanceof AndroidKeyStorePublicKey) {
                return new X509EncodedKeySpec(((AndroidKeyStorePublicKey) key).getEncoded());
            }
            throw new InvalidKeySpecException("Unsupported key type: " + key.getClass().getName() + ". X509EncodedKeySpec can be obtained only for Android Keystore public keys");
        } else if (PKCS8EncodedKeySpec.class.equals(keySpecClass)) {
            if (key instanceof AndroidKeyStorePrivateKey) {
                throw new InvalidKeySpecException("Key material export of Android Keystore private keys is not supported");
            }
            throw new InvalidKeySpecException("Cannot export key material of public key in PKCS#8 format. Only X.509 format (X509EncodedKeySpec) supported for public keys.");
        } else if (RSAPublicKeySpec.class.equals(keySpecClass)) {
            if (key instanceof AndroidKeyStoreRSAPublicKey) {
                AndroidKeyStoreRSAPublicKey rsaKey = (AndroidKeyStoreRSAPublicKey) key;
                return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Obtaining RSAPublicKeySpec not supported for ");
            sb.append(key.getAlgorithm());
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(key instanceof AndroidKeyStorePrivateKey ? VolumeInfo.ID_PRIVATE_INTERNAL : "public");
            sb.append(" key");
            throw new InvalidKeySpecException(sb.toString());
        } else if (!ECPublicKeySpec.class.equals(keySpecClass)) {
            throw new InvalidKeySpecException("Unsupported key spec: " + keySpecClass.getName());
        } else if (key instanceof AndroidKeyStoreECPublicKey) {
            AndroidKeyStoreECPublicKey ecKey = (AndroidKeyStoreECPublicKey) key;
            return new ECPublicKeySpec(ecKey.getW(), ecKey.getParams());
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Obtaining ECPublicKeySpec not supported for ");
            sb2.append(key.getAlgorithm());
            sb2.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb2.append(key instanceof AndroidKeyStorePrivateKey ? VolumeInfo.ID_PRIVATE_INTERNAL : "public");
            sb2.append(" key");
            throw new InvalidKeySpecException(sb2.toString());
        }
    }

    /* access modifiers changed from: protected */
    public PrivateKey engineGeneratePrivate(KeySpec spec) throws InvalidKeySpecException {
        throw new InvalidKeySpecException("To generate a key pair in Android Keystore, use KeyPairGenerator initialized with " + KeyGenParameterSpec.class.getName());
    }

    /* access modifiers changed from: protected */
    public PublicKey engineGeneratePublic(KeySpec spec) throws InvalidKeySpecException {
        throw new InvalidKeySpecException("To generate a key pair in Android Keystore, use KeyPairGenerator initialized with " + KeyGenParameterSpec.class.getName());
    }

    /* access modifiers changed from: protected */
    public Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if ((key instanceof AndroidKeyStorePrivateKey) || (key instanceof AndroidKeyStorePublicKey)) {
            return key;
        } else {
            throw new InvalidKeyException("To import a key into Android Keystore, use KeyStore.setEntry");
        }
    }
}
