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
    private FaceAuthenticationManager mFaceAuthenticationManager;

    public HwFaceManager(Context context) {
        this.mFaceAuthenticationManager = new FaceAuthenticationManager(context);
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean hasEnrolledFaces() {
        Log.i(TAG, "The hasEnrolledFaces interface is Deprecated,using interface hasEnrolledTemplates.");
        return false;
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean hasEnrolledTemplates() {
        return this.mFaceAuthenticationManager.hasEnrolledFace();
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean isHardwareDetected() {
        return this.mFaceAuthenticationManager.isOpenApiSupported(null);
    }

    @Override // com.huawei.facerecognition.FaceManager
    public void authenticate(final FaceManager.CryptoObject crypto, CancellationSignal cancel, int flags, final FaceManager.AuthenticationCallback callback, Handler handler) {
        if (callback != null) {
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
            }
            FaceAuthenticationManager.AuthenticationCallback authenticationCallback = new FaceAuthenticationManager.AuthenticationCallback() {
                /* class com.huawei.facerecognition.HwFaceManager.AnonymousClass1 */

                @Override // com.huawei.hardware.face.FaceAuthenticationManager.AuthenticationCallback
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    callback.onAuthenticationError(errorCode, errString);
                }

                @Override // com.huawei.hardware.face.FaceAuthenticationManager.AuthenticationCallback
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    callback.onAuthenticationHelp(helpCode, helpString);
                }

                @Override // com.huawei.hardware.face.FaceAuthenticationManager.AuthenticationCallback
                public void onAuthenticationSucceeded(FaceAuthenticationManager.AuthenticationResult result) {
                    callback.onAuthenticationSucceeded(new FaceManager.AuthenticationResult(crypto));
                }

                @Override // com.huawei.hardware.face.FaceAuthenticationManager.AuthenticationCallback
                public void onAuthenticationFailed() {
                    callback.onAuthenticationFailed();
                }
            };
            if (this.mFaceAuthenticationManager.isOpenApiSupported(authenticationCallback)) {
                this.mFaceAuthenticationManager.authenticate(hwcrypto, cancel, flags, authenticationCallback, handler);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an authentication callback");
    }
}
