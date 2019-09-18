package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.util.List;

public class FingerprintUtils {
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
    private static FingerprintUtils sInstance;
    private static final Object sInstanceLock = new Object();
    private boolean mIsDualFingerprint = false;
    @GuardedBy("this")
    private final SparseArray<FingerprintsUserState> mUsers = new SparseArray<>();
    private final SparseArray<FingerprintsUserStateEx> mUsersEx = new SparseArray<>();

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

    public void setDualFp(boolean isDualFp) {
        this.mIsDualFingerprint = isDualFp;
    }

    public boolean isDualFp() {
        return this.mIsDualFingerprint;
    }

    public List<Fingerprint> getFingerprintsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFingerprints();
    }

    public List<Fingerprint> getFingerprintsForUser(Context ctx, int userId, int deviceIndex) {
        return getDualStateForUser(ctx, userId).getFingerprints(deviceIndex);
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).addFingerprint(fingerId, userId);
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId, int deviceIndex) {
        getDualStateForUser(ctx, userId).addFinerprint(fingerId, userId, deviceIndex);
    }

    public void removeFingerprintIdForUser(Context ctx, int fingerId, int userId) {
        if (this.mIsDualFingerprint) {
            getDualStateForUser(ctx, userId).removeFingerprint(fingerId);
        } else {
            getStateForUser(ctx, userId).removeFingerprint(fingerId);
        }
    }

    public void renameFingerprintForUser(Context ctx, int fingerId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            if (this.mIsDualFingerprint) {
                getDualStateForUser(ctx, userId).renameFingerprint(fingerId, name);
            } else {
                getStateForUser(ctx, userId).renameFingerprint(fingerId, name);
            }
        }
    }

    private FingerprintsUserStateEx getDualStateForUser(Context ctx, int userId) {
        FingerprintsUserStateEx state;
        synchronized (this) {
            state = this.mUsersEx.get(userId);
            if (state == null) {
                state = new FingerprintsUserStateEx(ctx, userId);
                this.mUsersEx.put(userId, state);
            }
        }
        return state;
    }

    private FingerprintsUserState getStateForUser(Context ctx, int userId) {
        FingerprintsUserState state;
        synchronized (this) {
            state = this.mUsers.get(userId);
            if (state == null) {
                state = new FingerprintsUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }
}
