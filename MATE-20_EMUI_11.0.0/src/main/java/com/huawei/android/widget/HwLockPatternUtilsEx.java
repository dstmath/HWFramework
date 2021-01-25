package com.huawei.android.widget;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.LockPatternUtils;
import java.nio.charset.StandardCharsets;

public class HwLockPatternUtilsEx {
    public static final int AUTH_SOLUTION_STRONG = 1;
    public static final int AUTH_SOLUTION_WEAK = 0;
    private static final int DEFAULT_RETRY_COUNT = 100;
    public static final int PWD_BACKEND_HARDWARE_ERR = 1;
    public static final int PWD_BACKEND_NOT_SEC_CHIP = 10;
    public static final int PWD_BACKEND_OTHER_ERR = 2;
    public static final int PWD_BACKEND_STATUS_OK = 0;
    private static final String TAG = "HwLockPatternUtilsEx";
    private LockPatternUtils mLockPatternUtils;

    public HwLockPatternUtilsEx(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    @Deprecated
    public int getKeyguardStoredPasswordQuality(int userHandle) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils == null) {
            return 0;
        }
        return lockPatternUtils.getKeyguardStoredPasswordQuality(userHandle);
    }

    @Deprecated
    public boolean checkPattern(String pattern, int userId) {
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    if (this.mLockPatternUtils.getLockSettings().checkCredential(pattern != null ? pattern.getBytes(StandardCharsets.UTF_8) : null, 1, userId, (ICheckCredentialProgressCallback) null).getResponseCode() == 0) {
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

    @Deprecated
    public void requireStrongAuth(int strongAuthReason, int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            lockPatternUtils.requireStrongAuth(strongAuthReason, userId);
        }
    }

    public int getRemainAllowedRetryCount(int userId) {
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    return this.mLockPatternUtils.getLockSettings().getRemainAllowedRetryCount(userId);
                }
            }
            Log.w(TAG, "getRemainAllowedRetryCount error mLockPatternUtils is null uId " + userId);
            return 100;
        } catch (RemoteException e) {
            Log.w(TAG, "getRemainAllowedRetryCount error uId " + userId);
            Log.i(TAG, "getRemainAllowedRetryCount userId:" + userId + " retryCount:100");
            return 100;
        } catch (Exception e2) {
            Log.w(TAG, "getRemainAllowedRetryCount exception uId " + userId);
            Log.i(TAG, "getRemainAllowedRetryCount userId:" + userId + " retryCount:100");
            return 100;
        }
    }

    public long getRemainLockedTime(int userId) {
        long lockedTime = 0;
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    lockedTime = this.mLockPatternUtils.getLockSettings().getRemainLockedTime(userId);
                    Log.i(TAG, "getRemainLockedTime userId:" + userId + " lockedTime:" + lockedTime);
                    return lockedTime;
                }
            }
            Log.w(TAG, "getRemainLockedTime error mLockPatternUtils is null uId " + userId);
            return 0;
        } catch (RemoteException e) {
            Log.w(TAG, "getAbsoluteLockedTime error uId " + userId);
        } catch (Exception e2) {
            Log.w(TAG, "getAbsoluteLockedTime exception uId " + userId);
        }
    }

    public int getStrongAuthSolution(int userId) {
        int strongAuthSolution = 0;
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    strongAuthSolution = this.mLockPatternUtils.getLockSettings().getStrongAuthSolution(userId);
                    Log.i(TAG, "getStrongAuthSolution userId:" + userId + " strongAuthSolution:" + strongAuthSolution);
                    return strongAuthSolution;
                }
            }
            Log.w(TAG, "getStrongAuthSolution error mLockPatternUtils is null uId " + userId);
            return 0;
        } catch (RemoteException e) {
            Log.w(TAG, "getStrongAuthSolution error uId " + userId);
        } catch (Exception e2) {
            Log.w(TAG, "getStrongAuthSolution exception uId " + userId);
        }
    }

    public int getPasswordBackendStatus() {
        int status = 0;
        try {
            if (this.mLockPatternUtils != null) {
                if (this.mLockPatternUtils.getLockSettings() != null) {
                    status = this.mLockPatternUtils.getLockSettings().getPasswordBackendStatus();
                    return status;
                }
            }
            Log.w(TAG, "getPasswordBackendStatus error, mLockPatternUtils is null.");
            return 0;
        } catch (RemoteException e) {
            Log.w(TAG, "getPasswordBackendStatus error.");
        } catch (Exception e2) {
            Log.w(TAG, "getPasswordBackendStatus exception.");
        }
    }
}
