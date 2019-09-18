package com.huawei.facerecognition;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import com.huawei.facerecognition.FaceManager;
import com.huawei.hardware.face.FaceAuthenticationManager;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class HwFaceManager extends FaceManager {
    private static final String TAG = "Facerecognition.FaceManager";
    /* access modifiers changed from: private */
    public FaceManager.CryptoObject mCryptoObject;
    private FaceAuthenticationManager mFaceAuthenticationManager;

    public HwFaceManager(Context context) {
        this.mFaceAuthenticationManager = new FaceAuthenticationManager(context);
    }

    public boolean hasEnrolledFaces() {
        Log.i(TAG, "The hasEnrolledFaces interface is Deprecated,using interface hasEnrolledTemplates.");
        return false;
    }

    public boolean hasEnrolledTemplates() {
        return this.mFaceAuthenticationManager.hasEnrolledFace();
    }

    public boolean isHardwareDetected() {
        return this.mFaceAuthenticationManager.isHardwareDetected();
    }

    public void authenticate(FaceManager.CryptoObject crypto, CancellationSignal cancel, int flags, final FaceManager.AuthenticationCallback callback, Handler handler) {
        FaceAuthenticationManager.CryptoObject hwcrypto = null;
        if (crypto != null) {
            Signature signature = crypto.getSignature();
            Cipher cipher = crypto.getCipher();
            Mac mac = crypto.getMac();
            if (signature != null) {
                hwcrypto = new FaceAuthenticationManager.CryptoObject(signature);
            } else if (cipher != null) {
                hwcrypto = new FaceAuthenticationManager.CryptoObject(cipher);
            } else if (mac != null) {
                hwcrypto = new FaceAuthenticationManager.CryptoObject(mac);
            }
            this.mCryptoObject = crypto;
        }
        this.mFaceAuthenticationManager.authenticate(hwcrypto, cancel, flags, new FaceAuthenticationManager.AuthenticationCallback() {
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                callback.onAuthenticationError(errorCode, errString);
            }

            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                callback.onAuthenticationHelp(helpCode, helpString);
            }

            public void onAuthenticationSucceeded(FaceAuthenticationManager.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new FaceManager.AuthenticationResult(HwFaceManager.this.mCryptoObject));
            }

            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        }, handler);
    }
}
