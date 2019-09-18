package com.android.org.conscrypt;

import com.android.org.conscrypt.ct.CTConstants;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

final class CryptoUpcalls {
    private static final Logger logger = Logger.getLogger(CryptoUpcalls.class.getName());

    private CryptoUpcalls() {
    }

    private static boolean isOurProvider(Provider p) {
        return p.getClass().getPackage().equals(CryptoUpcalls.class.getPackage());
    }

    private static ArrayList<Provider> getExternalProviders(String algorithm) {
        ArrayList<Provider> providers = new ArrayList<>(1);
        for (Provider p : Security.getProviders(algorithm)) {
            if (!isOurProvider(p)) {
                providers.add(p);
            }
        }
        if (providers.isEmpty()) {
            logger.warning("Could not find external provider for algorithm: " + algorithm);
        }
        return providers;
    }

    static byte[] rawSignDigestWithPrivateKey(PrivateKey javaKey, byte[] message) {
        String algorithm;
        Signature signature;
        Signature signature2;
        String keyAlgorithm = javaKey.getAlgorithm();
        if ("RSA".equals(keyAlgorithm)) {
            algorithm = "NONEwithRSA";
        } else if ("EC".equals(keyAlgorithm)) {
            algorithm = "NONEwithECDSA";
        } else {
            throw new RuntimeException("Unexpected key type: " + javaKey.toString());
        }
        try {
            signature = Signature.getInstance(algorithm);
            signature.initSign(javaKey);
            if (isOurProvider(signature.getProvider())) {
                signature = null;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warning("Unsupported signature algorithm: " + algorithm);
            return null;
        } catch (InvalidKeyException e2) {
            logger.warning("Preferred provider doesn't support key:");
            e2.printStackTrace();
            signature = null;
        }
        if (signature == null) {
            Iterator<Provider> it = getExternalProviders("Signature." + algorithm).iterator();
            while (it.hasNext()) {
                try {
                    signature = Signature.getInstance(algorithm, it.next());
                    signature.initSign(javaKey);
                    break;
                } catch (NoSuchAlgorithmException e3) {
                    signature2 = null;
                } catch (InvalidKeyException e4) {
                    signature2 = null;
                }
            }
            if (signature == null) {
                logger.warning("Could not find provider for algorithm: " + algorithm);
                return null;
            }
        }
        try {
            signature.update(message);
            return signature.sign();
        } catch (Exception e5) {
            logger.log(Level.WARNING, "Exception while signing message with " + javaKey.getAlgorithm() + " private key:", e5);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b9  */
    static byte[] rsaDecryptWithPrivateKey(PrivateKey javaKey, int openSSLPadding, byte[] input) {
        String jcaPadding;
        Cipher c;
        Cipher c2;
        if (!"RSA".equals(javaKey.getAlgorithm())) {
            logger.warning("Unexpected key type: " + keyAlgorithm);
            return null;
        }
        if (openSSLPadding != 1) {
            switch (openSSLPadding) {
                case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                    jcaPadding = "NoPadding";
                    break;
                case 4:
                    jcaPadding = "OAEPPadding";
                    break;
                default:
                    logger.warning("Unsupported OpenSSL/BoringSSL padding: " + openSSLPadding);
                    return null;
            }
        } else {
            jcaPadding = "PKCS1Padding";
        }
        String transformation = "RSA/ECB/" + jcaPadding;
        try {
            c = Cipher.getInstance(transformation);
            c.init(2, javaKey);
            if (isOurProvider(c.getProvider())) {
                c = null;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warning("Unsupported cipher algorithm: " + transformation);
            return null;
        } catch (NoSuchPaddingException e2) {
            logger.warning("Unsupported cipher algorithm: " + transformation);
            return null;
        } catch (InvalidKeyException e3) {
            logger.log(Level.WARNING, "Preferred provider doesn't support key:", e3);
            c = null;
        }
        if (c == null) {
            Iterator<Provider> it = getExternalProviders("Cipher." + transformation).iterator();
            while (it.hasNext()) {
                try {
                    c = Cipher.getInstance(transformation, it.next());
                    c.init(2, javaKey);
                    if (c == null) {
                        logger.warning("Could not find provider for algorithm: " + transformation);
                        return null;
                    }
                } catch (NoSuchAlgorithmException e4) {
                    c2 = null;
                } catch (InvalidKeyException e5) {
                    c2 = null;
                } catch (NoSuchPaddingException e6) {
                    c2 = null;
                }
            }
            if (c == null) {
            }
        }
        try {
            return c.doFinal(input);
        } catch (Exception e7) {
            logger.log(Level.WARNING, "Exception while decrypting message with " + javaKey.getAlgorithm() + " private key using " + transformation + ":", e7);
            return null;
        }
    }
}
