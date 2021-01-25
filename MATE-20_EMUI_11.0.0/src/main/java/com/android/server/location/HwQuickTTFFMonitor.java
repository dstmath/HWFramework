package com.android.server.location;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.ArraySet;
import com.android.server.HwServiceFactory;
import com.android.server.LocationManagerService;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import libcore.io.IoUtils;

public class HwQuickTTFFMonitor implements IHwQuickTTFFMonitor {
    private static final float ACC_IN_WHITE_LIST = 50.0f;
    private static final long BLACKLIST_READ_INTERVAL = 600000;
    private static final int CN0S = 30;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_SIZE = 16;
    private static final int FIRST_SV = 0;
    private static final boolean IS_DEBUG_VERSION;
    private static final int LIST_TYPE_DISABLE = 4;
    private static final int LIST_TYPE_WHITE = 3;
    private static final int LOCATION_HAS_HWQUICKFIX = 256;
    private static final Object LOCK = new Object();
    private static final int METER_PER_SECOND = -700;
    private static final int QUICK_ENABLE_ONLY = 2;
    private static final float QUICK_FAKE_CARRIER_FREQUENCY = 1.56109798E9f;
    private static final String QUICK_GPS = "quickgps";
    private static final int QUICK_GPS_LOCATION = 2;
    private static final int QUICK_GPS_REQUEST_START = 1;
    private static final int QUICK_GPS_REQUEST_STOP = 3;
    private static final int QUICK_GPS_SESSION_START = 0;
    private static final int QUICK_GPS_SESSION_STOP = 4;
    private static final int QUICK_IS_ENABLE = 1;
    private static final int QUICK_LOCATION_START = 1;
    private static final int QUICK_LOCATION_STOP = 0;
    private static final int QUICK_REQUEST_START = 1;
    private static final int QUICK_REQUEST_STOP = 0;
    private static final String QUICK_START = "request_quick_ttff";
    private static final String QUICK_STOP = "stop_quick_ttff";
    private static final int QUICK_TTFF = 128;
    private static final String REQUEST_QUICK_TTFF = "request_quick_ttff";
    private static final int STATE_QTTFF = 17;
    private static final String STOP_QUICK_TTFF = "stop_quick_ttff";
    private static final int STOP_TIME = 50;
    private static final int SVID = 24;
    private static final int SVID_WITH_FLAGS = 24;
    private static final int SV_FAKE_DATA = 1;
    private static final int SV_FAKE_DATA_ZERO = 0;
    private static final String TAG = "HwQuickTTFFMonitor-V2018.8.27";
    private static final String VERSION = "V2018.8.27";
    private static final int WAIT_TIME = 30000;
    private static GnssClock sGnssClock;
    private static GnssMeasurement sGnssMeasurement;
    private static GnssMeasurementsEvent sGnssMeasurementsEvent;
    private static HwQuickTTFFMonitor sHwQuickTTFFMonitorManager;
    private long disableListUpdateTimetamp = 0;
    private boolean isRunning = false;
    private final Object listLock = new Object();
    private Looper looper;
    private ArraySet<String> mAccWhiteLists = new ArraySet<>();
    private HashMap<String, String> mAppMonitorMap;
    private Context mContext;
    private String mDebugPropertiesFile;
    private String mEnableQuickttff;
    private GnssLocationProvider mGnssProvider;
    private Handler mHandler;
    private HwLbsConfigManager mHwLbsConfigManager;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    private boolean mIsNavigating = false;
    private boolean mIsPermission = true;
    private boolean mIsQuickLocation = false;
    private boolean mIsQuickTtffEnable = false;
    private boolean mIsReceive = false;
    private volatile boolean mIsSatisfiedRequest = false;
    private boolean mIsSendStartCommand = false;
    private Properties mProperties;
    private Timer mQuickTtfftimer;
    private ArraySet<String> mQuickttffDisableLists = new ArraySet<>();
    private int mQuickttffEnableState;
    private ConcurrentHashMap<String, Long> mRemoveTimeMap;
    private GpsLocationProviderUtils mUtils;
    private List<String> powerQuickttffDisableLists = new ArrayList(16);

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    private HwQuickTTFFMonitor(Context context, GnssLocationProvider gnssLocationProvider) {
        this.mContext = context;
        this.mGnssProvider = gnssLocationProvider;
        this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
        this.mHwLbsConfigManager = HwLbsConfigManager.getInstance(context);
        startMonitor();
        this.mUtils = EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);
        GpsLocationProviderUtils gpsLocationProviderUtils = this.mUtils;
        if (gpsLocationProviderUtils != null) {
            this.looper = gpsLocationProviderUtils.getGnssLooper(this.mGnssProvider);
        }
        Looper looper2 = this.looper;
        if (looper2 != null) {
            this.mHandler = new QuickHandler(looper2);
        }
    }

    public static HwQuickTTFFMonitor getInstance(Context context, GnssLocationProvider provider) {
        synchronized (LOCK) {
            if (sHwQuickTTFFMonitorManager == null) {
                LBSLog.i(TAG, false, "sHwQuickTTFFMonitorManager create.", new Object[0]);
                sHwQuickTTFFMonitorManager = new HwQuickTTFFMonitor(context, provider);
            }
        }
        return sHwQuickTTFFMonitorManager;
    }

    public static HwQuickTTFFMonitor getMonitor() {
        return sHwQuickTTFFMonitorManager;
    }

    private void startMonitor() {
        quickTtffEnable(configPath());
        if (!this.mIsQuickTtffEnable) {
            LBSLog.e(TAG, false, "QuickTTFF is not enable %{public}s", this.mEnableQuickttff);
            return;
        }
        LBSLog.i(TAG, false, "startMonitor", new Object[0]);
        this.mAppMonitorMap = new HashMap<>(16);
        this.mRemoveTimeMap = new ConcurrentHashMap<>(16);
        this.isRunning = false;
        this.mIsSendStartCommand = false;
    }

    public void requestHwQuickTTFF(LocationRequest request, String packageName, String requestProvider, String id) {
        String requestAccuracy;
        Throwable th;
        if (this.mIsQuickTtffEnable) {
            if ("gps".equals(request.getProvider())) {
                if ("gps".equals(requestProvider)) {
                    if (this.mIsPermission) {
                        boolean isQuickttffDisable = !this.mQuickttffDisableLists.contains(packageName);
                        if (this.mAccWhiteLists.contains(packageName)) {
                            requestAccuracy = String.valueOf((float) ACC_IN_WHITE_LIST);
                        } else {
                            requestAccuracy = "null";
                        }
                        this.mHwLocationGpsLogServices.setQuickGpsParam(1, packageName + "," + isQuickttffDisable + "," + requestAccuracy);
                        if (isQuickttffDisableListApp(packageName)) {
                            LBSLog.i(TAG, false, "%{public}s is in black list can not start Quickttff", packageName);
                            return;
                        }
                        if (this.mRemoveTimeMap.size() > 0) {
                            for (Map.Entry<String, Long> entry : this.mRemoveTimeMap.entrySet()) {
                                String packageNameRemove = entry.getKey();
                                if (System.currentTimeMillis() - entry.getValue().longValue() > 2000) {
                                    this.mRemoveTimeMap.remove(packageNameRemove);
                                }
                            }
                            if (this.mRemoveTimeMap.containsKey(packageName)) {
                                LBSLog.i(TAG, false, "%{public}s can not start quickttff in 2s", packageName);
                                return;
                            }
                        }
                        synchronized (LOCK) {
                            try {
                                try {
                                    this.mAppMonitorMap.put(id, packageName);
                                    LBSLog.i(TAG, false, "requestHwQuickTTFF:%{public}s", packageName);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                        this.mIsSatisfiedRequest = true;
                        if (!this.isRunning && this.mIsNavigating) {
                            LBSLog.i(TAG, false, "start HwQuickTTFF when isNavigating", new Object[0]);
                            sendStartCommand();
                        }
                    }
                }
            }
        }
    }

    public void removeHwQuickTTFF(String packageName, String id, boolean isGps) {
        if (this.mIsQuickTtffEnable) {
            if (isGps) {
                this.mRemoveTimeMap.put(packageName, Long.valueOf(System.currentTimeMillis()));
            }
            synchronized (LOCK) {
                if (this.mAppMonitorMap.containsKey(id)) {
                    this.mAppMonitorMap.remove(id);
                    this.mHwLocationGpsLogServices.setQuickGpsParam(3, packageName);
                    LBSLog.i(TAG, false, "removeHwQuickTTFF:%{public}s", packageName);
                    if (this.mAppMonitorMap.size() <= 0) {
                        if (this.isRunning) {
                            LBSLog.i(TAG, false, "removeHwQuickTTFF HwQuickTTFF STOP", new Object[0]);
                            sendStopCommand();
                        }
                    }
                }
            }
        }
    }

    private void removeAllHwQuickTtff(String provider) {
        if (this.mIsQuickTtffEnable && !this.mIsQuickLocation && "gps".equals(provider)) {
            synchronized (LOCK) {
                if (this.isRunning) {
                    this.isRunning = false;
                    this.mIsSendStartCommand = false;
                } else {
                    return;
                }
            }
            LBSLog.i(TAG, false, "removeAllHwQuickTTFF HwQuickTTFF STOP", new Object[0]);
            if (this.mAppMonitorMap.size() > 0) {
                for (Map.Entry<String, String> entry : this.mAppMonitorMap.entrySet()) {
                    this.mRemoveTimeMap.put(entry.getValue(), Long.valueOf(System.currentTimeMillis()));
                }
            }
            this.mAppMonitorMap.clear();
            sendStopCommand();
        }
    }

    public boolean isLocationReportToApp(String packageName, String provider, Location location) {
        boolean isReportLocation = false;
        float acc = location.getAccuracy();
        if (!this.mIsQuickLocation || !"gps".equals(provider)) {
            return true;
        }
        if (this.mAppMonitorMap.containsValue(packageName)) {
            if (isAccSatisfied(packageName, acc)) {
                isReportLocation = true;
            } else {
                isReportLocation = false;
            }
        }
        LBSLog.i(TAG, false, "isReportQuickTTFFLocation:%{public}b, pkgname=%{public}s", Boolean.valueOf(isReportLocation), packageName);
        return isReportLocation;
    }

    private void setQuickTtffLocation(Location location, boolean isPassive) {
        if (this.mIsQuickTtffEnable && !isPassive) {
            if ((QUICK_GPS.equals(location.getProvider()) || "gps".equals(location.getProvider())) && this.isRunning) {
                this.mHwLocationGpsLogServices.setQuickGpsParam(2, String.valueOf(location.getAccuracy()));
            }
            if (QUICK_GPS.equals(location.getProvider())) {
                this.mIsQuickLocation = true;
                location.setProvider("gps");
                Bundle extras = location.getExtras();
                if (extras == null) {
                    extras = new Bundle();
                    location.setExtras(extras);
                }
                LBSLog.i(TAG, false, "SourceType %{public}d", Integer.valueOf(location.getExtras().getInt("SourceType")));
                extras.putBoolean("QUICKGPS", true);
                location.setExtras(extras);
                LBSLog.i(TAG, false, "is a QuickTTFFLocation", new Object[0]);
                if (!this.mIsReceive) {
                    this.mIsReceive = true;
                    LBSLog.i(TAG, false, "have receive QuickTTFFLocation", new Object[0]);
                    cancelLocalTimerTask();
                }
            } else if ("gps".equals(location.getProvider())) {
                this.mIsQuickLocation = false;
            } else {
                LBSLog.d(TAG, false, "setQuickTtffLocation", new Object[0]);
            }
        }
    }

    public boolean isQuickLocation(Location location) {
        int sourceType = 0;
        if (location.getExtras() != null) {
            sourceType = location.getExtras().getInt("SourceType");
        }
        if ((sourceType & 128) == 128) {
            return true;
        }
        return false;
    }

    private boolean isRunning() {
        return this.isRunning;
    }

    public void setPermission(boolean hasPermission) {
        this.mIsPermission = hasPermission;
    }

    private void setNavigating(boolean isNavigating) {
        this.mIsNavigating = isNavigating;
    }

    private boolean isQuickttffDisableListApp(String appName) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.disableListUpdateTimetamp > 600000) {
            this.disableListUpdateTimetamp = currentTime;
            updateBlackList(new ArrayList(16));
        }
        if (this.mQuickttffDisableLists.contains(appName)) {
            LBSLog.i(TAG, false, "%{public}s is not WhiteApp", appName);
            return true;
        }
        LBSLog.i(TAG, false, "%{public}s is WhiteApp", appName);
        return false;
    }

    private void updateBlackList(List<String> whiteList) {
        synchronized (this.listLock) {
            this.mQuickttffDisableLists.clear();
            this.mQuickttffDisableLists.addAll(this.mHwLbsConfigManager.getListForFeature(LbsConfigContent.CONFIG_QTTFF_DISABLE_BLACKLIST));
            if (whiteList != null && whiteList.size() > 0) {
                this.powerQuickttffDisableLists.clear();
                this.powerQuickttffDisableLists.addAll(whiteList);
            }
            for (String packageName : this.powerQuickttffDisableLists) {
                if (!this.mQuickttffDisableLists.contains(packageName)) {
                    this.mQuickttffDisableLists.add(packageName);
                }
            }
            LBSLog.i(TAG, false, "updateDisableList quickttffDisableList %{public}s", this.powerQuickttffDisableLists);
        }
    }

    private boolean isAccSatisfied(String appName, float acc) {
        if (!this.mAccWhiteLists.contains(appName)) {
            return true;
        }
        LBSLog.i(TAG, false, "%{public}s is inAccList ", appName);
        if (acc <= ACC_IN_WHITE_LIST) {
            LBSLog.i(TAG, false, "Satisfied with Acc %{public}d >= %{public}d ", Float.valueOf((float) ACC_IN_WHITE_LIST), Float.valueOf(acc));
            return true;
        }
        LBSLog.i(TAG, false, " Not Satisfied with Acc %{public}d < %{public}d", Float.valueOf((float) ACC_IN_WHITE_LIST), Float.valueOf(acc));
        return false;
    }

    public void updateWhiteList(int type, List<String> whiteList) {
        if (type == 3) {
            this.mAccWhiteLists.clear();
            this.mAccWhiteLists.addAll(whiteList);
            LBSLog.i(TAG, false, "updateAccWhiteList accWhiteList %{public}s", whiteList);
        } else if (type == 4) {
            updateBlackList(whiteList);
        } else {
            LBSLog.d(TAG, false, "updateWhiteList", new Object[0]);
        }
    }

    private void quickTtffEnable(String filename) {
        boolean isLocalQuickTtffEnable;
        this.mProperties = new Properties();
        try {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(new File(filename));
                this.mProperties.load(stream);
            } finally {
                IoUtils.closeQuietly(stream);
            }
        } catch (IOException e) {
            LBSLog.i(TAG, false, "Could not open higeo configuration file %{private}s", filename);
        }
        this.mEnableQuickttff = this.mProperties.getProperty("higeo_enable_quickttff");
        String str = this.mEnableQuickttff;
        if (str != null && !"".equals(str)) {
            try {
                this.mQuickttffEnableState = Integer.parseInt(this.mEnableQuickttff);
            } catch (NumberFormatException e2) {
                LBSLog.e(TAG, false, "unable to parse Enable_Quickttff: %{public}s", this.mEnableQuickttff);
            }
        }
        int i = this.mQuickttffEnableState;
        if (i == 1) {
            isLocalQuickTtffEnable = true;
            LBSLog.e(TAG, false, " QuickTTFF is enable %{public}s", this.mEnableQuickttff);
        } else if (i == 2) {
            isLocalQuickTtffEnable = true;
            LBSLog.e(TAG, false, " QuickTTFF only is enable %{public}s", this.mEnableQuickttff);
        } else {
            isLocalQuickTtffEnable = false;
            LBSLog.e(TAG, false, " QuickTTFF is not enable %{public}s", this.mEnableQuickttff);
        }
        if (this.mHwLbsConfigManager.isParamAlreadySetup(LbsConfigContent.CONFIG_QTTFF_ENABLE)) {
            this.mIsQuickTtffEnable = this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_QTTFF_ENABLE);
        } else {
            this.mIsQuickTtffEnable = isLocalQuickTtffEnable;
        }
    }

    private boolean isBetaUser() {
        return IS_DEBUG_VERSION;
    }

    private String configPath() {
        if (isBetaUser()) {
            LBSLog.i(TAG, false, "This is beta user", new Object[0]);
            File file = HwCfgFilePolicy.getCfgFile("xml/higeo_beta.conf", 0);
            if (file != null) {
                this.mDebugPropertiesFile = file.getPath();
                LBSLog.i(TAG, false, "configPath is %{private}s", this.mDebugPropertiesFile);
            } else {
                LBSLog.i(TAG, false, "configPath is not /cust_spec/xml/  ", new Object[0]);
                this.mDebugPropertiesFile = "/odm/etc/higeo_beta.conf";
            }
        } else {
            LBSLog.i(TAG, false, "This is not beta user", new Object[0]);
            File file2 = HwCfgFilePolicy.getCfgFile("xml/higeo.conf", 0);
            if (file2 != null) {
                this.mDebugPropertiesFile = file2.getPath();
                LBSLog.i(TAG, false, "configPath is %{private}s", this.mDebugPropertiesFile);
            } else {
                LBSLog.i(TAG, false, "configPath is not /cust_spec/xml/  ", new Object[0]);
                this.mDebugPropertiesFile = "/odm/etc/higeo.conf";
            }
        }
        LBSLog.i(TAG, false, "configPath is %{private}s", this.mDebugPropertiesFile);
        return this.mDebugPropertiesFile;
    }

    private boolean isReport(Location location) {
        if (this.isRunning || !QUICK_GPS.equals(location.getProvider())) {
            return true;
        }
        return false;
    }

    private void clearAppMonitorMap() {
        this.mAppMonitorMap.clear();
        LBSLog.i(TAG, false, "clearAppMonitorMap", new Object[0]);
    }

    private void sendStartCommand() {
        if (this.isRunning) {
            LBSLog.i(TAG, false, "HwQuickTTFF has be started before.", new Object[0]);
        } else if (!this.mIsSendStartCommand && this.mIsSatisfiedRequest) {
            this.mIsReceive = false;
            timerTask();
            this.isRunning = true;
            this.mIsSendStartCommand = true;
            LBSLog.i(TAG, false, "HwQuickTTFF Start", new Object[0]);
            this.mHwLocationGpsLogServices.setQuickGpsParam(0, "request_quick_ttff");
            sendMessage(1);
        }
    }

    private void sendMessage(int message) {
        Handler handler = this.mHandler;
        if (handler == null) {
            LBSLog.i(TAG, false, "Handler is null", new Object[0]);
        } else {
            handler.obtainMessage(message).sendToTarget();
        }
    }

    private final class QuickHandler extends Handler {
        public QuickHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int message = msg.what;
            if (message == 0) {
                HwQuickTTFFMonitor.this.sendTtffQuickCommand("stop_quick_ttff");
            } else if (message == 1) {
                HwQuickTTFFMonitor.this.sendTtffQuickCommand("request_quick_ttff");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendTtffQuickCommand(String quickCommand) {
        long identity = Binder.clearCallingIdentity();
        LocationManagerServiceUtil locationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
        if (locationManagerServiceUtil == null) {
            LBSLog.i(TAG, false, "locationManagerServiceUtil is null", new Object[0]);
            return;
        }
        try {
            if ("request_quick_ttff".equals(quickCommand)) {
                locationManagerServiceUtil.handleQuickLocation(1);
            } else if ("stop_quick_ttff".equals(quickCommand)) {
                locationManagerServiceUtil.handleQuickLocation(0);
            } else {
                LBSLog.w(TAG, false, "sendExtraCommand: unknown command " + quickCommand, new Object[0]);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendStopCommand() {
        LBSLog.i(TAG, false, "HwQuickTTFF STOP", new Object[0]);
        this.mHwLocationGpsLogServices.setQuickGpsParam(4, "ALL");
        sendMessage(0);
        cancelLocalTimerTask();
        this.mIsSendStartCommand = false;
        this.mIsQuickLocation = false;
        this.mIsReceive = false;
        this.mIsSatisfiedRequest = false;
        this.isRunning = false;
    }

    private synchronized void timerTask() {
        this.mQuickTtfftimer = new Timer();
        this.mQuickTtfftimer.schedule(new LocalTimerTask(), HwArbitrationDEFS.DelayTimeMillisA);
    }

    private GnssMeasurementsEvent getGnssMeasurements() {
        LBSLog.i(TAG, false, "quickgps event start. ", new Object[0]);
        sGnssMeasurement = new GnssMeasurement();
        sGnssClock = new GnssClock();
        sGnssMeasurement.setSvid(24);
        sGnssMeasurement.setConstellationType(1);
        sGnssMeasurement.setReceivedSvTimeNanos(1);
        sGnssMeasurement.setReceivedSvTimeUncertaintyNanos(1);
        sGnssMeasurement.setTimeOffsetNanos(0.0d);
        sGnssMeasurement.setCn0DbHz(30.0d);
        sGnssMeasurement.setPseudorangeRateMetersPerSecond(-700.0d);
        sGnssMeasurement.setPseudorangeRateUncertaintyMetersPerSecond(1.0d);
        sGnssMeasurement.setAccumulatedDeltaRangeState(1);
        sGnssMeasurement.setAccumulatedDeltaRangeMeters(1.0d);
        sGnssMeasurement.setAccumulatedDeltaRangeUncertaintyMeters(1.0d);
        sGnssMeasurement.setMultipathIndicator(0);
        sGnssMeasurement.setState(17);
        sGnssMeasurement.setCarrierFrequencyHz(QUICK_FAKE_CARRIER_FREQUENCY);
        sGnssMeasurement.setAutomaticGainControlLevelInDb(20.0d);
        GnssMeasurement[] mArray = {sGnssMeasurement};
        sGnssClock.setTimeNanos(1);
        sGnssClock.setHardwareClockDiscontinuityCount(0);
        sGnssMeasurementsEvent = new GnssMeasurementsEvent(sGnssClock, mArray);
        LBSLog.i(TAG, false, "quickgps event %{public}d", Integer.valueOf(sGnssMeasurementsEvent.getMeasurements().size()));
        return sGnssMeasurementsEvent;
    }

    private void pauseTask() {
        LBSLog.i(TAG, false, "quickgps event stop.", new Object[0]);
        try {
            Thread.currentThread();
            Thread.sleep(50);
        } catch (Exception e) {
            LBSLog.i(TAG, false, "quickgps sleep Exception", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void cancelLocalTimerTask() {
        if (this.mQuickTtfftimer != null) {
            this.mQuickTtfftimer.cancel();
            this.mQuickTtfftimer = null;
            LBSLog.i(TAG, false, "mQuickTtfftimer.cancel", new Object[0]);
        }
    }

    public void onStartNavigating() {
        sendStartCommand();
        setNavigating(true);
    }

    public void onStopNavigating() {
        if (isRunning()) {
            sendStopCommand();
        }
        setNavigating(false);
    }

    public int reportLocationEx(Location location) {
        int sourceType = 0;
        if (location.getProvider() != null && !location.getProvider().equals("gps")) {
            try {
                sourceType = Integer.parseInt(location.getProvider());
            } catch (NumberFormatException e) {
                LBSLog.e(TAG, false, "unable to parse mSourceType %{public}s", location.getProvider());
            }
            if ((sourceType & 128) == 128) {
                this.mUtils.reportMeasurementData(this.mGnssProvider, getGnssMeasurements());
                this.mUtils.reportMeasurementData(this.mGnssProvider, getGnssMeasurements());
                pauseTask();
                location.setProvider(QUICK_GPS);
            } else {
                location.setProvider("gps");
            }
        }
        return sourceType;
    }

    public boolean checkLocationChanged(Location location, LocationManagerService.LocationProvider provider) {
        boolean isPassive = provider.isPassiveLocked();
        setQuickTtffLocation(location, isPassive);
        removeAllHwQuickTtff(isPassive ? "passive" : provider.getName());
        return true;
    }

    /* access modifiers changed from: package-private */
    public class LocalTimerTask extends TimerTask {
        LocalTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            HwQuickTTFFMonitor.this.cancelLocalTimerTask();
            if (!HwQuickTTFFMonitor.this.mIsReceive && HwQuickTTFFMonitor.this.isRunning) {
                LBSLog.e(HwQuickTTFFMonitor.TAG, false, " Not receive location:stop quickttff", new Object[0]);
                HwQuickTTFFMonitor.this.sendStopCommand();
            }
        }
    }
}
