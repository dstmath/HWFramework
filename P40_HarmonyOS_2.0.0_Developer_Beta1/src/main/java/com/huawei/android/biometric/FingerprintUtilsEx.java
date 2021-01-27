package com.huawei.android.biometric;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import com.android.server.biometrics.fingerprint.FingerprintUtils;
import java.util.ArrayList;
import java.util.List;

public class FingerprintUtilsEx {
    private static final int DEFAULT_CAPACITY = 10;
    public static final int DEVICE_ALL = -1;
    public static final int DEVICE_BACK = 0;
    public static final int DEVICE_UD = 1;
    public static final int MSG_AUTH_ALL = 103;
    public static final int MSG_AUTH_UD = 102;
    public static final int MSG_ENROLL_UD = 101;
    public static final int MSG_ENUMERATE_UD = 105;
    public static final int MSG_GETOLDDATA_UD = 106;
    public static final int MSG_NOTIFY_UD = 100;
    public static final int MSG_REMOVE_ALL = 107;
    public static final int MSG_REMOVE_UD = 104;
    private static FingerprintUtilsEx staticInstance;

    private FingerprintUtilsEx() {
    }

    public static synchronized FingerprintUtilsEx getInstance() {
        FingerprintUtilsEx fingerprintUtilsEx;
        synchronized (FingerprintUtilsEx.class) {
            if (staticInstance == null) {
                staticInstance = new FingerprintUtilsEx();
            }
            fingerprintUtilsEx = staticInstance;
        }
        return fingerprintUtilsEx;
    }

    public List<FingerprintEx> getFingerprintsForUser(Context ctx, int userId, int deviceIndex) {
        List<FingerprintEx> finerprintsExs = new ArrayList<>(10);
        for (Fingerprint fingerprint : FingerprintUtils.getInstance().getFingerprintsForUser(ctx, userId, deviceIndex)) {
            FingerprintEx finger = new FingerprintEx(null, 0, 0, 0);
            finger.setFingerprint(fingerprint);
            finerprintsExs.add(finger);
        }
        return finerprintsExs;
    }

    public List<FingerprintEx> getBiometricsForUser(Context ctx, int userId) {
        List<FingerprintEx> finerprintsExs = new ArrayList<>(10);
        for (Fingerprint fingerprint : FingerprintUtils.getInstance().getBiometricsForUser(ctx, userId)) {
            FingerprintEx finger = new FingerprintEx(null, 0, 0, 0);
            finger.setFingerprint(fingerprint);
            finerprintsExs.add(finger);
        }
        return finerprintsExs;
    }

    public boolean isDualFp() {
        return FingerprintUtils.getInstance().isDualFp();
    }

    public void addBiometricForUser(Context context, int userId, FingerprintEx identifier) {
        FingerprintUtils.getInstance().addBiometricForUser(context, userId, identifier.getFingerprint());
    }

    public void removeFingerprintIdForUser(Context ctx, int fingerId, int userId) {
        FingerprintUtils.getInstance().removeFingerprintIdForUser(ctx, fingerId, userId);
    }

    public void renameFingerprintForUser(Context ctx, int fingerId, int userId, CharSequence name) {
        FingerprintUtils.getInstance().renameFingerprintForUser(ctx, fingerId, userId, name);
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId, int deviceIndex) {
        FingerprintUtils.getInstance().addFingerprintForUser(ctx, fingerId, userId, deviceIndex);
    }

    public void setDualFp(boolean isDualFp) {
        FingerprintUtils.getInstance().setDualFp(isDualFp);
    }
}
