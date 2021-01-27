package com.android.server.rms.iaware.appmng;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareSceneRecognize {
    private static final String DEFAULT_CONFIG = "AppFreeze";
    private static final String DEFAULT_FEATURE = "AppMng";
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final String KEY_PACKAGENAME = "packageName";
    private static final String KEY_UID = "uid";
    private static final Object LOCK = new Object();
    private static final int MSG_CAMERA_SHOT = 105;
    private static final int MSG_GALLERY_SCALE = 108;
    private static final int MSG_PROXIMITY_SENSOR = 106;
    private static final int MSG_SKIPPED_FRAME = 107;
    private static final int MSG_STOP_ACTIVITY = 104;
    private static final int MSG_STOP_FLING = 102;
    private static final int MSG_STOP_SCROLL = 101;
    public static final int SCENE_RECONGNIZE_CAMERA_SHOT = 8;
    public static final int SCENE_RECONGNIZE_DEFAULT = 1;
    public static final int SCENE_RECONGNIZE_EVENT_BEGIN = 1;
    public static final int SCENE_RECONGNIZE_EVENT_END = 0;
    public static final int SCENE_RECONGNIZE_GALLERY_SCALE = 64;
    public static final int SCENE_RECONGNIZE_PROXIMITY_SENSOR = 16;
    public static final int SCENE_RECONGNIZE_SKIPPED_FRAME = 32;
    public static final int SCENE_RECONGNIZE_SLIPPING = 2;
    public static final int SCENE_RECONGNIZE_START_ACTIVITY = 4;
    private static final long START_TIME_OUT = 3000;
    private static final long STOP_SCROLL_DELAY = 100;
    private static final String TAG = "RMS.AwareSceneRecognize";
    private static AwareSceneRecognize sAwareSceneRecognize = null;
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private final ArrayMap<IAwareSceneRecCallback, Integer> mCallbacks = new ArrayMap<>();
    private Handler mHandler = new SceneRecHandler();
    private AtomicBoolean mIsActivityStarting = new AtomicBoolean(false);
    private AtomicBoolean mIsFling = new AtomicBoolean(false);
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsScroll = new AtomicBoolean(false);
    private String mLastStartPkg = "";
    private Object mLock = new Object();
    private Map<Integer, String> mValidApp = new ArrayMap();

    public interface IAwareSceneRecCallback {
        void onStateChanged(int i, int i2, String str);
    }

    private AwareSceneRecognize() {
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            loadConfig();
            this.mIsInitialized.set(true);
        }
    }

    private void loadConfig() {
        AwareConfig awareConfig = getConfig();
        if (awareConfig == null) {
            AwareLog.e(TAG, "config is null");
            return;
        }
        List<AwareConfig.Item> itemList = awareConfig.getConfigList();
        if (itemList == null) {
            AwareLog.e(TAG, "config has no items");
            return;
        }
        AwareConfig.Item item = null;
        if (itemList.size() > 0) {
            item = itemList.get(0);
        }
        if (item == null) {
            AwareLog.e(TAG, "config has no item");
            return;
        }
        List<AwareConfig.SubItem> subItems = item.getSubItemList();
        if (subItems == null || subItems.isEmpty()) {
            AwareLog.e(TAG, "config has no sub item");
            return;
        }
        AwareConfig.SubItem subitem = subItems.get(0);
        if (subitem != null && "packageName".equals(subitem.getName())) {
            String pkg = subitem.getValue();
            if (pkg == null || "".equals(pkg.trim())) {
                AwareLog.e(TAG, "get the valid pkg failed from config");
            } else {
                initValidAppInfo(pkg.trim());
            }
        }
    }

    private void initValidAppInfo(String pkgInfo) {
        int uid;
        MultiTaskManagerService mtmService = MultiTaskManagerService.self();
        if (mtmService == null) {
            AwareLog.e(TAG, "get mtm services failed");
            return;
        }
        PackageManager pm = mtmService.context().getPackageManager();
        if (pm != null) {
            if (pkgInfo.contains(",")) {
                for (String pkg : pkgInfo.split(",")) {
                    String pkg2 = pkg.trim();
                    if (!"".equals(pkg2) && (uid = getUidByPackageName(pkg2, pm)) != -1) {
                        this.mValidApp.put(Integer.valueOf(uid), pkg2);
                    }
                }
                return;
            }
            int uid2 = getUidByPackageName(pkgInfo, pm);
            if (uid2 != -1) {
                this.mValidApp.put(Integer.valueOf(uid2), pkgInfo);
            }
        }
    }

    private int getUidByPackageName(String pkg, PackageManager pm) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 1);
            if (appInfo == null) {
                return -1;
            }
            AwareLog.i(TAG, "curCameraUID=" + appInfo.uid + ", mPackageName=" + pkg);
            return appInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.e(TAG, "get the camera uid faied");
            return -1;
        }
    }

    private AwareConfig getConfig() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                AwareConfig configList = IAwareCMSManager.getConfig(awareService, DEFAULT_FEATURE, DEFAULT_CONFIG);
                AwareLog.d(TAG, "configList:" + configList);
                return configList;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
            return null;
        }
    }

    private void deInitialize() {
        synchronized (this.mLock) {
            if (this.mIsInitialized.get()) {
                synchronized (this.mCallbacks) {
                    this.mCallbacks.clear();
                }
                this.mIsInitialized.set(false);
            }
        }
    }

    public static AwareSceneRecognize getInstance() {
        AwareSceneRecognize awareSceneRecognize;
        synchronized (LOCK) {
            if (sAwareSceneRecognize == null) {
                sAwareSceneRecognize = new AwareSceneRecognize();
            }
            awareSceneRecognize = sAwareSceneRecognize;
        }
        return awareSceneRecognize;
    }

    public void registerStateCallback(IAwareSceneRecCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                Integer states = this.mCallbacks.get(callback);
                if (states == null) {
                    this.mCallbacks.put(callback, Integer.valueOf(stateType));
                } else {
                    this.mCallbacks.put(callback, Integer.valueOf(states.intValue() | stateType));
                }
            }
        }
    }

    public void unregisterStateCallback(IAwareSceneRecCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStateChange(int sceneType, int eventType, String pkgName) {
        if (sDebug) {
            AwareLog.d(TAG, "SceneRec sceneType :" + sceneType + ", eventType:" + eventType + ", pkg=" + pkgName);
        }
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.isEmpty()) {
                for (Map.Entry<IAwareSceneRecCallback, Integer> entry : this.mCallbacks.entrySet()) {
                    IAwareSceneRecCallback callback = entry.getKey();
                    int state = entry.getValue().intValue();
                    if (state == 1 || (state & sceneType) != 0) {
                        callback.onStateChanged(sceneType, eventType, pkgName);
                    }
                }
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (sEnabled) {
            if (sDebug) {
                AwareLog.d(TAG, "eventId: " + eventId);
            }
            if (bundleArgs != null) {
                if (!this.mIsInitialized.get()) {
                    initialize();
                }
                if (eventId == 23) {
                    handleProximityEvent(bundleArgs);
                } else if (eventId == 15014) {
                    handleGalleryEvent(bundleArgs);
                } else if (eventId == 15011) {
                    Handler handler = this.mHandler;
                    if (handler != null) {
                        Message msg = handler.obtainMessage();
                        msg.what = 105;
                        msg.setData(bundleArgs);
                        this.mHandler.sendMessage(msg);
                    }
                } else if (eventId != 15012) {
                    switch (eventId) {
                        case 13:
                            handleStartScroll();
                            return;
                        case 14:
                            handleStopScroll();
                            return;
                        case 15:
                            handleFling(bundleArgs.getInt("scroll_duration"));
                            return;
                        default:
                            AwareLog.e(TAG, "Unknown EventID: " + eventId);
                            return;
                    }
                } else {
                    handleSkippedFrameEvent(bundleArgs);
                }
            }
        } else if (sDebug) {
            AwareLog.d(TAG, "AwareSceneRecognize feature disabled!");
        }
    }

    public void reportActivityStart(CollectData data) {
        if (sEnabled) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (data != null) {
                String eventData = data.getData();
                AttrSegments.Builder builder = new AttrSegments.Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (!attrSegments.isValid()) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return;
                }
                ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
                if (appInfo == null) {
                    AwareLog.d(TAG, "appInfo is NULL");
                    return;
                }
                int eventId = attrSegments.getEvent().intValue();
                if (eventId == 15005 || eventId == 85005) {
                    Message msg = this.mHandler.obtainMessage();
                    msg.what = eventId;
                    Bundle bundle = new Bundle();
                    bundle.putString("packageName", appInfo.get("packageName"));
                    msg.setData(bundle);
                    this.mHandler.sendMessage(msg);
                }
            }
        } else if (sDebug) {
            AwareLog.d(TAG, "AwareSceneRecognize feature disabled!");
        }
    }

    private void sendMessage(int what, long delay) {
        this.mHandler.sendEmptyMessageDelayed(what, delay);
    }

    private void handleStartScroll() {
        if (!this.mIsScroll.get()) {
            this.mIsScroll.set(true);
            if (!this.mIsFling.get()) {
                notifyStateChange(2, 1, null);
            }
            this.mIsFling.set(false);
            this.mHandler.removeMessages(102);
        }
    }

    private void handleStopScroll() {
        sendMessage(101, STOP_SCROLL_DELAY);
    }

    private void handleFling(int duration) {
        this.mIsFling.set(true);
        if (this.mIsScroll.get()) {
            this.mIsScroll.set(false);
        }
        sendMessage(102, (long) duration);
    }

    private void handleProximityEvent(Bundle bundleArgs) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = 106;
            msg.setData(bundleArgs);
            this.mHandler.sendMessage(msg);
        }
    }

    private void handleSkippedFrameEvent(Bundle bundleArgs) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_SKIPPED_FRAME;
            msg.setData(bundleArgs);
            this.mHandler.sendMessage(msg);
        }
    }

    private void handleGalleryEvent(Bundle bundleArgs) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_GALLERY_SCALE;
            msg.setData(bundleArgs);
            this.mHandler.sendMessage(msg);
        }
    }

    private void handleStartActivity(String pkgName) {
        if (pkgName != null && !"".equals(pkgName.trim())) {
            AwareAppAssociate assc = AwareAppAssociate.getInstance();
            if (assc == null) {
                AwareLog.d(TAG, "the aware assoc is not started");
                return;
            }
            List<String> homePkg = assc.getDefaultHomePackages();
            if (!this.mLastStartPkg.equals(pkgName) || !homePkg.contains(pkgName)) {
                this.mIsActivityStarting.set(true);
                notifyStateChange(4, 1, pkgName);
                if (this.mHandler.hasMessages(104)) {
                    this.mHandler.removeMessages(104);
                }
                this.mHandler.sendEmptyMessageDelayed(104, START_TIME_OUT);
                this.mLastStartPkg = pkgName;
            } else if (sDebug) {
                AwareLog.d(TAG, "current is home, no need restart home process, so filter it");
            }
        } else if (sDebug) {
            AwareLog.d(TAG, "current start pkgName is null or empty");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityStartingFinish(String pkg) {
        if (this.mIsActivityStarting.get()) {
            notifyStateChange(4, 0, pkg);
            this.mIsActivityStarting.set(false);
        }
        if (this.mHandler.hasMessages(104)) {
            this.mHandler.removeMessages(104);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hanldeCameraOperation(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int uid = bundle.getInt("uid", -1) % 100000;
            if (uid == -1 || !this.mValidApp.containsKey(Integer.valueOf(uid))) {
                AwareLog.i(TAG, "the data is not report by camera, dataUID=" + uid);
                return;
            }
            notifyStateChange(8, 1, this.mValidApp.get(Integer.valueOf(uid)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGalleryOperation(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int uid = bundle.getInt("uid", -1) % 100000;
            if (uid == -1 || !this.mValidApp.containsKey(Integer.valueOf(uid))) {
                AwareLog.i(TAG, "the data is not report by gallery, dataUID=" + uid + ", pkg name: " + this.mValidApp.get(Integer.valueOf(uid)));
                return;
            }
            notifyStateChange(64, 1, this.mValidApp.get(Integer.valueOf(uid)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProximitySensor(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            notifyStateChange(16, bundle.getInt("positive"), null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSkippedFrame(Message msg) {
        if (msg.getData() != null) {
            notifyStateChange(32, 1, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityBegin(Message msg) {
        Bundle data = msg.getData();
        if (data != null) {
            handleStartActivity(data.getString("packageName"));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityFinish(Message msg) {
        Bundle data = msg.getData();
        if (data != null) {
            handleActivityStartingFinish(data.getString("packageName"));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopFling() {
        this.mIsFling.set(false);
        if (!this.mIsScroll.get()) {
            notifyStateChange(2, 0, null);
        }
    }

    private class SceneRecHandler extends Handler {
        public SceneRecHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (AwareSceneRecognize.sDebug) {
                AwareLog.d(AwareSceneRecognize.TAG, "handleMessage message " + msg.what + ", isScroll=" + AwareSceneRecognize.this.mIsScroll.get() + ", isFling=" + AwareSceneRecognize.this.mIsFling.get());
            }
            int i = msg.what;
            if (i != 101) {
                if (i == 102) {
                    AwareSceneRecognize.this.handleStopFling();
                } else if (i == 15005) {
                    AwareSceneRecognize.this.handleActivityBegin(msg);
                } else if (i != 85005) {
                    switch (i) {
                        case 104:
                            AwareSceneRecognize awareSceneRecognize = AwareSceneRecognize.this;
                            awareSceneRecognize.handleActivityStartingFinish(awareSceneRecognize.mLastStartPkg);
                            return;
                        case 105:
                            AwareSceneRecognize.this.hanldeCameraOperation(msg);
                            return;
                        case 106:
                            AwareSceneRecognize.this.handleProximitySensor(msg);
                            return;
                        case AwareSceneRecognize.MSG_SKIPPED_FRAME /* 107 */:
                            AwareSceneRecognize.this.handleSkippedFrame(msg);
                            return;
                        case AwareSceneRecognize.MSG_GALLERY_SCALE /* 108 */:
                            AwareSceneRecognize.this.handleGalleryOperation(msg);
                            return;
                        default:
                            return;
                    }
                } else {
                    AwareSceneRecognize.this.handleActivityFinish(msg);
                }
            } else if (!AwareSceneRecognize.this.mIsFling.get() && AwareSceneRecognize.this.mIsScroll.get()) {
                AwareSceneRecognize.this.mIsScroll.set(false);
                AwareSceneRecognize.this.notifyStateChange(2, 0, null);
            }
        }
    }

    public static boolean isEnable() {
        return sEnabled;
    }

    public static void enable() {
        sEnabled = true;
        AwareSceneRecognize awareSceneRecognize = sAwareSceneRecognize;
        if (awareSceneRecognize != null) {
            awareSceneRecognize.initialize();
        }
    }

    public static void disable() {
        sEnabled = false;
        AwareSceneRecognize awareSceneRecognize = sAwareSceneRecognize;
        if (awareSceneRecognize != null) {
            awareSceneRecognize.deInitialize();
        }
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }
}
