package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.IMonitor;
import android.util.Log;
import com.android.internal.location.ProviderRequest;
import com.android.server.LocationManagerService;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.appactcontrol.AppActConstant;
import com.huawei.ncdft.HwNcDftConnManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

public class HwGpsLogServices implements IHwGpsLogServices {
    private static final int BUF_MIXSIZE = 10;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int DEFAULT_PACKAGENAME_SIZE = 5;
    private static final int DEFAULT_SIZE = 16;
    private static final int DOMAIN_GNSS = 0;
    private static final int ERROR = 1;
    private static final int EVENT_COUNT = 3;
    private static final String KEY_APK_TIME_STAMP = "apk_time_stamp";
    private static final String KEY_FREEZE_CHANGE_TIME = "freeze_time";
    private static final String KEY_FREEZE_IS_FRONT = "is_front";
    private static final String KEY_FREEZE_OR_UNFREEZE = "proxy";
    private static final String KEY_FREEZE_PACKAGE_NAME = "freeze_pkg";
    private static final String KEY_FREEZE_UID = "freeze_uid";
    private static final String KEY_IAWARE_CHANGE_TIME = "iaware_time";
    private static final String KEY_IAWARE_EXPECT_MODE = "iaware_mode";
    private static final String KEY_IAWARE_PACKAGE_NAME = "iaware_pkg";
    private static final String KEY_IDLE_ACTION = "is_idle";
    private static final String KEY_IDLE_CHANGE_TIME = "idle_time";
    private static final String KEY_IDLE_GNSS_STATUS = "idle_gnss";
    private static final String KEY_IDLE_MOTION_STATUS = "idle_motion";
    private static final String KEY_IDLE_SCREEN_STATUS = "idle_screen";
    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_IS_FREEZE = "is_freeze";
    private static final String KEY_IS_IAWARE_CONTROL = "is_iaware_control";
    private static final String KEY_IS_START = "is_start";
    private static final String KEY_IS_SUCCESS = "is_success";
    private static final String KEY_MIN_DISTANCE = "min_dis";
    private static final String KEY_PENDING_LOCK = "lock";
    private static final String KEY_PKG_NAME = "pkg";
    private static final String KEY_RECEIVER_HASH = "receiver_hash";
    private static final String KEY_SESSION_START_FLAG = "session_start";
    private static final String KEY_SESSION_STOP_FLAG = "session_stop";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_STOP_TIME = "stop_time";
    private static final int LISTENER_APK_INFO_EVENT = 910009006;
    private static final int LISTENER_DEFAULT_SIZE = 0;
    private static final int LISTENER_RECORD_ERROR = 112;
    private static final int LISTENER_RECORD_EVENT = 910000007;
    private static final long NETWORK_POS_TIMEOUT_SECOND = 11000;
    private static final int PACKAGENAME_SIZE_MAX = 500;
    private static final int PACKAGENAME_SIZE_MIN = 30;
    private static final int PACKAGENAME_SIZE_MULTIPLE = 50;
    private static final int SERVICE_NOT_AVAILABLE = -2;
    private static final int SUCCESS = 0;
    private static final String TAG = "HwGnssLogServices";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static HwGpsLogServices sHwGpsLogServices;
    private HwNcDftConnManager hwNcDftConnManager;
    private Context mContext;
    private HashMap<String, Integer> mCountHashMaps = new HashMap<>(3);
    private Handler mHwGpsLogHandler;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private boolean mNetWorkFixPending = false;
    private Timer mNlpTimer = null;
    private TimerTask mNlpTimerTask = null;
    private HashMap<String, HashMap<String, Integer>> mPackageHashMaps = new HashMap<>(5);
    private HandlerThread mThread;
    private int mTotalCount = 0;

    private HwGpsLogServices(Context context) {
        this.mContext = context;
        LBSLog.i(TAG, false, "enter HwGpsLogServices", new Object[0]);
        this.mThread = new HandlerThread("HwGpsLogServices");
        this.mThread.start();
        this.mHwGpsLogHandler = new HwGpsLogHandler(this.mThread.getLooper());
        this.hwNcDftConnManager = new HwNcDftConnManager(this.mContext);
    }

