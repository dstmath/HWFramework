package com.huawei.android.widget;

import android.content.Context;
import android.os.RemoteException;
import com.android.internal.widget.LockPatternUtils;

public class HwLockPatternUtilsEx {
    private LockPatternUtils mLockPatternUtils;

    public HwLockPatternUtilsEx(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    public int getKeyguardStoredPasswordQuality(int userHandle) {
        if (this.mLockPatternUtils == null) {
            return 0;
        }
        return this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userHandle);
    }

    public boolean checkPattern(String patternOfString, int userId) {
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    if (this.mLockPatternUtils.getLockSettings().checkCredential(patternOfString, 1, userId, null).getResponseCode() == 0) {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        } catch (RemoteException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }
}
