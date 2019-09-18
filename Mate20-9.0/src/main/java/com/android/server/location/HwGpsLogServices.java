package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.IMonitor;
import android.util.Log;
import com.android.internal.location.ProviderRequest;
import com.android.server.LocationManagerService;
import com.android.server.LocationManagerServiceUtil;
import com.huawei.ncdft.HwNcDftConnManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

public class HwGpsLogServices implements IHwGpsLogServices {
    private static final int BUF_MIXSIZE = 10;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int DFT_ASSERT_ERROR_CODE = 1002;
    private static final int DFT_CHIP_ASSERT_EVENT = 910009014;
    private static final int DOMAIN_GNSS = 0;
    public static final String KEY_FREEZE_CHANGE_TIME = "freeze_time";
    public static final String KEY_FREEZE_IS_FRONT = "is_front";
    public static final String KEY_FREEZE_OR_UNFREEZE = "proxy";
    public static final String KEY_FREEZE_PACKAGE_NAME = "freeze_pkg";
    public static final String KEY_FREEZE_UID = "freeze_uid";
    public static final String KEY_IAWARE_CHANGE_TIME = "iaware_time";
    public static final String KEY_IAWARE_EXPECT_MODE = "iaware_mode";
    public static final String KEY_IAWARE_PACKAGE_NAME = "iaware_pkg";
    public static final String KEY_IDLE_ACTION = "is_idle";
    public static final String KEY_IDLE_CHANGE_TIME = "idle_time";
    public static final String KEY_IDLE_GNSS_STATUS = "idle_gnss";
    public static final String KEY_IDLE_SCREEN_STATUS = "idle_screen";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_IS_FREEZE = "is_freeze";
    public static final String KEY_IS_IAWARE_CONTROL = "is_iaware_control";
    public static final String KEY_MIN_DISTANCE = "min_dis";
    public static final String KEY_PENDING_LOCK = "lock";
    public static final String KEY_PKG_NAME = "pkg";
    public static final String KEY_RECEIVER_HASH = "receiver_hash";
    public static final String KEY_SESSION_START_FLAG = "session_start";
    public static final String KEY_SESSION_STOP_FLAG = "session_stop";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_STOP_TIME = "stop_time";
    private static final long NETWORK_POS_TIMEOUT_SECOND = 11000;
    private static final String TAG = "HwGnssLogServices";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static HwGpsLogServices mHwGpsLogServices;
    private HwNcDftConnManager hwNcDftConnManager;
    private Context mContext;
    /* access modifiers changed from: private */
    public LocationManagerServiceUtil mLocationManagerServiceUtil;
    /* access modifiers changed from: private */
    public boolean mNetWorkFixPending = false;
    private Timer mNlpTimer = null;
    private TimerTask mNlpTimerTask = null;
    private HandlerThread mThread;

    private HwGpsLogServices(Context context) {
        this.mContext = context;
        Log.d(TAG, "enter HwGpsLogServices");
        this.mThread = new HandlerThread("HwGpsLogServices");
        this.mThread.start();
        this.hwNcDftConnManager = new HwNcDftConnManager(this.mContext);
    }

