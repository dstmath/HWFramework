package com.android.server.signedconfig;

import android.os.Build;
import android.util.Slog;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureVerifier {
    private static final boolean DBG = false;
    private static final String DEBUG_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmJKs4lSn+XRhMQmMid+Zbhbu13YrU1haIhVC5296InRu1x7A8PV1ejQyisBODGgRY6pqkAHRncBCYcgg5wIIJg==";
    private static final String PROD_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+lky6wKyGL6lE1VrD0YTMHwb0Xwc+tzC8MvnrzVxodvTpVY/jV7V+Zktcx+pry43XPABFRXtbhTo+qykhyBA1g==";
    private static final String TAG = "SignedConfig";
    private final PublicKey mDebugKey;
    private final SignedConfigEvent mEvent;
    private final PublicKey mProdKey;

    public SignatureVerifier(SignedConfigEvent event) {
        this.mEvent = event;
        this.mDebugKey = Build.IS_DEBUGGABLE ? createKey(DEBUG_KEY) : null;
        this.mProdKey = createKey(PROD_KEY);
    }

    private static PublicKey createKey(String base64) {
        try {
            try {
                return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Slog.e(TAG, "Failed to construct public key", e);
                return null;
            }
        } catch (IllegalArgumentException e2) {
            Slog.e(TAG, "Failed to base64 decode public key", e2);
            return null;
        }
    }

    private boolean verifyWithPublicKey(PublicKey key, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(key);
        verifier.update(data);
        return verifier.verify(signature);
    }

    public boolean verifySignature(String config, String base64Signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            byte[] signature = Base64.getDecoder().decode(base64Signature);
            byte[] data = config.getBytes(StandardCharsets.UTF_8);
            if (Build.IS_DEBUGGABLE) {
                PublicKey publicKey = this.mDebugKey;
                if (publicKey == null) {
                    Slog.w(TAG, "Debuggable build, but have no debug key");
                } else if (verifyWithPublicKey(publicKey, data, signature)) {
                    Slog.i(TAG, "Verified config using debug key");
                    this.mEvent.verifiedWith = 1;
                    return true;
                }
            }
            PublicKey publicKey2 = this.mProdKey;
            if (publicKey2 == null) {
                Slog.e(TAG, "No prod key; construction failed?");
                this.mEvent.status = 9;
                return false;
            } else if (verifyWithPublicKey(publicKey2, data, signature)) {
                Slog.i(TAG, "Verified config using production key");
                this.mEvent.verifiedWith = 2;
                return true;
            } else {
                this.mEvent.status = 7;
                return false;
            }
        } catch (IllegalArgumentException e) {
            this.mEvent.status = 3;
            Slog.e(TAG, "Failed to base64 decode signature");
            return false;
        }
    }
}
