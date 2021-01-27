package com.huawei.server.fingerprint;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.biometric.BiometricServiceReceiverListenerEx;
import java.util.Optional;

public class DefaultFingerViewController {
    public static final int TYPE_FINGER_VIEW = 2105;

    public DefaultFingerViewController(Context context) {
    }

    public Optional<Handler> getFingerHandler() {
        return Optional.empty();
    }

    public void setBiometricServiceReceiver(BiometricServiceReceiverListenerEx biometricServiceReceiver) {
    }

    public void setBiometricRequireConfirmation(boolean isBiometricConfirmation) {
    }

    public void notifyTouchUp(float upX, float upY) {
    }

    public void setHighlightViewAlpha(int brightness) {
    }
}