    public static synchronized HwGpsLogServices getInstance(Context context) {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            if (mHwGpsLogServices == null) {
                mHwGpsLogServices = new HwGpsLogServices(context);
            }
            hwGpsLogServices = mHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    public static synchronized HwGpsLogServices getGpsLogService() {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            hwGpsLogServices = mHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    private int sendToDft(int event, List<String> list) {
        if (this.hwNcDftConnManager != null) {
            this.hwNcDftConnManager.reportToDft(0, event, list);
            return 0;
        }
        Log.d(TAG, "hwNcDftConnManager is null, ignore!");
        return 1;
    }

    public void netWorkLocation(String provider, ProviderRequest providerRequest) {
        if (provider.equalsIgnoreCase("network")) {
            boolean isNeedUpdate = true;
            if (providerRequest.locationRequests.size() == 0) {
                isNeedUpdate = false;
            } else {
                for (LocationRequest request : providerRequest.locationRequests) {
                    if (request.getNumUpdates() <= 0) {
                        isNeedUpdate = false;
                    }
                }
            }
            if (this.hwNcDftConnManager != null && isNeedUpdate) {
                String requestInterval = String.valueOf(providerRequest.interval);
                List<String> list = new ArrayList<>();
                list.add(requestInterval);
                list.add(provider);
                sendToDft(0, list);
            }
            if (this.mNetWorkFixPending) {
                Log.e(TAG, "Network pos is already runing.");
                return;
            }
            this.mNetWorkFixPending = true;
            startNlpTimer();
        }
    }

    private void startNlpTimer() {
        if (this.mNlpTimer == null) {
            this.mNlpTimer = new Timer();
        }
        if (this.mNlpTimerTask != null) {
            this.mNlpTimerTask.cancel();
            this.mNlpTimerTask = null;
        }
        this.mNlpTimerTask = new TimerTask() {
            public void run() {
                LocationManagerServiceUtil unused = HwGpsLogServices.this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
                if (HwGpsLogServices.this.mLocationManagerServiceUtil == null) {
                    Log.d(HwGpsLogServices.TAG, " mLocationManagerServiceUtil == null ");
                    boolean unused2 = HwGpsLogServices.this.mNetWorkFixPending = false;
                    return;
                }
                LocationProviderInterface p = HwGpsLogServices.this.mLocationManagerServiceUtil.getRealProviders().get("network");
                if (p == null) {
                    Log.d(HwGpsLogServices.TAG, " LocationProviderInterface p is null ");
                    boolean unused3 = HwGpsLogServices.this.mNetWorkFixPending = false;
                    return;
                }
                Bundle extras = new Bundle();
                int status = p.getStatus(extras);
                Log.d(HwGpsLogServices.TAG, "  network position over 11s,  NLP status:  " + status);
                if (status > 2) {
                    HwGpsLogServices.this.updateNLPStatusRecord(status);
                }
                ArrayList<Integer> statusList = extras.getIntegerArrayList("status");
                if (statusList != null) {
                    int i = 10;
                    if (statusList.size() < 10) {
                        i = statusList.size();
                    }
                    int listSize = i;
                    Log.d(HwGpsLogServices.TAG, " list network position over 11s,  list size:  " + listSize);
                    for (int i2 = 0; i2 < listSize; i2++) {
                        HwGpsLogServices.this.updateNLPStatusRecord(statusList.get(i2).intValue());
                    }
                }
                boolean unused4 = HwGpsLogServices.this.mNetWorkFixPending = false;
            }
        };
        try {
            this.mNlpTimer.schedule(this.mNlpTimerTask, NETWORK_POS_TIMEOUT_SECOND);
        } catch (IllegalStateException e) {
            Log.e(TAG, " TimerTask is scheduled failed.");
        }
    }

    private void stopNlpTimer() {
        this.mNetWorkFixPending = false;
        Log.d(TAG, "stopNlpTimer ");
        if (this.mNlpTimer != null) {
            this.mNlpTimer.cancel();
            this.mNlpTimer.purge();
            this.mNlpTimer = null;
        }
        if (this.mNlpTimerTask != null) {
            this.mNlpTimerTask.cancel();
            this.mNlpTimerTask = null;
        }
    }

    public void openGpsSwitchFail(int open) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(open));
        sendToDft(14, list);
    }

