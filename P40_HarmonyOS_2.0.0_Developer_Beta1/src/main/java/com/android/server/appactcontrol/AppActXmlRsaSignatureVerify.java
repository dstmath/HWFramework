package com.android.server.appactcontrol;

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

class AppActXmlRsaSignatureVerify {
    private static final String PUBLIC_KEY = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEA33r+y25SNoGf309AvD994AgyZZ/sQdbR2tSUdAZr27hXIKh4tpV0Zwniah8Xd2DuC/kQvl/2QtQAr0EI3JCc9XqmEHQ4PvOkecI6gatpSs67pXJCPt8HB1FLmlG5AGv6lPEMXVBKKqgmMoFq9wmR6yrTYTdud0w1Hvte18uEYIrM8dNeIJgt2MYouW+1DuEesb0iVtxpSxwIXq3Yp7NCdGJmCuIN3PxEY4dzkyw2Odsc1Qonif3Kr2FoC3UaHLL3Fy9q56Lh3xX9Y26gLxLo6EIymEUZcPRe4FM7WVZR6JIaRd+So8Je7cnNwAEheCGTy9nsgdeJBuXfIl45LvQD0UFXHLUnGe0JfL/6ANQUlPQNUZ8EeAEx95CWJZejjs8wgQCk805RpKm3CtbHpajQ6CKWeKoMzyWdg5XC25cSpd9Uz6rQpG2x9YIvCctcTSwkyyRAn7lkdPTWP0o8NXPusLPjhCz3I0NwatPHYRsgJ5s3MIMvFqSWE56OktVAaHKjAgMBAAE=";
    private static final String SIGNATURE_EXTENSION = ".bin";
    private static final String TAG = "AppActXmlRsaSignatureVerify";

    AppActXmlRsaSignatureVerify() {
    }

    static boolean verifyRsa(byte[] inputData, String xmlFilePath) {
        try {
            String signatureFilePath = xmlFilePath + SIGNATURE_EXTENSION;
            if (!AppActUtils.verifyFile(signatureFilePath)) {
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
            Log.e(TAG, "verifyRsa error ");
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
