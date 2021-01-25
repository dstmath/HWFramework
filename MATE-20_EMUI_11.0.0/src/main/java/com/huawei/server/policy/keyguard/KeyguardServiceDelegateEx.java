package com.huawei.server.policy.keyguard;

import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.huawei.android.internal.policy.IKeyguardDismissCallbackEx;

public class KeyguardServiceDelegateEx {
    private KeyguardServiceDelegate mKeyguardDelegate;

    public void setKeyguardServiceDelegate(KeyguardServiceDelegate keyguardDelegate) {
        this.mKeyguardDelegate = keyguardDelegate;
    }

    public void dismiss(IKeyguardDismissCallbackEx callbackEx, CharSequence message) {
        IKeyguardDismissCallback callback = callbackEx != null ? callbackEx.getIKeyguardDismissCallback() : null;
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.dismiss(callback, message);
        }
    }

    public void doFaceRecognize(boolean isDetect, String reason) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.doFaceRecognize(isDetect, reason);
        }
    }
}
