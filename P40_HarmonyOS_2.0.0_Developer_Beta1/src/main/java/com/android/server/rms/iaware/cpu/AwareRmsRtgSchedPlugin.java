package com.android.server.rms.iaware.cpu;

import android.iawareperf.RtgSchedController;
import android.os.Process;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareRmsRtgSchedPlugin {
    private static final String AUX_SLIDE_SWITCH = "aux_slide_switch";
    private static final String FILTER_TYPE_LIST = "filter_type_list";
    private static final int INVALID_VALUE = -1;
    private static final int MY_PID = Process.myPid();
    private static final Object SLOCK = new Object();
    private static final String TAG = "AwareRmsRtgSchedPlugin";
    private static final int TYPE_APP_START = 4;
    private static final int TYPE_FOCUS_CHANGE = 3;
    private static final int TYPE_INPUT_METHOD_SHOW = 5;
    private static final int TYPE_REMOTE_ANIMATION_END = 1;
    private static final int TYPE_REMOTE_ANIMATION_START = 0;
    private static final int TYPE_SET_RENDER = 2;
    private static AwareRmsRtgSchedPlugin sInstance;
    private CpuFeature mCpuFeatureInstance;
    private int mCurRtgPid;
    private int mCurRtgTid;
    private Set<Integer> mFilterAppTypeSet = new ArraySet();
    private int mFocusAppPid = 0;
    private int mFocusAppType = -1;
    private boolean mIsRunningRemoteAnimation = false;
    private Map<String, String> mMargin = new ArrayMap();
    private RtgSchedController mRtgSchedController;
    private AtomicBoolean mRtgSchedEnable = new AtomicBoolean(false);
    private Map<String, String> mSchedConfig = new ArrayMap();
    private int mSupportRtgFocusPid;
    private int mSupportRtgFocusTid;

    private AwareRmsRtgSchedPlugin() {
    }

    public static AwareRmsRtgSchedPlugin getInstance() {
        AwareRmsRtgSchedPlugin awareRmsRtgSchedPlugin;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new AwareRmsRtgSchedPlugin();
            }
            awareRmsRtgSchedPlugin = sInstance;
        }
        return awareRmsRtgSchedPlugin;
    }

    public void enable(CpuFeature.CpuFeatureHandler handler, CpuFeature feature) {
        this.mRtgSchedEnable.set(true);
        if (this.mRtgSchedController == null) {
            this.mRtgSchedController = new RtgSchedController();
        }
        this.mCpuFeatureInstance = feature;
        this.mRtgSchedController.init();
        setRtgFreqEnable(true);
        setRtgSchedFreqParams();
        AuxRtgSched.getInstance().enable(handler, this.mRtgSchedController);
    }

    public void disable() {
        this.mRtgSchedEnable.set(false);
        AuxRtgSched.getInstance().disable();
        setRtgFreqEnable(false);
        RtgSchedController rtgSchedController = this.mRtgSchedController;
        if (rtgSchedController != null) {
            rtgSchedController.deInit();
            this.mRtgSchedController = null;
        }
        HwActivityManagerServiceEx.clearTopApps();
    }

    /* access modifiers changed from: protected */
    public RtgSchedController getRtgSchedController() {
        return this.mRtgSchedController;
    }

    private Integer parseInteger(String str) {
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException: parse Integer failed! not number: " + str);
            return null;
        }
    }

    public void setPluginConfig(Map<String, String> pluginConfig) {
        if (pluginConfig == null) {
            AwareLog.w(TAG, "setPluginConfig null pluginConfig.");
            return;
        }
        setFilterAppType(pluginConfig.get(FILTER_TYPE_LIST));
        AuxRtgSched.getInstance().setSlideSwitch(pluginConfig.get(AUX_SLIDE_SWITCH));
    }

    private void setFilterAppType(String filterTypes) {
        List<String> typeList;
        this.mFilterAppTypeSet.clear();
        if (filterTypes != null && (typeList = Arrays.asList(filterTypes.split(","))) != null) {
            Iterator<String> it = typeList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String typeStr = it.next();
                Integer type = parseInteger(typeStr);
                if (type == null) {
                    AwareLog.e(TAG, "Illegal apptype config " + typeStr);
                    this.mFilterAppTypeSet.clear();
                    break;
                }
                this.mFilterAppTypeSet.add(type);
            }
            AwareLog.i(TAG, "FilterAppTypeSet: " + this.mFilterAppTypeSet + ", mytid: " + Process.myTid());
        }
    }

    public void setSchedConfig(Map<String, String> schedConfig) {
        if (schedConfig == null) {
            AwareLog.w(TAG, "setSchedConfig null schedConfig.");
            return;
        }
        this.mSchedConfig.clear();
        this.mSchedConfig.putAll(schedConfig);
    }

    public void setMargin(Map<String, String> margin) {
        if (margin == null) {
            AwareLog.w(TAG, "setMargin null margin map.");
            return;
        }
        this.mMargin.clear();
        this.mMargin.putAll(margin);
    }

    public int getFocusAppPid() {
        return this.mFocusAppPid;
    }

    private void handleUnsupportRtgApp(int pid, int actionType) {
        if (pid > 0 && pid != MY_PID) {
            if (actionType == 4) {
                this.mIsRunningRemoteAnimation = false;
            }
            if (!this.mIsRunningRemoteAnimation) {
                if (this.mCurRtgPid > 0) {
                    setRtgThread(-1, -1);
                    HwActivityManagerServiceEx.notifyAppToTop(this.mCurRtgPid, 0);
                }
                AuxRtgSched.getInstance().onFocusChanged(this.mCurRtgPid, -1);
                this.mCurRtgPid = -1;
                this.mCurRtgTid = -1;
            }
            if (actionType == 3) {
                this.mFocusAppPid = pid;
                this.mSupportRtgFocusPid = -1;
                this.mSupportRtgFocusTid = -1;
            }
        }
    }

    private boolean isShouldSetRtgSched(int pid, int actionType) {
        if (pid <= 0 || pid == MY_PID) {
            return false;
        }
        if (this.mFilterAppTypeSet.size() == 0) {
            return true;
        }
        String pkg = CpuCommonUtil.getPackageNameForPid(pid);
        if (pkg != null) {
            this.mFocusAppType = AppTypeRecoManager.getInstance().getAppType(pkg);
        } else {
            this.mFocusAppType = -1;
        }
        if (!this.mFilterAppTypeSet.contains(Integer.valueOf(this.mFocusAppType))) {
            return true;
        }
        AwareLog.d(TAG, "disable rtg sched for pkg: " + pkg + ", appType:" + this.mFocusAppType);
        return false;
    }

    public void setRtgProcess(int pid, int renderTid, int actionType) {
        if (this.mRtgSchedEnable.get()) {
            if (!isShouldSetRtgSched(pid, actionType)) {
                handleUnsupportRtgApp(pid, actionType);
                return;
            }
            if (actionType == 0) {
                this.mIsRunningRemoteAnimation = true;
            } else if (actionType != 1) {
                if (actionType != 2) {
                    if (actionType == 3) {
                        this.mFocusAppPid = pid;
                        this.mSupportRtgFocusPid = pid;
                        renderTid = CpuCommonUtil.getRenderTid(pid);
                        this.mSupportRtgFocusTid = renderTid;
                    } else if (actionType == 4) {
                        this.mIsRunningRemoteAnimation = false;
                    } else if (actionType != 5) {
                        AwareLog.w(TAG, "setRtgProcess, unsurpported actionType: " + actionType);
                        return;
                    } else {
                        renderTid = CpuCommonUtil.getRenderTid(pid);
                    }
                }
                renderTid = CpuCommonUtil.getRenderTid(pid);
            } else {
                this.mIsRunningRemoteAnimation = false;
                pid = this.mSupportRtgFocusPid;
                renderTid = this.mSupportRtgFocusTid;
            }
            setRtgProcessInner(pid, renderTid, actionType);
        }
    }

    private void setFocusAppType(int pid) {
        if (pid == -1) {
            SystemPropertiesEx.set("sys.sched.rtg.current", "");
            return;
        }
        int appType = CpuCommonUtil.getAppTypeByPid(pid);
        SystemPropertiesEx.set("sys.sched.rtg.current", "pid=" + pid + ";appType=" + appType);
    }

    private void setRtgProcessInner(int pid, int renderTid, int actionType) {
        AwareLog.d(TAG, "handleFocusProcess pid:" + pid + ", tid:" + renderTid + ", actionType:" + actionType);
        if (!this.mIsRunningRemoteAnimation || actionType == 0) {
            if (this.mCurRtgPid != pid) {
                AuxRtgSched.getInstance().onFocusChanged(this.mCurRtgPid, pid);
            }
            if (this.mCurRtgPid != pid || this.mCurRtgTid != renderTid) {
                setRtgThread(pid, renderTid);
                setFocusAppType(pid);
                HwActivityManagerServiceEx.notifyAppToTop(pid, 1);
                int i = this.mCurRtgPid;
                if (i > 0 && i != pid) {
                    HwActivityManagerServiceEx.notifyAppToTop(i, 0);
                }
                this.mCurRtgPid = pid;
                this.mCurRtgTid = renderTid;
            }
        }
    }

    public void onScreenStateChanged(boolean screenOn) {
        int renderTid;
        if (this.mRtgSchedEnable.get()) {
            AuxRtgSched.getInstance().onScreenStateChanged(screenOn);
            if (screenOn) {
                int i = this.mCurRtgPid;
                if (i > 0 && (renderTid = HwActivityManagerServiceEx.getRenderTid(i)) >= 0) {
                    setRtgThread(this.mCurRtgPid, renderTid);
                    setFocusAppType(this.mCurRtgPid);
                    HwActivityManagerServiceEx.notifyAppToTop(this.mCurRtgPid, 1);
                    return;
                }
                return;
            }
            setRtgThread(-1, -1);
            setFocusAppType(-1);
            int i2 = this.mCurRtgPid;
            if (i2 > 0) {
                HwActivityManagerServiceEx.notifyAppToTop(i2, 0);
            }
        }
    }

    public void processDied(int pid) {
        if (!this.mRtgSchedEnable.get()) {
            AwareLog.d(TAG, "processDied RtgSched is disabled.");
            return;
        }
        if (this.mCurRtgPid == pid || this.mFocusAppPid == pid) {
            this.mCpuFeatureInstance.removeInputRtgMessage();
        }
        AuxRtgSched.getInstance().onProcessDied(pid);
        int i = this.mCurRtgPid;
        if (i == pid) {
            if (i == this.mSupportRtgFocusPid || !CpuMultiDisplay.getInstance().isPhoneDisplay(this.mSupportRtgFocusPid)) {
                this.mFocusAppPid = -1;
                this.mFocusAppType = -1;
                this.mCurRtgPid = -1;
                this.mCurRtgTid = -1;
                this.mSupportRtgFocusPid = -1;
                this.mSupportRtgFocusTid = -1;
            } else {
                setRtgProcess(this.mSupportRtgFocusPid, this.mSupportRtgFocusTid, 3);
            }
        }
        HwActivityManagerServiceEx.removeProcess(pid);
    }

    private void setRtgThread(int pid, int renderTid) {
        if (this.mRtgSchedController == null) {
            AwareLog.w(TAG, "setRtgThread failed: null controller.");
            return;
        }
        int uid = ProcessExt.getUidForPid(pid);
        StringBuilder builder = new StringBuilder();
        builder.append("fgUid");
        builder.append(":");
        builder.append(uid);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("uiTid");
        builder.append(":");
        builder.append(pid);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("renderTid");
        builder.append(":");
        builder.append(renderTid);
        AwareLog.d(TAG, "setRtgThread: " + builder.toString());
        this.mRtgSchedController.setRtgThread(builder.toString());
    }

    public void setHmThreadToRtg(int pid, List<Integer> tids, int type) {
        if (this.mRtgSchedController == null) {
            AwareLog.w(TAG, "setHmThreadToRtg failed: null controller.");
            return;
        }
        int uid = ProcessExt.getUidForPid(pid);
        StringBuilder builder = new StringBuilder();
        builder.append("hmUid:");
        builder.append(uid);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        builder.append("hmPid:");
        builder.append(pid);
        builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        for (Integer num : tids) {
            int tid = num.intValue();
            builder.append("hmTid:");
            builder.append(tid);
            builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        }
        builder.append("type:");
        builder.append(type);
        AwareLog.d(TAG, "setHmThreadToRtg: " + builder.toString());
        this.mRtgSchedController.setRtgThread(builder.toString());
    }

    private StringBuilder combineConfigMap(Map<String, String> configMap) {
        StringBuilder builder = new StringBuilder();
        if (configMap == null) {
            return builder;
        }
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue());
            builder.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        }
        int len = builder.length();
        if (len > 0) {
            builder.deleteCharAt(len - 1);
        }
        return builder;
    }

    private void setRtgFreqEnable(boolean enable) {
        if (this.mRtgSchedController == null) {
            AwareLog.w(TAG, "setRtgFreqEnable failed: null controller.");
        } else if (enable) {
            StringBuilder builder = combineConfigMap(this.mSchedConfig);
            AwareLog.i(TAG, "setRtgFreqEnable: enable, " + builder.toString());
            this.mRtgSchedController.setRtgFreqEnable(1, builder.toString());
        } else {
            AwareLog.i(TAG, "setRtgFreqEnable: disable");
            this.mRtgSchedController.setRtgFreqEnable(0, "");
        }
    }

    private void setRtgSchedFreqParams() {
        if (this.mRtgSchedController == null) {
            AwareLog.w(TAG, "setRtgSchedFreqParams failed: null controller.");
            return;
        }
        StringBuilder builder = combineConfigMap(this.mMargin);
        AwareLog.i(TAG, "setRtgSchedFreqParams: " + builder.toString());
        this.mRtgSchedController.setFreqParam(builder.toString());
    }
}
