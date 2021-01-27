package com.android.server.mtm.iaware.srms;

import android.rms.iaware.AwareLog;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.am.HwMtmBroadcastResourceManager;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class AwareBroadcastCache {
    private static final int AWARE_MAX_PROXY_BROADCAST = 60;
    private static final String TAG = "AwareBroadcastCache";
    private final Object mAms;
    private final HashMap<Integer, ArrayList<HwBroadcastRecord>> mAwareOrderedPendingBrMap = new HashMap<>();
    private final HashMap<Integer, ArrayList<HwBroadcastRecord>> mAwareParallelPendingBrMap = new HashMap<>();
    private String mBrName = null;
    private final HashMap<Integer, Integer> mProxyPidsCount = new HashMap<>();
    private HashMap<String, String> mSameKindsActionList = new HashMap<>();

    public AwareBroadcastCache(String name, Object ams) {
        this.mBrName = name;
        this.mAms = ams;
        this.mSameKindsActionList.put("android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF");
    }

    private ArrayList<HwBroadcastRecord> getHwBroadcastRecord(HashMap<Integer, ArrayList<HwBroadcastRecord>> broadcastRecordMap, int pid) {
        ArrayList<HwBroadcastRecord> awareBroadcasts;
        synchronized (this.mAms) {
            if (broadcastRecordMap.containsKey(Integer.valueOf(pid))) {
                awareBroadcasts = broadcastRecordMap.get(Integer.valueOf(pid));
            } else {
                awareBroadcasts = new ArrayList<>();
                broadcastRecordMap.put(Integer.valueOf(pid), awareBroadcasts);
            }
        }
        return awareBroadcasts;
    }

    public boolean awareTrimAndEnqueueBr(boolean[] broadcastInfo, HwBroadcastRecord record, int pid, String pkgName, AwareBroadcastPolicy policy) {
        if (broadcastInfo == null || broadcastInfo.length != 2 || record == null || pkgName == null) {
            return false;
        }
        boolean isParallel = broadcastInfo[0];
        boolean notify = broadcastInfo[1];
        boolean trim = false;
        notifyPgUnFreeze(record, notify, pid, pkgName);
        int count = 0;
        if (this.mProxyPidsCount.containsKey(Integer.valueOf(pid))) {
            count = this.mProxyPidsCount.get(Integer.valueOf(pid)).intValue();
        }
        if (isParallel) {
            ArrayList<HwBroadcastRecord> awareParallelPendingBroadcasts = getHwBroadcastRecord(this.mAwareParallelPendingBrMap, pid);
            Iterator<HwBroadcastRecord> it = awareParallelPendingBroadcasts.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwBroadcastRecord br = it.next();
                if (!notify && canTrim(record, br)) {
                    it.remove();
                    count--;
                    AwareBroadcastDumpRadar.increaseBrAfterCount(1);
                    trim = true;
                    break;
                }
            }
            awareParallelPendingBroadcasts.add(record);
        } else {
            ArrayList<HwBroadcastRecord> awareOrderedPendingBroadcasts = getHwBroadcastRecord(this.mAwareOrderedPendingBrMap, pid);
            Iterator<HwBroadcastRecord> it2 = awareOrderedPendingBroadcasts.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                HwBroadcastRecord br2 = it2.next();
                if (!notify && canTrim(record, br2)) {
                    it2.remove();
                    count--;
                    AwareBroadcastDumpRadar.increaseBrAfterCount(1);
                    trim = true;
                    break;
                }
            }
            awareOrderedPendingBroadcasts.add(record);
        }
        int count2 = count + 1;
        this.mProxyPidsCount.put(Integer.valueOf(pid), Integer.valueOf(count2));
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "trim and enqueue " + this.mBrName + " pid:" + pid + " count:" + count2 + ")(" + record + ")");
        }
        notifyPgOverFlowUnFreeze(pid, pkgName, count2, policy);
        return trim;
    }

    private void notifyPgUnFreeze(HwBroadcastRecord record, boolean notify, int pid, String pkgName) {
        if (notify) {
            LogPower.push((int) CpuFeature.MSG_EXIT_GAME_SCENE, record.getAction(), pkgName, String.valueOf(pid), new String[]{record.getCallerPackage()});
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "enqueueProxyBroadcast notify pg broadcast:" + record.getAction() + " pkg:" + pkgName + " pid:" + pid);
            }
        }
    }

    private void notifyPgOverFlowUnFreeze(int pid, String pkgName, int count, AwareBroadcastPolicy policy) {
        if (count >= 60 && policy != null) {
            policy.notifyOverFlow(pid);
        }
    }

    private boolean canTrim(HwBroadcastRecord r1, HwBroadcastRecord r2) {
        if (r1 == null || r2 == null || r1.getIntent() == null || r2.getIntent() == null || r1.getAction() == null || r2.getAction() == null) {
            return false;
        }
        Object o1 = r1.getBrReceivers().get(0);
        Object o2 = r2.getBrReceivers().get(0);
        String pkg1 = r1.getReceiverPkg();
        String pkg2 = r2.getReceiverPkg();
        if ((pkg1 != null && !pkg1.equals(pkg2)) || !HwMtmBroadcastResourceManager.isSameReceiver(o1, o2)) {
            return false;
        }
        String action1 = r1.getAction();
        String action2 = r2.getAction();
        if (action1 != null && action1.equals(action2)) {
            return true;
        }
        String a1 = this.mSameKindsActionList.get(action1);
        String a2 = this.mSameKindsActionList.get(action2);
        if ((a1 == null || !a1.equals(action2)) && (a2 == null || !a2.equals(action1))) {
            return false;
        }
        return true;
    }

    public void unproxyCacheBr(int pid) {
        synchronized (this.mAms) {
            Collection<? extends HwBroadcastRecord> parallelBrs = (ArrayList) this.mAwareParallelPendingBrMap.get(Integer.valueOf(pid));
            Collection<? extends HwBroadcastRecord> orderedBrs = (ArrayList) this.mAwareOrderedPendingBrMap.get(Integer.valueOf(pid));
            if (parallelBrs != null || orderedBrs != null) {
                ArrayList<HwBroadcastRecord> awareParallelBrs = new ArrayList<>();
                ArrayList<HwBroadcastRecord> awareOrderedBrs = new ArrayList<>();
                if (parallelBrs != null) {
                    awareParallelBrs.addAll(parallelBrs);
                }
                if (orderedBrs != null) {
                    awareOrderedBrs.addAll(orderedBrs);
                }
                this.mAwareParallelPendingBrMap.remove(Integer.valueOf(pid));
                this.mAwareOrderedPendingBrMap.remove(Integer.valueOf(pid));
                this.mProxyPidsCount.remove(Integer.valueOf(pid));
                HwMtmBroadcastResourceManager.unProxyCachedBr(awareParallelBrs, awareOrderedBrs);
            }
        }
    }

    public void clearCacheBr(int pid) {
        synchronized (this.mAms) {
            this.mAwareParallelPendingBrMap.remove(Integer.valueOf(pid));
            this.mAwareOrderedPendingBrMap.remove(Integer.valueOf(pid));
            this.mProxyPidsCount.remove(Integer.valueOf(pid));
        }
    }
}
