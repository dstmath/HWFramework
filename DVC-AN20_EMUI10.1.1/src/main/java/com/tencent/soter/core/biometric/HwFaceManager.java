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
    private static final int SOTER_FLAG = 5;
    private FaceAuthenticationManager mFaceAuthenticationManager;

    public HwFaceManager(Context context) {
        this.mFaceAuthenticationManager = new FaceAuthenticationManager(context);
    }

    @Override // com.tencent.soter.core.biometric.FaceManager
    public String getBiometricName(Context context) {
        return "人脸";
    }

    @Override // com.tencent.soter.core.biometric.FaceManager
    public boolean hasEnrolledFaces() {
        return this.mFaceAuthenticationManager.hasEnrolledFace();
    }

    @Override // com.tencent.soter.core.biometric.FaceManager
    public boolean isHardwareDetected() {
        return this.mFaceAuthenticationManager.isSoterApiSupported(null);
    }

    @Override // com.tencent.soter.core.biometric.FaceManager
    public void authenticate(final FaceManager.CryptoObject crypto, CancellationSignal cancel, int flags, final FaceManager.AuthenticationCallback callback, Handler handler) {
        if (callback != null) {
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
                }
            }
            FaceAuthenticationManager.AuthenticationCallback soterCallback = new FaceAuthenticationManager.AuthenticationCallback() {
                /* class com.tencent.soter.core.biometric.HwFaceManager.AnonymousClass1 */

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
            if (this.mFaceAuthenticationManager.isSoterApiSupported(soterCallback)) {
                this.mFaceAuthenticationManager.authenticate(hwCrypto, cancel, 5, soterCallback, handler);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an authentication callback");
    }
}
