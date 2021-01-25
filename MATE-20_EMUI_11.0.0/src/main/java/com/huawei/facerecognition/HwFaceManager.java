package com.huawei.facerecognition;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import com.huawei.android.util.SlogEx;
import com.huawei.facerecognition.FaceManager;
import com.huawei.hardware.face.FaceAuthenticationManager;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class HwFaceManager extends FaceManager {
    private static final String TAG = "Facerecognition.FaceManager";
    private FaceAuthenticationManager mFaceAuthenticationManager = null;

    public HwFaceManager(Context context) {
        initFaceAuthenticationManager(context);
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean hasEnrolledFaces() {
        SlogEx.i(TAG, "The hasEnrolledFaces interface is Deprecated, using interface hasEnrolledTemplates.");
        return false;
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean hasEnrolledTemplates() {
        FaceAuthenticationManager faceAuthenticationManager = this.mFaceAuthenticationManager;
        if (faceAuthenticationManager != null) {
            return faceAuthenticationManager.hasEnrolledFace();
        }
        SlogEx.e(TAG, "null face manager for the received context is null.");
        return false;
    }

    @Override // com.huawei.facerecognition.FaceManager
    public boolean isHardwareDetected() {
        FaceAuthenticationManager faceAuthenticationManager = this.mFaceAuthenticationManager;
        if (faceAuthenticationManager != null) {
            return faceAuthenticationManager.isOpenApiSupported(null);
        }
        SlogEx.e(TAG, "null face manager for the received context is null.");
        return false;
    }

    @Override // com.huawei.facerecognition.FaceManager
    public void authenticate(final FaceManager.CryptoObject crypto, CancellationSignal cancel, int flags, final FaceManager.AuthenticationCallback callback, Handler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an authentication callback");
        } else if (this.mFaceAuthenticationManager != null) {
            FaceAuthenticationManager.CryptoObject hwCrypto = null;
            if (crypto != null) {
                Signature signature = crypto.getSignature();
                Cipher cipher = crypto.getCipher();
                Mac mac = crypto.getMac();
                if (signature != null) {
                    hwCrypto = new FaceAuthenticationManager.CryptoObject(signature);
                } else if (cipher != null) {
                    hwCrypto = new FaceAuthenticationManager.CryptoObject(cipher);
                } else if (mac != null) {
                    hwCrypto = new FaceAuthenticationManager.CryptoObject(mac);
                } else {
                    SlogEx.i(TAG, "Not support crypto object");
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
                this.mFaceAuthenticationManager.authenticate(hwCrypto, cancel, flags, authenticationCallback, handler);
            }
        } else {
            throw new IllegalArgumentException("null face manager for the received context is null.");
        }
    }

    private void initFaceAuthenticationManager(Context context) {
        if (context == null) {
            SlogEx.e(TAG, "context is null.");
        } else {
            this.mFaceAuthenticationManager = new FaceAuthenticationManager(context);
        }
    }
}
