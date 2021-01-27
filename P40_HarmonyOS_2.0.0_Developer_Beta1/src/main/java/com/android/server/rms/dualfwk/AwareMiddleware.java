package com.android.server.rms.dualfwk;

import android.content.Context;
import android.graphics.Point;
import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.rms.dualfwk.IAwareObserver;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AwareMiddleware {
    private static final int CODE_GET_UNIPERF_CONFIG = 9;
    private static final int CODE_MIDDLEWARE_BASE = 1;
    private static final int CODE_REGISTER_OBSERVER = 1;
    private static final int CODE_REPORT_ADD_ZIDANE_APK = 4;
    private static final int CODE_REPORT_DTB_BIND = 6;
    private static final int CODE_REPORT_INIT_ZIDANE_APK = 3;
    private static final int CODE_REPORT_REMOVE_ZIDANE_APK = 5;
    private static final int CODE_UNIPERF_EVENT_LONG_TERM = 8;
    private static final int CODE_UNIPERF_EVENT_SHORT_TERM = 7;
    private static final int CODE_UNREGISTER_OBSERVER = 2;
    private static final String DEFAULT_DEVICE_NAME = "Huawei Device";
    private static final int EVENT_TYPE_ENTER = 1;
    private static final int EVENT_TYPE_EXIT = 0;
    private static final boolean HOSP_ENABLE = SystemProperties.getBoolean("hw_sc.build.os.enable", false);
    private static final String KEY_BUNDLE_EVENT_TYPE = "EVENT_TYPE";
    private static final String KEY_BUNDLE_PID = "PID";
    private static final String KEY_BUNDLE_STATE_TYPE = "STATE_TYPE";
    private static final String KEY_BUNDLE_UID = "UID";
    private static final int MAX_PKG_CNT = 1000;
    private static final int MAX_SIZE = 128;
    private static final int MIN_SIZE = 0;
    private static final int MSG_GET_DYNAMIC_PROFILE = 3;
    private static final int MSG_GET_PROFILE = 2;
    private static final int MSG_MIDDLEWARE_BASE = 0;
    private static final int MSG_RESUME_STATE = 1;
    private static final int MSG_UPDATE_STATE = 0;
    private static final int NONE_DTB_BIND_FLAG = -1;
    private static final float POW_FOR_SCREEN_SIZE = 2.0f;
    private static final String PRODUCT_MODEL = "ro.product.model";
    private static final int SCREEN_SIZE_FIGURES = 2;
    private static final int STATE_AUDIO_OUT = 0;
    private static final String[] STATE_STRING = {"STATE_AUDIO_OUT"};
    private static final String SYSTEM_DEVICE_NAME = "ro.config.marketing_name";
    private static final String TAG = "AwareMiddleware";
    private static final String TYPE_DEVICE_NAME = "device_name";
    private static final String TYPE_DEVICE_TYPE = "device_type";
    private static final String TYPE_SCREEN_RESOLUTION = "screen_resolution";
    private static final String TYPE_SCREEN_SIZE = "screen_size";
    private static AwareMiddleware sInstance;
    private volatile IAwareObserver mAwareObserver = null;
    private Context mContext = null;
    private DisplayMetrics mDisplayMetrics = null;
    private final Set<String> mDtbBindPackageNames = new ArraySet();
    private final Map<String, String> mLocalProfile = new ArrayMap();
    private MiddlewareHandler mMiddlewareHandler = null;
    private final SparseArray<SparseArray<ArraySet<Integer>>> mStateCacheIdInfos = new SparseArray<>();
    private final Set<String> mZidanePkgs = new ArraySet();

    private AwareMiddleware() {
        if (HOSP_ENABLE) {
            this.mMiddlewareHandler = new MiddlewareHandler(BackgroundThread.get().getLooper());
        }
    }

    public static synchronized AwareMiddleware getInstance() {
        AwareMiddleware awareMiddleware;
        synchronized (AwareMiddleware.class) {
            if (sInstance == null) {
                sInstance = new AwareMiddleware();
            }
            awareMiddleware = sInstance;
        }
        return awareMiddleware;
    }

    public void init(Context context) {
        if (HOSP_ENABLE && context != null) {
            this.mContext = context;
        }
    }

    public void handleParcel(Parcel data, Parcel reply) {
        if (HOSP_ENABLE) {
            if (data == null || reply == null) {
                AwareLog.e(TAG, "illegal parcel.");
                return;
            }
            int subCode = data.readInt();
            switch (subCode) {
                case 1:
                    registerObserver(IAwareObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeBoolean(true);
                    return;
                case 2:
                    unregisterObserver();
                    return;
                case 3:
                    initZidaneApk(data);
                    reply.writeBoolean(true);
                    return;
                case 4:
                    addZidaneApk(data);
                    reply.writeBoolean(true);
                    return;
                case 5:
                    removeZidaneApk(data);
                    reply.writeBoolean(true);
                    return;
                case 6:
                    updateDtbBindRelation(data);
                    return;
                case 7:
                    uniPerfEvent(data, reply);
                    return;
                case 8:
                    uniPerfEventWithPayload(data, reply);
                    return;
                case 9:
                    uniPerfGetConfig(data, reply);
                    return;
                default:
                    AwareLog.e(TAG, "handleParcel failed, illegal subCode " + subCode);
                    return;
            }
        }
    }

    public void sendMsgStateChange(int stateType, int eventType, int pid, int uid) {
        if (HOSP_ENABLE && stateType == 2) {
            Message msg = this.mMiddlewareHandler.obtainMessage();
            msg.what = 0;
            Bundle data = new Bundle();
            data.putInt(KEY_BUNDLE_STATE_TYPE, stateType);
            data.putInt(KEY_BUNDLE_EVENT_TYPE, eventType);
            data.putInt(KEY_BUNDLE_PID, pid);
            data.putInt(KEY_BUNDLE_UID, uid);
            msg.setData(data);
            this.mMiddlewareHandler.sendMessage(msg);
        }
    }

    public boolean isAppDtbBind(String packageName) {
        boolean contains;
        if (!HOSP_ENABLE) {
            return false;
        }
        if (packageName == null || packageName.isEmpty()) {
            AwareLog.d(TAG, "try check dtb bind for null package name.");
            return false;
        }
        synchronized (this.mDtbBindPackageNames) {
            contains = this.mDtbBindPackageNames.contains(packageName);
        }
        return contains;
    }

    public void onProfileChanged() {
        if (HOSP_ENABLE && this.mAwareObserver != null) {
            sendMsgChangedProfile();
        }
    }

    public void dumpStateCacheIdInfos(PrintWriter pw) {
        if (HOSP_ENABLE && pw != null) {
            pw.println("dump State Cache Ids start --------");
            synchronized (this.mStateCacheIdInfos) {
                for (int i = 0; i < this.mStateCacheIdInfos.size(); i++) {
                    int stateType = this.mStateCacheIdInfos.keyAt(i);
                    pw.println("State[" + stateToString(stateType) + "]:");
                    dumpCacheIdInfos(pw, this.mStateCacheIdInfos.get(stateType));
                }
            }
            pw.println("dump State Cache Ids end-----------");
        }
    }

    public boolean isZApp(String pkg) {
        boolean contains;
        if (!HOSP_ENABLE || pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (this.mZidanePkgs) {
            contains = this.mZidanePkgs.contains(pkg);
        }
        return contains;
    }

    public void dumpZAppPkgs(PrintWriter pw) {
        if (HOSP_ENABLE && pw != null) {
            pw.println("== DumpZAppPkgs Start ==");
            synchronized (this.mZidanePkgs) {
                Iterator<String> it = this.mZidanePkgs.iterator();
                while (it.hasNext()) {
                    pw.println("  " + it.next());
                }
            }
        }
    }

    private void dumpCacheIdInfos(PrintWriter pw, SparseArray<ArraySet<Integer>> cacheIdInfos) {
        if (pw != null) {
            for (int i = 0; i < cacheIdInfos.size(); i++) {
                int uid = cacheIdInfos.keyAt(i);
                pw.println("Uid:" + uid + ", Pids:" + cacheIdInfos.get(uid));
            }
        }
    }

    private static String stateToString(int stateType) {
        if (stateType < 0) {
            return "STATE_ILLEGAL";
        }
        String[] strArr = STATE_STRING;
        if (stateType >= strArr.length) {
            return "STATE_ILLEGAL";
        }
        return strArr[stateType];
    }

    private void sendMsgResumeState(int stateType, int pid, int uid) {
        Message msg = this.mMiddlewareHandler.obtainMessage();
        msg.what = 1;
        Bundle data = new Bundle();
        data.putInt(KEY_BUNDLE_STATE_TYPE, stateType);
        data.putInt(KEY_BUNDLE_PID, pid);
        data.putInt(KEY_BUNDLE_UID, uid);
        msg.setData(data);
        this.mMiddlewareHandler.sendMessage(msg);
    }

    private void sendMsgGetProfile() {
        Message msg = this.mMiddlewareHandler.obtainMessage();
        msg.what = 2;
        this.mMiddlewareHandler.sendMessage(msg);
    }

    private void sendMsgChangedProfile() {
        MiddlewareHandler middlewareHandler = this.mMiddlewareHandler;
        if (middlewareHandler != null) {
            Message msg = middlewareHandler.obtainMessage();
            msg.what = 3;
            this.mMiddlewareHandler.sendMessage(msg);
        }
    }

    private void registerObserver(IAwareObserver observer) {
        this.mAwareObserver = observer;
        resumeStates();
        sendMsgGetProfile();
    }

    private void unregisterObserver() {
        this.mAwareObserver = null;
    }

    private void resumeStates() {
        resumeState(0);
    }

    private void resumeState(int stateType) {
        if (this.mAwareObserver == null) {
            AwareLog.d(TAG, "null aware observer, resume failed.");
            return;
        }
        synchronized (this.mStateCacheIdInfos) {
            SparseArray<ArraySet<Integer>> cacheIdInfos = this.mStateCacheIdInfos.get(stateType);
            if (cacheIdInfos == null) {
                AwareLog.d(TAG, "no cache ids for state.");
                return;
            }
            for (int i = 0; i < cacheIdInfos.size(); i++) {
                int uid = cacheIdInfos.keyAt(i);
                Iterator<Integer> it = cacheIdInfos.get(uid).iterator();
                while (it.hasNext()) {
                    sendMsgResumeState(stateType, it.next().intValue(), uid);
                }
            }
        }
    }

    private void onStateChanged(int stateType, int eventType, int pid, int uid) {
        AwareLog.d(TAG, "onStateChanged, stateType:" + stateType + " eventType:" + eventType + " pid:" + pid + " uid:" + uid);
        if (this.mAwareObserver == null) {
            AwareLog.d(TAG, "null aware observer, on state changed failed.");
        } else if (stateType != 2) {
        } else {
            if (eventType == 1) {
                try {
                    this.mAwareObserver.onStateChanged(0, 1, pid, uid);
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "aware observer onStateChanged failed.");
                }
            } else if (eventType == 2) {
                this.mAwareObserver.onStateChanged(0, 0, pid, uid);
            } else {
                AwareLog.d(TAG, "useless event type when notify.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStateCacheIds(int stateType, int eventType, int pid, int uid) {
        if (stateType == 2) {
            onStateChanged(stateType, eventType, pid, uid);
            synchronized (this.mStateCacheIdInfos) {
                SparseArray<ArraySet<Integer>> cacheIdInfos = this.mStateCacheIdInfos.get(0);
                if (cacheIdInfos == null) {
                    cacheIdInfos = new SparseArray<>();
                    this.mStateCacheIdInfos.put(0, cacheIdInfos);
                }
                if (eventType == 1) {
                    addCacheIdInfo(pid, uid, cacheIdInfos);
                } else if (eventType == 2) {
                    removeCacheIdInfo(pid, uid, cacheIdInfos);
                } else {
                    AwareLog.d(TAG, "useless event type when update.");
                }
            }
        }
    }

    private void addCacheIdInfo(int pid, int uid, SparseArray<ArraySet<Integer>> cacheIdInfos) {
        ArraySet<Integer> pids = cacheIdInfos.get(uid);
        if (pids == null) {
            pids = new ArraySet<>();
            cacheIdInfos.put(uid, pids);
        }
        pids.add(Integer.valueOf(pid));
    }

    private void removeCacheIdInfo(int pid, int uid, SparseArray<ArraySet<Integer>> cacheIdInfos) {
        ArraySet<Integer> pids = cacheIdInfos.get(uid);
        if (pids == null) {
            AwareLog.d(TAG, "try remove not exist id.");
            return;
        }
        pids.remove(Integer.valueOf(pid));
        if (pids.isEmpty()) {
            cacheIdInfos.remove(uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getProfile() {
        if (this.mLocalProfile.isEmpty()) {
            getDeviceType();
            getDeviceName();
            getScreenResolution();
            getScreenSize();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getDynamicProfie() {
        getDeviceName();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onProfileNotify() {
        if (this.mAwareObserver == null) {
            AwareLog.e(TAG, "notify profile failed, null observer.");
        } else if (this.mLocalProfile.isEmpty()) {
            AwareLog.e(TAG, "notify profile failed, local profile has nothing.");
        } else {
            try {
                this.mAwareObserver.onProfileNotify(this.mLocalProfile);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "aware observer notify profile failed.");
            }
        }
    }

    private void getDeviceType() {
        this.mLocalProfile.put(TYPE_DEVICE_TYPE, SystemProperties.get("ro.build.characteristics", ModelBaseService.UNKONW_IDENTIFY_RET));
    }

    private void getDeviceName() {
        Context context = this.mContext;
        if (context == null) {
            AwareLog.e(TAG, "context is null");
            return;
        }
        String deviceName = Settings.Global.getStringForUser(context.getContentResolver(), "unified_device_name", 0);
        if (deviceName == null || deviceName.equals("")) {
            deviceName = SystemProperties.get(SYSTEM_DEVICE_NAME);
        }
        if (deviceName == null || deviceName.equals("")) {
            deviceName = SystemProperties.get(PRODUCT_MODEL, DEFAULT_DEVICE_NAME);
        }
        if (deviceName == null || deviceName.equals("")) {
            AwareLog.e(TAG, "device name is empty");
            return;
        }
        AwareLog.i(TAG, "get device name successfully");
        this.mLocalProfile.put(TYPE_DEVICE_NAME, deviceName);
    }

    private void getScreenResolution() {
        getDisplayMetrics();
        DisplayMetrics displayMetrics = this.mDisplayMetrics;
        if (displayMetrics != null) {
            int width = displayMetrics.widthPixels;
            int height = this.mDisplayMetrics.heightPixels;
            Map<String, String> map = this.mLocalProfile;
            map.put(TYPE_SCREEN_RESOLUTION, new String(width + "*" + height));
        }
    }

    private void getScreenSize() {
        getDisplayMetrics();
        if (this.mDisplayMetrics != null) {
            IWindowManager iWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService("window"));
            if (iWindowManager != null) {
                Point point = new Point();
                try {
                    iWindowManager.getInitialDisplaySize(0, point);
                    this.mLocalProfile.put(TYPE_SCREEN_SIZE, String.valueOf(new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / this.mDisplayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / this.mDisplayMetrics.ydpi), 2.0d))).setScale(2, 4).doubleValue()));
                    return;
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "remote exception when get screen size.");
                }
            }
            this.mLocalProfile.put(TYPE_SCREEN_SIZE, String.valueOf(new BigDecimal(Math.sqrt(Math.pow((double) (((float) this.mDisplayMetrics.widthPixels) / this.mDisplayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) this.mDisplayMetrics.heightPixels) / this.mDisplayMetrics.ydpi), 2.0d))).setScale(2, 4).doubleValue()));
        }
    }

    private void getDisplayMetrics() {
        Display display;
        if (this.mDisplayMetrics == null) {
            Object obj = this.mContext.getSystemService("window");
            WindowManager windowManager = obj instanceof WindowManager ? (WindowManager) obj : null;
            if (windowManager != null && (display = windowManager.getDefaultDisplay()) != null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                display.getRealMetrics(displayMetrics);
                if (displayMetrics.widthPixels > 0 || displayMetrics.heightPixels > 0) {
                    this.mDisplayMetrics = displayMetrics;
                }
            }
        }
    }

    private void updateDtbBindRelation(Parcel data) {
        if (data == null) {
            AwareLog.e(TAG, "updateDtbBindRelation failed, null data.");
            return;
        }
        ArraySet<String> dtbBindPackageNames = null;
        int size = data.readInt();
        if (size > 0) {
            if (size > 1000) {
                AwareLog.w(TAG, "updateDtbBindRelation too many package names, size = " + size);
                size = 1000;
            }
            dtbBindPackageNames = new ArraySet<>();
            for (int i = 0; i < size; i++) {
                String packageName = data.readString();
                if (packageName == null || packageName.isEmpty()) {
                    AwareLog.e(TAG, "updateDtbBindRelation failed, illegal package name.");
                    return;
                } else {
                    dtbBindPackageNames.add(packageName);
                }
            }
        } else if (size != -1) {
            AwareLog.e(TAG, "updateDtbBindRelation failed, illegal size = " + size);
            return;
        }
        synchronized (this.mDtbBindPackageNames) {
            this.mDtbBindPackageNames.clear();
            if (dtbBindPackageNames != null) {
                this.mDtbBindPackageNames.addAll(dtbBindPackageNames);
            }
        }
    }

    private void uniPerfEvent(Parcel data, Parcel reply) {
        reply.writeInt(uniPerfEventEx(data.readInt(), new int[0]));
    }

    private void uniPerfEventWithPayload(Parcel data, Parcel reply) {
        reply.writeInt(uniPerfEventEx(data.readInt(), data.readInt()));
    }

    private int uniPerfEventEx(int cmdId, int... payload) {
        return UniPerf.getInstance().uniPerfEvent(cmdId, "", payload);
    }

    /* JADX INFO: Multiple debug info for r2v3 int[]: [D('i' int), D('values' int[])] */
    private void uniPerfGetConfig(Parcel data, Parcel reply) {
        int tagsSize = data.readInt();
        if (tagsSize <= 0 || tagsSize >= 128) {
            AwareLog.e(TAG, "uniPerfGetConfig tags size invalid.");
            return;
        }
        int[] tags = new int[tagsSize];
        for (int i = 0; i < tagsSize; i++) {
            tags[i] = data.readInt();
        }
        int[] values = new int[tagsSize];
        reply.writeInt(UniPerf.getInstance().uniPerfGetConfig(tags, values));
        for (int value : values) {
            reply.writeInt(value);
        }
    }

    private void initZidaneApk(Parcel data) {
        int pkgCnt;
        if (data != null && (pkgCnt = data.readInt()) > 0 && pkgCnt < 1000) {
            Set<String> pkgSets = new ArraySet<>();
            for (int i = 0; i < pkgCnt; i++) {
                String pkg = data.readString();
                if (pkg != null && !pkg.isEmpty()) {
                    pkgSets.add(pkg);
                }
            }
            synchronized (this.mZidanePkgs) {
                this.mZidanePkgs.clear();
                this.mZidanePkgs.addAll(pkgSets);
            }
        }
    }

    private void addZidaneApk(Parcel data) {
        String pkgName;
        if (data != null && (pkgName = data.readString()) != null && !pkgName.isEmpty()) {
            synchronized (this.mZidanePkgs) {
                this.mZidanePkgs.add(pkgName);
            }
        }
    }

    private void removeZidaneApk(Parcel data) {
        String pkgName;
        if (data != null && (pkgName = data.readString()) != null && !pkgName.isEmpty()) {
            synchronized (this.mZidanePkgs) {
                this.mZidanePkgs.remove(pkgName);
            }
        }
    }

    /* access modifiers changed from: private */
    public class MiddlewareHandler extends Handler {
        public MiddlewareHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.e(AwareMiddleware.TAG, "handleMessage failed, null msg.");
                return;
            }
            Bundle bundle = msg.getData();
            int i = msg.what;
            if (i != 0) {
                if (i != 1) {
                    if (i == 2) {
                        AwareMiddleware.this.getProfile();
                        AwareMiddleware.this.onProfileNotify();
                    } else if (i == 3) {
                        AwareMiddleware.this.getDynamicProfie();
                        AwareMiddleware.this.onProfileNotify();
                    }
                } else if (bundle == null) {
                    AwareLog.e(AwareMiddleware.TAG, "handleMessage resume failed, null bundle.");
                } else {
                    try {
                        AwareMiddleware.this.mAwareObserver.onStateChanged(bundle.getInt(AwareMiddleware.KEY_BUNDLE_STATE_TYPE), 1, bundle.getInt(AwareMiddleware.KEY_BUNDLE_PID), bundle.getInt(AwareMiddleware.KEY_BUNDLE_UID));
                    } catch (RemoteException e) {
                        AwareLog.e(AwareMiddleware.TAG, "onStateChanged failed when resume.");
                    }
                }
            } else if (bundle == null) {
                AwareLog.e(AwareMiddleware.TAG, "handleMessage update failed, null bundle.");
            } else {
                int stateType = bundle.getInt(AwareMiddleware.KEY_BUNDLE_STATE_TYPE);
                int pid = bundle.getInt(AwareMiddleware.KEY_BUNDLE_PID);
                int uid = bundle.getInt(AwareMiddleware.KEY_BUNDLE_UID);
                AwareMiddleware.this.updateStateCacheIds(stateType, bundle.getInt(AwareMiddleware.KEY_BUNDLE_EVENT_TYPE), pid, uid);
            }
        }
    }
}
