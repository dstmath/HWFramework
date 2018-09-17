package com.android.server.mtm.iaware.srms;

import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.cpu.CPUFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareBroadcastConfig {
    private static final int PROXYBR_FGAPPLEVAL_ID = 3;
    private static final int PROXYBR_PROXYPARAM_ID = 4;
    private static final int PROXYBR_PROXY_SYSPKG_ID = 5;
    private static final int PROXYBR_UNPROXY_ACTION_ID = 1;
    private static final int PROXYBR_UNPROXY_PKG_ID = 2;
    private static final int SIZE_PROXY_PARAM = 5;
    private static final String TAG = "AwareBroadcastConfig";
    private static final int TOUCH_CHACK_COUNT_INDEX = 0;
    private static final int TOUCH_NO_CHACK_COUNT_INDEX = 1;
    private static final int UNPROXY_MAX_DURATION_INDEX = 2;
    private static final int UNPROXY_MAX_SPEED_INDEX = 3;
    private static final int UNPROXY_MIN_SPEED_INDEX = 4;
    private static AwareBroadcastConfig sInstance = null;
    private final ArraySet<String> mAwareProxySysPkgList = new ArraySet();
    private final ArraySet<String> mAwareUnProxyActionList = new ArraySet();
    private final ArraySet<String> mAwareUnProxyPkgList = new ArraySet();
    private int mFGAppLevel = 3;
    private AtomicBoolean mHasReadXml = new AtomicBoolean(false);
    private int mNoTouchCheckCount = 200;
    private int mTouchCheckCount = 60;
    private int mUnProxyMaxDuration = 20000;
    private int mUnProxyMaxSpeed = CPUFeature.MSG_SET_VIP_THREAD_PARAMS;
    private int mUnProxyMinSpeed = 20;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new Stub() {
        public void update() throws RemoteException {
            AwareLog.d(AwareBroadcastConfig.TAG, "IUpdateWhiteListCallback update whiteList.");
            AwareBroadcastConfig.this.mHasReadXml.set(false);
            AwareBroadcastConfig.this.updateConfigFromRMS();
        }
    };

    public static AwareBroadcastConfig getInstance() {
        AwareBroadcastConfig awareBroadcastConfig;
        synchronized (AwareBroadcastConfig.class) {
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
        updateConfigFromRMS();
    }

    private void updateConfigFromRMS() {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "updateConfigFromRMS begin.");
        }
        if (!this.mHasReadXml.get()) {
            updateUnProxyActionFromRMS(32);
            updateUnProxyPkgFromRMS(32);
            updateFgAppLevelFromRMS(32);
            updateProxyParamFromRMS(32);
            updateProxySysPkgFromRMS(32);
            this.mHasReadXml.set(true);
            if (MultiTaskManagerService.self() != null) {
                AwareBroadcastPolicy iawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
                if (iawareBrPolicy != null) {
                    iawareBrPolicy.updateXmlConfig();
                } else {
                    AwareLog.e(TAG, "updateConfigFromRMS exception, don't get polciy.");
                }
            } else {
                AwareLog.e(TAG, "updateConfigFromRMS exception, don't get mtm service.");
            }
        }
    }

    private void updateUnProxyActionFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 1);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy actions from rms :" + str);
            }
            ArraySet<String> actionList = new ArraySet();
            for (String content : str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content2.trim();
                if (!content2.isEmpty()) {
                    actionList.add(content2);
                }
            }
            synchronized (this.mAwareUnProxyActionList) {
                this.mAwareUnProxyActionList.clear();
                this.mAwareUnProxyActionList.addAll(actionList);
            }
        }
    }

    private void updateUnProxyPkgFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 2);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy pkgs from rms :" + str);
            }
            ArraySet<String> pkgList = new ArraySet();
            for (String content : str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content2.trim();
                if (!content2.isEmpty()) {
                    pkgList.add(content2);
                }
            }
            synchronized (this.mAwareUnProxyPkgList) {
                this.mAwareUnProxyPkgList.clear();
                this.mAwareUnProxyPkgList.addAll(pkgList);
            }
        }
    }

    private void updateFgAppLevelFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 3);
        if (str != null && !str.isEmpty()) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get fg app level from rms :" + str);
            }
            if (str.startsWith(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(1);
            }
            try {
                int fgAppLevel = Integer.parseInt(str.trim());
                if (fgAppLevel < 0) {
                    AwareLog.e(TAG, "fg app level error, it is wrong config");
                } else {
                    this.mFGAppLevel = fgAppLevel;
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "fg app level error in config");
            }
        }
    }

    private void updateProxyParamFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 4);
        if (str != null && !str.isEmpty()) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get proxy param from rms :" + str);
            }
            if (str.startsWith(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(1);
            }
            String[] contentArray = str.trim().split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (contentArray.length != 5) {
                AwareLog.e(TAG, "proxy param size error in config");
                return;
            }
            List<Integer> actionList = new ArrayList(5);
            int i = 0;
            int length = contentArray.length;
            while (i < length) {
                String content = contentArray[i].trim();
                if (!content.isEmpty()) {
                    try {
                        actionList.add(Integer.valueOf(Integer.parseInt(content)));
                        i++;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "proxy param error in config");
                        return;
                    }
                }
                return;
            }
            int touchCheckCount = ((Integer) actionList.get(0)).intValue();
            int noTouchCheckCount = ((Integer) actionList.get(1)).intValue();
            int unProxyMaxDuration = ((Integer) actionList.get(2)).intValue();
            int unProxyMaxSpeed = ((Integer) actionList.get(3)).intValue();
            int unProxyMinSpeed = ((Integer) actionList.get(4)).intValue();
            if (touchCheckCount <= 0 || noTouchCheckCount <= 0) {
                AwareLog.e(TAG, "check count error, it is wrong config");
            } else if (unProxyMaxSpeed <= 0 || unProxyMinSpeed <= 0 || unProxyMaxSpeed < unProxyMinSpeed) {
                AwareLog.e(TAG, "unproxy max speed less than min speed, it is wrong config");
            } else if (unProxyMaxDuration <= 0) {
                AwareLog.e(TAG, "unproxy max duration error, it is wrong config");
            } else {
                this.mTouchCheckCount = touchCheckCount;
                this.mNoTouchCheckCount = noTouchCheckCount;
                this.mUnProxyMaxDuration = unProxyMaxDuration;
                this.mUnProxyMaxSpeed = unProxyMaxSpeed;
                this.mUnProxyMinSpeed = unProxyMinSpeed;
            }
        }
    }

    private void updateProxySysPkgFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 5);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get proxy sys pkgs from rms :" + str);
            }
            ArraySet<String> pkgList = new ArraySet();
            for (String content : str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content2.trim();
                if (!content2.isEmpty()) {
                    pkgList.add(content2);
                }
            }
            synchronized (this.mAwareProxySysPkgList) {
                this.mAwareProxySysPkgList.clear();
                this.mAwareProxySysPkgList.addAll(pkgList);
            }
        }
    }

    public ArraySet<String> getUnProxyActionList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareUnProxyActionList) {
            arraySet = new ArraySet(this.mAwareUnProxyActionList);
        }
        return arraySet;
    }

    public ArraySet<String> getUnProxyPkgList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareUnProxyPkgList) {
            arraySet = new ArraySet(this.mAwareUnProxyPkgList);
        }
        return arraySet;
    }

    public ArraySet<String> getProxySysPkgList() {
        ArraySet<String> arraySet;
        synchronized (this.mAwareProxySysPkgList) {
            arraySet = new ArraySet(this.mAwareProxySysPkgList);
        }
        return arraySet;
    }

    public int getFGAppLevel() {
        return this.mFGAppLevel;
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
}
