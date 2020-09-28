package com.huawei.android.app;

import android.content.Context;
import android.util.Log;
import huawei.android.os.HwGeneralManager;

public class HwSdLockManager {
    public static final String ACTION_LOCKED_SD_AUTO_UNLOCK_FAILED = "android.intent.action.HWSDLOCK_AUTO_UNLOCK_FAILED";
    public static final String ACTION_LOCKED_SD_CLEAR_COMPLETED = "android.intent.action.HWSDLOCK_CLEAR_COMPLETED";
    public static final String ACTION_LOCKED_SD_FORCE_ERASE_COMPLETED = "android.intent.action.HWSDLOCK_FORCE_ERASE_COMPLETED";
    public static final String ACTION_LOCKED_SD_SET_PWD_COMPLETED = "android.intent.action.HWSDLOCK_SET_PWD_COMPLETED";
    public static final String ACTION_LOCKED_SD_UNLOCK_COMPLETED = "android.intent.action.HWSDLOCK_UNLOCK_COMPLETED";
    public static final int RESPONSE_CODE_OK = 200;
    private static final String TAG = "SdlockManager";
    public static final int VOLUME_DISK_ERASING = 645;
    public static final int VOLUME_DISK_LOCKED = 670;
    public static final int VOLUME_DISK_UNENCRYPTED = 639;
    public static final int VOLUME_DISK_UNLOCKED = 671;
    private HwGeneralManager mSdLockServiceWrapper = HwGeneralManager.getInstance();

    public HwSdLockManager(Context context) {
        if (this.mSdLockServiceWrapper == null) {
            Log.e(TAG, "The SdlockManager object is not ready.");
        }
    }

    public int setSDLockPassword(String pw) {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            return hwGeneralManager.setSDLockPassword(pw);
        }
        return -1;
    }

    public int clearSDLockPassword() {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            return hwGeneralManager.clearSDLockPassword();
        }
        return -1;
    }

    public int unlockSDCard(String pw) {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            return hwGeneralManager.unlockSDCard(pw);
        }
        return -1;
    }

    public void eraseSDLock() {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            hwGeneralManager.eraseSDLock();
        }
    }

    public int getSDLockState() {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            return hwGeneralManager.getSDLockState();
        }
        return -1;
    }

    public String getSDCardId() {
        HwGeneralManager hwGeneralManager = this.mSdLockServiceWrapper;
        if (hwGeneralManager != null) {
            return hwGeneralManager.getSDCardId();
        }
        return null;
    }
}
