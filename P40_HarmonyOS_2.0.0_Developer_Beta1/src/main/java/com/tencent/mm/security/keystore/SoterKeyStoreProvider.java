package com.tencent.mm.security.keystore;

import android.content.Context;
import android.hardware.biometrics.BiometricManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.HwKeyProperties;
import com.huawei.security.keystore.HwUniversalKeyStoreECPublicKey;
import com.huawei.security.keystore.HwUniversalKeyStorePrivateKey;
import com.huawei.security.keystore.HwUniversalKeyStoreProvider;
import com.huawei.security.keystore.HwUniversalKeyStorePublicKey;
import com.huawei.security.keystore.HwUniversalKeyStoreRSAPublicKey;
import com.tencent.mm.security.keystore.soter.SoterUtil;
import com.tencent.soter.core.biometric.FaceManager;
import com.tencent.soter.core.biometric.SoterFaceManagerFactory;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.json.JSONException;

public final class SoterKeyStoreProvider extends HwUniversalKeyStoreProvider {
    private static final String HUKS_PACKAGE_NAME = "com.huawei.security.keystore";
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePublicKey";
    private static final String PACKAGE_NAME = "com.tencent.mm.security.keystore.soter";
    private static final String PROVIDER_NAME = "SoterKeyStore.HUKS";
    private static final String PROVIDER_NAME_KEY = "KeyStore.SoterKeyStore.HUKS";
    private static final boolean SUPPORT_HWPKI = "true".equals(SystemPropertiesEx.get("ro.config.support_hwpki"));
    private static final boolean SUPPORT_ONLINE_HWPKI = "true".equals(SystemPropertiesEx.get("ro.config.support_mm_fp_pay"));
    private static final boolean SUPPORT_SOTER = SystemPropertiesEx.getBoolean("hw_mc.huks.support_soter", true);
    private static HwKeystoreManager sKeystoreManager = HwKeystoreManager.getInstance();
    private static final long serialVersionUID = 1;

    private SoterKeyStoreProvider() {
        super(PROVIDER_NAME, 1.0d, "Huawei Soter KeyStore security provider");
        put(PROVIDER_NAME_KEY, "com.tencent.mm.security.keystore.soter.SoterKeyStoreSpi");
        put("KeyPairGenerator.RSA", "com.tencent.mm.security.keystore.soter.SoterKeyStoreKeyPairGeneratorSpi$RSA");
        putSignatureImpl("SHA256withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA256WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA256WithRSAEncryption", "SHA256withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256withRSA");
        putSignatureImpl("SHA384withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA384WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA384WithRSAEncryption", "SHA384withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384withRSA");
        putSignatureImpl("SHA512withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA512WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA512WithRSAEncryption", "SHA512withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512withRSA");
        putSignatureImpl("SHA256withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA256WithPSSPadding");
        putSignatureImpl("SHA384withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA384WithPSSPadding");
        putSignatureImpl("SHA512withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA512WithPSSPadding");
        putAsymmetricCipherImpl("RSA/ECB/PKCS1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$PKCS1Padding");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA256AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA384AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA512AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
    }

    public static void install() {
        if ((SUPPORT_HWPKI && checkSoterCap()) || (SUPPORT_ONLINE_HWPKI && isExistTrustCerts())) {
            Log.i(PROVIDER_NAME, "Add SoterKeyStoreProvider");
            Security.addProvider(new SoterKeyStoreProvider());
        }
    }

    private static boolean checkSoterCap() {
        if (!SUPPORT_SOTER) {
            Log.i(PROVIDER_NAME, "The value of soter prop is false!");
            return false;
        }
        Context context = ActivityThreadEx.currentApplication().getApplicationContext();
        if (context == null) {
            Log.e(PROVIDER_NAME, "Get application context failed!");
            return false;
        } else if (isFingerprintSupported(context) || is3DFaceSupported(context)) {
            return true;
        } else {
            Log.i(PROVIDER_NAME, "Neither Fingerprint nor 3D face is supported!");
            return false;
        }
    }

    private static boolean isFingerprintSupported(@NonNull Context context) {
        try {
            Object obj = context.getSystemService("biometric");
            if (obj != null) {
                if (obj instanceof BiometricManager) {
                    int result = ((BiometricManager) obj).canAuthenticate();
                    if (result == 12 || result == 1) {
                        return false;
                    }
                    return true;
                }
            }
            Log.e(PROVIDER_NAME, "Get BiometricManager failed!");
            return false;
        } catch (SecurityException e) {
            Log.e(PROVIDER_NAME, "Triggered SecurityException in BiometricManager!");
            return false;
        }
    }

    private static boolean is3DFaceSupported(@NonNull Context context) {
        try {
            FaceManager mgr = SoterFaceManagerFactory.getFaceManager(context);
            if (mgr != null) {
                return mgr.isHardwareDetected();
            }
            Log.e(PROVIDER_NAME, "Get FaceManager failed!");
            return false;
        } catch (SecurityException e) {
            Log.e(PROVIDER_NAME, "Triggered SecurityException in FaceManager!");
            return false;
        }
    }

