package com.huawei.pgmng.plug;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.pgmng.plug.IPGSdk;
import com.huawei.pgmng.plug.IStateRecognitionSink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PGSdk implements IBinder.DeathRecipient {
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final int PG_ACTION_STATE_BASE = 10000;
    public static final int PG_ID_2DGAME_FRONT = 10011;
    public static final int PG_ID_3DGAME_FRONT = 10002;
    public static final int PG_ID_BROWSER_FRONT = 10001;
    public static final int PG_ID_CAMERA_END = 10017;
    public static final int PG_ID_CAMERA_FRONT = 10007;
    public static final int PG_ID_EBOOK_FRONT = 10003;
    public static final int PG_ID_GALLERY_FRONT = 10004;
    public static final int PG_ID_INPUT_END = 10006;
    public static final int PG_ID_INPUT_START = 10005;
    public static final int PG_ID_LAUNCHER_FRONT = 10010;
    public static final int PG_ID_MMS_FRONT = 10013;
    public static final int PG_ID_OFFICE_FRONT = 10008;
    public static final int PG_ID_VIDEO_END = 10016;
    public static final int PG_ID_VIDEO_FRONT = 10009;
    public static final int PG_ID_VIDEO_START = 10015;
    public static final int RES_TYPE_ALL = 65535;
    public static final int RES_TYPE_AUTOSTART = 64;
    public static final int RES_TYPE_BT_SCN = 8;
    public static final int RES_TYPE_CPU = 1;
    public static final int RES_TYPE_GPS = 4;
    public static final int RES_TYPE_HIGH_CURRENT = 128;
    public static final int RES_TYPE_NETLOCATION = 32;
    public static final int RES_TYPE_WAKELOCK = 2;
    public static final int RES_TYPE_WIFI_SCN = 16;
    public static final int STATE_APP_TYPE = 13;
    public static final int STATE_AUDIO_IN = 1;
    public static final int STATE_AUDIO_OUT = 2;
    public static final int STATE_BLUETOOTH = 8;
    public static final int STATE_GPS = 3;
    public static final int STATE_HIBERNATE = 6;
    public static final int STATE_NAT_TIMEOUT = 11;
    public static final int STATE_RESOURCE_ABNORMAL = 50;
    public static final int STATE_RESTART = 7;
    public static final int STATE_RES_MGR = 12;
    public static final int STATE_SENSOR = 4;
    public static final int STATE_THERMAL = 9;
    public static final int STATE_UPLOAD_DL = 5;
    private static final String TAG = "PGSdk";
    public static final int TYPE_APP_MARKET = 20;
    public static final int TYPE_BROWSER = 6;
    public static final int TYPE_CAMERA = 24;
    public static final int TYPE_CLOCK = 10;
    public static final int TYPE_EBOOK = 7;
    public static final int TYPE_EDUCATION = 22;
    public static final int TYPE_EMAIL = 3;
    public static final int TYPE_GALLERY = 16;
    public static final int TYPE_GAME = 5;
    public static final int TYPE_IM = 11;
    public static final int TYPE_INPUTMETHOD = 4;
    public static final int TYPE_LAUNCHER = 1;
    public static final int TYPE_LIFE_TOOL = 21;
    public static final int TYPE_LOCATION_PROVIDER = 14;
    public static final int TYPE_MONEY = 23;
    public static final int TYPE_MUSIC = 12;
    public static final int TYPE_NAVIGATION = 13;
    public static final int TYPE_NEWS_CLIENT = 18;
    public static final int TYPE_OFFICE = 15;
    public static final int TYPE_PEDOMETER = 25;
    public static final int TYPE_SCRLOCK = 9;
    public static final int TYPE_SHOP = 19;
    public static final int TYPE_SIP = 17;
    public static final int TYPE_SMS = 2;
    public static final int TYPE_UNKNOW = -1;
    public static final int TYPE_VIDEO = 8;
    private static BackLightAdjCfg mBackLightAdjCfg;
    private static IPGSdk mService;
    private static PGSdk sInstance = null;
    private final ArrayList<Integer> mEnabledStates = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final HashSet<Sink> mSinkSet = new HashSet<>();
    /* access modifiers changed from: private */
    public final HashMap<Sink, ArrayList<Integer>> mSinkSetStates = new HashMap<>();
    private final SinkTransport mSinkTransport = new SinkTransport();

    public interface ResourceSink extends Sink {
        void onPowerOverUsing(String str, int i, long j, long j2, String str2);
    }

    public interface Sink {
        void onStateChanged(int i, int i2, int i3, String str, int i4);
    }

    private final class SinkTransport extends IStateRecognitionSink.Stub {
        private SinkTransport() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            synchronized (PGSdk.this.mLock) {
                if (!PGSdk.this.mSinkSet.isEmpty()) {
                    Iterator it = PGSdk.this.mSinkSet.iterator();
                    while (it.hasNext()) {
                        Sink sink = (Sink) it.next();
                        ArrayList<Integer> states = (ArrayList) PGSdk.this.mSinkSetStates.get(sink);
                        if (states != null && states.contains(Integer.valueOf(stateType))) {
                            sink.onStateChanged(stateType, eventType, pid, pkg, uid);
                        }
                    }
                }
            }
        }

        public void onPowerOverUsing(String module, int resourceType, long stats_duration, long hold_time, String extend) {
            synchronized (PGSdk.this.mLock) {
                if (!PGSdk.this.mSinkSet.isEmpty()) {
                    Iterator it = PGSdk.this.mSinkSet.iterator();
                    while (it.hasNext()) {
                        Sink sink = (Sink) it.next();
                        ArrayList<Integer> states = (ArrayList) PGSdk.this.mSinkSetStates.get(sink);
                        if (states != null && states.contains(50) && (sink instanceof ResourceSink)) {
                            ((ResourceSink) sink).onPowerOverUsing(module, resourceType, stats_duration, hold_time, extend);
                        }
                    }
                }
            }
        }
    }

    static {
        mBackLightAdjCfg = null;
        mBackLightAdjCfg = new BackLightAdjCfg();
    }

    private PGSdk() {
    }

    public static PGSdk getInstance() {
        PGSdk pGSdk;
        synchronized (PGSdk.class) {
            if (sInstance == null) {
                sInstance = new PGSdk();
                if (!sInstance.initPGSdkService()) {
                    sInstance = null;
                }
            }
            pGSdk = sInstance;
        }
        return pGSdk;
    }

    private boolean initPGSdkService() {
        IPGSdk pgService = IPGSdk.Stub.asInterface(ServiceManager.getService("powergenius"));
        if (pgService != null) {
            mService = pgService;
            try {
                mService.asBinder().linkToDeath(this, 0);
            } catch (Exception e) {
                Log.w(TAG, "linkToDeath failed !");
            }
            return true;
        }
        Log.e(TAG, "PG Server is not found. calling pid: " + Binder.getCallingPid());
        return false;
    }

    public boolean checkStateByPid(Context context, int pid, int state) throws RemoteException {
        return mService.checkStateByPid(context.getPackageName(), pid, state);
    }

    public boolean checkStateByPkg(Context context, String pkg, int state) throws RemoteException {
        return mService.checkStateByPkg(context.getPackageName(), pkg, state);
    }

    public Map<String, String> getSensorInfoByUid(Context context, int uid) throws RemoteException {
        return mService.getSensorInfoByUid(context.getPackageName(), uid);
    }

    public int getPkgType(Context context, String pkg) throws RemoteException {
        return mService.getPkgType(context.getPackageName(), pkg);
    }

    public List<String> getHibernateApps(Context context) throws RemoteException {
        return mService.getHibernateApps(context.getPackageName());
    }

    public boolean hibernateApps(Context context, List<String> pkgNames, String reason) throws RemoteException {
        return mService.hibernateApps(context.getPackageName(), pkgNames, reason);
    }

    public boolean fastHibernation(Context context, List<AppInfo> appInfo, int duration, String reason) throws RemoteException {
        return mService.fastHibernation(context.getPackageName(), appInfo, duration, reason);
    }

    public boolean applyForResourceUse(Context context, String module, int resourceType, long timeoutInMS, String reason) throws RemoteException {
        return mService.applyForResourceUse(context.getPackageName(), true, module, resourceType, timeoutInMS, reason);
    }

    public boolean unapplyForResourceUse(Context context, String module, int resourceType) throws RemoteException {
        return mService.applyForResourceUse(context.getPackageName(), false, module, resourceType, -1, null);
    }

    public boolean notifyCallingModules(Context context, String self, List<String> callingModules) throws RemoteException {
        return mService.notifyCallingModules(context.getPackageName(), self, callingModules);
    }

    public String getTopFrontApp(Context context) throws RemoteException {
        return mService.getTopFrontApp(context.getPackageName());
    }

    public int getThermalInfo(Context context, int type) throws RemoteException {
        return mService.getThermalInfo(context.getPackageName(), type);
    }

    public List<DetailBatterySipper> getBatteryStats(Context context, List<UserHandle> userList) throws RemoteException {
        return mService.getBatteryStats(context.getPackageName(), userList);
    }

    private void startStateRecognitionProvider() {
        try {
            mService.registerSink(this.mSinkTransport);
        } catch (RemoteException e) {
            Log.e(TAG, "register sink transport fail.");
        }
    }

    private void stopStateRecognitionProvider() {
        try {
            mService.unregisterSink(this.mSinkTransport);
        } catch (RemoteException e) {
            Log.e(TAG, "unregister sink transport fail.");
        }
    }

    public int[] getSupportedStates() throws RemoteException {
        return mService.getSupportedStates();
    }

    public boolean isStateSupported(int stateType) throws RemoteException {
        return mService.isStateSupported(stateType);
    }

    private boolean registerSink(Sink sink) {
        if (sink == null) {
            Log.e(TAG, "registerSink a null sink fail.");
            return false;
        }
        if (!this.mSinkSet.contains(sink)) {
            this.mSinkSet.add(sink);
            if (this.mSinkSet.size() == 1) {
                startStateRecognitionProvider();
            }
        }
        return true;
    }

    private void unregisterSink(Sink sink) {
        this.mSinkSet.remove(sink);
        if (this.mSinkSet.size() == 0) {
            stopStateRecognitionProvider();
        }
    }

    public boolean enableStateEvent(Sink sink, int stateType) throws RemoteException {
        synchronized (this.mLock) {
            if (!registerSink(sink)) {
                return false;
            }
            ArrayList<Integer> states = this.mSinkSetStates.get(sink);
            if (states == null) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(Integer.valueOf(stateType));
                this.mSinkSetStates.put(sink, arrayList);
            } else {
                states.add(Integer.valueOf(stateType));
            }
            this.mEnabledStates.add(Integer.valueOf(stateType));
            return mService.enableStateEvent(stateType);
        }
    }

    public boolean disableStateEvent(Sink sink, int stateType) throws RemoteException {
        synchronized (this.mLock) {
            ArrayList<Integer> states = this.mSinkSetStates.get(sink);
            if (states != null) {
                states.remove(Integer.valueOf(stateType));
                if (states.size() == 0) {
                    this.mSinkSetStates.remove(sink);
                    unregisterSink(sink);
                }
            }
            this.mEnabledStates.remove(Integer.valueOf(stateType));
        }
        return mService.disableStateEvent(stateType);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0066, code lost:
        return;
     */
    public void binderDied() {
        Log.e(TAG, "PG Process Binder was died and connecting ...");
        int maxCount = 0;
        while (!initPGSdkService() && maxCount < 200) {
            SystemClock.sleep(maxCount < 60 ? 1000 : 5000);
            maxCount++;
        }
        synchronized (this.mLock) {
            if (!this.mSinkSet.isEmpty()) {
                if (mService != null) {
                    this.mSinkTransport.onStateChanged(7, 0, 0, "", 0);
                    startStateRecognitionProvider();
                    Iterator<Integer> it = this.mEnabledStates.iterator();
                    while (it.hasNext()) {
                        try {
                            mService.enableStateEvent(it.next().intValue());
                        } catch (RemoteException e) {
                            Log.e(TAG, "enable state type fail.");
                        }
                    }
                }
            }
        }
    }

    public static boolean isApkShouldAdjBackLight(String pkgName) {
        return mBackLightAdjCfg != null && mBackLightAdjCfg.isApkShouldAdjBackLight(pkgName);
    }

    public boolean isKeptAliveApp(Context ctx, String pkg, int uid) throws RemoteException {
        if (mService == null || ctx == null) {
            return false;
        }
        return mService.isKeptAliveApp(ctx.getPackageName(), pkg, uid);
    }
}
