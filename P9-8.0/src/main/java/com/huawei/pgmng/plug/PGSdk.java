package com.huawei.pgmng.plug;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.LogException;
import com.huawei.pgmng.plug.IStateRecognitionSink.Stub;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class PGSdk implements DeathRecipient {
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
    public static final int STATE_AUDIO_IN = 1;
    public static final int STATE_AUDIO_OUT = 2;
    public static final int STATE_BLUETOOTH = 8;
    public static final int STATE_GPS = 3;
    public static final int STATE_HIBERNATE = 6;
    public static final int STATE_RESTART = 7;
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
    private static IPGSdk mService;
    private static PGSdk sInstance = null;
    private final ArrayList<Integer> mEnabledStates = new ArrayList();
    private final Object mLock = new Object();
    private final HashSet<Sink> mSinkSet = new HashSet();
    private final HashMap<Sink, ArrayList<Integer>> mSinkSetStates = new HashMap();
    private final SinkTransport mSinkTransport = new SinkTransport(this, null);

    public interface Sink {
        void onStateChanged(int i, int i2, int i3, String str, int i4);
    }

    private final class SinkTransport extends Stub {
        /* synthetic */ SinkTransport(PGSdk this$0, SinkTransport -this1) {
            this();
        }

        private SinkTransport() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            synchronized (PGSdk.this.mLock) {
                if (PGSdk.this.mSinkSet.isEmpty()) {
                    return;
                }
                for (Sink sink : PGSdk.this.mSinkSet) {
                    ArrayList<Integer> states = (ArrayList) PGSdk.this.mSinkSetStates.get(sink);
                    if (states != null && states.contains(Integer.valueOf(stateType))) {
                        sink.onStateChanged(stateType, eventType, pid, pkg, uid);
                    }
                }
            }
        }
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
            if (registerSink(sink)) {
                ArrayList<Integer> states = (ArrayList) this.mSinkSetStates.get(sink);
                if (states == null) {
                    states = new ArrayList();
                    states.add(Integer.valueOf(stateType));
                    this.mSinkSetStates.put(sink, states);
                } else {
                    states.add(Integer.valueOf(stateType));
                }
                this.mEnabledStates.add(Integer.valueOf(stateType));
                return mService.enableStateEvent(stateType);
            }
            return false;
        }
    }

    public boolean disableStateEvent(Sink sink, int stateType) throws RemoteException {
        synchronized (this.mLock) {
            ArrayList<Integer> states = (ArrayList) this.mSinkSetStates.get(sink);
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

    /* JADX WARNING: Missing block: B:30:0x0066, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void binderDied() {
        Log.e(TAG, "PG Process Binder was died and connecting ...");
        int maxCount = 60;
        while (!initPGSdkService() && maxCount > 0) {
            SystemClock.sleep(1000);
            maxCount--;
        }
        synchronized (this.mLock) {
            if (this.mSinkSet.isEmpty()) {
            } else if (mService != null) {
                this.mSinkTransport.onStateChanged(7, 0, 0, LogException.NO_VALUE, 0);
                startStateRecognitionProvider();
                for (Integer stateType : this.mEnabledStates) {
                    try {
                        mService.enableStateEvent(stateType.intValue());
                    } catch (RemoteException e) {
                        Log.e(TAG, "enable state type fail.");
                    }
                }
            }
        }
    }
}
