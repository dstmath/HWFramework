package huawei.com.android.internal.widget;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.PasswordMetrics;
import android.content.ContentResolver;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
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
            getLockSettings().setLockCredential(null, -1, null, userHandle);
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
            getLockSettings().setLockCredential(password, 2, savedPassword, userHandle);
            PasswordMetrics metrics = PasswordMetrics.computeForPassword(password);
            int computedQuality = metrics.quality;
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
                dpm.setActivePasswordState(new PasswordMetrics(StubController.PERMISSION_SMSLOG_WRITE, 0), userHandle);
            } else {
                setLong("lockscreen.password_type", (long) (quality > computedQuality ? quality : computedQuality), userHandle);
                if (computedQuality != 0) {
                    int letters = 0;
                    int uppercase = 0;
                    int lowercase = 0;
                    int numbers = 0;
                    int symbols = 0;
                    int nonletter = 0;
                    int leng = password.length();
                    for (int i = 0; i < leng; i++) {
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
                    if (quality <= computedQuality) {
                        quality = computedQuality;
                    }
                    metrics.quality = quality;
                    metrics.length = password.length();
                    metrics.letters = letters;
                    metrics.upperCase = uppercase;
                    metrics.lowerCase = lowercase;
                    metrics.numeric = numbers;
                    metrics.symbols = symbols;
                    metrics.nonLetter = nonletter;
                    dpm.setActivePasswordState(metrics, userHandle);
                } else {
                    dpm.setActivePasswordState(new PasswordMetrics(0, 0), userHandle);
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
                int currentLength = ((hash.length * passwordHistoryLength) + passwordHistoryLength) - 1;
                passwordHistory = passwordHistory.substring(0, currentLength < passwordHistory.length() ? currentLength : passwordHistory.length());
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

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        if (!SystemProperties.getBoolean("ro.config.operator_remote_lock", false)) {
            return false;
        }
        try {
            return getLockSettings().setExtendLockScreenPassword(password, phoneNumber, userHandle);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        if (!SystemProperties.getBoolean("ro.config.operator_remote_lock", false)) {
            return false;
        }
        try {
            return getLockSettings().clearExtendLockScreenPassword(password, userHandle);
        } catch (RemoteException e) {
            return false;
        }
    }
}
