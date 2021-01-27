package com.android.server.mtm.iaware.appmng.appfreeze;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.huawei.android.os.SystemPropertiesEx;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareNativeFreezeManager {
    public static final int DEXOPT_COMPILE_NORMAL = 0;
    public static final int DEXOPT_FAST_COMPILE_BEGIN = 1;
    public static final int DEXOPT_FAST_COMPILE_END = 2;
    public static final int EVENT_DEX2OAT_INSTALL = 1;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final int FROZEN_BUF_SIZE = 8;
    private static final int FROZEN_BUF_SIZE_WITH_PID = 16;
    private static final String ITEM_CONFIG_DURATION = "installer_duration";
    private static final String ITEM_CONFIG_NAME = "installer_mgr";
    private static final String KEY_GROUP_INFO_EXTRA = "group_info";
    private static final String KEY_PID_EXTRA = "pid";
    private static final String KEY_UID_EXTRA = "uid";
    private static final int MAX_RETRY_GET_SERVICE = 6;
    private static final int MAX_SHORT_FRZ_DURATION = 10000;
    private static final int MIN_SETTING_DURATION = 1000;
    private static final int MSG_ACTIVITY_STARTING = 4;
    private static final int MSG_APP_SLIPPING = 5;
    private static final int MSG_APP_SLIP_END = 6;
    private static final int MSG_CAMERA_SHOT = 7;
    private static final int MSG_FROZEN_TIMEOUT = 10;
    private static final int MSG_GROUP_CHANGE = 2;
    private static final int MSG_INIT_INSTALLER_LIST = 3;
    private static final int MSG_NATIVE_MNG = 1;
    private static final int MSG_PROXIMITY_SCREEN_OFF = 12;
    private static final int MSG_PROXIMITY_SCREEN_ON = 11;
    private static final int MSG_SCREEN_OFF = 8;
    private static final int MSG_SCREEN_ON = 9;
    private static final int MSG_SKIPPED_FRAME = 13;
    private static final String PROP_SHORT_FRZ_DUR = "short_frz_dur";
    private static final String PROP_SLIDE_DURATION = "slide_frz_dur";
    private static final String PROP_UNFRZ_DUR = "unfrz_dur";
    private static final int RETRY_DURATION = 2000;
    private static final int SHORT_FROZEN_DURATION = 3000;
    private static final int SLIDE_FROZEN_DURATION = 6000;
    private static final int SOCK_FROZEN_EVENT = 0;
    private static final int SOCK_INIT_EVENT = 2;
    private static final int SOCK_MSG_INSTALLER_MGR = 501;
    private static final int SOCK_UNFROZEN_EVENT = 1;
    private static final String TAG = "AwareNativeFreezeManager";
    private static final int UNFROZEN_DRUATION = 1000;
    private SparseArray<BaseInfo> mAppEventMap = new SparseArray<>();
    private AwareNativeHandler mAwareNativeHandler = new AwareNativeHandler();
    private AwareSceneRecognizeCallback mAwareSceneRecognizeCallback = new AwareSceneRecognizeCallback();
    private Set<String> mFgPackageSet = new ArraySet();
    private Set<String> mInstallerSet = new ArraySet();
    private AtomicBoolean mIsEnable = new AtomicBoolean(false);
    private AtomicBoolean mIsFreeze = new AtomicBoolean(false);
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(false);
    private AtomicBoolean mIsSilde = new AtomicBoolean(false);
    private int mLastFrozenDuration = 0;
    private long mLastFrozenTimestamp = 0;
    private int mLastSceneEvent = 0;
    private long mLastSceneTimestamp = 0;
    private int mRetries = 0;
    private int mShortFrozenDurtion = SHORT_FROZEN_DURATION;
    private int mSlideFrozenDuration = SLIDE_FROZEN_DURATION;
    private int mUnfrozenDurtion = 1000;

    /* access modifiers changed from: private */
    public class AwareNativeHandler extends Handler {
        private AwareNativeHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (msg.obj instanceof CollectData) {
                        AwareNativeFreezeManager.this.processNativeEvent(msg.arg1, (CollectData) msg.obj);
                        return;
                    }
                    return;
                case 2:
                    AwareNativeFreezeManager.this.processActivitiesChanged(msg.getData());
                    return;
                case 3:
                    AwareNativeFreezeManager.this.initInstallerList();
                    return;
                case 4:
                    AwareNativeFreezeManager awareNativeFreezeManager = AwareNativeFreezeManager.this;
                    awareNativeFreezeManager.doFrozenForScene(4, awareNativeFreezeManager.mShortFrozenDurtion);
                    return;
                case 5:
                    AwareNativeFreezeManager.this.mLastSceneEvent = 0;
                    AwareNativeFreezeManager.this.mIsSilde.set(true);
                    return;
                case 6:
                    AwareNativeFreezeManager.this.mIsSilde.set(false);
                    return;
                case 7:
                    AwareNativeFreezeManager awareNativeFreezeManager2 = AwareNativeFreezeManager.this;
                    awareNativeFreezeManager2.doFrozenForScene(7, awareNativeFreezeManager2.mShortFrozenDurtion);
                    return;
                case 8:
                    AwareNativeFreezeManager.this.mIsScreenOn.set(false);
                    AwareNativeFreezeManager.this.doUnfrozenForScene();
                    return;
                case 9:
                    AwareNativeFreezeManager.this.mIsScreenOn.set(true);
                    return;
                case 10:
                    AwareNativeFreezeManager.this.doUnfrozenForScene();
                    return;
                case 11:
                    AwareNativeFreezeManager awareNativeFreezeManager3 = AwareNativeFreezeManager.this;
                    awareNativeFreezeManager3.doFrozenForScene(11, awareNativeFreezeManager3.mShortFrozenDurtion);
                    return;
                case 12:
                    AwareNativeFreezeManager awareNativeFreezeManager4 = AwareNativeFreezeManager.this;
                    awareNativeFreezeManager4.doFrozenForScene(12, awareNativeFreezeManager4.mShortFrozenDurtion);
                    return;
                case AwareNativeFreezeManager.MSG_SKIPPED_FRAME /* 13 */:
                    AwareNativeFreezeManager.this.handleSkippedFrameFreeze(AwareNativeFreezeManager.MSG_SKIPPED_FRAME);
                    return;
                default:
                    return;
            }
        }
    }

    public void start(Context context) {
        if ("0".equals(SystemPropertiesEx.get("persist.sys.app.installer", "1"))) {
            this.mIsEnable.set(false);
            return;
        }
        if (context != null) {
            Object obj = context.getSystemService("power");
            if (obj instanceof PowerManager) {
                PowerManager pm = (PowerManager) obj;
                if (pm != null) {
                    this.mIsScreenOn.set(pm.isInteractive());
                }
            } else {
                return;
            }
        }
        registerAwareSceneRecognize();
        this.mAwareNativeHandler.sendEmptyMessage(3);
        sendFrozenMsg(2);
        this.mIsEnable.set(true);
    }

    public void destroy() {
        this.mIsEnable.set(false);
        unregisterAwareSceneRecognize();
    }

    public void reportData(int event, CollectData data) {
        if (this.mIsEnable.get()) {
            Message msg = this.mAwareNativeHandler.obtainMessage(1);
            msg.arg1 = event;
            msg.obj = data;
            this.mAwareNativeHandler.sendMessage(msg);
        }
    }

    public void onFgActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        if (this.mIsEnable.get()) {
            Message msg = this.mAwareNativeHandler.obtainMessage(2);
            Bundle bundle = new Bundle();
            bundle.putInt("pid", pid);
            bundle.putInt("uid", uid);
            bundle.putBoolean(KEY_GROUP_INFO_EXTRA, foregroundActivities);
            msg.setData(bundle);
            this.mAwareNativeHandler.sendMessage(msg);
        }
    }

    private void sendFrozenMsg(int code) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(SOCK_MSG_INSTALLER_MGR);
        buffer.putInt(code);
        IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        AwareLog.d(TAG, "sendFrozenMsg code = " + code);
    }

    private void sendFrozenMsg(int code, int pid, int ppid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(SOCK_MSG_INSTALLER_MGR);
        buffer.putInt(code);
        buffer.putInt(pid);
        buffer.putInt(ppid);
        IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        AwareLog.d(TAG, "sendFrozenMsg code = " + code + " pid = " + pid);
    }

    private void sendFrozenMsg(boolean frozen, int pid, int ppid) {
        sendFrozenMsg(!frozen ? 1 : 0, pid, ppid);
    }

    private boolean sendFrozenMsg(boolean frozen, BaseInfo info) {
        if (info == null || !info.isValidInfo() || ((!info.isFrozen) ^ frozen)) {
            return false;
        }
        info.isFrozen = frozen;
        sendFrozenMsg(frozen, info.pid, info.ppid);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processNativeEvent(int eventType, CollectData data) {
        Bundle bundle;
        if (data != null && (bundle = data.getBundle()) != null && eventType == 1) {
            processInstallerEvent(eventType, bundle);
        }
    }

    private void processInstallerEvent(int eventType, Bundle bundle) {
        int eventId = bundle.getInt("eventId", 0);
        if (eventId == 0 || eventId == 1) {
            String installerPkgName = bundle.getString("installer_name", "");
            String pkgName = bundle.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME, "");
            int installerUid = bundle.getInt("installer_uid", 0);
            if (eventId == 0) {
                this.mAppEventMap.put(eventType, new InstallerInfo(installerPkgName, installerUid, pkgName));
            } else {
                this.mAppEventMap.remove(eventType);
                this.mIsFreeze.set(false);
            }
            AwareLog.d(TAG, "installPackage eventId = " + eventId + " installerPkgName = " + installerPkgName + " pkgName = " + pkgName);
        } else if (eventId == 2) {
            int dexoptPid = bundle.getInt("dexopt_pid", 0);
            int ppid = bundle.getInt("ppid", 0);
            int status = bundle.getInt("status", 0);
            String num = bundle.getString("num", "0");
            String time = bundle.getString("time", "0");
            String pkgName2 = bundle.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME, "");
            if (status == 1) {
                SystemPropertiesEx.set("persist.sys.aware.compile.prop.num", num);
                SystemPropertiesEx.set("persist.sys.aware.compile.prop.time", time);
                this.mAppEventMap.put(eventType, new InstallerInfo("fast_compile_thread", 0, pkgName2));
            } else if (status == 2) {
                this.mAppEventMap.remove(eventType);
                this.mIsFreeze.set(false);
            } else if (status == 0) {
                BaseInfo baseInfo = this.mAppEventMap.get(eventType);
                if (baseInfo instanceof InstallerInfo) {
                    InstallerInfo installerInfo = (InstallerInfo) baseInfo;
                    if (pkgName2 != null && pkgName2.equals(installerInfo.pkgName)) {
                        setInstallerInfo(installerInfo, dexoptPid, ppid);
                        installerInfo.onEventProcess(eventId);
                    }
                } else {
                    return;
                }
            } else {
                AwareLog.d(TAG, "not handle status " + status);
            }
            AwareLog.d(TAG, "installPackage EVENT_FORK_NOTIFY status = " + status + " dexoptPid = " + dexoptPid + " pkgName = " + pkgName2 + " num = " + num + " time = " + time);
        }
    }

    private void setInstallerInfo(InstallerInfo installerInfo, int dexoptPid, int ppid) {
        installerInfo.pid = dexoptPid;
        installerInfo.ppid = ppid;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processActivitiesChanged(Bundle bundle) {
        if (bundle != null) {
            int uid = bundle.getInt("uid", 0);
            boolean foregroundActivities = bundle.getBoolean(KEY_GROUP_INFO_EXTRA, false);
            String pkgName = InnerUtils.getPackageNameByUid(uid);
            if (foregroundActivities) {
                this.mFgPackageSet.add(pkgName);
            } else {
                this.mFgPackageSet.remove(pkgName);
            }
            int size = this.mAppEventMap.size();
            for (int i = 0; i < size; i++) {
                BaseInfo info = this.mAppEventMap.valueAt(i);
                if (info != null && info.onActivityChanged()) {
                    sendFrozenMsg(false, info);
                    AwareLog.d(TAG, "processActivitiesChanged unfrozen pid = " + info.pid + " for ActivitiesChanged");
                }
            }
        }
    }

    private void registerAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.registerStateCallback(this.mAwareSceneRecognizeCallback, 1);
        }
    }

    private void unregisterAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.unregisterStateCallback(this.mAwareSceneRecognizeCallback);
        }
    }

    /* access modifiers changed from: private */
    public class AwareSceneRecognizeCallback implements AwareSceneRecognize.IAwareSceneRecCallback {
        private AwareSceneRecognizeCallback() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback
        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            if (AwareNativeFreezeManager.this.mIsEnable.get()) {
                AwareLog.d(AwareNativeFreezeManager.TAG, "onStateChanged sceneType = " + sceneType + " eventType = " + eventType + " pkgName = " + pkgName);
                int what = -1;
                if (eventType == 1) {
                    if (sceneType == 2) {
                        what = 5;
                    } else if (sceneType == 4) {
                        what = 4;
                    } else if (sceneType == 8) {
                        what = 7;
                    } else if (sceneType == 16) {
                        what = 12;
                    } else if (sceneType == 32) {
                        what = AwareNativeFreezeManager.MSG_SKIPPED_FRAME;
                    }
                } else if (sceneType == 2) {
                    what = 6;
                } else if (sceneType == 16) {
                    what = 11;
                }
                if (what != -1) {
                    AwareNativeFreezeManager.this.mAwareNativeHandler.sendEmptyMessage(what);
                }
            }
        }
    }

    public void reportScreenEvent(boolean isScreenOn) {
        int what = 9;
        if (!isScreenOn) {
            what = 8;
        }
        reportEvent(what);
    }

    public void reportEvent(int what) {
        if (this.mIsEnable.get()) {
            AwareLog.d(TAG, "reportEvent what = " + what);
            this.mAwareNativeHandler.sendEmptyMessage(what);
        }
    }

    private boolean sendFrozenMsg(boolean frozen) {
        int size = this.mAppEventMap.size();
        boolean ret = false;
        for (int i = 0; i < size; i++) {
            BaseInfo info = this.mAppEventMap.valueAt(i);
            if (info != null && (!frozen || !info.onActivityChanged())) {
                ret |= sendFrozenMsg(frozen, info);
            }
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canFrozen() {
        if (expireMaxFrozenTime() || !this.mIsScreenOn.get()) {
            return false;
        }
        return true;
    }

    private boolean expireMaxFrozenTime() {
        int diff = (int) (SystemClock.uptimeMillis() - this.mLastFrozenTimestamp);
        int i = this.mLastFrozenDuration;
        if (diff <= i || diff >= i + this.mUnfrozenDurtion) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSkippedFrameFreeze(int type) {
        if (this.mIsSilde.get()) {
            doFrozenForScene(type, this.mSlideFrozenDuration);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doFrozenForScene(int sceneType, int duration) {
        updateLastSceneInfo(sceneType);
        if (this.mAppEventMap.size() != 0 && canFrozen() && !this.mIsFreeze.get() && sendFrozenMsg(true)) {
            saveFrozenInfo(duration);
        }
    }

    private void saveFrozenInfo(int duration) {
        this.mIsFreeze.set(true);
        this.mLastFrozenTimestamp = SystemClock.uptimeMillis();
        this.mLastFrozenDuration = duration;
        this.mAwareNativeHandler.removeMessages(10);
        this.mAwareNativeHandler.sendEmptyMessageDelayed(10, (long) duration);
    }

    private boolean isUserActivity(int duration) {
        return SystemClock.uptimeMillis() - this.mLastSceneTimestamp < ((long) duration);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUnfrozenForScene() {
        sendFrozenMsg(false);
        this.mIsFreeze.set(false);
        this.mAwareNativeHandler.removeMessages(10);
        updateLastSceneInfo(0);
    }

    private void updateLastSceneInfo(int sceneType) {
        this.mLastSceneEvent = sceneType;
        this.mLastSceneTimestamp = SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doFrozenForInstall(BaseInfo info) {
        if (info != null) {
            AwareLog.d(TAG, "doFrozenForInstall mLastSceneEvent = " + this.mLastSceneEvent);
            if (this.mLastSceneEvent != MSG_SKIPPED_FRAME || !this.mIsSilde.get() || !isUserActivity(this.mSlideFrozenDuration)) {
                int i = this.mLastSceneEvent;
                if (i == 0 || i == MSG_SKIPPED_FRAME || !isUserActivity(this.mShortFrozenDurtion)) {
                    AwareLog.d(TAG, "doFrozenForInstall not handle");
                    return;
                }
                sendFrozenMsg(true, info);
                saveFrozenInfo(this.mShortFrozenDurtion);
                return;
            }
            sendFrozenMsg(true, info);
            saveFrozenInfo(this.mSlideFrozenDuration);
        }
    }

    /* access modifiers changed from: private */
    public class BaseInfo {
        public boolean isFrozen = false;
        public int pid;
        public int ppid;

        public BaseInfo(int pid2, int ppid2) {
            this.pid = pid2;
            this.ppid = ppid2;
        }

        public void onEventProcess(int eventId) {
        }

        public boolean onActivityChanged() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean isValidInfo() {
            if (this.pid == 0) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getUidByPkgname(String pkgName) {
        ApplicationInfo appInfo = InnerUtils.getApplicationInfo(pkgName);
        if (appInfo != null) {
            return appInfo.uid;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public class InstallerInfo extends BaseInfo {
        private static final int DEADLINE_RUNTIME = 120000;
        public String installerPkgName;
        public int installerUid;
        public String pkgName;
        public long timestamp = SystemClock.uptimeMillis();

        public InstallerInfo(String installerPkg, int installerUid2, String pkgName2) {
            super(0, 0);
            this.installerPkgName = installerPkg;
            this.installerUid = installerUid2;
            this.pkgName = pkgName2;
        }

        @Override // com.android.server.mtm.iaware.appmng.appfreeze.AwareNativeFreezeManager.BaseInfo
        public void onEventProcess(int eventId) {
            if (!isValidInfo()) {
                AwareLog.e(AwareNativeFreezeManager.TAG, "onEventProcess invalid params!");
            } else if (eventId == 2) {
                if (this.installerUid <= 0) {
                    this.installerUid = AwareNativeFreezeManager.this.getUidByPkgname(this.installerPkgName);
                }
                AwareLog.d(AwareNativeFreezeManager.TAG, "onEventProcess eventId = " + eventId + " installerUid = " + this.installerUid);
                if (this.installerUid >= 10000 && !onActivityChanged() && AwareNativeFreezeManager.this.canFrozen()) {
                    AwareNativeFreezeManager.this.doFrozenForInstall(this);
                }
            }
        }

        @Override // com.android.server.mtm.iaware.appmng.appfreeze.AwareNativeFreezeManager.BaseInfo
        public boolean onActivityChanged() {
            if (isDeadLineRuntime()) {
                return true;
            }
            for (String name : AwareNativeFreezeManager.this.mFgPackageSet) {
                if (AwareNativeFreezeManager.this.mInstallerSet.contains(name)) {
                    return true;
                }
            }
            return AwareNativeFreezeManager.this.mFgPackageSet.contains(this.installerPkgName);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.mtm.iaware.appmng.appfreeze.AwareNativeFreezeManager.BaseInfo
        public boolean isValidInfo() {
            if (!super.isValidInfo() || this.installerPkgName == null || this.pkgName == null) {
                return false;
            }
            return true;
        }

        private boolean isDeadLineRuntime() {
            return SystemClock.uptimeMillis() - this.timestamp > 120000;
        }
    }

    private AwareConfig getAwareCustConfig(String feature, String config) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getCustConfig(awareService, feature, config);
            }
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            return null;
        }
    }

    private void setInstallerList(AwareConfig config) {
        List<AwareConfig.SubItem> subItemList;
        String itemValue;
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return;
        }
        List<AwareConfig.Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (subItemList = item.getSubItemList()) == null)) {
                    for (AwareConfig.SubItem subItem : subItemList) {
                        if (!(subItem == null || (itemValue = subItem.getValue()) == null)) {
                            this.mInstallerSet.add(itemValue);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initInstallerList() {
        int i = this.mRetries + 1;
        this.mRetries = i;
        if (i > 6) {
            AwareLog.w(TAG, "initInstallerList mRetries = " + this.mRetries + " reach max retry!");
            return;
        }
        AwareConfig config = getAwareCustConfig(FEATURE_NAME, ITEM_CONFIG_NAME);
        if (config == null) {
            this.mAwareNativeHandler.sendEmptyMessageDelayed(3, 2000);
            AwareLog.w(TAG, "initInstallerList getService failed retry = " + this.mRetries);
            return;
        }
        setInstallerList(config);
        setInstallerDuration();
    }

    private boolean isValidRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private void setPropValue(String itemProp, String itemValue) {
        if (itemProp != null && itemValue != null) {
            int value = 0;
            try {
                value = Integer.parseInt(itemValue);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "setPropValue itemValue = " + itemValue + " is not Integer!");
            }
            boolean isSetOk = false;
            if (PROP_SHORT_FRZ_DUR.equals(itemProp)) {
                if (isValidRange(value, 1000, 10000)) {
                    this.mShortFrozenDurtion = value;
                    isSetOk = true;
                }
            } else if (PROP_UNFRZ_DUR.equals(itemProp)) {
                if (isValidRange(value, 1000, RETRY_DURATION)) {
                    this.mUnfrozenDurtion = value;
                    isSetOk = true;
                }
            } else if (!PROP_SLIDE_DURATION.equals(itemProp)) {
                AwareLog.d(TAG, "unkown itemProp " + itemProp);
            } else if (isValidRange(value, 1000, 10000)) {
                this.mSlideFrozenDuration = value;
                isSetOk = true;
            }
            if (!isSetOk) {
                AwareLog.w(TAG, "setPropValue itemProp = " + itemProp + " itemValue = " + itemValue + " invalid, default!");
            }
        }
    }

    private void setInstallerDuration() {
        List<AwareConfig.SubItem> subItemList;
        AwareConfig config = getAwareCustConfig(FEATURE_NAME, ITEM_CONFIG_DURATION);
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return;
        }
        List<AwareConfig.Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (subItemList = item.getSubItemList()) == null)) {
                    for (AwareConfig.SubItem subItem : subItemList) {
                        if (subItem != null) {
                            setPropValue(subItem.getName(), subItem.getValue());
                        }
                    }
                }
            }
        }
    }
}
