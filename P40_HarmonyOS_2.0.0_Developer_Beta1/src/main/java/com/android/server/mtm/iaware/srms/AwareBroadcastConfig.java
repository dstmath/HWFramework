package com.android.server.mtm.iaware.srms;

import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.cpu.CpuFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareBroadcastConfig {
    private static final Object LOCK = new Object();
    private static final int PROXY_BR_FGAPPLEVAL_ID = 3;
    private static final int PROXY_BR_PROXY_PARAM_ID = 4;
    private static final int PROXY_BR_TRIM_ACTION_ID = 6;
    private static final int PROXY_BR_UNPROXY_ACTION_ID = 1;
    private static final int PROXY_BR_UNPROXY_PKG_ID = 2;
    private static final int PROXY_BR_UNPROXY_SYS_ID = 5;
    private static final int SIZE_PROXY_PARAM = 7;
    private static final String TAG = "AwareBroadcastConfig";
    private static final int TOUCH_CHACK_COUNT_INDEX = 0;
    private static final int TOUCH_NO_CHACK_COUNT_INDEX = 1;
    private static final int UNPROXY_HIGH_SPEED_INDEX = 6;
    private static final int UNPROXY_MAX_DURATION_INDEX = 2;
    private static final int UNPROXY_MAX_SPEED_INDEX = 3;
    private static final int UNPROXY_MIDDLE_SPEED_INDEX = 5;
    private static final int UNPROXY_MIN_SPEED_INDEX = 4;
    private static final int UNPROXY_SYS_SPLIT_LENGTH = 2;
    private static AwareBroadcastConfig sInstance = null;
    private final ArraySet<String> mAwareProxyTrimActionList = new ArraySet<>();
    private final ArraySet<String> mAwareUnProxyActionList = new ArraySet<>();
    private final ArraySet<String> mAwareUnProxyPkgList = new ArraySet<>();
    private final ArrayMap<String, ArraySet<String>> mAwareUnProxySys = new ArrayMap<>();
    private int mFgAppLevel = 6;
    private AtomicBoolean mHasReadXml = new AtomicBoolean(false);
    private int mNoTouchCheckCount = MultiTaskManagerService.MSG_POLICY_BR;
    private int mTouchCheckCount = 60;
    private int mUnProxyHighSpeed = 20;
    private int mUnProxyMaxDuration = 20000;
    private int mUnProxyMaxSpeed = CpuFeature.MSG_SET_VIP_THREAD_PARAMS;
    private int mUnProxyMiddleSpeed = 40;
    private int mUnProxyMinSpeed = 60;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new IUpdateWhiteListCallback.Stub() {
        /* class com.android.server.mtm.iaware.srms.AwareBroadcastConfig.AnonymousClass1 */

        public void update() throws RemoteException {
            AwareLog.d(AwareBroadcastConfig.TAG, "IUpdateWhiteListCallback update whiteList.");
            AwareBroadcastConfig.this.mHasReadXml.set(false);
            AwareBroadcastConfig.this.updateConfigFromRms();
        }
    };

    public static AwareBroadcastConfig getInstance() {
        AwareBroadcastConfig awareBroadcastConfig;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareBroadcastConfig();
            }
            awareBroadcastConfig = sInstance;
        }
        return awareBroadcastConfig;
    }

    private AwareBroadcastConfig() {
    }

    public void doinit() {
        if (!HwSysResManager.getInstance().registerResourceCallback(this.mUpdateWhiteListCallback)) {
            AwareLog.e(TAG, "IUpdateWhiteListCallback register failed");
        }
        updateConfigFromRms();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfigFromRms() {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "updateConfigFromRms begin.");
        }
        if (!this.mHasReadXml.get()) {
            updateUnProxyActionFromRms(32);
            updateUnProxyPkgFromRms(32);
            updateFgAppLevelFromRms(32);
            updateProxyParamFromRms(32);
            updateUnProxySysFromRms(32);
            updateTrimActionFromRms(32);
            this.mHasReadXml.set(true);
            if (MultiTaskManagerService.self() != null) {
                AwareBroadcastPolicy iawareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
                if (iawareBrPolicy != null) {
                    iawareBrPolicy.updateXmlConfig();
                } else {
                    AwareLog.e(TAG, "updateConfigFromRms exception, don't get polciy.");
                }
            } else {
                AwareLog.e(TAG, "updateConfigFromRms exception, don't get mtm service.");
            }
        }
    }

    private void updateUnProxyActionFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 1);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy actions from rms :" + str);
            }
            ArraySet<String> actionList = new ArraySet<>();
            for (String content : str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content.trim();
                if (!content2.isEmpty()) {
                    actionList.add(content2);
                }
            }
            synchronized (this.mAwareUnProxyActionList) {
                this.mAwareUnProxyActionList.clear();
                this.mAwareUnProxyActionList.addAll((ArraySet<? extends String>) actionList);
            }
        }
    }

    private void updateUnProxyPkgFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 2);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy pkgs from rms :" + str);
            }
            ArraySet<String> pkgList = new ArraySet<>();
            for (String content : str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content.trim();
                if (!content2.isEmpty()) {
                    pkgList.add(content2);
                }
            }
            synchronized (this.mAwareUnProxyPkgList) {
                this.mAwareUnProxyPkgList.clear();
                this.mAwareUnProxyPkgList.addAll((ArraySet<? extends String>) pkgList);
            }
        }
    }

    private void updateFgAppLevelFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 3);
        if (str != null && !str.isEmpty()) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get fg app level from rms :" + str);
            }
            if (str.startsWith(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(1);
            }
            try {
                int fgAppLevel = Integer.parseInt(str.trim());
                if (fgAppLevel < 0) {
                    AwareLog.e(TAG, "fg app level error, it is wrong config");
                } else {
                    this.mFgAppLevel = fgAppLevel;
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "fg app level error in config");
            }
        }
    }

    private void updateProxyInfo(List<Integer> actionList) {
        int touchCheckCount = actionList.get(0).intValue();
        int noTouchCheckCount = actionList.get(1).intValue();
        int unProxyMaxDuration = actionList.get(2).intValue();
        int unProxyMaxSpeed = actionList.get(3).intValue();
        int unProxyMinSpeed = actionList.get(4).intValue();
        int unProxyMiddleSpeed = actionList.get(5).intValue();
        int unProxyHighSpeed = actionList.get(6).intValue();
        if (touchCheckCount <= 0 || noTouchCheckCount <= 0) {
            AwareLog.e(TAG, "check count error, it is wrong config");
        } else if (unProxyMaxSpeed <= 0 || unProxyMinSpeed <= 0 || unProxyMaxSpeed < unProxyMinSpeed) {
            AwareLog.e(TAG, "unproxy max speed less than min speed, it is wrong config");
        } else if (unProxyMaxDuration <= 0) {
            AwareLog.e(TAG, "unproxy max duration error, it is wrong config");
        } else if (unProxyMiddleSpeed <= 0 || unProxyHighSpeed <= 0) {
            AwareLog.e(TAG, "check high or middle error, it is wrong config");
        } else {
            this.mTouchCheckCount = touchCheckCount;
            this.mNoTouchCheckCount = noTouchCheckCount;
            this.mUnProxyMaxDuration = unProxyMaxDuration;
            this.mUnProxyMaxSpeed = unProxyMaxSpeed;
            this.mUnProxyMinSpeed = unProxyMinSpeed;
            this.mUnProxyMiddleSpeed = unProxyMiddleSpeed;
            this.mUnProxyHighSpeed = unProxyHighSpeed;
        }
    }

    private void updateProxyParamFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 4);
        if (!(str == null || str.isEmpty())) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get proxy param from rms :" + str);
            }
            if (str.startsWith(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(1);
            }
            String[] contentArray = str.trim().split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (contentArray.length != 7) {
                AwareLog.e(TAG, "proxy param size error in config");
                return;
            }
            List<Integer> actionList = new ArrayList<>(7);
            for (String content : contentArray) {
                String content2 = content.trim();
                if (!content2.isEmpty()) {
                    try {
                        actionList.add(Integer.valueOf(Integer.parseInt(content2)));
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "proxy param error in config");
                        return;
                    }
                } else {
                    return;
                }
            }
            updateProxyInfo(actionList);
        }
    }

    private void updateTrimActionFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 6);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get trim action from rms :" + str);
            }
            ArraySet<String> actionList = new ArraySet<>();
            for (String str2 : str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content = str2.trim();
                if (!content.isEmpty()) {
                    actionList.add(content);
                }
            }
            synchronized (this.mAwareProxyTrimActionList) {
                this.mAwareProxyTrimActionList.clear();
                this.mAwareProxyTrimActionList.addAll((ArraySet<? extends String>) actionList);
            }
        }
    }

    private void updateOneUnProxySysFromRms(String content, ArrayMap<String, ArraySet<String>> sysApp) {
        if (!content.isEmpty()) {
            String[] pkgItems = content.split(":");
            if (pkgItems.length == 2) {
                ArraySet<String> actions = new ArraySet<>();
                String sysPkg = pkgItems[0].trim();
                for (String str : pkgItems[1].trim().split(",")) {
                    actions.add(str.trim());
                }
                ArraySet<String> actionsExist = sysApp.get(sysPkg);
                if (actionsExist != null) {
                    actionsExist.addAll((ArraySet<? extends String>) actions);
                } else {
                    sysApp.put(sysPkg, actions);
                }
            } else {
                AwareLog.e(TAG, "check unproxy sys app br error, it is wrong config");
            }
        }
    }

    private void updateUnProxySysFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 5);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get no proxy sys from rms :" + str);
            }
            ArrayMap<String, ArraySet<String>> sysApp = new ArrayMap<>();
            for (String str2 : str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                updateOneUnProxySysFromRms(str2.trim(), sysApp);
            }
            synchronized (this.mAwareUnProxySys) {
                this.mAwareUnProxySys.clear();
                this.mAwareUnProxySys.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) sysApp);
            }
        }
    }

    public ArraySet<String> getUnProxyActionList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareUnProxyActionList) {
            arraySet = new ArraySet<>(this.mAwareUnProxyActionList);
        }
        return arraySet;
    }

    public ArraySet<String> getUnProxyPkgList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareUnProxyPkgList) {
            arraySet = new ArraySet<>(this.mAwareUnProxyPkgList);
        }
        return arraySet;
    }

    public ArrayMap<String, ArraySet<String>> getUnProxySysList() {
        ArrayMap<String, ArraySet<String>> arrayMap;
        synchronized (this.mAwareUnProxySys) {
            arrayMap = new ArrayMap<>(this.mAwareUnProxySys);
        }
        return arrayMap;
    }

    public ArraySet<String> getTrimActionList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareProxyTrimActionList) {
            arraySet = new ArraySet<>(this.mAwareProxyTrimActionList);
        }
        return arraySet;
    }

    public int getFgAppLevel() {
        return this.mFgAppLevel;
    }

    public int getTouchCheckCount() {
        return this.mTouchCheckCount;
    }

    public int getNoTouchCheckCount() {
        return this.mNoTouchCheckCount;
    }

    public int getUnProxyMaxDuration() {
        return this.mUnProxyMaxDuration;
    }

    public int getUnProxyMaxSpeed() {
        return this.mUnProxyMaxSpeed;
    }

    public int getUnProxyMinSpeed() {
        return this.mUnProxyMinSpeed;
    }

    public int getUnProxyMiddleSpeed() {
        return this.mUnProxyMiddleSpeed;
    }

    public int getUnProxyHighSpeed() {
        return this.mUnProxyHighSpeed;
    }
}
