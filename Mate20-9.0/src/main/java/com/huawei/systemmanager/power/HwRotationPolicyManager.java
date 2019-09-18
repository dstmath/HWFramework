package com.huawei.systemmanager.power;

import android.content.Context;
import com.android.internal.view.RotationPolicy;

public class HwRotationPolicyManager {

    public static class RotationPolicyListenerEx {
        private RotationPolicy.RotationPolicyListener mInnerotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
            public void onChange() {
                RotationPolicyListenerEx.this.onChange();
            }
        };

        public void onChange() {
        }

        public RotationPolicy.RotationPolicyListener getInnerListener() {
            return this.mInnerotationPolicyListener;
        }
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListenerEx listener) {
        if (!checkNull(context, listener)) {
            RotationPolicy.registerRotationPolicyListener(context, listener.getInnerListener());
        }
    }

    private static boolean checkNull(Context context, RotationPolicyListenerEx listener) {
        if (context == null || listener == null) {
            return true;
        }
        return false;
    }

    public static void unregisterRotationPolicyListener(Context context, RotationPolicyListenerEx listener) {
        if (!checkNull(context, listener)) {
            RotationPolicy.unregisterRotationPolicyListener(context, listener.getInnerListener());
        }
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListenerEx listener, int userHandle) {
        if (!checkNull(context, listener)) {
            RotationPolicy.registerRotationPolicyListener(context, listener.getInnerListener(), userHandle);
        }
    }
}
