package com.android.server.rms.iaware.appmng;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.util.ArrayMap;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.data.content.AttrSegments.Builder;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareSceneRecognize {
    private static boolean DEBUG = true;
    private static final String DEFAULT_CONFIG = "AppFreeze";
    private static final String DEFAULT_FEATURE = "AppMng";
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final String KEY_PACKAGENAME = "packageName";
    private static final String KEY_UID = "uid";
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
    private static AwareSceneRecognize mAwareSceneRecognize = null;
    private static boolean mEnabled = false;
    private final ArrayMap<IAwareSceneRecCallback, Integer> mCallbacks = new ArrayMap();
    private Handler mHandler = new SceneRecHandler();
    private AtomicBoolean mIsActivityStarting = new AtomicBoolean(false);
    private AtomicBoolean mIsFling = new AtomicBoolean(false);
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsScroll = new AtomicBoolean(false);
    private String mLastStartPkg = "";
    private Map<Integer, String> mValidApp = new ArrayMap();

    public interface IAwareSceneRecCallback {
        void onStateChanged(int i, int i2, String str);
    }

    private class SceneRecHandler extends Handler {
        public void handleMessage(Message msg) {
            if (AwareSceneRecognize.DEBUG) {
                AwareLog.d(AwareSceneRecognize.TAG, "handleMessage message " + msg.what + ", isScroll=" + AwareSceneRecognize.this.mIsScroll.get() + ", isFling=" + AwareSceneRecognize.this.mIsFling.get());
            }
            switch (msg.what) {
                case 101:
                    if (!AwareSceneRecognize.this.mIsFling.get() && AwareSceneRecognize.this.mIsScroll.get()) {
                        AwareSceneRecognize.this.mIsScroll.set(false);
                        AwareSceneRecognize.this.notifyStateChange(2, 0, null);
                        return;
                    }
                    return;
                case 102:
                    AwareSceneRecognize.this.handleStopFling();
                    return;
                case 104:
                    AwareSceneRecognize.this.handleActivityStartingFinish(AwareSceneRecognize.this.mLastStartPkg);
                    return;
                case 105:
                    AwareSceneRecognize.this.hanldeCameraOperation(msg);
                    return;
                case 106:
                    AwareSceneRecognize.this.handleProximitySensor(msg);
                    return;
                case 107:
                    AwareSceneRecognize.this.handleSkippedFrame(msg);
                    return;
                case 108:
                    AwareSceneRecognize.this.handleGalleryOperation(msg);
                    return;
                case 15005:
                    AwareSceneRecognize.this.handleActivityBegin(msg);
                    return;
                case 85005:
                    AwareSceneRecognize.this.handleActivityFinish(msg);
                    return;
                default:
                    return;
            }
        }
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
        AwareConfig awareconfig = getConfig();
        if (awareconfig == null) {
            AwareLog.e(TAG, "config is null");
            return;
        }
        List<Item> itemList = awareconfig.getConfigList();
        if (itemList == null) {
            AwareLog.e(TAG, "config has no items");
            return;
        }
        Item item = null;
        if (itemList.size() > 0) {
            item = (Item) itemList.get(0);
        }
        if (item == null) {
            AwareLog.e(TAG, "config has no item");
            return;
        }
        List<SubItem> subItems = item.getSubItemList();
        if (subItems == null || subItems.isEmpty()) {
            AwareLog.e(TAG, "config has no sub item");
            return;
        }
        SubItem subitem = (SubItem) subItems.get(0);
        if (subitem != null && "packageName".equals(subitem.getName())) {
            String pkg = subitem.getValue();
            if (pkg == null || "".equals(pkg.trim())) {
                AwareLog.e(TAG, "get the valid pkg failed from config");
                return;
            }
            initValidAppInfo(pkg.trim());
        }
    }

    private void initValidAppInfo(String pkgInfo) {
        MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
        if (mMtmService == null) {
            AwareLog.e(TAG, "get mtm services failed");
            return;
        }
        PackageManager pm = mMtmService.context().getPackageManager();
        if (pm != null) {
            int uid;
            if (pkgInfo.contains(",")) {
                for (String pkg : pkgInfo.split(",")) {
                    String pkg2 = pkg2.trim();
                    if (!"".equals(pkg2)) {
                        uid = getUIDByPackageName(pkg2, pm);
                        if (uid != -1) {
                            this.mValidApp.put(Integer.valueOf(uid), pkg2);
                        }
                    }
                }
            } else {
                uid = getUIDByPackageName(pkgInfo, pm);
                if (uid != -1) {
                    this.mValidApp.put(Integer.valueOf(uid), pkgInfo);
                }
            }
        }
    }

    private int getUIDByPackageName(String pkg, PackageManager pm) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 1);
            if (appInfo == null) {
                return -1;
            }
            AwareLog.i(TAG, "curCameraUID=" + appInfo.uid + ", mPackageName=" + pkg);
            return appInfo.uid;
        } catch (NameNotFoundException e) {
            AwareLog.e(TAG, "get the camera uid faied");
            return -1;
        }
    }

    private AwareConfig getConfig() {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                AwareConfig configList = awareservice.getConfig(DEFAULT_FEATURE, DEFAULT_CONFIG);
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

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            synchronized (this.mCallbacks) {
                this.mCallbacks.clear();
            }
            this.mIsInitialized.set(false);
        }
    }

    public static synchronized AwareSceneRecognize getInstance() {
        AwareSceneRecognize awareSceneRecognize;
        synchronized (AwareSceneRecognize.class) {
            if (mAwareSceneRecognize == null) {
                mAwareSceneRecognize = new AwareSceneRecognize();
            }
            awareSceneRecognize = mAwareSceneRecognize;
        }
        return awareSceneRecognize;
    }

    public void registerStateCallback(IAwareSceneRecCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                Integer states = (Integer) this.mCallbacks.get(callback);
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

    private void notifyStateChange(int sceneType, int eventType, String pkgName) {
        if (DEBUG) {
            AwareLog.d(TAG, "SceneRec sceneType :" + sceneType + ", eventType:" + eventType + ", pkg=" + pkgName);
        }
        synchronized (this.mCallbacks) {
            if (this.mCallbacks.isEmpty()) {
                return;
            }
            for (Entry<IAwareSceneRecCallback, Integer> m : this.mCallbacks.entrySet()) {
                IAwareSceneRecCallback callback = (IAwareSceneRecCallback) m.getKey();
                int state = ((Integer) m.getValue()).intValue();
                if (1 == state || (state & sceneType) != 0) {
                    callback.onStateChanged(sceneType, eventType, pkgName);
                }
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "eventId: " + eventId);
            }
            if (bundleArgs != null) {
                if (!this.mIsInitialized.get()) {
                    initialize();
                }
                switch (eventId) {
                    case 13:
                        handleStartScroll();
                        break;
                    case 14:
                        handleStopScroll();
                        break;
                    case 15:
                        handleFling(bundleArgs.getInt("scroll_duration"));
                        break;
                    case 23:
                        handleProximityEvent(bundleArgs);
                        break;
                    case 15011:
                        Message msg = new Message();
                        msg.what = 105;
                        msg.setData(bundleArgs);
                        this.mHandler.sendMessage(msg);
                        break;
                    case 15012:
                        handleSkippedFrameEvent(bundleArgs);
                        break;
                    case 15014:
                        handleGalleryEvent(bundleArgs);
                        break;
                    default:
                        AwareLog.e(TAG, "Unknown EventID: " + eventId);
                        break;
                }
                return;
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "AwareSceneRecognize feature disabled!");
        }
    }

    public void reportActivityStart(CollectData data) {
        if (mEnabled) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (data != null) {
                String eventData = data.getData();
                Builder builder = new Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (attrSegments.isValid()) {
                    ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
                    if (appInfo == null) {
                        AwareLog.d(TAG, "appInfo is NULL");
                        return;
                    }
                    int eventId = attrSegments.getEvent().intValue();
                    if (15005 == eventId || 85005 == eventId) {
                        String pkgName = (String) appInfo.get("packageName");
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = eventId;
                        Bundle bundle = new Bundle();
                        bundle.putString("packageName", pkgName);
                        msg.setData(bundle);
                        this.mHandler.sendMessage(msg);
                    }
                    return;
                }
                AwareLog.e(TAG, "Invalid collectData, or event");
                return;
            }
            return;
        }
        if (DEBUG) {
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
        Message msg = new Message();
        msg.what = 106;
        msg.setData(bundleArgs);
        this.mHandler.sendMessage(msg);
    }

    private void handleSkippedFrameEvent(Bundle bundleArgs) {
        Message msg = new Message();
        msg.what = 107;
        msg.setData(bundleArgs);
        this.mHandler.sendMessage(msg);
    }

    private void handleGalleryEvent(Bundle bundleArgs) {
        Message msg = new Message();
        msg.what = 108;
        msg.setData(bundleArgs);
        this.mHandler.sendMessage(msg);
    }

    private void handleStartActivity(String pkgName) {
        if (pkgName == null || "".equals(pkgName.trim())) {
            if (DEBUG) {
                AwareLog.d(TAG, "current start pkgName is null or empty");
            }
            return;
        }
        AwareAppAssociate assc = AwareAppAssociate.getInstance();
        if (assc == null) {
            AwareLog.d(TAG, "the aware assoc is not started");
            return;
        }
        List<String> homePkg = assc.getDefaultHomePackages();
        if (this.mLastStartPkg.equals(pkgName) && homePkg.contains(pkgName)) {
            if (DEBUG) {
                AwareLog.d(TAG, "current is home, no need restart home process, so filter it");
            }
            return;
        }
        this.mIsActivityStarting.set(true);
        notifyStateChange(4, 1, pkgName);
        if (this.mHandler.hasMessages(104)) {
            this.mHandler.removeMessages(104);
        }
        this.mHandler.sendEmptyMessageDelayed(104, START_TIME_OUT);
        this.mLastStartPkg = pkgName;
    }

    private void handleActivityStartingFinish(String pkg) {
        if (this.mIsActivityStarting.get()) {
            notifyStateChange(4, 0, pkg);
            this.mIsActivityStarting.set(false);
        }
        if (this.mHandler.hasMessages(104)) {
            this.mHandler.removeMessages(104);
        }
    }

    private void hanldeCameraOperation(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int uid = bundle.getInt("uid", -1) % LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
            if (uid == -1 || (this.mValidApp.containsKey(Integer.valueOf(uid)) ^ 1) != 0) {
                AwareLog.i(TAG, "the data is not report by camera, dataUID=" + uid);
            } else {
                notifyStateChange(8, 1, (String) this.mValidApp.get(Integer.valueOf(uid)));
            }
        }
    }

    private void handleGalleryOperation(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int uid = bundle.getInt("uid", -1) % LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
            if (uid == -1 || (this.mValidApp.containsKey(Integer.valueOf(uid)) ^ 1) != 0) {
                AwareLog.i(TAG, "the data is not report by gallery, dataUID=" + uid + ", pkg name: " + ((String) this.mValidApp.get(Integer.valueOf(uid))));
            } else {
                notifyStateChange(64, 1, (String) this.mValidApp.get(Integer.valueOf(uid)));
            }
        }
    }

    private void handleProximitySensor(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            notifyStateChange(16, bundle.getInt("positive"), null);
        }
    }

    private void handleSkippedFrame(Message msg) {
        if (msg.getData() != null) {
            notifyStateChange(32, 1, null);
        }
    }

    private void handleActivityBegin(Message msg) {
        Bundle data = msg.getData();
        if (data != null) {
            handleStartActivity(data.getString("packageName"));
        }
    }

    private void handleActivityFinish(Message msg) {
        Bundle data = msg.getData();
        if (data != null) {
            handleActivityStartingFinish(data.getString("packageName"));
        }
    }

    private void handleStopFling() {
        this.mIsFling.set(false);
        if (!this.mIsScroll.get()) {
            notifyStateChange(2, 0, null);
        }
    }

    public static boolean isEnable() {
        return mEnabled;
    }

    public static void enable() {
        mEnabled = true;
        if (mAwareSceneRecognize != null) {
            mAwareSceneRecognize.initialize();
        }
    }

    public static void disable() {
        mEnabled = false;
        if (mAwareSceneRecognize != null) {
            mAwareSceneRecognize.deInitialize();
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }
}
