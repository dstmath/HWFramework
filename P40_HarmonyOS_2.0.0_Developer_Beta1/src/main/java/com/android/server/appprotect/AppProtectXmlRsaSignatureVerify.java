package com.android.server.appprotect;

import android.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class AppProtectXmlRsaSignatureVerify {
    private static final String PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAuBUJ1MHurl7U7/pcXIXWGNBmkHJd5l0l2Xl7LWrzYZGGQTESC3owup1uRHq1NW6+rY+grLFuNKC6eXAT76mLx5ZF9EcIJyKCP02AL/P9wnndeiZ+WTZaVpAbxCYwan1UkSMzNaBpUMQ07XIs42FmhS1zZIMmJZgQZlFZW5rG7S7uKdqoyaPVVc/B5HHW8uhxybojkMi4LnfotHBlQwL1T3V/QwVhObFrZHNpQ4qQIS9wNoLyq0bO/kuEoA4YSAhPgHpNJmcjM5lu9c+quApgyf40hBGTj0Ra7fBUqutWIv8HUu6Bmkf8fu8RDjiL4l7UdogKpSI2mGQ1zhVpMWdHiZGkpNL/5kj9A6h124/CL4YhS92CWedrzYKi1AA9PstpRp1tz0WaLO23OtlOE40Iuw78At4kigXAIwL/G/lf/FBAc88lHyyZNizy0kqTxAsGn47D+JJSpSL15m2TuySKT8kqZq/q4cOcDFhWZE9ajxtrwqSEoxtCAgP2LYsC6BJZTug+bx5qBYunTncBq1lxP4cxk0DeHZ2hS56G9IYunV13RpmQPF2hvRx5H8Wv9vJeZ0q+xg+QGMTATIFMpA9gT/1ruAqR+wNwhM1DLxLy1kX3VO1IxbslEm/aPw4ghsQlcyTqdhH/PVtYkBD6L71H5bfgUdijdKQUm0WOfYnXV8kCAwEAAQ==";
    private static final String SIGBATURE_EXTENSION = ".bin";
    private static final String TAG = "AppProtectXmlRsaSignatureVerify";

    AppProtectXmlRsaSignatureVerify() {
    }

    static boolean verifyRsa(byte[] inputData, String xmlFilePath) {
        try {
            String signatureFilePath = xmlFilePath + SIGBATURE_EXTENSION;
            if (!AppProtectUtil.verifyFile(signatureFilePath)) {
                Log.e(TAG, "verifyFile failure return false");
                return false;
            }
            if (verify(getPublicKeyFromStr(PUBLIC_KEY), inputData, Files.readAllBytes(Paths.get(signatureFilePath, new String[0])))) {
                Log.i(TAG, "verify success");
                return true;
            }
            Log.e(TAG, "verify failure");
            return false;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "verifyRsa NoSuchAlgorithmException");
        } catch (InvalidKeySpecException e2) {
            Log.e(TAG, "verifyRsa InvalidKeySpecException");
        } catch (InvalidKeyException e3) {
            Log.e(TAG, "verifyRsa InvalidKeyException");
        } catch (SignatureException e4) {
            Log.e(TAG, "verifyRsa SignatureException");
        } catch (Exception e5) {
            Log.e(TAG, "verifyRsa error");
        }
    }

    private static boolean verify(PublicKey key, byte[] input, byte[] signature) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature verifier = Signature.getInstance("SHA256WITHRSA");
        if (key == null || input == null || input.length == 0) {
            return false;
        }
        verifier.initVerify(key);
        verifier.update(input);
        return verifier.verify(signature);
    }

    private static PublicKey getPublicKeyFromStr(String pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(pubKey);
        if (decodedKey == null || decodedKey.length == 0) {
            return null;
        }
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
    }
}
