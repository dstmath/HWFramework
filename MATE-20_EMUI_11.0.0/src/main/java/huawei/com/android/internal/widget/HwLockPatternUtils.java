package huawei.com.android.internal.widget;

import android.app.ActivityManager;
import android.app.admin.PasswordMetrics;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;

public class HwLockPatternUtils extends LockPatternUtils {
    private static final String KEY_HW_PIN_TYPE = "lockscreen.pin_type";
    private static final int KEY_UNLOCK_SET_PASSWORD = 3;
    private static final int KEY_UNLOCK_SET_PATTERN = 4;
    private static final int KEY_UNLOCK_SET_PIN_FIX = 2;
    private static final int KEY_UNLOCK_SET_PIN_FIX4 = 0;
    private static final int KEY_UNLOCK_SET_PIN_FIX6 = 1;
    private static final int KEY_UNLOCK_TYPE_NOT_SET = -1;
    private static final int PASSWORD_FIX_FOUR = 4;
    private static final int PASSWORD_FIX_SIX = 6;
    private static final String PERMISSION = "com.huawei.locksettings.permission.ACCESS_HWKEYGUARD_SECURE_STORAGE";
    private static final String TAG = "HwLockPatternUtils";
    public static final int transaction_setActiveVisitorPasswordState = 1003;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    public HwLockPatternUtils(Context context) {
        super(context);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public long resetLockoutDeadline() {
        checkPermission();
        return resetLockoutDeadline(ActivityManager.getCurrentUser());
    }

    public long resetLockoutDeadline(int userHandle) {
        checkPermission();
        this.mLockoutDeadlines.put(userHandle, 0);
        return 0;
    }

    private void checkPermission() {
        if (Binder.getCallingPid() == Process.myPid()) {
            Log.i(TAG, "Self calling needn't check permission.");
        } else if (this.mContext.checkCallingPermission(PERMISSION) != 0) {
            throw new SecurityException("HwLockSettings Write");
        }
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        try {
            return getLockSettings().setExtendLockScreenPassword(password, phoneNumber, userHandle);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        try {
            return getLockSettings().clearExtendLockScreenPassword(password, userHandle);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean saveLockPassword(byte[] passwords, byte[] savedPasswords, int requestedQuality, int userHandle, boolean isUntrustedChangeAllowed, boolean isFixed) {
        try {
            boolean isResultSaved = HwLockPatternUtils.super.saveLockPassword(passwords, savedPasswords, requestedQuality, userHandle, isUntrustedChangeAllowed);
            if (!isResultSaved) {
                return isResultSaved;
            }
            writeLockScreenPinType(passwords, requestedQuality, userHandle, isFixed);
            return isResultSaved;
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            Log.e(TAG, "save lock password failed");
            return false;
        }
    }

    private void writeLockScreenPinType(byte[] passwords, int requestedQuality, int userHandle, boolean isFixed) {
        try {
            getLockSettings().setLong(KEY_HW_PIN_TYPE, getLockScreenPinType(passwords, computeKeyguardQuality(2, requestedQuality, PasswordMetrics.computeForPassword(passwords).quality), isFixed), userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "set lockscreen_pin_type failed");
        }
    }

    private long getLockScreenPinType(byte[] passwords, int requestedQuality, boolean isFixed) {
        if (requestedQuality == 0) {
            return -1;
        }
        if (requestedQuality == 65536) {
            return 4;
        }
        if (requestedQuality == 131072 || requestedQuality == 196608) {
            if (!isFixed) {
                return 2;
            }
            int length = passwords.length;
            if (length == 4) {
                return 0;
            }
            if (length == 6) {
                return 1;
            }
            return 2;
        } else if (requestedQuality == 262144 || requestedQuality == 327680 || requestedQuality == 393216 || requestedQuality == 524288) {
            return 3;
        } else {
            throw new IllegalStateException("Unknown security quality:" + requestedQuality);
        }
    }
}
