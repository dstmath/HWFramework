package com.android.server.location;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.HwServiceFactory;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import libcore.io.IoUtils;

public class HwQuickTTFFMonitor {
    private static final float ACC_IN_WHITE_LIST = 50.0f;
    private static final int CN0S = 30;
    private static final boolean DBG = true;
    private static final boolean DEBUG = true;
    private static final int FIRST_SV = 0;
    private static final boolean IS_DEBUG_VERSION;
    public static final int LIST_TYPE_DISABLE = 4;
    public static final int LIST_TYPE_WHITE = 3;
    private static final int LOCATION_HAS_HWQUICKFIX = 256;
    private static final int METER_PER_SECOND = -700;
    private static final int QUICK_ENABLE_ONLY = 2;
    private static final String QUICK_GPS = "quickgps";
    private static final int QUICK_GPS_LOCATION = 2;
    private static final int QUICK_GPS_REQUEST_START = 1;
    private static final int QUICK_GPS_REQUEST_STOP = 3;
    private static final int QUICK_GPS_SESSION_START = 0;
    private static final int QUICK_GPS_SESSION_STOP = 4;
    private static final int QUICK_IS_ENABLE = 1;
    private static final String QUICK_START = "request_quick_ttff";
    private static final String QUICK_STOP = "stop_quick_ttff";
    private static final int QUICK_TTFF = 128;
    private static final int STATE_QTTFF = 17;
    private static final int STOP_TIME = 50;
    private static final int SVID = 24;
    private static final int SVID_WITH_FLAGS = 24;
    private static final int SV_FAKE_DATA = 1;
    private static final int SV_FAKE_DATA_ZERO = 0;
    private static final int SV_FAKE_HZ = 1575000000;
    private static final String TAG = "HwQuickTTFFMonitor-V2018.8.27";
    private static final String VERSION = "V2018.8.27";
    private static final int WAIT_TIME = 30000;
    private static GnssClock mGnssClock;
    private static GnssMeasurement mGnssMeasurement;
    private static GnssMeasurementsEvent mGnssMeasurementsEvent;
    private static HwQuickTTFFMonitor mHwQuickTTFFMonitorManager;
    private static ArrayList<String> mLocalQttffDisableList = new ArrayList<>(Arrays.asList(new String[]{"com.huawei.msdp", "com.huawei.HwOPServer", "com.yuedong.sport", "com.gotokeep.keep", "com.codoon.gps", "cn.ledongli.ldl", "com.sec.android.app.shealth", "com.mandian.android.dongdong", "com.imohoo.shanpao", "co.runner.app", "me.chunyu.Pedometer", "gz.lifesense.weidong", "com.lptiyu.tanke", "com.garmin.android.apps.connectmobile", "me.chunyu.ChunyuDoctor", "com.zhiyun.feel", "com.yelong.jibuqi", "com.fittimellc.fittime", "com.hupu.joggers", "com.runtastic.android.pro2", "com.lolaage.tbulu.tools", "com.cashwalk.cashwalk", "com.clue.android", "com.endomondo.android", "com.fitbit.FitbitMobile", "com.fitnesskeeper.runkeeper.pro", "com.garmin.android.apps.connectmobile", "com.google.android.apps.fitness", "com.huawei.health", "com.myfitnesspal.android", "com.popularapp.periodcalendar", "com.strava", "com.stt.android", "com.xiaomi.hm.health", "de.komoot.android", "fr.cnamts.it.activity", "io.yuka.android", "jp.co.mti.android.lunalunalite", "st.android.imsspublico", "za.co.myvirginactive"}));
    private static final Object mLock = new Object();
    private String DEBUG_PROPERTIES_FILE;
    private String Enable_Quickttff;
    private boolean isNavigating = false;
    private boolean isPermission = true;
    private boolean isQuickLocation = false;
    private boolean isQuickTTFFEnable = false;
    /* access modifiers changed from: private */
    public boolean isReceive = false;
    private volatile boolean isSatisfiedRequest = false;
    private boolean isSendStartCommand = false;
    private HashMap<String, String> mAppMonitorMap;
    private Context mContext;
    private int mEnableQuickttff;
    LocationProviderInterface mGnssProvider;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    private Properties mProperties;
    private Timer mQuickTTFFtimer;
    private ConcurrentHashMap<String, Long> mRemoveTimeMap;
    private ArraySet<String> m_QuickttffDisableList = new ArraySet<>();
    private ArraySet<String> m_accWhiteList = new ArraySet<>();
    /* access modifiers changed from: private */
    public boolean m_running = false;