    public static synchronized HwGpsLogServices getInstance(Context context) {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            if (sHwGpsLogServices == null) {
                sHwGpsLogServices = new HwGpsLogServices(context);
            }
            hwGpsLogServices = sHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    public static synchronized HwGpsLogServices getGpsLogService() {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            hwGpsLogServices = sHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    private int sendToDft(int event, List<String> list) {
        HwNcDftConnManager hwNcDftConnManager2 = this.hwNcDftConnManager;
        if (hwNcDftConnManager2 != null) {
            hwNcDftConnManager2.reportToDft(0, event, list);
            return 0;
        }
        LBSLog.i(TAG, false, "hwNcDftConnManager is null, ignore!", new Object[0]);
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
                List<String> list = new ArrayList<>(16);
                list.add(requestInterval);
                list.add(provider);
                sendToDft(0, list);
            }
            if (this.mNetWorkFixPending) {
                LBSLog.e(TAG, false, "Network pos is already runing.", new Object[0]);
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
        TimerTask timerTask = this.mNlpTimerTask;
        if (timerTask != null) {
            timerTask.cancel();
            this.mNlpTimerTask = null;
        }
        this.mNlpTimerTask = new TimerTask() {
            /* class com.android.server.location.HwGpsLogServices.AnonymousClass1 */

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                HwGpsLogServices.this.uploadNlpStatus();
            }
        };
        try {
            this.mNlpTimer.schedule(this.mNlpTimerTask, NETWORK_POS_TIMEOUT_SECOND);
        } catch (IllegalStateException e) {
            LBSLog.e(TAG, false, " TimerTask is scheduled failed.", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadNlpStatus() {
        this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
        LocationManagerServiceUtil locationManagerServiceUtil = this.mLocationManagerServiceUtil;
        if (locationManagerServiceUtil == null) {
            LBSLog.i(TAG, false, " mLocationManagerServiceUtil == null ", new Object[0]);
            this.mNetWorkFixPending = false;
            return;
        }
        ArrayList<LocationManagerService.LocationProvider> locationProviders = locationManagerServiceUtil.getRealProviders();
        if (locationProviders == null) {
            LBSLog.w(TAG, false, " RealProviders is null ", new Object[0]);
            this.mNetWorkFixPending = false;
            return;
        }
        LocationManagerService.LocationProvider networkLocationProvider = null;
        int size = locationProviders.size();
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            LocationManagerService.LocationProvider provider = locationProviders.get(i);
            if ("network".equals(provider.getName())) {
                networkLocationProvider = provider;
                break;
            }
            i++;
        }
        if (networkLocationProvider == null) {
            LBSLog.w(TAG, false, " networkLocationProvider is null ", new Object[0]);
            this.mNetWorkFixPending = false;
            return;
        }
        Bundle extras = new Bundle();
        int status = networkLocationProvider.getStatusLocked(extras);
        LBSLog.i(TAG, false, "  network position over 11s,  NLP status:  %{public}d", Integer.valueOf(status));
        if (status > 2) {
            updateNLPStatusRecord(status);
        }
        ArrayList<Integer> statusList = null;
        try {
            statusList = extras.getIntegerArrayList("status");
        } catch (IndexOutOfBoundsException e) {
            LBSLog.e(TAG, false, "get status fail", new Object[0]);
        }
        if (statusList != null) {
            int listSize = 10;
            if (statusList.size() < 10) {
                listSize = statusList.size();
            }
            LBSLog.i(TAG, false, " list network position over 11s,  listSize:  %{public}d", Integer.valueOf(listSize));
            for (int i2 = 0; i2 < listSize; i2++) {
                updateNLPStatusRecord(statusList.get(i2).intValue());
            }
        }
        this.mNetWorkFixPending = false;
    }

    private void stopNlpTimer() {
        this.mNetWorkFixPending = false;
        LBSLog.i(TAG, false, "stopNlpTimer ", new Object[0]);
        Timer timer = this.mNlpTimer;
        if (timer != null) {
            timer.cancel();
            this.mNlpTimer.purge();
            this.mNlpTimer = null;
        }
        TimerTask timerTask = this.mNlpTimerTask;
        if (timerTask != null) {
            timerTask.cancel();
            this.mNlpTimerTask = null;
        }
    }

    public void openGpsSwitchFail(int open) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(open));
        sendToDft(14, list);
    }

    public void initGps(boolean isEnable, byte engineCapabilities) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(isEnable));
        list.add(Integer.toString(engineCapabilities));
        sendToDft(1, list);
    }

    public void updateXtraDloadStatus(boolean status) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(10, list);
    }

    public void updateNtpDloadStatus(boolean status) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(11, list);
    }

    public void updateSetPosMode(boolean status, int interval) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(status));
        list.add(Integer.toString(interval));
        sendToDft(12, list);
    }

    public void updateSetPosMode(boolean status) {
        LBSLog.i(TAG, false, "updateSetPosMode = %{public}b", Boolean.valueOf(status));
    }

    public void updateApkName(String provider, String hashCode, String name, String apkRunMode, String apkTimeStamp) {
        if (this.hwNcDftConnManager == null) {
            return;
        }
        if (provider.equalsIgnoreCase("gps") || provider.equalsIgnoreCase("network") || provider.equalsIgnoreCase("APKSTOPPROVIDER")) {
            List<String> list = new ArrayList<>(16);
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
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(29, list);
    }

    public void startGps(boolean isEnable, int positionMode) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(isEnable));
        list.add(Integer.toString(positionMode));
        sendToDft(2, list);
    }

    public void updateNetworkState(NetworkInfo info) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("info", info);
        LBSLog.i(TAG, false, "updateNetworkState ret:  %{public}d", Integer.valueOf(sendToDft(3, bundle)));
    }

    public void updateAgpsState(int type, int state) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(type));
        list.add(Integer.toString(state));
        sendToDft(4, list);
    }

    public void stopGps(boolean status) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(status));
        sendToDft(5, list);
    }

    public void permissionErr(String packageName) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(packageName);
        sendToDft(13, list);
    }

    public void addGeofenceStatus() {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add("0");
        sendToDft(15, list);
    }

    public void addBatchingStatus() {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add("0");
        sendToDft(16, list);
    }

    public void updateGpsRunState(int status) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(status));
        sendToDft(6, list);
    }

    public void updateLocation(Location location, long time, String provider) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", location);
        bundle.putLong("time", time);
        bundle.putString("provider", provider);
        LBSLog.i(TAG, false, "updateLocation ret:  %{public}d", Integer.valueOf(sendToDft(8, bundle)));
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        Bundle bundle = new Bundle();
        bundle.putInt("svCount", svCount);
        bundle.putIntArray("svs", svs);
        bundle.putFloatArray("snrs", snrs);
        bundle.putFloatArray("svElevations", svElevations);
        bundle.putFloatArray("svAzimuths", svAzimuths);
        LBSLog.i(TAG, false, "updateSvStatus ret:  %{public}d", Integer.valueOf(sendToDft(9, bundle)));
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(String.valueOf(currentNtpTime));
        list.add(String.valueOf(realTime));
        sendToDft(19, list);
    }

    public void reportBinderError() {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add("0");
        sendToDft(20, list);
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        LBSLog.i(TAG, false, "driver err is %{public}s", strJsonExceptionBody);
    }

    public void updateNtpServerInfo(String address) {
        if (address != null) {
            List<String> list = new ArrayList<>(16);
            list.clear();
            list.add(address);
            sendToDft(23, list);
        }
    }

    public void injectExtraParam(String extraParam) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(extraParam);
        sendToDft(24, list);
    }

    public void injectTimeParam(int timeSource, long ntpTime, int uncertainty) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(timeSource));
        list.add(String.valueOf(ntpTime));
        list.add(Integer.toString(uncertainty));
        sendToDft(27, list);
    }

    public int logEvent(int type, int event, String parameter) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(type));
        list.add(Integer.toString(event));
        list.add(parameter);
        int result = sendToDft(25, list);
        if (result != 0) {
            LBSLog.i(TAG, false, "send Higeo CHR msg error:%{public}d,%{public}d, %{public}s", Integer.valueOf(type), Integer.valueOf(event), parameter);
        }
        return result;
    }

    public void setLocationSettingsOffErr(String provider) {
        List<String> list = new ArrayList<>(16);
        list.add(provider);
        sendToDft(28, list);
    }

    public void setQuickGpsParam(int id, String param) {
        List<String> list = new ArrayList<>(16);
        list.clear();
        list.add(Integer.toString(id));
        list.add(param);
        sendToDft(30, list);
    }

    public void requestStart(LocationRequest request, LocationManagerService.Receiver receiver, String packageName, boolean isIAwareControl) {
        if ("gps".equals(request.getProvider())) {
            if (this.mLocationManagerServiceUtil == null) {
                this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
            }
            LocationManagerServiceUtil locationManagerServiceUtil = this.mLocationManagerServiceUtil;
            if (locationManagerServiceUtil != null) {
                boolean requestSessionStart = false;
                if (locationManagerServiceUtil.countRealGps() == 0) {
                    requestSessionStart = true;
                }
                long startTime = System.currentTimeMillis();
                long interval = request.getInterval();
                float minDistance = request.getSmallestDisplacement();
                boolean isFreeze = GpsFreezeProc.getInstance().isFreeze(packageName);
                String receiverHashcode = Integer.toHexString(System.identityHashCode(receiver));
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("pkg", packageName);
                    jsonObj.put(KEY_RECEIVER_HASH, receiverHashcode);
                    jsonObj.put(KEY_START_TIME, startTime);
                    jsonObj.put(KEY_INTERVAL, interval);
                    jsonObj.put(KEY_MIN_DISTANCE, (double) minDistance);
                    int i = 1;
                    jsonObj.put(KEY_IS_FREEZE, isFreeze ? 1 : 0);
                    if (isFreeze) {
                        jsonObj.put(KEY_FREEZE_IS_FRONT, LocationManagerServiceUtil.isForeGroundProc(this.mContext, packageName) ? 1 : 0);
                    }
                    jsonObj.put(KEY_IS_IAWARE_CONTROL, isIAwareControl ? 1 : 0);
                    if (!requestSessionStart) {
                        i = 0;
                    }
                    jsonObj.put(KEY_SESSION_START_FLAG, i);
                } catch (JSONException e) {
                    LBSLog.e(TAG, false, "requestStart json error!", new Object[0]);
                }
                List<String> list = new ArrayList<>(16);
                list.add(jsonObj.toString());
                sendToDft(100, list);
            }
        }
    }

    public void requestStop(LocationManagerService.Receiver receiver, String providers) {
        if (providers != null && providers.contains("gps")) {
            if (this.mLocationManagerServiceUtil == null) {
                this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
            }
            LocationManagerServiceUtil locationManagerServiceUtil = this.mLocationManagerServiceUtil;
            if (locationManagerServiceUtil != null) {
                boolean requestSessionStop = false;
                int i = 1;
                if (locationManagerServiceUtil.countRealGps() == 1) {
                    requestSessionStop = true;
                }
                long stopTime = System.currentTimeMillis();
                int pendingLocks = LocationManagerServiceUtil.getReceiverLockCnt(receiver);
                String receiverHashcode = Integer.toHexString(System.identityHashCode(receiver));
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(KEY_RECEIVER_HASH, receiverHashcode);
                    jsonObj.put(KEY_STOP_TIME, stopTime);
                    jsonObj.put(KEY_PENDING_LOCK, pendingLocks);
                    if (!requestSessionStop) {
                        i = 0;
                    }
                    jsonObj.put(KEY_SESSION_STOP_FLAG, i);
                } catch (JSONException e) {
                    LBSLog.e(TAG, false, "requestStop json error!", new Object[0]);
                }
                List<String> list = new ArrayList<>(16);
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
            LBSLog.e(TAG, false, "addIAwareControl json error!", new Object[0]);
        }
        List<String> list = new ArrayList<>(16);
        list.add(jsonObj.toString());
        sendToDft(102, list);
    }

    public void removeIAwareControl(String packageName) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_IAWARE_PACKAGE_NAME, packageName);
            jsonObj.put(KEY_IAWARE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            LBSLog.e(TAG, false, "addIAwareControl json error!", new Object[0]);
        }
        List<String> list = new ArrayList<>(16);
        list.add(jsonObj.toString());
        sendToDft(103, list);
    }

    public void gpsFreeze(String packageName, int uid, boolean proxy) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_FREEZE_PACKAGE_NAME, packageName);
            jsonObj.put(KEY_FREEZE_UID, uid);
            int i = 1;
            jsonObj.put(KEY_FREEZE_OR_UNFREEZE, proxy ? 1 : 0);
            if (!LocationManagerServiceUtil.isForeGroundProc(this.mContext, packageName)) {
                i = 0;
            }
            jsonObj.put(KEY_FREEZE_IS_FRONT, i);
            jsonObj.put(KEY_FREEZE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            LBSLog.e(TAG, false, "addIAwareControl json error!", new Object[0]);
        }
        List<String> list = new ArrayList<>(16);
        list.add(jsonObj.toString());
        sendToDft(104, list);
    }

    public void idleChange(boolean isIdle, boolean isScreenOn) {
        JSONObject jsonObj = new JSONObject();
        int i = 1;
        try {
            jsonObj.put(KEY_IDLE_ACTION, isIdle ? 1 : 0);
            if (!isScreenOn) {
                i = 0;
            }
            jsonObj.put(KEY_IDLE_SCREEN_STATUS, i);
            jsonObj.put(KEY_IDLE_MOTION_STATUS, -2);
            jsonObj.put(KEY_IDLE_CHANGE_TIME, System.currentTimeMillis());
        } catch (JSONException e) {
            LBSLog.e(TAG, false, "addIAwareControl json error!", new Object[0]);
        }
        List<String> list = new ArrayList<>(16);
        list.add(jsonObj.toString());
        sendToDft(105, list);
    }

    public void updateApkNetworkRequest(String providers, LocationManagerService.Receiver receiver, boolean isRequestStart) {
        if (this.hwNcDftConnManager != null) {
            if (this.mLocationManagerServiceUtil == null) {
                this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
            }
            LocationManagerServiceUtil locationManagerServiceUtil = this.mLocationManagerServiceUtil;
            if (locationManagerServiceUtil != null) {
                if (isRequestStart) {
                    if (providers == null || !"network".equals(providers)) {
                        return;
                    }
                } else if (!locationManagerServiceUtil.isReceiverHasNetworkProvider(receiver)) {
                    return;
                }
                boolean isSuccess = false;
                String hashCode = Integer.toHexString(System.identityHashCode(receiver));
                String packageName = this.mLocationManagerServiceUtil.getReceiverPackageName(receiver);
                if (!isRequestStart) {
                    isSuccess = this.mLocationManagerServiceUtil.isNetworkLocationSuccess(receiver);
                }
                long apkTimeStamp = System.currentTimeMillis();
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(KEY_RECEIVER_HASH, hashCode);
                    jsonObj.put("pkg", packageName);
                    jsonObj.put(KEY_IS_START, isRequestStart);
                    jsonObj.put(KEY_APK_TIME_STAMP, apkTimeStamp);
                    jsonObj.put(KEY_IS_SUCCESS, isSuccess);
                } catch (JSONException e) {
                    LBSLog.e(TAG, false, "updateApkNetworkRequest json error!", new Object[0]);
                }
                List<String> list = new ArrayList<>(16);
                list.add(jsonObj.toString());
                sendToDft(106, list);
            }
        }
    }

    public void recordGnssStatusStatistics(int eventId, int times, String packageName, Bundle extra) {
        Message msg = Message.obtain();
        msg.what = eventId;
        if (extra == null) {
            extra = new Bundle();
        }
        extra.putString(AppActConstant.ATTR_PACKAGE_NAME, packageName);
        msg.obj = extra;
        msg.arg1 = times;
        this.mHwGpsLogHandler.sendMessage(msg);
    }

    class HwGpsLogHandler extends Handler {
        HwGpsLogHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Bundle bundle = null;
                if (msg.obj instanceof Bundle) {
                    bundle = (Bundle) msg.obj;
                }
                if (bundle != null) {
                    HwGpsLogServices.this.handlerStatistics(msg.arg1, bundle);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerStatistics(int operationType, Bundle bundle) {
        HashMap<String, Integer> packageHashMaps;
        int times;
        String providerType = bundle.getString("providerType");
        int totalCount = 0;
        if (this.mCountHashMaps.containsKey(providerType)) {
            totalCount = this.mCountHashMaps.get(providerType).intValue();
        }
        int size = bundle.getInt("size", 0);
        if (size != totalCount) {
            String packageName = bundle.getString(AppActConstant.ATTR_PACKAGE_NAME);
            if (!"".equals(packageName)) {
                if (this.mPackageHashMaps.containsKey(providerType)) {
                    packageHashMaps = this.mPackageHashMaps.get(providerType);
                } else {
                    packageHashMaps = new HashMap<>(5);
                }
                if (packageHashMaps != null) {
                    int times2 = 0;
                    if (packageHashMaps.containsKey(packageName)) {
                        times2 = packageHashMaps.get(packageName).intValue();
                    }
                    LBSLog.i(TAG, false, "handlerStatistics:%{public}s, %{public}d", packageName, Integer.valueOf(operationType));
                    if (operationType == 1) {
                        times = times2 + 1;
                    } else if (operationType == 0) {
                        times = times2 - 1;
                    } else {
                        LBSLog.e(TAG, false, "current operation type is error", new Object[0]);
                        return;
                    }
                    if (times <= 0) {
                        packageHashMaps.remove(packageName);
                    } else {
                        packageHashMaps.put(packageName, Integer.valueOf(times));
                    }
                    this.mPackageHashMaps.put(providerType, packageHashMaps);
                    this.mCountHashMaps.put(providerType, Integer.valueOf(size));
                    handlerUpload(providerType);
                }
            }
        }
    }

    private void handlerUpload(String providerType) {
        if (isUpload(providerType)) {
            LBSLog.i(TAG, false, "handlerUpload", new Object[0]);
            HashMap<String, Integer> uploadHashMaps = getUploadHashMaps(providerType);
            if (!(uploadHashMaps == null || uploadHashMaps.size() == 0)) {
                IMonitor.EventStream gnssErrorStream = IMonitor.openEventStream((int) LISTENER_RECORD_EVENT);
                if (gnssErrorStream == null) {
                    LBSLog.e(TAG, false, "gnssErrorStream is null", new Object[0]);
                    return;
                }
                gnssErrorStream.setParam(1, 112);
                for (String key : uploadHashMaps.keySet()) {
                    IMonitor.EventStream apkInfoStream = IMonitor.openEventStream((int) LISTENER_APK_INFO_EVENT);
                    if (apkInfoStream == null) {
                        LBSLog.e(TAG, false, "apkInfoStream is null", new Object[0]);
                        return;
                    }
                    apkInfoStream.setParam(0, key);
                    apkInfoStream.setParam(1, providerType);
                    apkInfoStream.setParam(2, uploadHashMaps.get(key).intValue());
                    gnssErrorStream.fillArrayParam(3, apkInfoStream);
                    IMonitor.closeEventStream(apkInfoStream);
                }
                int totalCount = 0;
                if (this.mCountHashMaps.containsKey(providerType)) {
                    totalCount = this.mCountHashMaps.get(providerType).intValue();
                }
                gnssErrorStream.setParam(2, totalCount);
                IMonitor.sendEvent(gnssErrorStream);
                IMonitor.closeEventStream(gnssErrorStream);
            }
        }
    }

    private HashMap<String, Integer> getUploadHashMaps(String providerType) {
        HashMap<String, Integer> packageHashMaps = null;
        if (this.mPackageHashMaps.containsKey(providerType)) {
            packageHashMaps = this.mPackageHashMaps.get(providerType);
            if (packageHashMaps.size() > 5) {
                return getTopHashMaps(packageHashMaps);
            }
        }
        return packageHashMaps;
    }

    private HashMap<String, Integer> getTopHashMaps(HashMap<String, Integer> hashMaps) {
        String packageName = "";
        int maxCount = 0;
        for (String key : hashMaps.keySet()) {
            int value = hashMaps.get(key).intValue();
            if (value > maxCount) {
                maxCount = value;
                packageName = key;
            }
        }
        HashMap<String, Integer> topHashMaps = new HashMap<>(16);
        topHashMaps.put(packageName, Integer.valueOf(maxCount));
        return topHashMaps;
    }

    private boolean isUpload(String providerType) {
        int totalCount = 0;
        if (this.mCountHashMaps.containsKey(providerType)) {
            totalCount = this.mCountHashMaps.get(providerType).intValue();
        }
        if (totalCount == 30) {
            return true;
        }
        if (totalCount < 50 || totalCount > 500 || totalCount % 50 != 0) {
            return false;
        }
        return true;
    }

    private int sendToDft(int event, Bundle data) {
        HwNcDftConnManager hwNcDftConnManager2 = this.hwNcDftConnManager;
        if (hwNcDftConnManager2 != null) {
            return hwNcDftConnManager2.reportToDft(0, event, data);
        }
        LBSLog.i(TAG, false, "sendToDft, hwNcDftConnManager is null, ignore!", new Object[0]);
        return 1;
    }
}
