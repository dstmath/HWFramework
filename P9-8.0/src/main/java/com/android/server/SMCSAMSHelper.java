package com.android.server;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.server.am.HwActivityManagerService;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.android.smcs.SmartTrimProcessAddRelation;
import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.android.smcs.SmartTrimProcessPkgResume;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public final class SMCSAMSHelper {
    private static final boolean DBG = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private static final boolean DBG_PERFORMANCE = DBG;
    static final int SMCS_AMS_GET_MAX_OOM_MIN_FREE = 6;
    private static final int SMCS_AMS_GET_RUNNING_PROCESS = 1;
    static final int SMCS_AMS_MOVE_SELF_AUTO_TRIMED_PROCS = 5;
    private static final int SMCS_AMS_MOVE_ST_EVENTS = 2;
    private static final int SMCS_AMS_TRIM_PKGS = 4;
    private static final int SMCS_AMS_TRIM_PROCESSES = 3;
    static final String SMCS_TRIM_TYPE_SELF_AUTO = "trimer_self_auto";
    static final String SMCS_TRIM_TYPE_USER_ONE_SHOOT = "trimer_user_one_shoot";
    private static final int STP_EVENT_MAX_NUM = SystemProperties.getInt("ro.smart_trim.stpe_num", 200);
    private static final String TAG = "SMCSAMSHelper";
    private static SMCSAMSHelper mSelf = null;
    HwActivityManagerService mAms = HwActivityManagerService.self();
    private ArrayList<SmartTrimProcessEvent> mSTPEvent = new ArrayList();
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

    void setAlarmService(AlarmManagerService alarmService) {
    }

    public boolean handleTransact(Parcel data, Parcel reply, int flag) {
        boolean res = true;
        if (data == null) {
            return false;
        }
        int iEvent = data.readInt();
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleTransact: " + iEvent);
        }
        switch (iEvent) {
            case 1:
                handleGetRunningProcesses(data, reply, flag);
                break;
            case 2:
                handleMoveSTEvents(data, reply, flag);
                break;
            case 3:
                handleTrimProcesses(data, reply, flag);
                break;
            case 4:
                handleTrimPkgs(data, reply, flag);
                break;
            case 5:
                handleMoveSelfAutoTrimedProcs(data, reply);
                break;
            case 6:
                handleGetMaxOomMinFree(reply);
                break;
            default:
                res = false;
                break;
        }
        return res;
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

    public void smartTrimAddProcessRelation(String clientProc, int clientCurAdj, ArrayMap<String, ProcessStateHolder> clientPkgMap, String serverProc, int serverCurAdj, ArrayMap<String, ProcessStateHolder> serverPkgMap) {
        try {
            int i;
            HashSet<String> clientPkgList = new HashSet();
            HashSet<String> serverPkgList = new HashSet();
            for (i = 0; i < clientPkgMap.size(); i++) {
                clientPkgList.add((String) clientPkgMap.keyAt(i));
            }
            for (i = 0; i < serverPkgMap.size(); i++) {
                serverPkgList.add((String) serverPkgMap.keyAt(i));
            }
            long timeStart = -1;
            if (DBG_PERFORMANCE) {
                timeStart = System.currentTimeMillis();
            }
            if (clientProc == null || clientProc.length() == 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the client process is invalide");
                }
            } else if (clientCurAdj < 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the client process curAdj is too small. " + clientProc + " " + clientCurAdj);
                }
            } else if (clientPkgList.size() == 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the client process " + clientProc + " has no pkgs.");
                }
            } else if (serverProc == null || serverProc.length() == 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the service process is invalide");
                }
            } else if (serverCurAdj < 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the service curAdj is too small. serviceProc.curAdj " + serverProc + serverCurAdj);
                }
            } else if (serverPkgList.size() == 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: the server process " + serverProc + " has no pkgs.");
                }
            } else {
                if (DBG) {
                    Log.v(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: clientProc " + clientProc + " serviceProc " + serverProc);
                }
                if (!clientProc.equals(serverProc)) {
                    addSTPEvent(new SmartTrimProcessAddRelation(clientProc, clientPkgList, serverProc, serverPkgList));
                    if (DBG_PERFORMANCE) {
                        Log.v(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SMCSAMSHelper.smartTrimAddProcessRelation: catch exception: " + e.toString());
        }
    }

    public void smartTrimProcessPackageResume(ComponentName cn, String processName) {
        Exception e;
        long timeStart = -1;
        try {
            if (DBG_PERFORMANCE) {
                timeStart = System.currentTimeMillis();
            }
            if (cn == null) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: the component name is invalid.");
                }
            } else if (processName == null || processName.length() == 0) {
                if (DBG) {
                    Log.e(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: invalid process name");
                }
            } else {
                String sPkg = cn.getPackageName();
                if (sPkg == null || sPkg.length() == 0) {
                    if (DBG) {
                        Log.e(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: the component name has not pkg name.");
                    }
                    return;
                }
                SmartTrimProcessPkgResume e2 = new SmartTrimProcessPkgResume(sPkg, processName);
                try {
                    addSTPEvent(e2);
                    if (DBG_PERFORMANCE) {
                        Log.v(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: cost time " + (System.currentTimeMillis() - timeStart) + " ms end.");
                    }
                    SmartTrimProcessPkgResume smartTrimProcessPkgResume = e2;
                } catch (Exception e3) {
                    e = e3;
                    Log.e(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: catch exception: " + e.toString());
                }
            }
        } catch (Exception e4) {
            e = e4;
            Log.e(TAG, "SMCSAMSHelper.smartTrimProcessPackageResume: catch exception: " + e.toString());
        }
    }

    private void addSTPEvent(SmartTrimProcessEvent e) {
        long timeStart = -1;
        try {
            if (DBG_PERFORMANCE) {
                timeStart = System.currentTimeMillis();
            }
            synchronized (this.mSTPEvent) {
                if (e != null) {
                    if (this.mSTPEvent.size() >= STP_EVENT_MAX_NUM) {
                        if (DBG) {
                            Log.e(TAG, "SMCSAMSHelper.addSTPEvent: the number of the stp events has reached the upper limit.");
                        }
                        this.mSTPEvent.remove(0);
                    }
                    if (DBG) {
                        Log.v(TAG, "SMCSAMSHelper.addSTPEvent: add one event. STP_EVENT_MAX_NUM " + STP_EVENT_MAX_NUM);
                    }
                    this.mSTPEvent.add(e);
                }
            }
            if (DBG_PERFORMANCE) {
                Log.v(TAG, "SMCSAMSHelper.addSTPEvent: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        } catch (Exception exce) {
            Log.e(TAG, "SMCSAMSHelper.addSTPEvent: catch exception: " + exce.toString());
        }
    }

    private void handleSelfAutoTrimPostProcess(String trimProc, int uid, String trimType, HashSet<String> pkgList) {
        if (trimProc != null && trimProc.length() > 0) {
            if (this.mSelfAutoTrimedProcs == null) {
                this.mSelfAutoTrimedProcs = new HashSet();
            }
            this.mSelfAutoTrimedProcs.add(trimProc);
        }
        informTrimAlarm(pkgList);
    }

    private void informTrimAlarm(HashSet<String> hashSet) {
    }

    private void handleGetRunningProcesses(Parcel data, Parcel reply, int flag) {
        ArrayList<STProcessRecord> runList = new ArrayList();
        this.mAms.getRunningAppProcessRecord_HwSysM(runList);
        reply.writeTypedList(runList);
    }

    private void handleMoveSTEvents(Parcel data, Parcel reply, int flag) {
        try {
            synchronized (this.mSTPEvent) {
                reply.writeTypedList(this.mSTPEvent);
                this.mSTPEvent.clear();
            }
        } catch (Exception e) {
            Log.e(TAG, "SMCSAMSHelper.handleMoveSTEvents: catch exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void handleTrimProcesses(Parcel data, Parcel reply, int flag) {
        String trimType = data.readString();
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleTrimProcesses: trimType " + trimType);
        }
        if (trimType != null && trimType.length() != 0) {
            if (trimType.equals(SMCS_TRIM_TYPE_SELF_AUTO)) {
                handleSelfAutoTrimProcess(data, flag);
            } else if (trimType.equals(SMCS_TRIM_TYPE_USER_ONE_SHOOT)) {
                handleDirectTrimProcesses(data, reply, flag);
            }
        }
    }

    private void handleTrimPkgs(Parcel data, Parcel reply, int flag) {
        String trimType = data.readString();
        if (trimType == null || trimType.length() == 0) {
            if (DBG) {
                Log.e(TAG, "SMCSAMSHelper.handleTrimPkgs: invalid trim type.");
            }
            return;
        }
        if (trimType.equals(SMCS_TRIM_TYPE_USER_ONE_SHOOT)) {
            handleDirectTrimPkgs(data, reply, flag);
        }
    }

    private void handleSelfAutoTrimProcess(Parcel data, int flag) {
        this.mSelfAutoTrimId = data.readLong();
        String trimProc = data.readString();
        if (DBG) {
            Log.v(TAG, "SMCSAMSHelper.handleSelfAutoTrimProcess: trim process " + trimProc);
        }
        int uid = data.readInt();
        if (trimProc == null || trimProc.length() == 0) {
            if (DBG) {
                Log.e(TAG, "SMCSAMSHelper.handleSelfAutoTrimProcess: invalid trim process name.");
            }
        } else if (uid < 0) {
            if (DBG) {
                Log.e(TAG, "SMCSAMSHelper.handleSelfAutoTrimProcess: invalid trim process uid.");
            }
        } else {
            this.mAms.hwTrimApkPost_HwSysM(trimProc, uid, SMCS_TRIM_TYPE_SELF_AUTO);
        }
    }

    private void handleMoveSelfAutoTrimedProcs(Parcel data, Parcel reply) {
        long trimedId = data.readLong();
        if (trimedId != this.mSelfAutoTrimId) {
            if (DBG) {
                Log.v(TAG, "SMCSAMSHelper.handleMoveSelfAutoTrimedProcs: trimed id is different " + trimedId + " " + this.mSelfAutoTrimId);
            }
            return;
        }
        if (this.mSelfAutoTrimedProcs != null) {
            reply.writeInt(this.mSelfAutoTrimedProcs.size());
            Iterator<String> it = this.mSelfAutoTrimedProcs.iterator();
            while (it.hasNext()) {
                reply.writeString((String) it.next());
            }
            this.mSelfAutoTrimedProcs.clear();
        }
        this.mSelfAutoTrimId = -1;
    }

    private void handleDirectTrimProcesses(Parcel data, Parcel reply, int flag) {
        ArrayList<String> procs = data.createStringArrayList();
        HashSet<String> pkgList = new HashSet();
        this.mAms.hwTrimApk_HwSysM(procs, pkgList);
        ArrayList<String> trimedPkgs = stringChangeH2A(pkgList);
        if (DBG) {
            dumpStrings(trimedPkgs, "SMCSAMSHelper.handleDirectTrimProcesses: trimed pkgs");
        }
        reply.writeStringList(trimedPkgs);
        informTrimAlarm(pkgList);
    }

    private void handleDirectTrimPkgs(Parcel data, Parcel reply, int flag) {
        ArrayList<String> pkgs = data.createStringArrayList();
        this.mAms.hwTrimPkgs_HwSysM(pkgs);
        if (DBG) {
            dumpStrings(pkgs, "SMCSAMSHelper.handleDirectTrimPkgs: trimed pkgs");
        }
        reply.writeStringList(pkgs);
        informTrimAlarm(stringChangeA2H(pkgs));
    }

    private void handleGetMaxOomMinFree(Parcel reply) {
        if (reply != null) {
            reply.writeLong(this.mAms.getHWMemFreeLimit_HwSysM());
        }
    }

    private HashSet<String> stringChangeA2H(ArrayList<String> src) {
        if (src == null || src.size() == 0) {
            return null;
        }
        HashSet<String> dst = new HashSet();
        Iterator<String> it = src.iterator();
        while (it.hasNext()) {
            String sPkg = (String) it.next();
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
        ArrayList<String> dst = new ArrayList();
        Iterator<String> it = src.iterator();
        while (it.hasNext()) {
            dst.add((String) it.next());
        }
        return dst;
    }

    private void dumpStrings(ArrayList<String> strs, String sLog) {
        StringBuffer sb = new StringBuffer();
        sb.append(sLog + ":\n");
        if (strs != null) {
            Iterator<String> it = strs.iterator();
            while (it.hasNext()) {
                sb.append("    " + ((String) it.next()) + "\n");
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
            sb.append(((SmartTrimProcessEvent) events.get(i)).toString());
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
            STProcessRecord stpr = (STProcessRecord) it.next();
            if (stpr != null) {
                sb.append(stpr.toString());
                sb.append("\n");
            }
        }
        Log.v(TAG, sb.toString());
    }
}
