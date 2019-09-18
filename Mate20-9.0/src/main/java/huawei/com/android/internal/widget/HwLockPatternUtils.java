package huawei.com.android.internal.widget;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.PasswordMetrics;
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
        if (!isHwFallback) {
            setLong("lockscreen.password_type", 0, userHandle);
            setLong("lockscreen.password_type_alternate", 0, userHandle);
        }
    }

    public void saveLockPassword(String password, String savedPassword, int quality, boolean isHwFallback, int userHandle) {
        String passwordHistory;
        int leng;
        int type;
        String str = password;
        int i = quality;
        int i2 = userHandle;
        checkPermission();
        try {
            DevicePolicyManager dpm = getDevicePolicyManager();
            if (str == null || password.length() < 4) {
                throw new IllegalArgumentException("password must not be null and at least of length 4");
            }
            getLockSettings().setLockCredential(str, 2, savedPassword, i, i2);
            PasswordMetrics metrics = PasswordMetrics.computeForPassword(password);
            int computedQuality = metrics.quality;
            if (i2 == 0 && LockPatternUtils.isDeviceEncryptionEnabled()) {
                if (!shouldEncryptWithCredentials(true)) {
                    clearEncryptionPassword();
                } else {
                    boolean numeric = computedQuality == 131072;
                    boolean numericComplex = computedQuality == 196608;
                    if (!numeric) {
                        if (!numericComplex) {
                            type = 0;
                            updateEncryptionPassword(type, str);
                        }
                    }
                    type = 2;
                    updateEncryptionPassword(type, str);
                }
            }
            if (!isHwFallback) {
                int qual = i > computedQuality ? i : computedQuality;
                setLong("lockscreen.password_type", (long) qual, i2);
                if (computedQuality != 0) {
                    int symbols = 0;
                    int leng2 = password.length();
                    int numbers = 0;
                    int nonletter = 0;
                    int lowercase = 0;
                    int uppercase = 0;
                    int letters = 0;
                    int i3 = 0;
                    while (true) {
                        leng = leng2;
                        if (i3 >= leng) {
                            break;
                        }
                        int leng3 = leng;
                        int qual2 = qual;
                        int qual3 = str.charAt(i3);
                        if (qual3 >= 65 && qual3 <= 90) {
                            letters++;
                            uppercase++;
                        } else if (qual3 >= 97 && qual3 <= 122) {
                            letters++;
                            lowercase++;
                        } else if (qual3 < 48 || qual3 > 57) {
                            symbols++;
                            nonletter++;
                        } else {
                            numbers++;
                            nonletter++;
                        }
                        i3++;
                        leng2 = leng3;
                        qual = qual2;
                    }
                    int i4 = qual;
                    metrics.quality = i > computedQuality ? i : computedQuality;
                    metrics.length = password.length();
                    metrics.letters = letters;
                    metrics.upperCase = uppercase;
                    metrics.lowerCase = lowercase;
                    metrics.numeric = numbers;
                    metrics.symbols = symbols;
                    metrics.nonLetter = nonletter;
                    dpm.setActivePasswordState(metrics, i2);
                } else {
                    dpm.setActivePasswordState(new PasswordMetrics(0, 0), i2);
                }
            } else {
                setLong("lockscreen.password_type", 65536, i2);
                dpm.setActivePasswordState(new PasswordMetrics(StubController.PERMISSION_SMSLOG_WRITE, 0), i2);
            }
            String passwordHistory2 = getString("lockscreen.passwordhistory", i2);
            if (passwordHistory2 == null) {
                passwordHistory2 = "";
            }
            int passwordHistoryLength = getRequestedPasswordHistoryLength(i2);
            if (passwordHistoryLength == 0) {
                passwordHistory = "";
            } else {
                String passwordHistory3 = new String(passwordToHash(str, i2), StandardCharsets.UTF_8) + "," + passwordHistory2;
                int currentLength = ((passwordToHash(str, i2).length * passwordHistoryLength) + passwordHistoryLength) - 1;
                passwordHistory = passwordHistory3.substring(0, currentLength < passwordHistory3.length() ? currentLength : passwordHistory3.length());
            }
            setString("lockscreen.passwordhistory", passwordHistory, i2);
            onAfterChangingPassword(i2);
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
        this.mLockoutDeadlines.put(userHandle, 0);
        return 0;
    }

    private final void checkPermission() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "HwLockSettings Write");
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
}
