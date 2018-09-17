package com.android.server.mtm.iaware.appmng.appfreeze;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareNativeFreezeManager {
    public static final int EVENT_DEX2OAT_INSTALL = 1;
    private static final String FEATURE_NAME = "appmng_feature";
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
    private static final String PROP_LONG_FRZ_DUR = "long_frz_dur";
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
    private AtomicBoolean isFreeze = new AtomicBoolean(false);
    private AtomicBoolean isSilde = new AtomicBoolean(false);
    private SparseArray<BaseInfo> mAppEventMap = new SparseArray();
    private AwareNativeHandler mAwareNativeHandler = new AwareNativeHandler(this, null);
    private AwareSceneRecognizeCallback mAwareSceneRecognizeCallback = new AwareSceneRecognizeCallback(this, null);
    private Set<String> mFgPackageSet = new ArraySet();
    private Set<String> mInstallerSet = new ArraySet();
    private AtomicBoolean mIsEnable = new AtomicBoolean(false);
    private AtomicBoolean mIsScrrenOn = new AtomicBoolean(false);
    private int mLastFrozenDuration = 0;
    private long mLastFrozenTimestamp = 0;
    private int mLastSceneEvent = 0;
    private long mLastSceneTimestamp = 0;
    private int mRetries = 0;
    private int mShortFrozenDurtion = SHORT_FROZEN_DURATION;
    private int mSlideFrozenDuration = SLIDE_FROZEN_DURATION;
    private int mUnfrozenDurtion = 1000;

    private class AwareNativeHandler extends Handler {
        /* synthetic */ AwareNativeHandler(AwareNativeFreezeManager this$0, AwareNativeHandler -this1) {
            this();
        }

        private AwareNativeHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    AwareNativeFreezeManager.this.processNativeEvent(msg.arg1, msg.obj);
                    return;
                case 2:
                    AwareNativeFreezeManager.this.processActivitiesChanged(msg.getData());
                    return;
                case 3:
                    AwareNativeFreezeManager.this.initInstallerList();
                    return;
                case 4:
                    AwareNativeFreezeManager.this.doFrozenForScene(4, AwareNativeFreezeManager.this.mShortFrozenDurtion);
                    return;
                case 5:
                    AwareNativeFreezeManager.this.mLastSceneEvent = 0;
                    AwareNativeFreezeManager.this.isSilde.set(true);
                    return;
                case 6:
                    AwareNativeFreezeManager.this.isSilde.set(false);
                    return;
                case 7:
                    AwareNativeFreezeManager.this.doFrozenForScene(7, AwareNativeFreezeManager.this.mShortFrozenDurtion);
                    return;
                case 8:
                    AwareNativeFreezeManager.this.mIsScrrenOn.set(false);
                    AwareNativeFreezeManager.this.doUnfrozenForScene();
                    return;
                case 9:
                    AwareNativeFreezeManager.this.mIsScrrenOn.set(true);
                    return;
                case 10:
                    AwareNativeFreezeManager.this.doUnfrozenForScene();
                    return;
                case 11:
                    AwareNativeFreezeManager.this.doFrozenForScene(11, AwareNativeFreezeManager.this.mShortFrozenDurtion);
                    return;
                case 12:
                    AwareNativeFreezeManager.this.doFrozenForScene(12, AwareNativeFreezeManager.this.mShortFrozenDurtion);
                    return;
                case 13:
                    AwareNativeFreezeManager.this.handleSkippedFrameFreeze(13);
                    return;
                default:
                    return;
            }
        }
    }

    private class AwareSceneRecognizeCallback implements IAwareSceneRecCallback {
        /* synthetic */ AwareSceneRecognizeCallback(AwareNativeFreezeManager this$0, AwareSceneRecognizeCallback -this1) {
            this();
        }

        private AwareSceneRecognizeCallback() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            if (AwareNativeFreezeManager.this.mIsEnable.get()) {
                AwareLog.d(AwareNativeFreezeManager.TAG, "onStateChanged sceneType = " + sceneType + " eventType = " + eventType + " pkgName = " + pkgName);
                int what = -1;
                if (eventType != 1) {
                    switch (sceneType) {
                        case 2:
                            what = 6;
                            break;
                        case 16:
                            what = 11;
                            break;
                    }
                }
                switch (sceneType) {
                    case 2:
                        what = 5;
                        break;
                    case 4:
                        what = 4;
                        break;
                    case 8:
                        what = 7;
                        break;
                    case 16:
                        what = 12;
                        break;
                    case 32:
                        what = 13;
                        break;
                }
                if (what != -1) {
                    AwareNativeFreezeManager.this.mAwareNativeHandler.sendEmptyMessage(what);
                }
            }
        }
    }

    private class BaseInfo {
        public boolean isFrozen = false;
        public int pid;
        public int ppid;

        public BaseInfo(int pid, int ppid) {
            this.pid = pid;
            this.ppid = ppid;
        }

        public void onEventProcess(int eventId) {
        }

        public boolean onActivityChanged() {
            return false;
        }

        protected boolean isValidInfo() {
            if (this.pid == 0) {
                return false;
            }
            return true;
        }
    }

    private class InstallerInfo extends BaseInfo {
        private static final int DEADLINE_RUNTIME = 120000;
        public String installerPkgName;
        public int installerUid;
        public String pkgName;
        public long timestamp = SystemClock.uptimeMillis();

        public InstallerInfo(String installerPkg, int installerUid, String pkgName) {
            super(0, 0);
            this.installerPkgName = installerPkg;
            this.installerUid = installerUid;
            this.pkgName = pkgName;
        }

        public void onEventProcess(int eventId) {
            if (isValidInfo()) {
                if (eventId == 2) {
                    if (this.installerUid <= 0) {
                        this.installerUid = AwareNativeFreezeManager.this.getUidByPkgname(this.installerPkgName);
                    }
                    AwareLog.d(AwareNativeFreezeManager.TAG, "onEventProcess eventId = " + eventId + " installerUid = " + this.installerUid);
                    if (this.installerUid >= 10000 && !onActivityChanged() && (AwareNativeFreezeManager.this.canFrozen() ^ 1) == 0) {
                        AwareNativeFreezeManager.this.doFrozenForInstall(this);
                    } else {
                        return;
                    }
                }
                return;
            }
            AwareLog.e(AwareNativeFreezeManager.TAG, "onEventProcess invalid params!");
        }

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

        protected boolean isValidInfo() {
            if (!super.isValidInfo() || this.installerPkgName == null || this.pkgName == null) {
                return false;
            }
            return true;
        }

        private boolean isDeadLineRuntime() {
            if (SystemClock.uptimeMillis() - this.timestamp > 120000) {
                return true;
            }
            return false;
        }
    }

    public void start(Context context) {
        if ("0".equals(SystemProperties.get("persist.sys.app.installer", "1"))) {
            this.mIsEnable.set(false);
            return;
        }
        if (context != null) {
            PowerManager pm = (PowerManager) context.getSystemService("power");
            if (pm != null) {
                this.mIsScrrenOn.set(pm.isInteractive());
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
        sendFrozenMsg(frozen ? 0 : 1, pid, ppid);
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean sendFrozenMsg(boolean frozen, BaseInfo info) {
        if (info == null || (info.isValidInfo() ^ 1) != 0 || ((info.isFrozen ^ 1) ^ frozen) != 0) {
            return false;
        }
        info.isFrozen = frozen;
        sendFrozenMsg(frozen, info.pid, info.ppid);
        return true;
    }

    private void processNativeEvent(int eventType, Object obj) {
        if (obj != null && ((obj instanceof CollectData) ^ 1) == 0) {
            Bundle bundle = ((CollectData) obj).getBundle();
            if (bundle != null && eventType == 1) {
                processInstallerEvent(eventType, bundle);
            }
        }
    }

    private void processInstallerEvent(int eventType, Bundle bundle) {
        int eventId = bundle.getInt("eventId", 0);
        String pkgName;
        if (eventId == 0 || eventId == 1) {
            String installerPkgName = bundle.getString("installer_name", "");
            pkgName = bundle.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME, "");
            int installerUid = bundle.getInt("installer_uid", 0);
            if (eventId == 0) {
                this.mAppEventMap.put(eventType, new InstallerInfo(installerPkgName, installerUid, pkgName));
            } else {
                this.mAppEventMap.remove(eventType);
                this.isFreeze.set(false);
            }
            AwareLog.d(TAG, "installPackage eventId = " + eventId + " installerPkgName = " + installerPkgName + " pkgName = " + pkgName);
        } else if (eventId == 2) {
            BaseInfo baseInfo = (BaseInfo) this.mAppEventMap.get(eventType);
            int dexoptPid = bundle.getInt("dexopt_pid", 0);
            int ppid = bundle.getInt("ppid", 0);
            pkgName = bundle.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME, "");
            if (baseInfo != null && ((baseInfo instanceof InstallerInfo) ^ 1) == 0) {
                AwareLog.d(TAG, "installPackage eventId = " + eventId + " dexoptPid = " + dexoptPid + " pkgName = " + pkgName);
                InstallerInfo installerInfo = (InstallerInfo) baseInfo;
                if (pkgName != null && pkgName.equals(installerInfo.pkgName)) {
                    installerInfo.pid = dexoptPid;
                    installerInfo.ppid = ppid;
                    installerInfo.onEventProcess(eventId);
                }
            }
        }
    }

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
                BaseInfo info = (BaseInfo) this.mAppEventMap.valueAt(i);
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
            BaseInfo info = (BaseInfo) this.mAppEventMap.valueAt(i);
            if (!(info == null || (frozen && (info.onActivityChanged() ^ 1) == 0))) {
                ret |= sendFrozenMsg(frozen, info);
            }
        }
        return ret;
    }

    private boolean canFrozen() {
        if (expireMaxFrozenTime() || (this.mIsScrrenOn.get() ^ 1) != 0) {
            return false;
        }
        return true;
    }

    private boolean expireMaxFrozenTime() {
        int diff = (int) (SystemClock.uptimeMillis() - this.mLastFrozenTimestamp);
        if (diff <= this.mLastFrozenDuration || diff >= this.mLastFrozenDuration + this.mUnfrozenDurtion) {
            return false;
        }
        return true;
    }

    private void handleSkippedFrameFreeze(int type) {
        if (this.isSilde.get()) {
            doFrozenForScene(type, this.mSlideFrozenDuration);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x001b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void doFrozenForScene(int sceneType, int duration) {
        updateLastSceneInfo(sceneType);
        if (this.mAppEventMap.size() != 0 && (canFrozen() ^ 1) == 0 && !this.isFreeze.get() && sendFrozenMsg(true)) {
            saveFrozenInfo(duration);
        }
    }

    private void saveFrozenInfo(int duration) {
        this.isFreeze.set(true);
        this.mLastFrozenTimestamp = SystemClock.uptimeMillis();
        this.mLastFrozenDuration = duration;
        this.mAwareNativeHandler.removeMessages(10);
        this.mAwareNativeHandler.sendEmptyMessageDelayed(10, (long) duration);
    }

    private boolean isUserActivity(int duration) {
        if (SystemClock.uptimeMillis() - this.mLastSceneTimestamp < ((long) duration)) {
            return true;
        }
        return false;
    }

    private void doUnfrozenForScene() {
        sendFrozenMsg(false);
        this.isFreeze.set(false);
        this.mAwareNativeHandler.removeMessages(10);
        updateLastSceneInfo(0);
    }

    private void updateLastSceneInfo(int sceneType) {
        this.mLastSceneEvent = sceneType;
        this.mLastSceneTimestamp = SystemClock.uptimeMillis();
    }

    private void doFrozenForInstall(BaseInfo info) {
        if (info != null) {
            AwareLog.d(TAG, "doFrozenForInstall mLastSceneEvent = " + this.mLastSceneEvent);
            if (this.mLastSceneEvent == 13 && this.isSilde.get() && isUserActivity(this.mSlideFrozenDuration)) {
                sendFrozenMsg(true, info);
                saveFrozenInfo(this.mSlideFrozenDuration);
            } else if (!(this.mLastSceneEvent == 0 || this.mLastSceneEvent == 13 || !isUserActivity(this.mShortFrozenDurtion))) {
                sendFrozenMsg(true, info);
                saveFrozenInfo(this.mShortFrozenDurtion);
            }
        }
    }

    private int getUidByPkgname(String pkgName) {
        ApplicationInfo appInfo = InnerUtils.getApplicationInfo(pkgName);
        if (appInfo != null) {
            return appInfo.uid;
        }
        return -1;
    }

    private AwareConfig getAwareCustConfig(String feature, String config) {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                return awareservice.getCustConfig(feature, config);
            }
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            return null;
        }
    }

    private void setInstallerList(AwareConfig config) {
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return;
        }
        List<Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (Item item : itemList) {
                List<SubItem> subItemList = item.getSubItemList();
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        String itemValue = subItem.getValue();
                        if (itemValue != null) {
                            this.mInstallerSet.add(itemValue);
                        }
                    }
                }
            }
        }
    }

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
        if (value < min || value > max) {
            return false;
        }
        return true;
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
                if (isValidRange(value, 1000, 2000)) {
                    this.mUnfrozenDurtion = value;
                    isSetOk = true;
                }
            } else if (PROP_SLIDE_DURATION.equals(itemProp) && isValidRange(value, 1000, 10000)) {
                this.mSlideFrozenDuration = value;
                isSetOk = true;
            }
            if (!isSetOk) {
                AwareLog.w(TAG, "setPropValue itemProp = " + itemProp + " itemValue = " + itemValue + " invalid, default!");
            }
        }
    }

    private void setInstallerDuration() {
        AwareConfig config = getAwareCustConfig(FEATURE_NAME, ITEM_CONFIG_DURATION);
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return;
        }
        List<Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (Item item : itemList) {
                List<SubItem> subItemList = item.getSubItemList();
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        setPropValue(subItem.getName(), subItem.getValue());
                    }
                }
            }
        }
    }
}