    public void initGps(boolean isEnable, byte engineCapabilities) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(isEnable));
        list.add(Integer.toString(engineCapabilities));
        sendToDft(1, list);
    }

    public void updateXtraDloadStatus(boolean status) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(10, list);
    }

    public void updateNtpDloadStatus(boolean status) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(11, list);
    }

    public void updateSetPosMode(boolean status, int interval) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(status));
        list.add(Integer.toString(interval));
        sendToDft(12, list);
    }

    public void updateSetPosMode(boolean status) {
        Log.i(TAG, "updateSetPosMode = " + status);
    }

    public void updateApkName(String provider, String hashCode, String name, String apkRunMode, String apkTimeStamp) {
        if (this.hwNcDftConnManager == null) {
            return;
        }
        if (provider.equalsIgnoreCase("gps") || provider.equalsIgnoreCase("network") || provider.equalsIgnoreCase("APKSTOPPROVIDER")) {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(provider));
            list.add(String.valueOf(hashCode));
            list.add(String.valueOf(name));
            list.add(String.valueOf(apkRunMode));
            list.add(String.valueOf(apkTimeStamp));
            sendToDft(31, list);
        }
    }

    public void updateNLPStatus(int status) {
        stopNlpTimer();
        updateNLPStatusRecord(status);
    }

    public void updateNLPStatusRecord(int status) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(29, list);
    }

    public void startGps(boolean isEnable, int positionMode) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(isEnable));
        list.add(Integer.toString(positionMode));
        sendToDft(2, list);
    }

    public void updateNetworkState(NetworkInfo info) {
        if (this.hwNcDftConnManager != null) {
            this.hwNcDftConnManager.reportNetworkInfo(0, 3, info);
        }
    }

    public void updateAgpsState(int type, int state) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(type));
        list.add(Integer.toString(state));
        sendToDft(4, list);
    }

    public void stopGps(boolean status) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(5, list);
    }

    public void permissionErr(String packageName) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(packageName);
        sendToDft(13, list);
    }

    public void addGeofenceStatus() {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add("0");
        sendToDft(15, list);
    }

    public void addBatchingStatus() {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add("0");
        sendToDft(16, list);
    }

    public void updateGpsRunState(int status) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(status));
        sendToDft(6, list);
    }

    public void updateLocation(Location location, long time, String provider) {
        if (this.hwNcDftConnManager != null) {
            this.hwNcDftConnManager.reportGnssLocation(0, 8, location, time, provider);
        }
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        if (this.hwNcDftConnManager != null) {
            this.hwNcDftConnManager.reportGnssSvStatus(0, 9, svCount, svs, snrs, svElevations, svAzimuths);
        }
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(String.valueOf(currentNtpTime));
        list.add(String.valueOf(realTime));
        sendToDft(19, list);
    }

    public void reportBinderError() {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add("0");
        sendToDft(20, list);
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        Log.d(TAG, "driver err is " + strJsonExceptionBody);
    }

    public void updateModemData(byte aPosMode, byte aAidingDataStatus, byte aAidingDataReqFlg, byte[] aCurNetStatus, byte aAGPSResult, byte aSUPLStatus, byte aTimeFlg, byte aAddrFlg, byte[] aServerAdder, long aSUPLStatusCode, long aAgpsStartTime, long aAtlOpenTime, long aConnSvrTime, long aAgpsEndTime, short aServerIpPort) {
    }

    public void updateNtpServerInfo(String address) {
        if (address != null) {
            List<String> list = new ArrayList<>();
            list.clear();
            list.add(address);
            sendToDft(23, list);
        }
    }

    public void injectExtraParam(String extraParam) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(extraParam);
        sendToDft(24, list);
    }

    public void injectTimeParam(int timeSource, long ntpTime, int uncertainty) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(timeSource));
        list.add(String.valueOf(ntpTime));
        list.add(Integer.toString(uncertainty));
        sendToDft(27, list);
    }

    public int logEvent(int type, int event, String parameter) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(type));
        list.add(Integer.toString(event));
        list.add(parameter);
        int result = sendToDft(25, list);
        if (result != 0) {
            Log.d(TAG, "send Higeo CHR msg error:" + type + "," + event + "," + parameter);
        }
        return result;
    }

    public void setLocationSettingsOffErr(String provider) {
        List<String> list = new ArrayList<>();
        list.add(provider);
        sendToDft(28, list);
    }

    public void setQuickGpsParam(int id, String param) {
        List<String> list = new ArrayList<>();
        list.clear();
        list.add(Integer.toString(id));
        list.add(param);
        sendToDft(30, list);
    }

    public void requestStart(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName, boolean isIAwareControl) {
        String str = packageName;
        if ("gps".equals(request.getProvider())) {
            boolean requestSessionStart = false;
            if (this.mLocationManagerServiceUtil == null) {
                this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
            }
            if (this.mLocationManagerServiceUtil != null) {
                if (this.mLocationManagerServiceUtil.countRealGps() == 0) {
                    requestSessionStart = true;
                }
                boolean requestSessionStart2 = requestSessionStart;
                long startTime = System.currentTimeMillis();
                long interval = request.getInterval();
                float minDistance = request.getSmallestDisplacement();
                boolean isFreeze = GpsFreezeProc.getInstance().isFreeze(str);
                LocationManagerServiceUtil locationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
                String receiverHashcode = Integer.toHexString(System.identityHashCode(receiver));
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("pkg", str);
                    jsonObj.put(KEY_START_TIME, startTime);
                    jsonObj.put("interval", interval);
                    jsonObj.put(KEY_MIN_DISTANCE, (double) minDistance);
                    jsonObj.put(KEY_IS_FREEZE, isFreeze);
                    if (isFreeze) {
                        jsonObj.put(KEY_FREEZE_IS_FRONT, LocationManagerServiceUtil.isForeGroundProc(this.mContext, str) ? 1 : 0);
                    }
                    try {
                        jsonObj.put(KEY_IS_IAWARE_CONTROL, isIAwareControl ? 1 : 0);
                        jsonObj.put(KEY_RECEIVER_HASH, receiverHashcode);
                        jsonObj.put(KEY_SESSION_START_FLAG, requestSessionStart2 ? 1 : 0);
                    } catch (JSONException e) {
                        Log.e(TAG, "requestStart json error!");
                        List<String> list = new ArrayList<>();
                        list.add(jsonObj.toString());
                        sendToDft(100, list);
                    }
                } catch (JSONException e2) {
                    boolean z = isIAwareControl;
                    Log.e(TAG, "requestStart json error!");
                    List<String> list2 = new ArrayList<>();
                    list2.add(jsonObj.toString());
                    sendToDft(100, list2);
                }
                List<String> list22 = new ArrayList<>();
                list22.add(jsonObj.toString());
                sendToDft(100, list22);
            }
        }
    }

    public void requestStop(LocationManagerService.Receiver receiver, String providers) {
        if (providers != null && providers.contains("gps")) {
            if (this.mLocationManagerServiceUtil == null) {
                this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
            }
            if (this.mLocationManagerServiceUtil != null) {
                boolean requestSessionStop = false;
                int i = 1;
                if (this.mLocationManagerServiceUtil.countRealGps() == 1) {
                    requestSessionStop = true;
                }
                long stopTime = System.currentTimeMillis();
                int pendingLocks = LocationManagerServiceUtil.getReceiverLockCnt(receiver);
                String receiverHashcode = Integer.toHexString(System.identityHashCode(receiver));
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(KEY_STOP_TIME, stopTime);
                    jsonObj.put(KEY_PENDING_LOCK, pendingLocks);
                    jsonObj.put(KEY_RECEIVER_HASH, receiverHashcode);
                    if (!requestSessionStop) {
                        i = 0;
                    }
                    jsonObj.put(KEY_SESSION_STOP_FLAG, i);
                } catch (JSONException e) {
                    Log.e(TAG, "requestStop json error!");
                }
                List<String> list = new ArrayList<>();
                list.add(jsonObj.toString());
                sendToDft(101, list);
            }
        }
    }

    public void addIAwareControl(String packageName, int expectMode) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_IAWARE_PACKAGE_NAME, packageName);
            jsonObj.put(KEY_IAWARE_EXPECT_MODE, expectMode);
            jsonObj.put(KEY_IAWARE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            Log.e(TAG, "addIAwareControl json error!");
        }
        List<String> list = new ArrayList<>();
        list.add(jsonObj.toString());
        sendToDft(102, list);
    }

    public void removeIAwareControl(String packageName) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_IAWARE_PACKAGE_NAME, packageName);
            jsonObj.put(KEY_IAWARE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            Log.e(TAG, "addIAwareControl json error!");
        }
        List<String> list = new ArrayList<>();
        list.add(jsonObj.toString());
        sendToDft(103, list);
    }

    public void gpsFreeze(String pkg, int uid, boolean proxy) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_FREEZE_PACKAGE_NAME, pkg);
            jsonObj.put(KEY_FREEZE_UID, uid);
            jsonObj.put(KEY_FREEZE_OR_UNFREEZE, proxy);
            jsonObj.put(KEY_FREEZE_IS_FRONT, LocationManagerServiceUtil.isForeGroundProc(this.mContext, pkg) ? 1 : 0);
            jsonObj.put(KEY_FREEZE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            Log.e(TAG, "addIAwareControl json error!");
        }
        List<String> list = new ArrayList<>();
        list.add(jsonObj.toString());
        sendToDft(104, list);
    }

    public void idleChange(boolean isIdle, boolean isScreenOn) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_IDLE_ACTION, isIdle);
            jsonObj.put(KEY_IDLE_SCREEN_STATUS, isScreenOn);
            jsonObj.put(KEY_IDLE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            Log.e(TAG, "addIAwareControl json error!");
        }
        List<String> list = new ArrayList<>();
        list.add(jsonObj.toString());
        sendToDft(105, list);
    }

    public void logExcessReceiver(String info) {
        IMonitor.EventStream assertStream = IMonitor.openEventStream(DFT_CHIP_ASSERT_EVENT);
        if (assertStream == null) {
            Log.e(TAG, "assertStream is null");
            return;
        }
        assertStream.setParam(0, 1002);
        assertStream.setParam(1, info);
        IMonitor.sendEvent(assertStream);
        IMonitor.closeEventStream(assertStream);
    }
}