    private static boolean isExistTrustCerts() {
        if (sKeystoreManager == null) {
            Log.e(PROVIDER_NAME, "HwKeystoreManager is null!");
            return false;
        }
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (sKeystoreManager.exportTrustCert(outChain) != 1) {
            Log.e(PROVIDER_NAME, "exportTrustCert failed!");
            return false;
        } else if (outChain.getCertificates() != null) {
            return true;
        } else {
            Log.e(PROVIDER_NAME, "export certs failed!");
            return false;
        }
    }

    @NonNull
    public static KeyPair loadSoterKeyStoreKeyPairFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid, int flags) throws UnrecoverableKeyException {
        HwUniversalKeyStorePublicKey publicKey = loadHwKeyStorePublicKeyFromKeystore(keyStore, privateKeyAlias, uid, flags);
        Log.i(PROVIDER_NAME, "load public key succeed!");
        HwUniversalKeyStorePrivateKey privateKey = getHwKeyStorePrivateKey(publicKey);
        Log.i(PROVIDER_NAME, "load private key succeed!");
        return new KeyPair(publicKey, privateKey);
    }

    @NonNull
    public static KeyPair loadSoterKeyPairFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        return loadSoterKeyStoreKeyPairFromKeystore(keyStore, privateKeyAlias, uid, 0);
    }

    @NonNull
    private static HwUniversalKeyStorePublicKey getHwKeyStorePublicKeyInner(@NonNull String alias, int uid, @NonNull String keyAlgorithm, @NonNull byte[] x509EncodedForm, int flags) {
        PublicKey publicKey;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            if ((flags & 1) != 0) {
                byte[] realPublicKey = SoterUtil.getDataFromRaw(x509EncodedForm, SoterUtil.JSON_KEY_PUBLIC);
                if (realPublicKey != null) {
                    publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(realPublicKey));
                } else {
                    throw new NullPointerException("invalid soter public key");
                }
            } else {
                publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(x509EncodedForm));
            }
            if (HwKeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
                if ((flags & 1) == 0) {
                    return new HwUniversalKeyStoreRSAPublicKey(alias, uid, (RSAPublicKey) publicKey);
                }
                RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
                return new HwUniversalKeyStoreRSAPublicKey(alias, uid, x509EncodedForm, rsaPubKey.getModulus(), rsaPubKey.getPublicExponent());
            } else if (!HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
                throw new ProviderException("Unsupported HwKeystore public key algorithm: " + keyAlgorithm);
            } else if (publicKey instanceof ECPublicKey) {
                return new HwUniversalKeyStoreECPublicKey(alias, uid, (ECPublicKey) publicKey);
            } else {
                throw new ProviderException("public key is not ECPublicKey.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to obtain " + keyAlgorithm + " KeyFactory" + e.getMessage());
        } catch (InvalidKeySpecException e2) {
            throw new ProviderException("Invalid X.509 encoding of public key" + e2.getMessage());
        } catch (JSONException e3) {
            throw new ProviderException("Invalid json format" + e3.getMessage());
        }
    }

    @NonNull
    public static HwUniversalKeyStorePublicKey loadHwKeyStorePublicKeyFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid, int flags) throws UnrecoverableKeyException {
        HwExportResult exportResult;
        HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
        int errorCode = keyStore.getKeyCharacteristics(privateKeyAlias, null, null, uid, keyCharacteristics);
        if (errorCode == 1) {
            if ((flags & 1) != 0) {
                exportResult = keyStore.exportKey(privateKeyAlias, 1000, null, null, uid);
            } else {
                exportResult = keyStore.exportKey(privateKeyAlias, 0, null, null, uid);
            }
            if (exportResult.resultCode == 1) {
                byte[] x509EncodedPublicKey = exportResult.exportData;
                Integer keymasterAlgorithm = keyCharacteristics.getEnum(HwKeymasterDefs.KM_TAG_ALGORITHM);
                if (keymasterAlgorithm != null) {
                    try {
                        return getHwKeyStorePublicKeyInner(privateKeyAlias, uid, HwKeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(keymasterAlgorithm.intValue()), x509EncodedPublicKey, flags);
                    } catch (IllegalArgumentException e) {
                        UnrecoverableKeyException unrecoverableKeyException = new UnrecoverableKeyException("Failed to load private key");
                        unrecoverableKeyException.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
                        throw unrecoverableKeyException;
                    }
                } else {
                    throw new UnrecoverableKeyException("Key algorithm unknown");
                }
            } else {
                UnrecoverableKeyException unrecoverableKeyException2 = new UnrecoverableKeyException("Failed to obtain X.509 form of public key");
                unrecoverableKeyException2.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
                throw unrecoverableKeyException2;
            }
        } else {
            UnrecoverableKeyException unrecoverableKeyException3 = new UnrecoverableKeyException("Failed to obtain information about private key");
            unrecoverableKeyException3.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
            throw unrecoverableKeyException3;
        }
    }

    private void putSignatureImpl(String algorithm, String implClass) {
        put("Signature." + algorithm, implClass);
        put("Signature." + algorithm + " SupportedKeyClasses", "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey|com.huawei.security.keystore.HwUniversalKeyStorePublicKey");
    }

    private void putAsymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey|com.huawei.security.keystore.HwUniversalKeyStorePublicKey");
    }
}
