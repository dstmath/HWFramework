package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import java.util.HashMap;

public class RadioBugDetector {
    private static final int DEFAULT_SYSTEM_ERROR_COUNT_THRESHOLD = 100;
    private static final int DEFAULT_WAKELOCK_TIMEOUT_COUNT_THRESHOLD = 10;
    private static final int RADIO_BUG_NONE = 0;
    @VisibleForTesting
    protected static final int RADIO_BUG_REPETITIVE_SYSTEM_ERROR = 2;
    private static final int RADIO_BUG_REPETITIVE_WAKELOCK_TIMEOUT_ERROR = 1;
    private Context mContext;
    private int mContinuousWakelockTimoutCount = 0;
    private int mRadioBugStatus = 0;
    private int mSlotId;
    private HashMap<Integer, Integer> mSysErrRecord = new HashMap<>();
    private int mSystemErrorThreshold = 0;
    private int mWakelockTimeoutThreshold = 0;

    public RadioBugDetector(Context context, int slotId) {
        this.mContext = context;
        this.mSlotId = slotId;
        init();
    }

    private void init() {
        this.mWakelockTimeoutThreshold = Settings.Global.getInt(this.mContext.getContentResolver(), "radio_bug_wakelock_timeout_count_threshold", 10);
        this.mSystemErrorThreshold = Settings.Global.getInt(this.mContext.getContentResolver(), "radio_bug_system_error_count_threshold", 100);
    }

    public synchronized void detectRadioBug(int requestType, int error) {
        this.mContinuousWakelockTimoutCount = 0;
        if (error == 39) {
            this.mSysErrRecord.put(Integer.valueOf(requestType), Integer.valueOf(this.mSysErrRecord.getOrDefault(Integer.valueOf(requestType), 0).intValue() + 1));
            broadcastBug(true);
        } else {
            this.mSysErrRecord.remove(Integer.valueOf(requestType));
            if (!isFrequentSystemError()) {
                this.mRadioBugStatus = 0;
            }
        }
    }

    public void processWakelockTimeout() {
        this.mContinuousWakelockTimoutCount++;
        broadcastBug(false);
    }

    private synchronized void broadcastBug(boolean isSystemError) {
        int i;
        if (isSystemError) {
            if (!isFrequentSystemError()) {
                return;
            }
        } else if (this.mContinuousWakelockTimoutCount < this.mWakelockTimeoutThreshold) {
            return;
        }
        if (this.mRadioBugStatus == 0) {
            if (isSystemError) {
                i = 2;
            } else {
                i = 1;
            }
            this.mRadioBugStatus = i;
            Intent intent = new Intent("com.android.internal.telephony.ACTION_REPORT_RADIO_BUG");
            intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
            intent.putExtra("slotId", this.mSlotId);
            intent.putExtra("radioBugType", this.mRadioBugStatus);
            this.mContext.sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        }
    }

    private boolean isFrequentSystemError() {
        int countForError = 0;
        for (Integer num : this.mSysErrRecord.values()) {
            countForError += num.intValue();
            if (countForError >= this.mSystemErrorThreshold) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    public int getRadioBugStatus() {
        return this.mRadioBugStatus;
    }

    @VisibleForTesting
    public int getWakelockTimeoutThreshold() {
        return this.mWakelockTimeoutThreshold;
    }

    @VisibleForTesting
    public int getSystemErrorThreshold() {
        return this.mSystemErrorThreshold;
    }

    @VisibleForTesting
    public int getWakelockTimoutCount() {
        return this.mContinuousWakelockTimoutCount;
    }
}
