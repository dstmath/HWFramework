package com.android.server;

import android.os.Parcel;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.android.smcs.SmartTrimProcessEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public final class SMCSAMSHelper {
    private static final boolean DBG = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private static final boolean DBG_PERFORMANCE = DBG;
    static final int SMCS_AMS_MOVE_SELF_AUTO_TRIMED_PROCS = 5;
    static final String SMCS_TRIM_TYPE_SELF_AUTO = "trimer_self_auto";
    static final String SMCS_TRIM_TYPE_USER_ONE_SHOOT = "trimer_user_one_shoot";
    private static final int STP_EVENT_MAX_NUM = SystemProperties.getInt("ro.smart_trim.stpe_num", 200);
    private static final String TAG = "SMCSAMSHelper";
    private static SMCSAMSHelper mSelf = null;
    HwActivityManagerService mAms = HwActivityManagerService.self();
    private long mSelfAutoTrimId = 0;
    private HashSet<String> mSelfAutoTrimedProcs = null;

    private SMCSAMSHelper() {
    }

    public static SMCSAMSHelper getInstance() {
        if (mSelf == null) {
            mSelf = new SMCSAMSHelper();
        }
        return mSelf;
    }

    /* access modifiers changed from: package-private */
    public void setAlarmService(AlarmManagerService alarmService) {
    }

    public boolean handleTransact(Parcel data, Parcel reply, int flag) {
        if (data == null) {
            return false;
        }
        int iEvent = data.readInt();
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleTransact: " + iEvent);
        }
        if (iEvent != 5) {
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
        if (trimProc != null && trimProc.length() > 0) {
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

    private HashSet<String> stringChangeA2H(ArrayList<String> src) {
        if (src == null || src.size() == 0) {
            return null;
        }
        HashSet<String> dst = new HashSet<>();
        Iterator<String> it = src.iterator();
        while (it.hasNext()) {
            String sPkg = it.next();
            if (sPkg != null && sPkg.length() > 0) {
                dst.add(sPkg);
            }
        }
        return dst;
    }

    private ArrayList<String> stringChangeH2A(HashSet<String> src) {
        if (src == null || src.size() == 0) {
            return null;
        }
        ArrayList<String> dst = new ArrayList<>();
        Iterator<String> it = src.iterator();
        while (it.hasNext()) {
            dst.add(it.next());
        }
        return dst;
    }

    private void dumpStrings(ArrayList<String> strs, String sLog) {
        StringBuffer sb = new StringBuffer();
        sb.append(sLog + ":\n");
        if (strs != null) {
            Iterator<String> it = strs.iterator();
            while (it.hasNext()) {
                String s = it.next();
                sb.append("    " + s + "\n");
            }
            Log.v(TAG, sb.toString());
        }
    }

    private void dumpSmartTrimProcessEvent(ArrayList<SmartTrimProcessEvent> events, String sLog) {
        StringBuffer sb = new StringBuffer();
        if (events == null || events.size() == 0) {
            Log.v(TAG, sLog + ": empty events");
            return;
        }
        sb.append(sLog + "\n");
        int size = events.size();
        sb.append(sLog + ": total " + size + " events.");
        for (int i = 0; i < size; i++) {
            sb.append(events.get(i).toString());
            sb.append("\n");
        }
        Log.v(TAG, sb.toString());
    }

    private void dumpSTProcessRecords(ArrayList<STProcessRecord> stProcessRecords, String sLog) {
        if (stProcessRecords == null || stProcessRecords.size() == 0) {
            Log.v(TAG, sLog + " empty process records.");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(sLog + "\n");
        Iterator<STProcessRecord> it = stProcessRecords.iterator();
        while (it.hasNext()) {
            STProcessRecord stpr = it.next();
            if (stpr != null) {
                sb.append(stpr.toString());
                sb.append("\n");
            }
        }
        Log.v(TAG, sb.toString());
    }
}
