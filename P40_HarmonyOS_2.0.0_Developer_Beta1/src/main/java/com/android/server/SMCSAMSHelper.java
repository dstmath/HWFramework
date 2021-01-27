package com.android.server;

import android.os.Parcel;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import java.util.HashSet;
import java.util.Iterator;

public final class SMCSAMSHelper {
    private static final boolean DBG = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private static final boolean DBG_PERFORMANCE = DBG;
    private static SMCSAMSHelper INSTANCE = null;
    static final int SMCS_AMS_MOVE_SELF_AUTO_TRIMED_PROCS = 5;
    static final String SMCS_TRIM_TYPE_SELF_AUTO = "trimer_self_auto";
    static final String SMCS_TRIM_TYPE_USER_ONE_SHOOT = "trimer_user_one_shoot";
    private static final int STP_EVENT_MAX_NUM = SystemProperties.getInt("ro.smart_trim.stpe_num", 200);
    private static final String TAG = "SMCSAMSHelper";
    HwActivityManagerService mAms = HwActivityManagerService.self();
    private long mSelfAutoTrimId = 0;
    private HashSet<String> mSelfAutoTrimedProcs = null;

    private SMCSAMSHelper() {
    }

    public static SMCSAMSHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SMCSAMSHelper();
        }
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void setAlarmService(AlarmManagerService alarmService) {
    }

    public boolean handleTransact(Parcel data, Parcel reply, int flag) {
        if (data == null) {
            return false;
        }
        int event = data.readInt();
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleTransact: " + event);
        }
        if (event != 5) {
            return false;
        }
        handleMoveSelfAutoTrimedProcs(data, reply);
        return true;
    }

    public void trimProcessPostProcess(String trimProc, int uid, String trimType, HashSet<String> pkgList) {
        long timeStart = -1;
        if (DBG) {
            timeStart = System.currentTimeMillis();
        }
        if (trimType.equals(SMCS_TRIM_TYPE_SELF_AUTO)) {
            handleSelfAutoTrimPostProcess(trimProc, uid, trimType, pkgList);
        } else if (DBG) {
            Log.e(TAG, "SMCSAMSHelper.trimProcessPostProcess: unkonw trim type " + trimType);
        }
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.trimProcessPostProcess: total cost " + (System.currentTimeMillis() - timeStart) + " ms.");
        }
    }

    private void handleSelfAutoTrimPostProcess(String trimProc, int uid, String trimType, HashSet<String> pkgList) {
        if (!TextUtils.isEmpty(trimProc)) {
            if (this.mSelfAutoTrimedProcs == null) {
                this.mSelfAutoTrimedProcs = new HashSet<>();
            }
            this.mSelfAutoTrimedProcs.add(trimProc);
        }
        informTrimAlarm(pkgList);
    }

    private void informTrimAlarm(HashSet<String> hashSet) {
    }

    private void handleMoveSelfAutoTrimedProcs(Parcel data, Parcel reply) {
        long trimedId = data.readLong();
        if (trimedId == this.mSelfAutoTrimId) {
            HashSet<String> hashSet = this.mSelfAutoTrimedProcs;
            if (hashSet != null) {
                reply.writeInt(hashSet.size());
                Iterator<String> it = this.mSelfAutoTrimedProcs.iterator();
                while (it.hasNext()) {
                    reply.writeString(it.next());
                }
                this.mSelfAutoTrimedProcs.clear();
            }
            this.mSelfAutoTrimId = -1;
        } else if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleMoveSelfAutoTrimedProcs: trimed id is different " + trimedId + " " + this.mSelfAutoTrimId);
        }
    }
}
