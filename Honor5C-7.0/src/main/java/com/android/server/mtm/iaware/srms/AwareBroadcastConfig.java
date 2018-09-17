package com.android.server.mtm.iaware.srms;

import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
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
    private static AwareBroadcastConfig sInstance;
    private final ArraySet<String> mAwareProxySysPkgList;
    private final ArraySet<String> mAwareUnProxyActionList;
    private final ArraySet<String> mAwareUnProxyPkgList;
    private int mFGAppLevel;
    private AtomicBoolean mHasReadXml;
    private int mNoTouchCheckCount;
    private int mTouchCheckCount;
    private int mUnProxyMaxDuration;
    private int mUnProxyMaxSpeed;
    private int mUnProxyMinSpeed;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.srms.AwareBroadcastConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.srms.AwareBroadcastConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.srms.AwareBroadcastConfig.<clinit>():void");
    }

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
        this.mHasReadXml = new AtomicBoolean(false);
        this.mAwareUnProxyActionList = new ArraySet();
        this.mAwareUnProxyPkgList = new ArraySet();
        this.mAwareProxySysPkgList = new ArraySet();
        this.mFGAppLevel = UNPROXY_MAX_SPEED_INDEX;
        this.mTouchCheckCount = 60;
        this.mNoTouchCheckCount = WifiProCommonUtils.HTTP_REACHALBE_HOME;
        this.mUnProxyMaxDuration = 20000;
        this.mUnProxyMaxSpeed = 150;
        this.mUnProxyMinSpeed = 20;
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                AwareLog.d(AwareBroadcastConfig.TAG, "IUpdateWhiteListCallback update whiteList.");
                AwareBroadcastConfig.this.mHasReadXml.set(false);
                AwareBroadcastConfig.this.updateConfigFromRMS();
            }
        };
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
            updateUnProxyActionFromRMS(38);
            updateUnProxyPkgFromRMS(38);
            updateFgAppLevelFromRMS(38);
            updateProxyParamFromRMS(38);
            updateProxySysPkgFromRMS(38);
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
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, TOUCH_NO_CHACK_COUNT_INDEX);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy actions from rms :" + str);
            }
            ArraySet<String> actionList = new ArraySet();
            String[] contentArray = str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = contentArray.length;
            for (int i = TOUCH_CHACK_COUNT_INDEX; i < length; i += TOUCH_NO_CHACK_COUNT_INDEX) {
                String content = contentArray[i].trim();
                if (!content.isEmpty()) {
                    actionList.add(content);
                }
            }
            synchronized (this.mAwareUnProxyActionList) {
                this.mAwareUnProxyActionList.clear();
                this.mAwareUnProxyActionList.addAll(actionList);
            }
        }
    }

    private void updateUnProxyPkgFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, UNPROXY_MAX_DURATION_INDEX);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get unproxy pkgs from rms :" + str);
            }
            ArraySet<String> pkgList = new ArraySet();
            String[] contentArray = str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = contentArray.length;
            for (int i = TOUCH_CHACK_COUNT_INDEX; i < length; i += TOUCH_NO_CHACK_COUNT_INDEX) {
                String content = contentArray[i].trim();
                if (!content.isEmpty()) {
                    pkgList.add(content);
                }
            }
            synchronized (this.mAwareUnProxyPkgList) {
                this.mAwareUnProxyPkgList.clear();
                this.mAwareUnProxyPkgList.addAll(pkgList);
            }
        }
    }

    private void updateFgAppLevelFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, UNPROXY_MAX_SPEED_INDEX);
        if (str != null && !str.isEmpty()) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get fg app level from rms :" + str);
            }
            if (str.startsWith(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(TOUCH_NO_CHACK_COUNT_INDEX);
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
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, UNPROXY_MIN_SPEED_INDEX);
        if (str != null && !str.isEmpty()) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get proxy param from rms :" + str);
            }
            if (str.startsWith(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                str = str.substring(TOUCH_NO_CHACK_COUNT_INDEX);
            }
            String[] contentArray = str.trim().split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (contentArray.length != SIZE_PROXY_PARAM) {
                AwareLog.e(TAG, "proxy param size error in config");
                return;
            }
            List<Integer> actionList = new ArrayList(SIZE_PROXY_PARAM);
            int i = TOUCH_CHACK_COUNT_INDEX;
            int length = contentArray.length;
            while (i < length) {
                String content = contentArray[i].trim();
                if (!content.isEmpty()) {
                    try {
                        actionList.add(Integer.valueOf(Integer.parseInt(content)));
                        i += TOUCH_NO_CHACK_COUNT_INDEX;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "proxy param error in config");
                        return;
                    }
                }
                return;
            }
            int touchCheckCount = ((Integer) actionList.get(TOUCH_CHACK_COUNT_INDEX)).intValue();
            int noTouchCheckCount = ((Integer) actionList.get(TOUCH_NO_CHACK_COUNT_INDEX)).intValue();
            int unProxyMaxDuration = ((Integer) actionList.get(UNPROXY_MAX_DURATION_INDEX)).intValue();
            int unProxyMaxSpeed = ((Integer) actionList.get(UNPROXY_MAX_SPEED_INDEX)).intValue();
            int unProxyMinSpeed = ((Integer) actionList.get(UNPROXY_MIN_SPEED_INDEX)).intValue();
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
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, SIZE_PROXY_PARAM);
        if (str != null) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "get proxy sys pkgs from rms :" + str);
            }
            ArraySet<String> pkgList = new ArraySet();
            String[] contentArray = str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = contentArray.length;
            for (int i = TOUCH_CHACK_COUNT_INDEX; i < length; i += TOUCH_NO_CHACK_COUNT_INDEX) {
                String content = contentArray[i].trim();
                if (!content.isEmpty()) {
                    pkgList.add(content);
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
