package com.android.server.rms.iaware.cpu;

import android.app.ActivityManager;
import android.content.Context;
import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.CommonUtils;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hardware.iawareperfpolicy.IPerfPolicyAdapter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CpuFeature extends RFeature {
    private static final int BIGDATA_RECORD_PERIOD = 600;
    private static final int BYTE_ALLOCATION_SIZE = 2;
    private static final int BYTE_BUFFER_SIZE = 4;
    private static final int CYCLE_MAX_NUM = 5;
    private static final int INVALID_VALUE = -1;
    public static final String KEY_BUNDLE_PID = "KEY_PID";
    public static final String KEY_BUNDLE_RENDER_TID = "KEY_REANDER_TID";
    public static final int MSG_AUX_COMM_CHANGE = 160;
    public static final int MSG_AUX_RTG_SCHED = 159;
    public static final int MSG_BASE_VALUE = 100;
    public static final int MSG_BINDER_THREAD_CREATE = 151;
    private static final int MSG_BOOST_KILL_SWITCH = 107;
    private static final int MSG_CPUCTL_SUBSWITCH = 140;
    private static final int MSG_CPUFEATURE_OFF = 114;
    public static final int MSG_DELAYED_REFRESH = 162;
    private static final int MSG_ENABLE_EAS = 145;
    public static final int MSG_ENTER_GAME_SCENE = 147;
    public static final int MSG_EXIT_GAME_SCENE = 148;
    public static final int MSG_GAME_SCENE_LEVEL = 157;
    public static final int MSG_HIGH_LOAD_THREAD = 184;
    private static final int MSG_INPUT_EVENT = 158;
    public static final int MSG_MOVETO_BACKGROUND = 105;
    public static final int MSG_MOVETO_K_BACKGROUND = 104;
    public static final int MSG_PROCESS_GROUP_CHANGE = 106;
    public static final int MSG_RESET_BOOST_CPUS = 136;
    public static final int MSG_RESET_ON_FIRE = 154;
    public static final int MSG_RESET_TOP_APP_CPUSET = 117;
    public static final int MSG_RESET_VIP_THREAD = 144;
    public static final int MSG_RM_AUX_THREAD = 161;
    private static final int MSG_SCREEN_OFF = 102;
    private static final int MSG_SCREEN_ON = 101;
    public static final int MSG_SEND_POWER_MODE = 185;
    public static final int MSG_SET_BG_UIDS = 142;
    public static final int MSG_SET_BOOST_CPUS = 135;
    public static final int MSG_SET_CPUCONFIG = 124;
    public static final int MSG_SET_CPUSETCONFIG_SCREENOFF = 127;
    public static final int MSG_SET_CPUSETCONFIG_SCREENON = 126;
    public static final int MSG_SET_FG_CGROUP = 139;
    public static final int MSG_SET_FG_HL_CTL_PARAMS = 182;
    public static final int MSG_SET_FG_UIDS = 141;
    private static final int MSG_SET_FOCUS_PROCESS = 149;
    public static final int MSG_SET_LIMIT_CGROUP = 137;
    private static final int MSG_SET_THREAD_TO_STATIC_VIP = 155;
    public static final int MSG_SET_THREAD_TO_TA = 146;
    public static final int MSG_SET_THREAD_TO_VIP = 153;
    public static final int MSG_SET_TOP_APP_CPUSET = 116;
    public static final int MSG_SET_VIP_THREAD = 143;
    public static final int MSG_SET_VIP_THREAD_PARAMS = 150;
    public static final int MSG_THREAD_BOOST = 115;
    public static final int MSG_UNIPERF_BOOST_OFF = 119;
    public static final int MSG_UNIPERF_BOOST_OFF_SEND = 120;
    public static final int MSG_UNIPERF_BOOST_ON = 118;
    public static final int MSG_UPDATE_FG_UIDS = 183;
    public static final int MSG_VIP_THREAD_DYM_GRAN = 2;
    public static final int MSG_VIP_THREAD_MIGRATION = 3;
    public static final int MSG_VIP_THREAD_SCHED_DELAY = 1;
    private static final String PERF_POLICY_SERVICE = "perfpolicy";
    private static final int SCREEN_OFF_DELAYED = 5000;
    private static final int SEND_BYTE_NULL = -1;
    private static final int SEND_RETRY_TIME = 2;
    private static final int SEND_RETRY_TIME_OUT = -2;
    public static final int SEND_SUCCESS = 1;
    private static final String SMART_POWER = "persist.sys.smart_power";
    private static final int SWITCH_APP_START_ON_FIRE = 131072;
    private static final int SWITCH_APP_WARMCOLD_START = 524288;
    private static final int SWITCH_BG_CPUCTL = 33554432;
    private static final int SWITCH_CPUCTL_FG_DETECT = 1024;
    private static final int SWITCH_EAS_MODE = 8192;
    private static final int SWITCH_FG_HLCTL = 67108864;
    private static final int SWITCH_FIFO_SCHED = 536870912;
    private static final int SWITCH_GAME_SCENE = 16384;
    private static final int SWITCH_KERNEL_BG_CPUCTL = 4;
    private static final int SWITCH_KERNEL_FG_CPUCTL = 1;
    private static final int SWITCH_KERNEL_FG_HLCTL = 8;
    private static final int SWITCH_KERNEL_NATIVE_SCHED = 2;
    private static final int SWITCH_MIN_UTIL_BOOST = 268435456;
    private static final int SWITCH_MULTI_DISPLAY = 134217728;
    private static final int SWITCH_NATIVE_SCHED = 16777216;
    private static final int SWITCH_NET_MANAGE = 512;
    private static final int SWITCH_ON_DEMAND_BOOST = 262144;
    private static final int SWITCH_RTG_SCHED = 1048576;
    private static final int SWITCH_THREAD_BOOST = 16;
    private static final int SWITCH_VIP_MODE = 2048;
    private static final int SWITCH_VIP_QUICKSET = 2097152;
    private static final int SWITCH_VIP_SCHED = 1073741824;
    private static final String TAG = "CpuFeature";
    private static final int TYPE_FOCUS_CHANGE = 3;
    private static boolean sCpuSetEnable;
    private Context mContext;
    private CpuFeatureHandler mCpuFeatureHandler;
    private CpuFreqInteractive mCpuFreqInteractive;
    private CpuGameFreq mCpuGameFreq;
    private CpuNetLink mCpuNetLink;
    private CpuProcessInherit mCpuProcessInherit = null;
    private CpuXmlConfiguration mCpuXmlConfiguration;
    private int mCycleNum = 0;
    private int mFeatureFlag = 0;
    private final Object mLock = new Object();

    public CpuFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        this.mContext = context;
        this.mCpuFeatureHandler = new CpuFeatureHandler();
        this.mCpuFreqInteractive = new CpuFreqInteractive(context);
        this.mCpuGameFreq = new CpuGameFreq();
    }

    private boolean isFeatureEnable(int feature) {
        return sCpuSetEnable && (this.mFeatureFlag & feature) != 0;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (!sCpuSetEnable || data == null) {
            return false;
        }
        int resId = data.getResId();
        if (resId == AwareConstant.ResourceType.RESOURCE_SCREEN_ON.ordinal()) {
            UniPerf.getInstance().uniPerfEvent(20226, "", new int[]{1});
            removeScreenMessages();
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(101, 0);
        } else if (resId == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()) {
            UniPerf.getInstance().uniPerfEvent(20226, "", new int[]{0});
            removeScreenMessages();
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(102, 5000);
        } else if (resId == AwareConstant.ResourceType.RESOURCE_GAME_BOOST.ordinal()) {
            this.mCpuGameFreq.gameFreq(data.getBundle(), isFeatureEnable(SWITCH_EAS_MODE));
        } else if (resId == AwareConstant.ResourceType.RESOURCE_NET_MANAGE.ordinal()) {
            onReportDataNetManager(data);
        } else if (resId == AwareConstant.ResourceType.RESOURCE_WINSTATE.ordinal()) {
            Message msg = this.mCpuFeatureHandler.obtainMessage(MSG_SET_FOCUS_PROCESS);
            msg.obj = data;
            this.mCpuFeatureHandler.sendMessage(msg);
        } else if (resId == AwareConstant.ResourceType.RES_INPUT.ordinal() || resId == AwareConstant.ResourceType.RESOURCE_SCENE_REC.ordinal()) {
            Message msg2 = this.mCpuFeatureHandler.obtainMessage(MSG_INPUT_EVENT);
            msg2.obj = data;
            this.mCpuFeatureHandler.sendMessage(msg2);
            CpuHighLoadManager.getInstance().reportData(data);
        } else {
            AwareLog.d(TAG, "invlaid resId " + resId);
        }
        return true;
    }

    private void onReportDataNetManager(CollectData data) {
        Bundle bundle = data.getBundle();
        if (bundle != null) {
            int enable = bundle.getInt("enbale", 0);
            int mode = bundle.getInt("mode", 0);
            AwareLog.d(TAG, "reportData: RESOURCE_NET_MANAGE enable= " + enable + "mode= " + mode);
            ActivityManager activityManager = null;
            Object obj = this.mContext.getSystemService(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG);
            if (obj instanceof ActivityManager) {
                activityManager = (ActivityManager) obj;
            }
            if (activityManager != null) {
                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                Message msg = NetManager.getInstance().mNetManagerHandler.obtainMessage();
                msg.obj = appProcesses;
                msg.what = 12;
                msg.arg1 = enable;
                msg.arg2 = mode;
                NetManager.getInstance().mNetManagerHandler.sendMessage(msg);
            }
        }
    }

    private void subscribeResourceTypes() {
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_GAME_BOOST, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_NET_MANAGE, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_WINSTATE, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
    }

    private void unsubscribeResourceTypes() {
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_GAME_BOOST, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_NET_MANAGE, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_WINSTATE, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
    }

    private void enableGameScene() {
        if (isFeatureEnable(SWITCH_GAME_SCENE)) {
            CpuGameScene.getInstance().enable(this.mCpuFeatureHandler);
        }
    }

    private void enableThreadMinUtilBoost() {
        if (isFeatureEnable(SWITCH_MIN_UTIL_BOOST)) {
            CpuThreadMinUtilBoost.getInstance().start(this);
        }
    }

    private void enableCpuFifoSched() {
        if (isFeatureEnable(SWITCH_FIFO_SCHED)) {
            CpuFifoSched.getInstance().start(this);
        }
    }

    private void enableVipThread() {
        if (isFeatureEnable(SWITCH_VIP_MODE)) {
            CpuVipThread.getInstance().start(this);
            CpuVipThread.getInstance().setHandler(this.mCpuFeatureHandler);
            enableVipCgroupControl();
        }
    }

    private void enableThreadVipSched() {
        if (isFeatureEnable(SWITCH_VIP_SCHED)) {
            CpuVipThread.getInstance().enableThreadVipSched();
        }
    }

    private void notifyCpusetSwitch(boolean enable, int subSwitch) {
        HwActivityManager.setCpusetSwitch(enable, subSwitch);
    }

    private void notifyWarmColdSwitch(boolean enable) {
        HwActivityTaskManager.setWarmColdSwitch(enable);
    }

    private void enableCpuHighFgControl() {
        if (isFeatureEnable(1024)) {
            CpuHighFgControl.getInstance().start(this);
            handleSendMsg(MSG_CPUCTL_SUBSWITCH);
        }
    }

    private void enableVipCgroupControl() {
        VipCgroupControl.getInstance().enable(this);
    }

    private void enableCpuNetLink() {
        if ((isFeatureEnable(1024) || isFeatureEnable(SWITCH_VIP_MODE) || isFeatureEnable(16) || isFeatureEnable(SWITCH_RTG_SCHED)) && this.mCpuNetLink == null) {
            this.mCpuNetLink = new CpuNetLink();
            this.mCpuNetLink.start();
        }
    }

    private void getEasSwitch() {
        if (isFeatureEnable(SWITCH_EAS_MODE)) {
            sendPacketByMsgCode(MSG_ENABLE_EAS);
        }
    }

    private void enableNetManager() {
        if (isFeatureEnable(512)) {
            NetManager.getInstance().enable(this);
        }
    }

    private void sendSubSwitchToUniperf() {
        UniPerf.getInstance().uniPerfEvent(20225, "", new int[]{this.mFeatureFlag});
    }

    private void enableAppStartOnFire() {
        if (isFeatureEnable(SWITCH_APP_START_ON_FIRE)) {
            CpuAppStartOnFire.getInstance().enable(this.mCpuFeatureHandler);
        }
    }

    private void enableRtgSchedPlugin() {
        if (isFeatureEnable(SWITCH_RTG_SCHED)) {
            AwareRmsRtgSchedPlugin.getInstance().enable(this.mCpuFeatureHandler);
        }
    }

    private void enableHighLoadSched() {
        boolean isNativeCtlEnable = isFeatureEnable(SWITCH_NATIVE_SCHED) || isFeatureEnable(SWITCH_BG_CPUCTL);
        boolean isFgHlCtlEnable = isFeatureEnable(SWITCH_FG_HLCTL);
        if (isNativeCtlEnable || isFgHlCtlEnable) {
            CpuHighLoadManager.getInstance().enable(isNativeCtlEnable, isFgHlCtlEnable);
            handleSendMsg(MSG_CPUCTL_SUBSWITCH);
        }
    }

    private void enableMultiDisplay() {
        if (isFeatureEnable(SWITCH_MULTI_DISPLAY)) {
            CpuMultiDisplay.getInstance().enable(this.mContext);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        if (sCpuSetEnable) {
            AwareLog.e(TAG, "cpuset has already enabled!");
            return true;
        }
        if (this.mCpuXmlConfiguration == null) {
            this.mCpuXmlConfiguration = new CpuXmlConfiguration();
        }
        this.mCpuXmlConfiguration.startSetProperty(this);
        try {
            this.mFeatureFlag = Integer.parseInt(SystemPropertiesEx.get("persist.sys.cpuset.subswitch", "0"));
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "enable parseInt catch exception msg = " + e.getMessage());
        }
        setEnable(true);
        notifyCpusetSwitch(true, this.mFeatureFlag);
        CpuKeyBackground.getInstance().start(this);
        setBoostKillSwitch(true);
        CpuPowerMode.getInstance().enable(this.mContext);
        notifyWarmColdSwitch(isFeatureEnable(SWITCH_APP_WARMCOLD_START));
        if (isFeatureEnable(16)) {
            CpuThreadBoost.getInstance().start(this, this.mCpuFeatureHandler);
        }
        if (this.mCpuProcessInherit == null) {
            this.mCpuProcessInherit = new CpuProcessInherit();
        }
        this.mCpuProcessInherit.registerPorcessObserver();
        subscribeResourceTypes();
        if (isScreenOn()) {
            doScreenOn();
        }
        if (isFeatureEnable(16) || isFeatureEnable(SWITCH_GAME_SCENE)) {
            this.mCpuFreqInteractive.startGameStateMoniter();
        }
        CpuResourceConfigControl.getInstance().enable();
        CpuFeatureAmsCommunicator.getInstance().start(this);
        CpuFeatureAmsCommunicator.getInstance().setOnDemandBoostEnable(isFeatureEnable(SWITCH_ON_DEMAND_BOOST));
        if (isFeatureEnable(SWITCH_ON_DEMAND_BOOST)) {
            sendOnDemandBoostParams();
        }
        AwareLog.d(TAG, "CpuFeature enabled");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion >= CommonUtils.AwareVersion.FIRST.getVersion()) {
            enable();
        }
        if (realVersion >= CommonUtils.AwareVersion.SECOND.getVersion()) {
            getEasSwitch();
            enableCpuNetLink();
            enableCpuHighFgControl();
            enableVipThread();
            enableNetManager();
            enableGameScene();
            sendSubSwitchToUniperf();
            enableAppStartOnFire();
        }
        if (realVersion >= CommonUtils.AwareVersion.FIFTH.getVersion()) {
            enableRtgSchedPlugin();
            enableHighLoadSched();
        }
        if (realVersion < CommonUtils.AwareVersion.SIXTH.getVersion()) {
            return true;
        }
        enableThreadMinUtilBoost();
        enableCpuFifoSched();
        enableThreadVipSched();
        enableMultiDisplay();
        return true;
    }

    private void disableCpuHighFgControl() {
        if (isFeatureEnable(1024)) {
            CpuHighFgControl.getInstance().stop();
            handleSendMsg(MSG_CPUCTL_SUBSWITCH);
        }
    }

    private void disableThreadMinUtilBoost() {
        if (isFeatureEnable(SWITCH_MIN_UTIL_BOOST)) {
            CpuThreadMinUtilBoost.getInstance().stop();
        }
    }

    private void disableCpuFifoSched() {
        if (isFeatureEnable(SWITCH_FIFO_SCHED)) {
            CpuFifoSched.getInstance().stop();
        }
    }

    private void disableVipThread() {
        if (isFeatureEnable(SWITCH_VIP_MODE)) {
            CpuVipThread.getInstance().stop();
            disableVipCgroupControl();
        }
    }

    private void disableThreadVipSched() {
        if (isFeatureEnable(SWITCH_VIP_SCHED)) {
            CpuVipThread.getInstance().disableThreadVipSched();
        }
    }

    private void disableVipCgroupControl() {
        VipCgroupControl.getInstance().disable();
    }

    private void disableCpuNetLink() {
        CpuNetLink cpuNetLink = this.mCpuNetLink;
        if (cpuNetLink != null) {
            cpuNetLink.stop();
            this.mCpuNetLink = null;
        }
    }

    private void disableNetManager() {
        if (isFeatureEnable(512)) {
            NetManager.getInstance().disable();
        }
    }

    private void disableGameScene() {
        if (isFeatureEnable(SWITCH_GAME_SCENE)) {
            CpuGameScene.getInstance().disable();
        }
    }

    private void disableAppStartOnFire() {
        if (isFeatureEnable(SWITCH_APP_START_ON_FIRE)) {
            CpuAppStartOnFire.getInstance().disable();
        }
    }

    private void disableHighLoadSched() {
        if (isFeatureEnable(SWITCH_NATIVE_SCHED) || isFeatureEnable(SWITCH_BG_CPUCTL) || isFeatureEnable(SWITCH_FG_HLCTL)) {
            CpuHighLoadManager.getInstance().disable();
            handleSendMsg(MSG_CPUCTL_SUBSWITCH);
        }
    }

    private void disableCpuMultiDisplay() {
        if (isFeatureEnable(SWITCH_MULTI_DISPLAY)) {
            CpuMultiDisplay.getInstance().disable();
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (!sCpuSetEnable) {
            AwareLog.e(TAG, "cpuset has already disabled!");
            return true;
        }
        if (isFeatureEnable(16) || isFeatureEnable(SWITCH_GAME_SCENE)) {
            this.mCpuFreqInteractive.stopGameStateMoniter();
        }
        CpuFeatureAmsCommunicator.getInstance().setOnDemandBoostEnable(false);
        AwareLog.d(TAG, "CpuFeature destroyed");
        CpuKeyBackground.getInstance().destroy();
        if (isFeatureEnable(16)) {
            CpuThreadBoost.getInstance().stop();
        }
        if (isFeatureEnable(SWITCH_RTG_SCHED)) {
            AwareRmsRtgSchedPlugin.getInstance().disable();
        }
        disableHighLoadSched();
        disableCpuHighFgControl();
        disableNetManager();
        CpuPowerMode.getInstance().disable();
        disableCpuMultiDisplay();
        disableVipThread();
        disableThreadVipSched();
        disableCpuFifoSched();
        disableThreadMinUtilBoost();
        disableCpuNetLink();
        setBoostKillSwitch(false);
        CpuProcessInherit cpuProcessInherit = this.mCpuProcessInherit;
        if (cpuProcessInherit != null) {
            cpuProcessInherit.unregisterPorcessObserver();
        }
        doFeatureOff();
        disableAppStartOnFire();
        setEnable(false);
        notifyCpusetSwitch(false, 0);
        notifyWarmColdSwitch(false);
        unsubscribeResourceTypes();
        CpuResourceConfigControl.getInstance().disable();
        disableGameScene();
        return true;
    }

    private void setEnable(boolean enable) {
        try {
            SystemPropertiesEx.set("persist.sys.cpuset.enable", enable ? "1" : "0");
            sCpuSetEnable = enable;
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "setEnable catch exception!");
        }
    }

    public static boolean isCpusetEnable() {
        return sCpuSetEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int doScreenOn() {
        AwareLog.d(TAG, "get screen on event");
        sendPacketByMsgCode(101);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int doScreenOff() {
        AwareLog.d(TAG, "get screen off event");
        sendPacketByMsgCode(102);
        return 0;
    }

    private int doFeatureOff() {
        sendPacketByMsgCode(MSG_CPUFEATURE_OFF);
        return 0;
    }

    public int sendPacket(ByteBuffer buffer) {
        synchronized (this.mLock) {
            if (buffer == null) {
                try {
                    return -1;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                int retry = 2;
                while (!IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position())) {
                    retry--;
                    if (retry <= 0) {
                        return SEND_RETRY_TIME_OUT;
                    }
                }
                return 1;
            }
        }
    }

    private boolean isScreenOn() {
        Object obj = this.mContext.getSystemService("power");
        if (obj instanceof PowerManager) {
            return ((PowerManager) obj).isInteractive();
        }
        return false;
    }

    private void removeScreenMessages() {
        this.mCpuFeatureHandler.removeMessages(101);
        this.mCpuFeatureHandler.removeMessages(102);
    }

    public class CpuFeatureHandler extends Handler {
        public CpuFeatureHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 101) {
                CpuFeature.this.doScreenOn();
                AwareRmsRtgSchedPlugin.getInstance().onScreenStateChanged(true);
            } else if (i == 102) {
                CpuFeature.this.doScreenOff();
                CpuGameScene.getInstance().setScreenOffScene();
                AwareRmsRtgSchedPlugin.getInstance().onScreenStateChanged(false);
            } else if (i == 135) {
                CpuFeature.this.sendPacketByMsgCode(CpuFeature.MSG_SET_BOOST_CPUS);
            } else if (i == 136) {
                CpuFeature.this.sendPacketByMsgCode(CpuFeature.MSG_RESET_BOOST_CPUS);
            } else if (i == CpuFeature.MSG_CPUCTL_SUBSWITCH) {
                CpuFeature.this.doSendCpuCtlSwitchMsg();
            } else if (i == 154) {
                CpuAppStartOnFire.getInstance().resetOnFire();
            } else if (i == 143) {
                CpuFeature.this.doVipThreadSet(msg);
            } else if (i == 144) {
                CpuFeature.this.doVipThreadSet(msg);
            } else if (i == 147) {
                CpuGameScene.getInstance().setGameScene();
            } else if (i != 148) {
                handleMessageDefault(msg);
            } else {
                CpuGameScene.getInstance().resetGameScene();
            }
        }

        private void handleMessageDefault(Message msg) {
            Bundle bundle;
            int i = msg.what;
            if (i != CpuFeature.MSG_SET_FOCUS_PROCESS) {
                if (i != 153) {
                    switch (i) {
                        case CpuFeature.MSG_INPUT_EVENT /* 158 */:
                            if (msg.obj instanceof CollectData) {
                                AuxRtgSched.getInstance().onInputEvent((CollectData) msg.obj);
                                return;
                            }
                            return;
                        case CpuFeature.MSG_AUX_RTG_SCHED /* 159 */:
                            AuxRtgSched.getInstance().onAuxRtgTimeOut();
                            return;
                        case CpuFeature.MSG_AUX_COMM_CHANGE /* 160 */:
                            AuxRtgSched.getInstance().onCommChanged(msg.arg1, msg.arg2);
                            return;
                        case CpuFeature.MSG_RM_AUX_THREAD /* 161 */:
                            AuxRtgSched.getInstance().removeAuxThread(msg.arg1, msg.arg2);
                            return;
                        case CpuFeature.MSG_DELAYED_REFRESH /* 162 */:
                            AuxRtgSched.getInstance().refreshCgroup();
                            return;
                        default:
                            AwareLog.e(CpuFeature.TAG, "error msg what = " + msg.what);
                            return;
                    }
                } else {
                    CpuFeature.this.doVipGroupSet(msg);
                }
            } else if ((msg.obj instanceof CollectData) && (bundle = ((CollectData) msg.obj).getBundle()) != null) {
                int pid = bundle.getInt(SceneRecogFeature.DATA_PID, -1);
                int renderTid = bundle.getInt("tid", -1);
                int type = bundle.getInt("type", -1);
                int displayId = bundle.getInt("displayId", -1);
                CpuMultiDisplay.getInstance().addPidDisplayInfo(pid, displayId);
                if (type == 3) {
                    CpuMultiDisplay.getInstance().multiDisplayFocusProcess(pid, displayId);
                }
                if (CpuMultiDisplay.getInstance().isPhoneDisplay(pid)) {
                    AwareRmsRtgSchedPlugin.getInstance().setFocusProcess(pid, renderTid, type);
                    CpuThreadMinUtilBoost.getInstance().focusChangeReport(pid, type);
                }
            }
        }
    }

    private int setBoostKillSwitch(boolean isEnable) {
        if (!isCpusetEnable()) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_BOOST_KILL_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        int resCode = sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "setBoostKillSwitch sendPacket failed, isEnable:" + isEnable + ",code:" + resCode);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPacketByMsgCode(int msg) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(msg);
        int resCode = sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "sendPacketByMsgCode sendPacket failed, msg:" + msg + ",code:" + resCode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSendCpuCtlSwitchMsg() {
        int value = 0;
        if (isFeatureEnable(1024)) {
            value = 0 | 1;
        }
        if (isFeatureEnable(SWITCH_NATIVE_SCHED)) {
            value |= 2;
        }
        if (isFeatureEnable(SWITCH_BG_CPUCTL)) {
            value |= 4;
        }
        boolean isPerfMode = CpuPowerMode.isPowerModePerformance(SystemPropertiesEx.getInt(SMART_POWER, 0));
        if (isFeatureEnable(SWITCH_FG_HLCTL) && !isPerfMode) {
            value |= 8;
        }
        sendPacketByMsgValueCode(MSG_CPUCTL_SUBSWITCH, value);
    }

    private void handleSendMsg(int msg) {
        this.mCpuFeatureHandler.removeMessages(msg);
        this.mCpuFeatureHandler.sendEmptyMessage(msg);
    }

    private void sendPacketByMsgValueCode(int msg, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(msg);
        buffer.putInt(value);
        int resCode = sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "sendPacketByMsgPidCode sendPacket failed, msg, value:" + msg + value + ", send error code:" + resCode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doVipGroupSet(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null) {
            AwareLog.e(TAG, "doVipGroupSet inavlid params null bundle!");
            return;
        }
        int pid = bundle.getInt(KEY_BUNDLE_PID);
        int renderThreadTid = bundle.getInt(KEY_BUNDLE_RENDER_TID);
        if (pid > 0) {
            int msgId = msg.what;
            ArrayList<Integer> threads = new ArrayList<>();
            threads.add(Integer.valueOf(pid));
            threads.add(Integer.valueOf(renderThreadTid));
            CpuVipThread.getInstance().sendPacket(pid, threads, msgId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doVipThreadSet(Message msg) {
        int msgId = msg.what;
        int pid = msg.arg1;
        if (msg.obj != null && pid > 0) {
            List<Integer> threads = (List) msg.obj;
            if (isFeatureEnable(SWITCH_VIP_QUICKSET) && (msgId == 143 || msgId == 144)) {
                boolean isSet = msgId == 143;
                AwareLog.d(TAG, "doVipThreadSet pid : " + pid + ", threads : " + threads);
                for (Integer num : threads) {
                    ResourceCollector.setThreadVip(pid, num.intValue(), isSet);
                }
            }
            if (isFeatureEnable(SWITCH_VIP_SCHED)) {
                CpuVipThread.getInstance().setThreadVipPolicy(msgId, pid, threads);
            }
            if (msgId == 143 && msg.arg2 == -1) {
                msgId = MSG_SET_THREAD_TO_STATIC_VIP;
            }
            CpuVipThread.getInstance().sendPacket(pid, threads, msgId);
        }
    }

    private void sendOnDemandBoostParams() {
        IPerfPolicyAdapter perfPolicy = new IPerfPolicyAdapter();
        try {
            if (perfPolicy.getService(PERF_POLICY_SERVICE)) {
                int coldstartDuration = OnDemandBoost.getInstance().getColdStartDuration();
                int winswitchDuration = OnDemandBoost.getInstance().getWindowSwitchDuration();
                AwareLog.d(TAG, "sendOnDemandBoostParams coldstartDuration = " + coldstartDuration + " winswitchDuration = " + winswitchDuration);
                perfPolicy.setOnDemandBoostPolicy("cold", coldstartDuration);
                perfPolicy.setOnDemandBoostPolicy("winswitch", winswitchDuration);
            }
        } catch (RemoteException | NoSuchElementException e) {
            AwareLog.e(TAG, "sendOnDemandBoostParams catch exception = " + e.getMessage());
        }
    }
}
