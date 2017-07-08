package huawei.com.android.internal.widget;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.hsm.permission.StubController;
import java.nio.charset.StandardCharsets;

public class HwLockPatternUtils extends LockPatternUtils {
    private static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    private static final String PERMISSION = "com.huawei.locksettings.permission.ACCESS_HWKEYGUARD_SECURE_STORAGE";
    private static final String TAG = "HwLockPatternUtils";
    private static final int transaction_checkvisitorpassword = 1002;
    public static final int transaction_setActiveVisitorPasswordState = 1003;
    private static final int transaction_setlockvisitorpassword = 1001;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    public HwLockPatternUtils(Context context) {
        super(context);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public void clearLockEx(boolean isFallback, boolean isHwFallback) {
        clearLockEx(isHwFallback, ActivityManager.getCurrentUser());
    }

    public void clearLockEx(boolean isHwFallback, int userHandle) {
        checkPermission();
        try {
            getLockSettings().setLockPassword(null, null, userHandle);
            getLockSettings().setLockPattern(null, null, userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "clearLockEx error ");
        }
        if (!isHwFallback) {
            setLong("lockscreen.password_type", 0, userHandle);
            setLong("lockscreen.password_type_alternate", 0, userHandle);
        }
    }

    public void saveLockPassword(String password, String savedPassword, int quality, boolean isHwFallback, int userHandle) {
        checkPermission();
        try {
            DevicePolicyManager dpm = getDevicePolicyManager();
            if (password == null || password.length() < 4) {
                throw new IllegalArgumentException("password must not be null and at least of length 4");
            }
            getLockSettings().setLockPassword(password, savedPassword, userHandle);
            int computedQuality = computePasswordQuality(password);
            if (userHandle == 0 && LockPatternUtils.isDeviceEncryptionEnabled()) {
                if (shouldEncryptWithCredentials(true)) {
                    int type;
                    boolean numeric = computedQuality == 131072;
                    boolean numericComplex = computedQuality == 196608;
                    if (numeric || numericComplex) {
                        type = 3;
                    } else {
                        type = 0;
                    }
                    updateEncryptionPassword(type, password);
                } else {
                    clearEncryptionPassword();
                }
            }
            if (isHwFallback) {
                setLong("lockscreen.password_type", 65536, userHandle);
                dpm.setActivePasswordState(StubController.PERMISSION_SMSLOG_WRITE, 0, 0, 0, 0, 0, 0, 0, userHandle);
            } else {
                setLong("lockscreen.password_type", (long) Math.max(quality, computedQuality), userHandle);
                if (computedQuality != 0) {
                    int letters = 0;
                    int uppercase = 0;
                    int lowercase = 0;
                    int numbers = 0;
                    int symbols = 0;
                    int nonletter = 0;
                    for (int i = 0; i < password.length(); i++) {
                        char c = password.charAt(i);
                        if (c >= 'A' && c <= 'Z') {
                            letters++;
                            uppercase++;
                        } else if (c >= 'a' && c <= 'z') {
                            letters++;
                            lowercase++;
                        } else if (c < '0' || c > '9') {
                            symbols++;
                            nonletter++;
                        } else {
                            numbers++;
                            nonletter++;
                        }
                    }
                    dpm.setActivePasswordState(Math.max(quality, computedQuality), password.length(), letters, uppercase, lowercase, numbers, symbols, nonletter, userHandle);
                } else {
                    dpm.setActivePasswordState(0, 0, 0, 0, 0, 0, 0, 0, userHandle);
                }
            }
            String passwordHistory = getString("lockscreen.passwordhistory", userHandle);
            if (passwordHistory == null) {
                passwordHistory = "";
            }
            int passwordHistoryLength = getRequestedPasswordHistoryLength(userHandle);
            if (passwordHistoryLength == 0) {
                passwordHistory = "";
            } else {
                byte[] hash = passwordToHash(password, userHandle);
                passwordHistory = new String(hash, StandardCharsets.UTF_8) + "," + passwordHistory;
                passwordHistory = passwordHistory.substring(0, Math.min(((hash.length * passwordHistoryLength) + passwordHistoryLength) - 1, passwordHistory.length()));
            }
            setString("lockscreen.passwordhistory", passwordHistory, userHandle);
            onAfterChangingPassword(userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Unable to save lock password " + re);
        }
    }

    public long resetLockoutDeadline() {
        checkPermission();
        return resetLockoutDeadline(ActivityManager.getCurrentUser());
    }

    public long resetLockoutDeadline(int userHandle) {
        checkPermission();
        setLong("lockscreen.lockoutattemptdeadline", 0, userHandle);
        return 0;
    }

    private final void checkPermission() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "HwLockSettings Write");
    }
}
