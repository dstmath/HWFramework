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
    private static final int TYPE_REMOTE_ANIMATION_END = 1;
    private static final int TYPE_REMOTE_ANIMATION_START = 0;
    private static final int TYPE_SET_RENDER = 2;
    private static AwareRmsRtgSchedPlugin sInstance;
    private int mCurFocusPid;
    private int mCurFocusTid;
    private int mCurPid;
    private int mCurTid;
    private Set<Integer> mFilterAppTypeSet = new ArraySet();
    private Map<String, String> mMargin = new ArrayMap();
    private RtgSchedController mRtgSchedController;
    private AtomicBoolean mRtgSchedEnable = new AtomicBoolean(false);
    private boolean mRunningRemoteAnimation = false;
    private Map<String, String> mSchedConfig = new ArrayMap();

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

    public void enable(CpuFeature.CpuFeatureHandler handler) {
        this.mRtgSchedEnable.set(true);
        if (this.mRtgSchedController == null) {
            this.mRtgSchedController = new RtgSchedController();
        }
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

    private boolean filterAppType(int pid) {
        if (pid == MY_PID) {
            return true;
        }
        if (this.mFilterAppTypeSet.size() == 0) {
            return false;
        }
        int type = -1;
        String pkg = CpuCommonUtil.getPackageNameForPid(pid);
        if (pkg != null) {
            type = AppTypeRecoManager.getInstance().getAppType(pkg);
        }
        if (!this.mFilterAppTypeSet.contains(Integer.valueOf(type))) {
            return false;
        }
        AwareLog.d(TAG, "disable rtg sched for pkg: " + pkg + ", type:" + type);
        return true;
    }

    private boolean isFocusProcessValid(int pid, int type) {
        if (pid <= 0 || !CpuMultiDisplay.getInstance().isPhoneDisplay(pid)) {
            return false;
        }
        if (!filterAppType(pid)) {
            return true;
        }
        if (!this.mRunningRemoteAnimation) {
            if (this.mCurPid > 0) {
                setRtgThread(-1, -1);
                HwActivityManagerServiceEx.notifyAppToTop(this.mCurPid, 0);
            }
            AuxRtgSched.getInstance().onFocusChanged(this.mCurPid, -1);
            this.mCurPid = -1;
            this.mCurTid = -1;
        }
        if (type == 3) {
            this.mCurFocusPid = -1;
            this.mCurFocusTid = -1;
        }
        return false;
    }

    public void setFocusProcess(int pid, int renderTid, int type) {
        if (this.mRtgSchedEnable.get() && isFocusProcessValid(pid, type)) {
            if (type == 0) {
                this.mRunningRemoteAnimation = true;
            } else if (type != 1) {
                if (type != 2) {
                    if (type == 3) {
                        renderTid = CpuCommonUtil.getRenderTid(pid);
                        this.mCurFocusPid = pid;
                        this.mCurFocusTid = renderTid;
                    } else if (type != 4) {
                        AwareLog.w(TAG, "setFocusProcess, unsurpported type: " + type);
                        return;
                    } else {
                        this.mRunningRemoteAnimation = false;
                    }
                }
                renderTid = CpuCommonUtil.getRenderTid(pid);
            } else {
                this.mRunningRemoteAnimation = false;
                pid = this.mCurFocusPid;
                renderTid = this.mCurFocusTid;
            }
            setFocusProcessInner(pid, renderTid, type);
        }
    }

    private void setFocusProcessInner(int pid, int renderTid, int type) {
        AwareLog.d(TAG, "handleFocusProcess pid:" + pid + ", tid:" + renderTid + ", type:" + type);
        if (!this.mRunningRemoteAnimation || type == 0) {
            if (this.mCurPid != pid) {
                AuxRtgSched.getInstance().onFocusChanged(this.mCurPid, pid);
            }
            if (this.mCurPid != pid || this.mCurTid != renderTid) {
                setRtgThread(pid, renderTid);
                HwActivityManagerServiceEx.notifyAppToTop(pid, 1);
                int i = this.mCurPid;
                if (i > 0 && i != pid) {
                    HwActivityManagerServiceEx.notifyAppToTop(i, 0);
                }
                this.mCurPid = pid;
                this.mCurTid = renderTid;
            }
        }
    }

    public void onScreenStateChanged(boolean screenOn) {
        int renderTid;
        if (this.mRtgSchedEnable.get()) {
            AuxRtgSched.getInstance().onScreenStateChanged(screenOn);
            if (screenOn) {
                int i = this.mCurPid;
                if (i > 0 && (renderTid = HwActivityManagerServiceEx.getRenderTid(i)) >= 0) {
                    setRtgThread(this.mCurPid, renderTid);
                    HwActivityManagerServiceEx.notifyAppToTop(this.mCurPid, 1);
                    return;
                }
                return;
            }
            setRtgThread(-1, -1);
            int i2 = this.mCurPid;
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
        AuxRtgSched.getInstance().onProcessDied(pid);
        int i = this.mCurPid;
        if (i == pid) {
            if (i == this.mCurFocusPid || !CpuMultiDisplay.getInstance().isPhoneDisplay(this.mCurFocusPid)) {
                this.mCurPid = -1;
                this.mCurTid = -1;
                this.mCurFocusPid = -1;
                this.mCurFocusTid = -1;
            } else {
                setFocusProcess(this.mCurFocusPid, this.mCurFocusTid, 3);
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
