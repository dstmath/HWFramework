package com.android.server.rms.iaware.cpu;

import android.iawareperf.RtgSchedController;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.Pair;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.huawei.android.os.ProcessExt;
import com.huawei.server.AnimationThreadEx;
import com.huawei.server.wm.SurfaceAnimationThreadEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuxRtgSched {
    private static final String AMS = "ams";
    private static final String ANIMATOR = "animator";
    private static final String ANIMATOR_LF = "animator_lf";
    private static final int ANIM_LF_TID = SurfaceAnimationThreadEx.getThreadId();
    private static final int ANIM_TID = AnimationThreadEx.getThreadId();
    private static final int AUX_FORK_DELAY = 100;
    private static final int AUX_RTG_SCHED_TIME = 1500;
    private static final String BACKGROUND = "background";
    private static final int CGROUP_REFRESH_DELAY = 500;
    private static final String CPUSET = "cpuset";
    private static final int INVALID_VALUE = -1;
    private static final int MAX_KEY_THREAD_COUNT_PER_APP = 9;
    private static final int MAX_KEY_THREAD_COUNT_PER_PROC = 9;
    private static final String RMS = "rms";
    private static final int RMS_TID = Process.myTid();
    private static final Object SLOCK = new Object();
    private static final int SYSTEM_SERVER_PID = Process.myPid();
    private static final String TAG = "AuxRtgSched";
    private static AuxRtgSched sInstance;
    private int mAmsTid = -1;
    private Map<Integer, AppInfo> mAppInfos = new ConcurrentHashMap();
    private AtomicBoolean mAuxRtgEnable = new AtomicBoolean(false);
    private Map<String, Pair<String, String>> mAuxThreadConfig = new ArrayMap();
    private AtomicBoolean mClicked = new AtomicBoolean(false);
    private CpuFeature.CpuFeatureHandler mCpuFeatureHandler;
    private AtomicBoolean mEnableSlide = new AtomicBoolean(false);
    private int mFgUid = -1;
    private List<String> mKeyThreads = new ArrayList();
    private RtgSchedController mRtgSchedController;
    private AtomicBoolean mSlided = new AtomicBoolean(false);
    private Map<Integer, PidInfo> mSysAuxThreads = new ArrayMap();
    private Map<Integer, Integer> mTgidToUidMap = new ConcurrentHashMap();
    private int mTotalAuxThread = 0;

    static {
        AnimationThreadEx.get();
        SurfaceAnimationThreadEx.get();
    }

    private AuxRtgSched() {
    }

    private void setSystemAuxThread(int enable) {
        for (Map.Entry<Integer, PidInfo> entry : this.mSysAuxThreads.entrySet()) {
            PidInfo pidInfo = entry.getValue();
            if (pidInfo != null) {
                pidInfo.setRtgSchedForFg(enable);
            }
        }
    }

    public static AuxRtgSched getInstance() {
        AuxRtgSched auxRtgSched;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new AuxRtgSched();
            }
            auxRtgSched = sInstance;
        }
        return auxRtgSched;
    }

    public void enable(CpuFeature.CpuFeatureHandler handler, RtgSchedController controller) {
        if (this.mAuxRtgEnable.get()) {
            AwareLog.w(TAG, "AuxRtgSched already enabled.");
        } else if (this.mAuxThreadConfig.size() == 0) {
            AwareLog.w(TAG, "AuxRtgSched disabled locking if aux config.");
        } else {
            this.mCpuFeatureHandler = handler;
            this.mRtgSchedController = controller;
            if (HwActivityManagerService.self().getAmsHandlerThread() != null) {
                this.mAmsTid = HwActivityManagerService.self().getAmsHandlerThread().getThreadId();
            }
            this.mSysAuxThreads.clear();
            this.mSysAuxThreads.put(Integer.valueOf(ANIM_TID), new PidInfo(SYSTEM_SERVER_PID, ANIM_TID, ANIMATOR));
            this.mSysAuxThreads.put(Integer.valueOf(this.mAmsTid), new PidInfo(SYSTEM_SERVER_PID, this.mAmsTid, AMS));
            this.mSysAuxThreads.put(Integer.valueOf(RMS_TID), new PidInfo(SYSTEM_SERVER_PID, RMS_TID, RMS));
            this.mSysAuxThreads.put(Integer.valueOf(ANIM_LF_TID), new PidInfo(SYSTEM_SERVER_PID, ANIM_LF_TID, ANIMATOR_LF));
            setAnimatorToVip(true);
            this.mTotalAuxThread = 0;
            setKeyThreadToKernel(this.mKeyThreads);
            this.mAuxRtgEnable.set(true);
        }
    }

    private void setAnimatorToVip(boolean isSet) {
        Pair<String, String> animConfig = this.mAuxThreadConfig.get(ANIMATOR);
        if (animConfig != null && animConfig.first != null && animConfig.second != null) {
            int msgId = isSet ? CpuFeature.MSG_SET_VIP_THREAD : CpuFeature.MSG_RESET_VIP_THREAD;
            ArrayList<Integer> threads = new ArrayList<>();
            threads.add(Integer.valueOf(ANIM_TID));
            CpuVipThread.getInstance().sendPacket(ANIM_TID, threads, msgId);
        }
    }

    /* access modifiers changed from: protected */
    public void setSlideSwitch(String slideSwitch) {
        if ("1".equals(slideSwitch)) {
            this.mEnableSlide.set(true);
        } else {
            this.mEnableSlide.set(false);
        }
    }

    public void disable() {
        if (!this.mAuxRtgEnable.get()) {
            AwareLog.w(TAG, "AuxRtgSched already disabled.");
            return;
        }
        this.mAuxRtgEnable.set(false);
        setAnimatorToVip(false);
        this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_AUX_RTG_SCHED);
        this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_DELAYED_REFRESH);
        onAuxRtgTimeOut();
        setKeyThreadToKernel(new ArrayList());
    }

    public void setAuxRtgConfig(Map<String, String> auxRtgConfig) {
        if (auxRtgConfig == null) {
            AwareLog.w(TAG, "setAuxRtgConfig null auxRtgConfig map.");
            return;
        }
        for (Map.Entry<String, String> entry : auxRtgConfig.entrySet()) {
            String auxRtgType = entry.getKey();
            List<String> auxValueList = Arrays.asList(entry.getValue().split(","));
            if (auxValueList.size() != 2) {
                AwareLog.e(TAG, "Illegal aux_rtg_config " + auxRtgType);
            } else {
                this.mAuxThreadConfig.put(auxRtgType, new Pair<>(auxValueList.get(0), auxValueList.get(1)));
            }
        }
        AwareLog.i(TAG, "setAuxRtgConfig: " + this.mAuxThreadConfig);
    }

    public void setKeyThreadConfig(List<String> keyThreads) {
        if (keyThreads != null && !keyThreads.isEmpty()) {
            this.mKeyThreads.clear();
            this.mKeyThreads.addAll(keyThreads);
            AwareLog.i(TAG, "KeyThreadConfig: " + this.mKeyThreads);
        }
    }

    public int getAuxForkDelay() {
        return 100;
    }

    public void onScreenStateChanged(boolean isScreenOn) {
        if (this.mAuxRtgEnable.get() && !isScreenOn) {
            CpuFeature.CpuFeatureHandler cpuFeatureHandler = this.mCpuFeatureHandler;
            if (cpuFeatureHandler != null) {
                cpuFeatureHandler.removeMessages(CpuFeature.MSG_AUX_RTG_SCHED);
            }
            onAuxRtgTimeOut();
        }
    }

    public void removeAuxThread(int pid, int tgid) {
        if (this.mAuxRtgEnable.get()) {
            int uid = getAppUidByTgid(tgid);
            AppInfo appInfo = this.mAppInfos.get(Integer.valueOf(uid));
            if (appInfo != null) {
                AwareLog.d(TAG, "Remove aux thread, pid=" + tgid + ", tid=" + pid);
                appInfo.remove(pid, tgid);
                if (appInfo.size() == 0) {
                    this.mAppInfos.remove(Integer.valueOf(uid));
                }
            }
        }
    }

    public void refreshCgroup() {
        AppInfo appInfo;
        if (this.mAuxRtgEnable.get() && (appInfo = this.mAppInfos.get(Integer.valueOf(this.mFgUid))) != null) {
            appInfo.refresh();
        }
    }

    public void sendAuxCommMessage(int msgId, int pid, int tgid, int delay) {
        if (this.mAuxRtgEnable.get()) {
            Message msg = this.mCpuFeatureHandler.obtainMessage(msgId);
            msg.arg1 = pid;
            msg.arg2 = tgid;
            this.mCpuFeatureHandler.sendMessageDelayed(msg, (long) delay);
        }
    }

    public void onCommChanged(int pid, int tgid) {
        if (this.mAuxRtgEnable.get()) {
            String comm = getThreadComm(tgid, pid);
            if (validThreadComm(comm)) {
                int uid = getAppUidByTgid(tgid);
                AwareLog.d(TAG, "KeyThreads.onCommChanged pid=" + pid + " tgid=" + tgid + " uid=" + uid + " comm=" + comm);
                if (uid == -1) {
                    AwareLog.d(TAG, "KeyThreads.onCommChanged invalid uid.");
                    return;
                }
                this.mTgidToUidMap.put(Integer.valueOf(tgid), Integer.valueOf(uid));
                AppInfo appInfo = this.mAppInfos.get(Integer.valueOf(uid));
                if (appInfo == null) {
                    AwareLog.d(TAG, "KeyThreads.onCommChanged add appInfo for uid=" + uid);
                    appInfo = new AppInfo(uid);
                    this.mAppInfos.put(Integer.valueOf(uid), appInfo);
                }
                Optional<PidInfo> pidInfo = appInfo.add(pid, tgid, comm);
                if (!this.mClicked.get() && (!this.mEnableSlide.get() || !this.mSlided.get())) {
                    AwareLog.d(TAG, "onCommChanged aux rtg not in progress.");
                } else if (uid == this.mFgUid && pidInfo.isPresent()) {
                    pidInfo.get().setRtgSchedForFg(1);
                }
            }
        }
    }

    public boolean setAuxRtgThread(int auxTid, int enable, String auxType) {
        Pair<String, String> pair;
        if (!this.mAuxRtgEnable.get() || this.mRtgSchedController == null || auxTid <= 0 || (pair = this.mAuxThreadConfig.get(auxType)) == null) {
            return false;
        }
        String prio = (String) pair.first;
        String util = (String) pair.second;
        if (prio == null || util == null) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("auxTid");
        builder.append(":");
        builder.append(auxTid);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("enable");
        builder.append(":");
        builder.append(enable);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("minUtil");
        builder.append(":");
        builder.append(util);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("rtgPrio");
        builder.append(":");
        builder.append(prio);
        AwareLog.d(TAG, "setAuxRtgThread(" + auxType + "): " + builder.toString());
        this.mRtgSchedController.setRtgThread(builder.toString());
        return true;
    }

    public boolean setAuxRtgThreadAndCount(int auxTid, int enable, String auxType) {
        boolean ret;
        if (!this.mAuxRtgEnable.get() || this.mRtgSchedController == null) {
            return false;
        }
        if (this.mSysAuxThreads.get(Integer.valueOf(auxTid)) != null) {
            return setAuxRtgThread(auxTid, enable, auxType);
        }
        if (enable != 1) {
            ret = setAuxRtgThread(auxTid, enable, auxType);
            if (ret) {
                this.mTotalAuxThread--;
            }
        } else if (this.mTotalAuxThread >= 9) {
            return false;
        } else {
            ret = setAuxRtgThread(auxTid, enable, auxType);
            if (ret) {
                this.mTotalAuxThread++;
            }
        }
        if (this.mTotalAuxThread < 0) {
            this.mTotalAuxThread = 0;
        }
        return ret;
    }

    public void onFocusChanged(int oldPid, int newPid) {
        if (this.mAuxRtgEnable.get()) {
            int oldUid = this.mFgUid;
            int newUid = getAppUidByTgid(newPid);
            AppInfo newAppInfo = this.mAppInfos.get(Integer.valueOf(newUid));
            if (newAppInfo != null) {
                newAppInfo.focusChanged(newPid, this.mClicked.get() && newUid == oldUid);
            }
            this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_DELAYED_REFRESH);
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_DELAYED_REFRESH, 500);
            if (newUid != oldUid) {
                this.mFgUid = newUid;
                AwareLog.i(TAG, "onFocusChanged oldUid=" + oldUid + " newUid=" + newUid + " pid=" + newPid);
                AppInfo oldAppInfo = this.mAppInfos.get(Integer.valueOf(oldUid));
                if (oldAppInfo != null) {
                    oldAppInfo.setRtgSchedForFg(0);
                }
                if (!this.mClicked.get() && (!this.mEnableSlide.get() || !this.mSlided.get())) {
                    AwareLog.d(TAG, "onFocusChanged aux rtg not in progress.");
                } else if (newAppInfo != null) {
                    newAppInfo.refresh();
                    newAppInfo.setRtgSchedForFg(1);
                }
            }
        }
    }

    public void onInputEvent(CollectData data) {
        if (this.mAuxRtgEnable.get() && data != null) {
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCENE_REC) {
                Bundle bundle = data.getBundle();
                if (bundle != null) {
                    int event = bundle.getInt("relationType");
                    if (event == 13 || event == 14 || event == 15) {
                        onSlideEvent();
                    }
                }
            } else if (type == AwareConstant.ResourceType.RES_INPUT) {
                AttrSegments attrSegments = parseCollectData(data);
                if (!attrSegments.isValid()) {
                    return;
                }
                if (attrSegments.getEvent().intValue() == 10001) {
                    onTouchDownEvent();
                } else if (attrSegments.getEvent().intValue() == 80001) {
                    onTouchUpEvent();
                } else {
                    AwareLog.d(TAG, "ignored event " + attrSegments.getEvent());
                }
            } else {
                AwareLog.d(TAG, "ignored resource type " + type);
            }
        }
    }

    private void setAuxKeyThreads(int enable) {
        setSystemAuxThread(enable);
        AppInfo appInfo = this.mAppInfos.get(Integer.valueOf(this.mFgUid));
        if (appInfo != null) {
            appInfo.setRtgSchedForFg(enable);
        }
    }

    public void onAuxRtgTimeOut() {
        AwareLog.i(TAG, "KeyThreads.onTimeout fgUid=" + this.mFgUid);
        setAuxKeyThreads(0);
        this.mTotalAuxThread = 0;
        this.mClicked.set(false);
        this.mSlided.set(false);
    }

    public void onProcessDied(int tgid) {
        Integer uid;
        if (this.mAuxRtgEnable.get() && (uid = this.mTgidToUidMap.get(Integer.valueOf(tgid))) != null) {
            this.mTgidToUidMap.remove(Integer.valueOf(tgid));
            AppInfo appInfo = this.mAppInfos.get(uid);
            if (appInfo != null) {
                AwareLog.d(TAG, "KeyThreads.onProcessDied tgid=" + tgid + " uid=" + uid);
                appInfo.remove(tgid);
                appInfo.refresh();
                if (appInfo.size() == 0) {
                    this.mAppInfos.remove(uid);
                    AwareLog.d(TAG, "KeyThreads.onProcessDied remove appInfo uid=" + uid + " count=" + this.mAppInfos.size());
                }
            }
        }
    }

    private boolean validThreadComm(String comm) {
        if (comm == null) {
            AwareLog.d(TAG, "Invalid thread comm:null");
            return false;
        } else if (this.mKeyThreads.contains(comm)) {
            return true;
        } else {
            AwareLog.d(TAG, "Invalid thread comm:" + comm);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getThreadComm(int tgid, int pid) {
        String comm = CpuHighFgControl.getInstance().getContentWithOneLine("/proc/" + tgid + "/task/" + pid + "/comm", "");
        if (comm != null) {
            return comm.trim();
        }
        return comm;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFgThread(int tgid, int pid) {
        String cgroupStr = CpuHighFgControl.getInstance().getContentWithOneLine("/proc/" + tgid + "/task/" + pid + "/cgroup", CPUSET);
        if (cgroupStr == null || cgroupStr.contains("background")) {
            return false;
        }
        return true;
    }

    private void setKeyThreadToKernel(List<String> keyThreads) {
        if (!(keyThreads == null || this.mRtgSchedController == null)) {
            StringBuilder sb = new StringBuilder("keyThreads:");
            for (String keyThread : keyThreads) {
                sb.append(keyThread);
                sb.append(',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            AwareLog.d(TAG, "setKeyThreadConfig: " + sb.toString());
            this.mRtgSchedController.setRtgThread(sb.toString());
        }
    }

    private void onTouchDownEvent() {
        this.mSlided.set(false);
    }

    private void onSlideEvent() {
        if (!this.mEnableSlide.get()) {
            this.mSlided.set(true);
        } else if (this.mClicked.get() || this.mSlided.get()) {
            this.mSlided.set(true);
            this.mClicked.set(false);
        } else {
            AwareLog.i(TAG, "KeyThreads.onSlideEvent set rtg sched fgUid=" + this.mFgUid);
            this.mSlided.set(true);
            this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_AUX_RTG_SCHED);
            setAuxKeyThreads(1);
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_AUX_RTG_SCHED, 1500);
        }
    }

    private void onTouchUpEvent() {
        if (this.mSlided.get()) {
            this.mClicked.set(this.mEnableSlide.get());
        } else if (!this.mClicked.get()) {
            AwareLog.i(TAG, "KeyThreads.onTouchUpEvent set rtg sched fgUid=" + this.mFgUid);
            this.mClicked.set(true);
            this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_AUX_RTG_SCHED);
            setAuxKeyThreads(1);
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_AUX_RTG_SCHED, 1500);
        }
    }

    private int getAppUidByTgid(int tgid) {
        int uid = -1;
        ProcessInfo procInfo = ProcessInfoCollector.getInstance().getProcessInfo(tgid);
        if (procInfo != null) {
            uid = procInfo.mAppUid;
        }
        if (uid <= 0) {
            return ProcessExt.getUidForPid(tgid);
        }
        return uid;
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }

    /* access modifiers changed from: package-private */
    public static class PidInfo {
        private String comm;
        private boolean isFg;
        private boolean isRtg = false;
        private final int pid;
        private final int tgid;

        PidInfo(int tgid2, int pid2, String comm2) {
            AwareLog.d(AuxRtgSched.TAG, "New pidinfo pid:" + pid2 + ", comm:" + comm2);
            this.tgid = tgid2;
            this.pid = pid2;
            this.comm = comm2;
            refresh();
        }

        /* access modifiers changed from: package-private */
        public void setRtgSchedForFg(int enable) {
            boolean z = false;
            if (this.isRtg != (enable == 1)) {
                if (enable != 1 || this.isFg) {
                    AwareLog.d(AuxRtgSched.TAG, "KeyThreads.PidInfo.setRtgSchedForFg enable=" + enable + ", pid=" + this.pid + ", comm=" + this.comm);
                    if (AuxRtgSched.getInstance().setAuxRtgThreadAndCount(this.pid, enable, this.comm)) {
                        if (enable == 1) {
                            z = true;
                        }
                        this.isRtg = z;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateComm(String newComm) {
            if (newComm != null && !newComm.equals(this.comm)) {
                refresh();
                AwareLog.d(AuxRtgSched.TAG, "Set comm from " + this.comm + " to " + newComm);
                if (this.isRtg) {
                    setRtgSchedForFg(0);
                    this.comm = newComm;
                    setRtgSchedForFg(1);
                    return;
                }
                this.comm = newComm;
            }
        }

        public String toString() {
            return "PidInfo(pid=" + this.pid + " comm=" + this.comm + " isRtg=" + this.isRtg + ")";
        }

        /* access modifiers changed from: package-private */
        public boolean isAlive() {
            boolean ret = false;
            String str = this.comm;
            if (str != null && str.equals(AuxRtgSched.getInstance().getThreadComm(this.tgid, this.pid))) {
                ret = true;
            }
            if (!ret) {
                setRtgSchedForFg(0);
            }
            AwareLog.d(AuxRtgSched.TAG, "isAlive pid:" + this.pid + ", isAlive:" + ret);
            return ret;
        }

        /* access modifiers changed from: package-private */
        public void refresh() {
            this.isFg = AuxRtgSched.getInstance().isFgThread(this.tgid, this.pid);
            if (!this.isFg && this.isRtg) {
                setRtgSchedForFg(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class TgidInfo {
        private Map<Integer, PidInfo> pidInfos = new ArrayMap(6);
        private final int tgid;

        TgidInfo(int tgid2) {
            this.tgid = tgid2;
        }

        /* access modifiers changed from: package-private */
        public Optional<PidInfo> add(int pid, String comm) {
            if (size() >= 9) {
                refresh();
                if (size() >= 9) {
                    AwareLog.d(AuxRtgSched.TAG, "Tgid.info add reach max capacity per proc.");
                    return Optional.empty();
                }
            }
            PidInfo pidInfo = this.pidInfos.get(Integer.valueOf(pid));
            if (pidInfo != null) {
                pidInfo.updateComm(comm);
                return Optional.empty();
            }
            PidInfo pidInfo2 = new PidInfo(this.tgid, pid, comm);
            this.pidInfos.put(Integer.valueOf(pid), pidInfo2);
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.TgidInfo.add success pid=" + pid + " " + this);
            return Optional.of(pidInfo2);
        }

        /* access modifiers changed from: package-private */
        public PidInfo remove(int pid) {
            PidInfo pidInfo = this.pidInfos.remove(Integer.valueOf(pid));
            if (pidInfo != null) {
                pidInfo.setRtgSchedForFg(0);
                AwareLog.d(AuxRtgSched.TAG, "KeyThreads.TgidInfo.remove success pid=" + pid + " " + this);
            }
            return pidInfo;
        }

        /* access modifiers changed from: package-private */
        public void setRtgSchedForFg(int enable) {
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.TgidInfo.setRtgSchedForFg enable=" + enable + " " + this);
            for (Map.Entry<Integer, PidInfo> entry : this.pidInfos.entrySet()) {
                PidInfo pidInfo = entry.getValue();
                if (pidInfo != null) {
                    pidInfo.setRtgSchedForFg(enable);
                }
            }
        }

        public String toString() {
            return "TgidInfo(tgid=" + this.tgid + " size=" + this.pidInfos.size() + ")";
        }

        /* access modifiers changed from: package-private */
        public int refresh() {
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.TgidInfo.refresh " + this);
            int count = 0;
            Iterator<Map.Entry<Integer, PidInfo>> it = this.pidInfos.entrySet().iterator();
            while (it.hasNext()) {
                PidInfo pidInfo = it.next().getValue();
                if (pidInfo == null || !pidInfo.isAlive()) {
                    it.remove();
                    count++;
                } else {
                    pidInfo.refresh();
                }
            }
            return count;
        }

        /* access modifiers changed from: package-private */
        public boolean isAlive() {
            AuxRtgSched instance = AuxRtgSched.getInstance();
            int i = this.tgid;
            boolean ret = instance.getThreadComm(i, i) != null;
            AwareLog.d(AuxRtgSched.TAG, "isAlive tgid:" + this.tgid + ", isAlive:" + ret);
            return ret;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.pidInfos.size();
        }
    }

    /* access modifiers changed from: package-private */
    public static class AppInfo {
        private int fgTgid = -1;
        private Map<Integer, TgidInfo> tgidInfos = new ArrayMap();
        private int total = 0;
        private final int uid;

        AppInfo(int uid2) {
            this.uid = uid2;
        }

        /* access modifiers changed from: package-private */
        public Optional<PidInfo> add(int pid, int tgid, String comm) {
            TgidInfo tgidInfo = this.tgidInfos.get(Integer.valueOf(tgid));
            if (tgidInfo == null) {
                tgidInfo = new TgidInfo(tgid);
                this.tgidInfos.put(Integer.valueOf(tgid), tgidInfo);
                AwareLog.d(AuxRtgSched.TAG, "KeyThreads.AppInfo.add success tgid=" + tgid + " " + this);
            }
            Optional<PidInfo> pidInfo = tgidInfo.add(pid, comm);
            if (!pidInfo.isPresent()) {
                return pidInfo;
            }
            this.total++;
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.AppInfo.add success pid=" + pid + " tgid=" + tgid + " " + this);
            return pidInfo;
        }

        /* access modifiers changed from: package-private */
        public void focusChanged(int pid, boolean isInnerAppFocusChanged) {
            if (isInnerAppFocusChanged) {
                refresh();
                setRtgSchedForFg(0);
            }
            this.fgTgid = pid;
            if (isInnerAppFocusChanged) {
                setRtgSchedForFg(1);
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(int tgid) {
            TgidInfo tgidInfo = this.tgidInfos.remove(Integer.valueOf(tgid));
            if (tgidInfo != null && tgidInfo.size() != 0) {
                tgidInfo.setRtgSchedForFg(0);
                this.total -= tgidInfo.size();
                AwareLog.d(AuxRtgSched.TAG, "KeyThreads.AppInfo.remove success tgid=" + tgid + " " + this);
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(int pid, int tgid) {
            TgidInfo tgidInfo = this.tgidInfos.get(Integer.valueOf(tgid));
            if (tgidInfo != null && tgidInfo.remove(pid) != null) {
                this.total--;
            }
        }

        /* access modifiers changed from: package-private */
        public void refresh() {
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.AppInfo.refresh " + this);
            Iterator<Map.Entry<Integer, TgidInfo>> it = this.tgidInfos.entrySet().iterator();
            while (it.hasNext()) {
                TgidInfo tgidInfo = it.next().getValue();
                if (tgidInfo == null) {
                    it.remove();
                } else if (!tgidInfo.isAlive()) {
                    it.remove();
                    this.total -= tgidInfo.size();
                } else {
                    this.total -= tgidInfo.refresh();
                    if (tgidInfo.size() == 0) {
                        it.remove();
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setRtgSchedForFg(int enable) {
            AwareLog.d(AuxRtgSched.TAG, "KeyThreads.AppInfo.setRtgSchedForFg enable=" + enable + " " + this);
            TgidInfo fgTgidInfo = this.tgidInfos.get(Integer.valueOf(this.fgTgid));
            if (fgTgidInfo != null) {
                fgTgidInfo.setRtgSchedForFg(enable);
            }
            for (Map.Entry<Integer, TgidInfo> entry : this.tgidInfos.entrySet()) {
                TgidInfo tgidInfo = entry.getValue();
                if (!(tgidInfo == null || tgidInfo == fgTgidInfo)) {
                    tgidInfo.setRtgSchedForFg(enable);
                }
            }
        }

        public String toString() {
            return "AppInfo(uid=" + this.uid + " tgidSize=" + this.tgidInfos.size() + " pidSize=" + this.total + ")";
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.total;
        }
    }
}
