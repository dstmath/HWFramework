package com.tencent.soter.core.biometric;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import com.huawei.hardware.face.FaceAuthenticationManager;
import com.tencent.soter.core.biometric.FaceManager;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class HwFaceManager extends FaceManager {
    private static final int FACE_SECURE_LEVEL_PAY = 1;
    private static final int SECURE_FACE_MODE_WITH_3D = 4;
    private static final int SOTER_FLAG = 5;
    /* access modifiers changed from: private */
    public FaceManager.CryptoObject mCryptoObject;
    private FaceAuthenticationManager mFaceAuthenticationManager;

    public HwFaceManager(Context context) {
        this.mFaceAuthenticationManager = new FaceAuthenticationManager(context);
    }

    public String getBiometricName(Context context) {
        return "人脸";
    }

    public boolean hasEnrolledFaces() {
        return this.mFaceAuthenticationManager.hasEnrolledFace();
    }

    public boolean isHardwareDetected() {
        FaceAuthenticationManager.FaceRecognitionAbility ability = this.mFaceAuthenticationManager.getFaceRecognitionAbility();
        return ability.faceMode == 4 && ability.secureLevel == 1;
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
        this.mFaceAuthenticationManager.authenticate(hwcrypto, cancel, 5, new FaceAuthenticationManager.AuthenticationCallback() {
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