    class LocalTimerTask extends TimerTask {
        LocalTimerTask() {
        }

        public void run() {
            HwQuickTTFFMonitor.this.cancelLocalTimerTask();
            if (!HwQuickTTFFMonitor.this.isReceive && HwQuickTTFFMonitor.this.m_running) {
                Log.e(HwQuickTTFFMonitor.TAG, " Not receive location :stop quickttff");
                HwQuickTTFFMonitor.this.sendStopCommand();
            }
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    private HwQuickTTFFMonitor(Context context, LocationProviderInterface provider) {
        this.mContext = context;
        this.mGnssProvider = provider;
        this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    public static HwQuickTTFFMonitor getInstance(Context context, LocationProviderInterface provider) {
        synchronized (mLock) {
            if (mHwQuickTTFFMonitorManager == null) {
                Log.i(TAG, "mHwQuickTTFFMonitorManager create.");
                mHwQuickTTFFMonitorManager = new HwQuickTTFFMonitor(context, provider);
            }
        }
        return mHwQuickTTFFMonitorManager;
    }

    public static HwQuickTTFFMonitor getMonitor() {
        return mHwQuickTTFFMonitorManager;
    }

    public void startMonitor() {
        quickTTFFEnable(configPath());
        if (!this.isQuickTTFFEnable) {
            Log.e(TAG, " QuickTTFF is  not enable" + this.Enable_Quickttff);
            return;
        }
        Log.i(TAG, "startMonitor");
        this.mAppMonitorMap = new HashMap<>();
        this.mRemoveTimeMap = new ConcurrentHashMap<>();
        this.m_running = false;
        this.isSendStartCommand = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0106, code lost:
        r1.isSatisfiedRequest = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x010a, code lost:
        if (r1.m_running != false) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x010e, code lost:
        if (r1.isNavigating == false) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0110, code lost:
        android.util.Log.i(TAG, "start HwQuickTTFF  when isNavigating");
        sendStartCommand();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x011b, code lost:
        return;
     */
    public void requestHwQuickTTFF(LocationRequest request, String packageName, String requestProvider, String id) {
        String requestAccuracy;
        String str = packageName;
        if (this.isQuickTTFFEnable) {
            if (!"gps".equals(request.getProvider())) {
                String str2 = requestProvider;
            } else if ("gps".equals(requestProvider) && this.isPermission) {
                String permissionStatus = String.valueOf(!this.m_QuickttffDisableList.contains(str));
                if (this.m_accWhiteList.contains(str)) {
                    requestAccuracy = String.valueOf(ACC_IN_WHITE_LIST);
                } else {
                    requestAccuracy = "null";
                }
                this.mHwLocationGpsLogServices.setQuickGpsParam(1, str + "," + permissionStatus + "," + requestAccuracy);
                if (isQuickttffDisableListApp(str)) {
                    Log.i(TAG, str + " is in black list  can not start Quickttff");
                    return;
                }
                if (this.mRemoveTimeMap.size() > 0) {
                    for (Map.Entry<String, Long> entry : this.mRemoveTimeMap.entrySet()) {
                        String packageNameRemove = entry.getKey();
                        if (System.currentTimeMillis() - entry.getValue().longValue() > 2000) {
                            this.mRemoveTimeMap.remove(packageNameRemove);
                        }
                    }
                    if (this.mRemoveTimeMap.containsKey(str)) {
                        Log.i(TAG, str + " can not start quickttff in 2s");
                        return;
                    }
                }
                synchronized (mLock) {
                    try {
                        try {
                            this.mAppMonitorMap.put(id, str);
                            Log.i(TAG, "requestHwQuickTTFF:" + str);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        String str3 = id;
                        throw th;
                    }
                }
            }
            String str4 = id;
        }
    }

    public void removeHwQuickTTFF(String packageName, String id, boolean isGps) {
        if (this.isQuickTTFFEnable) {
            if (isGps) {
                this.mRemoveTimeMap.put(packageName, Long.valueOf(System.currentTimeMillis()));
            }
            synchronized (mLock) {
                if (this.mAppMonitorMap.containsKey(id)) {
                    this.mAppMonitorMap.remove(id);
                    this.mHwLocationGpsLogServices.setQuickGpsParam(3, packageName);
                    Log.i(TAG, "removeHwQuickTTFF:" + packageName);
                    if (this.mAppMonitorMap.size() <= 0) {
                        if (this.m_running) {
                            Log.i(TAG, "removeHwQuickTTFF HwQuickTTFF STOP");
                            sendStopCommand();
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        android.util.Log.i(TAG, "removeAllHwQuickTTFF HwQuickTTFF STOP");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        if (r6.mAppMonitorMap.size() <= 0) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        r0 = r6.mAppMonitorMap.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
        if (r0.hasNext() == false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        r6.mRemoveTimeMap.put(r0.next().getValue(), java.lang.Long.valueOf(java.lang.System.currentTimeMillis()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        r6.mAppMonitorMap.clear();
        sendStopCommand();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
        return;
     */
    public void removeAllHwQuickTTFF(String provider) {
        if (this.isQuickTTFFEnable && !this.isQuickLocation && "gps".equals(provider)) {
            synchronized (mLock) {
                if (this.m_running) {
                    this.m_running = false;
                    this.isSendStartCommand = false;
                }
            }
        }
    }

    public boolean isLocationReportToApp(String packageName, String provider, Location location) {
        boolean isReportLocation = false;
        float acc = location.getAccuracy();
        if (!this.isQuickLocation || !"gps".equals(provider)) {
            return true;
        }
        if (this.mAppMonitorMap.containsValue(packageName)) {
            if (isAccSatisfied(packageName, acc)) {
                isReportLocation = true;
            } else {
                isReportLocation = false;
            }
        }
        Log.i(TAG, "isReportQuickTTFFLocation:" + isReportLocation + ", pkgname=" + packageName);
        return isReportLocation;
    }

    public void setQuickTTFFLocation(Location location, boolean passive) {
        if (this.isQuickTTFFEnable && !passive) {
            if ((QUICK_GPS.equals(location.getProvider()) || "gps".equals(location.getProvider())) && this.m_running) {
                this.mHwLocationGpsLogServices.setQuickGpsParam(2, String.valueOf(location.getAccuracy()));
            }
            if (QUICK_GPS.equals(location.getProvider())) {
                this.isQuickLocation = true;
                location.setProvider("gps");
                Bundle extras = location.getExtras();
                if (extras == null) {
                    extras = new Bundle();
                }
                Log.i(TAG, "SourceType" + location.getExtras().getInt("SourceType"));
                extras.putBoolean("QUICKGPS", true);
                location.setExtras(extras);
                Log.i(TAG, "is a QuickTTFFLocation");
                if (!this.isReceive) {
                    this.isReceive = true;
                    Log.i(TAG, "have receive QuickTTFFLocation");
                    cancelLocalTimerTask();
                }
            } else if ("gps".equals(location.getProvider())) {
                this.isQuickLocation = false;
            }
        }
    }

    public boolean isQuickLocation(Location location) {
        int mSourceType = 0;
        if (location.getExtras() != null) {
            mSourceType = location.getExtras().getInt("SourceType");
        }
        if ((mSourceType & 128) == 128) {
            return true;
        }
        return false;
    }

    public boolean isRunning() {
        return this.m_running;
    }

    public void setPermission(boolean permission) {
        this.isPermission = permission;
    }

    public void setNavigating(boolean Navigating) {
        this.isNavigating = Navigating;
    }

    private boolean isQuickttffDisableListApp(String appName) {
        if (this.m_QuickttffDisableList == null) {
            return false;
        }
        if (this.m_QuickttffDisableList.contains(appName)) {
            Log.i(TAG, appName + "is not WhiteApp");
            return true;
        }
        Log.i(TAG, appName + "is  WhiteApp");
        return false;
    }

    private boolean isAccSatisfied(String appName, float acc) {
        if (this.m_QuickttffDisableList == null || !this.m_accWhiteList.contains(appName)) {
            return true;
        }
        Log.i(TAG, appName + "is inAccList ");
        if (ACC_IN_WHITE_LIST >= acc) {
            Log.d(TAG, "Satisfied with Acc50.0" + acc);
            return true;
        }
        Log.d(TAG, " Not Satisfied with Acc50.0" + acc);
        return false;
    }

    public void updateAccWhiteList(List<String> accWhiteList) {
        Log.d(TAG, "AccWhiteList " + accWhiteList.size());
        Iterator<String> it = accWhiteList.iterator();
        while (it.hasNext()) {
            Log.d(TAG, "AccWhiteList " + it.next());
        }
        this.m_accWhiteList.clear();
        this.m_accWhiteList.addAll(accWhiteList);
        Log.d(TAG, "accWhiteList" + accWhiteList);
    }

    public void updateDisableList(List<String> QuickttffDisableList) {
        Log.d(TAG, "DisableList " + QuickttffDisableList.size());
        Iterator<String> it = QuickttffDisableList.iterator();
        while (it.hasNext()) {
            Log.d(TAG, "DisableList " + it.next());
        }
        this.m_QuickttffDisableList.clear();
        this.m_QuickttffDisableList.addAll(mLocalQttffDisableList);
        this.m_QuickttffDisableList.addAll(QuickttffDisableList);
        Log.d(TAG, " QuickttffDisableList " + QuickttffDisableList);
    }

    public void quickTTFFEnable(String filename) {
        FileInputStream stream;
        this.mProperties = new Properties();
        try {
            stream = null;
            stream = new FileInputStream(new File(filename));
            this.mProperties.load(stream);
            IoUtils.closeQuietly(stream);
        } catch (IOException e) {
            Log.d(TAG, "Could not open higeo configuration file " + filename);
            this.isQuickTTFFEnable = false;
        } catch (Throwable th) {
            IoUtils.closeQuietly(stream);
            throw th;
        }
        this.Enable_Quickttff = this.mProperties.getProperty("higeo_enable_quickttff");
        if (this.Enable_Quickttff != null && !BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(this.Enable_Quickttff)) {
            try {
                this.mEnableQuickttff = Integer.parseInt(this.Enable_Quickttff);
            } catch (NumberFormatException e2) {
                Log.e(TAG, "unable to parse Enable_Quickttff: " + this.Enable_Quickttff);
                this.isQuickTTFFEnable = false;
            }
        }
        if (this.mEnableQuickttff == 1) {
            this.isQuickTTFFEnable = true;
            Log.e(TAG, " QuickTTFF is enable" + this.Enable_Quickttff);
        } else if (this.mEnableQuickttff == 2) {
            this.isQuickTTFFEnable = true;
            Log.e(TAG, " QuickTTFF only is enable" + this.Enable_Quickttff);
        } else {
            this.isQuickTTFFEnable = false;
            Log.e(TAG, " QuickTTFF is  not enable" + this.Enable_Quickttff);
        }
    }

    private boolean isBetaUser() {
        return IS_DEBUG_VERSION;
    }

    private String configPath() {
        if (isBetaUser()) {
            Log.i(TAG, "This is beta user");
            File file = HwCfgFilePolicy.getCfgFile("xml/higeo_beta.conf", 0);
            if (file != null) {
                this.DEBUG_PROPERTIES_FILE = file.getPath();
                Log.d(TAG, "configPath is" + this.DEBUG_PROPERTIES_FILE);
            } else {
                Log.d(TAG, "configPath is not /cust_spec/xml/  ");
                this.DEBUG_PROPERTIES_FILE = "/odm/etc/higeo_beta.conf";
            }
        } else {
            Log.i(TAG, "This is not beta user");
            File file2 = HwCfgFilePolicy.getCfgFile("xml/higeo.conf", 0);
            if (file2 != null) {
                this.DEBUG_PROPERTIES_FILE = file2.getPath();
                Log.d(TAG, "configPath is" + this.DEBUG_PROPERTIES_FILE);
            } else {
                Log.d(TAG, "configPath is not /cust_spec/xml/  ");
                this.DEBUG_PROPERTIES_FILE = "/odm/etc/higeo.conf";
            }
        }
        Log.d(TAG, "configPath is" + this.DEBUG_PROPERTIES_FILE);
        return this.DEBUG_PROPERTIES_FILE;
    }

    public boolean isReport(Location location) {
        if (this.m_running || !QUICK_GPS.equals(location.getProvider())) {
            return true;
        }
        return false;
    }

    public void clearAppMonitorMap() {
        this.mAppMonitorMap.clear();
        Log.i(TAG, "clearAppMonitorMap");
    }

    public void sendStartCommand() {
        if (this.m_running) {
            Log.i(TAG, "HwQuickTTFF has be started before.");
            return;
        }
        if (!this.isSendStartCommand && this.isSatisfiedRequest) {
            this.isReceive = false;
            timerTask();
            this.m_running = true;
            this.isSendStartCommand = true;
            Log.i(TAG, "HwQuickTTFF Start");
            this.mHwLocationGpsLogServices.setQuickGpsParam(0, QUICK_START);
            this.mGnssProvider.sendExtraCommand(QUICK_START, null);
            Log.e(TAG, "isSendStartCommand" + this.isSendStartCommand);
        }
    }

    public void sendStopCommand() {
        this.m_running = false;
        this.isSendStartCommand = false;
        this.isQuickLocation = false;
        this.isReceive = false;
        this.isSatisfiedRequest = false;
        Log.i(TAG, "HwQuickTTFF STOP");
        this.mHwLocationGpsLogServices.setQuickGpsParam(4, "ALL");
        this.mGnssProvider.sendExtraCommand(QUICK_STOP, null);
        cancelLocalTimerTask();
    }

    public void timerTask() {
        this.mQuickTTFFtimer = new Timer();
        this.mQuickTTFFtimer.schedule(new LocalTimerTask(), 30000);
    }

    public static GnssMeasurementsEvent getGnssMeasurements() {
        Log.i(TAG, "quickgps event start. ");
        mGnssMeasurement = new GnssMeasurement();
        mGnssClock = new GnssClock();
        mGnssMeasurement.setSvid(24);
        mGnssMeasurement.setConstellationType(1);
        mGnssMeasurement.setReceivedSvTimeNanos(1);
        mGnssMeasurement.setReceivedSvTimeUncertaintyNanos(1);
        mGnssMeasurement.setTimeOffsetNanos(0.0d);
        mGnssMeasurement.setCn0DbHz(30.0d);
        mGnssMeasurement.setPseudorangeRateMetersPerSecond(-700.0d);
        mGnssMeasurement.setPseudorangeRateUncertaintyMetersPerSecond(1.0d);
        mGnssMeasurement.setAccumulatedDeltaRangeState(1);
        mGnssMeasurement.setAccumulatedDeltaRangeMeters(1.0d);
        mGnssMeasurement.setAccumulatedDeltaRangeUncertaintyMeters(1.0d);
        mGnssMeasurement.setCarrierPhase(0.0d);
        mGnssMeasurement.setCarrierFrequencyHz(1.57500006E9f);
        mGnssMeasurement.setCarrierPhaseUncertainty(1.0d);
        mGnssMeasurement.setSnrInDb(1.0d);
        mGnssMeasurement.setAutomaticGainControlLevelInDb(1.0d);
        mGnssMeasurement.setMultipathIndicator(0);
        mGnssMeasurement.setState(17);
        GnssMeasurement[] mArray = {mGnssMeasurement};
        mGnssClock.setTimeNanos(1);
        mGnssClock.setHardwareClockDiscontinuityCount(0);
        mGnssMeasurementsEvent = new GnssMeasurementsEvent(mGnssClock, mArray);
        Log.i(TAG, "quickgps event " + mGnssMeasurementsEvent.getMeasurements().size());
        return mGnssMeasurementsEvent;
    }

    public static void pauseTask() {
        Log.i(TAG, "quickgps event stop. ");
        try {
            Thread.currentThread();
            Thread.sleep(50);
        } catch (Exception e) {
            Log.i(TAG, "quickgps sleep Exception");
        }
    }

    /* access modifiers changed from: private */
    public synchronized void cancelLocalTimerTask() {
        if (this.mQuickTTFFtimer != null) {
            this.mQuickTTFFtimer.cancel();
            this.mQuickTTFFtimer = null;
            Log.i(TAG, "mQuickTTFFtimer.cancel");
        }
    }
}
