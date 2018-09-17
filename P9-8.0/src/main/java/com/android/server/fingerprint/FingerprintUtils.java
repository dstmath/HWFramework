package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.util.List;

public class FingerprintUtils {
    private static final long[] FP_ERROR_VIBRATE_PATTERN = new long[]{0, 30, 100, 30};
    private static final long[] FP_SUCCESS_VIBRATE_PATTERN = new long[]{0, (long) SystemProperties.getInt("ro.config.enroll_vibrate_time", 30)};
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FP_ERROR_VIBRATE_PATTERN = 14;
    private static final long[] HW_FP_ERROR_VIBRATE_PATTERN = new long[]{0, 30};
    private static FingerprintUtils sInstance;
    private static final Object sInstanceLock = new Object();
    @GuardedBy("this")
    private final SparseArray<FingerprintsUserState> mUsers = new SparseArray();

    public static FingerprintUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FingerprintUtils();
            }
        }
        return sInstance;
    }

    private FingerprintUtils() {
    }

    public List<Fingerprint> getFingerprintsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFingerprints();
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).addFingerprint(fingerId, userId);
    }

    public void removeFingerprintIdForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).removeFingerprint(fingerId);
    }

    public void renameFingerprintForUser(Context ctx, int fingerId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameFingerprint(fingerId, name);
        }
    }

    public static void vibrateFingerprintError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFingerprintErrorHw(Context context) {
        if (context != null) {
            if (FRONT_FINGERPRINT_NAVIGATION) {
                Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
                if (vibrator != null) {
                    SystemVibrator sysVibrator = (SystemVibrator) vibrator;
                    if (sysVibrator != null) {
                        sysVibrator.hwVibrate(null, 14);
                    }
                }
            } else {
                vibrateFingerprintError(context);
            }
        }
    }

    public static void vibrateFingerprintSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

    private FingerprintsUserState getStateForUser(Context ctx, int userId) {
        FingerprintsUserState state;
        synchronized (this) {
            state = (FingerprintsUserState) this.mUsers.get(userId);
            if (state == null) {
                state = new FingerprintsUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }
}
